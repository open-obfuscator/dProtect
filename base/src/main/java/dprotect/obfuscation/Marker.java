package dprotect.obfuscation;

import dprotect.ClassObfuSpecVisitorFactory;
import dprotect.Configuration;
import dprotect.obfuscation.info.ObfuscationInfoSetter;
import dprotect.obfuscation.strings.StringObfuscationMarker;
import dprotect.obfuscation.arithmetic.ArithmeticObfuscationMarker;
import dprotect.obfuscation.constants.ConstantObfuscationMarker;
import dprotect.obfuscation.controlflow.ControlFlowObfuscationMarker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.AppView;
import proguard.classfile.visitor.*;
import proguard.pass.Pass;
import proguard.ClassSpecificationVisitorFactory;


public class Marker implements Pass
{
    private static final Logger logger = LogManager.getLogger(Marker.class);

    private final Configuration configuration;

    public Marker(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void execute(AppView appView)
    {
        logger.info("Marking classes and class members to be dprotect-obfuscated ...");

        // Attach obfuscation info to all the classes and class member
        ObfuscationInfoSetter obfSetter = new ObfuscationInfoSetter();
        appView.programClassPool.classesAccept(obfSetter);
        appView.programClassPool.classesAccept(new AllMemberVisitor(obfSetter));

        MultiClassPoolVisitor classPoolVisitor =
            new MultiClassPoolVisitor(
                createStringObfuscationMarker(configuration),
                createCFObfuscationMarker(configuration),
                createArithmeticObfuscationMarker(configuration),
                createConstantsObfuscationMarker(configuration)
            );

        appView.programClassPool.accept(classPoolVisitor);
        appView.libraryClassPool.accept(classPoolVisitor);
    }

    // Marker factory

    private ClassPoolVisitor createStringObfuscationMarker(Configuration configuration)
    {
        StringObfuscationMarker marker = new StringObfuscationMarker();

        return new ClassSpecificationVisitorFactory()
            .createClassPoolVisitor(configuration.obfuscateStrings,
                                    marker, marker);
    }

    private ClassPoolVisitor createArithmeticObfuscationMarker(Configuration configuration)
    {
        return new ClassObfuSpecVisitorFactory()
            .createClassPoolVisitor(configuration.obfuscateArithmetic,
                                    /* Class Visitor */
                                    (spec) -> { return new ArithmeticObfuscationMarker(spec); },
                                    /* Member Visitor */
                                    (spec) -> { return new ArithmeticObfuscationMarker(spec); });
    }

    private ClassPoolVisitor createConstantsObfuscationMarker(Configuration configuration)
    {
        return new ClassObfuSpecVisitorFactory()
            .createClassPoolVisitor(configuration.obfuscateConstants,
                                    /* Class Visitor */
                                    (spec) -> { return new ConstantObfuscationMarker(spec); },
                                    /* Member Visitor */
                                    (spec) -> { return new ConstantObfuscationMarker(spec); });
    }

    private ClassPoolVisitor createCFObfuscationMarker(Configuration configuration)
    {
        return new ClassObfuSpecVisitorFactory()
            .createClassPoolVisitor(configuration.obfuscateControlFlow,
                                    /* Class Visitor */
                                    (spec) -> { return new ControlFlowObfuscationMarker(spec); },
                                    /* Member Visitor */
                                    (spec) -> { return new ControlFlowObfuscationMarker(spec); });
    }

}
