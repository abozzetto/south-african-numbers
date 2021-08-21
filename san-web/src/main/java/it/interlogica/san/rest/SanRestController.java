package it.interlogica.san.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import it.interlogica.san.kernel.SanEngine;
import it.interlogica.san.kernel.SanResult;
import it.interlogica.san.kernel.driver.CSVDriver;
import it.interlogica.san.kernel.driver.HtmlDriver;

@Controller
@RequestMapping(value = "/")
public class SanRestController {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(SanRestController.class);


  @RequestMapping(method = RequestMethod.GET, value = "checkNumber")
  public @ResponseBody ResponseEntity<SanResult> checkNumber(@RequestParam("number") String number)
      throws InterruptedException {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setContentType(MediaType.valueOf("application/json"));

    SanEngine engine = new SanEngine();
    SanResult result = engine.checkNumber(number);

    return new ResponseEntity<SanResult>(result, responseHeaders, HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(method = RequestMethod.POST, value = "/checkNumbers",
      consumes = {"multipart/form-data"})
  public void checkNumbers(
      @RequestParam(required = false, value = "format", defaultValue = "text/html") String format,
      MultipartHttpServletRequest multipartHttpServletRequest, HttpServletResponse response) {
    HttpHeaders responseHeaders = new HttpHeaders();

    InputStream stream = null;
    if (multipartHttpServletRequest.getFile("numbers") == null) {
      responseError(response, "{\"response\": \"KO\", \"errorMessage\": \"missing numbers file\"}",
          HttpStatus.BAD_REQUEST.value());
      return;
    }
    try {

      stream = multipartHttpServletRequest.getFile("numbers").getInputStream();

      SanEngine engine = new SanEngine();
      switch (format) {
        case "text/csv":
          getCSVResult(format, responseHeaders, stream, engine, response);
          return;


        case "text/html":
          engine.checkNumbers(stream, response.getWriter(), new HtmlDriver());
          return;

        default:
          throw new Exception("invalid format " + format);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      responseError(response, getJsonError(e), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  protected void responseError(HttpServletResponse response, String message, int status) {
    response.setStatus(status);

    try {
      PrintWriter out = response.getWriter();
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      out.print(message);
      out.flush();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

  }

  protected void getCSVResult(String format, HttpHeaders responseHeaders, InputStream stream,
      SanEngine engine, HttpServletResponse response)
      throws IOException, Exception, MalformedURLException {
    engine.checkNumbers(stream, response.getWriter(), new CSVDriver());
    response.setContentType("text/csv");
    response.setCharacterEncoding("UTF-8");
  }



  private String getJsonError(Throwable e) {
    JSONObject jsonError = new JSONObject();
    jsonError.put("response", "KO");
    jsonError.put("errorMessage", e.getMessage());
    return jsonError.toString();
  }
}

