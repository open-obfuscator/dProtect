package dprotect.obfuscation.strings;

import dprotect.obfuscation.info.ObfuscationInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.visitor.ClassVisitor;
import proguard.classfile.visitor.*;

public class StringObfuscationMarker
implements   ClassVisitor,
             MemberVisitor
{

    private static final Logger logger = LogManager.getLogger(StringObfuscationMarker.class);

    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) { }

    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        // Flag the class as it enables to quickly determine whether
        // it should be considered by the obfuscation pass
        ObfuscationInfo.getObfuscationInfo(programClass).encodeStrings = true;
    }

    // Implementations for MemberVisitor.

    @Override
    public void visitAnyMember(Clazz clazz, Member member) { }

    @Override
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        ObfuscationInfo.getObfuscationInfo(programMethod).encodeStrings = true;
    }

    @Override
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        ObfuscationInfo.getObfuscationInfo(programField).encodeStrings = true;
    }

}
