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
package eu.trentorise.smartcampus.trentinofamiglia.fragments.info;

import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.CommunityData;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.RatingHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObject;

public class InfoDetailsFragment extends Fragment {

	public static final String ARG_INFO_ID = "info_id";

	InfoObject mInfo = null;
	String mInfoId;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mInfoId = getArguments().getString(ARG_INFO_ID);
			mInfo = DTHelper.findInfoById(mInfoId);
		}
	}

	private InfoObject getInfo() {
		if (mInfoId == null) {
			mInfoId = getArguments().getString(ARG_INFO_ID);
		}

		mInfo = DTHelper.findInfoById(mInfoId);
		return mInfo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.infodetails, container, false);
	}

	private void updateRating() {
		if (getView() != null) {
			RatingBar rating = (RatingBar) getView().findViewById(R.id.info_rating);
			if (mInfo.getCommunityData() != null) {
				CommunityData cd = mInfo.getCommunityData();

				if (cd.getRating() != null && !cd.getRating().isEmpty()) {
					Iterator<Map.Entry<String, Integer>> entries = cd.getRating().entrySet().iterator();
					float rate = 0;
					while (entries.hasNext()) {
						Map.Entry<String, Integer> entry = entries.next();
						rate = entry.getValue();
					}
					rating.setRating(rate);
				}

				// user rating

				// total raters
				((TextView) getView().findViewById(R.id.info_rating_raters)).setText(getString(
						R.string.ratingtext_raters, cd.getRatingsCount()));

				// averange rating
				((TextView) getView().findViewById(R.id.info_rating_average)).setText(getString(
						R.string.ratingtext_average, cd.getAverageRating()));
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getInfo() != null) {
			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.info_details_title);
			tv.setText(mInfo.getTitle());

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.info_details_descr);
			String customDescr = mInfo.customDescription(getActivity());
			if (customDescr != null && customDescr.length() > 0) {
				tv.setText(Html.fromHtml(customDescr));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.infodetails)).removeView(tv);
			}


			// rating
			RatingBar rating = (RatingBar) getView().findViewById(R.id.info_rating);
			rating.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						{
							ratingDialog();
						}
					}
					return true;
				}
			});

			updateRating();
//			if (tmp_comments.length > 0) {
//				// Comments
//				LinearLayout commentsList = (LinearLayout) getView().findViewById(R.id.comments_list);
//				for (int i = 0; i < tmp_comments.length; i++) {
//					View entry = getLayoutInflater(this.getArguments()).inflate(R.layout.comment_row, null);
//
//					TextView tmp = (TextView) entry.findViewById(R.id.comment_text);
//					tmp.setText(tmp_comments[i].getText());
//					tmp = (TextView) entry.findViewById(R.id.comment_author);
//					tmp.setText(tmp_comments[i].getAuthor());
//					tmp = (TextView) entry.findViewById(R.id.comment_date);
//					tmp.setText(tmp_comments[i].getDate());
//					commentsList.addView(entry);
//				}
//			} else {
				((LinearLayout) getView().findViewById(R.id.infodetails)).removeView(getView().findViewById(
						R.id.info_comments));
				((LinearLayout) getView().findViewById(R.id.infodetails)).removeView(getView().findViewById(
						R.id.comments_list));
				((LinearLayout) getView().findViewById(R.id.infodetails)).removeView(getView().findViewById(
						R.id.info_comments_separator));
//			}

		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getActivity().getMenuInflater().inflate(R.menu.gripmenu, menu);
		// String userId = DTHelper.getUserId();

		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();

		submenu.add(Menu.NONE, R.id.submenu_show_related_events, Menu.NONE, R.string.submenu_related_events);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private void ratingDialog() {
		float rating = (mInfo != null && mInfo.getCommunityData() != null && mInfo.getCommunityData().getAverageRating() > 0) ? mInfo
				.getCommunityData().getAverageRating() : 2.5f;
		RatingHelper.ratingDialog(getActivity(), rating, new RatingProcessor(getActivity()),
				R.string.rating_place_dialog_title);
	}

	/*
	 * CLASSES
	 */
	private class RatingProcessor extends AbstractAsyncTaskProcessor<Integer, Integer> implements RatingHandler {
		public RatingProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public Integer performAction(Integer... params) throws SecurityException, Exception {
			return DTHelper.rate(mInfo, params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			mInfo = null;
			getInfo();
			updateRating();
			Toast.makeText(getActivity(), R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this).execute((int) rating);
		}
	}
}
