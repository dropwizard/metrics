package me.everything.metrics.charts.activities;

import java.util.Collection;

import me.everything.metrics.charts.R;
import me.everything.metrics.snapshots.MetricSnapshot;
import me.everything.metrics.utils.GlobPatternList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.widget.LinearLayout;

public class MetricsHistogramActivity extends MetricsActivity {

    public static Intent createIntent(Context context, String chartTitle, GlobPatternList filter) {
		Intent intent = new Intent(context, MetricsHistogramActivity.class);
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
		
    private void addSeries(MetricSnapshot snap, boolean fillBelow) {
		String name = snap.nameAndType();
		int color = seriesColor(name);
		
		String seriesTitle = name + " [" + snap.count() + "]";

		XYSeries series = new XYSeries(seriesTitle);
		XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
		seriesRenderer.setColor(color);
		seriesRenderer.setLineWidth(4f);

		SparseArray<Double> percentiles = snap.percentileValues();

		for (int i=0; i<percentiles.size(); i++) {
			int percent = percentiles.keyAt(i);
			double val = percentiles.valueAt(i);
			series.add(percent, val);				

			if (mIsEmpty) {
				mMinVal = mMaxVal = val;
				mIsEmpty = false;
			}
			
			mMaxVal = Math.max(mMaxVal, val);
		}
		
        seriesRenderer.setPointStyle(PointStyle.X);
        seriesRenderer.setFillPoints(true);

		if (fillBelow) {
	        FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
	        fill.setColor(color);
	        seriesRenderer.addFillOutsideLine(fill);
		}

		mDataset.addSeries(series);
		mRenderer.addSeriesRenderer(seriesRenderer);
    }
    
	@Override
	protected void performRefresh() {
		Collection<MetricSnapshot> latestAll = reporter().getLatestMetrics();
		// Do we need to add or remote series?
        mChartView.invalidate();
        mRenderer.setShowLegend(false);

        mRenderer.removeAllRenderers();
		mDataset.clear();
						
		mMinVal = 0;
		mMaxVal = 10;
		mIsEmpty = true;
		
		for (MetricSnapshot snap : latestAll) {
			if (filter().matches(snap.nameAndType())) {
				addSeries(snap, false);
			}
		}
		
		if (!mIsEmpty) {
			double margin = (mMaxVal - mMinVal) * 0.05;
			double yMin;
			if ((Math.abs(mMinVal) < (margin / 10)) && (mMinVal >= 0)) {
				yMin = 0;
			} else {
				yMin = mMinVal - margin;
			}
			double yMax = mMaxVal + margin;
	        mRenderer.setYAxisMin(yMin);
	        mRenderer.setYAxisMax(yMax);
		}
		        
        mRenderer.setShowLegend(true);
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
        mRenderer.setPointSize(6f);
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsPadding(10f);
        mRenderer.setXLabelsPadding(8f);
        mRenderer.setChartValuesTextSize(24);
        
        mRenderer.setShowLegend(true);
        mRenderer.setGridColor(Color.DKGRAY);
        mRenderer.setShowGridX(true);
        mRenderer.setShowGridY(true);
        
        mRenderer.setMargins(new int[] { 20, 80, 20, 20 });

        mRenderer.setXAxisMin(0);
        mRenderer.setXAxisMax(100);
        mRenderer.setXLabels(11);
        mRenderer.setYLabels(11);
        
        mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        mChartView.setBackgroundColor(Color.BLACK);
        mChartView.repaint();
        mLayout.addView(mChartView);

		refresh();
    }
}
