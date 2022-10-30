package dprotect;

import proguard.ClassSpecificationVisitorFactory;

import proguard.classfile.visitor.*;

import java.util.List;
import java.util.function.Function;

public class ClassObfuSpecVisitorFactory extends ClassSpecificationVisitorFactory
{
    public ClassPoolVisitor createClassPoolVisitor(List classSpecifications,
                                                   Function<ObfuscationClassSpecification, ClassVisitor>  classCreator,
                                                   Function<ObfuscationClassSpecification, MemberVisitor> memberCreator)
    {
        MultiClassPoolVisitor multiClassPoolVisitor = new MultiClassPoolVisitor();

        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                ObfuscationClassSpecification classSpecification =
                    (ObfuscationClassSpecification)classSpecifications.get(index);

                ClassVisitor classVisitor = null;
                MemberVisitor memberVisitor = null;

                if (classCreator != null)
                {
                    classVisitor = classCreator.apply(classSpecification);
                }

                if (memberCreator != null)
                {
                    memberVisitor = memberCreator.apply(classSpecification);
                }

                multiClassPoolVisitor.addClassPoolVisitor(
                    super.createClassPoolVisitor(classSpecification,
                                                 classVisitor,
                                                 memberVisitor,
                                                 memberVisitor,
                                                 null,
                                                 null));
            }
        }
        return multiClassPoolVisitor;

    }
}
