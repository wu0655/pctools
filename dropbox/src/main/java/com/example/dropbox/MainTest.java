package com.example.dropbox;

import java.io.File;
import java.io.IOException;

public class MainTest {

    public static void main(String args[]) throws IOException {
        for (int i = 0; i < args.length; i++) {
            System.out.println("args[" + i + "]=" + args[i]);
        }

        File f = new File(args[0]);
        if (!f.isDirectory()) {
            System.out.println("please input dir where dropbox file is saved.\n");
            return;
        }

        long start = System.currentTimeMillis();
        DropBoxHandler test = new DropBoxHandler();
        test.ScanDir(f);
        test.printResult();
        long stop = System.currentTimeMillis();
        //test.test();
        System.out.println("analysis tooks " + (stop - start) + " ms");
    }
}
