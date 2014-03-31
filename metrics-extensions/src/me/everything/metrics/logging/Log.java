package me.everything.metrics.logging;

public class Log {
	private static final String LOG_PREFIX = "metrics.";
	private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
	private static final int MAX_LOG_TAG_LENGTH = 30;
	private static boolean mEnabled = false;	
	
	public static String makeLogTag(String str) 
	{
		if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) 
		{
			return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
		}
		return LOG_PREFIX + str;
	}

	public static void setLogEnabled( boolean enabled ) {
		mEnabled  = enabled;
	}
	
	/**
	 * WARNING: Don't use this when obfuscating class names with Proguard!
	 */
	public static String makeLogTag(Class<?> cls) 
	{
		return makeLogTag(cls.getSimpleName());
	}
	
	static public void v(String tag, String msg)
	{
		try
		{
			if (mEnabled) android.util.Log.v(tag, msg);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+v) [" + tag + "] " + msg);
		}
	}

	static public void v(String tag, String msg, Throwable tr)
	{
		try
		{
			if (mEnabled) android.util.Log.v(tag, msg, tr);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+v) [" + tag + "] " + msg  + ": " + tr.toString());
		}
	}

	static public void d(String tag, String msg)
	{
		try
		{
			if (mEnabled) android.util.Log.d(tag, msg);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+d) [" + tag + "] " + msg);
		}
	}

	static public void d(String tag, String msg, Throwable tr)
	{
		try
		{
			if (mEnabled) android.util.Log.d(tag, msg, tr);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+d) [" + tag + "] " + msg  + ": " + tr.toString());
		}
	}

	static public void i(String tag, String msg)
	{
		try
		{
			if (mEnabled) android.util.Log.i(tag, msg);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+i) [" + tag + "] " + msg);
		}
	}

	static public void i(String tag, String msg, Throwable tr)
	{
		try
		{
			if (mEnabled) android.util.Log.i(tag, msg, tr);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+i) [" + tag + "] " + msg  + ": " + tr.toString());
		}
	}

	static public void w(String tag, String msg)
	{
		try
		{
			if (mEnabled) android.util.Log.w(tag, msg);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+w) [" + tag + "] " + msg);
		}
	}

	static public void w(String tag, String msg, Throwable tr)
	{
		try
		{
			if (mEnabled) android.util.Log.w(tag, msg, tr);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+w) [" + tag + "] " + msg  + ": " + tr.toString());
		}
	}

	static public void e(String tag, String msg)
	{
		try
		{
			if (mEnabled) android.util.Log.e(tag, msg);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+e) [" + tag + "] " + msg);
		}
	}

	static public void e(String tag, String msg, Throwable tr)
	{
		try
		{
			if (mEnabled) android.util.Log.e(tag, msg, tr);
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("(+e) [" + tag + "] " + msg  + ": " + tr.toString());
		}
	}
}
