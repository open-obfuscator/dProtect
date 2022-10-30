package dprotect.deobfuscation;

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

public class Deobfuscator implements Pass
{
    private static final Logger logger = LogManager.getLogger(Deobfuscator.class);

    private static final String DEOBFUSCATION_STRING         = "deobfuscation/string";

    private final Configuration configuration;

    private boolean codeObfuscationString;
    private boolean codeObfuscationArithmeticMba;
    private boolean codeObfuscationConstants;
    private boolean codeObfuscationControlFlow;

    public Deobfuscator(Configuration configuration)
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

        //codeObfuscationString        = filter.matches(OBFUSCATION_STRING);
        //codeObfuscationArithmeticMba = filter.matches(OBFUSCATION_ARITHMETIC_MBA);
        //codeObfuscationConstants     = filter.matches(OBFUSCATION_CONSTANTS);
        //codeObfuscationControlFlow   = filter.matches(OBFUSCATION_CONTROL_FLOW);

        //logger.info("Applying code obfuscation ...");

        //if (configuration.seed == null)
        //{
        //    configuration.seed = (int)System.currentTimeMillis();
        //}

        //logger.info("Using obfuscation seed: {}", configuration.seed);

        //obfuscate(configuration,
        //          appView.programClassPool,
        //          appView.libraryClassPool,
        //          appView.extraDataEntryNameMap);
    }


    private void deobfuscate(Configuration         configuration,
                             ClassPool             programClassPool,
                             ClassPool             libraryClassPool,
                             ExtraDataEntryNameMap extraDataEntryNameMap)
    throws IOException
    {
    }
}

