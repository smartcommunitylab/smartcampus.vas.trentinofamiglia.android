package eu.trentorise.smartcampus.trentinofamiglia.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private static final String HEADER_TAG = "isanheader";

	private ArrayList<Integer> positions;

	/**
	 * 
	 * @param context
	 *            the activity
	 * @param labels
	 *            all the elements that goes into the drawer
	 * @param positions
	 *            of the element to render as an header
	 */
	public NavDrawerAdapter(Context context, List<String> labels,
			Integer[] positions) {
		super(context, R.layout.drawer_element_row, labels);
		this.positions = new ArrayList<Integer>();
		Collections.addAll(this.positions, positions);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View out = convertView;

		out = createView(out, position, parent);

		Resources res = getContext().getResources();
		int imagid = -1;
		if (out.getTag()==null || !out.getTag().equals(HEADER_TAG)) {
			imagid = getIconId(position, res);
		}

		setText(position, out, res, imagid);
		
		return out;
	}

	private void setText(int position, View out, Resources res, int imagid) {
		TextView tv = (TextView) out.findViewById(R.id.drawer_list_textview);
		if (tv != null) {
			if (imagid != -1) {
				tv.setCompoundDrawablesWithIntrinsicBounds(
						res.getDrawable(imagid), null, null, null);
			}
			tv.setText(getItem(position));
		}
	}

	private int getIconId(int position, Resources res) {
		TypedArray icons;
		if (position <= positions.get(0)) {
			icons = res.obtainTypedArray(R.array.drawer_items_events_icons);
		} else {
			icons = res.obtainTypedArray(R.array.drawer_items_places_icons);
		}
		int imagid = icons.getResourceId(position, -1);
		icons.recycle();
		return imagid;
	}

	private View createView(View out, int position, ViewGroup parent) {
		if (out == null) {
			// check type of the item
			if (!positions.contains(position)) {
				// it's a normal element
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				out = inflater.inflate(R.layout.drawer_element_row, parent, false);
			} else {
				// it's an header
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				out = inflater.inflate(R.layout.drawer_header_row, parent, false);
				out.setTag(HEADER_TAG);
			}
		}
		return out;
	}
}
