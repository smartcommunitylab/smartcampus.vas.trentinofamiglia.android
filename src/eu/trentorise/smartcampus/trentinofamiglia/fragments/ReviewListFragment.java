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

package eu.trentorise.smartcampus.trentinofamiglia.fragments;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.Review;

/**
 * @author raman
 *
 */
public class ReviewListFragment extends ListFragment {

//	private ListView list = null;
	private ReviewAdapter adapter = null;

	public static Bundle prepareArgs(BaseDTObject obj) {
		Bundle b = new Bundle();
		b.putSerializable("object", obj);
		return b;
	}
	
	private BaseDTObject getObject() {
		return (BaseDTObject)getArguments().getSerializable("object");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.reviewlist, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		list  = (ListView)getView().findViewById(android.R.id.list);
		adapter = new ReviewAdapter(getActivity());
		setListAdapter(adapter);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		TextView title = (TextView) getView().findViewById(R.id.list_title);
		title.setText(getObject().getTitle());
		new SCAsyncTask<Void, Void, List<Review>>(getActivity(), new LoadCommentsProcessor(getActivity())).execute();
	}
	
	private class LoadCommentsProcessor extends AbstractAsyncTaskProcessor<Void, List<Review>> {
		public LoadCommentsProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public List<Review> performAction(Void... params) throws SecurityException, Exception {
			return DTHelper.loadReviews(getObject().getId());
		}

		@Override
		public void handleResult(List<Review> result) {
			if (result == null || result.isEmpty()) {
//				ViewHelper.addEmptyListView(container);
			}
			adapter.clear();
			for (Review r : result) {
				adapter.add(r);
			}
			adapter.notifyDataSetChanged();
		}
	}	

}
