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
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CommentsHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.ExplorerObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class TrackDetailsFragment extends Fragment {

	public static final String ARG_TRACK_ID = "track_id";

	TrackObject mTrack = null;
	String mTrackId;

	private Fragment mFragment = this;

	private CommentsHandler commentsHandler;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mTrackId = getArguments().getString(ARG_TRACK_ID);
			mTrack = DTHelper.findTrackById(mTrackId);
		}
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
		return inflater.inflate(R.layout.trackdetails, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getTrack() != null) {
			ImageView certifiedIcon = (ImageView) this.getView().findViewById(R.id.track_details_icon);
			Drawable icon = null;
			certifiedIcon.setImageDrawable(getResources().getDrawable(CategoryHelper.getIconByType(mTrack.getType())));

			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.track_details_title);
			tv.setText(mTrack.getTitle());

			/*
			 * BUTTONS
			 */

			// map
			ImageButton mapBtn = (ImageButton) getView().findViewById(R.id.trackdetails_map);
			mapBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
					list.add(mTrack);
					MapManager.switchToMapView(list, mFragment);
				}
			});

			// directions
			ImageButton directionsBtn = (ImageButton) getView().findViewById(R.id.trackdetails_directions);
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
			tv = (TextView) this.getView().findViewById(R.id.track_details_descr);
			String customDescr = mTrack.customDescription(getActivity());
			if (customDescr != null && customDescr.length() > 0) {
				tv.setText(Html.fromHtml(customDescr));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.trackdetails)).removeView(tv);
			}


			commentsHandler  = new CommentsHandler(getTrack(), getActivity(), getView()); 
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
