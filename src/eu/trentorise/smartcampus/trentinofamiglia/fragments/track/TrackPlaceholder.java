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

import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;

public class TrackPlaceholder {
	public TrackObject track;
	public TextView title, notes, location, tags, source; // description;
	public ImageView icon;
	public RatingBar rating;

}
