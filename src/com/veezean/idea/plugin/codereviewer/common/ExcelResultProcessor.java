package com.veezean.idea.plugin.codereviewer.common;

import com.veezean.idea.plugin.codereviewer.consts.InputTypeDefine;
import com.veezean.idea.plugin.codereviewer.model.Column;
import com.veezean.idea.plugin.codereviewer.model.RecordColumns;
import com.veezean.idea.plugin.codereviewer.model.ReviewComment;
import com.veezean.idea.plugin.codereviewer.util.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Excel格式结果文件处理器
 *
 * @author Wang Weiren
 * @since 2023/1/14
 */
public class ExcelResultProcessor {

    /**
     * 导出评审意见
     *
     * @param savePath 目标存储路径
     * @param contents 评审内容
     * @throws Exception 导出失败时抛出
     */
    public static void export(String savePath, List<ReviewComment> contents) throws Exception {
        // 自动生成与IDEA插件配置的显示表格内容一样的Excel表格
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOutputStream = new FileOutputStream(savePath)) {
            XSSFSheet sheet = workbook.createSheet("review comments");

            // 获取配置的字段信息并生成表头
            RecordColumns recordColumns = GlobalConfigManager.getInstance().getCustomConfigColumns();
            List<Column> availableColumns = recordColumns.getExcelColumns();
            int columnSize = availableColumns.size();

            // 设置列名
            XSSFCellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFColor headerCellColor = new XSSFColor(new DefaultIndexedColorMap());
            headerCellColor.setRGB(new byte[]{(byte) 255, (byte) 217, (byte) 35});
            headerCellStyle.setFillForegroundColor(headerCellColor);
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            XSSFFont headerCellFont = workbook.createFont();
            headerCellFont.setBold(true);
            headerCellFont.setFontName("黑体");
            headerCellFont.setFontHeightInPoints((short) 12);
            headerCellStyle.setFont(headerCellFont);

            XSSFRow rowHeader = sheet.createRow(0);
            for (int i = 0; i < columnSize; i++) {
                Column column = availableColumns.get(i);
                XSSFCell cell = rowHeader.createCell(i);
                cell.setCellValue(column.getShowName());
                cell.setCellStyle(headerCellStyle);

                // 根据配置文件设置列宽，excel列宽要换算乘以256
                sheet.setColumnWidth(i, column.getExcelColumnWidth() * 256);

                // 根据配置文件设置，如果指定的是下拉框，则设定excel数据约束仅可以选择下拉框
                if (InputTypeDefine.COMBO_BOX.getValue().equalsIgnoreCase(column.getInputType())) {
                    String[] values = column.getEnumValues().toArray(new String[0]);
                    XSSFDataValidationHelper dataValidationHelper = new XSSFDataValidationHelper(sheet);
                    XSSFDataValidationConstraint vConstraint =
                            (XSSFDataValidationConstraint) dataValidationHelper.createExplicitListConstraint(values);
                    CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, i, i);
                    XSSFDataValidation validation =
                            (XSSFDataValidation) dataValidationHelper.createValidation(vConstraint, addressList);
                    sheet.addValidationData(validation);
                }
            }

            // 逐行写入数据
            XSSFCellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setWrapText(true);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            int dataRowIdx = 1;
            for (ReviewComment comment : contents) {
                XSSFRow dataRow = sheet.createRow(dataRowIdx);
                for (int i = 0; i < columnSize; i++) {
                    XSSFCell dataCell = dataRow.createCell(i);
                    dataCell.setCellValue(comment.getPropValue(availableColumns.get(i).getColumnCode()));
                    dataCell.setCellStyle(dataCellStyle);
                }

                dataRowIdx++;
            }

            workbook.write(fileOutputStream);
        } catch (Exception e) {
            Logger.error("数据导出失败", e);
            throw e;
        }
    }


    /**
     * 导入excel表格
     *
     * @param path excel路径
     * @return 导入后的数据
     * @throws Exception 如果导入操作失败时抛出
     */
    public static List<ReviewComment> importExcel(String path) throws Exception {
        List<ReviewComment> models = new ArrayList<>();

        InputStream xlsFile = null;
        XSSFWorkbook workbook = null;
        try {
            xlsFile = new FileInputStream(path);
            // 获得工作簿对象
            workbook = new XSSFWorkbook(xlsFile);
            // 获得所有工作表,0指第一个表格
            XSSFSheet sheet = workbook.getSheetAt(0);
            Map<Integer, String> columnMaps = new HashMap<>();
            XSSFRow headerRow = sheet.getRow(0);
            short lastCellNum = headerRow.getLastCellNum();

            // 根据系统配置的列名，以及excel的表头，自动映射
            RecordColumns recordColumns =
                    GlobalConfigManager.getInstance().getCustomConfigColumns();
            for (int i = 0; i < lastCellNum; i++) {
                XSSFCell cell = headerRow.getCell(i);
                Optional<Column> column = recordColumns.getColumnByShowName(cell.getStringCellValue().trim());
                if (column.isPresent()) {
                    columnMaps.put(i, column.get().getColumnCode());
                }
            }

            // 逐行解析内容，转换为评审意见对象
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNum; i++) {
                XSSFRow dataRow = sheet.getRow(i);
                ReviewComment comment = new ReviewComment();
                columnMaps.forEach((colIndex, colName) -> {
                    XSSFCell dataRowCell = dataRow.getCell(colIndex);
                    dataRowCell.setCellType(CellType.STRING);
                    comment.setPropValue(colName, dataRowCell.getStringCellValue());
                    comment.setLineRangeInfo();
                });
                models.add(comment);
            }
        } catch (Exception ex) {
            throw new Exception(ex);
        } finally {
            CommonUtil.closeQuitely(xlsFile);
            CommonUtil.closeQuitely(workbook);
        }

        return models;
    }
}
