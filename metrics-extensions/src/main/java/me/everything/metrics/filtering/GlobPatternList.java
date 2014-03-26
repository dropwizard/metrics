package me.everything.metrics.filtering;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class GlobPatternList extends ArrayList<GlobPattern> implements Parcelable {

	private static final long serialVersionUID = -770026780857747733L;

	public static GlobPatternList ALL = new GlobPatternList("*");
	public static GlobPatternList NONE = new GlobPatternList();
	
	public GlobPatternList() {
		super();
	}
	
	public GlobPatternList(String globString) {
		super();
		add(globString);
	}
	
	public GlobPatternList(String[] globStrings) {
		addAll(globStrings);
	}
	

	public boolean matches(String seriesName) {
		for (GlobPattern glob : this) {
			if (glob.matches(seriesName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean add(String globString) {
		return add(new GlobPattern(globString));
	}
	
	public void addAll(String[] globStrings) {
		for (String globString : globStrings) {
			add(globString);
		}
	}
		
	public String toString() {
		String str = java.util.Arrays.toString(this.toArray());
		return str;
	}
	
	public GlobPatternList(Parcel in) {
		this();
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		this.clear();
		int size = in.readInt();
		for (int i=0; i<size; i++) {
			GlobPattern p = new GlobPattern(in.readString());
			this.add(p);
		}
	}
	
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<GlobPatternList> CREATOR = new Parcelable.Creator<GlobPatternList>() {
		public GlobPatternList createFromParcel(Parcel in) {
			return new GlobPatternList(in);
		}
		
		public GlobPatternList[] newArray(int size) {
			return new GlobPatternList[size];
		}
	};
	
	public void writeToParcel(Parcel dest, int flags) {
		int size = this.size();
		dest.writeInt(size);
		for (int i=0; i<size; i++) {
			GlobPattern p = this.get(i);
			dest.writeString(p.getExpression());
		}
	}
}
