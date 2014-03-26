package me.everything.metrics.charts.activities;

import java.util.concurrent.ConcurrentHashMap;

import me.everything.metrics.charts.MetricSnapshotMultiTimeSeries;
import me.everything.metrics.charts.R;
import me.everything.metrics.utils.GlobPatternList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.LinearLayout;

public class MetricsTimeSeriesActivity extends MetricsActivity {

	private LinearLayout mLayout;
    private GraphicalView mChartView;
    
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mRenderer;
		
    public static Intent createIntent(Context context, String chartTitle, GlobPatternList filter) {
		Intent intent = new Intent(context, MetricsTimeSeriesActivity.class);
		intent.putExtra(MetricsActivity.SeriesFilterExtraName, (Parcelable)filter);
		intent.putExtra(MetricsActivity.ChartTitle, chartTitle);
		return intent;
    }
    
	@Override
	public void performRefresh() {
		MetricSnapshotMultiTimeSeries multiTimeSeries = reporter().getDataset();
		ConcurrentHashMap<String, TimeSeries> entireDataset = multiTimeSeries.getAllSeries();
		
		long windowEnd = multiTimeSeries.timeLast();
		long windowBegin = windowEnd - multiTimeSeries.windowSize();
		
		// Do we need to add or remote series?
        mChartView.invalidate();

		mRenderer.removeAllRenderers();
		mDataset.clear();
		
		for (ConcurrentHashMap.Entry<String, TimeSeries> entry : entireDataset.entrySet()) {
			TimeSeries series = entry.getValue();
			String name = entry.getKey();
			if (filter().matches(name)) {
				mDataset.addSeries(series);
				
		        XYSeriesRenderer r = new XYSeriesRenderer();
		        r.setColor(seriesColor(name));
		        r.setLineWidth(2f);
		        r.setPointStyle(PointStyle.CIRCLE);
		        r.setFillPoints(true);		        
		        mRenderer.addSeriesRenderer(r);
			}
		}

        mRenderer.setXAxisMin(windowBegin);
        mRenderer.setXAxisMax(windowEnd);
        
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
        mRenderer.setAxisTitleTextSize(24);
        mRenderer.setChartTitleTextSize(24);
        mRenderer.setLabelsTextSize(24);
        mRenderer.setLegendTextSize(24);
        mRenderer.setPointSize(3f);
        mRenderer.setClickEnabled(false);
        //mRenderer.setSelectableBuffer(20);
        mRenderer.setPanEnabled(false);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(10.0f);
        mRenderer.setXLabelsPadding(8.0f);
        mRenderer.setChartValuesTextSize(24);
        
        mRenderer.setShowLegend(true);
        mRenderer.setGridColor(Color.DKGRAY);
        mRenderer.setShowGridX(true);
        mRenderer.setShowGridY(true);
        mRenderer.setXLabels(9);
        mRenderer.setYLabels(11);

        mRenderer.setMargins(new int[] { 20, 80, 20, 20 });

        mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "H:mm:ss");
        mChartView.setBackgroundColor(Color.BLACK);
        mChartView.repaint();
        mLayout.addView(mChartView);

		refresh();
    }
	
}
