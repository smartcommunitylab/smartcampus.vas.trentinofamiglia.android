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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;


// in EventsListingFragment
public class EventAdapter extends ArrayAdapter<LocalEventObject> {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private static final DateFormat extDateFormat = new SimpleDateFormat("EEEEEE dd/MM/yyyy");
	private Context context;
	private int layoutResourceId;
	private int elementSelected = -1;
	private boolean postProcAndHeader = true;
	public EventAdapter(Context context, int layoutResourceId, boolean postProcAndHeader) {
		super(context, layoutResourceId);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.postProcAndHeader = postProcAndHeader; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		EventPlaceholder e = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(layoutResourceId, parent, false);
			e = new EventPlaceholder();
			e.title = (TextView) row.findViewById(R.id.event_placeholder_title);
			e.location = (TextView) row.findViewById(R.id.event_placeholder_loc);
			e.hour = (TextView) row.findViewById(R.id.event_placeholder_hour);
			e.icon = (ImageView) row.findViewById(R.id.event_placeholder_icon);
			e.dateSeparator = (TextView) row.findViewById(R.id.date_separator);
			row.setTag(e);
		} else
		{
			e = (EventPlaceholder) row.getTag();
		}
		
		e.event = getItem(position);
		e.title.setText(e.event.getTitle());
		if (e.event.getCustomData() != null && e.event.getCustomData().get("place")!=null) {
			e.location.setText((CharSequence) e.event.getCustomData().get("place"));
		} else {
			e.location.setText(null);
		}
		e.hour.setText(e.event.dateTimeString());
		//e.hour.setText(e.event.getTimingFormatted());
		Drawable drawable = context.getResources().getDrawable(CategoryHelper.getIconByType(e.event.getType()));

		if (CategoryHelper.FAMILY_CATEGORY_EVENT.equals(e.event.getType()))
			drawable = eventCertified(e.event);
		e.icon.setImageDrawable(drawable);
		// Choose if show the separator or not
		LocalEventObject event = getItem(position);

		Calendar previousEvent = null;
		Calendar currentEvent = Calendar.getInstance();
		;
		currentEvent.setTimeInMillis(event.getFromTime());

		if (position - 1 >= 0) {
			previousEvent = Calendar.getInstance();
			previousEvent.setTimeInMillis(getItem(position - 1).getFromTime());
		}

		if ((previousEvent == null || previousEvent.get(Calendar.DATE) != currentEvent.get(Calendar.DATE)) && postProcAndHeader) {
			e.dateSeparator.setVisibility(View.VISIBLE);
			// create date
			e.dateSeparator.setText(setDateString(e));
		} else {
			e.dateSeparator.setVisibility(View.GONE);
		}

		return row;
	}

	private Drawable eventCertified(LocalEventObject o) {
		
		if (o.getCustomData()!=null && ((Boolean) o.getCustomData().get("certified"))) {
			/* se ceretificato e evento */
			return context.getResources().getDrawable(R.drawable.ic_e_family_certified);
		}

		return context.getResources().getDrawable(CategoryHelper.getIconByType(o.getType()));
	}

	private String setDateString(EventPlaceholder e) {
		String newdateformatted = new String("");

		Date dateToday = new Date();
		String stringToday = (dateFormat.format(dateToday));
		String stringEvent = (dateFormat.format(new Date(e.event.getFromTime())));

		Calendar cal = Calendar.getInstance();
		cal.setTime(dateToday);
		cal.add(Calendar.DAY_OF_YEAR, 1); // <--
		Date tomorrow = cal.getTime();
		String stringTomorrow = (dateFormat.format(tomorrow));
		// check actual date
		if (stringToday.equals(stringEvent)) {
			// if equal put the Today string
			newdateformatted = stringToday;
			newdateformatted = this.context.getString(R.string.list_event_today) + " " + newdateformatted;
		} else if (stringTomorrow.equals(stringEvent)) {
			// else if it's tomorrow, cat that string
			newdateformatted = stringTomorrow;
			newdateformatted = this.context.getString(R.string.list_event_tomorrow) + " " + newdateformatted;
		}
		// else put the day's name
		else
			newdateformatted = extDateFormat.format(new Date(e.event.getFromTime()));
		return newdateformatted;
	}

	public int getElementSelected() {
		return elementSelected;
	}

	public void setElementSelected(int elementSelected) {
		this.elementSelected = elementSelected;
	}

}
