package me.everything.metrics.utils;

import java.util.Locale;
import java.util.regex.Pattern;

import android.os.Parcel;
import android.os.Parcelable;

public class GlobPattern implements Parcelable {
	
	private String mExpression;
	private transient Pattern mPattern;
	
	public GlobPattern() {
		set("");
	}	

	public GlobPattern(String globExpression) {
		set(globExpression);
	}
	
	public void set(String globExpression) {
		mExpression = globExpression.toLowerCase(Locale.US);
		mPattern = compileGlobPattern(mExpression);
	}
	
	public boolean matches(String str) {
		return mPattern.matcher(str.toLowerCase(Locale.US)).find();
	}
	
	public String toString() {
		return mExpression;
	}
	
	public String getExpression() {
		return mExpression;
	}
	
	public static Pattern compileGlobPattern(String line) {
	    line = line.trim();
	    int strLen = line.length();
	    StringBuilder sb = new StringBuilder(strLen);
	    // Remove beginning and ending * globs because they're useless
	    if (line.startsWith("*"))
	    {
	        line = line.substring(1);
	        strLen--;
	    }
	    if (line.endsWith("*"))
	    {
	        line = line.substring(0, strLen-1);
	        strLen--;
	    }
	    boolean escaping = false;
	    int inCurlies = 0;
	    for (char currentChar : line.toCharArray())
	    {
	        switch (currentChar)
	        {
	        case '*':
	            if (escaping)
	                sb.append("\\*");
	            else
	                sb.append(".*");
	            escaping = false;
	            break;
	        case '?':
	            if (escaping)
	                sb.append("\\?");
	            else
	                sb.append('.');
	            escaping = false;
	            break;
	        case '.':
	        case '(':
	        case ')':
	        case '+':
	        case '|':
	        case '^':
	        case '$':
	        case '@':
	        case '%':
	            sb.append('\\');
	            sb.append(currentChar);
	            escaping = false;
	            break;
	        case '\\':
	            if (escaping)
	            {
	                sb.append("\\\\");
	                escaping = false;
	            }
	            else
	                escaping = true;
	            break;
	        case '{':
	            if (escaping)
	            {
	                sb.append("\\{");
	            }
	            else
	            {
	                sb.append('(');
	                inCurlies++;
	            }
	            escaping = false;
	            break;
	        case '}':
	            if (inCurlies > 0 && !escaping)
	            {
	                sb.append(')');
	                inCurlies--;
	            }
	            else if (escaping)
	                sb.append("\\}");
	            else
	                sb.append("}");
	            escaping = false;
	            break;
	        case ',':
	            if (inCurlies > 0 && !escaping)
	            {
	                sb.append('|');
	            }
	            else if (escaping)
	                sb.append("\\,");
	            else
	                sb.append(",");
	            break;
	        default:
	            escaping = false;
	            sb.append(currentChar);
	        }
	    }
	    return Pattern.compile(sb.toString());
	}
	
	
	
	public GlobPattern(Parcel in) {
		this();
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		set(in.readString());
	}
	
	@Override
	public void writeToParcel(Parcel dest, int falgs) {
		dest.writeString(mExpression);
	}
	
	public int describeContents() {
		return 0;
	}
	
	public final static Parcelable.Creator<GlobPattern> CREATOR = new Parcelable.Creator<GlobPattern>() {
		public GlobPattern createFromParcel(Parcel in) {
			return new GlobPattern(in);
		}
		
		public GlobPattern[] newArray(int size) {
			return new GlobPattern[size];
		}
	};	
}
