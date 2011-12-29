package com.yammer.metrics.aop.benchmarks;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.concurrent.TimeUnit;

import static com.yammer.metrics.aop.Instrumentation.instrument;

public class BasicBenchmark {
    public static class Fixture {
        private static final TimerMetric TIMER = Metrics.newTimer(Fixture.class, "regular");
        
        public String regular() {
            final TimerContext context = TIMER.time();
            try {
                return "regular";
            } finally {
                context.stop();
            }
        }

        @Timed
        public String instrumented() {
            return "instrumented";
        }
    }
    
    private static final long ITERATIONS = 100000000L;
    
    public static void main(String[] args) {
        if (args.length > 0) {
            ConsoleReporter.enable(1, TimeUnit.SECONDS);

            if (args[0].equals("regular")) {
                runRegular();
            } else if (args[0].equals("instrumented")) {
                runInstrumented();
            } else {
                System.err.println("No idea what to make of " + args[0]);
            }
        } else {
            System.err.println("java -jar project.jar regular|instrumented");
        }
    }

    private static void runRegular() {
        System.err.println("Benchmarking regular methods");
        final Fixture fixture = new Fixture();
        for (long i = 0; i < ITERATIONS; i++) {
            fixture.regular();
        }
    }

    private static void runInstrumented() {
        System.err.println("Benchmarking instrumented methods");
        final Fixture fixture = instrument(new Fixture());
        for (long i = 0; i < ITERATIONS; i++) {
            fixture.instrumented();
        }
    }

}
