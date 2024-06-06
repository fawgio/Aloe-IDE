package io.github.fawgio.aloe;

import java.io.*;

public class AloeClassLoader extends ClassLoader{
    File directory;

    public AloeClassLoader(File dir) {
        directory = dir;
    }

    public Class findClass (String classname){
        String filename = classname.replace('.',File.separatorChar)+".class";
        File f = new File(directory, filename);
        byte[] bytes = new byte[0];
        try {
            bytes = new BufferedInputStream(new FileInputStream(f)).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Class c = defineClass(classname, bytes, 0, bytes.length);
        resolveClass(c);
        return c;
    }
}
