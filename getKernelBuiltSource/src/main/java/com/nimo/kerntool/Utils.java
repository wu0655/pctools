package com.nimo.kerntool;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static File getSymlink(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("File must not be null");

        File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir = file.getParentFile().getCanonicalFile();
            canon = new File(canonDir, file.getName());
        }

        System.out.println("canon.getCanonicalFile()=" + canon.getCanonicalFile());
        System.out.println("canon.getAbsoluteFile()=" + canon.getAbsoluteFile());
        if (!canon.getCanonicalFile().equals(canon.getAbsoluteFile())) {
            return canon.getCanonicalFile();
        } else {
            return null;
        }
    }
}
