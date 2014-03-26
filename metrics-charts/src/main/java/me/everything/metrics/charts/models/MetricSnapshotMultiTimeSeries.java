package me.everything.metrics.charts.models;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.everything.metrics.charts.logging.Log;
import me.everything.metrics.snapshots.MetricSnapshot;
import me.everything.metrics.utils.TimeUtils;

import org.achartengine.model.TimeSeries;

public class MetricSnapshotMultiTimeSeries {
	
	private static final String LOG = Log.makeLogTag(MetricSnapshotMultiTimeSeries.class);

	private long mTimeFirst = 0;
	private long mTimeLast = 0;
	private long mWindowSizeMs = TimeUtils.SECONDS_IN_MINUTE * TimeUtils.MSECS_IN_SECOND;
	private ConcurrentHashMap<String, TimeSeries> mAllSeries;
	
	public MetricSnapshotMultiTimeSeries() {
		mAllSeries = new ConcurrentHashMap<String, TimeSeries>();
	}
	
	public Set<String> seriesNames() {
		return mAllSeries.keySet();
	}
	
	public ConcurrentHashMap<String, TimeSeries> getAllSeries() {
		return mAllSeries;
	}
	
	public long windowSize() {
		return mWindowSizeMs;
	}
	
	public long timeFirst() {
		return mTimeFirst;
	}
	
	public long timeLast() {
		return mTimeLast;
	}
	
	public void clear() {
		mTimeFirst = 0;
		mTimeLast = 0;
		mAllSeries.clear();
	}
	
	public void setWindowSize(long ms) {
		mWindowSizeMs = ms;		
	}
	
	public int add(MetricSnapshot snap) {
		long time = snap.timestamp();
		String metricName = snap.name();
		String type = snap.metricType();
		String key;
		int additions = 0;
		
		if ((mTimeFirst == 0) || (mTimeLast == 0)) {
			mTimeFirst = time;
			mTimeLast = time;
		} else {
			if (time < mTimeFirst) {
				mTimeFirst = time;
			}
			if (time > mTimeLast) {
				mTimeLast = time;
			}			
		}
		
		Map<String, Double> allSeries = snap.allValues();
		
		for (Map.Entry<String, Double> entry : allSeries.entrySet()) {
			key = metricName + ":" + type + "." + entry.getKey();
			TimeSeries series;
			if (mAllSeries.containsKey(key)) {
				series = mAllSeries.get(key);
			} else {
				Log.v(LOG, "add: creating new TimeSeries(" + key + ")");
				series = new TimeSeries(key);				
			}
			
			Date d = new Date(time);
			Double val = entry.getValue();
			if (val == null) {
				Log.w(LOG, "add: received null value " + key + "(" + time + ")");
			} else {
				series.add(d, entry.getValue());
				//Log.v(LOG, "add: adding value " + key + "(" + time + ", " + val + ")");
				additions++;
				mAllSeries.put(key, series);
			}
		}		
				
		//Log.v(LOG, "add: performed " + additions + " modifications");
		return additions;
	}
	
	public static int findFirstIndexBelow(TimeSeries series, long time) {
		double tolerance = 500.0;
		double threshold = ((double)time) - tolerance;
		double x;
		
		if (series.getItemCount() <= 0) {
			return -1;
		} else {			
			int i=0;
			x = series.getX(i);
			while ((i < series.getItemCount()) && (x <= threshold)) {
				i++;
				x = series.getX(i);
			} 
			return i-1;
		}		
	}
	
	public int cullTimesOutsideWindow() {
		if ((mTimeFirst == 0) || (mTimeLast == 0)) {
			return 0;
		}
		if (mWindowSizeMs <= 0) {
			return 0;
		}
		long firstAllowed = mTimeLast - mWindowSizeMs;
		//Log.v(LOG, "cullTimes: mTimeFirst=" + mTimeFirst + ", mTimeLast=" + mTimeLast + ", firstAllowed=" + firstAllowed + ", removeMs=" + (firstAllowed - mTimeFirst));
		int deletions = 0;
		if (mTimeFirst < firstAllowed) {
			for (ConcurrentHashMap.Entry<String, TimeSeries> entry : mAllSeries.entrySet()) {
				TimeSeries series = entry.getValue();
				int index = findFirstIndexBelow(series, firstAllowed);
				//Log.v(LOG, "cullTimes: series=" + seriesName + ", index(" + firstAllowed + ")=" + index);
				while (index >= 0) {
					series.remove(index);
					index--;
					deletions++;
				}
			}
			Log.v(LOG, "cullTimes: removed " + (firstAllowed - mTimeFirst) + " ms of expired data (new window size = " + (mTimeLast - firstAllowed));
			mTimeFirst = firstAllowed;
		}
		Log.v(LOG, "cullTimes: deleted " + deletions + " entries");
		return deletions;
	}
}
