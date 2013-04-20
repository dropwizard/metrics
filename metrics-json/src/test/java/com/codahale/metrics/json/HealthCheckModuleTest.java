package com.codahale.metrics.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.codahale.metrics.health.HealthCheck;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HealthCheckModuleTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new HealthCheckModule());

    @Test
    public void serializesAHealthyResult() throws Exception {
        assertThat(mapper.writeValueAsString(HealthCheck.Result.healthy()))
                .isEqualTo("{\"healthy\":true}");
    }

    @Test
    public void serializesAHealthyResultWithAMessage() throws Exception {
        assertThat(mapper.writeValueAsString(HealthCheck.Result.healthy("yay for %s", "me")))
                .isEqualTo("{" +
                                   "\"healthy\":true," +
                                   "\"message\":\"yay for me\"}");
    }

    @Test
    public void serializesAnUnhealthyResult() throws Exception {
        assertThat(mapper.writeValueAsString(HealthCheck.Result.unhealthy("boo")))
                .isEqualTo("{" +
                                   "\"healthy\":false," +
                                   "\"message\":\"boo\"}");
    }

    @Test
    public void serializesAnUnhealthyResultWithAnException() throws Exception {
        final Throwable e = mock(Throwable.class);
        when(e.getMessage()).thenReturn("oh no");
        when(e.getStackTrace()).thenReturn(new StackTraceElement[]{
                new StackTraceElement("Blah", "bloo", "Blah.java", 100)
        });

        assertThat(mapper.writeValueAsString(HealthCheck.Result.unhealthy(e)))
                .isEqualTo("{" +
                                   "\"healthy\":false," +
                                   "\"message\":\"oh no\"," +
                                   "\"error\":{" +
                                       "\"message\":\"oh no\"," +
                                       "\"stack\":[\"Blah.bloo(Blah.java:100)\"]" +
                                   "}" +
                                   "}");
    }

    @Test
    public void serializesAnUnhealthyResultWithNestedExceptions() throws Exception {
        final Throwable a = mock(Throwable.class);
        when(a.getMessage()).thenReturn("oh no");
        when(a.getStackTrace()).thenReturn(new StackTraceElement[]{
                new StackTraceElement("Blah", "bloo", "Blah.java", 100)
        });

        final Throwable b = mock(Throwable.class);
        when(b.getMessage()).thenReturn("oh well");
        when(b.getStackTrace()).thenReturn(new StackTraceElement[]{
                new StackTraceElement("Blah", "blee", "Blah.java", 150)
        });
        when(b.getCause()).thenReturn(a);

        assertThat(mapper.writeValueAsString(HealthCheck.Result.unhealthy(b)))
                .isEqualTo("{" +
                                   "\"healthy\":false," +
                                   "\"message\":\"oh well\"," +
                                   "\"error\":{" +
                                       "\"message\":\"oh well\"," +
                                       "\"stack\":[\"Blah.blee(Blah.java:150)\"]," +
                                       "\"cause\":{" +
                                           "\"message\":\"oh no\"," +
                                           "\"stack\":[\"Blah.bloo(Blah.java:100)\"]" +
                                       "}" +
                                   "}" +
                                   "}");
    }
}
