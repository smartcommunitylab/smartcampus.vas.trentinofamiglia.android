package eu.trentorise.smartcampus.trentinofamiglia.map;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MapFragment extends Fragment {

	private static final String TAG_FRAGMENT_POI_SELECT = "poi_select";
	public static final String ARG_POI_CATEGORY = "poi category";
	public static final String ARG_EVENT_CATEGORY = "event category";
	public static final String ARG_OBJECTS = "objects";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View out = inflater.inflate(R.layout.fragment_map, container, false);
		setHasOptionsMenu(true);
		return out;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_poi) {
			PoiSelectFragment psf = new PoiSelectFragment();
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		// this is needed because the activity manage the navigation drawer
		return getActivity().onOptionsItemSelected(item);
	}

}
