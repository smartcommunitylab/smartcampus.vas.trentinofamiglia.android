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
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CommentsHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.ExplorerObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.POIHelper;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class PoiDetailsFragment extends Fragment {

	public static final String ARG_POI_ID = "poi_id";

	POIObject mPoi = null;
	String mPoiId;

	private Fragment mFragment = this;

	private CommentsHandler commentsHandler;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mPoiId = getArguments().getString(ARG_POI_ID);
			mPoi = DTHelper.findPOIById(mPoiId);
		}
	}

	private POIObject getPOI() {
		if (mPoiId == null) {
			mPoiId = getArguments().getString(ARG_POI_ID);
		}

		mPoi = DTHelper.findPOIById(mPoiId);
		return mPoi;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.poidetails, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getPOI() != null) {
			ImageView certifiedIcon = (ImageView) this.getView().findViewById(R.id.poi_details_icon);
			ImageView bannerCertifiedIcon = (ImageView) this.getView().findViewById(R.id.banner_certified);

			Drawable icon = null;
			if (CategoryHelper.CAT_POI_FAMILY_IN_TRENTINO.equals(mPoi.getType())){
				icon = POIHelper.getDrawablePoiFamilyTrentinoDetail(getActivity(),mPoi);
				bannerCertifiedIcon.setVisibility(View.VISIBLE);
			}
			else if (CategoryHelper.CAT_POI_FAMILY_AUDIT.equals(mPoi.getType())){
				icon = POIHelper.getDrawablePoiFamilyAuditDetail(getActivity(),mPoi);
				bannerCertifiedIcon.setVisibility(View.VISIBLE);

			}
			certifiedIcon.setImageDrawable(icon);

			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.poi_details_title);
			tv.setText(mPoi.getTitle());

			/*
			 * BUTTONS
			 */


			// map
			ImageButton mapBtn = (ImageButton) getView().findViewById(R.id.poidetails_map);
			
			mapBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
					list.add(mPoi);
					MapManager.switchToMapView(list, mFragment);
				}
			});

			// directions
			ImageButton directionsBtn = (ImageButton) getView().findViewById(R.id.poidetails_directions);
			directionsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Address to = Utils.getPOIasGoogleAddress(mPoi);
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
			
			if(mPoi.getLocation()[0]==0 && mPoi.getLocation()[1]==0){
				mapBtn.setVisibility(View.INVISIBLE);
				directionsBtn.setVisibility(View.GONE);
			}
			
			/*
			 * END BUTTONS
			 */

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.poi_details_descr);
			String customDesc = POIHelper.customDescription(mPoi, getActivity());
			if (customDesc != null && customDesc.length() > 0) {
				tv.setText(Html.fromHtml(customDesc));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.poidetails)).removeView(tv);
			}

//			// notes
//			tv = (TextView) this.getView().findViewById(R.id.poi_details_notes);
//			((LinearLayout) this.getView().findViewById(R.id.poidetails)).removeView(tv);

			// location
			tv = (TextView) this.getView().findViewById(R.id.poi_details_loc);
			tv.setText(Html.fromHtml("<a href=\"\">" + Utils.getPOIshortAddress(mPoi) + "</a> "));
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
					list.add(mPoi);
					MapManager.switchToMapView(list, PoiDetailsFragment.this);
				}
			});

			commentsHandler  = new CommentsHandler(getPOI(), getActivity(), getView()); 

		}
	}

	private boolean isCertified(POIObject poi) {
		String status = (String) poi.getCustomData().get("status");
		if (("Certificato finale").equals(status) || ("Certificato base").equals(status)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.submenu_show_related_events) {
			/*
			 * It would be great if we could already know whether there are any
			 * events related to this POI: in case there were not, this entry in
			 * the menu could be omitted
			 */
			FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
			EventsListingFragment fragment = new EventsListingFragment();
			Bundle args = new Bundle();
			args.putString(EventsListingFragment.ARG_POI, mPoi.getId());
			args.putString(EventsListingFragment.ARG_POI_NAME, mPoi.getTitle());
			fragment.setArguments(args);
			fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// fragmentTransaction.detach(this);
			fragmentTransaction.replace(R.id.frame_content, fragment, "pois");
			fragmentTransaction.addToBackStack(fragment.getTag());
			fragmentTransaction.commit();
			return true;
		} 
//		else if (item.getItemId() == R.id.submenu_edit || item.getItemId() == R.id.submenu_tag) {
//			// if (new
//			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
//			// // show dialog box
//			// UserRegistration.upgradeuser(getSherlockActivity());
//			// return false;
//			// } else
//			{
//				FragmentTransaction fragmentTransaction = getSherlockActivity().getSupportFragmentManager()
//						.beginTransaction();
//				Fragment fragment = new CreatePoiFragment();
//				Bundle args = new Bundle();
//				args.putSerializable(CreatePoiFragment.ARG_POI, mPoi);
//				fragment.setArguments(args);
//				fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//				// fragmentTransaction.detach(this);
//				fragmentTransaction.replace(android.R.id.content, fragment, "pois");
//				fragmentTransaction.addToBackStack(fragment.getTag());
//				fragmentTransaction.commit();
//				return true;
//			}
//		} else if (item.getItemId() == R.id.submenu_delete) {
//			// if (new
//			// AMSCAccessProvider().isUserAnonymous(getSherlockActivity())) {
//			// // show dialog box
//			// UserRegistration.upgradeuser(getSherlockActivity());
//			// return false;
//			// } else
//			{
//				new SCAsyncTask<POIObject, Void, Boolean>(getActivity(), new POIDeleteProcessor(getActivity()))
//						.execute(mPoi);
//				return true;
//			}
//		} 
		else {
			return super.onOptionsItemSelected(item);
		}
	}

//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (requestCode == 3000) {
//			if (resultCode == Activity.RESULT_OK) {
//				mStart = false;
//				Topic topic = (Topic) data.getSerializableExtra("topic");
//				new FollowAsyncTask().execute(topic.getId());
//				// fix to avoid onActivityResult DiscoverTrentoActivity failure
//				data.putExtra(AccountManager.KEY_AUTHTOKEN, DTHelper.getAuthToken());
//			} else {
//				getPOI().getCommunityData().getFollowing().clear();
//				mCanceledFollow = true;
//			}
//		}
//		super.onActivityResult(requestCode, resultCode, data);
//	}


//	class FollowAsyncTask extends AsyncTask<String, Void, Void> {
//
//		@Override
//		protected Void doInBackground(String... params) {
//			try {
//				DTHelper.follow(DTHelper.findPOIById(mPoiId));
//			} catch (Exception e) {
//				Log.e(FollowAsyncTask.class.getName(), String.format("Exception following event %s", mPoiId));
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			// getSherlockActivity().invalidateOptionsMenu();
//			if (followButtonView != null) {
//				followButtonView.setBackgroundResource(R.drawable.ic_btn_monitor_on);
//				followButtonView = null;
//			}
//			mStart = true;
//		}
//
//	}

}
