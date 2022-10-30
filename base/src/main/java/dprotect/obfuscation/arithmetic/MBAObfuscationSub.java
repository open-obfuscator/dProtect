package dprotect.obfuscation.arithmetic;


import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;

public class MBAObfuscationSub
implements   ReplacementSequences {

    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;

    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBAObfuscationSub(ClassPool programClassPool,
                             ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);

        // X - Y <=> (X ^ -Y) + 2*(X & -Y)
        SEQUENCES = new Instruction[][][]
        {
        /*
         * Integer Support
         * ===================================================
         */
            // (int) A - B
            {
                ____.iload(X)
                    .iload(Y)
                    .isub()
                    .__(),
                ____.iload(X)
                    .iload(Y)
                    .ineg()
                    .ixor()
                    .iconst_2()
                    .iload(X)
                    .iload(Y)
                    .ineg()
                    .iand()
                    .imul()
                    .iadd()
                    .__(),
            },
            // (int) A - CST
            {
                ____.iload(X)
                    .iconst(Y)
                    .isub()
                    .__(),
                ____.iload(X)
                    .iconst(Y)
                    .ineg()
                    .ixor()
                    .iconst_2()
                    .iload(X)
                    .iconst(Y)
                    .ineg()
                    .iand()
                    .imul()
                    .iadd()
                    .__(),
            },
            // (int) A - LARGE_CST
            {
                ____.iload(X)
                    .ldc_(Y)
                    .isub()
                    .__(),
                ____.iload(X)
                    .ldc_(Y)
                    .ineg()
                    .ixor()
                    .iconst_2()
                    .iload(X)
                    .ldc_(Y)
                    .ineg()
                    .iand()
                    .imul()
                    .iadd()
                    .__(),
            },
            // (int) A - LARGE_CST
            {
                ____.iload(X)
                    .ldc_w_(Y)
                    .isub()
                    .__(),
                ____.iload(X)
                    .ldc_w_(Y)
                    .ineg()
                    .ixor()
                    .iconst_2()
                    .iload(X)
                    .ldc_w_(Y)
                    .ineg()
                    .iand()
                    .imul()
                    .iadd()
                    .__(),
            },
        /*
         * Long Support
         * ===================================================
         */
            // (long) A - B
            {
                ____.lload(X)
                    .lload(Y)
                    .lsub()
                    .__(),
                ____.lload(X)
                    .lload(Y)
                    .lneg()
                    .lxor()
                    .ldc2_w((long)2)
                    .lload(X)
                    .lload(Y)
                    .lneg()
                    .land()
                    .lmul()
                    .ladd()
                    .__(),
            },
            // (long) A - CST
            {
                ____.lload(X)
                    .lconst(Y)
                    .lsub()
                    .__(),
                ____.lload(X)
                    .lconst(Y)
                    .lneg()
                    .lxor()
                    .ldc2_w((long)2)
                    .lload(X)
                    .lconst(Y)
                    .lneg()
                    .land()
                    .lmul()
                    .ladd()
                    .__(),
            },
            // (long) A - LARGE_CST
            {
                ____.lload(X)
                    .ldc2_w(Y)
                    .lsub()
                    .__(),
                ____.lload(X)
                    .ldc2_w(Y)
                    .lneg()
                    .lxor()
                    .ldc2_w((long)2)
                    .lload(X)
                    .ldc2_w(Y)
                    .lneg()
                    .land()
                    .lmul()
                    .ladd()
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
