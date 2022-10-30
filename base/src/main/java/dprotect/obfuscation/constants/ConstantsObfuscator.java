package dprotect.obfuscation.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.Constant;
import proguard.classfile.constant.visitor.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.visitor.*;

public class ConstantsObfuscator
implements   ClassVisitor,
             AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor
{
    private static final Logger logger = LogManager.getLogger(ConstantsObfuscator.class);

    private static final String              OPAQUE_CONSTANTS_ARRAY_PREFIX = "OPAQUE_CONSTANTS_ARRAY";
    private final        CodeAttributeEditor codeAttributeEditor           = new CodeAttributeEditor();
    private              ProgramField        arrayField                    = null;
    private              Long                longVal                       = null;
    private              ArrayList<Long>     constants                     = new ArrayList<Long>();
    private              HashMap<Long, Long> constantsKeys                 = new HashMap<Long, Long>();
    private final        Random              rand;

    public ConstantsObfuscator(int seed)
    {
        rand = new Random((long)seed);
    }

    // Implementations for ClassVisitor.
    @Override
    public void visitAnyClass(Clazz clazz) {
        if (clazz instanceof ProgramClass) {
            visitProgramClass((ProgramClass)clazz);
        }
    }


    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        if ((programClass.getAccessFlags() & AccessConstants.INTERFACE) != 0 ||
            (programClass.getAccessFlags() & AccessConstants.ABSTRACT) != 0)
        {
            return;
        }

        constants.clear();
        constantsKeys.clear();

        ClassBuilder classBuilder = new ClassBuilder(programClass);

        arrayField = classBuilder.addAndReturnField(AccessConstants.PRIVATE | AccessConstants.STATIC,
                                                    OPAQUE_CONSTANTS_ARRAY_PREFIX, "[J");

        programClass.accept(new AllMethodVisitor(
                            new AllAttributeVisitor(
                            this)));

        if (!constants.isEmpty()) {
            new InitializerEditor(programClass).addStaticInitializerInstructions(/*mergeIntoExistingInitializer=*/true,
                ____ -> {
                    // Allocate space for the array that holds the constants
                    ____.ldc(constants.size())
                        .newarray(Instruction.ARRAY_T_LONG)
                        .putstatic(programClass, arrayField);

                    // Push the values
                    for (int i = 0; i < constants.size(); ++i) {
                        Long value   = constants.get(i);
                        Long key     = constantsKeys.get(value);
                        Long encoded = value ^ key;
                        ____.getstatic(programClass, arrayField)
                            .sipush(i)
                            .ldc2_w(encoded)
                            .lastore();
                    }
            });
        }
    }


    // Implementations for AttributeVisitor.

    @Override
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}

    @Override
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.accept(clazz, method, codeAttributeEditor);
    }

    // Implementations for InstructionVisitor.

    @Override
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}

    @Override
    public void visitSimpleInstruction(Clazz             clazz,
                                       Method            method,
                                       CodeAttribute     codeAttribute,
                                       int               offset,
                                       SimpleInstruction instruction)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder((ProgramClass)clazz);
        byte opcode = instruction.opcode;
        switch (opcode) {
            case Instruction.OP_BIPUSH:
                {
                    int value = instruction.constant;
                    int index = getOrInsert((long)value);
                    Long key  = constantsKeys.get(Long.valueOf(value));
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                                .ldc(index)
                                .laload()
                                .l2i()
                                .ldc(key.intValue())
                                .ixor()
                                .__());
                    break;
                }

            case Instruction.OP_ICONST_0: // 0 value
                {
                    int value = instruction.constant;
                    int index = getOrInsert((long)value);
                    Long key  = constantsKeys.get(Long.valueOf(value));
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                                .ldc(index)
                                .laload()
                                .l2i()
                                .ldc(key.intValue())
                                .ixor()
                                .__());

                    break;
                }

            case Instruction.OP_ICONST_1: // 1 value
                {
                    int value = instruction.constant;
                    int index = getOrInsert((long)value);
                    Long key  = constantsKeys.get(Long.valueOf(value));
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                                .ldc(index)
                                .laload()
                                .l2i()
                                .ldc(key.intValue())
                                .ixor()
                                .__());
                    break;
                }

            case Instruction.OP_ICONST_2: // 2 value
                {
                    int value = instruction.constant;
                    int index = getOrInsert((long)value);
                    Long key  = constantsKeys.get(Long.valueOf(value));
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                                .ldc(index)
                                .laload()
                                .l2i()
                                .ldc(key.intValue())
                                .ixor()
                                .__());
                    break;
                }

            case Instruction.OP_ICONST_3: // 3 value
                {
                    int value = instruction.constant;
                    int index = getOrInsert((long)value);
                    Long key  = constantsKeys.get(Long.valueOf(value));
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                                .ldc(index)
                                .laload()
                                .l2i()
                                .ldc(key.intValue())
                                .ixor()
                                .__());
                    break;
                }
        }
    }

    @Override
    public void visitConstantInstruction(Clazz               clazz,
                                         Method              method,
                                         CodeAttribute       codeAttribute,
                                         int                 offset,
                                         ConstantInstruction constantInstruction)
    {
        longVal = null;
        clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
        if (longVal == null) {
            return;
        }

        int index = getOrInsert(longVal);
        Long key  = constantsKeys.get(longVal);
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder((ProgramClass)clazz);

        switch(constantInstruction.opcode) {
            case Instruction.OP_LDC:
                {
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                            .ldc(index)
                            .laload()
                            .l2i()
                            .ldc(key.intValue())
                            .ixor()
                            .__());
                    break;
                }

            case Instruction.OP_LDC_W:
                {
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                            .ldc(index)
                            .laload()
                            .l2i()
                            .ldc(key.intValue())
                            .ixor()
                            .__());
                    break;
                }

            case Instruction.OP_LDC2_W:
                {
                    codeAttributeEditor.replaceInstruction(offset,
                            ____.getstatic(clazz, arrayField)
                            .ldc(index)
                            .laload()
                            .ldc2_w(key.longValue())
                            .lxor()
                            .__());
                    break;
                }
        }
    }

    // Implementations for ConstantVisitor.

    @Override
    public void visitIntegerConstant(Clazz clazz, IntegerConstant constant)
    {
        longVal = (long)constant.getValue();
    }

    @Override
    public void visitLongConstant(Clazz clazz, LongConstant constant)
    {
        longVal = constant.getValue();
    }

    @Override
    public void visitAnyConstant(Clazz clazz, Constant constant) {}


    // Helpers

    int getOrInsert(Long value)
    {
        int index = constants.indexOf(value);
        if (index == -1)
        {
            index = constants.size();
            constants.add(value);
            int key = rand.nextInt(Integer.MAX_VALUE - 1);
            constantsKeys.put(value, (long)key);
        }
        return index;
    }
}
