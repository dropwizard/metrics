package com.codehale.metrics.influxdb;

import java.util.Map;

import com.codehale.metrics.influxdb.data.InfluxDbPoint;

public interface InfluxDbSender {
    /**
     * Flushes buffer, if applicable.
     */
    void flush();

    /**
     * @return true if there is data available to send.
     */
    boolean hasSeriesData();

    /**
     * Adds this metric point to the buffer.
     *
     * @param point metric point with tags and fields
     */
    void appendPoints(final InfluxDbPoint point);

    /**
     * Writes buffer data to InfluxDb.
     *
     * @return the response code for the request sent to InfluxDb.
     *
     * @throws Exception exception while writing to InfluxDb api
     */
    int writeData() throws Exception;

    /**
     * Set tags applicable for all the points.
     *
     * @param tags map containing tags common to all metrics
     */
    void setTags(final Map<String, String> tags);
}
