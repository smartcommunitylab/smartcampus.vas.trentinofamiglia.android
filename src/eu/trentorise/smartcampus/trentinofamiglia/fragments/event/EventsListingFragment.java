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
package eu.trentorise.smartcampus.trentinofamiglia.fragments.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import android.view.inputmethod.InputMethodManager;
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
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.ViewHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.ExplorerObject;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

// to be used for event listing both in categories and in My Events
public class EventsListingFragment extends AbstractLstingFragment<ExplorerObject> implements TagProvider {
	private ListView list;
	private Context context;

	public static final String ARG_CATEGORY = "event_category";
	public static final String ARG_POI = "event_poiId";
	public static final String ARG_POI_NAME = "event_poi_title";
	public static final String ARG_QUERY = "event_query";
	public static final String ARG_QUERY_TODAY = "event_query_today";
	public static final String ARG_MY = "event_my";
	public static final String ARG_CATEGORY_SEARCH = "category_search";
	public static final String ARG_MY_EVENTS_SEARCH = "my_events_search";
	public static final String ARG_LIST = "event_list";
	public static final String ARG_ID = "id_event";
	public static final String ARG_INDEX = "index_adapter";

	private String category;
	private EventAdapter eventsAdapter;
	private boolean mFollowByIntent;
	private String idEvent = "";
	private Integer indexAdapter;
	private Boolean reload = false;
	private Integer postitionSelected = -1;
	private List<ExplorerObject> listEvents = new ArrayList<ExplorerObject>();
	private boolean postProcAndHeader = true;

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		list = (ListView) getActivity().findViewById(R.id.events_list);
		if (arg0 != null) {
			// Restore last state for checked position.
			idEvent = arg0.getString(ARG_ID);
			indexAdapter = arg0.getInt(ARG_INDEX);

		}

		postProcAndHeader = false;
		/* create the adapter is it is the first time you load */
		if (eventsAdapter == null) {
			eventsAdapter = new EventAdapter(context, R.layout.events_row, postProcAndHeader);
		}
		setAdapter(eventsAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (!idEvent.equals("")) {
			// get info of the event
			ExplorerObject event = DTHelper.findEventById(idEvent);
			// notify
			eventsAdapter.notifyDataSetChanged();
			idEvent = "";
			indexAdapter = 0;
		}

	}

	private void restoreElement(EventAdapter eventsAdapter2, Integer indexAdapter2, ExplorerObject event) {
		removeEvent(eventsAdapter, indexAdapter);
		insertEvent(event, indexAdapter);

	}

	/*
	 * insert in the same adapter the new itemsdo the post proc if they are
	 * multiday events
	 */
	private void insertEvent(ExplorerObject event, Integer indexAdapter2) {

		Calendar cal = Calendar.getInstance();
		calToDate(cal);
		long biggerFromTime = cal.getTimeInMillis();
		// add in the right place
		List<ExplorerObject> returnList = new ArrayList<ExplorerObject>();
		int i = 0;
		int j = 0;
		while (i < eventsAdapter.getCount() + 1) {
			if (i != indexAdapter2) {
				returnList.add(eventsAdapter.getItem(j));
				j++;
			} else
				returnList.add(event);
			i++;

		}
		eventsAdapter.clear();

		// post proc for multidays
		i = 0;
		List<ExplorerObject> newList = returnList;// postProcForRecurrentEvents(returnList,
													// biggerFromTime, false);
		while (i < newList.size()) {
			eventsAdapter.insert(newList.get(i), i);
			i++;
		}
	}

	/* clean the adapter from the items modified or erased */
	private void removeEvent(EventAdapter eventsAdapter, Integer indexAdapter) {
		ExplorerObject objectToRemove = eventsAdapter.getItem(indexAdapter);
		int i = 0;
		while (i < eventsAdapter.getCount()) {
			if (eventsAdapter.getItem(i).getEntityId() == objectToRemove.getEntityId())
				eventsAdapter.remove(eventsAdapter.getItem(i));
			else
				i++;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ARG_ID, idEvent);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventslist, container, false);
	}

	private void setFollowByIntent() {
		try {
			ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(),
					PackageManager.GET_META_DATA);
			Bundle aBundle = ai.metaData;
			mFollowByIntent = aBundle.getBoolean("follow-by-intent");
		} catch (NameNotFoundException e) {
			mFollowByIntent = false;
			Log.e(EventsListingFragment.class.getName(), "you should set the follow-by-intent metadata in app manifest");
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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
				MapManager.switchToMapView(category, MapFragment.ARG_EVENT_CATEGORY, this);
			} else {
				ArrayList<BaseDTObject> target = new ArrayList<BaseDTObject>();
				if (list != null) {
					for (int i = 0; i < list.getAdapter().getCount(); i++) {
						BaseDTObject o = (BaseDTObject) list.getAdapter().getItem(i);
						if (o.getLocation() != null && o.getLocation()[0] != 0 && o.getLocation()[1] != 0) {
							target.add(o);
						}
					}
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
			args.putString(CategoryHelper.CATEGORY_TYPE_EVENTS, CategoryHelper.CATEGORY_TYPE_EVENTS);
			if (getArguments() != null && getArguments().containsKey(SearchFragment.ARG_MY)
					&& getArguments().getBoolean(SearchFragment.ARG_MY))
				args.putBoolean(SearchFragment.ARG_MY, getArguments().getBoolean(SearchFragment.ARG_MY));
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			fragmentTransaction.replace(R.id.frame_content, fragment, "events");
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
		// hide keyboard if it is still open
		Utils.hideKeyboard(getActivity());
		
		Bundle bundle = this.getArguments();

		if (reload) {
			eventsAdapter = new EventAdapter(context, R.layout.events_row, postProcAndHeader);
			setAdapter(eventsAdapter);
			reload = false;
		}

		// set title
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		String category = bundle.getString(SearchFragment.ARG_CATEGORY);
		CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered(
				CategoryHelper.CATEGORY_TYPE_EVENTS, category);
		String categoryString = (catDescriptor != null) ? context.getResources().getString(catDescriptor.description)
				: null;
//		//load warning toast if summer events
//		warningToast(catDescriptor);

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

		} else if (bundle != null && bundle.containsKey(SearchFragment.ARG_CATEGORY)
				&& (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {
			title.setText(categoryString);
		}
		// else if (bundle != null && bundle.containsKey(SearchFragment.ARG_MY)
		// && bundle.getBoolean(SearchFragment.ARG_MY)) {
		// title.setText(R.string.myevents);
		// }
		else if (bundle != null && bundle.containsKey(ARG_POI_NAME)) {
			String poiName = bundle.getString(ARG_POI_NAME);
			title.setText(getResources().getString(R.string.eventlist_at_place) + " " + poiName);
		} else if (bundle != null && bundle.containsKey(ARG_QUERY)) {
			String query = bundle.getString(ARG_QUERY);
			title.setText(context.getResources().getString(R.string.search_for) + " '" + query + "'");
			if (bundle.containsKey(ARG_CATEGORY_SEARCH)) {
				category = bundle.getString(ARG_CATEGORY_SEARCH);
				if (category != null)
					title.append(context.getResources().getString(R.string.search_in_category) + " " + category);
			}
		} else if (bundle != null && bundle.containsKey(ARG_QUERY_TODAY)) {
			title.setText(context.getResources().getString(R.string.search_today_events));
		}
		if (bundle.containsKey(SearchFragment.ARG_WHERE_SEARCH)) {
			WhereForSearch where = bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH);
			if (where != null)
				title.append(" " + where.getDescription() + " ");
		}

		if (bundle.containsKey(SearchFragment.ARG_WHEN_SEARCH)) {
			WhenForSearch when = bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH);
			if (when != null)
				title.append(" " + when.getDescription() + " ");
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
				setStoreEventId(view, position);
			}

		});

		super.onStart();

	}

//	private void warningToast(CategoryDescriptor catDescriptor) {
//		if (catDescriptor.category.equals(CategoryHelper.CAT_EVENT_ESTATE_GIOVANI_E_FAMIGLIA))
//			Toast.makeText(getActivity(), R.string.warning_summer, Toast.LENGTH_LONG).show();
//	}

	private void setStoreEventId(View v, int position) {
		final ExplorerObject event = ((EventPlaceholder) v.getTag()).event;
		idEvent = event.getId();
		indexAdapter = position;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		if ((postitionSelected != -1) && (scrollState == SCROLL_STATE_TOUCH_SCROLL)) {
			hideListItemsMenu(view, true);

		}
	}

	private void hideListItemsMenu(View v, boolean close) {
		boolean toBeHidden = false;
		for (int index = 0; index < list.getChildCount(); index++) {
			View view = list.getChildAt(index);
			if (view != null && view instanceof LinearLayout && ((LinearLayout) view).getChildCount() == 2)
				view = ((LinearLayout) view).getChildAt(1);
			if (view instanceof ViewSwitcher && ((ViewSwitcher) view).getDisplayedChild() == 1) {
				((ViewSwitcher) view).showPrevious();
				toBeHidden = true;
				eventsAdapter.setElementSelected(-1);
				postitionSelected = -1;
			}
		}
		if (!toBeHidden && v != null && v.getTag() != null && !close) {
			// no items needed to be flipped, fill and open details page
			FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			EventDetailsFragment fragment = new EventDetailsFragment();

			Bundle args = new Bundle();
			// args.putSerializable(EventDetailsFragment.ARG_EVENT_OBJECT,
			// ((EventPlaceholder) v.getTag()).event);
			args.putString(EventDetailsFragment.ARG_EVENT_ID, ((EventPlaceholder) v.getTag()).event.getId());

			fragment.setArguments(args);

			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.frame_content, fragment, "events");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();

		}
	}

	private List<ExplorerObject> getEvents(AbstractLstingFragment.ListingRequest... params) {
		try {
			Collection<ExplorerObject> result = null;

			Bundle bundle = getArguments();
			boolean my = false;

			if (bundle == null) {
				return Collections.emptyList();
			}
			if (bundle.getBoolean(SearchFragment.ARG_MY))
				my = true;
			String categories = bundle.getString(SearchFragment.ARG_CATEGORY);
			SortedMap<String, Integer> sort = new TreeMap<String, Integer>();
			sort.put("fromTime", 1);
			if (bundle.containsKey(SearchFragment.ARG_CATEGORY)
					&& (bundle.getString(SearchFragment.ARG_CATEGORY) != null)) {

				result = Utils.convertToLocalEventFromBean(DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,
						EventObjectForBean.class, sort, categories));

			} else if (bundle.containsKey(ARG_POI) && (bundle.getString(ARG_POI) != null)) {
				result = Utils.convertToLocalEvent(DTHelper.getEventsByPOI(params[0].position, params[0].size,
						bundle.getString(ARG_POI)));
			} else if (bundle.containsKey(SearchFragment.ARG_MY) && (bundle.getBoolean(SearchFragment.ARG_MY))) {

				result = Utils.convertToLocalEventFromBean(DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,
						EventObjectForBean.class, sort, categories));

			} else if (bundle.containsKey(SearchFragment.ARG_QUERY)) {

				result = Utils.convertToLocalEventFromBean(DTHelper.searchInGeneral(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY),
						(WhereForSearch) bundle.getParcelable(SearchFragment.ARG_WHERE_SEARCH),
						(WhenForSearch) bundle.getParcelable(SearchFragment.ARG_WHEN_SEARCH), my,
						EventObjectForBean.class, sort, categories));

			} else if (bundle.containsKey(ARG_QUERY_TODAY)) {
				result = DTHelper.searchTodayEvents(params[0].position, params[0].size,
						bundle.getString(SearchFragment.ARG_QUERY));
			} else if (bundle.containsKey(SearchFragment.ARG_LIST)) {
				result = (Collection<ExplorerObject>) bundle.get(SearchFragment.ARG_LIST);
			} else {
				return Collections.emptyList();
			}

			/* conversion to LocalObject */
			listEvents.addAll(result);

			List<ExplorerObject> sorted = new ArrayList<ExplorerObject>(listEvents);
			if (!postProcAndHeader) {
				return sorted;
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(System.currentTimeMillis());
				calToDate(cal);
				long biggerFromTime = cal.getTimeInMillis();
				if (sorted.size() > 0) {
					// listEvents.addAll(postProcForRecurrentEvents(sorted,
					// biggerFromTime));
					return postProcForRecurrentEvents(sorted, biggerFromTime, result.size() == 0
							|| result.size() < getSize());
				} else {
					return sorted;
				}
			}
		} catch (Exception e) {
			Log.e(EventsListingFragment.class.getName(), "" + e.getMessage());
			e.printStackTrace();
			listEvents = Collections.emptyList();
			return listEvents;
		}
	}

	private void calToDate(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}

	@Override
	public List<SemanticSuggestion> getTags(CharSequence text) {
		try {
			return DTHelper.getSuggestions(text);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	private class EventLoader extends
			AbstractAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<ExplorerObject>> {

		public EventLoader(Activity activity) {
			super(activity);
		}

		// fetches the events
		@Override
		public List<ExplorerObject> performAction(AbstractLstingFragment.ListingRequest... params)
				throws SecurityException, Exception {
			return getEvents(params);
		}

		// populates the listview with the events
		@Override
		public void handleResult(List<ExplorerObject> result) {
			eventsAdapter.clear();
			updateList(result == null || result.isEmpty());
		}
	}

	private List<ExplorerObject> postProcForRecurrentEvents(List<ExplorerObject> result, long lessFromTime,
			boolean endReached) {
		List<ExplorerObject> returnList = new ArrayList<ExplorerObject>();
		EventComparator r = new EventComparator();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(result.get(result.size() - 1).getFromTime());
		calToDate(cal);
		long biggerFromTime = cal.getTimeInMillis();
		if (biggerFromTime < lessFromTime)
			biggerFromTime = lessFromTime;
		for (ExplorerObject event : result) {
			if (event.getToTime() == null || event.getToTime() == 0) {
				event.setToTime(event.getFromTime());
			}
			/*
			 * if an event has toTime null o equal to toTime, it is only for
			 * that day
			 */

			if ((event.getToTime() != null) && (event.getFromTime() != null)
					&& (event.getToTime() != event.getFromTime())) {
				long eventFromTime = event.getFromTime();
				long eventToTime = event.getToTime();

				Calendar calFromTime = Calendar.getInstance();
				Calendar calToTime = Calendar.getInstance();

				calFromTime.setTime(new Date(eventFromTime));
				calToDate(calFromTime);

				calToTime.setTime(new Date(eventToTime));
				calToDate(calToTime);
				long dayFromTime = calFromTime.getTimeInMillis();
				long dayToTime = calToTime.getTimeInMillis();

				if (dayFromTime == dayToTime) {
					/* it takes the same day */
					returnList.add(event);

				} else {
					/*
					 * if and event takes more than one day, duplicate it (until
					 * X)
					 */
					dayFromTime = Math.max(dayFromTime, lessFromTime);
					if (!endReached) {
						dayToTime = Math.min(dayToTime, biggerFromTime);
					}
					long dayTmpTime = dayFromTime;

					while (dayTmpTime <= dayToTime) {
						ExplorerObject newEvent = event.copy();
						newEvent.setFromTime(dayTmpTime);
						newEvent.setToTime(dayTmpTime);
						Calendar caltmp = Calendar.getInstance();
						caltmp.setTimeInMillis(dayTmpTime);
						caltmp.add(Calendar.DATE, 1);
						dayTmpTime = caltmp.getTimeInMillis();
						returnList.add(newEvent);
						if (this.getArguments().containsKey(ARG_QUERY_TODAY))
							break;
					}
					/* calculate how much days use the events */
					/* create and entry for every day */
				}

			} else {
				/* put it in the returnList */
				returnList.add(event);
			}
		}
		Collections.sort(returnList, r);
		return returnList;

	}

	private static class EventComparator implements Comparator<ExplorerObject> {
		public int compare(ExplorerObject c1, ExplorerObject c2) {
			if (c1.getFromTime() == c2.getFromTime())
				return 0;
			if (c1.getFromTime() < c2.getFromTime())
				return -1;
			if (c1.getFromTime() > c2.getFromTime())
				return 1;
			return 0;
		}
	}

	@Override
	protected SCAsyncTaskProcessor<AbstractLstingFragment.ListingRequest, List<ExplorerObject>> getLoader() {
		return new EventLoader(getActivity());
	}

	@Override
	protected ListView getListView() {
		return list;
	}

	private void updateList(boolean empty) {
		if (getView() != null) {
			ViewHelper.removeEmptyListView((LinearLayout) getView().findViewById(R.id.eventlistcontainer));
			if (empty) {
				ViewHelper.addEmptyListView((LinearLayout) getView().findViewById(R.id.eventlistcontainer));
			}
			hideListItemsMenu(null, false);
		}
	}

}
