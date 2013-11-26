/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.trentinofamiglia.fragments.track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.ViewHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class TrackListingFragment extends AbstractLstingFragment<TrackObject> implements TagProvider {

	private ListView list;
	private Context context;
	private String category;
	private TrackAdapter trackAdapter;
	public static final String ARG_ID = "id_track";
	public static final String ARG_INDEX = "index_adapter";
	private String idTrack = "";
	private Integer indexAdapter;
	private Boolean reload = false;
	private Integer postitionSelected = 0;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_ID, idTrack);
		if (indexAdapter != null)
			outState.putInt(ARG_INDEX, indexAdapter);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getActivity();
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		list = (ListView) getActivity().findViewById(R.id.pois_list);

		if (arg0 != null) {
			// Restore last state for checked position.
			idTrack = arg0.getString(ARG_ID);
			indexAdapter = arg0.getInt(ARG_INDEX);

		}
		if (trackAdapter == null) {
			trackAdapter = new TrackAdapter(context, R.layout.pois_row);
		}
		setAdapter(trackAdapter);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.poislist, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!idTrack.equals("")) {
			// get info of the track
			TrackObject track = DTHelper.findTrackById(idTrack);

			if (track == null) {
				// cancellazione
				removeTrack(trackAdapter, indexAdapter);

			} else {
				if (track.getUpdateTime() == 0) {
					removeTrack(trackAdapter, indexAdapter);
					insertTrack(track);
				}
			}
			// notify
			trackAdapter.notifyDataSetChanged();
			idTrack = "";
			indexAdapter = 0;
		}
	}

	/*
	 * insert in the same adapter the new item
	 */
	private void insertTrack(TrackObject poi) {

		// add in the right place
		int i = 0;
		boolean insert = false;
		while (i < trackAdapter.getCount()) {
			if (trackAdapter.getItem(i).getTitle() != null) {
				if (trackAdapter.getItem(i).getTitle().toLowerCase().compareTo(poi.getTitle().toLowerCase()) <= 0) {
					i++;
				} else {
					trackAdapter.insert(poi, i);
					insert = true;
					break;
				}
			}
		}

		if (!insert) {
			trackAdapter.insert(poi, trackAdapter.getCount());
		}
	}

	/* clean the adapter from the items modified or erased */
	private void removeTrack(TrackAdapter trackAdapter, Integer indexAdapter) {
		TrackObject objectToRemove = trackAdapter.getItem(indexAdapter);
		int i = 0;
		while (i < trackAdapter.getCount()) {
			if (trackAdapter.getItem(i).getEntityId() == objectToRemove.getEntityId()) {
				trackAdapter.remove(trackAdapter.getItem(i));
			} else {
				i++;
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		/*
		 * menu.clear(); MenuItem item = menu.add(Menu.CATEGORY_SYSTEM,
		 * R.id.map_view, Menu.NONE, R.string.map_view);
		 * item.setIcon(R.drawable.ic_map);
		 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		 */
		menu.clear();
		getActivity().getMenuInflater().inflate(R.menu.list_menu, menu);

		if (category == null) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.map_view) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
			boolean query = getArguments().containsKey(SearchFragment.ARG_QUERY);

			if (category != null && !query) {
				MapManager.switchToMapView(category, MapFragment.ARG_TRACK_CATEGORY, this);
			} else {
				ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
				for (int i = 0; i < list.getAdapter().getCount(); i++) {
					BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
					target.add(o);
				}
				MapManager.switchToMapView(target, this);
			}
			return true;
		} else if (item.getItemId() == R.id.search_action) {
			FragmentTransaction fragmentTransaction;
			Fragment fragment;
			fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			fragment = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, category);
			args.putString(CategoryHelper.CATEGORY_TYPE_TRACKS, CategoryHelper.CATEGORY_TYPE_TRACKS);
			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY)
					&& getArguments().getBoolean(SearchFragment.ARG_MY))
				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.frame_content, fragment, "tracks");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			/* add category to bundle */
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		if (reload) {
			trackAdapter = new TrackAdapter(context, R.layout.pois_row);
			setAdapter(trackAdapter);
			reload = false;
		}
		Bundle bundle = this.getArguments();
		String category = (bundle != null) ? bundle.getString(SearchFragment.ARG_CATEGORY) : null;
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered(CategoryHelper.CATEGORY_TYPE_TRACKS, category);
		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description)
				: null;

		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		if (categoryString != null) {
			title.setText(categoryString);
		} else if (bundle != null && bundle.containsKey(SearchFragment.ARG_QUERY)) {
			String query = bundle.getString(SearchFragment.ARG_QUERY);
			title.setText(context.getResources().getString(R.string.search_for) + "'" + query + "'");

			if (bundle.containsKey(SearchFragment.ARG_CATEGORY_SEARCH)) {
				category = bundle.getString(SearchFragment.ARG_CATEGORY_SEARCH);
				if (category != null)
					title.append(" " + context.getResources().getString(R.string.search_for) + " " + categoryString);
			}

		}
		if (bundle.containsKey(SearchFragment.ARG_WHERE_SEARCH)) {
			WhereForSearch where = bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH);
			if (where != null)
				title.append(" " + where.getDescription() + " ");
		}

		// close items menus if open
		((View) list.getParent()).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hideListItemsMenu(v, false);
			}
		});
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideListItemsMenu(view, false);
				setStoredTrackId(view, position);

			}
		});
		super.onStart();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		if ((postitionSelected != -1) && (scrollState == SCROLL_STATE_TOUCH_SCROLL)) {
			hideListItemsMenu(view, false);
		}
	}

	private void hideListItemsMenu(View v, boolean close) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
				trackAdapter.setElementSelected(-1);
				postitionSelected = -1;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null && !close) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			TrackDetailsFragment fragment = new TrackDetailsFragment();

			Bundle args = new Bundle();
			args.putString(TrackDetailsFragment.ARG_TRACK_ID, ((TrackPlaceholder) v.getTag()).track.getId());
			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.frame_content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private void setStoredTrackId(View v, int position) {
		final TrackObject poi = ((TrackPlaceholder) v.getTag()).track;
		idTrack = poi.getId();
		indexAdapter = position;
	}

	private List<TrackObject> getTracks(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<TrackObjectForBean> result = null;
			List<TrackObject> returnArray = new ArrayList<TrackObject>();
			Bundle bundle = getArguments();
			boolean my = false;
			if (bundle.getBoolean(SearchFragment.ARG_MY))
				my = true;
			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);
			String query = bundle.getString(SearchFragment.ARG_QUERY);

			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
			sort.put("title", 1);

			if (categories != null || my || query != null) {
				result = DTHelper.searchInGeneral(params[0].position, params[0].size, query,
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,
						TrackObjectForBean.class, sort, categories);
			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
				List<TrackObjectForBean> results = (List<TrackObjectForBean>) bundle.get(SearchFragment.ARG_LIST);
				for (TrackObjectForBean trackBean : results) {
					returnArray.add(trackBean.getObjectForBean());
				}
				return returnArray;
			} else {
				return Collections.emptyList();
			}

			for (TrackObjectForBean trackBean : result) {
				returnArray.add(trackBean.getObjectForBean());
			}
			return returnArray;
		} catch (Exception e) {
			Log.e(TrackListingFragment.class.getName(), e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private class TrackLoader extends
			AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<TrackObject>> {

		public TrackLoader(Activity activity) {
			super(activity);
		}

		@Override
		public List<TrackObject> performAction(AbstractLstingFragment.ListingRequest... params)
				throws SecurityException, Exception {
			return getTracks(params);
		}

		@Override
		public void handleResult(List<TrackObject> result) {
			updateList(result == null || result.isEmpty());
		}

	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return DTHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<TrackObject>> getLoader() {
		return new TrackLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	private void updateList(boolean empty) {
		if (getView() != null) {

			ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(R.id.poilistcontainer));
			if (empty) {
				ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(R.id.poilistcontainer));
			}
			hideListItemsMenu(null, false);
		}
	}

}
