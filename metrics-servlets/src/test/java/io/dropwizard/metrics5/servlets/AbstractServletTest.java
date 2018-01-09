package io.dropwizard.metrics5.servlets;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractServletTest {
    private final ServletTester tester = new ServletTester();
    protected final HttpTester.Request request = HttpTester.newRequest();
    protected HttpTester.Response response;

    @Before
    public void setUpTester() throws Exception {
        setUp(tester);
        tester.start();
    }

    protected abstract void setUp(ServletTester tester);

    @After
    public void tearDownTester() throws Exception {
        tester.stop();
    }

    protected void processRequest() throws Exception {
        this.response = HttpTester.parseResponse(tester.getResponses(request.generate()));
    }
}
