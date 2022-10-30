package dprotect.runtime.util;

import proguard.classfile.*;
import proguard.io.*;
import proguard.classfile.visitor.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * This class is used to load bytecode file (.class) as a Proguard ClassPool
 */
public class Loader {
    public static File currentJar() throws URISyntaxException {
        return new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    public static ClassPool getRuntimeClasses() {
        ClassPool classPool = new ClassPool();
        try {
            DataEntrySource source =
                new FileSource(
                        new File(currentJar().getPath()));

            DataEntryReader classReader =
                new NameFilteredDataEntryReader("dprotect/runtime/**.class",
                        new ClassReader(false, false, false, false, null,
                            new ClassNameFilter("**",
                                new ClassPoolFiller(classPool))));
            classReader = new JarReader(classReader);
            source.pumpDataEntries(classReader);
            return classPool;

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return classPool;
        }
    }

}
