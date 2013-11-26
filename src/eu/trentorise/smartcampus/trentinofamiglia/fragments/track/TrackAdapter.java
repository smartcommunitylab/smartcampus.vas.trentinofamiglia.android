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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;

public class TrackAdapter extends ArrayAdapter<TrackObject> {

	private Context context;
	private int layoutResourceId;
	private int elementSelected = -1;

	public TrackAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TrackPlaceholder p = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			p = new TrackPlaceholder();
			p.title = (TextView) row.findViewById(R.id.track_placeholder_title);
			// p.description = (TextView)
			// row.findViewById(R.id.track_placeholder_descr);
			p.icon = (ImageView) row.findViewById(R.id.track_placeholder_icon);

			p.location = (TextView) row.findViewById(R.id.track_placeholder_loc);
			row.setTag(p);
		} else
			p = (TrackPlaceholder) row.getTag();

		p.track = getItem(position);// data[position];
		p.title.setText(p.track.getTitle());
		Drawable drawable = context.getResources().getDrawable(CategoryHelper.getIconByType(p.track.getType()));
		p.icon.setImageDrawable(drawable);

		return row;
	}

	public int getElementSelected() {
		return elementSelected;
	}

	public void setElementSelected(int elementSelected) {
		this.elementSelected = elementSelected;
	}


}
