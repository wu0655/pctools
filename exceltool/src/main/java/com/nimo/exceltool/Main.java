package com.nimo.exceltool;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.nimo.exceltool.CsvMain.csv_main;

/**
 * Created by wupeng on 18-9-19.
 */
public class Main {

    public static void main(String[] args) {
        int ret = -1;

       for (String s : args) {
            System.out.println("args=" + s);
        }

        String filename = args[0];
        File f = new File(filename);
        if (! f.exists()) {
            System.out.println("input file is not exist. file=" + args[0]);
        }

        System.out.println("name=" + f.getName());

        if (filename.endsWith("csv")) {
            ret = csv_main(filename);
        } else if (filename.endsWith("xls")) {
            ret =  excel_main(filename);
        }

        return;
    }

    public static int excel_main(String filename) {
       {
            com.nimo.exceltool.ExcelReader reader = ExcelReader.getInstance();
            try {
                String str[] = reader.readExcelTitle(filename);
                int i = 0;
                for (String s : str) {
                    System.out.println("col[" + (i++) + "]=" + s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
            List<Map<String, String>> list = reader.readExcelContent(args[0]);
            Map<String, String> map = null;
            for (int i = 0; i < list.size(); i++) {
                map = list.get(i);
                Map.Entry<String, String> entry = null;
                for (Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext(); ) {
                    entry = it.next();
                    System.out.println(entry.getKey() + "-->" + entry.getValue());
                }
                System.out.println("............");
            }
            */
        }

        return 0;
    }
}
