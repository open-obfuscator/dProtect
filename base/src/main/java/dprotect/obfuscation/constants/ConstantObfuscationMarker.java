package dprotect.obfuscation.constants;

import dprotect.ConstantObfuscationClassSpecification;
import dprotect.ObfuscationClassSpecification;

import dprotect.obfuscation.info.ObfuscationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.*;

public class ConstantObfuscationMarker
implements   ClassVisitor,
             MemberVisitor
{
    private static final Logger logger = LogManager.getLogger(ConstantObfuscationMarker.class);
    private        final ConstantObfuscationClassSpecification spec;

    public ConstantObfuscationMarker(ObfuscationClassSpecification spec)
    {
        this.spec = (ConstantObfuscationClassSpecification)spec;
    }

    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) { }

    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        // Flag the class as it enables to quickly determine whether
        // it should be considered by the obfuscation pass
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programClass);
        if (info.constants == null)
        {
            info.constants = new ConstantObfuscationInfo(spec.obfuscationLvl);
        }
    }

    // Implementations for MemberVisitor.

    @Override
    public void visitAnyMember(Clazz clazz, Member member) { }

    @Override
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programMethod);
        if (info.constants == null)
        {
            info.constants = new ConstantObfuscationInfo(spec.obfuscationLvl);
        }
    }

    @Override
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programField);
        if (info.constants == null)
        {
            info.constants = new ConstantObfuscationInfo(spec.obfuscationLvl);
        }
    }

}
