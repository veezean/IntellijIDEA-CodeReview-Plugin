package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <类功能简要描述>
 *
 * @author admin
 * @since 2019/10/2
 */
public class ExcelOperateUtil {

    public static List<ReviewCommentInfoModel> importExcel(String path) throws Exception{
        List<ReviewCommentInfoModel> models = new ArrayList<>();

        InputStream xlsFile = null;
        XSSFWorkbook workbook = null;
        try {
            xlsFile = new FileInputStream(path);
            // 获得工作簿对象
            workbook = new XSSFWorkbook(xlsFile);
            // 获得所有工作表,0指第一个表格
            XSSFSheet sheet = workbook.getSheet("Review Comments");

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum < 10) {
                return models;
            }



            for (int i = 10; i <= lastRowNum;i++) {
                XSSFRow row = sheet.getRow(i);
                try {
                    ReviewCommentInfoModel model = new ReviewCommentInfoModel();
                    String identifier = row.getCell(0).getStringCellValue();
                    model.setIdentifier(Long.valueOf(identifier));
                    model.setReviewer(row.getCell(1).getStringCellValue());
                    model.setComments(row.getCell(2).getStringCellValue());
                    model.setType(row.getCell(3).getStringCellValue());
                    model.setSeverity(row.getCell(4).getStringCellValue());
                    model.setFactor(row.getCell(5).getStringCellValue());
                    model.setFilePath(row.getCell(6).getStringCellValue());

                    String lineRange = row.getCell(7).getStringCellValue();
                    String[] split = lineRange.split("~");
                    model.setStartLine(Integer.parseInt(split[0].trim()));
                    model.setEndLine(Integer.parseInt(split[1].trim()));

                    model.setContent(row.getCell(8).getStringCellValue());
                    model.setDateTime(row.getCell(9).getStringCellValue());
                    models.add(model);
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            CommonUtil.closeQuitely(xlsFile);
            CommonUtil.closeQuitely(workbook);
        }

        return models;
    }

    public static void exportExcel(String path, List<ReviewCommentInfoModel> commentInfoModels) throws Exception {
        File destFile = new File(path);

        InputStream xlsFile = null;
        FileOutputStream fileOutputStream = null;
        XSSFWorkbook workbook = null;
        try {
//            copyFile(templateFile, destFile);

            xlsFile = ExcelOperateUtil.class.getClassLoader().getResourceAsStream("code-review-result.xlsx");
            // 获得工作簿对象
            workbook = new XSSFWorkbook(xlsFile);
            // 获得所有工作表,0指第一个表格
            XSSFSheet sheet = workbook.getSheet("Review Comments");


            //设置边框
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN); // 底部边框
            cellStyle.setBorderLeft(BorderStyle.THIN);  // 左边边框
            cellStyle.setBorderRight(BorderStyle.THIN); // 右边边框
            cellStyle.setBorderTop(BorderStyle.THIN); // 上边边框


            // 从0计数，从第11行开始写（index为10）
            int rowIndex = 10;
            for (ReviewCommentInfoModel value : commentInfoModels) {
                XSSFRow sheetRow = sheet.createRow(rowIndex);
                buildCell(sheetRow,cellStyle, 0, String.valueOf(value.getIdentifier()));
                buildCell(sheetRow,cellStyle, 1, value.getReviewer());
                buildCell(sheetRow,cellStyle, 2, value.getComments());
                buildCell(sheetRow,cellStyle, 3, value.getType());
                buildCell(sheetRow,cellStyle, 4, value.getSeverity());
                buildCell(sheetRow,cellStyle, 5, value.getFactor());
                buildCell(sheetRow,cellStyle, 6, value.getFilePath());
                buildCell(sheetRow,cellStyle, 7, value.getLineRange());
                buildCell(sheetRow,cellStyle, 8, value.getContent());
                buildCell(sheetRow,cellStyle, 9, value.getDateTime());
                buildCell(sheetRow,cellStyle, 10, "");
                buildCell(sheetRow,cellStyle, 11, "");
                buildCell(sheetRow,cellStyle, 12, "");
                buildCell(sheetRow,cellStyle, 13, "");

                rowIndex++;
            }

            //将excel写入
            fileOutputStream = new FileOutputStream(destFile);
            workbook.write(fileOutputStream);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
            CommonUtil.closeQuitely(xlsFile);
            CommonUtil.closeQuitely(fileOutputStream);
            CommonUtil.closeQuitely(workbook);
        }
    }

    private static void buildCell(XSSFRow sheetRow, XSSFCellStyle cellStyle, int cellColumnIndex, String value) {
        XSSFCell cell = sheetRow.createCell(cellColumnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private static void copyFile(File src, File dest) throws Exception {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            byte[] buffer = new byte[2048];
            while (true) {
                int ins = in.read(buffer);
                if (ins == -1) {
                    break;
                }
                out.write(buffer, 0, ins);
            }
            out.flush();
        } catch (Exception e) {
            throw new Exception("copy failed", e);
        } finally {
            CommonUtil.closeQuitely(in);
            CommonUtil.closeQuitely(out);
        }
    }

}
