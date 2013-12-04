package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
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
import eu.trentorise.smartcampus.trentinofamiglia.MainActivity;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DTParamsHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventDetailsFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoiDetailsFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoisListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.track.TrackListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.PoiSelectFragment.REQUEST_TYPE;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller.AppItem;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller.AppTask;

public class MapFragment extends Fragment implements MapItemsHandler,
		OnCameraChangeListener, OnMarkerClickListener, MapObjectContainer {

	private static final String TAG_FRAGMENT_POI_SELECT = "poi_select";
	public static final String ARG_POI_CATEGORY = "poi category";
	public static final String ARG_EVENT_CATEGORY = "event category";
	public static final String ARG_OBJECTS = "objects";
	public static final String ARG_TRACK_CATEGORY = "track_category";
	protected GoogleMap mMap;
	private AppItem launcher;
	private AppTask mAppTask;
	private ApkDownloaderTask mDownloaderTask;
	private String[] poiCategories = null;
	private String[] eventsCategories = null;
	private String[] tracksCategories = null;
	private String[] eventsNotTodayCategories = null;
	private Collection<? extends BaseDTObject> objects;

	private boolean loaded = false;
	private boolean listmenu = false;
	private ApkInstaller apkI= new ApkInstaller();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_poi_places) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(this,
					R.array.map_items_places_labels,
					R.array.map_items_places_icons, REQUEST_TYPE.POI,
					new String[] { CategoryHelper.POI_CATEGORIES[4].category,
							CategoryHelper.POI_CATEGORIES[0].category });
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		} else if (item.getItemId() == R.id.action_poi_events) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(this,
					R.array.map_items_events_labels,
					R.array.map_items_events_icons, REQUEST_TYPE.EVENT,
					CategoryHelper.getEventCategories());
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		} else if (item.getItemId() == R.id.action_poi_freetime) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(this,
					R.array.map_items_freetime_labels,
					R.array.map_items_freetime_icons, new String[] {
							CategoryHelper.TRACK_CATEGORIES[0].category,
							CategoryHelper.TRACK_CATEGORIES[1].category,
							CategoryHelper.POI_CATEGORIES[1].category });
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		} else if (item.getItemId() == R.id.action_poi_babies) {
			PoiSelectFragment psf = PoiSelectFragment.istantiate(this,
					R.array.map_items_babies_labels,
					R.array.map_items_babies_icons, REQUEST_TYPE.POI,
					new String[] { CategoryHelper.POI_CATEGORIES[3].category });
			psf.show(getFragmentManager(), TAG_FRAGMENT_POI_SELECT);
			return true;
		} else if (item.getItemId() == R.id.action_list) {
			if (getArguments().containsKey(ARG_EVENT_CATEGORY)
					|| getArguments().containsKey(ARG_TRACK_CATEGORY)
					|| getArguments().containsKey(ARG_POI_CATEGORY)) {
				switchToList();
			}
			return true;
		} else if (item.getItemId() == R.id.to_be_update){
			ApkInstaller.update_launcher(ApkInstaller.APP_URL,ApkInstaller.APP_NAME);
			return true;
		}
		// this is needed because the activity manage the navigation drawer
		return getActivity().onOptionsItemSelected(item);
	}

	private void switchToList() {
		FragmentTransaction ft = getActivity().getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.enter, R.anim.exit);

		String cat = null;
		if (getArguments().containsKey(ARG_EVENT_CATEGORY)) {
			cat = getArguments().getString(ARG_EVENT_CATEGORY);
			Bundle args = new Bundle();
			EventsListingFragment elf = new EventsListingFragment();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			elf.setArguments(args);
			ft.replace(R.id.frame_content, elf,
					MainActivity.TAG_FRAGMENT_EVENT_LIST);
			ft.addToBackStack(MainActivity.TAG_FRAGMENT_EVENT_LIST);
		} else if (getArguments().containsKey(ARG_TRACK_CATEGORY)) {
			cat = getArguments().getString(ARG_TRACK_CATEGORY);
			Bundle args = new Bundle();
			TrackListingFragment elf = new TrackListingFragment();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			elf.setArguments(args);
			ft.replace(R.id.frame_content, elf,
					MainActivity.TAG_FRAGMENT_TRACK_LIST);
			ft.addToBackStack(MainActivity.TAG_FRAGMENT_TRACK_LIST);
		} else if (getArguments().containsKey(ARG_POI_CATEGORY)) {
			cat = getArguments().getString(ARG_POI_CATEGORY);
			Bundle args = new Bundle();
			PoisListingFragment elf = new PoisListingFragment();
			args.putString(SearchFragment.ARG_CATEGORY, cat);
			elf.setArguments(args);
			ft.replace(R.id.frame_content, elf,
					MainActivity.TAG_FRAGMENT_POI_LIST);
			ft.addToBackStack(MainActivity.TAG_FRAGMENT_POI_LIST);
		}

		ft.commit();
	}
	
	private void startNewAppTask() {
		// Stopping task
		stopAnyActiveAppTask();
		// Starting new one
		mAppTask = apkI.new AppTask(getActivity());
		mAppTask.execute();
	}
	
	private void stopAnyActiveAppTask() {
		if (mAppTask != null && !mAppTask.isCancelled()) {
			mAppTask.cancel(true);
		}
	}
	@Override
	public void onStart() {
		super.onStart();
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				getActivity().findViewById(R.id.frame_content).getWindowToken(),
				0);
		startNewAppTask();
		if (!loaded) {
			String key = getString(R.string.view_intent_arg_object_id);
			if (getActivity().getIntent() != null
					&& getActivity().getIntent().hasExtra(key)) {
				new SCAsyncTask<Void, Void, BaseDTObject>(getActivity(),
						new LoadDataProcessor(getActivity())).execute();
				eventsCategories = null;
				poiCategories = null;
			} else {
				initView();
			}
			loaded = true;
		}
		else {
			initView();
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
		CategoryDescriptor[] poisDefault = DTParamsHelper
				.getDefaultArrayByParams(CategoryHelper.CATEGORY_TYPE_POIS);
		if (poisDefault != null) {
			List<String> poisCategory = new ArrayList<String>();
			for (CategoryDescriptor poi : poisDefault)
				poisCategory.add(poi.category);
			poiCategories = Arrays.asList(poisCategory.toArray()).toArray(
					new String[poisCategory.toArray().length]);

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
			drawTracks((List<BaseDTObject>) getArguments().getSerializable(
					ARG_OBJECTS));
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_POI_CATEGORY)) {
			listmenu = true;
			eventsCategories = null;
			setPOICategoriesToLoad(getArguments().getString(ARG_POI_CATEGORY));
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_EVENT_CATEGORY)) {
			listmenu = true;
			poiCategories = null;
			setEventCategoriesToLoad(getArguments().getString(
					ARG_EVENT_CATEGORY));
		} else if (getArguments() != null
				&& getArguments().containsKey(ARG_TRACK_CATEGORY)) {
			listmenu = true;
			tracksCategories = null;
			setMiscellaneousCategoriesToLoad(getArguments().getString(ARG_TRACK_CATEGORY));
		} else {
			if (poiCategories != null) {
				setPOICategoriesToLoad(poiCategories);
			}
			if (eventsCategories != null) {
				setEventCategoriesToLoad(eventsCategories);
			}
		}
	}

	// Mi dava fastidio il giallo u.u
	@SuppressWarnings("unchecked")
	private void drawTracks(List<? extends BaseDTObject> list) {

		new AsyncTask<List<? extends BaseDTObject>, Void, List<? extends BaseDTObject>>() {
			@Override
			protected List<? extends BaseDTObject> doInBackground(
					List<? extends BaseDTObject>... params) {
				return params[0];
			}

			@Override
			protected void onPostExecute(List<? extends BaseDTObject> result) {
				addObjects(result);
			}
		}.execute(list);
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
		SharedPreferences settings = getActivity().getSharedPreferences(ApkInstaller.PREFS_NAME, 0);
		getActivity().getMenuInflater().inflate(R.menu.map_menu, menu);

		MenuItem item = menu.getItem(0).setVisible(settings.getBoolean("to_be_updated", false));


		if (listmenu) {
			menu.getItem(1).setVisible(false);
		} else {
			menu.getItem(2).setVisible(false);
		}
		super.onPrepareOptionsMenu(menu);
	}

	public void setPOICategoriesToLoad(final String... categories) {
		this.poiCategories = categories;
		/* actually only event or poi at the same time */
		this.eventsCategories = null;

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapLoadProcessor(getActivity(), this,
						getSupportMap()) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							/*
							 * check if todays is checked and cat with
							 * searchTodayEvents
							 */
							Collection<POIObject> list = DTHelper
									.getPOIByCategory(0, -1, categories);
							Iterator<POIObject> i = list.iterator();
							while (i.hasNext()) {
								POIObject obj = i.next();
								obj.getLocation();
								if (obj.getLocation()[0] == 0
										&& obj.getLocation()[1] == 0)
									i.remove();
							}
							return list;

						} catch (Exception e) {
							e.printStackTrace();
							return Collections.emptyList();
						}
					}

				}).execute();
	}

	public void setMiscellaneousListToLoad(final List<String> trackCategories,
			List<String> poiCategories, List<String> eventCategories) {

		final String[] pcat = poiCategories.toArray(new String[poiCategories
				.size()]);
		this.poiCategories = pcat;
		final String[] ecat = eventCategories
				.toArray(new String[eventCategories.size()]);
		this.eventsCategories = ecat;

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapLoadProcessor(getActivity(), this,
						getSupportMap()) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							/*
							 * check if todays is checked and cat with
							 * searchTodayEvents
							 */
							Collection<BaseDTObject> list = new ArrayList<BaseDTObject>();
							if(pcat.length>0)
								list.addAll(DTHelper.getPOIByCategory(0, -1, pcat));
							if(ecat.length>0)
								list.addAll(DTHelper.getEventsByCategories(0, -1, ecat));
							SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
							sort.put("title", 1);
							
							if(trackCategories.size()>0){
								Collection<TrackObjectForBean> result = DTHelper.searchInGeneral(
										0, -1, null, null, null, false,
										TrackObjectForBean.class, sort,
										trackCategories
												.toArray(new String[trackCategories
														.size()]));
	
								for (TrackObjectForBean trackBean : result) {
									list.add(trackBean.getObjectForBean());
								}
							}

							return list;

						} catch (Exception e) {
							e.printStackTrace();
							return Collections.emptyList();
						}
					}

				}).execute();
	}

	@Override
	public void setMiscellaneousCategoriesToLoad(String... categories) {
		List<String> tracks = new ArrayList<String>();
		List<String> events = new ArrayList<String>();
		List<String> pois = new ArrayList<String>();
		for (String s : categories) {
			if (Arrays.asList(CategoryHelper.getTrackCategories()).contains(s)) {
				tracks.add(s);

			} else if (Arrays.asList(CategoryHelper.getEventCategories())
					.contains(s)) {
				events.add(s);
			} else if (Arrays.asList(CategoryHelper.getPOICategories())
					.contains(s)) {
				pois.add(s);
			} else
				Log.e(this.getClass().getName(), "category not found: " + s);
		}

		setMiscellaneousListToLoad(tracks, pois, events);
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
		FragmentTransaction fragmentTransaction = getFragmentManager()
				.beginTransaction();
		Fragment fragment = null;
		Bundle args = new Bundle();
		if (list.get(0) instanceof LocalEventObject) {
			fragment = new EventsListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		} else if (list.get(0) instanceof POIObject) {
			fragment = new PoisListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		} else if (list.get(0) instanceof TrackObject) {
			fragment = new TrackListingFragment();
			args.putSerializable(SearchFragment.ARG_LIST, new ArrayList(list));
		}
		if (fragment != null) {
			fragment.setArguments(args);
			fragmentTransaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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

		getSupportMap().clear();

		new SCAsyncTask<Void, Void, Collection<? extends BaseDTObject>>(
				getActivity(), new MapLoadProcessor(getActivity(), this,
						getSupportMap()) {
					@Override
					protected Collection<? extends BaseDTObject> getObjects() {
						try {
							/*
							 * check if todays is checked and cat with
							 * searchTodayEvents
							 */
							Collection<LocalEventObject> newList;
							if (isTodayIncluded()) {
								newList = new ArrayList<LocalEventObject>();
								newList.addAll(DTHelper.searchTodayEvents(0,
										-1, ""));
								if (categories != null)
									newList.addAll(DTHelper
											.getEventsByCategories(0, -1,
													eventsNotTodayCategories));

							} else
								newList = DTHelper.getEventsByCategories(0, -1,
										categories);
							Iterator<LocalEventObject> i = newList.iterator();
							while (i.hasNext()) {
								LocalEventObject obj = i.next();
								obj.getLocation();
								if (obj.getLocation()[0] == 0
										&& obj.getLocation()[1] == 0)
									i.remove();
							}
							return newList;
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
		eventsNotTodayCategories = categoriesNotToday
				.toArray(new String[categoriesNotToday.size()]);
		return istodayincluded;
	}

	private GoogleMap getSupportMap() {
		if (mMap == null) {
			if (getFragmentManager().findFragmentById(R.id.map) != null
					&& getFragmentManager().findFragmentById(R.id.map) instanceof SupportMapFragment)
				mMap = ((SupportMapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();
			if (mMap != null)
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
						MapManager.DEFAULT_POINT, MapManager.ZOOM_DEFAULT));

		}
		return mMap;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		List<BaseDTObject> list = MapManager.ClusteringHelper
				.getFromGridId(marker.getTitle());
		if (list == null || list.isEmpty())
			return true;

		if (list.size() == 1) {
			onBaseDTObjectTap(list.get(0));
		} else if (getSupportMap().getCameraPosition().zoom >= getSupportMap().getMaxZoomLevel()) {
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
	public <T extends BaseDTObject> void addObjects(
			Collection<? extends BaseDTObject> objects) {
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
				List<MarkerOptions> cluster = MapManager.ClusteringHelper
						.cluster(getActivity().getApplicationContext(),
								getSupportMap(), objects);
				MapManager.ClusteringHelper.render(getActivity(),
						getSupportMap(), cluster, objects);
			}
		}

	}

	private class LoadDataProcessor extends
			AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params)
				throws SecurityException, Exception {
			String entityId = getActivity().getIntent().getStringExtra(
					getString(R.string.view_intent_arg_object_id));
			String type = getActivity().getIntent().getStringExtra(
					getString(R.string.view_intent_arg_entity_type));

			if (entityId != null && type != null) {
				if ("event".equals(type))
					return DTHelper.findEventByEntityId(entityId)
							.getObjectForBean();
				else if ("location".equals(type))
					return DTHelper.findPOIByEntityId(entityId)
							.getObjectForBean();

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
					Toast.makeText(getActivity(),
							R.string.app_failure_obj_not_found,
							Toast.LENGTH_LONG).show();
					return;
				}

				Fragment fragment = null;
				Bundle args = new Bundle();
				if (result instanceof POIObject) {
					fragment = new PoiDetailsFragment();
					args.putString(PoiDetailsFragment.ARG_POI_ID,
							result.getId());
				} else if (result instanceof EventObject) {
					fragment = new EventDetailsFragment();
					args.putString(EventDetailsFragment.ARG_EVENT_ID,
							(result.getId()));
				}

				if (fragment != null) {
					FragmentTransaction fragmentTransaction = getActivity()
							.getSupportFragmentManager().beginTransaction();
					fragment.setArguments(args);

					fragmentTransaction
							.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(R.id.frame_content, fragment,
							"me");
					fragmentTransaction.addToBackStack(fragment.getTag());
					fragmentTransaction.commit();
				}
			}
		}

	}

}
