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

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
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

/**
 * @author raman
 *
 */
public class CommentsHandler {

	private TextView toggle = null;
	private LinearLayout container = null;
	private boolean commentsVisible = false;
	
	private Activity activity = null;
	private LayoutInflater inflatter;
	private View main;
	
	private List<Review> data = null;
	private BaseDTObject object = null;

	/**
	 * @param ctx
	 * @param toggle
	 * @param container
	 */
	public CommentsHandler(BaseDTObject object, Activity activity, View main, LayoutInflater inflatter) {
		super();
		this.activity = activity;
		this.main = main;
		this.inflatter = inflatter;
		this.toggle = (TextView)main.findViewById(R.id.comments_tv);
		this.container = (LinearLayout)main.findViewById(R.id.comments_list);
		this.object = object;
		this.toggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				commentsVisible = !commentsVisible;
				CommentsHandler.this.container.setVisibility(commentsVisible ? View.VISIBLE : View.GONE);
				if (commentsVisible) {
					loadComments(CommentsHandler.this.container);
				}
			}
		});
		
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
				((TextView) main.findViewById(R.id.rating_raters)).setText(activity.getString(
						R.string.ratingtext_raters, cd.getRatingsCount()));
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


	private void loadComments(LinearLayout commentsList) {
		new SCAsyncTask<Void, Void, List<Review>>(activity, new LoadCommentsProcessor(activity)).execute();
	}

	private class LoadCommentsProcessor extends AbstractAsyncTaskProcessor<Void, List<Review>> {
		public LoadCommentsProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Review> performAction(Void... params) throws SecurityException, Exception {
			if (data != null) return data;
			return DTHelper.loadReviews(object.getId());
		}

		@Override
		public void handleResult(List<Review> result) {
			container.removeAllViews();
			if (result == null) return;
			data = result;
			for (Review r : result) {
				View entry = inflatter.inflate(R.layout.comment_row, null);
				TextView tmp = (TextView) entry.findViewById(R.id.comment_text);
				tmp.setText(r.getComment());
				tmp = (TextView) entry.findViewById(R.id.comment_author);
				tmp.setText(r.getAuthor());
				tmp = (TextView) entry.findViewById(R.id.comment_date);
				tmp.setText(r.formattedDate());
				RatingBar rb = (RatingBar) entry.findViewById(R.id.rating);
				rb.setRating(r.getRating());
				container.addView(entry);
			}
			
			final ScrollView scroll = (ScrollView)main.findViewById(R.id.details_sv); 
			scroll.post(new Runnable() {
				@Override
				public void run() {
					scroll.fullScroll(View.FOCUS_DOWN);
				}
			});
		}
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
