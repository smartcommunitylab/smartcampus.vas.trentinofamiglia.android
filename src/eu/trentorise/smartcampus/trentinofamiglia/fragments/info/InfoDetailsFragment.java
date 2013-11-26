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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CommentsHandler;
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
