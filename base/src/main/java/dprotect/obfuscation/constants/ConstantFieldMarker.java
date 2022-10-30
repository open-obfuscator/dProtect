package dprotect.obfuscation.constants;

import dprotect.obfuscation.info.ObfuscationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.attribute.CodeAttribute;

// TODO(romain): Not yet implemented but it aims at marking constants
//               associated with user-flagged fields (like for the strings)
public class ConstantFieldMarker
implements   InstructionVisitor,
             ConstantVisitor
{
    private static final Logger logger = LogManager.getLogger(ConstantFieldMarker.class);

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
    }


    // Implementations for ConstantVisitor.

    @Override
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
    }

    @Override
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
    }

    @Override
    public void visitAnyConstant(Clazz clazz, Constant constant) {}

}
