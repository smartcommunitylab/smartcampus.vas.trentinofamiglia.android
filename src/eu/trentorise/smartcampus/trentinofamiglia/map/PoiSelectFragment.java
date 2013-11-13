package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.ExpandibleSelectPoiAdapter;
import eu.trentorise.smartcampus.trentinofamiglia.model.DrawerItem;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

public class PoiSelectFragment extends DialogFragment {

	private List<String> mHeaders;
	private Map<String, List<String>> mItems;
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaders=new ArrayList<String>();
		mItems=new HashMap<String, List<String>>();
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle("Something");
		View v = inflater.inflate(R.layout.fragment_select_poi, container,
				false);
		ExpandableListView elv = (ExpandableListView) v
				.findViewById(R.id.select_poi_exp_listview);
		buildItems();
		elv.setAdapter(new ExpandibleSelectPoiAdapter(getActivity(), mHeaders,
				mItems));
		return v;
	}

	private void buildItems() {
		int[] ids = { R.array.drawer_items_events_labels,
				R.array.drawer_items_places_labels,
				R.array.drawer_items_organizations_labels };
		for (int i = 0; i < ids.length; i++) {
			String[] labels = getResources().getStringArray(ids[i]);
			mHeaders.add(labels[0]);
			ArrayList<String> tmpElems = new ArrayList<String>();
			for (int j = 1; j < labels.length; j++) {
				tmpElems.add(labels[j]);
			}
			mItems.put(labels[0],tmpElems);
		}
	}
}
