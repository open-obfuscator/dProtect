package dprotect.obfuscation.arithmetic;

import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;
// A & B = (X + Y) - (X | Y)
public class MBAObfuscationAnd
implements   ReplacementSequences {

    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;

    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBAObfuscationAnd(ClassPool programClassPool,
                             ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);

        SEQUENCES = new Instruction[][][]
        {
        /*
         * Integer Support
         * ===================================================
         */
            // X & Y
            {
                ____.iload(X)
                    .iload(Y)
                    .iand()
                    .__(),
                ____.iload(X)
                    .iload(Y)
                    .iadd()
                    .iload(X)
                    .iload(Y)
                    .ior()
                    .isub()
                    .__(),
            },
            // X & CST
            {
                ____.iload(X)
                    .iconst(Y)
                    .iand()
                    .__(),
                ____.iload(X)
                    .iconst(Y)
                    .iadd()
                    .iload(X)
                    .iconst(Y)
                    .ior()
                    .isub()
                    .__(),
            },
            // X & LARGE_CST
            {
                ____.iload(X)
                    .ldc_(Y)
                    .iand()
                    .__(),
                ____.iload(X)
                    .ldc_(Y)
                    .iadd()
                    .iload(X)
                    .ldc_(Y)
                    .ior()
                    .isub()
                    .__(),
            },
            // X & LARGE_CST
            {
                ____.iload(X)
                    .ldc_w_(Y)
                    .iand()
                    .__(),
                ____.iload(X)
                    .ldc_w_(Y)
                    .iadd()
                    .iload(X)
                    .ldc_w_(Y)
                    .ior()
                    .isub()
                    .__(),
            },
        /*
         * Long Support
         * ===================================================
         */
            // X & Y
            {
                ____.lload(X)
                    .lload(Y)
                    .land()
                    .__(),
                ____.lload(X)
                    .lload(Y)
                    .ladd()
                    .lload(X)
                    .lload(Y)
                    .lor()
                    .lsub()
                    .__(),
            },
            // X & CST
            {
                ____.lload(X)
                    .lconst(Y)
                    .land()
                    .__(),
                ____.lload(X)
                    .lconst(Y)
                    .ladd()
                    .lload(X)
                    .lconst(Y)
                    .lor()
                    .lsub()
                    .__(),
            },
            // X & LARGE_CST
            {
                ____.lload(X)
                    .ldc2_w(Y)
                    .land()
                    .__(),
                ____.lload(X)
                    .ldc2_w(Y)
                    .ladd()
                    .lload(X)
                    .ldc2_w(Y)
                    .lor()
                    .lsub()
                    .__(),
            },
        };

        // TODO(xxx): Handle Float and Double
        CONSTANTS = ____.constants();
    }

    @Override
    public Instruction[][][] getSequences() {
        return SEQUENCES;
    }

    @Override
    public Constant[] getConstants() {
        return CONSTANTS;
    }
}
