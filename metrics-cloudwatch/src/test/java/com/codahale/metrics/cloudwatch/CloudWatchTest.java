package com.codahale.metrics.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class CloudWatchTest {
    private final AmazonCloudWatchClient client = mock(AmazonCloudWatchClient.class);
    private final CloudWatchPacket cloudWatchPacket = new CloudWatchPacket("example", client);


    @Test
    public void writeValuesToCloudWatch() throws Exception {

        cloudWatchPacket.add("example.metric1", 1.0);
        cloudWatchPacket.add("example.metric2", 2.0);
        cloudWatchPacket.send();

        Matcher<PutMetricDataRequest> matcher = new TypeSafeMatcher<PutMetricDataRequest>() {
            @Override
            protected boolean matchesSafely(PutMetricDataRequest item) {
                assertEquals(item.getNamespace(), "example");
                assertEquals(item.getMetricData().size(), 2);
                assertEquals(item.getMetricData().get(1).getMetricName(), "example.metric2");
                assertEquals(item.getMetricData().get(0).getDimensions().size(), 1);
                assertEquals(item.getMetricData().get(0).getDimensions().get(0).getName(), "hostname");
                assertNotNull(item.getMetricData().get(0).getDimensions().get(0).getName());
                return true;
            }

            @Override
            public void describeTo(Description description) {

            }
        };

        verify(client).putMetricData(argThat(matcher));

    }


}
