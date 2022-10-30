package dprotect.runtime.util;

public class Helpers {
    public static String getNormalizedClassName(Class cls)
    {
        return cls.getCanonicalName().replace('.', '/');
    }
}
