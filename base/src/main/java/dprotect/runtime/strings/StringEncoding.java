package dprotect.runtime.strings;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StringEncoding {
    public static class EncodingPair {
        public Method encode;
        public Method decode;
        public EncodingPair(Method encode, Method decode)
        {
            this.encode = encode;
            this.decode = decode;
        }
    }

    private static final Logger logger = LogManager.getLogger(StringEncoding.class);
    public static List<EncodingPair> ENCODING_METHODS;

    /*
     * Register the decode/encode that aim at being used to protect
     * Strings
     */
    static {
        try {
            StringEncoding.ENCODING_METHODS = Arrays.asList(
                new EncodingPair(StringEncoding.class.getMethod("simpleXorEncode", String.class, char.class),
                                 StringEncoding.class.getMethod("simpleXorDecode", String.class)),
                new EncodingPair(StringEncoding.class.getMethod("simpleIndexedXorEncode", String.class, char.class),
                                 StringEncoding.class.getMethod("simpleIndexedXorDecode", String.class)),
                new EncodingPair(StringEncoding.class.getMethod("fibonnaciLFSREncode", String.class, char.class),
                                 StringEncoding.class.getMethod("fibonnaciLFSRDecode", String.class))
                );
        } catch (NoSuchMethodException e) {
            logger.fatal("{}", e);
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // Encoding/Decoding methods: (0xFAFB is a "magic" number dynamically replaced)
    ///////////////////////////////////////////////////////////////////////////

    /*
     * Simple xor
     */
    public static String simpleXorDecode(String str)
    {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char x = str.charAt(i);
            out.append((char) (x ^ (char) 0xFAFB));
        }
        return out.toString();
    }

    public static String simpleXorEncode(String str, char key)
    {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char x = str.charAt(i);
            out.append((char) (x ^ (char) key));
        }
        return out.toString();
    }

    /*
     * Simple xor that also involves the character's position
     */
    public static String simpleIndexedXorDecode(String str)
    {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char x = str.charAt(i);
            out.append((char) (x ^ (char) 0xFAFB ^ (i % (char) 0xFFFF)));
        }
        return out.toString();
    }

    public static String simpleIndexedXorEncode(String str, char key)
    {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            char x = str.charAt(i);
            out.append((char) (x ^ (char) key ^ (i % (char) 0xFFFF)));
        }
        return out.toString();
    }

    /*
     * Use a Fibonnaci LFSR stream
     */
    public static String fibonnaciLFSRDecode(String str)
    {
        char state = (char)0xFAFB;
        char lfsr = state;
        char bit;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i)
        {
            bit  = (char)(((lfsr >> 0) ^ (lfsr >> 2) ^ (lfsr >> 3) ^ (lfsr >> 5)) & 1);
            lfsr = (char)((lfsr >> 1) | (bit << 15));
            char x = str.charAt(i);
            out.append((char)(x ^ lfsr));
        }
        return out.toString();
    }

    public static String fibonnaciLFSREncode(String str, char key)
    {
        char state = (char)key;
        char lfsr = state;
        char bit;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < str.length(); ++i)
        {
            bit  = (char)(((lfsr >> 0) ^ (lfsr >> 2) ^ (lfsr >> 3) ^ (lfsr >> 5)) & 1);
            lfsr = (char)((lfsr >> 1) | (bit << 15));
            char x = str.charAt(i);
            out.append((char)(x ^ lfsr));
        }
        return out.toString();
    }
}
