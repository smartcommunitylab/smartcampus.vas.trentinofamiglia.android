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
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CommentsHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class EventDetailsFragment extends Fragment {
	public static final String ARG_EVENT_ID = "event_id";


	private LocalEventObject mEvent = null;
	private String mEventId;

	private Fragment mFragment = this;

	private CommentsHandler commentsHandler = null;
	
	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setHasOptionsMenu(true);

		if (getArguments() != null) {
			mEventId = getArguments().getString(ARG_EVENT_ID);
			mEvent = DTHelper.findEventById(mEventId);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.eventdetails, container, false);
	}

//	private POIObject getPOI() {
//		if (mPoi == null) {
//			getEvent();
//		}
//		return mPoi;
//	}

	private LocalEventObject getEvent() {
		if (mEventId == null) {
			mEventId = getArguments().getString(ARG_EVENT_ID);
		}

		if (mEvent == null) {
			mEvent = DTHelper.findEventById(mEventId);
		}
//		if (mEvent != null) {
//			mPoi = DTHelper.findPOIById(mEvent.getPoiId());
//			mEvent.assignPoi(mPoi);
//		}

		return mEvent;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mEvent != null) {
			ImageView certifiedBanner = (ImageView) this.getView().findViewById(R.id.banner_certified);
			if (CategoryHelper.FAMILY_CATEGORY_EVENT.equals(mEvent.getType()) && isCertified(mEvent))
				certifiedBanner.setVisibility(View.VISIBLE);
			else
				certifiedBanner.setVisibility(View.GONE);

			// title
			TextView tv = (TextView) this.getView().findViewById(R.id.event_details_title);
			tv.setText(mEvent.getTitle());

			/*
			 * BUTTONS
			 */
			// follow/unfollow
			// if (mStart) {
			// ToggleButton followTbtn = (ToggleButton)
			// this.getView().findViewById(R.id.event_details_follow_tbtn);
			// if
			// (getEvent().getCommunityData().getFollowing().containsKey(DTHelper.getUserId()))
			// {
			// followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_on);
			// followTbtn.setChecked(true);
			// } else {
			// followTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_off);
			// followTbtn.setChecked(false);
			// }
			//
			// followTbtn.setOnCheckedChangeListener(new
			// OnCheckedChangeListener() {
			// @Override
			// public void onCheckedChanged(CompoundButton buttonView, boolean
			// isChecked) {
			// if (!mCanceledFollow) {
			// if (isChecked) {
			// // FOLLOW
			// {
			// SCAsyncTask<Object, Void, BaseDTObject> followTask = new
			// SCAsyncTask<Object, Void, BaseDTObject>(
			// getActivity(), new FollowAsyncTaskProcessor(getActivity(),
			// buttonView));
			// followTask.execute(mEvent);
			// }
			// } else {
			// // UNFOLLOW
			// SCAsyncTask<BaseDTObject, Void, BaseDTObject> unfollowTask = new
			// SCAsyncTask<BaseDTObject, Void, BaseDTObject>(
			// getActivity(), new UnfollowAsyncTaskProcessor(getActivity(),
			// buttonView));
			// unfollowTask.execute(mEvent);
			//
			// }
			// } else {
			// mCanceledFollow = false;
			// }
			// }
			// });
			// }
			//
			// // attend
			// ToggleButton attendTbtn = (ToggleButton)
			// this.getView().findViewById(R.id.event_details_attend_tbtn);
			// if (getEvent().getAttending() == null ||
			// getEvent().getAttending().isEmpty()) {
			// attendTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_off);
			// attendTbtn.setChecked(false);
			// } else {
			// attendTbtn.setBackgroundResource(R.drawable.ic_btn_monitor_on);
			// attendTbtn.setChecked(true);
			// }
			//
			// attendTbtn.setOnCheckedChangeListener(new
			// OnCheckedChangeListener() {
			// @Override
			// public void onCheckedChanged(CompoundButton buttonView, boolean
			// isChecked) {
			// // if (new
			// // AMSCAccessProvider().isUserAnonymous(getActivity()))
			// // {
			// // // show dialog box
			// // UserRegistration.upgradeuser(getActivity());
			// // } else
			// {
			// new SCAsyncTask<Boolean, Void, LocalEventObject>(getActivity(),
			// new AttendProcessor(
			// getActivity(), buttonView)).execute(getEvent().getAttending() ==
			// null
			// || getEvent().getAttending().isEmpty());
			// }
			// }
			// });

			// map
			ImageButton mapBtn = (ImageButton) getView().findViewById(R.id.event_details_map);
			mapBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEvent.getLocation() != null) {
						ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
						getEvent().setLocation(mEvent.getLocation());
						list.add(getEvent());
						MapManager.switchToMapView(list, mFragment);
					} else {
						Toast.makeText(getActivity(), R.string.toast_poi_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			});

			// directions
			ImageButton directionsBtn = (ImageButton) getView().findViewById(R.id.event_details_directions);
			directionsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mEvent.getLocation() != null) {

						bringMeThere(getEvent());
					} else {
						Toast.makeText(getActivity(), R.string.toast_poi_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			});
			/*
			 * END BUTTONS
			 */

			// location
			tv = (TextView) this.getView().findViewById(R.id.event_details_loc);
//			POIObject poi = getPOI();
			if (mEvent.getCustomData() != null && mEvent.getCustomData().get("place")!=null) {
				//esegui get address information
					tv.setText((CharSequence) mEvent.getCustomData().get("place"));
					} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// timing
			tv = (TextView) this.getView().findViewById(R.id.event_timing);
			if (getEvent().getTiming() != null && mEvent.getTiming().length() > 0) {
				tv.setText(mEvent.getTimingFormatted());
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// description, optional
			tv = (TextView) this.getView().findViewById(R.id.event_details_descr);
			String customDesc = mEvent.customDescription(getActivity());
			if (customDesc != null && customDesc.length() > 0) {
				tv.setText(Html.fromHtml(customDesc));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// notes
			tv = (TextView) this.getView().findViewById(R.id.event_details_notes);
			// if (mEvent.getCommunityData() != null &&
			// mEvent.getCommunityData().getNotes() != null
			// && mEvent.getCommunityData().getNotes().length() > 0) {
			// tv.setText(mEvent.getCommunityData().getNotes());

			// } else {
			((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			// }

			// tags
			tv = (TextView) this.getView().findViewById(R.id.event_details_tags);
			if (mEvent.getCommunityData() != null && mEvent.getCommunityData().getTags() != null
					&& mEvent.getCommunityData().getTags().size() > 0) {
				tv.setText(Utils.conceptToSimpleString(mEvent.getCommunityData().getTags()));
			} else {
				((LinearLayout) this.getView().findViewById(R.id.eventdetails)).removeView(tv);
			}

			// date
			tv = (TextView) this.getView().findViewById(R.id.event_details_date);
			if (mEvent.getFromTime() != null && mEvent.getFromTime() > 0) {
				CharSequence fromTime = mEvent.dateTimeString();
				CharSequence toTime = mEvent.toDateTimeString();
				if (fromTime.equals(toTime)) {
					tv.setText(fromTime);
				} else {
					tv.setText(fromTime + " - " + toTime);
				}
			} else {
				tv.setText("");
			}
			commentsHandler  = new CommentsHandler(getEvent(), getActivity(), getView(), getLayoutInflater(getArguments())); 
		}

	}


	private boolean isCertified(LocalEventObject event) {
		if (event.getCustomData() != null && (Boolean) event.getCustomData().get("certified"))
			return true;
		else
			return false;

	}

	// private void updateAttending() {
	// TextView tv;
	// if (this.getView() != null) {
	// // attendees
	// tv = (TextView) this.getView().findViewById(R.id.attendees_num);
	// if (getEvent().getAttendees() != null) {
	// tv.setText(getEvent().getAttendees() + " " +
	// getString(R.string.attendees_extended));
	// } else {
	// tv.setText("0 " + getString(R.string.attendees_extended));
	// }
	// }
	// }

	/*
	 * private boolean hasMultimediaAttached() { return true; }
	 */


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
			return super.onOptionsItemSelected(item);
	}

	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// if (requestCode == 3000) {
	// if (resultCode == Activity.RESULT_OK) {
	// Topic topic = (Topic) data.getSerializableExtra("topic");
	// new FollowAsyncTask().execute(topic.getId());
	// // fix to avoid onActivityResult DiscoverTrentoActivity failure
	// data.putExtra(AccountManager.KEY_AUTHTOKEN, DTHelper.getAuthToken());
	// mStart = false;
	// } else {
	// getEvent().getCommunityData().getFollowing().clear();
	// mCanceledFollow = true;
	// }
	// }
	// super.onActivityResult(requestCode, resultCode, data);
	// }

	@Override
	public void onResume() {
		// getActivity().invalidateOptionsMenu();
		super.onResume();
	}

	private void bringMeThere(LocalEventObject eventObject) {
		AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(getActivity());
		/* check event Object */
		if (!CategoryHelper.FAMILY_CATEGORY_EVENT.equals(eventObject.getType())) {
			/* if it's not a family event, no problem */
			callBringMeThere();
			return;
		} else {
			/* if it is, show the dialog box */
			/* press true return true, press false return false */
			DialogInterface.OnClickListener updateDialogClickListener;

			updateDialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// upgrade the user
						callBringMeThere();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// CLOSE

						break;

					}

				}
			};
			builder.setCancelable(false).setMessage(getActivity().getString(R.string.warning_for_direction))
					.setPositiveButton(android.R.string.yes, updateDialogClickListener)
					.setNegativeButton(R.string.cancel, updateDialogClickListener).show();
		}
		return;
	}

	/**
	 * 
	 */
	protected void callBringMeThere() {
		Address to = new Address(Locale.getDefault());
		to.setLatitude(mEvent.getLocation()[0]);
		to.setLongitude(mEvent.getLocation()[1]);
		Address from = null;
		GeoPoint mylocation = MapManager.requestMyLocation(getActivity());
		if (mylocation != null) {
			from = new Address(Locale.getDefault());
			from.setLatitude(mylocation.getLatitudeE6() / 1E6);
			from.setLongitude(mylocation.getLongitudeE6() / 1E6);
		}
		DTHelper.bringmethere(getActivity(), from, to);

	}


}
