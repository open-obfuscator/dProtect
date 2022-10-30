package dprotect.obfuscation.arithmetic;

import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;

// X + Y <==> (X & Y) + (X | Y)
public class MBAObfuscationAdd
implements   ReplacementSequences {

    private static final int A = InstructionSequenceMatcher.A;
    private static final int B = InstructionSequenceMatcher.B;
    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;

    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBAObfuscationAdd(ClassPool programClassPool,
                             ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);
        /*
         * These transformations are taken from https://github.com/quarkslab/sspam
         * developed by Ninon Eyrolles
         */
        SEQUENCES = new Instruction[][][]
        {
        /*
         * Integer Support
         * ===================================================
         */
            // (int) X + Y
            {
                ____.iload(X)
                    .iload(Y)
                    .iadd()
                    .__(),
                ____.iload(X)
                    .iload(Y)
                    .iand()
                    .iload(X)
                    .iload(Y)
                    .ior()
                    .iadd()
                    .__(),
            },
            // (int) X + CST
            {
                ____.iload(X)
                    .iconst(Y)
                    .iadd()
                    .__(),
                ____.iload(X)
                    .iconst(Y)
                    .iand()
                    .iload(X)
                    .iconst(Y)
                    .ior()
                    .iadd()
                    .__(),
            },
            // (int) X + LARGE_CST
            {
                ____.iload(X)
                    .ldc_(Y)
                    .iadd()
                    .__(),
                ____.iload(X)
                    .ldc_(Y)
                    .iand()
                    .iload(X)
                    .ldc_(Y)
                    .ior()
                    .iadd()
                    .__(),
            },
            // (int) X + LARGE_CST
            {
                ____.iload(X)
                    .ldc_w_(Y)
                    .iadd()
                    .__(),
                ____.iload(X)
                    .ldc_w_(Y)
                    .iand()
                    .iload(X)
                    .ldc_w_(Y)
                    .ior()
                    .iadd()
                    .__(),
            },
            // (int) X++
            {
                ____.iinc(X, Y)
                    .__(),
                ____.iload(X)
                    .bipush(Y)
                    .iand()
                    .iload(X)
                    .bipush(Y)
                    .ior()
                    .iadd()
                    .istore(X)
                    .__(),
            },
        /*
         * Long Support
         * ===================================================
         */
            // (long) X + Y
            {
                ____.lload(X)
                    .lload(Y)
                    .ladd()
                    .__(),
                ____.lload(X)
                    .lload(Y)
                    .land()
                    .lload(X)
                    .lload(Y)
                    .lor()
                    .ladd()
                    .__(),
            },
            // (long) X + CST
            {
                ____.lload(X)
                    .lconst(Y)
                    .ladd()
                    .__(),
                ____.lload(X)
                    .lconst(Y)
                    .land()
                    .lload(X)
                    .lconst(Y)
                    .lor()
                    .ladd()
                    .__(),
            },
            // (long) X + LARGE_CST
            {
                ____.lload(X)
                    .ldc2_w(Y)
                    .ladd()
                    .__(),
                ____.lload(X)
                    .ldc2_w(Y)
                    .land()
                    .lload(X)
                    .ldc2_w(Y)
                    .lor()
                    .ladd()
                    .__(),
            },
        /*
         * Long Array Support
         * /!\ TODO: To be completed /!\
         * ===================================================
         */
            // A[X] + B[Y] (iconst / iconst for the indexes)
            {
                ____
                    // Op 0 -> A[X]
                    .aload(A)
                    .iconst(X)
                    .laload()
                    // Op 1 -> B[Y]
                    .aload(B)
                    .iconst(Y)
                    .laload()
                    // Op: (long) Add
                    .ladd()
                    .__(),
                ____
                    // A[X] & B[Y] {
                        .aload(A)
                        .iconst(X)
                        .laload()
                        .aload(B)
                        .iconst(Y)
                        .laload()
                        .land()
                    // }
                    // A[X] | B[Y] {
                        .aload(A)
                        .iconst(X)
                        .laload()
                        .aload(B)
                        .iconst(Y)
                        .laload()
                        .lor()
                    // }
                    .ladd()
                    .__(),
            },
            // A[X] + B[Y] (bipush / bipush for the indexes)
            {
                ____
                    // Op 0 -> A[X]
                    .aload(A)
                    .bipush(X)
                    .laload()
                    // Op 1 -> B[Y]
                    .aload(B)
                    .bipush(Y)
                    .laload()
                    // Op: (long) Add
                    .ladd()
                    .__(),
                ____
                    // A[X] & B[Y] {
                        .aload(A)
                        .bipush(X)
                        .laload()
                        .aload(B)
                        .bipush(Y)
                        .laload()
                        .land()
                    // }
                    // A[X] | B[Y] {
                        .aload(A)
                        .bipush(X)
                        .laload()
                        .aload(B)
                        .bipush(Y)
                        .laload()
                        .lor()
                    // }
                    .ladd()
                    .__(),
            },

        /*
         * Int Array Support
         * /!\ TODO: To be completed  /!\
         * ===================================================
         */
            // A[X] + B[Y]
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
                    // A[X] + B[Y]
                    .iadd()
                    .__(),

                ____
                    // A[X] & A[Y] {
                        .aload(A)
                        .iload(X)
                        .iaload()

                        .aload(B)
                        .iload(Y)
                        .iaload()

                        .iand()
                    // }
                    // A[X] | A[Y] {
                        .aload(A)
                        .iload(X)
                        .iaload()

                        .aload(B)
                        .iload(Y)
                        .iaload()

                        .ior()
                    // }
                    .iadd()
                    .__(),
            },

        /*
         * Float Support
         * /!\ TODO /!\
         * ===================================================
         */
        // ....
        /*
         * Double Support
         * /!\ TODO /!\
         * ===================================================
         */
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
