package com.yammer.metrics.reporting;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.HistogramMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.util.MetricPredicate;

public class CsvReporter extends AbstractPollingReporter {

    private static class StreamPair{
      String file;
      PrintStream stream;
    }
  
    private final MetricPredicate predicate;
    private final File outputDir;
    private final Map<MetricName, StreamPair> streamMap;
    private long startTime;
    private TimeUnit _rollDuration;
    private File _currentFile;

    public CsvReporter(File outputDir,
                       MetricsRegistry metricsRegistry,
                       MetricPredicate predicate) throws Exception {
        super(metricsRegistry, "csv-reporter");
        this.outputDir = outputDir;
        this.predicate = predicate;
        this.streamMap = new HashMap<MetricName, StreamPair>();
        this.startTime = 0L;
        this._rollDuration = TimeUnit.HOURS;
        _currentFile = null;
        outputDir.mkdirs();
    }

    public CsvReporter(File outputDir, MetricsRegistry metricsRegistry)
            throws Exception {
        this(outputDir, metricsRegistry, MetricPredicate.ALL);
    }
    
    public void setRollDuration(TimeUnit duration){
        _rollDuration = duration;
    }
    
    private static long getFileTime(File file){
      String name = file.getName();
      int idx = name.lastIndexOf('-');
      int idx2 = name.lastIndexOf(".csv");
      String numberString = name.substring(idx+1,idx2);
      return Long.parseLong(numberString);
    }
    
    private File getFile(MetricName metricName){
        final String mname = metricName.getName();
        
        File[] files = outputDir.listFiles(new FileFilter(){

          @Override
          public boolean accept(File pathname) {
            String filename = pathname.getName();
            if (filename.startsWith(mname+"-") && filename.endsWith(".csv")){
              return true;
            }
            return false;
          }
          
        });
        
        long currentTime = System.nanoTime();
        if (_currentFile == null){
          if (files.length==0){
            String filename = metricName.getName()+"-"+currentTime+".csv";
            _currentFile = new File(outputDir,filename);
            return _currentFile;
          }
          
          // get the latest file
          long maxTime = 0;
            
          for (File file : files){
            long time = getFileTime(file);
            if (time > maxTime){
              maxTime = time;
              _currentFile = file;
            }
          }
        }
        
        long fileTime = getFileTime(_currentFile);
        long duration = currentTime - fileTime;
          
        long maxDuration = _rollDuration.toNanos(1);
          
        if (duration > maxDuration){
         String filename = metricName.getName()+"-"+currentTime+".csv";
         _currentFile = new File(outputDir,filename);
        }
        return _currentFile;
    }
    
    

    private PrintStream getPrintStream(MetricName metricName, Metric metric)
            throws IOException {
        StreamPair streamPair;
        synchronized (streamMap) {
            streamPair = streamMap.get(metricName);
            
            if (streamPair != null){
              File f = getFile(metricName);
              if (!streamPair.file.equals(f.getName())){
                streamPair.stream.close();
                streamPair = null;
              }
            }
            if (streamPair == null) {
                final File newFile = getFile(metricName);
                
                streamPair = new StreamPair();
                streamPair.file = newFile.getName();
                
                PrintStream stream;
                if (!newFile.exists() && newFile.createNewFile()) {
                    stream = new PrintStream(new FileOutputStream(newFile));
                    streamPair.stream = stream;
                    streamMap.put(metricName, streamPair);
                    if (metric instanceof GaugeMetric<?>) {
                        stream.println("# time,value");
                    } else if (metric instanceof CounterMetric) {
                        stream.println("# time,count");
                    } else if (metric instanceof HistogramMetric) {
                        stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
                    } else if (metric instanceof MeterMetric) {
                        stream.println("# time,count,1 min rate,mean rate,5 min rate,15 min rate");
                    } else if (metric instanceof TimerMetric) {
                        stream.println("# time,min,max,mean,median,stddev,90%,95%,99%");
                    }
                } else {
                    throw new IOException("Unable to create " + newFile);
                }
            }
        }
        return streamPair.stream;
    }

    @Override
    public void run() {
        final long time = (System.currentTimeMillis() - startTime) / 1000;
        final Set<Entry<MetricName, Metric>> metrics = metricsRegistry.allMetrics().entrySet();
        try {
            for (Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {
                    final StringBuilder buf = new StringBuilder();
                    buf.append(time).append(",");
                    if (metric instanceof GaugeMetric<?>) {
                        final Object objVal = ((GaugeMetric<?>) metric).value();
                        buf.append(objVal);
                    } else if (metric instanceof CounterMetric) {
                        buf.append(((CounterMetric) metric).count());
                    } else if (metric instanceof HistogramMetric) {
                        final HistogramMetric timer = (HistogramMetric) metric;

                        final double[] percentiles = timer.percentiles(0.5, 0.90, 0.95, 0.99);
                        buf.append(timer.min()).append(",");
                        buf.append(timer.max()).append(",");
                        buf.append(timer.mean()).append(",");
                        buf.append(percentiles[0]).append(","); // median
                        buf.append(timer.stdDev()).append(",");
                        buf.append(percentiles[1]).append(","); // 90%
                        buf.append(percentiles[2]).append(","); // 95%
                        buf.append(percentiles[3]); // 99 %
                    } else if (metric instanceof MeterMetric) {
                        buf.append(((MeterMetric) metric).count()).append(",");
                        buf.append(((MeterMetric) metric).oneMinuteRate())
                           .append(",");
                        buf.append(((MeterMetric) metric).meanRate()).append(
                                ",");
                        buf.append(((MeterMetric) metric).fiveMinuteRate())
                           .append(",");
                        buf.append(((MeterMetric) metric).fifteenMinuteRate());
                    } else if (metric instanceof TimerMetric) {
                        final TimerMetric timer = (TimerMetric) metric;

                        final double[] percentiles = timer.percentiles(0.5, 0.90, 0.95, 0.99);
                        buf.append(timer.min()).append(",");
                        buf.append(timer.max()).append(",");
                        buf.append(timer.mean()).append(",");
                        buf.append(percentiles[0]).append(","); // median
                        buf.append(timer.stdDev()).append(",");
                        buf.append(percentiles[1]).append(","); // 90%
                        buf.append(percentiles[2]).append(","); // 95%
                        buf.append(percentiles[3]); // 99 %
                    }

                    final PrintStream out = getPrintStream(metricName, metric);
                    out.println(buf.toString());
                    out.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.startTime = System.currentTimeMillis();
        super.start(period, unit);
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
        } finally {
            for (StreamPair out : streamMap.values()) {
                out.stream.close();
            }
        }
    }
    
    public static void main(String[] args) throws Exception{
      
     final CounterMetric counterMetric = Metrics.newCounter(new MetricName("group","counter","test"));
      
      final int max = 100;
      Thread t = new Thread(new Runnable(){
        public void run(){
          for (int i=0;i<max;++i){
            counterMetric.inc();
            
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }
      });
      
      t.start();
      
      MetricsRegistry metricsRegistry = Metrics.defaultRegistry();
      CsvReporter reporter = new CsvReporter(new File("/tmp/metrics-test"), metricsRegistry);
      reporter.setRollDuration(TimeUnit.SECONDS);
      
      reporter.start(500, TimeUnit.MILLISECONDS);
      t.join();
    }
}
