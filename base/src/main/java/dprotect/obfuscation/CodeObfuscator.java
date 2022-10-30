package dprotect.obfuscation;

import dprotect.Configuration;
import dprotect.obfuscation.arithmetic.*;
import dprotect.obfuscation.constants.*;
import dprotect.obfuscation.controlflow.*;
import dprotect.obfuscation.info.ObfuscationInfo;
import dprotect.obfuscation.strings.*;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.AppView;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.instruction.visitor.AllInstructionVisitor;
import proguard.classfile.util.PrimitiveArrayConstantReplacer;
import proguard.classfile.util.ClassReferenceInitializer;
import proguard.classfile.visitor.*;
import proguard.classfile.constant.visitor.AllConstantVisitor;
import proguard.io.ExtraDataEntryNameMap;
import proguard.obfuscate.util.InstructionSequenceObfuscator;
import proguard.pass.Pass;
import proguard.util.*;

public class CodeObfuscator implements Pass
{
    private static final Logger logger = LogManager.getLogger(CodeObfuscator.class);

    private static final String OBFUSCATION_STRING         = "obfuscation/string";
    private static final String OBFUSCATION_ARITHMETIC_MBA = "obfuscation/arithmetic/mba";
    private static final String OBFUSCATION_CONSTANTS      = "obfuscation/constants";
    private static final String OBFUSCATION_CONTROL_FLOW   = "obfuscation/controlflow";

    private final Configuration configuration;

    private boolean codeObfuscationString;
    private boolean codeObfuscationArithmeticMba;
    private boolean codeObfuscationConstants;
    private boolean codeObfuscationControlFlow;

    public CodeObfuscator(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Performs obfuscation of the given program class pool.
     */
    @Override
    public void execute(AppView appView) throws IOException
    {
        // Create a matcher for filtering optimizations.
        StringMatcher filter = configuration.optimizations != null ?
                               new ListParser(new NameParser()).parse(configuration.obfuscations) :
                               new ConstantMatcher(true);

        codeObfuscationString        = filter.matches(OBFUSCATION_STRING);
        codeObfuscationArithmeticMba = filter.matches(OBFUSCATION_ARITHMETIC_MBA);
        codeObfuscationConstants     = filter.matches(OBFUSCATION_CONSTANTS);
        codeObfuscationControlFlow   = filter.matches(OBFUSCATION_CONTROL_FLOW);

        logger.info("Applying code obfuscation ...");

        if (configuration.seed == null)
        {
            configuration.seed = (int)System.currentTimeMillis();
        }

        logger.info("Using obfuscation seed: {}", configuration.seed);

        obfuscate(configuration,
                  appView.programClassPool,
                  appView.libraryClassPool,
                  appView.extraDataEntryNameMap);
    }


    private void obfuscate(Configuration         configuration,
                           ClassPool             programClassPool,
                           ClassPool             libraryClassPool,
                           ExtraDataEntryNameMap extraDataEntryNameMap)
    throws IOException
    {
        /* Make sure that PrimitiveArrayConstant are correctly expanded before running
         * the pass.
         *
         * This PrimitiveArrayConstantReplacer is also run in the main DProtect.java/Proguard.java
         * but it comes after the optimization/obfuscation pipeline. I don't know if it is a bug
         * but for this pass, it needs to be run **before**
         */
        {
            programClassPool.classesAccept(new PrimitiveArrayConstantReplacer());
        }

        if (codeObfuscationString)
        {
            runStringObfuscation(configuration,
                                 programClassPool, libraryClassPool,
                                 extraDataEntryNameMap);
        }

        if (codeObfuscationControlFlow)
        {
            runControlFlowObfuscation(configuration,
                                      programClassPool, libraryClassPool,
                                      extraDataEntryNameMap);
        }

        if (codeObfuscationArithmeticMba)
        {
            runArithmeticObfuscation(configuration,
                                     programClassPool, libraryClassPool,
                                     extraDataEntryNameMap);
        }

        if (codeObfuscationConstants)
        {
            runConstantsObfuscation(configuration,
                                    programClassPool, libraryClassPool,
                                    extraDataEntryNameMap);
        }

        programClassPool.accept(new AllClassVisitor(
                                new AllFieldVisitor(
                                new ClassReferenceInitializer(programClassPool, libraryClassPool))));

        programClassPool.accept(new AllClassVisitor(
                                new AllConstantVisitor(
                                new ClassReferenceInitializer(programClassPool, libraryClassPool))));
    }

    private void runStringObfuscation(Configuration       configuration,
                                      ClassPool             programClassPool,
                                      ClassPool             libraryClassPool,
                                      ExtraDataEntryNameMap extraDataEntryNameMap)
    {
        logger.info("dProtect: Applying strings encoding ...");

        programClassPool.accept(
                new AllClassVisitor(
                new ClassVisitor() {
                    /*
                     * The purpose of this visitor is to early filter
                     * on the classes that are flagged with the obfuscate-strings options
                     */
                    public ClassVisitor obfuscator;
                    @Override
                    public void visitAnyClass(Clazz clazz)
                    {
                        if (ObfuscationInfo.getObfuscationInfo(clazz).encodeStrings)
                        {
                            /*
                             * StringFieldMarker is used to flag the strings
                             * that are associated with field write accesses
                             */
                            clazz.accept(
                                new AllMethodVisitor(
                                new AllAttributeVisitor(
                                new AllInstructionVisitor(
                                new StringFieldMarker()))));
                            /*
                             * Run the obfuscation pass
                             */
                            obfuscator.visitAnyClass(clazz);
                        }
                    }
                    ClassVisitor apply(ClassVisitor obfuscator) { this.obfuscator = obfuscator; return this; }
                }.apply(new StringObfuscator(configuration.obfuscateStringsList, configuration.seed))));
    }
    private void runArithmeticObfuscation(Configuration       configuration,
                                          ClassPool             programClassPool,
                                          ClassPool             libraryClassPool,
                                          ExtraDataEntryNameMap extraDataEntryNameMap)
    {

        MBANormalizer     normalizer = new MBANormalizer    (programClassPool, libraryClassPool);
        MBAObfuscationAdd mbaAdd     = new MBAObfuscationAdd(programClassPool, libraryClassPool);
        MBAObfuscationXor mbaXor     = new MBAObfuscationXor(programClassPool, libraryClassPool);
        MBAObfuscationAnd mbaAnd     = new MBAObfuscationAnd(programClassPool, libraryClassPool);
        MBAObfuscationOr  mbaOr      = new MBAObfuscationOr (programClassPool, libraryClassPool);
        MBAObfuscationSub mbaSub     = new MBAObfuscationSub(programClassPool, libraryClassPool);

        logger.info("dProtect: Applying Mixed Boolean-Arithmetic expressions ...");
        programClassPool.accept(
            new AllClassVisitor(
            new ClassVisitor() {
                /*
                 * Early filter
                 * on the classes that are flagged with obfuscate-arithmetic
                 */
                public MemberVisitor obfuscator;
                @Override
                public void visitAnyClass(Clazz clazz)
                {
                    if (ObfuscationInfo.getObfuscationInfo(clazz).arithmetic != null)
                    {
                        clazz.methodsAccept(new ArithmeticObfuscationFilter(obfuscator));
                    }
                }
                ClassVisitor apply(MemberVisitor obfuscator) { this.obfuscator = obfuscator; return this; }
            }.apply(new MultiMemberVisitor(
                        new InstructionSequenceObfuscator(normalizer),
                        new InstructionSequenceObfuscator(mbaAdd),
                        new InstructionSequenceObfuscator(mbaXor),
                        new InstructionSequenceObfuscator(mbaAnd),
                        new InstructionSequenceObfuscator(mbaOr),
                        new InstructionSequenceObfuscator(mbaSub)))));
    }

    private void runControlFlowObfuscation(Configuration         configuration,
                                           ClassPool             programClassPool,
                                           ClassPool             libraryClassPool,
                                           ExtraDataEntryNameMap extraDataEntryNameMap)
    {
        logger.info("dProtect: Obfuscating control-flow ...");
        programClassPool.accept(
                new AllClassVisitor(
                new ClassVisitor() {
                    /*
                     * Early filter classes that are flagged with 'obfuscate-controlflow'
                     */
                    public ClassVisitor obfuscator;
                    @Override
                    public void visitAnyClass(Clazz clazz)
                    {
                        if (ObfuscationInfo.getObfuscationInfo(clazz).controlflow != null)
                        {
                            /*
                             * Run the obfuscation pass
                             */
                            obfuscator.visitAnyClass(clazz);
                        }
                    }
                    ClassVisitor apply(ClassVisitor obfuscator) { this.obfuscator = obfuscator; return this; }
                }.apply(new ControlFlowObfuscation(configuration.seed))));
    }

    private void runConstantsObfuscation(Configuration         configuration,
                                         ClassPool             programClassPool,
                                         ClassPool             libraryClassPool,
                                         ExtraDataEntryNameMap extraDataEntryNameMap)
    {
        logger.info("dProtect: Protecting Constants ...");

        programClassPool.accept(
                new AllClassVisitor(
                new ClassVisitor() {
                    /*
                     * Early filter classes that are flagged with 'obfuscate-constants'
                     */
                    public ClassVisitor obfuscator;
                    @Override
                    public void visitAnyClass(Clazz clazz)
                    {
                        if (ObfuscationInfo.getObfuscationInfo(clazz).constants != null)
                        {
                            /*
                             * Flag constant associated with marked fields
                             * /!\ TODO(romain): To be implemented /!\
                             */
                            //clazz.accept(
                            //    new AllMethodVisitor(
                            //    new AllAttributeVisitor(
                            //    new AllInstructionVisitor(
                            //    new ConstantFieldMarker()))));
                            /*
                             * Run the obfuscation pass
                             */
                            obfuscator.visitAnyClass(clazz);
                        }
                    }
                    ClassVisitor apply(ClassVisitor obfuscator) { this.obfuscator = obfuscator; return this; }
                }.apply(new ConstantsObfuscator(configuration.seed))));
    }

}

