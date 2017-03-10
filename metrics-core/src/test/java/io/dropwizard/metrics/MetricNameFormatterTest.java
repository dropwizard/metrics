package io.dropwizard.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class MetricNameFormatterTest {
	
	@Test
	public void nameOnlyFormatterTest() {
		MetricName name = new MetricName("this.is.a.test",Collections.singletonMap("k1", "v2"));
		Assert.assertEquals("this.is.a.test", MetricNameFormatter.NAME_ONLY.formatMetricName(name));		
	}
	
	@Test
	public void appendTagsFormatterTest() {
		MetricNameFormatter formatter = MetricNameFormatter.APPEND_TAGS;
		MetricName name = new MetricName("this.is.a.test",Collections.singletonMap("k1", "v2"));
		Assert.assertEquals("this.is.a.test.k1.v2",formatter.formatMetricName(name));
		
		// test sorting is done
		Map<String,String> tags = new HashMap<>();
		tags.put("tag1", "value1");
		tags.put("key1", "kv1");
		tags.put("a", "b");
		name = new MetricName("this.is.a.test",tags);
		
		Assert.assertEquals("this.is.a.test.a.b.key1.kv1.tag1.value1", formatter.formatMetricName(name));
	}
	
	@Test
	public void appendTagValuesFormatterTest() {
		MetricNameFormatter formatter = MetricNameFormatter.APPEND_TAG_VALUES;
		MetricName name = new MetricName("this.is.a.test",Collections.singletonMap("k1", "v2"));
		Assert.assertEquals("this.is.a.test.v2",formatter.formatMetricName(name));
		
		// test sorting is done correctly
		Map<String,String> tags = new HashMap<>();
		tags.put("tag1", "value1");
		tags.put("zkey1", "kv1");
		tags.put("a", "b");
		name = new MetricName("this.is.a.test",tags);
		
		Assert.assertEquals("this.is.a.test.b.kv1.value1", formatter.formatMetricName(name));
	}
	
	@Test
	public void toStringFormatterTest() {
		Map<String,String> tags = new HashMap<>();
		tags.put("tag1", "value1");
		tags.put("key1", "kv1");
		tags.put("a", "b");
		MetricName name = new MetricName("this.is.a.test",tags);
		
		Assert.assertEquals(name.toString(), MetricNameFormatter.METRIC_NAME_TOSTRING.formatMetricName(name));
		
	}
	
	

}
