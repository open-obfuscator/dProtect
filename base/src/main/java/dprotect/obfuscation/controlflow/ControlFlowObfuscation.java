package dprotect.obfuscation.controlflow;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.*;
import proguard.classfile.visitor.*;

import proguard.evaluation.*;
import proguard.evaluation.value.*;

public class ControlFlowObfuscation
implements   ClassVisitor,
             AttributeVisitor,
             InstructionVisitor
{
    private static final Logger logger = LogManager.getLogger(ControlFlowObfuscation.class);

    private static final boolean             DEBUG               = false;
    private static final String              OPAQUE_FIELD_0      = "OPAQUE_0";
    private final        Random              rand;
    private final        CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();

    private final ReferenceTracingValueFactory referenceTracingValueFactory = new ReferenceTracingValueFactory(new TypedReferenceValueFactory());
    private final PartialEvaluator             partialEvaluator             = new PartialEvaluator(referenceTracingValueFactory,
                                                                                                   new ReferenceTracingInvocationUnit(new BasicInvocationUnit(referenceTracingValueFactory)),
                                                                                                   /*evaluateAllCode=*/true,
                                                                                                   referenceTracingValueFactory);

    public ControlFlowObfuscation(int seed)
    {
        rand = new Random((long)seed);
    }

    // Implementations for ClassVisitor.

    @Override
    public void visitAnyClass(Clazz clazz) {
        if (clazz instanceof ProgramClass) {
            visitProgramClass((ProgramClass)clazz);
        }
    }


    @Override
    public void visitProgramClass(ProgramClass programClass)
    {
        if ((programClass.getAccessFlags() & AccessConstants.INTERFACE) != 0)
        {
            return;
        }
        // On a flagged program class, add an new field that is used for an opaque condition
        prepareClassFields(programClass);
        programClass.accept(new AllMethodVisitor(
                            new AllAttributeVisitor(this)));

    }

    // Implementations for AttributeVisitor.

    @Override
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}

    @Override
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        // Start by evaluating the current bytecode
        partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);

        codeAttributeEditor.reset(codeAttribute.u4codeLength);

        if (DEBUG) { logger.info("=== {}.{} ===", clazz.getName(), method.getName(clazz)); }
        if (DEBUG) { showEvaluatedInst(clazz, method, codeAttribute); }

        // Process to the modification and commit the modifications (if any)
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.accept(clazz, method, codeAttributeEditor);

        if (DEBUG)
        {
            logger.info("[+] -> New Instructions:");
            showInst(clazz, method, codeAttribute);
            logger.info("[-] <- New Instructions");
            // This can be used to early detect inconsistency's
            partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
        }
    }


    // Implementations for InstructionVisitor.

    @Override
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) { }

    @Override
    public void visitBranchInstruction(Clazz             clazz,
                                       Method            method,
                                       CodeAttribute     codeAttribute,
                                       int               offset,
                                       BranchInstruction branch)
    {
        InstructionSequenceBuilder ____ =
            new InstructionSequenceBuilder((ProgramClass)clazz);

        // Currently, this pass only targets GOTO instructions
        if (branch.opcode != Instruction.OP_GOTO) {
            return;
        }

        FrameFinder finder = new FrameFinder(this.partialEvaluator, offset);
        codeAttribute.instructionsAccept(clazz, method, finder);

        if (finder.targets.isEmpty())
        {
            // We can't find a suitable location to
            // redirect the goto. Hence, we inject a new block
            CodeAttributeEditor.Label OPAQUE_BLOCK = codeAttributeEditor.label();
            float randomFloat = rand.nextFloat();

            ____.label(OPAQUE_BLOCK)
                .ldc(randomFloat)
                .ldc((float)getInvariantValue(255))
                .fmul()
                .f2i()
                .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                .iadd()
                .putstatic(clazz.getName(), OPAQUE_FIELD_0, "I");

            insertOpaquePredicate(clazz, ____)
            .ifne(branch.branchOffset)
            .goto_(OPAQUE_BLOCK.offset());

            codeAttributeEditor.replaceInstruction(offset, ____.instructions());
        }
        else
        {
            int idx        = rand.nextInt(finder.targets.size());
            int target     = finder.targets.get(idx);
            int reltarget  = target - offset;

            if (DEBUG) logger.info("[{}] -> [{}]", offset, target);

            // Insert an opaque predicate that is **always** != 0
            insertOpaquePredicate(clazz, ____)
            // **always** != 0 => This branch is **always** taken (original goto's offset)
            .ifne(branch.branchOffset)
            // Opaque branch: jump randomly to another suitable instruction
            .goto_(reltarget);

            // Replace the original goto instruction with the previous instructions
            codeAttributeEditor.replaceInstruction(offset, ____.instructions());
        }
    }

    int getInvariantValue(int range)
    {
        int X = rand.nextInt(range);
        X -= X % 2;
        return X;
    }

    private InstructionSequenceBuilder insertOpaquePredicate(Clazz clazz, InstructionSequenceBuilder ____)
    {
        final class OPAQUE_PREDICATES
        {
            public static final int OP_0 = 0;
            public static final int OP_1 = 1;
            public static final int OP_2 = 2;

            public static final int LEN = 3;
        }

        int id = rand.nextInt(OPAQUE_PREDICATES.LEN);

        switch (id)
        {
            // (X + 1) % 2 != 0
            case OPAQUE_PREDICATES.OP_0:
                {
                    ____.getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .iconst_1()
                        .iadd()
                        .iconst_2()
                        .irem();
                    return ____;
                }
            // (X ^ 2 + X + 7) mod 81 != 0
            case OPAQUE_PREDICATES.OP_1:
                {
                    ____.bipush(getInvariantValue(128))
                        .putstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .imul()
                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .iadd()
                        .bipush(7)
                        .iadd()
                        .bipush(81)
                        .irem();
                    return ____;
                }
            // 7y ^ 2 − 1 != x
            // -> 7 (x + RND) ^ 2 - 1 - x != 0
            case OPAQUE_PREDICATES.OP_2:
                {
                    int var = getInvariantValue(128);
                    ____.bipush(7)

                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .ldc(var)
                        .iadd() // (X + RND)

                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .ldc(var)
                        .iadd() // (X + RND)

                        .imul() // ^ 2
                        .imul() // * 7
                        .iconst_1()
                        .isub()
                        .getstatic(clazz.getName(), OPAQUE_FIELD_0, "I")
                        .isub();
                    return ____;
                }
        }
        return ____;
    }

    /*
     * Add and initialize new field(s) so that it can be used to generate opaque predicates
     *
     * OPAQUE_FIELD_0 should follow an invariant on which opaque conditions can rely on.
     * In the current form, the invariant is: "OPAQUE_FIELD_0 must be even"
     */
    private void prepareClassFields(ProgramClass programClass)
    {
        ConstantPoolEditor constantPoolEditor = new ConstantPoolEditor(programClass);

        ClassEditor classEditor      = new ClassEditor(programClass);
        int          nameIndex       = constantPoolEditor.addUtf8Constant(OPAQUE_FIELD_0);
        int          descriptorIndex = constantPoolEditor.addUtf8Constant("I");
        ProgramField opaqueField     = new ProgramField(AccessConstants.PRIVATE | AccessConstants.STATIC,
                                                    nameIndex, descriptorIndex, null);
        classEditor.addField(opaqueField);

        final int value = getInvariantValue(Integer.MAX_VALUE - 1);

        new InitializerEditor(programClass).addStaticInitializerInstructions(/*mergeIntoExistingInitializer=*/true,
            /* Initialize OPAQUE_FIELD_0 with the invariant */
            ____ -> {
                ____.ldc(value)
                    .putstatic(programClass, opaqueField);
            });
    }

    /*
     * This Class Visitor is used to find instructions
     * which share the same stack/local frames
     */
    private static class FrameFinder
    implements           InstructionVisitor
    {
        public final PartialEvaluator evaluator;
        public final int              offset;
        public final TracedVariables  variables;
        public final TracedStack      stack;
        public final ArrayList<Integer> targets = new ArrayList<Integer>();

        public FrameFinder(PartialEvaluator evaluator,
                           int              offset)
        {
            this.evaluator = evaluator;
            this.offset    = offset;
            this.variables = evaluator.getVariablesBefore(offset);
            this.stack     = evaluator.getStackBefore(offset);
        }

        @Override
        public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
        {
            if (instruction instanceof BranchInstruction)
            {
                return;
            }

            if (offset == this.offset)
            {
                return;
            }

            TracedStack stack = evaluator.getStackBefore(offset);
            if (stack.size() != this.stack.size())
            {
                return;
            }

            TracedVariables vars = evaluator.getVariablesBefore(offset);
            if (vars.size() != this.variables.size())
            {
                return;
            }

            for (int index = 0; index < stack.size(); ++index)
            {
                Value target  = stack.getBottom(index);
                Value current = this.stack.getBottom(index);

                if (current == null && target != null) return;
                if (current == null && target == null) continue;
                if (!current.equals(target)          ) return;
            }

            for (int index = 0; index < vars.size(); ++index)
            {
                Value target  = vars.getValue(index);
                Value current = this.variables.getValue(index);

                if (current == null && target != null) return;
                if (current == null && target == null) continue;
                if (!current.equals(target)          ) return;
            }

            targets.add(offset);
        }

    }


    // Helpers for debugging

    private void showInst(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.instructionsAccept(clazz, method, new InstructionVisitor() {
            @Override
            public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute,
                                            int offset, Instruction instruction)
            {
                logger.info("{}", instruction.toString(clazz, offset));
            }
        });
    }

    private void showEvaluatedInst(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.instructionsAccept(clazz, method, new InstructionVisitor() {
            @Override
            public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute,
                                            int offset, Instruction instruction)
            {
                TracedStack stackbefore = partialEvaluator.getStackBefore(offset);
                TracedStack stackafter  = partialEvaluator.getStackAfter(offset);

                TracedVariables varbefore = partialEvaluator.getVariablesBefore(offset);
                TracedVariables varfter   = partialEvaluator.getVariablesAfter(offset);
                logger.info("{} ({}/{}) {} | {}", String.format("%-70s", instruction.toString(clazz, offset)),
                                                  stackbefore.size(), stackafter.size(),
                                                  varbefore, varfter);
                logger.info("{} ({}/{}) {} | {}", String.format("%-70s", ""),
                                                  stackbefore.size(), stackafter.size(),
                                                  stackbefore, stackafter);
            }
        });
    }

}

