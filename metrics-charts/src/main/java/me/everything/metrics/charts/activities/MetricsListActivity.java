package me.everything.metrics.charts.activities;

import java.util.ArrayList;
import java.util.HashMap;

import me.everything.metrics.charts.R;
import me.everything.metrics.charts.logging.Log;
import me.everything.metrics.filtering.GlobPatternList;
import me.everything.metrics.snapshots.MetricSnapshot;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class MetricsListActivity extends MetricsActivity {

	private static final String LOG = Log.makeLogTag(MetricsListActivity.class);
	
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, MetricsListActivity.class);
		return intent;		
	}
	
	public static Intent createIntent(Context context, String chartTitle, GlobPatternList filter) {
		Intent intent = new Intent(context, MetricsListActivity.class);
		intent.putExtra(MetricsActivity.SeriesFilterExtraName, (Parcelable)filter);
		intent.putExtra(MetricsActivity.ChartTitle, chartTitle);
		return intent;
    }
	
	private SimpleAdapter mSimpleAdapter;

	@Override
	protected void performRefresh() {
		ListView lv = (ListView) findViewById(R.id.listView);
		ArrayList<HashMap<String, String>> displayList = new ArrayList<HashMap<String,String>>();		

		for ( MetricSnapshot snap : reporter().getLatestMetrics()) {	
			if (filter().matches(snap.name())) {
				Log.d(LOG, snap.name() + ": " + snap.toString());
				
				HashMap<String, String> record = new HashMap<String, String>();
				record.put("metricName", "[" + snap.metricType() + "] " + snap.name());
				record.put("metricData", snap.dataToString());
				
				Log.d(LOG, record.toString());
				
				displayList.add(record);
			}
		}
		
		mSimpleAdapter = new SimpleAdapter(this, displayList, 
					R.layout.metrics_list_item, 
					new String[] {"metricName", "metricData" }, 
					new int[] { R.id.metricName, R.id.metricData });
		
		lv.setAdapter(mSimpleAdapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.metrics_list);
		refresh();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
