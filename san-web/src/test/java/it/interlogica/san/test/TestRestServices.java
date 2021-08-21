package it.interlogica.san.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import it.interlogica.san.kernel.SanResult.SanNumberStatus;

public class TestRestServices {

  protected static Server jettyServer;
  protected static Integer ourPort = null;
  private static String servletPath;

  @BeforeClass
  public static void beforeClass() throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    System.setProperty("log4jOut", "stdoutTest");

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");

    context.addEventListener(new org.springframework.web.context.ContextLoaderListener());
    context.setInitParameter("contextConfigLocation",
        "file:./src/main/webapp/WEB-INF/san-applicationContext.xml");

    ServletHolder jerseyServlet =
        context.addServlet(org.springframework.web.servlet.DispatcherServlet.class, "/*");
    jerseyServlet.setInitParameter("contextConfigLocation",
        "file:./src/main/webapp/WEB-INF/san-applicationContext.xml");
    jettyServer = new Server(0);
    jettyServer.setHandler(context);
    startJettyServer();


    Thread.sleep(500);
    servletPath = "http://localhost:" + ourPort + "/";



  }

  @AfterClass
  public static void afterClass() throws Exception {
    JettyUtil.closeServer(jettyServer);
  }

  protected static void startJettyServer() throws IOException, Exception, InterruptedException {
    JettyUtil.startServer(jettyServer);
    ourPort = JettyUtil.getPortForStartedServer(jettyServer);
    Thread.sleep(500);
    System.out.println("Server started");
  }

  protected String sendRequest(String servicePath, int expectedStatus) {
    HttpClient httpClient = HttpClientBuilder.create().build();
    String path = servletPath + servicePath;
    System.out.println(path);
    HttpGet get = new HttpGet(path);
    get.setHeader("Content-Type", "application/json");
    HttpResponse response;
    String out = "";
    try {
      response = httpClient.execute(get);
      BufferedReader rd =
          new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line = "";

      while ((line = rd.readLine()) != null) {
        out += line;
      }
      if (StringUtils.isEmpty(out))
        return null;
      assertEquals(path + " return error. \n" + out, expectedStatus,
          response.getStatusLine().getStatusCode());


    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());

    }
    return out;

  }


  private String sendPost(String servicePath, int expectedStatus, String fileName) {
    HttpClient httpClient = HttpClientBuilder.create().build();
    String out = "";
    HttpPost post;
    post = new HttpPost(servletPath + servicePath);


    File file = new File(fileName);

    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
    String boundary = "---------------" + UUID.randomUUID().toString();
    builder.setBoundary(boundary);

    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    // builder.addPart("codeSystem", cbFile);
    try {
      builder.addBinaryBody("numbers", FileUtils.readFileToByteArray(file),
          ContentType.APPLICATION_OCTET_STREAM, file.getName());


      HttpEntity entity = builder.build();
      post.setHeader("Content-Type",
          ContentType.MULTIPART_FORM_DATA.getMimeType() + ";boundary=" + boundary);

      post.setEntity(entity);
      HttpResponse response = httpClient.execute(post);
      BufferedReader rd =
          new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line = "";

      while ((line = rd.readLine()) != null) {
        out += line + "\n";
      }
      assertEquals("Status errato: " + response.getStatusLine().getStatusCode() + " - " + out,
          expectedStatus, response.getStatusLine().getStatusCode());
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());

    }
    return out;

  }



  @Test
  public void testNewWrongNumber() {
    String result = sendRequest("checkNumber?number=27831234ab7", 200);
    JSONObject jsonResult = new JSONObject(result);
    assertEquals("status wrong", "WRONG", jsonResult.getString("status"));

    result = sendRequest("checkNumber?number=84528784843", 200);
    jsonResult = new JSONObject(result);
    assertEquals("status wrong", "WRONG", jsonResult.getString("status"));

    result = sendRequest("checkNumber?number=12312", 200);
    jsonResult = new JSONObject(result);
    assertEquals("status wrong", "WRONG", jsonResult.getString("status"));

  }


  @Test
  public void testOKNumber() {
    String result = sendRequest("checkNumber?number=27831234567", 200);
    JSONObject jsonResult = new JSONObject(result);
    assertEquals("status wrong", "OK", jsonResult.getString("status"));
  }

  @Test
  public void testCorrectedNumber() {
    String result = sendRequest("checkNumber?number=831234567", 200);
    JSONObject jsonResult = new JSONObject(result);
    assertEquals("status wrong", SanNumberStatus.CORRECTED.toString(),
        jsonResult.getString("status"));
    assertEquals("suggested wrong", "27831234567", jsonResult.getString("suggested"));

  }

  @Test
  public void testFileToCsv() throws IOException {
    String csv = sendPost("checkNumbers?format=text/csv", 200,
        "./src/test/resources/South_African_Mobile_Numbers.xlsx");
    System.out.println(csv);

    CSVParser csvParser;
    InputStream outStream =
        new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8.toString()));
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


  }

  @Test
  public void testFileToHtml() throws IOException {
    String html = sendPost("checkNumbers?format=text/html", 200,
        "./src/test/resources/South_African_Mobile_Numbers.xlsx");
    System.out.println(html);

    FileUtils.writeStringToFile(File.createTempFile("xxxx", ".html"), html);
  }



}
