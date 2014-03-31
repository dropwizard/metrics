package me.everything.metrics.utils;

import me.everything.metrics.filtering.GlobPatternList;

public class MetricPreset {

	private String id = "";
	private String title = "Metrics";
	private String type = "list";
	
	// HACK: GSON doesn't handle deserialization of GlobPatternList directly well :(
	private String[] filter = null;
	private transient GlobPatternList compiledFilter = null;
	
	public MetricPreset(String id, String title, String chartType, String[] filter) {
		this.id = id;
		this.title = title;
		this.type = chartType;
		this.filter = filter;
		this.compiledFilter = new GlobPatternList(filter);
	}
	
	public MetricPreset(String id, String title, String chartType, GlobPatternList filter) {
		this.id = id;
		this.title = title;
		this.type = chartType;
		this.filter = null;
		this.compiledFilter = filter;
	}
	
	public String toString() {
		return "MetricPreset: \"" + title + "\" (" + type + "), " + getFilter().toString();
	}

	public String getType() {
		return type;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
	}
	
	public GlobPatternList getFilter() {
		if (compiledFilter == null) {
			compiledFilter = new GlobPatternList(this.filter);
		}
		return compiledFilter;
	}
	
}
