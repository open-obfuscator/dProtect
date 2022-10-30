package dprotect.obfuscation.arithmetic;

import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;

// X + Y + 1 + (~X | ~Y)
public class MBAObfuscationOr
implements   ReplacementSequences {

    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;


    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBAObfuscationOr(ClassPool programClassPool,
                            ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);
        SEQUENCES = new Instruction[][][]
        {
        /*
         * Integer Support
         * ===================================================
         */
            // X | Y
            {
                ____.iload(X)
                    .iload(Y)
                    .ior()
                    .__(),
                ____.iload(X)
                    .iload(Y)
                    .iadd()
                    .iconst_1()
                    .iadd()
                    .iload(X)
                    .iconst_m1()
                    .ixor()
                    .iload(Y)
                    .iconst_m1()
                    .ixor()
                    .ior()
                    .iadd()
                    .__(),
            },
            // X | CST
            {
                ____.iload(X)
                    .iconst(Y)
                    .ior()
                    .__(),
                ____.iload(X)
                    .iconst(Y)
                    .iadd()
                    .iconst_1()
                    .iadd()
                    .iload(X)
                    .iconst_m1()
                    .ixor()
                    .iconst(Y)
                    .iconst_m1()
                    .ixor()
                    .ior()
                    .iadd()
                    .__(),
            },
            // X | LARGE_CST
            {
                ____.iload(X)
                    .ldc_(Y)
                    .ior()
                    .__(),
                ____.iload(X)
                    .ldc_(Y)
                    .iadd()
                    .iconst_1()
                    .iadd()
                    .iload(X)
                    .iconst_m1()
                    .ixor()
                    .ldc_(Y)
                    .iconst_m1()
                    .ixor()
                    .ior()
                    .iadd()
                    .__(),
            },
            // X | LARGE_CST
            {
                ____.iload(X)
                    .ldc_w_(Y)
                    .ior()
                    .__(),
                ____.iload(X)
                    .ldc_w_(Y)
                    .iadd()
                    .iconst_1()
                    .iadd()
                    .iload(X)
                    .iconst_m1()
                    .ixor()
                    .ldc_w_(Y)
                    .iconst_m1()
                    .ixor()
                    .ior()
                    .iadd()
                    .__(),
            },
        /*
         * Long Support
         * ===================================================
         */
            // X | Y
            {
                ____.lload(X)
                    .lload(Y)
                    .lor()
                    .__(),
                ____.lload(X)
                    .lload(Y)
                    .ladd()
                    .lconst_1()
                    .ladd()
                    .lload(X)
                    .ldc2_w((long)-1)
                    .lxor()
                    .lload(Y)
                    .ldc2_w((long)-1)
                    .lxor()
                    .lor()
                    .ladd()
                    .__(),
            },
            // X | CST
            {
                ____.lload(X)
                    .lconst(Y)
                    .lor()
                    .__(),
                ____.lload(X)
                    .lconst(Y)
                    .ladd()
                    .lconst_1()
                    .ladd()
                    .lload(X)
                    .ldc2_w((long)-1)
                    .lxor()
                    .lconst(Y)
                    .ldc2_w((long)-1)
                    .lxor()
                    .lor()
                    .ladd()
                    .__(),
            },
            // X | CST
            {
                ____.lload(X)
                    .ldc2_w(Y)
                    .lor()
                    .__(),
                ____.lload(X)
                    .ldc2_w(Y)
                    .ladd()
                    .lconst_1()
                    .ladd()
                    .lload(X)
                    .ldc2_w((long)-1)
                    .lxor()
                    .ldc2_w(Y)
                    .ldc2_w((long)-1)
                    .lxor()
                    .lor()
                    .ladd()
                    .__(),
            },
        };

        // TODO(XXX): Handle Float and Double

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
