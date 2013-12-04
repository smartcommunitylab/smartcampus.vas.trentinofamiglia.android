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
package eu.trentorise.smartcampus.trentinofamiglia.fragments.poi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import eu.trentorise.smartcampus.android.common.SCAsyncTask.SCAsyncTaskProcessor;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.TaggingDialog.TagProvider;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.ViewHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.PoiObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class PoisListingFragment extends AbstractLstingFragment<POIObject> implements TagProvider {

	private ListView list;
	private Context context;
	private String category;
	private boolean mFollowByIntent;
	private PoiAdapter poiAdapter;
	public static final String ARG_ID = "id_poi";
	public static final String ARG_INDEX = "index_adapter";
	private String idPoi = "";
	private Integer indexAdapter;
	private Boolean reload = false;
	private Integer postitionSelected = 0;
	private ViewSwitcher previousViewSwitcher;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_ID, idPoi);
		if (indexAdapter != null)
			outState.putInt(ARG_INDEX, indexAdapter);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.context = this.getActivity();
		setHasOptionsMenu(true);
		setFollowByIntent();
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		list = (ListView) getActivity().findViewById(R.id.pois_list);

		if (arg0 != null) {
			// Restore last state for checked position.
			idPoi = arg0.getString(ARG_ID);
			indexAdapter = arg0.getInt(ARG_INDEX);

		}
		if (poiAdapter == null) {
			poiAdapter = new PoiAdapter(context, R.layout.pois_row);
		}
		setAdapter(poiAdapter);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.poislist, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!idPoi.equals("")) {
			// get info of the event
			POIObject poi = DTHelper.findPOIById(idPoi);

			if (poi == null) {
				// cancellazione
				removePoi(poiAdapter, indexAdapter);

			} else {
				// modifica se numero della versione e' diverso
				// if (poi.getUpdateTime() != poiAdapter.getItem(indexAdapter)
				// .getUpdateTime()) {
				if (poi.getUpdateTime() == 0) {
					removePoi(poiAdapter, indexAdapter);
					insertPOI(poi);
				}
			}
			// notify
			poiAdapter.notifyDataSetChanged();
			idPoi = "";
			indexAdapter = 0;
		}
	}

	/*
	 * insert in the same adapter the new item
	 */
	private void insertPOI(POIObject poi) {

		// add in the right place
		int i = 0;
		boolean insert = false;
		while (i < poiAdapter.getCount()) {
			if (poiAdapter.getItem(i).getTitle() != null) {
				if (poiAdapter.getItem(i).getTitle().toLowerCase().compareTo(poi.getTitle().toLowerCase()) <= 0) {
					i++;
				} else {
					poiAdapter.insert(poi, i);
					insert = true;
					break;
				}
			}
		}

		if (!insert) {
			poiAdapter.insert(poi, poiAdapter.getCount());
		}
	}

	/* clean the adapter from the items modified or erased */
	private void removePoi(PoiAdapter poisAdapter, Integer indexAdapter) {
		POIObject objectToRemove = poisAdapter.getItem(indexAdapter);
		int i = 0;
		while (i < poisAdapter.getCount()) {
			if (poisAdapter.getItem(i).getEntityId() == objectToRemove.getEntityId()) {
				poisAdapter.remove(poisAdapter.getItem(i));
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
//		SubMenu submenu = menu.getItem(0).getSubMenu();
//		submenu.clear();

		if (category == null) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
		}


		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

//		NotificationsSherlockFragmentDT.onOptionsItemSelectedNotifications(getActivity(), item);

		if (item.getItemId() == R.id.map_view) {
			category = (getArguments() != null) ? getArguments().getString(SearchFragment.ARG_CATEGORY) : null;
			boolean query = getArguments().containsKey(SearchFragment.ARG_QUERY);
			if (category != null && !query) {
				MapManager.switchToMapView(category, MapFragment.ARG_POI_CATEGORY, this);
			} else {
				ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
				for (int i = 0; i < list.getAdapter().getCount(); i++) {
					BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
					target.add(o);
				}
				MapManager.switchToMapView(target, this);
			}
			return true;
		}

	else if (item.getItemId() == R.id.search_action) {
			FragmentTransaction fragmentTransaction;
			Fragment fragment;
			fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			fragment = new SearchFragment();
			Bundle args = new Bundle();
			args.putString(SearchFragment.ARG_CATEGORY, category);
			args.putString(CategoryHelper.CATEGORY_TYPE_POIS, CategoryHelper.CATEGORY_TYPE_POIS);
			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY)
					&& getArguments().getBoolean(SearchFragment.ARG_MY))
				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.frame_content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			/* add category to bundle */
			return true;

		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(
					getActivity().getPackageName(), PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(PoisListingFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onStart() {
		if (reload) {
			poiAdapter = new PoiAdapter(context, R.layout.pois_row);
			setAdapter(poiAdapter);
			reload = false;
		}
		Bundle bundle = this.getArguments();
		String category = (bundle != null) ? bundle.getString(SearchFragment.ARG_CATEGORY) : null;
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered(CategoryHelper.CATEGORY_TYPE_POIS, category);
		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description) : null;

		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		if (categoryString != null) {
			title.setText(categoryString);
		} 
		if (bundle != null && bundle.containsKey(SearchFragment.ARG_QUERY)
				&& bundle.getString(SearchFragment.ARG_QUERY) != null) {
			String query = bundle.getString(SearchFragment.ARG_QUERY);
			title.setText(context.getResources().getString(R.string.search_for) + " ' " + query + " '");
			if (bundle.containsKey(SearchFragment.ARG_CATEGORY)) {
				category = bundle.getString(SearchFragment.ARG_CATEGORY);
				if (category != null)
					title.append(" " + context.getResources().getString(R.string.search_in_category) + " "
							+ getString(catDescriptor.description));
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
				setStorePoiId(view, position);

			}
		});

		// open items menu for that entry
		// list.setOnItemLongClickListener(new
		// AdapterView.OnItemLongClickListener() {
		// public boolean onItemLongClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// if ((position != postitionSelected) && (previousViewSwitcher !=
		// null)) {
		// // //close the old viewSwitcher
		// previousViewSwitcher.showPrevious();
		// poiAdapter.setElementSelected(-1);
		// previousViewSwitcher = null;
		// hideListItemsMenu(view, true);
		//
		// }
		// ViewSwitcher vs = (ViewSwitcher)
		// view.findViewById(R.id.poi_viewswitecher);
		// setupOptionsListeners(vs, position);
		// vs.showNext();
		// postitionSelected = position;
		// poiAdapter.setElementSelected(position);
		// previousViewSwitcher = vs;
		//
		// return true;
		// }
		// });

//		FeedbackFragmentInflater.inflateHandleButton(getActivity(), getView());
		super.onStart();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		if ((postitionSelected != -1) && (scrollState == SCROLL_STATE_TOUCH_SCROLL)) {
			hideListItemsMenu(view, false);
		}
	}

//	protected void setupOptionsListeners(final ViewSwitcher vs, final int position) {
//		final POIObject poi = ((PoiPlaceholder) vs.getTag()).poi;
//
//		ImageButton b = (ImageButton) vs.findViewById(R.id.poi_delete_btn);
//		// CAN DELETE ONLY OWN OBJECTS
//		if (DTHelper.isOwnedObject(poi)) {
//			b.setVisibility(View.VISIBLE);
//			b.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
////					if (new AMSCAccessProvider().isUserAnonymous(getActivity())) {
////						// show dialog box
////						UserRegistration.upgradeuser(getActivity());
////					} else 
//					{
//						new SCAsyncTask<POIObject, Void, Boolean>(getActivity(), new POIDeleteProcessor(getActivity()))
//								.execute(poi);
//					}
//				}
//			});
//		} else {
//			b.setVisibility(View.GONE);
//		}
//
//		b = (ImageButton) vs.findViewById(R.id.poi_edit_btn);
//		b.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
////				if (new AMSCAccessProvider().isUserAnonymous(getActivity())) {
////					// show dialog box
////					UserRegistration.upgradeuser(getActivity());
////				} else 
//				{
//					FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
//							.beginTransaction();
//					Fragment fragment = new CreatePoiFragment();
//					setStorePoiId((View) vs, position);
//					Bundle args = new Bundle();
//					args.putSerializable(CreatePoiFragment.ARG_POI, poi);
//					fragment.setArguments(args);
//					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//					// fragmentTransaction.detach(this);
//					fragmentTransaction.replace(android.R.id.content, fragment, "pois");
//					fragmentTransaction.addToBackStack(fragment.getTag());
//					fragmentTransaction.commit();
//				}
//			}
//		});
//		// b = (ImageButton) vs.findViewById(R.id.poi_share_btn);
//		// b.setOnClickListener(new OnClickListener() {
//		// @Override
//		// public void onClick(View v) {
//		// Toast.makeText(getActivity(),
//		// getString(R.string.toast_poi_shared),
//		// Toast.LENGTH_SHORT).show();
//		//
//		// }
//		// });
//		b = (ImageButton) vs.findViewById(R.id.poi_tag_btn);
//		b.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
////				if (new AMSCAccessProvider().isUserAnonymous(getActivity())) {
////					// show dialog box
////					UserRegistration.upgradeuser(getActivity());
////				} else 
//				{
//					TaggingDialog taggingDialog = new TaggingDialog(getActivity(), new TaggingDialog.OnTagsSelectedListener() {
//
//						@SuppressWarnings("unchecked")
//						@Override
//						public void onTagsSelected(Collection<SemanticSuggestion> suggestions) {
//							new TaggingAsyncTask(poi).execute(Utils.conceptConvertSS(suggestions));
//						}
//					}, PoisListingFragment.this, Utils.conceptConvertToSS(poi.getCommunityData().getTags()));
//					taggingDialog.show();
//				}
//			}
//		});
//		b = (ImageButton) vs.findViewById(R.id.poi_follow_btn);
//		b.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
////				FollowEntityObject obj = new FollowEntityObject(poi.getEntityId(), poi.getTitle(), DTConstants.ENTITY_TYPE_POI);
////				if (mFollowByIntent) {
////					FollowHelper.follow(getActivity(), obj);
////				} else {
//					SCAsyncTask<Object, Void, BaseDTObject> followTask = new SCAsyncTask<Object, Void, BaseDTObject>(getActivity(),
//							new FollowAsyncTaskProcessor(getActivity(), null));
//					followTask.execute(getActivity().getApplicationContext(), DTParamsHelper.getAppToken(),
//							DTHelper.getAuthToken(), poi);
//
////				}
//			}
//		});
//	}

	private void hideListItemsMenu(View v, boolean close) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
				poiAdapter.setElementSelected(-1);
				postitionSelected = -1;
				previousViewSwitcher = null;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null && !close) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			PoiDetailsFragment fragment = new PoiDetailsFragment();

			Bundle args = new Bundle();
			args.putString(PoiDetailsFragment.ARG_POI_ID, ((PoiPlaceholder) v.getTag()).poi.getId());
			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.frame_content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
		}
	}

	private void setStorePoiId(View v, int position) {
		final POIObject poi = ((PoiPlaceholder) v.getTag()).poi;
		idPoi = poi.getId();
		indexAdapter = position;
	}

	private List<POIObject> getPOIs(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<PoiObjectForBean> result = null;
			List<POIObject> returnArray = new ArrayList<POIObject>();
			Bundle bundle = getArguments();
			boolean my = false;
			if (bundle.getBoolean(SearchFragment.ARG_MY))
				my = true;
			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);

			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
			sort.put("title", 1);


			if (bundle.containsKey(SearchFragment.ARG_CATEGORY) && (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_MY) && (bundle.getBoolean(SearchFragment.ARG_MY))) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {

				result = DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my, PoiObjectForBean.class, sort,
						categories);

			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
				return (List<POIObject>) bundle.get(SearchFragment.ARG_LIST);
			} else {
				return Collections.emptyList();
			}


			for (PoiObjectForBean storyBean : result) {
				returnArray.add(storyBean.getObjectForBean());
			}
			return returnArray;
			
		} catch (Exception e) {
			Log.e(PoisListingFragment.class.getName(), e.getMessage());
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	private class PoiLoader extends AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<POIObject>> {

		public PoiLoader(Activity activity) {
			super(activity);
		}

		@Override
		public List<POIObject> performAction(AbstractLstingFragment.ListingRequest... params) throws SecurityException,
				Exception {
//			if (getArguments().containsKey(SearchFragment.ARG_CATEGORY) && (getArguments().getString(SearchFragment.ARG_CATEGORY) != null && (getArguments().getString(SearchFragment.ARG_CATEGORY).compareTo(CategoryHelper.CAT_POI_BABY_LITTLE_HOME)==0))) {
//				return Collections.emptyList();
//			}
			return getPOIs(params);
		}

		@Override
		public void handleResult(List<POIObject> result) {
//			if (getArguments().containsKey(SearchFragment.ARG_CATEGORY) && (getArguments().getString(SearchFragment.ARG_CATEGORY) != null && (getArguments().getString(SearchFragment.ARG_CATEGORY).compareTo(CategoryHelper.CAT_POI_BABY_LITTLE_HOME)==0))) {
//				Toast.makeText(context, getString(R.string.coming_soon), Toast.LENGTH_LONG).show();
//			}
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

//	private class TaggingAsyncTask extends SCAsyncTask<List<Concept>, Void, Void> {
//
//		public TaggingAsyncTask(final POIObject p) {
//			super(getActivity(), new AbstractAsyncTaskProcessor<List<Concept>, Void>(getActivity()) {
//				@Override
//				public Void performAction(List<Concept>... params) throws SecurityException, Exception {
//					p.getCommunityData().setTags(params[0]);
//					DTHelper.savePOI(p);
//					return null;
//				}
//
//				@Override
//				public void handleResult(Void result) {
//					Toast.makeText(getActivity(), getString(R.string.tags_successfully_added), Toast.LENGTH_SHORT)
//							.show();
//				}
//			});
//		}
//	}

//	private class POIDeleteProcessor extends AbstractAsyncTaskProcessor<POIObject, Boolean> {
//		private POIObject object = null;
//
//		public POIDeleteProcessor(Activity activity) {
//			super(activity);
//		}
//
//		@Override
//		public Boolean performAction(POIObject... params) throws SecurityException, Exception {
//			object = params[0];
//			return DTHelper.deletePOI(params[0]);
//		}
//
//		@Override
//		public void handleResult(Boolean result) {
//			if (result) {
//				((PoiAdapter) list.getAdapter()).remove(object);
//				((PoiAdapter) list.getAdapter()).notifyDataSetChanged();
//				updateList(((PoiAdapter) list.getAdapter()).isEmpty());
//			} else {
//				Toast.makeText(getActivity(), getString(R.string.app_failure_cannot_delete), Toast.LENGTH_LONG).show();
//			}
//		}
//
//	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<POIObject>> getLoader() {
		return new PoiLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	private void updateList(boolean empty) {
		if (getView()!=null){

		ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(
				R.id.poilistcontainer));
		if (empty) {
			ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(
					R.id.poilistcontainer));
		}
		hideListItemsMenu(null, false);
		}
	}

}
