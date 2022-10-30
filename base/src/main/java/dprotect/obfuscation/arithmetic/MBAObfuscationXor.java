package dprotect.obfuscation.arithmetic;

import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;

// A ^ B <=> (A | B) - (A & B)
public class MBAObfuscationXor
implements   ReplacementSequences {


    private static final int A = InstructionSequenceMatcher.A;
    private static final int B = InstructionSequenceMatcher.B;

    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;


    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBAObfuscationXor(ClassPool programClassPool,
                             ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);


        SEQUENCES = new Instruction[][][]
        {
        /*
         * Integer Support
         * ===================================================
         */
            // (int) X ^ Y
            {
                ____.iload(X)
                    .iload(Y)
                    .ixor()
                    .__(),
                ____.iload(X)
                    .iload(Y)
                    .ior()
                    .iload(X)
                    .iload(Y)
                    .iand()
                    .isub()
                    .__()
            },
            // (int) X ^ CST
            {
                ____.iload(X)
                    .iconst(Y)
                    .ixor()
                    .__(),
                ____.iload(X)
                    .iconst(Y)
                    .ior()
                    .iload(X)
                    .iconst(Y)
                    .iand()
                    .isub()
                    .__()
            },
            // (int) X ^ LARGE_CST
            {
                ____.iload(X)
                    .ldc_(Y)
                    .ixor()
                    .__(),
                ____.iload(X)
                    .ldc_(Y)
                    .ior()
                    .iload(X)
                    .ldc_(Y)
                    .iand()
                    .isub()
                    .__()
            },

            // (int) X ^ LARGE_CST
            {
                ____.iload(X)
                    .ldc_w_(Y)
                    .ixor()
                    .__(),
                ____.iload(X)
                    .ldc_w_(Y)
                    .ior()
                    .iload(X)
                    .ldc_w_(Y)
                    .iand()
                    .isub()
                    .__()
            },
        /*
         * Long Support
         * ===================================================
         */
            // (long) X ^ B
            {
                ____.lload(X)
                    .lload(Y)
                    .lxor()
                    .__(),
                ____.lload(X)
                    .lload(Y)
                    .lor()
                    .lload(X)
                    .lload(Y)
                    .land()
                    .lsub()
                    .__()
            },
            // (long) X ^ CST
            {
                ____.lload(X)
                    .lconst(Y)
                    .lxor()
                    .__(),
                ____.lload(X)
                    .lconst(Y)
                    .lor()
                    .lload(X)
                    .lconst(Y)
                    .land()
                    .lsub()
                    .__()
            },
            // (long) X ^ LARGE_CST
            {
                ____.lload(X)
                    .ldc2_w(Y)
                    .lxor()
                    .__(),
                ____.lload(X)
                    .ldc2_w(Y)
                    .lor()
                    .lload(X)
                    .ldc2_w(Y)
                    .land()
                    .lsub()
                    .__()
            },
        /*
         * Int Array Support
         * /!\ NOT FINISHED /!\
         * ===================================================
         */
            // A[X] ^ B[Y]
            {
                ____
                    // A[X]
                    .aload(A)
                    .iload(X)
                    .iaload()
                    // B[Y]
                    .aload(B)
                    .iload(Y)
                    .iaload()
                    // A[X] ^ B[Y]
                    .ixor()
                    .__(),

                ____
                    // A[X] | A[Y] {
                        .aload(A)
                        .iload(X)
                        .iaload()

                        .aload(B)
                        .iload(Y)
                        .iaload()

                        .ior()
                    // }
                    // A[X] & A[Y] {
                        .aload(A)
                        .iload(X)
                        .iaload()

                        .aload(B)
                        .iload(Y)
                        .iaload()

                        .iand()
                    // }
                    .isub()
                    .__(),
            },

        };

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
