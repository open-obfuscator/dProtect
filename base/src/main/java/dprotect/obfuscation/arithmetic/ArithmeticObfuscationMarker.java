package dprotect.obfuscation.arithmetic;

import dprotect.ArithmeticObfuscationClassSpecification;
import dprotect.ObfuscationClassSpecification;
import dprotect.obfuscation.info.ObfuscationInfo;
import proguard.classfile.visitor.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;

public class ArithmeticObfuscationMarker
implements   ClassVisitor,
             MemberVisitor
{

    private static final Logger logger = LogManager.getLogger(ArithmeticObfuscationMarker.class);
    private        final ArithmeticObfuscationClassSpecification spec;

    public ArithmeticObfuscationMarker(ObfuscationClassSpecification spec)
    {
        this.spec = (ArithmeticObfuscationClassSpecification)spec;
    }

    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) { }

    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programClass);
        if (info.arithmetic == null)
        {
            info.arithmetic = new ArithmeticObfuscationInfo(spec.obfuscationLvl);
        }
    }

    // Implementations for MemberVisitor.

    @Override
    public void visitAnyMember(Clazz clazz, Member member) { }

    @Override
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programMethod);
        if (info.arithmetic == null)
        {
            info.arithmetic = new ArithmeticObfuscationInfo(spec.obfuscationLvl);
        }
        info.arithmetic.skipFloat = spec.skipFloat;
    }

    @Override
    public void visitProgramField(ProgramClass programClass, ProgramField programField) { }

}
