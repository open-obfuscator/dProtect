package dprotect.obfuscation.info;

import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.MemberVisitor;

import proguard.classfile.constant.Constant;

public class ObfuscationInfoSetter
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor
{
    private final boolean overwrite;

    public ObfuscationInfoSetter()
    {
        this(false);
    }

    public ObfuscationInfoSetter(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    // Implementation for ClassVisitor

    @Override
    public void visitAnyClass(Clazz clazz) { }

    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        if (programClass.getObfuscationInfo() == null || overwrite)
        {
            ObfuscationInfo.setClassObfuscationInfo(programClass);
        }
        programClass.constantPoolEntriesAccept(this);

    }

    // Implementation for MemberVisitor

    @Override
    public void visitAnyMember(Clazz clazz, Member member) { }

    @Override
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (programMethod.getObfuscationInfo() == null || overwrite)
        {
            ObfuscationInfo.setMethodObfuscationInfo(programMethod);
        }
    }

    @Override
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (programField.getObfuscationInfo() == null || overwrite)
        {
            ObfuscationInfo.setFieldObfuscationInfo(programField);
        }
    }

    @Override
    public void visitAnyConstant(Clazz clazz, Constant constant)
    {
        if (constant.getObfuscationInfo() == null || overwrite)
        {
            ObfuscationInfo.setConstantObfuscationInfo(constant);
        }
    }

}
