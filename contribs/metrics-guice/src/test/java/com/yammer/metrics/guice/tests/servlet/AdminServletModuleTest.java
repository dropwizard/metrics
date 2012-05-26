package com.yammer.metrics.guice.tests.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.guice.servlet.AdminServletModule;
import com.yammer.metrics.reporting.AdminServlet;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;

public class AdminServletModuleTest {

  private Server server;
  private int localPort;

  @Before
  public void setUp() throws Exception {
    server = buildTestServer();
    server.start();
    localPort = server.getConnectors()[0].getLocalPort();
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void shouldProvideValidJsonForMetrics() throws Exception {
    final String url = "http://127.0.0.1:" + localPort + AdminServletModule.DEFAULT_PATH + AdminServlet.DEFAULT_METRICS_URI;

    final AsyncHttpClient client = new AsyncHttpClient();
    final Response response = client.prepareGet(url).execute().get();

    assertEquals("Failed to get metrics from " + url, 200, response.getStatusCode());

    final String body = response.getResponseBody();
    try {
      new Gson().fromJson(body, HashMap.class);
    }
    catch (JsonSyntaxException e) {
      fail("Failed to parse json " + body);
    }
  }

  private static Server buildTestServer() {
    final Server server = new Server(0);

    final ServletContextHandler servletHandler = new ServletContextHandler(server, "/");
    servletHandler.addEventListener(new TestGuiceServletConfig());
    servletHandler.addFilter(GuiceFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    servletHandler.addServlet(DefaultServlet.class, "/");
    server.setHandler(servletHandler);

    return server;
  }

  // needed because AdminServletProvider has mandatory dep on set of health checks
  private static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      Multibinder.newSetBinder(binder(), HealthCheck.class);
    }
  }

  private static class TestGuiceServletConfig extends GuiceServletContextListener {
    @Override
    protected Injector getInjector() {
      return Guice.createInjector(
        new TestModule(),
        new AdminServletModule()
      );
    }
  }
}
