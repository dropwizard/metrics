package com.codahale.metrics.json;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckModuleTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new HealthCheckModule());

    @Test
    public void serializesAHealthyResult() throws Exception {
        HealthCheck.Result result = HealthCheck.Result.healthy();
        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{\"healthy\":true,\"duration\":0,\"timestamp\":\"" + result.getTimestamp() + "\"}");
    }

    @Test
    public void serializesAHealthyResultWithAMessage() throws Exception {
        HealthCheck.Result result = HealthCheck.Result.healthy("yay for %s", "me");
        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{" +
                "\"healthy\":true," +
                "\"message\":\"yay for me\"," +
                "\"duration\":0," +
                "\"timestamp\":\"" + result.getTimestamp() + "\"" +
                "}");
    }

    @Test
    public void serializesAnUnhealthyResult() throws Exception {
        HealthCheck.Result result = HealthCheck.Result.unhealthy("boo");
        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{" +
                "\"healthy\":false," +
                "\"message\":\"boo\"," +
                "\"duration\":0," +
                "\"timestamp\":\"" + result.getTimestamp() + "\"" +
                "}");
    }

    @Test
    public void serializesAnUnhealthyResultWithAnException() throws Exception {
        final RuntimeException e = new RuntimeException("oh no");
        e.setStackTrace(new StackTraceElement[]{
            new StackTraceElement("Blah", "bloo", "Blah.java", 100)
        });

        HealthCheck.Result result = HealthCheck.Result.unhealthy(e);
        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{" +
                "\"healthy\":false," +
                "\"message\":\"oh no\"," +
                "\"error\":{" +
                "\"type\":\"java.lang.RuntimeException\"," +
                "\"message\":\"oh no\"," +
                "\"stack\":[\"Blah.bloo(Blah.java:100)\"]" +
                "}," +
                "\"duration\":0," +
                "\"timestamp\":\"" + result.getTimestamp() + "\"" +
                "}");
    }

    @Test
    public void serializesAnUnhealthyResultWithNestedExceptions() throws Exception {
        final RuntimeException a = new RuntimeException("oh no");
        a.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("Blah", "bloo", "Blah.java", 100)
        });

        final RuntimeException b = new RuntimeException("oh well", a);
        b.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("Blah", "blee", "Blah.java", 150)
        });

        HealthCheck.Result result = HealthCheck.Result.unhealthy(b);
        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{" +
                "\"healthy\":false," +
                "\"message\":\"oh well\"," +
                "\"error\":{" +
                "\"type\":\"java.lang.RuntimeException\"," +
                "\"message\":\"oh well\"," +
                "\"stack\":[\"Blah.blee(Blah.java:150)\"]," +
                "\"cause\":{" +
                "\"type\":\"java.lang.RuntimeException\"," +
                "\"message\":\"oh no\"," +
                "\"stack\":[\"Blah.bloo(Blah.java:100)\"]" +
                "}" +
                "}," +
                "\"duration\":0," +
                "\"timestamp\":\"" + result.getTimestamp() + "\"" +
                "}");
    }

    @Test
    public void serializeResultWithDetail() throws Exception {
        Map<String, Object> complex = new LinkedHashMap<>();
        complex.put("field", "value");

        HealthCheck.Result result = HealthCheck.Result.builder()
            .healthy()
            .withDetail("boolean", true)
            .withDetail("integer", 1)
            .withDetail("long", 2L)
            .withDetail("float", 3.546F)
            .withDetail("double", 4.567D)
            .withDetail("BigInteger", new BigInteger("12345"))
            .withDetail("BigDecimal", new BigDecimal("12345.56789"))
            .withDetail("String", "string")
            .withDetail("complex", complex)
            .build();

        assertThat(mapper.writeValueAsString(result))
            .isEqualTo("{" +
                "\"healthy\":true," +
                "\"duration\":0," +
                "\"boolean\":true," +
                "\"integer\":1," +
                "\"long\":2," +
                "\"float\":3.546," +
                "\"double\":4.567," +
                "\"BigInteger\":12345," +
                "\"BigDecimal\":12345.56789," +
                "\"String\":\"string\"," +
                "\"complex\":{" +
                "\"field\":\"value\"" +
                "}," +
                "\"timestamp\":\"" + result.getTimestamp() + "\"" +
                "}");
    }
}
