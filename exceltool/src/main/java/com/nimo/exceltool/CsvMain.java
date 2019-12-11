package com.nimo.exceltool;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CsvMain {

    private final class IgnoreType implements Comparable<IgnoreType>{
        private String ss;


        public IgnoreType(String[] csv){
            /*main + sub + minor + tag + buildtype
            * main = csv[I]
            * sub = csv[N]
            * minor = csv[K]
            * tag = csv[O]
            * buildtype= csv[]
            * */
            ss = csv['I' - 'A'] + ":" + csv['N' - 'A'] + csv['K' - 'A'] + csv['O' - 'A'];
        }
        @Override
        public int compareTo(IgnoreType ignoreType) {
            return ignoreType.ss.compareTo(this.ss);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) return true;
            if (other == null) return false;
            if (other.getClass() != this.getClass()) return false;
            IgnoreType that = (IgnoreType) other;
            return (this.compareTo(that) == 0);
        }

    }
    ArrayList<IgnoreType> ignore_list = new ArrayList<>();
    boolean ignore_list_init(String csvIgnore){
        boolean ret = false;
        CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(csvIgnore));
            String[] line;
            while ((line = reader.readNext()) != null) {
                IgnoreType node = new IgnoreType(line);
                ignore_list.add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    boolean isIgnoreType(IgnoreType tt) {
        for (IgnoreType type:ignore_list) {
            if (type.equals(tt)){
                return true;
            }
        }
        return false;
    }

    public static int csv_main(String csvFile) {
        int ret = -1;
        CSVReader reader = null;

        HashMap<String, Integer> sn_total = new HashMap<String, Integer>();
        String sn = null;
        String fieldid = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
            String[] line;
            while ((line = reader.readNext()) != null) {
                //System.out.println("Country [devicetype= " + line[3] + ", tag= " + line[14] + " , sn=" + line[11] + "]");
                //fieldid = line[5];
                //HttpUtils.getsInstance().DownloadFile(fieldid);
                sn = line[11];
                if (sn_total.containsKey(sn)) {
                    int i = sn_total.get(sn);
                    i++;
                    sn_total.put(sn,i);
                } else {
                    sn_total.put(sn, 1);
                }
            }

            ret = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<Map.Entry<String, Integer>> iterator = sn_total.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            System.out.println("sn=" + entry.getKey() + " count=" + entry.getValue());
        }

        System.out.println("total sn=" + sn_total.size());
        return 0;
    }
}
