package io.dropwizard.metrics.jetty12.ee10;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.CompletableResponseListener;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.Request;
import org.eclipse.jetty.client.Response;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.server.Handler;
import org.junit.Test;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

public class AsyncTest extends AbstractIntegrationTest {

    @Override
    protected Handler getHandler() {
        return new ServletHandler();
    }

    @Test
    public void testAsyncTimeout() throws Exception {
        servletContextHandler.addFilter((request, response, chain) -> {
            AsyncContext asyncContext = request.startAsync();
            asyncContext.setTimeout(1);
        }, "/*", EnumSet.allOf(DispatcherType.class));

        client.GET(uri("/"));
        Meter asyncTimeouts = registry.meter(MetricRegistry.name(ServletHandler.class, "handler.async-timeouts"));
        assertThat(asyncTimeouts.getCount()).isEqualTo(1L);

        client.GET(uri("/"));
        assertThat(asyncTimeouts.getCount()).isEqualTo(2L);
    }

    @Test
    public void testActiveSuspended() {
        servletContextHandler.addFilter((request, response, chain) -> {
            AsyncContext asyncContext = request.startAsync();
            asyncContext.start(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                asyncContext.complete();
            });
        }, "/*", EnumSet.allOf(DispatcherType.class));

        Counter activeSuspended = registry.counter(MetricRegistry.name(ServletHandler.class, "handler.active-suspended"));
        Request request = client.POST(uri("/"));
        CompletableResponseListener completableResponseListener = new CompletableResponseListener(request);
        CompletableFuture<ContentResponse> asyncResponse = completableResponseListener.send();
        assertThatNoException().isThrownBy(() -> {
            await()
                    .atMost(750, TimeUnit.MILLISECONDS)
                    .until(() -> activeSuspended.getCount() == 1L);
            asyncResponse.get();
        });
        assertThat(activeSuspended.getCount()).isEqualTo(0L);
    }

    @Test
    public void testAsyncDispatches() throws Exception {
        servletContextHandler.addFilter((request, response, chain) -> {
            if (!(request instanceof HttpServletRequest)) {
                throw new IllegalStateException("Expecting ServletRequest to be an instance of HttpServletRequest");
            }
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            if ("/".equals(httpServletRequest.getRequestURI())) {
                AsyncContext asyncContext = request.startAsync();
                asyncContext.dispatch("/dispatch");
                return;
            }
            if ("/dispatch".equals(httpServletRequest.getRequestURI())) {
                AsyncContext asyncContext = request.startAsync();
                if (!(response instanceof HttpServletResponse)) {
                    throw new IllegalStateException("Expecting ServletResponse to be an instance of HttpServletResponse");
                }
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(204);
                asyncContext.complete();
                return;
            }
            throw new UnsupportedOperationException("Only '/' and '/dispatch' are valid paths");
        }, "/*", EnumSet.allOf(DispatcherType.class));

        ContentResponse contentResponse = client.GET(uri("/"));
        assertThat(contentResponse).isNotNull().extracting(Response::getStatus).isEqualTo(204);
        Meter asyncDispatches = registry.meter(MetricRegistry.name(ServletHandler.class, "handler.async-dispatches"));
        assertThat(asyncDispatches.getCount()).isEqualTo(1L);
    }
}
