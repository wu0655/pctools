package com.nimo.kerntool;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;


public class BuildAnalysis {
    static final int UNINT = -1;
    static final int DEPS_LINE = 3;
    static final int SOURCE_LINE = 2;
    private static final boolean DEBUG = false;
    String m_deps_line = null;

    String m_blt_path;
    String m_kern_path;
    String m_curr_path;
    String m_out;
    String m_map_path;

    ArrayList<String> m_infoIds = null;

    Map<String, String> m_c_map = new HashMap<>();
    Map<String, String> m_h_map = new HashMap<>();
    Map<String, String> m_other_map = new HashMap<>();
    Map<String, String> m_wildcard_map = new HashMap<>();

    Map<String, String> m_cmd_map = new HashMap<>();


    int m_total_temp;
    int m_total;

    public BuildAnalysis(String kern_path, String built_path) {
        File f = new File(built_path);
        try {
            m_blt_path = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        f = new File(kern_path);
        try {
            m_kern_path = f.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        m_total = 0;
    }

    public boolean init() {
        if ((m_kern_path == null) || (m_blt_path == null))
            return false;

        String cmd;
        Process ps;
        boolean ret = false;
        try {
            cmd = "pwd";
            ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (m_curr_path == null)
                    m_curr_path = line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (m_curr_path != null) {
            if (m_out == null) {
                File dir = new File(m_curr_path + "/out");
                dir.deleteOnExit();
                dir.mkdir();

                m_out = dir.getAbsolutePath() + "/list.txt";
            }

            ret = true;
        }

        return ret;
    }

    public boolean runAnalysis() {
        int count = handleCmdFile();
        if (count == 0) {
            System.out.println("not valid info founded.\n");
            return false;
        }

        //dump(m_cmd_map);
        System.out.println(count + " .cmd files founded");

        Iterator iter = m_cmd_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            //System.out.println(key.toString() + " = " + val.toString());
            parseCmdFile(key.toString());
        }

        //m_total = m_c_map.size() + m_h_map.size();
        //flush_clear_map(m_c_map, false, "source file in cmd file");
        //flush_clear_map(m_h_map, true, "header file in cmd file");
        m_total = sortbyName();
        //dump(m_other_map);
        flush_key(m_other_map, false, false, null, m_out + "_m_other_map");
        flush_key(m_wildcard_map, false, false, null, m_out + "_m_wildcard_map");


        m_h_map.clear();
        m_cmd_map.clear();

        count = SearchDtbBuildFile();
        if (count == 0) {
            System.out.println("not valid dtb founded.\n");
        }

        iter = m_cmd_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            //System.out.println(key.toString() + " = " + val.toString());
            parseDtbCmdFile(key.toString());
        }
        flush_key(m_h_map, false, false, null, m_out + "_dtb");
        return true;
    }

    public int sortbyName() {
        m_infoIds = new ArrayList<String>();

        m_infoIds.addAll(m_h_map.keySet());
        m_infoIds.addAll(m_c_map.keySet());
        Collections.sort(m_infoIds);
        flush_ArrayList(m_infoIds, "");
        /*
        Collections.sort(m_infoIds, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                int ret;
                String t1 = o1.getKey();
                String t2 = o2.getKey();
                if (t2 > t1)
                    ret = -1;
                else if (t2 == t1)
                    ret = 0;
                else
                    ret = 1;
                return ret;
                //return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });
        */
        return m_infoIds.size();
    }

    String string_wildcard(String s) {
        String yy = s.trim();
        if (yy.length() < ":$(wildcard ".length()) {
            System.out.println("xxxxx string_wildcard Error+++++++ line:" + s);
            return "";
        } else {
            String xx = yy.substring(":$(wildcard ".length()-1, yy.length() - 1);
            return xx;
        }
    }

    int handle_deps(String line) {
        if (line.startsWith("deps_"))
            return DEPS_LINE;

        if (line.trim().length() == 0) {
            return UNINT;
        }

        //String path = line.split(" ")[0].trim();
        String path = line.substring(0, line.length()-1).trim();

        String filename = null;
        try {
            if (path.startsWith("/"))  {
                File x = new File(path);
                filename = x.getCanonicalPath();
                if (filename.startsWith(m_kern_path))
                    m_h_map.put(path, "");
                else
                    m_other_map.put(path, "");
            } else if (path.startsWith("$")) {
                String temp = string_wildcard(path.trim());
                m_wildcard_map.put(temp, "");
            } else {
                File x = new File(m_blt_path + "/" + path);
                if (x.isFile()) {
                    m_h_map.put(x.getCanonicalPath(), "");
                } else {
                    System.out.println("xxxxxError+++++++ line:" + line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        return (line.endsWith("\\")) ? DEPS_LINE : UNINT;
    }

    int handle_source(String line) {
        String arr[] = line.split(":=");
        String xx = arr[arr.length - 1].trim();
        if (test_flag) {
            System.out.println("line:" + line);
        }


        String filename = null;
        try {
            if (xx.startsWith("/"))  {
                File x = new File(xx);
                filename = x.getCanonicalPath();
            } else {
                File x = new File(m_blt_path + "/" + xx);
                filename = x.getCanonicalPath();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (test_flag) {
            System.out.println("filename:" + filename);
            System.out.println("m_kern_path:" + m_kern_path);
        }

        if (filename.startsWith(m_kern_path))
            m_c_map.put(filename, "");
        else {
            m_other_map.put(filename, "");
            System.out.println("other line:" + line);
        }
        return UNINT;
    }

    static boolean test_flag = false;
    int parseCmdFile(String path) {


        int before = m_h_map.size() + m_c_map.size() + m_other_map.size();
        int count = 0;
        //System.out.println("parseCmdFile:" + path);
        boolean is_file = true;
        try {
            File x = new File(path);
            if (! x.isFile()) {
                is_file = false;
                System.out.println("parseCmdFile:" + path + " is not file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            is_file = false;
        }

        if (! is_file)
            return 0;

        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            int line_type = -1;

            if (test_flag) {
                System.out.println("parseCmdFile:" + path);
            }
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println("linetype:" + line_type);
                line = line.trim();
                if (line_type == UNINT) {
                    if (line.startsWith("source_")) {
                        line_type = handle_source(line);
                    } else if (line.startsWith("deps_")) {
                        line_type = handle_deps(line);
                    }
                } else {
                    switch (line_type) {
                        case DEPS_LINE:
                            line_type = handle_deps(line);
                            break;
                    }
                }
            }

            br.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("parse " + path + " fail");
        }

        if (test_flag) {
            System.out.println("parseCmdFile finish:" + path);
            test_flag = false;
        }

        int after = m_h_map.size() + m_c_map.size() + m_other_map.size();
        if ((after == before) && (! path.endsWith(".built-in.o.cmd"))) {
            System.out.println("parseCmdFile:" + path + ". ++++++no valid source and deps");
        }
        return 0;
    }

    int parseDtbCmdFile(String path) {


        int before = m_h_map.size() + m_c_map.size() + m_other_map.size();
        int count = 0;
        //System.out.println("parseCmdFile:" + path);
        boolean is_file = true;
        try {
            File x = new File(path);
            if (! x.isFile()) {
                is_file = false;
                System.out.println("parseCmdFile:" + path + " is not file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            is_file = false;
        }

        if (! is_file)
            return 0;

        try {
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            int line_type = -1;

            if (test_flag) {
                System.out.println("parseCmdFile:" + path);
            }
            String line = null;
            while ((line = br.readLine()) != null) {
                //System.out.println("linetype:" + line_type);
                String[] s = line.trim().split(" ");
                for (String x : s) {
                    String ss = x.trim();
                    if (ss.endsWith(".h") || ss.endsWith(".dtsi") || ss.endsWith(".dts")) {

                        if (ss.startsWith("/")) {
                            m_h_map.put(ss, "");
                        } else {
                            File xx = new File(m_blt_path + "/" + ss);
                            if (! xx.isFile()) {
                                System.out.println("Error: parseDtbCmdFile:" + xx);
                            } else {
                                m_h_map.put(xx.getCanonicalPath(), "");
                            }
                        }

                    }
                }
            }

            br.close();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("parse " + path + " fail");
        }

        if (test_flag) {
            System.out.println("parseCmdFile finish:" + path);
            test_flag = false;
        }

        int after = m_h_map.size() + m_c_map.size() + m_other_map.size();
        if ((after == before) && (! path.endsWith(".built-in.o.cmd"))) {
            System.out.println("parseCmdFile:" + path + ". ++++++no valid source and deps");
        }
        return 0;
    }


    int printResult() {
        File out = new File(m_out);
        System.out.println("output = " + out.getAbsolutePath());
        System.out.println("total=" + m_total);

        System.out.println("m_other_map = " + m_other_map.size());

        System.out.println("m_wildcard_map = " + m_wildcard_map.size());
        return 0;
    }

    int test() {
        System.out.println("-------------------");
        dump(m_other_map);
        System.out.println("-------------------");
        return 0;
    }

    public int handleCmdFile() {
        int count = 0;
        File f = new File(m_blt_path);
        String bltpath = f.getAbsolutePath();
        String cmdfile = (f.getAbsoluteFile() + "/cmdfile");
        try {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(cmdfile));
                String line = reader.readLine();
                while (line != null) {
                    //System.out.println(line);
                    // read next line
                    line = reader.readLine();
                    File ff = new File(f.getAbsoluteFile() + "/" + line);
                    String name = ff.getName();
                    String path = ff.getAbsolutePath();
                    //System.out.println(path);
                    m_cmd_map.put(path,name);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        return m_cmd_map.size();
    }

    public int SearchDtbBuildFile() {
        int count = 0;
        File f = new File(m_blt_path + "/" + "arch/arm/dts");

        try {
            SearchBltDir search = new SearchBltDir(m_cmd_map, "");

            count += search.doSearchDir(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public int SearchBuildCmdFile() {
        int count = 0;
        File f = new File(m_blt_path + "");

        try {
            SearchBltDir search = new SearchBltDir(m_cmd_map, ".*cmd$");
            if (!search.init()) {
                System.out.println("Error while init search. path = " + f.getAbsolutePath());
                return -1;
            }

            count += search.doSearchDir(f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public int flush_clear_map(Map<String, String> map, boolean if_append, String str) {
        return flush_key(map, true, if_append, str, m_out);
    }

    public int flush_key(Map<String, String> map, boolean is_clear, boolean is_append, String str, String out_path) {
        int count = 0;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str + "++begin++\n");
            for (String key : map.keySet()) {
                bw.write(key + "\n");
                count++;
            }
            if (str != null)
                bw.write("#" + str + "++end++\n");
            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());
        if (is_clear)
            map.clear();
        return count;
    }

    public int flush_val(Map<String, String> map, boolean is_clear, boolean is_append, String str) {
        int count = 0;
        String out_path = m_out;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path, is_append);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str + "++begin++\n");
            for (String val : map.values()) {
                bw.write(val + "\n");
                count++;
            }
            if (str != null)
                bw.write("#" + str + "++end++\n");
            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + map.size());

        if (is_clear)
            map.clear();
        return count;
    }

    public int flush_ArrayList(ArrayList<String> list, String str) {
        int count = 0;
        String out_path = m_out;

        try {
            // write string to file
            FileWriter writer = new FileWriter(out_path);
            BufferedWriter bw = new BufferedWriter(writer);
            if (str != null)
                bw.write("#" + str + "++begin++\n");
            for (String key : list) {
                bw.write(key + "\n");
                count++;
            }
            if (str != null)
                bw.write("#" + str + "++end++\n");
            bw.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (DEBUG)
            System.out.println("flush num =" + list.size());
        return count;
    }

    int dump() {
        int total = 0;
        total += dump(m_c_map);
        System.out.println("++++++++++++++");
        total += dump(m_h_map);
        System.out.println("++++++++++++++");
        return total;
    }

    int dump(Map<String, String> map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            System.out.println(key.toString() + " = " + val.toString());
        }

        return map.size();
    }
}

