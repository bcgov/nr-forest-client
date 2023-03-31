package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.exception.CannotWriteReportException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class SheetWriter {

  private final BufferedOutputStream fileOut;
  // Create a new workbook
  private final XSSFWorkbook workbook;
  //Create the main sheet
  private final Sheet sheet;
  //This sets the initial line for the entries
  private final AtomicInteger rowCounter = new AtomicInteger(18);
  //This evaluator is to execute all the formulas/functions
  private final FormulaEvaluator evaluator;
  //Setting all the available styles
  private final CellStyle rightAlignment;
  private final CellStyle rightPercentAlignment;
  private final CellStyle leftAlignment;
  private final CellStyle leftTitles;
  private final CellStyle rightTitles;
  private final CellStyle centerTitles;
  private final Font boldFont;

  public SheetWriter(File sheetFile) {

    try {
      this.fileOut = new BufferedOutputStream(Files.newOutputStream(sheetFile.toPath()));
    } catch (IOException e) {
      throw new CannotWriteReportException(
          "Cannot generate report file. Reason " + e.getMessage()
      );
    }

    this.workbook = new XSSFWorkbook();
    this.sheet = workbook.createSheet("C an A");

    this.rightAlignment = workbook.createCellStyle();
    this.rightPercentAlignment = workbook.createCellStyle();
    this.leftAlignment = workbook.createCellStyle();
    this.leftTitles = workbook.createCellStyle();
    this.rightTitles = workbook.createCellStyle();
    this.centerTitles = workbook.createCellStyle();
    this.boldFont = workbook.createFont();
    this.evaluator = workbook.getCreationHelper().createFormulaEvaluator();

    initializeStyles();
    initializeDataHeader();
  }

  private void initializeDataHeader() {
    valueRow(17, centerTitles,
        "Client Number",
        "Registration Number",
        "Found?",
        "Active in OrgBook?",
        "Same name?",
        "Name in Client",
        "Name in OB"
    );
    CellRangeAddress range = CellRangeAddress.valueOf("B18:G18");
    sheet.setAutoFilter(range);
  }

  private void initializeStyles() {

    boldFont.setBold(true);
    rightAlignment.setAlignment(HorizontalAlignment.RIGHT);
    rightAlignment.setBorderBottom(BorderStyle.THIN);
    rightAlignment.setBorderTop(BorderStyle.THIN);
    rightAlignment.setBorderLeft(BorderStyle.THIN);
    rightAlignment.setBorderRight(BorderStyle.THIN);

    rightPercentAlignment.setAlignment(HorizontalAlignment.RIGHT);
    rightPercentAlignment.setBorderBottom(BorderStyle.THIN);
    rightPercentAlignment.setBorderTop(BorderStyle.THIN);
    rightPercentAlignment.setBorderLeft(BorderStyle.THIN);
    rightPercentAlignment.setBorderRight(BorderStyle.THIN);
    rightPercentAlignment.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));


    leftAlignment.setAlignment(HorizontalAlignment.LEFT);
    leftAlignment.setBorderBottom(BorderStyle.THIN);
    leftAlignment.setBorderTop(BorderStyle.THIN);
    leftAlignment.setBorderLeft(BorderStyle.THIN);
    leftAlignment.setBorderRight(BorderStyle.THIN);

    centerTitles.setAlignment(HorizontalAlignment.CENTER);
    centerTitles.setFont(boldFont);
    centerTitles.setBorderBottom(BorderStyle.THIN);
    centerTitles.setBorderTop(BorderStyle.THIN);
    centerTitles.setBorderLeft(BorderStyle.THIN);
    centerTitles.setBorderRight(BorderStyle.THIN);

    leftTitles.setAlignment(HorizontalAlignment.LEFT);
    leftTitles.setFont(boldFont);
    leftTitles.setBorderBottom(BorderStyle.THIN);
    leftTitles.setBorderTop(BorderStyle.THIN);
    leftTitles.setBorderLeft(BorderStyle.THIN);
    leftTitles.setBorderRight(BorderStyle.THIN);

    rightTitles.setAlignment(HorizontalAlignment.RIGHT);
    rightTitles.setFont(boldFont);
    rightTitles.setBorderBottom(BorderStyle.THIN);
    rightTitles.setBorderTop(BorderStyle.THIN);
    rightTitles.setBorderLeft(BorderStyle.THIN);
    rightTitles.setBorderRight(BorderStyle.THIN);
  }

  public void write(ClientPublicViewDto client) {

    valueRow(
        rowCounter.getAndIncrement(),
        leftAlignment,
        client.clientNumber(),
        client.incorporationNumber(),
        client.found(),
        client.active(),
        client.sameName(),
        client.clientName(),
        client.orgBookName()
    );
    try {
      workbook.write(fileOut);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void complete() {
    generateCountData();
    close();
  }

  private void close() {
    try {
      workbook.write(fileOut);
      fileOut.close();
      workbook.close();
      log.debug("Saving and closing file");
    } catch (Exception e) {
      log.error("Error while closing spreadsheet", e);
    }
  }

  private void generateCountData() {

    totalClientInfo();
    totalFoundInfo();
    totalOrgBookActiveInfo();
    totalPartialInfo();

    evaluator.evaluateAll();
    for (int columnId = 0; columnId < 8; columnId++) {
      sheet.autoSizeColumn(columnId);
    }
  }

  private void totalFoundInfo() {
    headerRow(3);
    infoRow(4,
        "Total Found (Registration Number AND Name)",
        getCountIf("D", "F"),
        getPercentage(5, 2)
    );
    infoRow(5,
        "Total Partially Found (Registration Number OR Name)",
        getCountIf("D", "PF"),
        getPercentage(6, 2)
    );
    infoRow(6,
        "Total Not Found",
        getCountIf("D", "NF"),
        getPercentage(7, 2)
    );
  }

  private void totalOrgBookActiveInfo() {
    headerRow(8);
    infoRow(9,
        "Total of Active Found in OB",
        getCountIfs("E", "Yes", "F"),
        getPercentage(10, 5)
    );
    infoRow(10,
        "Total of Inactive Found Clients in OB",
        getCountIfs("E", "No", "F"),
        getPercentage(11, 5)
    );

  }

  private void totalPartialInfo() {
    headerRow(12);
    infoRow(13,
        "Total of Partially Found by Name",
        getCountIfs("F", "Yes", "PF"),
        getPercentage(14, 6)
    );
    infoRow(14,
        "Total of Partially Found by Registration Number",
        getCountIfs("F", "No", "PF"),
        getPercentage(15, 6)
    );
  }

  private void totalClientInfo() {
    Row totalRow = sheet.createRow(1);
    setMergedValue(totalRow, 1, "Total Clients", leftTitles);
    setCellValue(totalRow, 4, String.valueOf(rowCounter.get() - 18), rightTitles);
  }

  private void headerRow(int rowNumber) {
    Row headerRow = sheet.createRow(rowNumber);
    setCellValue(headerRow, 4, "# of Records", centerTitles);
    setCellValue(headerRow, 5, "Percentage", centerTitles);
  }

  private void valueRow(int rowNumber, CellStyle style, String... contents) {
    Row valueRow = sheet.createRow(rowNumber);

    for (int columnId = 1; columnId <= contents.length; columnId++) {
      Cell cell = valueRow.createCell(columnId);
      cell.setCellValue(contents[columnId - 1]);
      cell.setCellStyle(style);
    }
  }

  private void infoRow(
      int rowNumber,
      String contentTitle,
      String contentRecords,
      String contentPercentage
  ) {
    Row valueRow = sheet.createRow(rowNumber);
    setMergedValue(valueRow, 1, contentTitle, leftAlignment);
    setCellFormula(valueRow, 4, contentRecords, rightAlignment);
    setCellFormula(valueRow, 5, contentPercentage, rightPercentAlignment);
  }

  private void setCellValue(Row row, int cellId, String cellValue, CellStyle style) {
    setCell(row, cellId, cellValue, style, false);
  }

  private void setCellFormula(Row row, int cellId, String cellValue, CellStyle style) {
    setCell(row, cellId, cellValue, style, true);
  }

  private void setCell(
      Row row,
      int cellId,
      String cellValue,
      CellStyle style,
      boolean formula
  ) {
    Cell cell = row.createCell(cellId);
    if (formula) {
      cell.setCellFormula(cellValue);
    } else {
      cell.setCellValue(cellValue);
    }
    cell.setCellStyle(style);
  }

  private void setMergedValue(Row row, int cellId, String cellValue, CellStyle style) {
    setCellValue(row, cellId, cellValue, style);
    CellRangeAddress mergedRegion = new CellRangeAddress(
        row.getRowNum(),
        row.getRowNum(),
        cellId,
        cellId + 2
    );
    sheet.addMergedRegion(mergedRegion);
  }

  private String getPercentage(int columnValue, int columnTotal) {
    return String.format("IF(E%d>0,(E%d/E%d),0)", columnTotal, columnValue, columnTotal);
  }

  private String getCountIf(String column, String value) {
    return String.format("COUNTIF(%S19:D%d,\"%s\")", column, rowCounter.get(), value);
  }

  private String getCountIfs(String column, String value, String reference) {
    return String.format("COUNTIFS(%s19:%s%d,\"%s\",D19:D%d,\"%s\")", column, column,
        rowCounter.get(), value, rowCounter.get(), reference);
  }
}
