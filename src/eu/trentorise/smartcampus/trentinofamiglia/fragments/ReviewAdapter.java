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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.Review;


// in EventsListingFragment
public class ReviewAdapter extends ArrayAdapter<Review> {

	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private Context context;

	public ReviewAdapter(Context context) {
		super(context, R.layout.comment_row);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ReviewPlaceholder e = null;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.comment_row, parent, false);
			e = new ReviewPlaceholder();
			e.comment = (TextView) row.findViewById(R.id.comment_text);
			e.author = (TextView) row.findViewById(R.id.comment_author);
			e.date = (TextView) row.findViewById(R.id.comment_date);
			e.rating = (RatingBar) row.findViewById(R.id.rating);
			row.setTag(e);
		} else
		{
			e = (ReviewPlaceholder) row.getTag();
		}
		
		e.review = getItem(position);
		e.author.setText(e.review.getAuthor());
		e.comment.setText(e.review.getComment());
		e.date.setText(dateFormat.format(new Date(e.review.getDate())));
		if (e.review.getRating() != null) {
			e.rating.setRating(e.review.getRating());
		}

		return row;
	}

	private class ReviewPlaceholder {

		public Review review;
		public TextView comment, date, author;
		public RatingBar rating;

	}

}
