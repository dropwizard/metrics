package com.yammer.metrics.servlets.tests;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractServletTest {
    private final ServletTester tester = new ServletTester();
    protected final HttpTester request = new HttpTester();
    protected final HttpTester response = new HttpTester();

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
        response.parse(tester.getResponses(request.generate()));
    }
}
