package me.everything.metrics.charts.activities;

import java.util.Collection;
import java.util.Map;

import me.everything.metrics.charts.R;
import me.everything.metrics.charts.logging.Log;
import me.everything.metrics.filtering.GlobPatternList;
import me.everything.metrics.snapshots.MetricSnapshot;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.LinearLayout;

public class MetricsBarChartActivity extends MetricsActivity {

	private static final String LOG = Log.makeLogTag(MetricsBarChartActivity.class);
	
    public static Intent createIntent(Context context, String chartTitle, GlobPatternList filter) {
		Intent intent = new Intent(context, MetricsBarChartActivity.class);
		intent.putExtra(MetricsActivity.SeriesFilterExtraName, (Parcelable)filter);
		intent.putExtra(MetricsActivity.ChartTitle, chartTitle);
		return intent;
    }
	
	private LinearLayout mLayout;
    private GraphicalView mChartView;
    
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
    
    private double mMinVal;
    private double mMaxVal;
    private boolean mIsEmpty;
    
    private int mBarIndex;
		
    private void addSeries(String name, double value) {
		int color = seriesColor(name);

		XYSeries series = new XYSeries(name);
		series.add(mBarIndex, value);
		
		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		seriesRenderer.setColor(color);
		
		mDataset.addSeries(series);
		mRenderer.addSeriesRenderer(seriesRenderer);
		mRenderer.setDisplayChartValues(true);
    }
    
	@Override
	protected void performRefresh() {
		Collection<MetricSnapshot> latestAll = reporter().getLatestMetrics();
		// Do we need to add or remote series?
        mChartView.invalidate();

        mRenderer.removeAllRenderers();
		mDataset.clear();
						
		mMinVal = 0;
		mMaxVal = 10;
		mIsEmpty = true;
		mBarIndex = 0;
		
        int viewWidth = mChartView.getWidth();
		
		Log.d(LOG, "---");
		for (MetricSnapshot snap : latestAll) {
			for (Map.Entry<String, Double> entry : snap.barValues().entrySet()) {
				double val = entry.getValue();
				String name = entry.getKey();
				String key = snap.nameAndType() + "." + name;
				if (filter().matches(key)) {				
					mBarIndex++;
					addSeries(key, val);
					
					if (mIsEmpty) {
						mMinVal = mMaxVal = val;
						mIsEmpty = false;
					} else {					
						mMaxVal = Math.max(mMaxVal, val);
						mMinVal = Math.min(mMinVal, val);
					}
				}				
			}
		}
		
		if (!mIsEmpty) {
			double margin = Math.max((mMaxVal - mMinVal) * 0.05, 0.1);
			double yMin;
			if ((Math.abs(mMinVal) < (margin / 10)) && (mMinVal >= 0)) {
				yMin = 0;
			} else {
				yMin = mMinVal - margin;
			}
			double yMax = mMaxVal + margin;
			if (yMax - yMin < margin) {
				yMin = yMin - margin;
				yMax = yMax + margin;
			}
			
			Log.d(LOG, "min=" + yMin + ", max=" + yMax + ", index=" + mBarIndex);
	        mRenderer.setYAxisMin(yMin);
	        mRenderer.setYAxisMax(yMax);
	        
	        int[] margins = mRenderer.getMargins();
	        float totalSize = (float)viewWidth - (margins[1] + margins[3]);
	        float oneSize = totalSize / (float)mBarIndex;
	        //mRenderer.setBarSpacing(0f);
			//mRenderer.setBarWidth(oneSize);
			Log.d(LOG, "totalSize=" + totalSize + ", oneSize=" + oneSize + ", barSpacing=" + mRenderer.getBarSpacing() + ", barWidth=" + mRenderer.getBarWidth());
			
			mRenderer.setXAxisMin(0.5f);
	        mRenderer.setXAxisMax(mBarIndex + 0.5f);
		}
		
		
        mChartView.repaint();
	}	
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.metrics_chart);

        mLayout = (LinearLayout) findViewById(R.id.chart);

        // create dataset and renderer
        mDataset = new XYMultipleSeriesDataset();
        mRenderer = new XYMultipleSeriesRenderer();
        
        mRenderer.setZoomButtonsVisible(false);
        mRenderer.setZoomEnabled(true);
        mRenderer.setExternalZoomEnabled(true);
        mRenderer.setClickEnabled(false);
        mRenderer.setPanEnabled(false);

        mRenderer.setAxisTitleTextSize(24);
        mRenderer.setChartTitleTextSize(24);
        mRenderer.setLabelsTextSize(24);
        mRenderer.setLegendTextSize(24);
        //mRenderer.setPointSize(6f);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(10f);
        mRenderer.setXLabelsPadding(8f);
        mRenderer.setChartValuesTextSize(24);
        
        mRenderer.setShowLegend(true);
        mRenderer.setGridColor(Color.DKGRAY);
        mRenderer.setShowGridX(true);
        mRenderer.setShowGridY(false);
        
        mRenderer.setMargins(new int[] { 20, 80, 20, 20 });

        mRenderer.setXLabels(0);
        mRenderer.setYLabels(11);
        
        mChartView = ChartFactory.getBarChartView(this, mDataset, mRenderer, Type.DEFAULT);
        mChartView.setBackgroundColor(Color.BLACK);
        mChartView.repaint();
        mLayout.addView(mChartView);

		refresh();
    }
}
