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

import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;

// contains all info about the event. To be passed to the listing and details fragments.
public class EventPlaceholder {

	public LocalEventObject event;
	public TextView dateSeparator;
	public int displayChild=0;
	public TextView title, description, notes, location, hour, tags, source, attendees;
	public ImageView icon;
	public RatingBar rating;

}
