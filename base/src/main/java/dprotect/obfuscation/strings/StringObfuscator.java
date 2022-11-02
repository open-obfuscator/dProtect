package dprotect.obfuscation.strings;

import dprotect.obfuscation.info.ObfuscationInfo;
import dprotect.runtime.strings.StringEncoding;
import dprotect.runtime.util.Helpers;
import dprotect.runtime.util.Loader;
import dprotect.util.MethodCopier;
import dprotect.obfuscation.controlflow.ControlFlowObfuscationInfo;
import dprotect.obfuscation.arithmetic.ArithmeticObfuscationInfo;
import static dprotect.ObfuscationClassSpecification.Level;

import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.visitor.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.Instruction;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.visitor.*;
import proguard.util.*;

public class StringObfuscator
implements   ClassVisitor,
             AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor
{
    private static final Logger logger = LogManager.getLogger(StringObfuscator.class);
    public  static final char   KEY_ID = 0xFAFB;

    private final ClassPool                   runtime;
    private       ConstantPoolEditor          constantPoolEditor;
    private       String                      replacement         = null;
    private final CodeAttributeEditor         codeAttributeEditor = new CodeAttributeEditor();
    private final Random                      rand;
    private       char                        currentKey          = 0;
    private       boolean                     isMethodEligible    = false;
    private       boolean                     isConstantEligible  = false;
    private final StringMatcher               filter;
    private       StringEncoding.EncodingPair selectedEncoding;

    public StringObfuscator(List<String> strings, int seed)
    {
        runtime = Loader.getRuntimeClasses();
        rand    = new Random((long)seed);

        filter = strings != null && !strings.isEmpty() ?
                 new ListParser(new NameParser()).parse(strings) :
                 new ConstantMatcher(false);
    }


    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz)
    {

        int methodId = rand.nextInt(StringEncoding.ENCODING_METHODS.size());
        selectedEncoding = StringEncoding.ENCODING_METHODS.get(methodId);

        try {
            addDecodeMethod((ProgramClass)clazz,
                            runtime.getClass(Helpers.getNormalizedClassName(StringEncoding.class)));

            clazz.methodsAccept(new AllAttributeVisitor(this));
        } catch (Exception e) {
            logger.error("Can't add the decoding method: {}", e);
        }
    }


    // Implementations for AttributeVisitor.

    @Override
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}

    @Override
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        isMethodEligible = ObfuscationInfo.getObfuscationInfo(method).encodeStrings;

        constantPoolEditor = new ConstantPoolEditor((ProgramClass)clazz);
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.accept(clazz, method, codeAttributeEditor);
    }

    // Implementations for InstructionVisitor.

    @Override
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}

    @Override
    public void visitConstantInstruction(Clazz               clazz,
                                         Method              method,
                                         CodeAttribute       codeAttribute,
                                         int                 offset,
                                         ConstantInstruction constantInstruction)
    {
        if (constantInstruction.opcode == Instruction.OP_LDC ||
            constantInstruction.opcode == Instruction.OP_LDC_W)
        {
            replacement = null;
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
            if (replacement != null)
            {
                constantInstruction.constantIndex = constantPoolEditor.addStringConstant(replacement);

                Method meth = clazz.findMethod(selectedEncoding.decode.getName(), "(Ljava/lang/String;)Ljava/lang/String;");
                int index = constantPoolEditor.addMethodrefConstant(clazz, meth);
                Instruction replacementInstruction = new ConstantInstruction(Instruction.OP_INVOKESTATIC, index);
                codeAttributeEditor.replaceInstruction(offset, constantInstruction);
                codeAttributeEditor.insertAfterInstruction(offset, replacementInstruction);
            }
        }
    }

    // Implementations for ConstantVisitor.

    @Override
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(stringConstant);
        isConstantEligible = (info != null && info.encodeStrings);
        clazz.constantPoolEntryAccept(stringConstant.u2stringIndex, this);
    }

    @Override
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        String original = utf8Constant.getString();
        boolean shouldProtect = isMethodEligible                                ||
                                (!isMethodEligible && filter.matches(original)) ||
                                (!isMethodEligible && isConstantEligible);

        if (!shouldProtect) {
            return;
        }

        try {
            replacement = (String)selectedEncoding.encode.invoke(/* static */null, original, currentKey);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }


    @Override
    public void visitAnyConstant(Clazz clazz, Constant constant) {}

    void addDecodeMethod(ProgramClass target, Clazz src) {
        ClassBuilder builder = new ClassBuilder(target);
        String decodeName = selectedEncoding.decode.getName();
        ProgramMethod meth = builder.addAndReturnMethod(
                AccessConstants.PUBLIC | AccessConstants.STATIC,
                decodeName, "(Ljava/lang/String;)Ljava/lang/String;");

        Method srcDecoding = src.findMethod(decodeName, "(Ljava/lang/String;)Ljava/lang/String;");

        if (srcDecoding == null)
        {
            logger.fatal("Can't find {}", decodeName);
            return;
        }

        ProgramClass targetProgramClass = builder.getProgramClass();
        MethodCopier.copy(targetProgramClass, meth, src, srcDecoding);

        Method insertedDecode = target.findMethod(decodeName, "(Ljava/lang/String;)Ljava/lang/String;");
        markDecodeMethod(insertedDecode);
        currentKey = (char)rand.nextInt(0xFFFF);

        insertedDecode.accept(target, new AllAttributeVisitor(
                                      new KeyChanger(currentKey)));
    }

    void markDecodeMethod(Method method) {
        ObfuscationInfo.setMethodObfuscationInfo(method);
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(method);
        info.controlflow = new ControlFlowObfuscationInfo(Level.LOW);
        info.arithmetic  = new ArithmeticObfuscationInfo(Level.LOW);
    }

    static private class KeyChanger
    implements           AttributeVisitor,
                         InstructionVisitor,
                         ConstantVisitor
    {

        private       ConstantPoolEditor  constantPoolEditor;
        private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
        private final char                newKey;
        private       boolean             isKey = false;

        public KeyChanger(char newKey)
        {
            this.newKey = newKey;
        }

        // Implementations for AttributeVisitor.

        @Override
        public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}

        @Override
        public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
        {
            this.constantPoolEditor = new ConstantPoolEditor((ProgramClass)clazz);
            codeAttributeEditor.reset(codeAttribute.u4codeLength);
            codeAttribute.instructionsAccept(clazz, method, this);
            codeAttribute.accept(clazz, method, codeAttributeEditor);
        }

        // Implementations for InstructionVisitor.
        @Override
        public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) { }

        @Override
        public void visitConstantInstruction(Clazz               clazz,
                                             Method              method,
                                             CodeAttribute       codeAttribute,
                                             int                 offset,
                                             ConstantInstruction constantInstruction)
        {
            if (constantInstruction.opcode == Instruction.OP_LDC ||
                constantInstruction.opcode == Instruction.OP_LDC_W)
            {
                isKey = false;
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                if (isKey)
                {
                    int index = constantPoolEditor.addIntegerConstant(newKey);
                    codeAttributeEditor.replaceInstruction(
                            offset, new ConstantInstruction(constantInstruction.opcode, index));
                }
            }
        }

        // Implementations for ConstantVisitor.

        @Override
        public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
        {
            isKey = (integerConstant.getValue() == KEY_ID);
        }

    }
}
