package com.yammer.metrics.librato;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Realm;
import com.ning.http.client.Response;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * User: mihasya
 * Date: 6/14/12
 * Time: 1:51 PM
 * A class that represents an aggregation of metric data from a given run
 */
public class LibratoBatch {
    public static final int DEFAULT_BATCH_SIZE = 500;

    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<Measurement> measurements = new ArrayList<Measurement>();

    private final int postBatchSize;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    private static final Logger LOG = LoggerFactory.getLogger(LibratoBatch.class);

    public LibratoBatch(int postBatchSize, long timeout, TimeUnit timeoutUnit) {
        this.postBatchSize = postBatchSize;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    /**
     * for advanced measurement fu
     */
    public void addMeasurement(Measurement measurement) {
        measurements.add(measurement);
    }

    public void addCounterMeasurement(String name, Long value) {
        measurements.add(new CounterMeasurement(name, value));
    }

    public void addGaugeMeasurement(String name, Number value) {
        measurements.add(new SingleValueGaugeMeasurement(name, value));
    }

    public void post(AsyncHttpClient.BoundRequestBuilder builder, String source, long epoch) {
        Map<String, Object> resultJson = new HashMap<String, Object>();
        resultJson.put("source", source);
        resultJson.put("measure_time", epoch);
        List<Map<String, Object>> gaugeData = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> counterData = new ArrayList<Map<String, Object>>();

        int counter = 0;
        for (Measurement measurement : measurements) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("name", measurement.getName());
            data.putAll(measurement.toMap());
            if (measurement instanceof CounterMeasurement) {
                counterData.add(data);
            } else {
                gaugeData.add(data);
            }
            counter++;
            if (counter % postBatchSize == 0) {
                resultJson.put("counters", counterData);
                resultJson.put("gauges", gaugeData);
                postPortion(builder , resultJson);
                resultJson.remove("gauges");
                resultJson.remove("counters");
                gaugeData = new ArrayList<Map<String, Object>>();
                counterData = new ArrayList<Map<String, Object>>();
            }
        }
        resultJson.put("counters", counterData);
        resultJson.put("gauges", gaugeData);
        postPortion(builder, resultJson);
        LOG.debug("Posted %d measurements", counter);
    }

    private void postPortion(AsyncHttpClient.BoundRequestBuilder builder, Map<String, Object> chunk) {
        try {
            String chunkStr = mapper.writeValueAsString(chunk);
            builder.setBody(chunkStr);
            Future<Response> response = builder.execute();
            Response result = response.get(timeout, timeoutUnit);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Unable to post to Librato API", e);
        }
    }
}
