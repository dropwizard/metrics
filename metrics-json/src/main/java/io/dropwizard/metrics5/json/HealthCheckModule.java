package io.dropwizard.metrics5.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.dropwizard.metrics5.health.HealthCheck;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class HealthCheckModule extends Module {
    private static class HealthCheckResultSerializer extends StdSerializer<HealthCheck.Result> {

        private static final long serialVersionUID = 1L;

        private HealthCheckResultSerializer() {
            super(HealthCheck.Result.class);
        }

        @Override
        public void serialize(HealthCheck.Result result,
                              JsonGenerator json,
                              SerializerProvider provider) throws IOException {
            json.writeStartObject();
            json.writeBooleanField("healthy", result.isHealthy());

            final String message = result.getMessage();
            if (message != null) {
                json.writeStringField("message", message);
            }

            serializeThrowable(json, result.getError(), "error");

            Map<String, Object> details = result.getDetails();
            if (details != null && !details.isEmpty()) {
                for (Map.Entry<String, Object> e : details.entrySet()) {
                    json.writeObjectField(e.getKey(), e.getValue());
                }
            }

            json.writeEndObject();
        }

        private void serializeThrowable(JsonGenerator json, Throwable error, String name) throws IOException {
            if (error != null) {
                json.writeObjectFieldStart(name);
                json.writeStringField("message", error.getMessage());
                json.writeArrayFieldStart("stack");
                for (StackTraceElement element : error.getStackTrace()) {
                    json.writeString(element.toString());
                }
                json.writeEndArray();

                if (error.getCause() != null) {
                    serializeThrowable(json, error.getCause(), "cause");
                }

                json.writeEndObject();
            }
        }
    }

    @Override
    public String getModuleName() {
        return "healthchecks";
    }

    @Override
    public Version version() {
        return MetricsModule.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new SimpleSerializers(Collections.singletonList(new HealthCheckResultSerializer())));
    }
}
