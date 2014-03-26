package me.everything.metrics.charts.activities;

import java.util.Random;

import me.everything.metrics.charts.logging.Log;
import me.everything.metrics.charts.reporters.DebugReporter;
import me.everything.metrics.charts.reporters.MetricSnapshotReporter;
import me.everything.metrics.filtering.GlobPatternList;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

public abstract class MetricsActivity extends Activity implements MetricSnapshotReporter.IListener {

	private DebugReporter mReporter;
	private final Handler mHandler = new Handler();
	
	private static final String LOG = Log.makeLogTag(MetricsActivity.class);
	public static final String SeriesFilterExtraName = "metricsFilter";
	public static final String ChartTitle = "title";
	
	
	private GlobPatternList mSeriesFilter;

	public GlobPatternList filter() {
		return mSeriesFilter;
	}
	
	protected DebugReporter reporter() {
		return mReporter;
	}
	
	protected Handler handler() {
		return mHandler;
	}	

	@Override
	public void onMetricsReport(int modifications) {
		if (modifications > 0) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					refresh();
				}
			});	
		}
	}
	
	private void verifyValidFilter() {
		if (mSeriesFilter == null) {
			mSeriesFilter = defaultFilter();
		}
	}
	
	public final void refresh() {
		verifyValidFilter();
		performRefresh();
	}
	
	protected abstract void performRefresh();

	protected GlobPatternList defaultFilter() {
		GlobPatternList filter = new GlobPatternList();
		filter.add("*");
		return filter;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mSeriesFilter = getPatternListFromIntent(intent);
		String title = getChartTitleFromIntent(intent);
		if (title != null) {
			setTitleString(title);
		} else {
			setTitle("Metrics");
		}
		mReporter = DebugReporter.getInstance();
		mReporter.addListener(this);
	}
	
	protected GlobPatternList getPatternListFromIntent(Intent intent) {
		GlobPatternList filter = null;
		if (intent.hasExtra(SeriesFilterExtraName)) {
			Object obj = intent.getExtras().get(SeriesFilterExtraName);
			if (obj == null) {
				Log.w(LOG, "Intent contanis the appropriate extra field, but is equal to null");
			} else if (obj instanceof GlobPatternList) {
				filter = (GlobPatternList)obj;
			} else {
				Log.w(LOG, "Received series filter in intent extras, but not correct object type!");
			}
		} else {
			Log.d(LOG, "Intent does not have filter extra");
		}
		return filter;
	}
	
	protected String getChartTitleFromIntent(Intent intent) {
		String title = null;
		if (intent.hasExtra(ChartTitle)) {
			title = intent.getExtras().getString(ChartTitle);
			if (title == null) {
				Log.w(LOG, "Intent contanis the appropriate extra field, but is equal to null");
			}
		} else {
			Log.d(LOG, "Intent does not have title extra");
		}
		return title;		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mSeriesFilter == null) {
			mSeriesFilter = defaultFilter();
		}		
	}
	
	@Override
	protected void onDestroy() {
		mReporter.removeListener(this);
		mReporter = null;
		mSeriesFilter = null;
		super.onDestroy();
	}
	
	protected void setTitleString(String title) {
		getActionBar().setTitle(title);
	}

	public static int seriesColor(String seriesName) {
		Random rand = new Random(seriesName.hashCode());
		int r = rand.nextInt(128) + 127;
		int g = rand.nextInt(128) + 127;
		int b = rand.nextInt(128) + 127;
        return Color.rgb(r, g, b);
	}

}
