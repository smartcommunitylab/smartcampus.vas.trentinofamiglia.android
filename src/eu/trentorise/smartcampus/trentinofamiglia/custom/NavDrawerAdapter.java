package eu.trentorise.smartcampus.trentinofamiglia.custom;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.model.DrawerItem;

public class NavDrawerAdapter extends ArrayAdapter<DrawerItem> {

	public NavDrawerAdapter(Context context, List<DrawerItem> objects) {
		super(context, R.layout.drawer_element_row, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerItem item = getItem(position);
		View out=createView(getContext(), convertView, parent,item);
		TextView tv = (TextView) out.findViewById(R.id.drawer_list_textview);
		if(item.icon!=null)
			tv.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
		tv.setText(item.text);
		return out;
	}

	private View createView(Context ctx, View out, ViewGroup parent,
			DrawerItem item) {
		// check type of the item
		if (!item.header) {
			// it's a normal element
			LayoutInflater inflater = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			out = inflater.inflate(R.layout.drawer_element_row, parent, false);
		} else {
			// it's an header
			LayoutInflater inflater = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			out = inflater.inflate(R.layout.drawer_header_row, parent, false);
			out.setEnabled(false);
			out.setClickable(false);
		}
		return out;
	}

}
