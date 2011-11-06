package com.yammer.metrics.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.*;
import com.yammer.metrics.guice.ExceptionMetered;
import com.yammer.metrics.guice.InstrumentationModule;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ExceptionMeteredTest {

	InstrumentedWithExceptionMetered instance;
    MetricsRegistry registry;
	
	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new InstrumentationModule());
        instance = injector.getInstance(InstrumentedWithExceptionMetered.class);	
        registry = injector.getInstance(MetricsRegistry.class);
	}
	
    @Test
    public void anExceptionMeteredAnnotatedMethodWithPublicScope() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "exceptionCounter"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        
        try {
        	instance.explodeWithPublicScope(true);
        	fail("Expected an exception to be thrown");
        } catch(RuntimeException e) {      
        	// Swallow the expected exception
        }
        
        assertThat("Metric is marked",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithNoMetricName() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "explodeForUnnamedMetric" + ExceptionMetered.DEFAULT_NAME_SUFFIX));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        
        try {
        	instance.explodeForUnnamedMetric();
        	fail("Expected an exception to be thrown");
        } catch(RuntimeException e) {      
        	// Swallow the expected exception
        }
        
        assertThat("Metric is marked",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithPublicScopeButNoExceptionThrown() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "exceptionCounter"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        
        instance.explodeWithPublicScope(false);
        
        assertThat("Metric should remain at zero if no exception is thrown",
                   ((MeterMetric) metric).count(),
                   is(0L));
    }
	
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithDefaultScope() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "explodeWithDefaultScopeExceptionMetric"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));

        try {
        	instance.explodeWithDefaultScope();
        	fail("Expected an exception to be thrown");
        } catch(RuntimeException e) {        	
        }       

        assertThat("Metric is marked",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
	
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithProtectedScope() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "explodeWithProtectedScopeExceptionMetric"));

        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        
        try {
        	instance.explodeWithProtectedScope();
        	fail("Expected an exception to be thrown");
        } catch(RuntimeException e) {        	
        }        

        assertThat("Metric is marked",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithPublicScope_AndSpecificTypeOfException() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "failures"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        try {
        	instance.errorProneMethod(new MyException());
        	fail("Expected an exception to be thrown");
        } catch(MyException e) {        	
        } 
        
        assertThat("Metric should be marked when the specified exception type is thrown",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithPublicScope_AndSubclassesOfSpecifiedException() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "failures"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        try {
        	instance.errorProneMethod(new MySpecialisedException());
        	fail("Expected an exception to be thrown");
        } catch(MyException e) {        	
        } 
        
        assertThat("Metric should be marked when a subclass of the specified exception type is thrown",
                   ((MeterMetric) metric).count(),
                   is(1L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithPublicScope_ButDifferentTypeOfException() throws Exception {

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "failures"));
        assertMetricIsSetup(metric);

        assertThat("Metric intialises to zero",
                   ((MeterMetric) metric).count(),
                   is(0L));
        try {
        	instance.errorProneMethod(new MyOtherException());
        	fail("Expected an exception to be thrown");
        } catch(MyOtherException e) {        	
        } 
        
        assertThat("Metric should not be marked if the exception is a different type",
                   ((MeterMetric) metric).count(),
                   is(0L));
    }
    
    @Test
    public void anExceptionMeteredAnnotatedMethod_WithExtraOptions() throws Exception {
    	
    	try {
    		instance.causeAnOutOfBoundsException();
    	} catch (ArrayIndexOutOfBoundsException e) {
    		
    	}

        final Metric metric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "things"));

        assertMetricIsSetup(metric);

        assertThat("Guice creates a meter which gets marked",
                   ((MeterMetric) metric).count(),
                   is(1L));

        assertThat("Guice creates a meter with the given event type",
                   ((MeterMetric) metric).eventType(),
                   is("poops"));

        assertThat("Guice creates a meter with the given rate unit",
                   ((MeterMetric) metric).rateUnit(),
                   is(TimeUnit.MINUTES));
    }
   

    @Test
    public void aMethodAnnotatedWithBothATimerAndAnExceptionCounter() throws Exception {

        final Metric timedMetric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "timedAndException"));
        
        final Metric errorMetric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
        																"timedAndExceptionExceptionMetric"));

		assertThat("Guice creates a metric",
				timedMetric,
                   is(notNullValue()));

        assertThat("Guice creates a timer",
        		timedMetric,
                   is(instanceOf(TimerMetric.class)));        

		assertThat("Guice creates a metric",
				errorMetric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
        		errorMetric,
                   is(instanceOf(MeterMetric.class)));
        
        // Counts should start at zero        
        assertThat("Timer Metric should be zero when initialised",
                   ((TimerMetric) timedMetric).count(),
                   is(0L));
        
        
        assertThat("Error Metric should be zero when initialised",
                   ((MeterMetric) errorMetric).count(),
                   is(0L));
        
        // Invoke, but don't throw an exception
        instance.timedAndException(null);
        
        assertThat("Expected the meter metric to be marked on invocation",
                   ((TimerMetric) timedMetric).count(),
                   is(1L));
                
        assertThat("Expected the exception metric to be zero since no exceptions thrown",
                   ((MeterMetric) errorMetric).count(),
                   is(0L));

        // Invoke and throw an exception
        try {
        	instance.timedAndException(new RuntimeException());
        	fail("Should have thrown an exception");
        } catch (Exception e) {}
      
        assertThat("Expected a count of 2, one for each invocation",
                   ((TimerMetric) timedMetric).count(),
                   is(2L));
                
        assertThat("Expected exception count to be 1 as one (of two) invocations threw an exception",
                   ((MeterMetric) errorMetric).count(),
                   is(1L));
        
    }
    
    @Test
    public void aMethodAnnotatedWithBothAMeteredAndAnExceptionCounter() throws Exception {

        final Metric meteredMetric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
                                                                       "meteredAndException"));
        
        final Metric errorMetric = registry.allMetrics().get(new MetricName(InstrumentedWithExceptionMetered.class,
        																"meteredAndExceptionExceptionMetric"));

		assertThat("Guice creates a metric",
				meteredMetric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
        		meteredMetric,
                   is(instanceOf(MeterMetric.class)));        

		assertThat("Guice creates a metric",
				errorMetric,
                   is(notNullValue()));

        assertThat("Guice creates an exception meter",
        		errorMetric,
                   is(instanceOf(MeterMetric.class)));
        
        // Counts should start at zero        
        assertThat("Meter Metric should be zero when initialised",
                   ((MeterMetric) meteredMetric).count(),
                   is(0L));
        
        
        assertThat("Error Metric should be zero when initialised",
                   ((MeterMetric) errorMetric).count(),
                   is(0L));
        
        // Invoke, but don't throw an exception
        instance.meteredAndException(null);
        
        assertThat("Expected the meter metric to be marked on invocation",
                   ((MeterMetric) meteredMetric).count(),
                   is(1L));
                
        assertThat("Expected the exception metric to be zero since no exceptions thrown",
                   ((MeterMetric) errorMetric).count(),
                   is(0L));

        // Invoke and throw an exception
        try {
        	instance.meteredAndException(new RuntimeException());
        	fail("Should have thrown an exception");
        } catch (Exception e) {}
      
        assertThat("Expected a count of 2, one for each invocation",
                   ((MeterMetric) meteredMetric).count(),
                   is(2L));
                
        assertThat("Expected exception count to be 1 as one (of two) invocations threw an exception",
                   ((MeterMetric) errorMetric).count(),
                   is(1L));
        
    }

	private void assertMetricIsSetup(final Metric metric) {
		assertThat("Guice creates a metric",
                   metric,
                   is(notNullValue()));

        assertThat("Guice creates a meter",
                   metric,
                   is(instanceOf(MeterMetric.class)));
	}		
	
	@SuppressWarnings("serial")
	private static class MyOtherException extends RuntimeException {
	}
	
	@SuppressWarnings("serial")
	private static class MySpecialisedException extends MyException {
	}
}
