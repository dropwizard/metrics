package com.codehale.metrics.influxdb.utils;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;

import com.codehale.metrics.influxdb.data.InfluxDbWriteObject;

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
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);

        final SimpleModule module = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
        module.addSerializer(Map.class, new MapSerializer());
        objectMapper.registerModule(module);
    }

    public String getJsonString(InfluxDbWriteObject influxDbWriteObject) throws Exception {
        return objectMapper.writeValueAsString(influxDbWriteObject);
    }
}
