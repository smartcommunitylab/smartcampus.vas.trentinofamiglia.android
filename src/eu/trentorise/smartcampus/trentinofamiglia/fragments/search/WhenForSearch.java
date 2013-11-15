package eu.trentorise.smartcampus.trentinofamiglia.fragments.search;

import android.os.Parcel;
import android.os.Parcelable;

public class WhenForSearch implements Parcelable{
	
	private String description;
	private long from;
	private long to;

	
	public WhenForSearch(String description, long from, long to) {
		this.description=description;
		this.from = from;
		this.to = to;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}


	public long getFrom() {
		return from;
	}
	public void setFrom(long from) {
		this.from = from;
	}
	public long getTo() {
		return to;
	}
	public void setTo(long to) {
		this.to = to;
	}
	@Override
	public String toString() {
		return description;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}
}
