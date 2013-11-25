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
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.CommunityData;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.RatingHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.RatingHelper.RatingHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TmpComment;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class TrackDetailsFragment extends Fragment {

	public static final String ARG_TRACK_ID = "track_id";

	TrackObject mTrack = null;
	String mTrackId;
	private TmpComment tmp_comments[];

	private Fragment mFragment = this;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mTrackId = getArguments().getString(ARG_TRACK_ID);
			mTrack = DTHelper.findTrackById(mTrackId);
		}

		tmp_comments = new TmpComment[0];
		// tmp_comments = new TmpComment[5];
		for (int i = 0; i < tmp_comments.length; i++)
			tmp_comments[i] = new TmpComment("This is a comment about the POI", "student", new Date());
	}

	private TrackObject getTrack() {
		if (mTrackId == null) {
			mTrackId = getArguments().getString(ARG_TRACK_ID);
		}

		mTrack = DTHelper.findTrackById(mTrackId);
		return mTrack;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.poidetails, container, false);
	}

	private void updateRating() {
		if (getView() != null) {
			RatingBar rating = (RatingBar) getView().findViewById(R.id.poi_rating);
			if (mTrack.getCommunityData() != null) {
				CommunityData cd = mTrack.getCommunityData();

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
				((TextView) getView().findViewById(R.id.poi_rating_raters)).setText(getString(
						R.string.ratingtext_raters, cd.getRatingsCount()));

				// averange rating
				((TextView) getView().findViewById(R.id.poi_rating_average)).setText(getString(
						R.string.ratingtext_average, cd.getAverageRating()));
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getTrack() != null) {
			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.poi_details_title);
			tv.setText(mTrack.getTitle());

			/*
			 * BUTTONS
			 */

			// map
			ImageButton mapBtn = (ImageButton) getView().findViewById(R.id.poidetails_map);
			mapBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
					list.add(mTrack);
					MapManager.switchToMapView(list, mFragment);
				}
			});

			// directions
			ImageButton directionsBtn = (ImageButton) getView().findViewById(R.id.poidetails_directions);
			directionsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Address to = Utils.getTrackAsGoogleAddress(mTrack);
					Address from = null;
					GeoPoint mylocation = MapManager.requestMyLocation(getActivity());
					if (mylocation != null) {
						from = new Address(Locale.getDefault());
						from.setLatitude(mylocation.getLatitudeE6() / 1E6);
						from.setLongitude(mylocation.getLongitudeE6() / 1E6);
					}
					DTHelper.bringmethere(getActivity(), from, to);
				}
			});
			/*
			 * END BUTTONS
			 */

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.poi_details_descr);
			if (mTrack.getDescription() != null && mTrack.getDescription().length() > 0) {
				tv.setText(mTrack.getDescription());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails)).removeView(tv);
			}

			tv = (TextView) this.getView().findViewById(R.id.poi_details_notes);
			((LinearLayout) this.getView().findViewById(R.id.poidetails)).removeView(tv);

			// multimedia
			((LinearLayout) getView().findViewById(R.id.multimedia_source)).removeView(getView().findViewById(
					R.id.gallery_btn));
			// source
			tv = (TextView) this.getView().findViewById(R.id.poi_details_source);
			if (mTrack.getSource() != null && mTrack.getSource().length() > 0) {
				/* Source is "ou" sometimes O_o */
				tv.setText(mTrack.getSource());
			} else if (Utils.isCreatedByUser(mTrack)) {
				tv.setText(getString(R.string.source_smartcampus));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails)).removeView(tv);
			}

			// rating
			RatingBar rating = (RatingBar) getView().findViewById(R.id.poi_rating);
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
			if (tmp_comments.length > 0) {
				// Comments
				LinearLayout commentsList = (LinearLayout) getView().findViewById(R.id.comments_list);
				for (int i = 0; i < tmp_comments.length; i++) {
					View entry = getLayoutInflater(this.getArguments()).inflate(R.layout.comment_row, null);

					TextView tmp = (TextView) entry.findViewById(R.id.comment_text);
					tmp.setText(tmp_comments[i].getText());
					tmp = (TextView) entry.findViewById(R.id.comment_author);
					tmp.setText(tmp_comments[i].getAuthor());
					tmp = (TextView) entry.findViewById(R.id.comment_date);
					tmp.setText(tmp_comments[i].getDate());
					commentsList.addView(entry);
				}
			} else {
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(
						R.id.poi_comments));
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(
						R.id.comments_list));
				((LinearLayout) getView().findViewById(R.id.poidetails)).removeView(getView().findViewById(
						R.id.poi_comments_separator));
			}

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
		float rating = (mTrack != null && mTrack.getCommunityData() != null && mTrack.getCommunityData().getAverageRating() > 0) ? mTrack
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
			return DTHelper.rate(mTrack, params[0]);
		}

		@Override
		public void handleResult(Integer result) {
			mTrack = null;
			getTrack();
			updateRating();
			Toast.makeText(getActivity(), R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRatingChanged(float rating) {
			new SCAsyncTask<Integer, Void, Integer>(getActivity(), this).execute((int) rating);
		}
	}
}
