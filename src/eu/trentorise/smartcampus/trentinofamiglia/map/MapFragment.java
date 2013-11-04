package eu.trentorise.smartcampus.trentinofamiglia.map;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MapFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View out = inflater.inflate(R.layout.fragment_map, container,false);
		return out;
	}
	
}
