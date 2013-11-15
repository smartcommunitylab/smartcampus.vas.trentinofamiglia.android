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
package eu.trentorise.smartcampus.trentinofamiglia.fragments.search;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.listing.AbstractLstingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper.CategoryDescriptor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.SearchHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoisListingFragment;

public class SearchFragment extends Fragment {

	public static final String ARG_WHEN_SEARCH = "when_search";
	public static final String ARG_WHERE_SEARCH = "where_search";
	public static final String ARG_MY = "my";
	public static final String ARG_CATEGORY = "category";
	public static final String ARG_LIST = "list";
	public static final String ARG_CATEGORY_SEARCH = "category_search";
	public static final String ARG_QUERY = "query";

	private TextView titleSearch;
	private EditText whatSearch;
	private List<WhenForSearch> when = new ArrayList<WhenForSearch>();
	private TextView whenLabel;
	private Spinner whenSearchSpinner;
	private ArrayAdapter<WhenForSearch> whenAdapter;
	private List<WhereForSearch> where = new ArrayList<WhereForSearch>();
	private TextView whereLabel;
	private Spinner whereSearchSpinner;
	private WhereAdapter whereAdapter;
	private String selectedWhat;
	private WhereForSearch selectedWhere;
	private WhenForSearch selectedWhen;
	private Boolean selectedMy;
	private String selectedCategory;
	private boolean isonline = true;
	private String type = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.search, container, false);
	}

	@Override
	public void onStart() {
		/* check if online or not and disable some components */
		super.onStart();

		/* params */
		titleSearch = (TextView) getView().findViewById(R.id.title_search_label);
		whatSearch = (EditText) getView().findViewById(R.id.what_text);
		Bundle bundle = this.getArguments();

		setupSpinnersAndLabel();
		hideViewDependOnType(bundle);

//		if (bundle != null && bundle.containsKey(ARG_MY)) {
//			selectedMy = bundle.getBoolean(ARG_MY);
//			titleSearch.setText(R.string.myevents);
//
//		} else 
			if (bundle != null && bundle.containsKey(ARG_CATEGORY)) {
			selectedCategory = bundle.getString(ARG_CATEGORY);
			CategoryDescriptor catDescriptor = CategoryHelper.getCategoryDescriptorByCategoryFiltered(type, selectedCategory);
			String categoryString = (catDescriptor != null) ? getActivity().getResources().getString(
					catDescriptor.description) : null;
			titleSearch.setText(categoryString);
		} else
			titleSearch.setText(R.string.search_txt);

		Button sendBtn = (Button) getView().findViewById(R.id.search_ok_button);
		sendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				AbstractLstingFragment fragment = null;
				WhenForSearch passedWhen = null;
				selectedWhat = whatSearch.getText().toString();
				selectedWhen = whenSearchSpinner.getSelectedItemPosition() > 0 ? when.get(whenSearchSpinner
						.getSelectedItemPosition()) : null;
				/* set when if events */
				if (CategoryHelper.CATEGORY_TYPE_EVENTS.equals(type)) {
					if (selectedWhen == null) {
						selectedWhen = new WhenForSearch("", 0, 0);
						passedWhen = new WhenForSearch("", DTHelper.getCurrentDateTimeForSearching(), 0);
					} else {
						passedWhen = new WhenForSearch("", DTHelper.getCurrentDateTimeForSearching() + selectedWhen.getFrom(),
								DTHelper.getCurrentDateTimeForSearching() + selectedWhen.getFrom() + selectedWhen.getTo());
					}
				}
				selectedWhere = whereSearchSpinner.getSelectedItemPosition() > 0 ? where.get(whereSearchSpinner
						.getSelectedItemPosition()) : null;
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
				if (CategoryHelper.CATEGORY_TYPE_EVENTS.equals(type)) {
					fragment = new EventsListingFragment();
				} else if (CategoryHelper.CATEGORY_TYPE_POIS.equals(type)) {
					fragment = new PoisListingFragment();
				} 
//				else if (CategoryHelper.CATEGORY_TYPE_STORIES.equals(type)) {
//					fragment = new StoriesListingFragment();
//				}

				Bundle args = new Bundle();
				if (selectedWhen != null)
					args.putParcelable(ARG_WHEN_SEARCH, passedWhen);
				if (selectedWhere != null)
					args.putParcelable(ARG_WHERE_SEARCH, selectedWhere);
				if (selectedMy != null)
					args.putBoolean(ARG_MY, selectedMy);
				if (selectedCategory != null)
					args.putString(ARG_CATEGORY, selectedCategory);
				if (selectedWhat != null)
					args.putString(ARG_QUERY, selectedWhat);

				fragment.setArguments(args);
				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				fragmentTransaction.replace(android.R.id.content, fragment, type);
				fragmentTransaction.addToBackStack(fragment.getTag());
				fragmentTransaction.commit();
			}
		});
		Button cancelBtn = (Button) getView().findViewById(R.id.search_cancel_button);
		cancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
	}

	/*
	 * Hide button and field depending on the type searched by the user Events
	 * search by distance AND by date Pois search by distance Stories search
	 * only by text (common for all of these)
	 */
	private void hideViewDependOnType(Bundle bundle) {
		/* get the type */
		if (bundle != null && bundle.containsKey(CategoryHelper.CATEGORY_TYPE_EVENTS)) {
			type = CategoryHelper.CATEGORY_TYPE_EVENTS;

		} else if (bundle != null && bundle.containsKey(CategoryHelper.CATEGORY_TYPE_POIS)) {
			type = CategoryHelper.CATEGORY_TYPE_POIS;
			whenSearchSpinner.setVisibility(View.GONE);
			whenLabel.setVisibility(View.GONE);

		} else if (bundle != null && bundle.containsKey(CategoryHelper.CATEGORY_TYPE_STORIES)) {
			type = CategoryHelper.CATEGORY_TYPE_STORIES;
			whenSearchSpinner.setVisibility(View.GONE);
			whenLabel.setVisibility(View.GONE);
			whereSearchSpinner.setVisibility(View.GONE);
			whereLabel.setVisibility(View.GONE);

		}

	}

	@Override
	public void onResume() {
		super.onResume();
		isonline = DTHelper.checkInternetConnection(getActivity());
		whereSearchSpinner.setEnabled(isonline);
		if (!isonline) {
			new AlertDialog.Builder(getActivity()).setTitle(R.string.dialog_connectivity_title)
					.setMessage(R.string.dialog_connection_search)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// continue with delete
						}
					}).show();
		}
	}

	private void setupSpinnersAndLabel() {
		SearchHelper.initSpinners(getActivity());
		whenLabel = (TextView) getView().findViewById(R.id.when_label);
		whenSearchSpinner = (Spinner) getView().findViewById(R.id.when_spinner);
		whenAdapter = new ArrayAdapter<WhenForSearch>(getActivity(), R.layout.dd_list, R.id.dd_textview, when);
		whenAdapter.clear();
		whenSearchSpinner.setAdapter(whenAdapter);
		for (WhenForSearch when : SearchHelper.getWhenList()) {
			whenAdapter.add(when);
		}
		whenAdapter.notifyDataSetChanged();
		whereLabel = (TextView) getView().findViewById(R.id.where_label);
		whereSearchSpinner = (Spinner) getView().findViewById(R.id.where_spinner);
		whereAdapter = new WhereAdapter(getActivity(), R.layout.dd_list, R.id.dd_textview, where);
		whereAdapter.clear();
		whereSearchSpinner.setAdapter(whereAdapter);
		for (WhereForSearch when : SearchHelper.getWhereList()) {
			whereAdapter.add(when);
		}
		whereAdapter.notifyDataSetChanged();

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getActivity().getMenuInflater().inflate(R.menu.gripmenu, menu);

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	private class WhereAdapter extends ArrayAdapter<WhereForSearch> {
		private final int SPINNER_ENABLED = R.layout.simple_spinner_item_enabled;
		private final int SPINNER_DISABLED = R.layout.simple_spinner_item_disabled;

		public WhereAdapter(Context context, int resource, int textViewResourceId, List<WhereForSearch> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public final View getView(int position, View convertView, ViewGroup parent) {
			View newView = null;
			TextView someText;

			LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (isonline)
				newView = inflater.inflate(SPINNER_ENABLED, null);
			else {
				newView = inflater.inflate(SPINNER_DISABLED, null);
				parent.setEnabled(false);
			}

			TextView textView = (TextView) newView.findViewById(R.id.text1);
			textView.setText(getItem(position).toString());
			return newView;
		}

	}
}
