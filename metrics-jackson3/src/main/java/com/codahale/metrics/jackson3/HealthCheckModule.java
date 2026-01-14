package com.codahale.metrics.jackson3;

import com.codahale.metrics.health.HealthCheck;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleSerializers;
import tools.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class HealthCheckModule extends JacksonModule {
    private static class HealthCheckResultSerializer extends StdSerializer<HealthCheck.Result> {

        private static final long serialVersionUID = 1L;

        private HealthCheckResultSerializer() {
            super(HealthCheck.Result.class);
        }

        @Override
        public void serialize(HealthCheck.Result result,
                              JsonGenerator json,
                              SerializationContext provider) {
            json.writeStartObject();
            json.writeBooleanProperty("healthy", result.isHealthy());

            final String message = result.getMessage();
            if (message != null) {
                json.writeStringProperty("message", message);
            }

            try {
                serializeThrowable(json, result.getError(), "error");
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
            json.writeNumberProperty("duration", result.getDuration());

            Map<String, Object> details = result.getDetails();
            if (details != null && !details.isEmpty()) {
                for (Map.Entry<String, Object> e : details.entrySet()) {
                    json.writePOJOProperty(e.getKey(), e.getValue());
                }
            }

            json.writeStringProperty("timestamp", result.getTimestamp());
            json.writeEndObject();
        }

        private void serializeThrowable(JsonGenerator json, Throwable error, String name) throws IOException {
            if (error != null) {
                json.writeObjectPropertyStart(name);
                json.writeStringProperty("type", error.getClass().getTypeName());
                json.writeStringProperty("message", error.getMessage());
                json.writeArrayPropertyStart("stack");
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
