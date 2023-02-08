package io.dropwizard.metrics5.servlets;

import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractServletTest {
    private final ServletTester tester = new ServletTester();
    protected final HttpTester.Request request = HttpTester.newRequest();
    protected HttpTester.Response response;

    @BeforeEach
    public void setUpTester() throws Exception {
        setUp(tester);
        tester.start();
    }

    protected abstract void setUp(ServletTester tester);

    @AfterEach
    public void tearDownTester() throws Exception {
        tester.stop();
    }

    protected void processRequest() throws Exception {
        this.response = HttpTester.parseResponse(tester.getResponses(request.generate()));
    }
}
