package dprotect.obfuscation.arithmetic;
import dprotect.obfuscation.info.ObfuscationInfo;
import static dprotect.ObfuscationClassSpecification.Level;

import proguard.classfile.visitor.MemberVisitor;
import proguard.classfile.Clazz;
import proguard.classfile.Member;
import proguard.classfile.ProgramClass;
import proguard.classfile.ProgramMethod;

public class ArithmeticObfuscationFilter
implements   MemberVisitor
{
    static final int NONE_OR_LOW_ROUNDS = 1;
    static final int MEDIUM_ROUNDS      = 2;
    static final int HIGH_ROUNDS        = 3;

    private final MemberVisitor visitor;
    public ArithmeticObfuscationFilter(MemberVisitor visitor)
    {
        this.visitor = visitor;
    }

    @Override
    public void visitAnyMember(Clazz clazz, Member member) { }


    @Override
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        ObfuscationInfo info = ObfuscationInfo.getObfuscationInfo(programMethod);
        if (info == null || info.arithmetic == null)
        {
            return;
        }
        Level level = info.arithmetic.level;

        int passes = level == Level.NONE   ? NONE_OR_LOW_ROUNDS :
                     level == Level.LOW    ? NONE_OR_LOW_ROUNDS :
                     level == Level.MEDIUM ? MEDIUM_ROUNDS :
                     level == Level.HIGH   ? HIGH_ROUNDS : 0;

        for (int i = 0; i < passes; ++i)
        {
            visitor.visitProgramMethod(programClass, programMethod);
        }
    }
}
