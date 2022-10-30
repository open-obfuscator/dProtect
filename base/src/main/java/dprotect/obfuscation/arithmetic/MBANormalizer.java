package dprotect.obfuscation.arithmetic;

import proguard.classfile.constant.*;
import proguard.classfile.instruction.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.InstructionSequenceMatcher;
import proguard.classfile.*;
import proguard.obfuscate.util.ReplacementSequences;

public class MBANormalizer
implements   ReplacementSequences {

    private static final int A = InstructionSequenceMatcher.A;
    private static final int B = InstructionSequenceMatcher.B;
    private static final int X = InstructionSequenceMatcher.X;
    private static final int Y = InstructionSequenceMatcher.Y;

    private final Instruction[][][] SEQUENCES;
    private final Constant[]        CONSTANTS;

    public MBANormalizer(ClassPool programClassPool,
                         ClassPool libraryClassPool)
    {
        InstructionSequenceBuilder ____ = new InstructionSequenceBuilder(programClassPool, libraryClassPool);
        SEQUENCES = new Instruction[][][]
        {
            {
                ____.aload(A)
                    .iload(X)
                    .dup2()
                    .__(),
                ____.aload(A)
                    .iload(X)
                    .nop()         // See: https://github.com/Guardsquare/proguard-core/issues/75
                    .aload(A)
                    .iload(X)
                    .__()
            },
            {
                ____.aload(A)
                    .bipush(X)
                    .dup2()
                    .__(),
                ____.aload(A)
                    .bipush(X)
                    .nop()        // See: https://github.com/Guardsquare/proguard-core/issues/75
                    .aload(A)
                    .bipush(X)
                    .__()
            },

            //Fix for: https://github.com/Guardsquare/proguard-core/issues/75
            {
                ____.aload(A)
                    .iload(X)
                    .aload(A)
                    .iload(X)
                    .__(),
                ____.aload(A)
                    .iload(X)
                    .nop()
                    .aload(A)
                    .iload(X)
                    .__()
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
