package dprotect.deobfuscation.strings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.ClassSpecification;
import proguard.MemberSpecification;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.*;

import java.util.Base64;


public class XoredStrings
implements   AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor

{
  private final byte[]              KEY;
  private final ClassSpecification  decodeSpecifier;
  private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
  private       ConstantPoolEditor  constantPoolEditor  = null;
  private       String              stackedString       = null;
  private       LdcInfo             ldcString           = null;

  private static final Logger logger = LogManager.getLogger(XoredStrings.class);

  private class LdcInfo
  {
    public int offset;
    public byte opcode;
    LdcInfo(int offset, byte opcode)
    {
      this.offset = offset;
      this.opcode = opcode;
    }
  }

  public XoredStrings(String talsecKey, ClassSpecification decodeInfo)
  {
    decodeSpecifier = decodeInfo;
    KEY = Base64.getDecoder().decode(talsecKey);
  }

  private boolean isDecodeMethod(String clazz, String method)
  {
    if (!decodeSpecifier.className.equals(clazz))
    {
      return false;
    }
    if (decodeSpecifier.methodSpecifications.isEmpty())
    {
      return false;
    }

    Object spec = decodeSpecifier.methodSpecifications.get(0);

    if (!(spec instanceof MemberSpecification))
    {
      return false;
    }
    MemberSpecification memberSpec = (MemberSpecification)spec;
    return memberSpec.name.equals(method);
  }

  public static byte[] hex2bytes(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i),     16) << 4)
                             + Character.digit(s.charAt(i + 1), 16)     );
    }
    return data;
  }

  private String decode(byte[] bytes) {
    byte[] decoded = new byte[bytes.length];
    for (int i = 0; i < decoded.length; ++i) {
      decoded[i] = (byte)((bytes[i]) ^ KEY[i % KEY.length]);
    }
    return new String(decoded);
  }

  private String decode(String enc)
  {
    return decode(hex2bytes(enc));
  }

  // Implementations for AttributeVisitor.

  @Override
  public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}

  @Override
  public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
  {
    this.constantPoolEditor = new ConstantPoolEditor((ProgramClass)clazz);
    codeAttributeEditor.reset(codeAttribute.u4codeLength);
    codeAttribute.instructionsAccept(clazz, method, this);
    codeAttribute.accept(clazz, method, codeAttributeEditor);
  }

  // Implementations for InstructionVisitor.
  @Override
  public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}

  @Override
  public void visitConstantInstruction(Clazz clazz,
                                       Method method,
                                       CodeAttribute codeAttribute,
                                       int offset,
                                       ConstantInstruction constantInstruction)
  {
    byte opcode = constantInstruction.opcode;
    if (opcode == Instruction.OP_LDC || opcode == Instruction.OP_LDC_W)
    {
      this.ldcString = new LdcInfo(offset, opcode);
      clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
    }

    if (opcode == Instruction.OP_INVOKESTATIC) {
      ProgramClass programClass = (ProgramClass) clazz;
      Constant ref = programClass.getConstant(constantInstruction.constantIndex);
      if (!(ref instanceof MethodrefConstant)) {
        return;
      }

      MethodrefConstant methodRef = (MethodrefConstant)ref;
      String className  = methodRef.getClassName(clazz);
      String methodName = methodRef.getName(clazz);
      if (isDecodeMethod(className, methodName)) {
        if (this.stackedString != null) {
          //logger.info("{} -> {}", stackedString, decode(stackedString));
          this.codeAttributeEditor.deleteInstruction(offset);
          int idx = this.constantPoolEditor.addStringConstant(decode(this.stackedString));
          ConstantInstruction replacedLdc = new ConstantInstruction(this.ldcString.opcode, idx);
          this.codeAttributeEditor.replaceInstruction(this.ldcString.offset, replacedLdc);
          this.stackedString = null;
        }
      }
    }
  }

  // Implementations for ConstantVisitor.

  @Override
  public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
  {
    clazz.constantPoolEntryAccept(stringConstant.u2stringIndex, this);
  }

  @Override
  public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
  {
    this.stackedString = utf8Constant.getString();
  }

  @Override
  public void visitAnyConstant(Clazz clazz, Constant constant) {}
}
