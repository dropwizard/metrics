package io.dropwizard.metrics.jetty12.ee10;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseStatusTest extends AbstractIntegrationTest {

    @Override
    protected Handler getHandler() {
        ServletHandler servletHandler = new ResponseStatusHandler();
        servletHandler.addServletWithMapping(DefaultServlet.class, "/");
        return servletHandler;
    }

    @Test
    public void testResponseCodes() throws Exception {

        for (int i = 2; i <= 5; i++) {
            String status = String.format("%d00", i);
            ContentResponse contentResponse = client.POST(uri("/"))
                    .body(new StringRequestContent(status))
                    .headers(headers -> headers.add("Content-Type", "text/plain"))
                    .send();
            assertThat(contentResponse).isNotNull().satisfies(response ->
                    assertThat(response.getStatus()).hasToString(status));

            Meter meter = registry.meter(MetricRegistry.name(ResponseStatusHandler.class, String.format("handler.%dxx-responses", i)));
            assertThat(meter.getCount()).isEqualTo(1L);
        }
    }

    private static class ResponseStatusHandler extends ServletHandler {
        @Override
        public boolean handle(Request request, Response response, Callback callback) throws Exception {
            try (InputStream inputStream = Request.asInputStream(request);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                String status = bufferedReader.readLine();
                int statusCode = Integer.parseInt(status);
                response.setStatus(statusCode);
                callback.succeeded();
                return true;
            }
        }
    }
}
