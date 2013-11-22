package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DTParamsHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventDetailsFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoiDetailsFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoisListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;

public class MapFragment extends Fragment 	implements MapItemsHandler, OnCameraChangeListener, OnMarkerClickListener,
MapObjectContainer {


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_poi_places) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.map_items_places_labels,
					R.array.map_items_places_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		else if (item.getItemId() == R.id.action_poi_events) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.map_items_events_labels,
					R.array.map_items_events_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		else if (item.getItemId() == R.id.action_poi_freetime) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.map_items_freetime_labels,
					R.array.map_items_freetime_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		else if (item.getItemId() == R.id.action_poi_babies) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(
					R.array.map_items_babies_labels,
					R.array.map_items_babies_icons);
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		}
		// this is needed because the activity manage the navigation drawer
		return getActivity().onOptionsItemSelected(item);
	}


private static final String TAG_FRAGMENT_POI_SELECT = "poi_select";
public static final String ARG_POI_CATEGORY = "poi category";
public static final String ARG_EVENT_CATEGORY = "event category";
public static final String ARG_OBJECTS = "objects";
public static final String ARG_TRACK_CATEGORY = "track_category";
protected GoogleMap mMap;

private String[] poiCategories = null;
private String[] eventsCategories = null;
private String[] eventsNotTodayCategories = null;
private Collection<? extends BaseDTObject> objects;

private boolean loaded = false;

private static View view;

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	inflater.inflate(R.menu.map_menu, menu);
	super.onCreateOptionsMenu(menu, inflater);
}

@Override
public void onStart() {
	super.onStart();
	// hide keyboard if it is still open
	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.frame_content).getWindowToken(), 0);

	if (!loaded) {
		String key = getString(R.string.view_intent_arg_object_id);
		if (getActivity().getIntent() != null && getActivity().getIntent().hasExtra(key)) {
			new SCAsyncTask<Void, Void, BaseDTObject>(getActivity(), new LoadDataProcessor(getActivity()))
					.execute();
			eventsCategories = null;
			poiCategories = null;
		} else {
			initView();
		}
		loaded = true;
	}
}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putBoolean("loaded", loaded);
}

@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	CategoryDescriptor[] eventsDefault = DTParamsHelper
			.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_EVENTS);
	if (eventsDefault != null) {
		List<String> eventCategory = new ArrayList<String>();
		for (CategoryDescriptor event : eventsDefault)
			eventCategory.add(event.category);
		eventsCategories = Arrays.asList(eventCategory.toArray()).toArray(
				new String[eventCategory.toArray().length]);
	}
	CategoryDescriptor[] poisDefault = DTParamsHelper.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_POIS);
	if (poisDefault != null) {
		List<String> poisCategory = new ArrayList<String>();
		for (CategoryDescriptor poi : poisDefault)
			poisCategory.add(poi.category);
		poiCategories = Arrays.asList(poisCategory.toArray()).toArray(new String[poisCategory.toArray().length]);

	}

	setHasOptionsMenu(true);
}

/**
 * @return
 */
@SuppressWarnings("unchecked")
protected void initView() {
	if (getSupportMap() != null) {
		getSupportMap().clear();
		getSupportMap().getUiSettings().setRotateGesturesEnabled(false);
		getSupportMap().getUiSettings().setTiltGesturesEnabled(false);
	}

	if (getArguments() != null && getArguments().containsKey(ARG_OBJECTS)) {
		poiCategories = null;
		eventsCategories = null;
		List<BaseDTObject> list = (List<BaseDTObject>) getArguments().getSerializable(ARG_OBJECTS);
		new AsyncTask<List<BaseDTObject>, Void, List<BaseDTObject>>() {
			@Override
			protected List<BaseDTObject> doInBackground(List<BaseDTObject>... params) {
				return params[0];
			}

			@Override
			protected void onPostExecute(List<BaseDTObject> result) {
				addObjects(result);
			}
		}.execute(list);
	} else if (getArguments() != null && getArguments().containsKey(ARG_POI_CATEGORY)) {
		eventsCategories = null;
		setPOICategoriesToLoad(getArguments().getString(ARG_POI_CATEGORY));
	} else if (getArguments() != null && getArguments().containsKey(ARG_EVENT_CATEGORY)) {
		poiCategories = null;
		setEventCategoriesToLoad(getArguments().getString(ARG_EVENT_CATEGORY));
	} else {
		if (poiCategories != null) {
			setPOICategoriesToLoad(poiCategories);
		}
		if (eventsCategories != null) {
			setEventCategoriesToLoad(eventsCategories);
		}
	}
}

@Override
public void onResume() {
	super.onResume();
	if (getSupportMap() != null) {
		getSupportMap().setMyLocationEnabled(true);
		getSupportMap().setOnCameraChangeListener(this);
		getSupportMap().setOnMarkerClickListener(this);
		// if (objects != null) {
		// render(objects);
		// }
	}
}

@Override
public void onConfigurationChanged(Configuration arg0) {
	super.onConfigurationChanged(arg0);
}

@Override
public void onPause() {
	super.onPause();
	if (getSupportMap() != null) {
		getSupportMap().setMyLocationEnabled(false);
		getSupportMap().setOnCameraChangeListener(null);
		getSupportMap().setOnMarkerClickListener(null);
	}
}

@Override
public void onPrepareOptionsMenu(Menu menu) {
	menu.clear();
	// MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
	// R.id.menu_item_show_places_layers, 1,
	// R.string.menu_item__places_layers_text);
	// item.setIcon(R.drawable.ic_menu_pois);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	// item = menu.add(Menu.CATEGORY_SYSTEM,
	// R.id.menu_item_show_events_layers, 1,
	// R.string.menu_item__events_layers_text);
	// item.setIcon(R.drawable.ic_menu_events);
	// item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	super.onPrepareOptionsMenu(menu);
}


public void setPOICategoriesToLoad(final String... categories) {
	this.poiCategories = categories;
	/* actually only event or poi at the same time */
	this.eventsCategories = null;

	new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
			getActivity(), this, getSupportMap()) {
		@Override
		protected Collection<? extends BaseDTObject> getObjects() {
			try {
				/* check if todays is checked and cat with searchTodayEvents */
				return DTHelper.getPOIByCategory(0, -1, categories);
			} catch (Exception e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}

	}).execute();
}

private void onBaseDTObjectTap(BaseDTObject o) {
	Bundle args = new Bundle();
	args.putSerializable(InfoDialog.PARAM, o);
	InfoDialog dtoTap = new InfoDialog();
	dtoTap.setArguments(args);
	dtoTap.show(getActivity().getSupportFragmentManager(), "me");
}

private void onBaseDTObjectsTap(List<BaseDTObject> list) {
	if (list == null || list.size() == 0)
		return;
	if (list.size() == 1) {
		onBaseDTObjectTap(list.get(0));
		return;
	}
	FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
	Fragment fragment = null;
	Bundle args = new Bundle();
	if (list.get(0) instanceof LocalEventObject) {
		fragment = new EventsListingFragment();
		args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
	} else if (list.get(0) instanceof POIObject) {
		fragment = new PoisListingFragment();
		args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
	}
	if (fragment != null) {
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// fragmentTransaction.detach(this);
		fragmentTransaction.replace(R.id.frame_content, fragment, "me");
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();
	}
}

@Override
public void setEventCategoriesToLoad(final String... categories) {
	this.eventsCategories = categories;
	this.eventsNotTodayCategories = categories;

	/* actually only event or poi at the same time */
	this.poiCategories = null;

	// mItemizedoverlay.clearMarkers();

	new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(getActivity(), new MapLoadProcessor(
			getActivity(), this, getSupportMap()) {
		@Override
		protected Collection<? extends BaseDTObject> getObjects() {
			try {
				/* check if todays is checked and cat with searchTodayEvents */

				if (isTodayIncluded()) {
					List<LocalEventObject> newList = new ArrayList<LocalEventObject>();
					newList.addAll(DTHelper.searchTodayEvents(0, -1, ""));
					if (categories != null)
						newList.addAll(DTHelper.getEventsByCategories(0, -1, eventsNotTodayCategories));
					return newList;
				} else
					return DTHelper.getEventsByCategories(0, -1, categories);
			} catch (Exception e) {
				e.printStackTrace();
				return Collections.emptyList();
			}
		}

	}).execute();
}

private boolean isTodayIncluded() {
	List<String> categoriesNotToday = new ArrayList<String>();
	boolean istodayincluded = false;
	if (eventsCategories.length > 0)
		for (int i = 0; i < eventsCategories.length; i++) {
			if (eventsCategories[i].contains("Today")) {

				istodayincluded = true;
			} else
				categoriesNotToday.add(eventsCategories[i]);

		}
	eventsNotTodayCategories = categoriesNotToday.toArray(new String[categoriesNotToday.size()]);
	return istodayincluded;
}

private GoogleMap getSupportMap() {
	if (mMap == null) {
		if (getFragmentManager().findFragmentById(R.id.map) != null
				&& getFragmentManager().findFragmentById(R.id.map) instanceof SupportMapFragment)
			mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		if (mMap != null)
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MapManager.DEFAULT_POINT, MapManager.ZOOM_DEFAULT));

	}
	return mMap;
}

@Override
public boolean onMarkerClick(Marker marker) {
	List<BaseDTObject> list = MapManager.ClusteringHelper.getFromGridId(marker.getTitle());
	if (list == null || list.isEmpty())
		return true;

	if (list.size() == 1) {
		onBaseDTObjectTap(list.get(0));
	} else if (getSupportMap().getCameraPosition().zoom == getSupportMap().getMaxZoomLevel()) {
		onBaseDTObjectsTap(list);
	} else {
		MapManager.fitMapWithOverlays(list, getSupportMap());
	}
	return true;
}

@Override
public void onCameraChange(CameraPosition position) {
	render(objects);
}

@Override
public <T extends BaseDTObject> void addObjects(Collection<? extends BaseDTObject> objects) {
	if (getSupportMap() != null) {
		this.objects = objects;
		render(objects);
		MapManager.fitMapWithOverlays(objects, getSupportMap());
	}
}

private void render(Collection<? extends BaseDTObject> objects) {
	if (getSupportMap() != null) {
		getSupportMap().clear();
		if (objects != null && getActivity() != null) {
			List<MarkerOptions> cluster = MapManager.ClusteringHelper.cluster(
					getActivity().getApplicationContext(), getSupportMap(), objects);
			MapManager.ClusteringHelper.render(getSupportMap(), cluster);
		}
	}

}

private class LoadDataProcessor extends AbstractAsyncTaskProcessor<Void, BaseDTObject> {

	public LoadDataProcessor(Activity activity) {
		super(activity);
	}

	@Override
	public BaseDTObject performAction(Void... params) throws SecurityException, Exception {
		String entityId = getActivity().getIntent().getStringExtra(getString(R.string.view_intent_arg_object_id));
		String type = getActivity().getIntent().getStringExtra(getString(R.string.view_intent_arg_entity_type));

		if (entityId != null && type != null) {
			if ("event".equals(type))
				return DTHelper.findEventByEntityId(entityId).getObjectForBean();
			else if ("location".equals(type))
				return DTHelper.findPOIByEntityId(entityId).getObjectForBean();
			// else if ("narrative".equals(type))
			// return
			// DTHelper.findStoryByEntityId(entityId).getObjectForBean();
		}
		return null;
	}

	@Override
	public void handleResult(BaseDTObject result) {

		String key = getString(R.string.view_intent_arg_object_id);
		String entityId = getActivity().getIntent().getStringExtra(key);
		getActivity().getIntent().removeExtra(key);

		if (entityId != null) {
			if (result == null) {
				Toast.makeText(getActivity(), R.string.app_failure_obj_not_found, Toast.LENGTH_LONG).show();
				return;
			}

			Fragment fragment = null;
			Bundle args = new Bundle();
			if (result instanceof POIObject) {
				fragment = new PoiDetailsFragment();
				args.putString(PoiDetailsFragment.ARG_POI_ID, result.getId());
			} else if (result instanceof EventObject) {
				fragment = new EventDetailsFragment();
				args.putString(EventDetailsFragment.ARG_EVENT_ID, (result.getId()));
			}
			// else if (result instanceof StoryObject) {
			// fragment = new StoryDetailsFragment();
			// args.putString(StoryDetailsFragment.ARG_STORY_ID,
			// result.getId());
			// }
			if (fragment != null) {
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
						.beginTransaction();
				fragment.setArguments(args);

				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(R.id.frame_content, fragment, "me");
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		}
	}

}
}
