package com.codehale.metrics.influxdb.utils;

import java.io.IOException;
import java.util.Map;

import com.codehale.metrics.influxdb.data.InfluxDbWriteObject;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class InfluxDbWriteObjectSerializer {

    protected static class MapSerializer<P, Q> extends JsonSerializer<Map<P, Q>> {
        @Override
        public void serialize(final Map<P, Q> influxDbMap, final JsonGenerator jsonGenerator, final SerializerProvider provider)
                throws IOException {
            if (influxDbMap != null) {
                jsonGenerator.writeStartObject();
                for (Map.Entry<P, Q> entry : influxDbMap.entrySet()) {
                    jsonGenerator.writeFieldName(entry.getKey().toString());
                    jsonGenerator.writeObject(entry.getValue());
                }
                jsonGenerator.writeEndObject();
            }
        }
    }

    private final ObjectMapper objectMapper;

    public InfluxDbWriteObjectSerializer() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        final SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null, null, null));
        module.addSerializer(Map.class, new MapSerializer());
        objectMapper.registerModule(module);
    }

    public String getJsonString(InfluxDbWriteObject influxDbWriteObject) throws Exception {
        return objectMapper.writeValueAsString(influxDbWriteObject);
    }
}
