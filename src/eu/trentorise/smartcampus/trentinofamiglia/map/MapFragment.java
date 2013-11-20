package eu.trentorise.smartcampus.trentinofamiglia.map;

import eu.trentorise.smartcampus.trentinofamiglia.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
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

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View out = inflater.inflate(R.layout.fragment_map, container, false);
	// setHasOptionsMenu(true);
	// return out;
	// }
	private static View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_map, container, false);
		} catch (InflateException e) {
			/* map is already there, just return view as it is */
		}
		return view;
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
		if (item.getItemId() == R.id.action_poi_places) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.drawer_items_places_labels,
					R.array.drawer_items_places_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		else if (item.getItemId() == R.id.action_poi_organizations) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.drawer_items_organizations_labels,
					R.array.drawer_items_organizations_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		// this is needed because the activity manage the navigation drawer
		return getActivity().onOptionsItemSelected(item);
	}

}
