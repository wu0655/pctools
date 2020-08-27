package com.example.dropbox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class DropBoxHandler {
    HashMap<Long, File> mMap = new HashMap<>();
    SimpleDateFormat mDateFormat;

    DropBoxHandler() {
        mDateFormat = new SimpleDateFormat();// 格式化时间
        mDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");// a为am/pm的标记
    }

    public int ScanDir(File dir) {
        File flist[] = dir.listFiles();
        if (flist == null || flist.length == 0) {
            return 0;
        }

        try {
            for (File f : flist) {
                if (f.isFile()) {
                    handlefile(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mMap.size();
    }

    int handlefile(File f) {
        String name = f.getName();
        String s[]= name.split("[@.]");

        if (s.length < 2) {
            System.out.println("ignore " + name);
            return -1;
        }

        long time = Long.parseLong(s[1]);
        mMap.put(time, f);
        return 0;
    }

    int printResult() {
        if (mMap.size() == 0)
            return -1;

        //将keySet放入list
        ArrayList<Long> list= new ArrayList<>(mMap.keySet());
        //调用sort方法并重写比较器进行升/降序
        Collections.sort(list, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1>o2?1:-1;
            }
        });

        Iterator<Long> iterator = list.iterator();
        //迭代排序后的key的list
        while ((iterator.hasNext())){
            Long key = iterator.next();
            File value = mMap.get(key);
            String name = value.getName();
            String type = name.split("@")[0];

            if (type.equals("SYSTEM_BOOT"))
                System.out.println("\n" + mDateFormat.format(new Date(key)) + "\t" + name);
            else
                System.out.println("\t" + mDateFormat.format(new Date(key)) + "\t" + name);
        }
        System.out.println();

        return 0;
    }
}
