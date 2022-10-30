package dprotect;

import proguard.ClassSpecification;

import java.util.*;


/**
 * The extended dProtect configuration based on Proguard
 * @author Romain Thomas
 */

public class Configuration extends proguard.Configuration
{

    ///////////////////////////////////////////////////////////////////////////
    // Obfuscation options.
    ///////////////////////////////////////////////////////////////////////////


    /**
     * These options are used for deobfuscation purpose and
     * should not be used right now.
     */
    public ClassSpecification            deobfStrDecodeName;
    public String                        deobfStrXorKey;

    /**
     * List of obfuscation passes enbled
     */
    public List<String>                                 obfuscations;

    /**
     * List of classes which must be constant-obfuscated
     */
    public List<ConstantObfuscationClassSpecification>   obfuscateConstants;

    /**
     * List of classes which must be control-flow-obfuscated
     */
    public List<CFObfuscationClassSpecification>         obfuscateControlFlow;

    /**
     * List of classes for which arithmetic operations must
     * be obfuscated
     */
    public List<ArithmeticObfuscationClassSpecification> obfuscateArithmetic;

    /**
     * List of classes for which strings must be obfuscated
     */
    public List<ClassSpecification>                      obfuscateStrings;

    /**
     * List of string that must also be obfuscated
     */
    public List<String>                                  obfuscateStringsList;


    /**
     * Seed used for the random generator of obfuscation passes
     */
    public Integer                                       seed = null;
}
