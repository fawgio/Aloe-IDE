package io.github.fawgio.aloe;

import java.io.File;

public class FileNode {
    File file;
    boolean isRoot;
    public FileNode(File file, boolean isRoot) {
        this.file = file;
        this.isRoot = isRoot;
    }

    public File getFile(){
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String toString(){
        return isRoot ? file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\")+1) + " ( " + file.getAbsolutePath() + " )" : file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\")+1);
    }
}
