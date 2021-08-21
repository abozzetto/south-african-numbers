package it.interlogica.san.test;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class JettyUtil {
  public static int getPortForStartedServer(Server server) {
    assert server.isStarted();

    Connector[] connectors = server.getConnectors();

    assert connectors.length == 1;

    return ((ServerConnector) ((ServerConnector) connectors[0])).getLocalPort();
  }

  public static void startServer(Server server) throws Exception {
    // server.insertHandler(new StatisticsHandler());

    server.start();

  }

  public static void closeServer(Server server) throws Exception {
    if (server != null) {
      server.stop();
      server.destroy();
    }
  }
}
