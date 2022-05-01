package com.nimo.kerntool;

import java.io.File;
import java.io.IOException;

public class MainTest {
    //new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
    public static void main(String args[]) throws IOException {
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]=" + args[i]);
        }

        File f = new File(args[0]);
        if (!f.isDirectory()) {
            System.out.println("input is not dir\n");
            printUsuage();
            return;
        }


        String temp_path = args[0] + "/source";
        File kern_src = Utils.getSymlink(new File(temp_path));
        if (kern_src == null) {
            System.out.println("input dir is not valid\n");
            return;
        }


        String in_path = args[0];
        String kern_path = kern_src.getAbsolutePath();
        System.out.println("kern_path= " + kern_path);
        System.out.println("built_path= " + in_path);

        long start = System.currentTimeMillis();
        long curr = 0;
        BuildAnalysis test = new BuildAnalysis(kern_path, in_path);
        if (test.init()) {
            test.runAnalysis();
            test.printResult();
        }
        long stop = System.currentTimeMillis();
        //test.test();
        System.out.println("analysis tooks " + (stop - start) + " ms");
    }

    public static void printUsuage() {
        System.out.println("Usage: java -jar xx.jar [KERN_SOURCE_DIR] [KERNEL_BUILD_DIR]\n");
    }
}