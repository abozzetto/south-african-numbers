package it.interlogica.san.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.LoggerFactory;
import it.interlogica.san.kernel.SanResult.SanNumberStatus;
import it.interlogica.san.kernel.driver.ISanDriver;

public class SanEngine {

  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SanEngine.class.getName());
  private static final String AFRICAN_PREFIX = "27";
  private Pattern africanPattern = Pattern.compile("^\\d{11}$");



  private boolean internalCheckNumber(String number) {
    Matcher m = africanPattern.matcher(number);
    return m.matches() && number.startsWith(AFRICAN_PREFIX);
  }

  /**
   * check an African number
   * 
   * @param number
   * @return
   */
  public SanResult checkNumber(String number) {
    /*
     * length 11 digits
     * start with 27
     * if length == 9 add 27 to the left and suggest a new number
     */

    if (StringUtils.isEmpty(number))
      return new SanResult(number, SanNumberStatus.WRONG, null);
    number = number.trim();
    int numLength = number.length();

    if (numLength == 9) {
      String newNumber = "27" + number;
      if (internalCheckNumber(newNumber))
        return new SanResult(number, SanNumberStatus.CORRECTED, "27" + number);
      else
        return new SanResult(number, SanNumberStatus.WRONG, null);
    }
    if (numLength == 11) {
      if (internalCheckNumber(number))
        return new SanResult(number, SanNumberStatus.OK, null);
      else
        return new SanResult(number, SanNumberStatus.WRONG, null);
    }
    return new SanResult(number, SanNumberStatus.WRONG, null);
  }

  private String getCellValue(Row row, int index) {
    Cell cell = row.getCell(index);
    String result = null;
    if (cell != null) {
      CellType cellType = cell.getCellTypeEnum();

      switch (cellType) {

        case NUMERIC:
          double val = cell.getNumericCellValue();
          long l = new Double(val).longValue();
          result = new Long(l).toString();
          break;
        default:
          result = cell.getStringCellValue();
          break;
      }
    }
    return result;
  }

  /**
   * Read numbers from an xls file id|number and return the result in a csv or html file format with
   * the column id|number|status|suggested
   * 
   * @param stream
   * @param writer
   * @param driver
   * @throws Exception
   */

  public void checkNumbers(InputStream stream, Writer writer, ISanDriver driver) throws Exception {

    Workbook wb;
    try {
      wb = WorkbookFactory.create(stream);
    } catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
      log.error(e.getMessage(), e);
      throw new Exception("Invalid stream format: we accept csv or xls");
    }
    try {
      Sheet sheet = wb.getSheetAt(0);

      Row row = sheet.getRow(0);

      /*
       *
       * |id|number
       */

      int minRow = -1; // Skip first row
      boolean end = false;
      boolean empty = false;
      String id = null;
      String number = null;
      for (int r = 0; !empty && minRow == -1; r++) {
        row = sheet.getRow(r);
        if (row == null) {
          empty = true;
        }
        id = (String) getCellValue(row, 1);
        if (StringUtils.isNotEmpty(id) && id.chars().allMatch(Character::isDigit))
          minRow = r;
      }

      if (empty)
        throw new Exception("Empty stream");
      log.debug("starting from row number " + minRow);

      driver.init(writer);
      SanResult result;
      try {
        for (int r = minRow; end == false; r++) {
          row = sheet.getRow(r);
          if (row == null) {
            end = true;
          } else {
            id = (String) getCellValue(row, 0);
            number = (String) getCellValue(row, 1);
            result = checkNumber(number);
            driver.writeRecord(id, number, result.getStatus().toString(),
                (StringUtils.isNotEmpty(result.getSuggested()) ? result.getSuggested() : ""));

          }
        }

      } finally {
        driver.close();
      }
    } finally {
      wb.close();
    }

  }

}
