package it.interlogica.san.kernel.driver;

import it.interlogica.san.kernel.SanResult;

public class HtmlSanResult extends SanResult {
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public HtmlSanResult(String id, String number, SanNumberStatus status, String suggested) {
    super(number, status, suggested);
    this.id = id;
  }

}
