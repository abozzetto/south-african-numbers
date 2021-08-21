package it.interlogica.san.kernel.driver;

import java.io.IOException;
import java.io.Writer;

public interface ISanDriver {

  void init(Writer writer) throws IOException;

  void writeRecord(String id, String number, String status, String suggested) throws IOException;

  void close() throws IOException;

}
