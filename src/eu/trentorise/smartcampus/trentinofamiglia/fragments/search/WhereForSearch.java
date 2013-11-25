package eu.trentorise.smartcampus.trentinofamiglia.fragments.search;

import android.os.Parcel;
import android.os.Parcelable;



public class WhereForSearch implements Parcelable {
	
	String description;
	Double radius;
	
	public WhereForSearch(String description, double radius) {
		this.description = description;
	this.radius = radius;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Double getFilter() {
		return radius;
	}
	public void setFilter(Double filter) {
		this.radius = filter;
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
