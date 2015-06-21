package io.dropwizard.metrics;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
* @author victorp
*/
public class SlidingTimeWindowReservoirLeakTest {

    private static Logger log  = LoggerFactory.getLogger(SlidingTimeWindowReservoirLeakTest.class);

    private static final long RESERVOIR_TIME_WIN_IN_MILLIS = 10000;
    private static final long BYTE_IN_ONE_KB = 1024 ;
    private static final long BYTE_IN_ONE_MB = 1024*1024;

    private ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(10);

    @Test
    public void runTest() throws Exception {
        log.info("RESERVOIR_TIME_WIN_IN_MILLIS : {}", RESERVOIR_TIME_WIN_IN_MILLIS);
        MetricRegistry registry = new MetricRegistry();
        String histNamePrefix = "test.hist.";
        long testTimeInMillis = 2*60000; //2 minutes


        int metricsCount = 100000;
        Set<Integer> hashes = new HashSet<Integer>();
        for (int i = 0; i < metricsCount; i++) {
            String name = histNamePrefix + i;
            Reservoir reservoir = new SlidingTimeWindowReservoir(RESERVOIR_TIME_WIN_IN_MILLIS,TimeUnit.MILLISECONDS);
            final Timer timer = new Timer(reservoir);
            registry.register(name, timer);
            hashes.add(name.hashCode());
        }

        log.info("Hashes.size = {}",hashes.size());

        Reservoir reservoirFotGc = new SlidingTimeWindowReservoir(testTimeInMillis*2, TimeUnit.MINUTES);
        final Histogram gcHist = new Histogram(reservoirFotGc);
        String gcHistogramName = "test.gc.hist";
        registry.register(gcHistogramName, gcHist);

        threadPoolExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long used = getUsedMemory();
                gcHist.update(used);
            }
        }, 10, 10, TimeUnit.SECONDS);

        for (int i = 0; i<= 3; i++){
            registry.histogram("test.used.before.load").update(getUsedMemory());
            Thread.sleep(1000);
        }
        long usedMemoryBeforeLoad = registry.histogram("test.used.before.load").getSnapshot().getMin();
        log.info("usedMemoryBeforeLoad: {}",usedMemoryBeforeLoad);

        log.info("Start");
        long totalCount = 0;
        long startTime = System.currentTimeMillis();

        Set<Future<Long>> futures = new HashSet<Future<Long>>();
        for (int threadCount = 0 ; threadCount < 5; threadCount++){
            Future<Long> runLoadFuture = threadPoolExecutor.submit(new RunLoad(registry, histNamePrefix, testTimeInMillis, metricsCount, totalCount, startTime));
            futures.add(runLoadFuture);
        }

        for (Future<Long> future :futures){
            totalCount += future.get(testTimeInMillis*2,TimeUnit.MILLISECONDS);
        }


        double usedMemoryByMetrics = gcHist.getSnapshot().getMedian() - usedMemoryBeforeLoad;
        log.info("End. Total count: {}, samples count:{},  used memory: median = {} per75 = {} per99 = {} max={}",totalCount,gcHist.getSnapshot().size(),gcHist.getSnapshot().getMedian(),gcHist.getSnapshot().get75thPercentile(),gcHist.getSnapshot().get99thPercentile(),gcHist.getSnapshot().getMax());
        log.info("All used memory results: {}",gcHist.getSnapshot().getValues());
        log.info("Used memory by metrics: {}",usedMemoryByMetrics);
        double rate = (totalCount*100000)/testTimeInMillis;
        log.info("Rate updates/milli: {}",(totalCount*100000)/testTimeInMillis);
        double normalRate = 100;
        log.info("Normal Rate updates/milli: {}",100);
        double normalizedUsedMemoryInBytes = (normalRate/rate)*usedMemoryByMetrics;
        log.info("Normalized used memory according to normal rate: {} B, {} KB, {} MB",normalizedUsedMemoryInBytes,normalizedUsedMemoryInBytes/BYTE_IN_ONE_KB, normalizedUsedMemoryInBytes/BYTE_IN_ONE_MB );


        Assert.assertTrue("Normalized used memory must be lower than 70 MB but was " + normalizedUsedMemoryInBytes / BYTE_IN_ONE_MB, 70 > normalizedUsedMemoryInBytes / BYTE_IN_ONE_MB);
        Assert.assertTrue("Rate updates/milli must be bigger than 430 but was "+rate,rate>430);

    }

    private static class RunLoad implements Callable<Long> {
        private long startTime;
        private long testTimeInMillis;
        private int metricsCount;
        private MetricRegistry registry;
        private String histNamePrefix;


        public  RunLoad(MetricRegistry registry, String histNamePrefix, long testTimeInMillis, int metricsCount, long totalCount, long startTime) {
            this.startTime = startTime;
            this.testTimeInMillis = testTimeInMillis;
            this.metricsCount = metricsCount;
            this.registry = registry;
            this.histNamePrefix = histNamePrefix;
        }

        @Override
        public Long call() throws Exception {
            boolean printed = false;

            Long totalCount = 0L;
            while ((System.currentTimeMillis() - startTime) < testTimeInMillis) {
                for (int i = 0; i < metricsCount; i++) {
                    registry.timer(histNamePrefix + i).time().stop();
                }
                totalCount++;
                if (!printed) {
                    if (System.currentTimeMillis() - startTime > RESERVOIR_TIME_WIN_IN_MILLIS*5) {
                        log.info("Memory should be stable from now. Total count: {}", totalCount);
                        printed = true;
                    }
                }
            }
            return totalCount;
        }
    }

    private long getUsedMemory() {
        System.gc();
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        log.info("Memory: used in bytes: {}, in KB: {}, in MB: {}",used,used/BYTE_IN_ONE_KB,used/BYTE_IN_ONE_MB);
        return used;
    }

}
