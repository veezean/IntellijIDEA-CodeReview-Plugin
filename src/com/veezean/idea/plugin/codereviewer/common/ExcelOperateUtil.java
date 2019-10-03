package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.model.ReviewCommentInfoModel;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * <类功能简要描述>
 *
 * @author admin
 * @since 2019/10/2
 */
public class ExcelOperateUtil {

    public static void importExcel() {

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
