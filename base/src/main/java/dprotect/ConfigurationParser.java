package dprotect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import proguard.ClassSpecification;
import proguard.ParseException;
import proguard.WordReader;
import proguard.classfile.JavaAccessConstants;
import proguard.classfile.util.ClassUtil;

import java.io.*;
import java.net.*;
import java.util.*;

public class ConfigurationParser extends proguard.ConfigurationParser
{

    private static final Logger logger = LogManager.getLogger(ConfigurationParser.class);

    public ConfigurationParser(String[]   args,
                               Properties properties) throws IOException
    {
        super(args, properties);
    }



    public ConfigurationParser(String[]   args,
                               File       baseDir,
                               Properties properties) throws IOException
    {
        super(args, baseDir, properties);
    }



    public ConfigurationParser(String     lines,
                               String     description,
                               File       baseDir,
                               Properties properties) throws IOException
    {
        super(lines, description, baseDir, properties);
    }



    public ConfigurationParser(File file) throws IOException
    {
        super(file);
    }



    public ConfigurationParser(File       file,
                               Properties properties) throws IOException
    {
        super(file, properties);
    }



    public ConfigurationParser(URL        url,
                               Properties properties) throws IOException
    {
        super(url, properties);
    }



    public ConfigurationParser(WordReader reader,
                               Properties properties) throws IOException
    {
        super(reader, properties);
    }


    public void parse(Configuration configuration)
    throws ParseException, IOException
    {
        while (nextWord != null)
        {
            lastComments = reader.lastComments();

            // First include directives.
            if      (ConfigurationConstants.AT_DIRECTIVE                                     .startsWith(nextWord) ||
                     ConfigurationConstants.INCLUDE_DIRECTIVE                                .startsWith(nextWord)) configuration.lastModified                          = super.parseIncludeArgument(configuration.lastModified);
            else if (ConfigurationConstants.BASE_DIRECTORY_DIRECTIVE                         .startsWith(nextWord)) super.parseBaseDirectoryArgument();

            // Then configuration options with or without arguments.
            else if (ConfigurationConstants.INJARS_OPTION                                    .startsWith(nextWord)) configuration.programJars                           = super.parseClassPathArgument(configuration.programJars, false, true);
            else if (ConfigurationConstants.OUTJARS_OPTION                                   .startsWith(nextWord)) configuration.programJars                           = super.parseClassPathArgument(configuration.programJars, true, false);
            else if (ConfigurationConstants.LIBRARYJARS_OPTION                               .startsWith(nextWord)) configuration.libraryJars                           = super.parseClassPathArgument(configuration.libraryJars, false, false);
            else if (ConfigurationConstants.RESOURCEJARS_OPTION                              .startsWith(nextWord)) throw new ParseException("The '-resourcejars' option is no longer supported. Please use the '-injars' option for all input");
            else if (ConfigurationConstants.SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION           .startsWith(nextWord)) configuration.skipNonPublicLibraryClasses           = super.parseNoArgument(true);
            else if (ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASSES_OPTION      .startsWith(nextWord)) configuration.skipNonPublicLibraryClasses           = super.parseNoArgument(false);
            else if (ConfigurationConstants.DONT_SKIP_NON_PUBLIC_LIBRARY_CLASS_MEMBERS_OPTION.startsWith(nextWord)) configuration.skipNonPublicLibraryClassMembers      = super.parseNoArgument(false);
            else if (ConfigurationConstants.TARGET_OPTION                                    .startsWith(nextWord)) configuration.targetClassVersion                    = super.parseClassVersion();
            else if (ConfigurationConstants.DONT_COMPRESS_OPTION                             .startsWith(nextWord)) configuration.dontCompress                          = super.parseCommaSeparatedList("file name", true, true, false, true, false, true, false, false, false, configuration.dontCompress);
            else if (ConfigurationConstants.ZIP_ALIGN_OPTION                                 .startsWith(nextWord)) configuration.zipAlign                              = super.parseIntegerArgument();
            else if (ConfigurationConstants.FORCE_PROCESSING_OPTION                          .startsWith(nextWord)) configuration.lastModified                          = super.parseNoArgument(Long.MAX_VALUE);

            else if (ConfigurationConstants.IF_OPTION                                        .startsWith(nextWord)) configuration.keep                                  = super.parseIfCondition(configuration.keep);
            else if (ConfigurationConstants.KEEP_OPTION                                      .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, true,  true,  false, false, false, null);
            else if (ConfigurationConstants.KEEP_CLASS_MEMBERS_OPTION                        .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, false, true,  false, false, false, null);
            else if (ConfigurationConstants.KEEP_CLASSES_WITH_MEMBERS_OPTION                 .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, false, true,  false, true,  false, null);
            else if (ConfigurationConstants.KEEP_NAMES_OPTION                                .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, true,  true,  false, false, true,  null);
            else if (ConfigurationConstants.KEEP_CLASS_MEMBER_NAMES_OPTION                   .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, false, true,  false, false, true,  null);
            else if (ConfigurationConstants.KEEP_CLASSES_WITH_MEMBER_NAMES_OPTION            .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, false, true,  false, true,  true,  null);
            else if (ConfigurationConstants.KEEP_CODE_OPTION                                 .startsWith(nextWord)) configuration.keep                                  = super.parseKeepClassSpecificationArguments(configuration.keep, false, false, true,  false, false, null);
            else if (ConfigurationConstants.PRINT_SEEDS_OPTION                               .startsWith(nextWord)) configuration.printSeeds                            = super.parseOptionalFile();

            // After '-keep'.
            else if (ConfigurationConstants.KEEP_DIRECTORIES_OPTION                          .startsWith(nextWord)) configuration.keepDirectories                       = super.parseCommaSeparatedList("directory name", true, true, false, true, false, true, true, false, false, configuration.keepDirectories);

            else if (ConfigurationConstants.DONT_SHRINK_OPTION                               .startsWith(nextWord)) configuration.shrink                                = super.parseNoArgument(false);
            else if (ConfigurationConstants.PRINT_USAGE_OPTION                               .startsWith(nextWord)) configuration.printUsage                            = super.parseOptionalFile();
            else if (ConfigurationConstants.WHY_ARE_YOU_KEEPING_OPTION                       .startsWith(nextWord)) configuration.whyAreYouKeeping                      = super.parseClassSpecificationArguments(configuration.whyAreYouKeeping);

            else if (ConfigurationConstants.DONT_OPTIMIZE_OPTION                             .startsWith(nextWord)) configuration.optimize                              = super.parseNoArgument(false);
            else if (ConfigurationConstants.OPTIMIZATION_PASSES                              .startsWith(nextWord)) configuration.optimizationPasses                    = super.parseIntegerArgument();
            else if (ConfigurationConstants.OPTIMIZATIONS                                    .startsWith(nextWord)) configuration.optimizations                         = super.parseCommaSeparatedList("optimization name", true, false, false, false, false, true, false, false, false, configuration.optimizations);
            else if (ConfigurationConstants.ASSUME_NO_SIDE_EFFECTS_OPTION                    .startsWith(nextWord)) configuration.assumeNoSideEffects                   = super.parseAssumeClassSpecificationArguments(configuration.assumeNoSideEffects);
            else if (ConfigurationConstants.ASSUME_NO_EXTERNAL_SIDE_EFFECTS_OPTION           .startsWith(nextWord)) configuration.assumeNoExternalSideEffects           = super.parseAssumeClassSpecificationArguments(configuration.assumeNoExternalSideEffects);
            else if (ConfigurationConstants.ASSUME_NO_ESCAPING_PARAMETERS_OPTION             .startsWith(nextWord)) configuration.assumeNoEscapingParameters            = super.parseAssumeClassSpecificationArguments(configuration.assumeNoEscapingParameters);
            else if (ConfigurationConstants.ASSUME_NO_EXTERNAL_RETURN_VALUES_OPTION          .startsWith(nextWord)) configuration.assumeNoExternalReturnValues          = super.parseAssumeClassSpecificationArguments(configuration.assumeNoExternalReturnValues);
            else if (ConfigurationConstants.ASSUME_VALUES_OPTION                             .startsWith(nextWord)) configuration.assumeValues                          = super.parseAssumeClassSpecificationArguments(configuration.assumeValues);
            else if (ConfigurationConstants.ALLOW_ACCESS_MODIFICATION_OPTION                 .startsWith(nextWord)) configuration.allowAccessModification               = super.parseNoArgument(true);
            else if (ConfigurationConstants.MERGE_INTERFACES_AGGRESSIVELY_OPTION             .startsWith(nextWord)) configuration.mergeInterfacesAggressively           = super.parseNoArgument(true);

            else if (ConfigurationConstants.DONT_OBFUSCATE_OPTION                            .startsWith(nextWord)) configuration.obfuscate                             = super.parseNoArgument(false);
            else if (ConfigurationConstants.PRINT_MAPPING_OPTION                             .startsWith(nextWord)) configuration.printMapping                          = super.parseOptionalFile();
            else if (ConfigurationConstants.APPLY_MAPPING_OPTION                             .startsWith(nextWord)) configuration.applyMapping                          = super.parseFile();
            else if (ConfigurationConstants.OBFUSCATION_DICTIONARY_OPTION                    .startsWith(nextWord)) configuration.obfuscationDictionary                 = super.parseURL();
            else if (ConfigurationConstants.CLASS_OBFUSCATION_DICTIONARY_OPTION              .startsWith(nextWord)) configuration.classObfuscationDictionary            = super.parseURL();
            else if (ConfigurationConstants.PACKAGE_OBFUSCATION_DICTIONARY_OPTION            .startsWith(nextWord)) configuration.packageObfuscationDictionary          = super.parseURL();
            else if (ConfigurationConstants.OVERLOAD_AGGRESSIVELY_OPTION                     .startsWith(nextWord)) configuration.overloadAggressively                  = super.parseNoArgument(true);
            else if (ConfigurationConstants.USE_UNIQUE_CLASS_MEMBER_NAMES_OPTION             .startsWith(nextWord)) configuration.useUniqueClassMemberNames             = super.parseNoArgument(true);
            else if (ConfigurationConstants.DONT_USE_MIXED_CASE_CLASS_NAMES_OPTION           .startsWith(nextWord)) configuration.useMixedCaseClassNames                = super.parseNoArgument(false);
            else if (ConfigurationConstants.KEEP_PACKAGE_NAMES_OPTION                        .startsWith(nextWord)) configuration.keepPackageNames                      = super.parseCommaSeparatedList("package name", true, true, false, false, true, false, false, true, false, configuration.keepPackageNames);
            else if (ConfigurationConstants.FLATTEN_PACKAGE_HIERARCHY_OPTION                 .startsWith(nextWord)) configuration.flattenPackageHierarchy               = ClassUtil.internalClassName(super.parseOptionalArgument());
            else if (ConfigurationConstants.REPACKAGE_CLASSES_OPTION                         .startsWith(nextWord) ||
                     ConfigurationConstants.DEFAULT_PACKAGE_OPTION                           .startsWith(nextWord)) configuration.repackageClasses                      = ClassUtil.internalClassName(super.parseOptionalArgument());
            else if (ConfigurationConstants.KEEP_ATTRIBUTES_OPTION                           .startsWith(nextWord)) configuration.keepAttributes                        = super.parseCommaSeparatedList("attribute name", true, true, false, false, true, false, false, false, false, configuration.keepAttributes);
            else if (ConfigurationConstants.KEEP_PARAMETER_NAMES_OPTION                      .startsWith(nextWord)) configuration.keepParameterNames                    = super.parseNoArgument(true);
            else if (ConfigurationConstants.RENAME_SOURCE_FILE_ATTRIBUTE_OPTION              .startsWith(nextWord)) configuration.newSourceFileAttribute                = super.parseOptionalArgument();
            else if (ConfigurationConstants.ADAPT_CLASS_STRINGS_OPTION                       .startsWith(nextWord)) configuration.adaptClassStrings                     = super.parseCommaSeparatedList("class name", true, true, false, false, true, false, false, true, false, configuration.adaptClassStrings);
            else if (ConfigurationConstants.ADAPT_RESOURCE_FILE_NAMES_OPTION                 .startsWith(nextWord)) configuration.adaptResourceFileNames                = super.parseCommaSeparatedList("resource file name", true, true, false, true, false, true, false, false, false, configuration.adaptResourceFileNames);
            else if (ConfigurationConstants.ADAPT_RESOURCE_FILE_CONTENTS_OPTION              .startsWith(nextWord)) configuration.adaptResourceFileContents             = super.parseCommaSeparatedList("resource file name", true, true, false, true, false, true, false, false, false, configuration.adaptResourceFileContents);
            else if (ConfigurationConstants.DONT_PROCESS_KOTLIN_METADATA                     .startsWith(nextWord)) configuration.dontProcessKotlinMetadata             = parseNoArgument(true);
            else if (ConfigurationConstants.KEEP_KOTLIN_METADATA                             .startsWith(nextWord)) configuration.keepKotlinMetadata                    = parseKeepKotlinMetadata();

            else if (ConfigurationConstants.DONT_PREVERIFY_OPTION                            .startsWith(nextWord)) configuration.preverify                             = super.parseNoArgument(false);
            else if (ConfigurationConstants.MICRO_EDITION_OPTION                             .startsWith(nextWord)) configuration.microEdition                          = super.parseNoArgument(true);
            else if (ConfigurationConstants.ANDROID_OPTION                                   .startsWith(nextWord)) configuration.android                               = super.parseNoArgument(true);

            else if (ConfigurationConstants.KEY_STORE_OPTION                                 .startsWith(nextWord)) configuration.keyStores                             = super.parseFiles(configuration.keyStores);
            else if (ConfigurationConstants.KEY_STORE_PASSWORD_OPTION                        .startsWith(nextWord)) configuration.keyStorePasswords                     = super.parseCommaSeparatedList("keystore password", true, false, false, false, false, false, true, false, false, configuration.keyStorePasswords);
            else if (ConfigurationConstants.KEY_ALIAS_OPTION                                 .startsWith(nextWord)) configuration.keyAliases                            = super.parseCommaSeparatedList("key", true, false, false, false, false, false, true, false, false, configuration.keyAliases);
            else if (ConfigurationConstants.KEY_PASSWORD_OPTION                              .startsWith(nextWord)) configuration.keyPasswords                          = super.parseCommaSeparatedList("key password", true, false, false, false, false, false, true, false, false, configuration.keyPasswords);

            else if (ConfigurationConstants.VERBOSE_OPTION                                   .startsWith(nextWord)) configuration.verbose                               = super.parseNoArgument(true);
            else if (ConfigurationConstants.DONT_NOTE_OPTION                                 .startsWith(nextWord)) configuration.note                                  = super.parseCommaSeparatedList("class name", true, true, false, false, true, false, false, true, false, configuration.note);
            else if (ConfigurationConstants.DONT_WARN_OPTION                                 .startsWith(nextWord)) configuration.warn                                  = super.parseCommaSeparatedList("class name", true, true, false, false, true, false, false, true, false, configuration.warn);
            else if (ConfigurationConstants.IGNORE_WARNINGS_OPTION                           .startsWith(nextWord)) configuration.ignoreWarnings                        = super.parseNoArgument(true);
            else if (ConfigurationConstants.PRINT_CONFIGURATION_OPTION                       .startsWith(nextWord)) configuration.printConfiguration                    = super.parseOptionalFile();
            else if (ConfigurationConstants.DUMP_OPTION                                      .startsWith(nextWord)) configuration.dump                                  = super.parseOptionalFile();
            else if (ConfigurationConstants.ADD_CONFIGURATION_DEBUGGING_OPTION               .startsWith(nextWord)) configuration.addConfigurationDebugging             = super.parseNoArgument(true);
            else if (ConfigurationConstants.OPTIMIZE_AGGRESSIVELY                            .startsWith(nextWord)) configuration.optimizeConservatively                = parseNoArgument(false);
            else
            {
                parseObfuscationConfig(configuration);
            }
        }
    }

    public void parseObfuscationConfig(Configuration configuration)
    throws ParseException, IOException
    {
        if      (ConfigurationConstants.DEOBFUSCATE_XOR_STRINGS_DECODE_NAME              .startsWith(nextWord)) configuration.deobfStrDecodeName                    = super.parseClassSpecificationArguments(true, true, true);
        else if (ConfigurationConstants.DEOBFUSCATE_XOR_STRINGS_KEY                      .startsWith(nextWord)) configuration.deobfStrXorKey                        = super.parseOptionalArgument();

        else if (ConfigurationConstants.OBFUSCATIONS                                     .startsWith(nextWord)) configuration.obfuscations                          = super.parseCommaSeparatedList("obfuscations name", true, false, false, false, false, true, false, false, false, configuration.obfuscations);
        else if (ConfigurationConstants.OBFUSCATION_SEED                                 .startsWith(nextWord)) configuration.seed                                  = super.parseIntegerArgument();
        else if (ConfigurationConstants.OBFUSCATE_STRING                                 .startsWith(nextWord)) parseStringOpt(configuration);
        else if (ConfigurationConstants.OBFUSCATE_ARITHMETIC                             .startsWith(nextWord)) parseArithmeticOpt(configuration);
        else if (ConfigurationConstants.OBFUSCATE_CONSTANTS                              .startsWith(nextWord)) parseConstantsOpt(configuration);
        else if (ConfigurationConstants.OBFUSCATE_CONTROL_FLOW                           .startsWith(nextWord)) parseControlFlowOpt(configuration);
        else
        {
            throw new ParseException("Unknown option " + reader.locationDescription());
        }
    }

    private void parseStringOpt(Configuration configuration)
    throws ParseException, IOException
    {
        configuration.obfuscateStringsList = new ArrayList<String>();
        super.readNextWord("", false, false, false);
        if (ConfigurationConstants.CLASS_KEYWORD.startsWith(nextWord) ||
            JavaAccessConstants.INTERFACE.startsWith(nextWord)        ||
            JavaAccessConstants.ENUM.startsWith(nextWord))
        {
            if (configuration.obfuscateStrings == null) {
                configuration.obfuscateStrings = new ArrayList();
            }
            configuration.obfuscateStrings.add(super.parseClassSpecificationArguments(false, true, true));
        }
        else
        {
            configuration.obfuscateStringsList =
                super.parseCommaSeparatedList("strings to obfuscate",
                                        /* readFirstWord             */ false,
                                        /* allowEmptyList            */ false,
                                        /* defaultIfEmpty            */ null,
                                        /* expectClosingParenthesis  */ false,
                                        /* isFileName                */ false,
                                        /* checkJavaIdentifiers      */ false,
                                        /* allowGenerics             */ true,
                                        /* replaceSystemProperties   */ false,
                                        /* replaceExternalClassNames */ false,
                                        /* replaceExternalTypes      */ false,
                                        configuration.obfuscateStringsList);
        }
    }

    /*
     * Parse the -obfuscate-arithmetic option and its specifier.
     * The code of this pass is highly inspired from
     * proguard.ConfigurationParser.parseKeepClassSpecificationArguments
     */
    private void parseArithmeticOpt(Configuration configuration)
    throws ParseException, IOException
    {
        boolean skipFloat                         = false;
        ObfuscationClassSpecification.Level level = ObfuscationClassSpecification.Level.NONE;
        while (true)
        {
            readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                         "', '"      + JavaAccessConstants.INTERFACE +
                         "', or '"   + JavaAccessConstants.ENUM + "'",
                         false, false, true);

            if (!ConfigurationConstants.ARGUMENT_SEPARATOR_KEYWORD.equals(nextWord))
            {
                // Not a comma. Stop parsing the keep modifiers.
                break;
            }

            readNextWord("keyword '" + ConfigurationConstants.OBFUCATION_LEVEL_LOW +
                         "', '"      + ConfigurationConstants.OBFUCATION_LEVEL_MEDIUM +
                         "', '"      + ConfigurationConstants.OBFUCATION_LEVEL_HIGH +
                         "', or '"   + ConfigurationConstants.ARITHMETIC_OPT_SKIP_FLOAT + "'");

            if      (ConfigurationConstants.OBFUCATION_LEVEL_LOW.startsWith(nextWord))
            {
                level = ObfuscationClassSpecification.Level.LOW;
            }
            else if (ConfigurationConstants.OBFUCATION_LEVEL_MEDIUM.startsWith(nextWord))
            {
                level = ObfuscationClassSpecification.Level.MEDIUM;
            }
            else if (ConfigurationConstants.OBFUCATION_LEVEL_HIGH.startsWith(nextWord))
            {
                level = ObfuscationClassSpecification.Level.HIGH;
            }
            else if (ConfigurationConstants.ARITHMETIC_OPT_SKIP_FLOAT.startsWith(nextWord))
            {
                skipFloat = true;
            }
            else
            {
                throw new ParseException("Expecting keyword '" + ConfigurationConstants.OBFUCATION_LEVEL_LOW +
                                         "', '"                + ConfigurationConstants.OBFUCATION_LEVEL_MEDIUM +
                                         "', '"                + ConfigurationConstants.OBFUCATION_LEVEL_HIGH +
                                         "', or '"             + ConfigurationConstants.ARITHMETIC_OPT_SKIP_FLOAT +
                                         "' before " + reader.locationDescription());
            }
        }

        ClassSpecification classSpecification =
            parseClassSpecificationArguments(false, true, false);

        if (configuration.obfuscateArithmetic == null) {
            configuration.obfuscateArithmetic = new ArrayList<ArithmeticObfuscationClassSpecification>();
        }

        configuration.obfuscateArithmetic.add(
                new ArithmeticObfuscationClassSpecification(classSpecification,
                                                            level, skipFloat));
    }

    /*
     * Parse the -obfuscate-constants option. This option does not currently support extra modifiers
     * but the there is a free room for that.
     *
     * The code of this pass is highly inspired from
     * proguard.ConfigurationParser.parseKeepClassSpecificationArguments
     */
    private void parseConstantsOpt(Configuration configuration)
    throws ParseException, IOException
    {
        ObfuscationClassSpecification.Level level = ObfuscationClassSpecification.Level.NONE;

        readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                     "', '"      + JavaAccessConstants.INTERFACE +
                     "', or '"   + JavaAccessConstants.ENUM + "'",
                     false, false, true);

        ClassSpecification classSpecification =
            parseClassSpecificationArguments(false, true, false);

        if (configuration.obfuscateConstants == null) {
            configuration.obfuscateConstants = new ArrayList<ConstantObfuscationClassSpecification>();
        }

        configuration.obfuscateConstants.add(
                new ConstantObfuscationClassSpecification(classSpecification, level));
    }


    /*
     * Parse the -obfuscate-control-flow option. This option does not currently support extra modifiers
     * but the there is a free room for that.
     *
     * The code of this pass is highly inspired from
     * proguard.ConfigurationParser.parseKeepClassSpecificationArguments
     */
    private void parseControlFlowOpt(Configuration configuration)
    throws ParseException, IOException
    {
        ObfuscationClassSpecification.Level level = ObfuscationClassSpecification.Level.NONE;

        readNextWord("keyword '" + ConfigurationConstants.CLASS_KEYWORD +
                     "', '"      + JavaAccessConstants.INTERFACE +
                     "', or '"   + JavaAccessConstants.ENUM + "'",
                     false, false, true);

        ClassSpecification classSpecification =
            parseClassSpecificationArguments(false, true, false);

        if (configuration.obfuscateControlFlow == null) {
            configuration.obfuscateControlFlow = new ArrayList<CFObfuscationClassSpecification>();
        }

        configuration.obfuscateControlFlow.add(
                new CFObfuscationClassSpecification(classSpecification, level));
    }

}
