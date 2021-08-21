package it.interlogica.san.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import it.interlogica.san.kernel.SanEngine;
import it.interlogica.san.kernel.SanResult;
import it.interlogica.san.kernel.SanResult.SanNumberStatus;
import it.interlogica.san.kernel.driver.CSVDriver;

public class TestAfricanNumbers {

  @Test
  public void testWrongNumber() {
    SanEngine engine = new SanEngine();
    SanResult result = engine.checkNumber("12312");
    assertNotNull(result);
    assertEquals(result.getStatus(), SanNumberStatus.WRONG);
    result = engine.checkNumber("27831234ab7");
    assertNotNull(result);
    assertEquals(result.getStatus(), SanNumberStatus.WRONG);
    result = engine.checkNumber("84528784843");
    assertNotNull(result);
    assertEquals(result.getStatus(), SanNumberStatus.WRONG);

  }

  @Test
  public void testOKNumber() {
    SanEngine engine = new SanEngine();
    SanResult result = engine.checkNumber("27831234567");
    assertNotNull(result);
    assertEquals(result.getStatus(), SanNumberStatus.OK);
  }

  @Test
  public void testCorrectedNumber() {
    SanEngine engine = new SanEngine();
    SanResult result = engine.checkNumber("831234567");
    assertNotNull(result);
    assertEquals(result.getStatus(), SanNumberStatus.CORRECTED);
    assertEquals(result.getSuggested(), "27831234567");
  }

  @Test
  public void testFile() throws Exception {
    SanEngine engine = new SanEngine();
    FileInputStream fileStream =
        new FileInputStream(new File("./src/test/resources/South_African_Mobile_Numbers.xlsx"));
    try {
      File tempFile = File.createTempFile("test", ".csv");
      try {
        FileWriter writer = new FileWriter(tempFile);
        engine.checkNumbers(fileStream, writer, new CSVDriver());
        writer.close();
        CSVParser csvParser;
        FileInputStream outStream = new FileInputStream(tempFile);
        try {
          csvParser = CSVFormat.DEFAULT.parse(new InputStreamReader(outStream));
          boolean firstRow = true;
          int rowNumber = 0;
          String status, suggested;
          for (CSVRecord record : csvParser) {
            if (!firstRow) {
              assertTrue("wrong value type on row " + rowNumber + " cell 0",
                  record.get(0).chars().allMatch(Character::isDigit));
              status = record.get(2);
              assertTrue("wrong status " + status,
                  status.equals("OK") || status.equals("Wrong") || status.equals("Corrected"));
              if (status.equals("Corrected")) {
                suggested = record.get(3);
                assertTrue("wrong suggested " + suggested,
                    suggested.chars().allMatch(Character::isDigit));
              }
            }
            rowNumber = rowNumber + 1;
          }
          assertEquals("record count wrong", 1001, csvParser.getRecordNumber());
        } finally {
          outStream.close();
        }

      } finally {
        //tempFile.delete();
      }

    } finally {
      fileStream.close();
    }


  }



}
