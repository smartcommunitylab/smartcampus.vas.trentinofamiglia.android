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
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObject;


public class InfoAdapter extends ArrayAdapter<InfoObject> {

	private Context context;
	private int layoutResourceId;
	private int elementSelected = -1;

	public InfoAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		InfoPlaceholder p = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			p = new InfoPlaceholder();
			p.title = (TextView) row.findViewById(R.id.info_placeholder_title);
			// p.description = (TextView)
			// row.findViewById(R.id.info_placeholder_descr);
			p.icon = (ImageView) row.findViewById(R.id.info_placeholder_icon);

			p.subtitle = (TextView) row.findViewById(R.id.info_placeholder_subtitle);
			row.setTag(p);
		} else
			p = (InfoPlaceholder) row.getTag();

		p.info = getItem(position);// data[position];
		p.title.setText(p.info.getTitle());
		String subtitle = p.info.subtitle();
		if (subtitle != null) p.subtitle.setText(subtitle);
		else p.subtitle.setVisibility(View.GONE);
		Drawable drawable = context.getResources().getDrawable(CategoryHelper.getIconByType(p.info.getType()));
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
