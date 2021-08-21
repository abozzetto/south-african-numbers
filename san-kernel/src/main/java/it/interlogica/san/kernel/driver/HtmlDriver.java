package it.interlogica.san.kernel.driver;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import it.interlogica.san.kernel.SanResult;
import it.interlogica.san.kernel.SanResult.SanNumberStatus;

public class HtmlDriver implements ISanDriver {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(HtmlDriver.class.getName());
  private Writer writer;
  private List<SanResult> results;
  private static ResourceLoader resourceLoader = new PathMatchingResourcePatternResolver();


  @Override
  public void init(Writer writer) throws IOException {

    this.writer = writer;
    this.results = new ArrayList<SanResult>();
  }

  @Override
  public void writeRecord(String id, String number, String status, String suggested)
      throws IOException {
    results.add(new HtmlSanResult(id, number, SanNumberStatus.valueOf(status), suggested));

  }

  @Override
  public void close() throws IOException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
    cfg.setTemplateLoader(new SpringTemplateLoader(resourceLoader, "classpath:/ftl"));


    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    Template template = cfg.getTemplate("htmlTemplate.ftl");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("ROWS", results);
    try {
      template.process(map, writer);
    } catch (TemplateException | IOException e) {
      log.error(e.getMessage(), e);
      throw new IOException(e);
    }

  }

}
