package it.interlogica.san.kernel;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class SanResult {

  private String number;
  private SanNumberStatus status;
  private String suggested;

  public enum SanNumberStatus {
    OK("OK"), CORRECTED("CORRECTED"), WRONG("WRONG");

    private String status;

    SanNumberStatus(String status) {
      this.status = status;
    }

    @Override
    public String toString() {
      return status;
    }
  }

  public SanResult(String number, SanNumberStatus status, String suggested) {
    super();
    this.number = number;
    this.status = status;
    this.suggested = suggested;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public SanNumberStatus getStatus() {
    return status;
  }

  public void setStatus(SanNumberStatus status) {
    this.status = status;
  }

  public String getSuggested() {
    return suggested;
  }

  public void setSuggested(String suggested) {
    this.suggested = suggested;
  }

  @Override
  public String toString() {
    return "Number: " + number + " - Status: " + status.toString()
        + (StringUtils.isNotEmpty(suggested) ? (" Suggested: " + suggested) : "");
  }


  public String toJsonString() {
    JSONObject json = new JSONObject();
    json.put("status", status.toString());
    json.put("number", number);
    if (StringUtils.isNotEmpty(suggested)) {
      json.put("suggested", suggested);
    }
    return json.toString();

  }


}
