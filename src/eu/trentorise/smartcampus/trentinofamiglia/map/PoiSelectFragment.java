package eu.trentorise.smartcampus.trentinofamiglia.map;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;

public class PoiSelectFragment extends DialogFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//FIXME
		getDialog().setTitle("Something");
		View v = inflater.inflate(R.layout.fragment_select_poi, container,false);
		ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.select_poi_exp_listview);
		return v;
	}
	
}
