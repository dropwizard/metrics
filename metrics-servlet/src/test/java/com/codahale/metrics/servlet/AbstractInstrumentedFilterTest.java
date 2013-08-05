package com.codahale.metrics.servlet;

import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class AbstractInstrumentedFilterTest {
    public static final String METRICS_REGISTRY_ATTR_NAME = "metricsRegistry";
    public static final String METRIC_2XX = "com.codahale.metrics.servlet.AbstractInstrumentedFilter.2xx";
    private static final String METRIC_204 = "com.codahale.metrics.servlet.AbstractInstrumentedFilter.noContent";
    private static final String METRIC_OTHER = "com.codahale.metrics.servlet.AbstractInstrumentedFilter.otherMetric";
    
    private final FilterConfig filterConfigMock = mock(FilterConfig.class);
    private final HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);
    private final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
    private final ServletContext servletContextMock = mock(ServletContext.class);
    private final FilterChainStub filterChainStub = new FilterChainStub();
    
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private AbstractInstrumentedFilter filterUnderTest 
        = new AbstractInstrumentedFilter(METRICS_REGISTRY_ATTR_NAME, createMeterNamesByStatusCode(), "otherMetric") {
    };


    @Before
    public void initMockServletEnvironment() throws IOException, ServletException {
        stub(filterConfigMock.getServletContext()).toReturn(servletContextMock);
        stub(servletContextMock.getAttribute(METRICS_REGISTRY_ATTR_NAME)).toReturn(metricRegistry);

        filterUnderTest.init(filterConfigMock);
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(1);
        meterNamesByStatusCode.put(204, "noContent");
        return meterNamesByStatusCode;
    }

    @Test
    public void marksGroupMetricIfNoSingleStatusCodeMetricExists() throws ServletException, IOException {
        filterChainStub.allwaysReturnStatus(205);
        filterUnderTest.doFilter(servletRequestMock, httpServletResponseMock, filterChainStub);      
        assertThat(metricRegistry.meter(METRIC_2XX).getCount(), is(1L));
    }

    @Test
    public void marksGroupAndSingleValueMetricIfBothExistForTheGivenStatusCode() throws IOException, ServletException {
        filterChainStub.allwaysReturnStatus(204);
        filterUnderTest.doFilter(servletRequestMock, httpServletResponseMock, filterChainStub);
        assertThat(metricRegistry.meter(METRIC_204).getCount(), is(1L));
        assertThat(metricRegistry.meter(METRIC_2XX).getCount(), is(1L));
    }

    @Test
    public void marksOtherMeterIfNeiterGroupNorSingleStatusCodeMetricsExist() throws IOException, ServletException {
        filterChainStub.allwaysReturnStatus(603);
        filterUnderTest.doFilter(servletRequestMock, httpServletResponseMock, filterChainStub);
        assertThat(metricRegistry.meter(METRIC_OTHER).getCount(), is(1L));
    }

    private static class FilterChainStub implements FilterChain {

        private int status;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            ((HttpServletResponse) response).setStatus(status);
        }

        public void allwaysReturnStatus(int status) {
            this.status = status;
        }
    }
}
