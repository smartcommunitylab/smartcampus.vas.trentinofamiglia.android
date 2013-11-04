package eu.trentorise.smartcampus.trentinofamiglia.custom;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NavDrawerAdapter extends ArrayAdapter<String> {

	public NavDrawerAdapter(Context context, String[] labels) {
		super(context, R.layout.drawer_row, labels);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View out = convertView;
		if (out == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			out = inflater.inflate(R.layout.drawer_row, parent, false);
		}
		Resources res = getContext().getResources();
		TypedArray icons = res.obtainTypedArray(R.array.drawer_items_icons);
		int imagid = icons.getResourceId(position, -1);
		icons.recycle();
		TextView tv = (TextView) out.findViewById(R.id.drawer_list_textview);
		if (tv != null) {
			if (imagid != -1) {
				tv.setCompoundDrawablesWithIntrinsicBounds(
						res.getDrawable(imagid), null, null, null);
			}
			tv.setText(getItem(position));
		}
		return out;
	}

}
