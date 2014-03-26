package me.everything.metrics.charts;

import me.everything.metrics.charts.logging.Log;
import me.everything.metrics.utils.GlobPatternList;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

public class GlobPatternMetricFilter implements MetricFilter {

	private static final String LOG = Log.makeLogTag(GlobPatternMetricFilter.class);
	
	// HACK: GSON doesn't handle deserialization of GlobPatternList directly well :(	
	private String[] include = null;
	private String[] exclude = null;
	
	private transient GlobPatternList compiledInclude = null;
	private transient GlobPatternList compiledExclude = null;
	
	public static GlobPatternMetricFilter ALL = new GlobPatternMetricFilter(GlobPatternList.ALL, GlobPatternList.NONE);
	public static GlobPatternMetricFilter NONE = new GlobPatternMetricFilter(GlobPatternList.NONE, GlobPatternList.ALL);
	
	public GlobPatternMetricFilter() {		
	}
	
	public GlobPatternMetricFilter(GlobPatternList include) {
		this(include, GlobPatternList.NONE); 
	}
	
	public GlobPatternMetricFilter(String include) {
		this(new GlobPatternList(include), GlobPatternList.NONE);
	}

	public GlobPatternMetricFilter(String include, String exclude) {
		this(new GlobPatternList(include), new GlobPatternList(exclude));
	}
	
	public GlobPatternMetricFilter(String[] includes) {
		this(new GlobPatternList(includes), GlobPatternList.NONE);
	}

	public GlobPatternMetricFilter(String[] includes, String[] excludes) {
		this(new GlobPatternList(includes), new GlobPatternList(excludes));
	}
	
	public GlobPatternMetricFilter(GlobPatternList include, GlobPatternList exclude) {
		this.compiledInclude = include;
		this.compiledExclude = exclude;
	}
	
	public String toString() {
		return "Include: " + getInclude().toString() + ", Exclude: " + getExclude().toString();
	}

	@Override
	public boolean matches(String name, Metric metric) {		
		String fullName = name + ":" + metric.getClass().getSimpleName();
		boolean ret = false;
		if (getInclude().matches(fullName)) {
			if (!getExclude().matches(fullName)) {
				ret = true;
			}
		}
		Log.d(LOG, fullName + ": " + ret);
		return ret;
	}
	
	public GlobPatternList getInclude() {
		if (compiledInclude == null) {
			if (include != null) {
				compiledInclude = new GlobPatternList(include);
			} else {
				compiledInclude = GlobPatternList.ALL;
			}
		}
		return compiledInclude;
	}
	
	public GlobPatternList getExclude() {
		if (compiledExclude == null) {
			if (exclude != null) {
				compiledExclude = new GlobPatternList(exclude);
			} else {
				compiledExclude = GlobPatternList.NONE;
			}
		}
		return compiledExclude;
	}

}
