package dprotect.obfuscation.strings;

import dprotect.obfuscation.info.ObfuscationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.attribute.CodeAttribute;

// NOTE(romain): I think we could improve this code using the PartialEvaluator
public class StringFieldMarker
implements   InstructionVisitor,
             ConstantVisitor
{
    private static final Logger logger = LogManager.getLogger(StringFieldMarker.class);

    private StringConstant stringConstant;

    // Implementations for MemberVisitor.

    @Override
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) { }

    @Override
    public void visitConstantInstruction(Clazz               clazz,
                                         Method              method,
                                         CodeAttribute       codeAttribute,
                                         int                 offset,
                                         ConstantInstruction constantInstruction)
    {
        if (constantInstruction.opcode == Instruction.OP_LDC      ||
            constantInstruction.opcode == Instruction.OP_LDC_W    ||
            constantInstruction.opcode == Instruction.OP_PUTFIELD ||
            constantInstruction.opcode == Instruction.OP_PUTSTATIC)
        {
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
        }
    }


    // Implementations for ConstantVisitor.

    @Override
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        Field field = fieldrefConstant.referencedField;
        if (ObfuscationInfo.getObfuscationInfo(field).encodeStrings &&
            stringConstant != null)
        {
            ObfuscationInfo.getObfuscationInfo(stringConstant).encodeStrings = true;
        }
        this.stringConstant = null;
    }

    @Override
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        this.stringConstant = stringConstant;
    }

    @Override
    public void visitAnyConstant(Clazz clazz, Constant constant) {}

}
