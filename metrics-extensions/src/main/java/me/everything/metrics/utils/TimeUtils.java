package me.everything.metrics.utils;

public class TimeUtils {

	public static final long MSECS_IN_SECOND = 1000;
	public static final long SECONDS_IN_MINUTE = 60;
	public static final long MINUTES_IN_HOUR = 60;
	public static final long HOURS_IN_DAY = 24;

    public static String friendlyDuration(long ms) {
    	String unit = "ms";
    	long val = ms;
    	if (val >= MSECS_IN_SECOND) {
    		val = val / MSECS_IN_SECOND;
    		unit = "sec";    		
    	}
    	if (val >= SECONDS_IN_MINUTE) {
    		val = val / SECONDS_IN_MINUTE;
    		unit = "min";
    	}
    	if (val >= MINUTES_IN_HOUR) {
    		val = val / MINUTES_IN_HOUR;
    		unit = "hr";
    	}
    	if (val >= HOURS_IN_DAY) {
    		val = val / HOURS_IN_DAY;
    		unit = "day";
    	}
    	String str = val + unit;
    	return str;
    }
	    
}
