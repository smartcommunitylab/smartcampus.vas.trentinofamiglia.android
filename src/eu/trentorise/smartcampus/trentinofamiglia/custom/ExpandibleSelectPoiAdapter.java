package eu.trentorise.smartcampus.trentinofamiglia.custom;

import java.util.List;
import java.util.Map;

import eu.trentorise.smartcampus.trentinofamiglia.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandibleSelectPoiAdapter extends BaseExpandableListAdapter {

	private Activity context;
	private Map<String, List<String>> elements;
	private List<String> headers;

	public ExpandibleSelectPoiAdapter(Activity context, List<String> headers,
			Map<String, List<String>> elements) {
		this.context = context;
		this.elements = elements;
		this.headers = headers;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return elements.get(headers.get(groupPosition)).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final String childText = (String) getChild(groupPosition, childPosition);
		LayoutInflater inflater = context.getLayoutInflater();

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.select_poi_list_element_row, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.select_poi_checkTv);
		tv.setText(childText);
		tv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckedTextView ctv = (CheckedTextView)v;
				ctv.setChecked(!ctv.isChecked());
			}
		});
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		return elements.get(headers.get(groupPosition)).size();
	}

	public Object getGroup(int groupPosition) {
		return headers.get(groupPosition);
	}

	public int getGroupCount() {
		return headers.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerString = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.select_poi_list_element_header,null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.select_poi_Tv);
		tv.setText(headerString);
		return convertView;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}