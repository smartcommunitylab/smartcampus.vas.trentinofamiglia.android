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
package eu.trentorise.smartcampus.trentinofamiglia.map;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventDetailsFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoiDetailsFragment;

public class InfoDialog extends DialogFragment {
	public static final String PARAM = "DTO_OBJECT";
	private BaseDTObject data;

	
	 public InfoDialog() {
	 }

//	 public InfoDialog(BaseDTObject o) {
//	 this.data = o;
//	 }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.data == null) {
			this.data = (BaseDTObject) getArguments().getSerializable(PARAM);
		}
		if (data instanceof POIObject) {
			getDialog().setTitle(getString(R.string.info_dialog_title_poi));
		} else if (data instanceof LocalEventObject) {
			getDialog().setTitle(R.string.info_dialog_title_event);
		}
		return inflater.inflate(R.layout.mapdialog, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		TextView msg = (TextView) getDialog().findViewById(R.id.mapdialog_msg);

		if (data instanceof POIObject) {
			msg.setText(Html.fromHtml("<h2>" + ((POIObject) data).getTitle() + "</h2><br/><p>"
					+ Utils.getPOIshortAddress(((POIObject) data)) + "</p>"));
		} else if (data instanceof LocalEventObject) {
			LocalEventObject event = (LocalEventObject) data;
			POIObject poi = DTHelper.findPOIById(event.getPoiId());
			String msgText = "";
			msgText += "<h2>";
			msgText += event.getTitle();
			msgText += "</h2><br/><p>";
			if (event.getType() != null) {
				msgText += "<p>";
				msgText += getString(CategoryHelper.getCategoryDescriptorByCategoryFiltered(
						CategoryHelper.CATEGORY_TYPE_EVENTS, event.getType()).description);
				msgText += "</p><br/>";
			}
			msgText += "<p>" + event.getTiming() + "</p>";
			if (poi != null) {
				msgText += "<p>" + Utils.getPOIshortAddress(poi) + "</p>";
			}
			msg.setText(Html.fromHtml(msgText));
		}

		msg.setMovementMethod(new ScrollingMovementMethod());

		Button b = (Button) getDialog().findViewById(R.id.mapdialog_cancel);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});

		b = (Button) getDialog().findViewById(R.id.mapdialog_ok);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager()
						.beginTransaction();
				Bundle args = new Bundle();

				if (data instanceof POIObject) {
					PoiDetailsFragment fragment = new PoiDetailsFragment();
					args.putString(PoiDetailsFragment.ARG_POI_ID, data.getId());
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(R.id.frame_content, fragment, "me");
					fragmentTransaction.addToBackStack(fragment.getTag());
				} else if (data instanceof LocalEventObject) {
					EventDetailsFragment fragment = new EventDetailsFragment();
					args.putString(EventDetailsFragment.ARG_EVENT_ID, (data.getId()));
					fragment.setArguments(args);
					fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					fragmentTransaction.replace(R.id.frame_content, fragment, "me");
					fragmentTransaction.addToBackStack(fragment.getTag());
				}
				fragmentTransaction.commit();
				getDialog().dismiss();
			}
		});

	}
}
