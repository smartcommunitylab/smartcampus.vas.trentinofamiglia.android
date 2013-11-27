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

package eu.trentorise.smartcampus.trentinofamiglia.custom;

import com.google.android.gms.internal.ac;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.CommunityData;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.ReviewHelper.ReviewHandler;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.Review;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.ReviewListFragment;

/**
 * @author raman
 *
 */
public class CommentsHandler {

	private boolean commentsVisible = false;
	
	private FragmentActivity activity = null;
	private View main;
	
	private BaseDTObject object = null;

	/**
	 * @param ctx
	 * @param toggle
	 * @param container
	 */
	public CommentsHandler(BaseDTObject object, FragmentActivity activity, View main) {
		super();
		this.activity = activity;
		this.main = main;
		TextView toggle = (TextView)main.findViewById(R.id.comments_tv);
		final ImageView toggleButton = (ImageView) main.findViewById(R.id.comments_button);
		this.object = object;
		
		OnClickListener commentsListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				commentsVisible = !commentsVisible;
				if (commentsVisible) {
					loadComments();
				}
			}
		};
		toggle.setOnClickListener(commentsListener);
		toggleButton.setOnClickListener(commentsListener);
		
		RatingBar rating = (RatingBar) main.findViewById(R.id.rating);
		rating.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					ratingDialog();
				}
				return true;
			}
		});

		updateRating();
	}


	private void ratingDialog() {
		if (!signedIn()) {
			return;
		}
		float rating = (object != null && object.getCommunityData() != null && object.getCommunityData()
				.getAverageRating() > 0) ? object.getCommunityData().getAverageRating() : 2.5f;
		ReviewHelper.reviewDialog(activity, rating, new ReviewProcessor(activity),
				R.string.rating_event_dialog_title);
	}

	private void updateRating() {
		if (main != null) {
			RatingBar rating = (RatingBar) main.findViewById(R.id.rating);
			if (object.getCommunityData() != null) {
				CommunityData cd = object.getCommunityData();
				rating.setRating(cd.getAverageRating());

				// total raters
				((TextView) main.findViewById(R.id.rating_raters)).setText(activity.getString(R.string.ratingtext_raters,""+cd.getRatingsCount()));
			}
		}

	}

	protected boolean signedIn() {
		SCAccessProvider provider = SCAccessProvider.getInstance(activity);
		try {
			if (provider.isLoggedIn(activity)) {
				return true;
			}
			showLoginDialog(provider);
		} catch (AACException e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	private void showLoginDialog(final SCAccessProvider accessprovider) {
		// dialogbox for registration
		DialogInterface.OnClickListener updateDialogClickListener;

		updateDialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					try {
						accessprovider.login(activity, null);
					} catch (AACException e) {
						e.printStackTrace();
					}
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(false);
		builder.setMessage(activity.getString(R.string.auth_required))
				.setPositiveButton(android.R.string.yes, updateDialogClickListener)
				.setNegativeButton(android.R.string.no, updateDialogClickListener).show();
	}


	private void loadComments() {
		FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
		ReviewListFragment fragment = new ReviewListFragment();
		fragment.setArguments(ReviewListFragment.prepareArgs(object));

		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		fragmentTransaction.replace(R.id.frame_content, fragment);
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();
	}

	private class ReviewProcessor extends AbstractAsyncTaskProcessor<Review, CommunityData> implements ReviewHandler {

		public ReviewProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public CommunityData performAction(Review... params) throws SecurityException, Exception {
			return DTHelper.review(object, params[0]);
		}

		@Override
		public void handleResult(CommunityData result) {
			object.setCommunityData(result);
			updateRating();
			if (activity != null)
				Toast.makeText(activity, R.string.rating_success, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onReviewChanged(Review review) {
			new SCAsyncTask<Review, Void, CommunityData>(activity, this).execute(review);
		}
	}

}
