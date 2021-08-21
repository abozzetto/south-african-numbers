package it.interlogica.san.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import it.interlogica.san.kernel.SanEngine;
import it.interlogica.san.kernel.driver.CSVDriver;

public class SanConsole {



  public static void main(String[] args) {
    CommandLine cmd = null;
    Options options = new Options();
    Option fileOption =
        OptionBuilder.withArgName("file").hasArg().withDescription("file to check").create("file");
    Option outputOption = OptionBuilder.withArgName("output").hasArg()
        .withDescription("file to save").create("output");
    Option numberOption = OptionBuilder.withArgName("number").hasArg()
        .withDescription("number to check").create("number");
    options.addOption(fileOption);
    options.addOption(outputOption);
    options.addOption(fileOption);
    options.addOption(numberOption);

    CommandLineParser parser = new GnuParser();
    try {
      cmd = parser.parse(options, args);
      if (!cmd.hasOption("file") && !cmd.hasOption("number")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar san-cli.jar", options);
        System.exit(1);
      }


      if (!cmd.hasOption("file") && cmd.hasOption("output")) {
        throw new ParseException("missing file parameter");
      }
      if (cmd.hasOption("file") && cmd.hasOption("number")) {
        throw new ParseException("invalid \"number\" parameter with file");
      }
    } catch (ParseException e) {
      e.printStackTrace();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar san-cli.jar", options);
      System.exit(1);
    }
    String fileName = cmd.getOptionValue("file");
    String outFileName = cmd.getOptionValue("output");
    String numberCheck = cmd.getOptionValue("number");


    long startTime = System.nanoTime();

    try {

      if (StringUtils.isNotEmpty(numberCheck)) {
        SanEngine engine = new SanEngine();
        System.out.println(engine.checkNumber(numberCheck));
        System.exit(0);
      }

      InputStream input = null;
      input = getInputStream(fileName);
      Writer out = new PrintWriter(System.out);

      if (StringUtils.isNotEmpty(outFileName)) {
        File f = new File(outFileName);
        out = new FileWriter(f);
      }

      SanEngine engine = new SanEngine();
      engine.checkNumbers(input, out, new CSVDriver());


    } catch (Throwable e) {
      e.printStackTrace();
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar san-cli.jar", options);
      System.exit(1);
    }


    long endTime = System.nanoTime();
    long duration = endTime - startTime;

    System.out.println("Check completed in in (ms) : "
        + TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS));


  }

  protected static InputStream getInputStream(String fileName) throws FileNotFoundException {
    InputStream input = null;
    if (StringUtils.isNotEmpty(fileName)) {
      File f = new File(fileName);
      if (!f.exists()) {
        System.out.println("invalid filename " + fileName);
        System.exit(1);
      }
      input = new FileInputStream(f);
    }
    return input;
  }


}
