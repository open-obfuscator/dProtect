package dprotect.obfuscation.info;

import dprotect.obfuscation.arithmetic.ArithmeticObfuscationInfo;
import dprotect.obfuscation.constants.ConstantObfuscationInfo;
import dprotect.obfuscation.controlflow.ControlFlowObfuscationInfo;

import proguard.classfile.Clazz;
import proguard.classfile.Method;
import proguard.classfile.Field;
import proguard.classfile.constant.Constant;

public class ObfuscationInfo
{
    public boolean                    encodeStrings = false;
    public ArithmeticObfuscationInfo  arithmetic    = null;
    public ConstantObfuscationInfo    constants     = null;
    public ControlFlowObfuscationInfo controlflow   = null;

    public static void setClassObfuscationInfo(Clazz clazz) {
        clazz.setObfuscationInfo(new ObfuscationInfo());
    }

    public static void setMethodObfuscationInfo(Method meth) {
        meth.setObfuscationInfo(new ObfuscationInfo());
    }

    public static void setFieldObfuscationInfo(Field field) {
        field.setObfuscationInfo(new ObfuscationInfo());
    }

    public static void setConstantObfuscationInfo(Constant constant) {
        constant.setObfuscationInfo(new ObfuscationInfo());
    }

    public static ObfuscationInfo getObfuscationInfo(Clazz clazz) {
        return (ObfuscationInfo)clazz.getObfuscationInfo();
    }

    public static ObfuscationInfo getObfuscationInfo(Method meth) {
        return (ObfuscationInfo)meth.getObfuscationInfo();
    }

    public static ObfuscationInfo getObfuscationInfo(Field field) {
        return (ObfuscationInfo)field.getObfuscationInfo();
    }

    public static ObfuscationInfo getObfuscationInfo(Constant constant) {
        return (ObfuscationInfo)constant.getObfuscationInfo();
    }
}
