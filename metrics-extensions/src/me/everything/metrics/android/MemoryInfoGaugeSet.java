package me.everything.metrics.android;


public class MemoryInfoGaugeSet /*implements MetricSet*/ {	
//	
//	private final ActivityManager mActivityManager;
//	private final int mMyPid;
//	private MemoryInfo mLastMemoryInfo;
//	private long mLastRefreshTime = 0;
//	private final long mUpdateThresholdMs;
//	
//	public MemoryInfoGaugeSet(ActivityManager activityManager, long updateThresholdMs) {
//		mActivityManager = activityManager;
//		mMyPid = android.os.Process.myPid();
//		mUpdateThresholdMs = updateThresholdMs;
//		refresh();
//	}
//	
//	public void refresh() {
//		long time = System.currentTimeMillis();
//		if (time - mLastRefreshTime >= mUpdateThresholdMs) {
//			mLastMemoryInfo = mActivityManager.getProcessMemoryInfo(new int[] {mMyPid})[0];
//			mLastRefreshTime = time;
//		}
//	}
//
//	@Override
//	public Map<String, Metric> getMetrics() {
//        final Map<String, Metric> metrics = new HashMap<String, Metric>();
//
//        metrics.put(MetricRegistry.name("android", "mem", "dalvikPrivateDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.dalvikPrivateDirty;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "dalvikPss"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.dalvikPss;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "dalvikSharedDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.dalvikSharedDirty;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "nativePrivateDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.nativePrivateDirty;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "nativeSharedDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.nativeSharedDirty;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "nativePss"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.nativePss;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "otherPrivateDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.otherPrivateDirty;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "otherPss"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.otherPss;
//            }
//        });
//
//        metrics.put(MetricRegistry.name("android", "mem", "otherSharedDirty"), new Gauge<Integer>() {
//            @Override
//            public Integer getValue() {
//            	refresh();
//            	return mLastMemoryInfo.otherSharedDirty;
//            }
//        });
//        
//		return metrics;
//	}

}
