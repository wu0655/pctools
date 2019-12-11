package com.nimo.exceltool;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class ExcelReader {
    static ExcelReader mInstance;

    public static ExcelReader getInstance() {
        if (mInstance == null) {
            mInstance = new ExcelReader();
        }
        return mInstance;
    }

    public String[] readExcelTitle(String filename) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(filename)) {
            POIFSFileSystem fs = new POIFSFileSystem(fileIn);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);

            HSSFRow row = sheet.getRow(0);

            int colNum = row.getPhysicalNumberOfCells();// 获取行的列数
            String[] titles = new String[colNum];
            for (int i = 0; i < titles.length; i++) {
                titles[i] = row.getCell(i).getStringCellValue();
            }
            return titles;
        }
    }

    public List<Map<String, String>> readExcelContent(String fileName) {
        return null;
    }
}