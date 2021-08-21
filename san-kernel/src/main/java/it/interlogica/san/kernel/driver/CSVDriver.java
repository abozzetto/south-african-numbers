package it.interlogica.san.kernel.driver;

import java.io.IOException;
import java.io.Writer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVDriver implements ISanDriver {

  private CSVPrinter printer;

  @Override
  public void init(Writer writer) throws IOException {
    printer = CSVFormat.DEFAULT.print(writer);
    printer.printRecord("id", "number", "status", "corrected");

  }

  @Override
  public void writeRecord(String id, String number, String status, String suggested)
      throws IOException {
    printer.printRecord(id, number, status, suggested);

  }

  @Override
  public void close() throws IOException {
    printer.flush();
    printer.close();
  }

}
