package dprotect;

public class ConfigurationConstants extends proguard.ConfigurationConstants
{
    public static final String OBFUSCATIONS           = "-obfuscations";
    public static final String OBFUSCATION_SEED       = "-obfuscation-seed";
    public static final String OBFUSCATE_STRING       = "-obfuscate-strings";
    public static final String OBFUSCATE_ARITHMETIC   = "-obfuscate-arithmetic";
    public static final String OBFUSCATE_CONSTANTS    = "-obfuscate-constants";
    public static final String OBFUSCATE_CONTROL_FLOW = "-obfuscate-control-flow";

    public static final String OBFUCATION_LEVEL_LOW    = "low";
    public static final String OBFUCATION_LEVEL_MEDIUM = "medium";
    public static final String OBFUCATION_LEVEL_HIGH   = "high";

    public static final String ARITHMETIC_OPT_SKIP_FLOAT = "skipfloat";

    public static final String DEOBFUSCATE_XOR_STRINGS_DECODE_NAME = "-deobfuscate-xor-strings-decode-name";
    public static final String DEOBFUSCATE_XOR_STRINGS_KEY         = "-deobfuscate-xor-strings-key";
}
