package ca.bc.gov.app.util;

import ca.bc.gov.app.dto.ClientPublicViewDto;
import ca.bc.gov.app.exception.CannotWriteReportException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

@Slf4j
public class SheetWriter {

  public static final String BLACK_COLOR = "black";
  private final BufferedOutputStream fileOut;
  // Create a new workbook
  private final Workbook workbook;
  //Create the main sheet
  private final Worksheet sheet;
  //This sets the initial line for the entries
  private final AtomicInteger rowCounter = new AtomicInteger(18);

  public SheetWriter(File sheetFile) {

    try {
      this.fileOut = new BufferedOutputStream(Files.newOutputStream(sheetFile.toPath()));
    } catch (IOException e) {
      throw new CannotWriteReportException(
          "Cannot generate report file. Reason " + e.getMessage()
      );
    }

    this.workbook = new Workbook(this.fileOut, "SampleName", "1.0");
    this.sheet = workbook.newWorksheet("C an A");


  }

  private void initializeDataHeader() {
    sheet
        .range(17, 1, rowCounter.get() - 1, 7)
        .createTable(
            "Client Number",
            "Registration Number",
            "Found?",
            "Active in OrgBook?",
            "Same name?",
            "Name in Client",
            "Name in OB"
        );
  }

  public void write(ClientPublicViewDto client) {

    valueRow(
        rowCounter.getAndIncrement(),
        client.clientNumber(),
        client.incorporationNumber(),
        client.found(),
        client.active(),
        client.sameName(),
        client.clientName(),
        client.orgBookName()
    );
  }

  public void complete() {
    generateCountData();
    initializeDataHeader();
    sheet.freezePane(8,18);
    close();
  }

  private void close() {
    try {
      sheet.finish();
      workbook.finish();
      fileOut.close();
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

    sheet
        .range(1, 1, 1, 4)
        .style()
        .bold()
        .borderColor(BLACK_COLOR)
        .borderStyle("thin")
        .set();


    sheet
        .range(18, 1, rowCounter.get() - 1, 7)
        .style()
        .shadeAlternateRows(Color.GRAY2)
        .borderColor(BLACK_COLOR)
        .borderStyle("thin")
        .set();

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
    setMergedValue(1, 1, "Total Clients");
    setCellValue(1, 4, String.valueOf(rowCounter.get() - 18));
    right(1, 4);
    setBold(1, 1);
    setBold(1, 4);
    setBorder(1, 1);
    setBorder(1, 4);
  }

  private void headerRow(int rowNumber) {
    setCellValue(rowNumber, 4, "# of Records");
    setBold(rowNumber, 4);
    center(rowNumber, 4);
    setBorder(rowNumber, 4);

    setCellValue(rowNumber, 5, "Percentage");
    setBold(rowNumber, 5);
    center(rowNumber, 5);
    setBorder(rowNumber, 5);
  }

  private void valueRow(int rowNumber, String... contents) {
    for (int columnId = 1; columnId <= contents.length; columnId++) {
      this.sheet.value(rowNumber, columnId, contents[columnId - 1]);
      setBorder(rowNumber, columnId);
      left(rowNumber, columnId);
    }
  }

  private void infoRow(
      int rowNumber,
      String contentTitle,
      String contentRecords,
      String contentPercentage
  ) {
    setMergedValue(rowNumber, 1, contentTitle);

    setCellFormula(rowNumber, 4, contentRecords);

    setCellFormula(rowNumber, 5, contentPercentage);

    sheet.style(rowNumber, 5).format("0.00%").set();

  }

  private void setCellValue(int rowId, int cellId, String cellValue) {
    setCell(rowId, cellId, cellValue, false);
  }

  private void setCellFormula(int rowId, int cellId, String cellValue) {
    setCell(rowId, cellId, cellValue, true);
    right(rowId, cellId);
    setBorder(rowId, cellId);
  }

  private void setCell(
      int rowId,
      int cellId,
      String cellValue,
      boolean formula
  ) {
    if (formula) {
      sheet.formula(rowId, cellId, cellValue);
    } else {
      sheet.value(rowId, cellId, cellValue);
    }
  }

  private void setMergedValue(int rowId, int cellId, String cellValue) {
    setCellValue(rowId, cellId, cellValue);
    sheet.range(rowId, cellId, rowId, cellId + 2).merge();
    left(rowId, 1);
    setBold(rowId, 1);
    setBorder(rowId, 1, 3);
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

  private void setBorder(int rowId, int columnId) {
    sheet
        .style(rowId, columnId)
        .borderColor(BLACK_COLOR)
        .borderStyle("thin")
        .set();
  }

  private void setBorder(int rowId, int columnId, int lastColumn) {
    if (lastColumn == 0) {
      setBorder(rowId, columnId);
    } else {
      sheet
          .range(rowId, columnId, rowId, lastColumn)
          .style()
          .borderColor(BLACK_COLOR)
          .borderStyle("thin")
          .set();
    }
  }

  private void setBold(int rowId, int columnId) {
    sheet
        .style(rowId, columnId)
        .bold()
        .set();
  }

  private void center(int rowId, int columnId) {
    sheet
        .style(rowId, columnId)
        .horizontalAlignment("center")
        .set();
  }

  private void left(int rowId, int columnId) {
    sheet
        .style(rowId, columnId)
        .horizontalAlignment("left")
        .set();
  }

  private void right(int rowId, int columnId) {
    sheet
        .style(rowId, columnId)
        .horizontalAlignment("right")
        .set();
  }

}
