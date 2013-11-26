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

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.Review;

public class ReviewHelper {

	public interface ReviewHandler {
		public void onReviewChanged(Review review);
	}
	
	public static void reviewDialog(Context context, float initValue, final ReviewHandler handler, int  ResourceString) {
		final Dialog rankDialog = new Dialog(context);
        rankDialog.setContentView(R.layout.rating);
        rankDialog.setCancelable(true);
        rankDialog.setTitle(ResourceString);
        final RatingBar ratingBar = (RatingBar) rankDialog.findViewById(R.id.ratingBar);
        ratingBar.setRating(initValue);
		final EditText comment = (EditText) rankDialog.findViewById(R.id.rating_text);
        
        Button updateButton = (Button) rankDialog.findViewById(R.id.rating_ok);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Review r = new Review(null,comment.getText().toString(),(int)ratingBar.getRating());
            	handler.onReviewChanged(r);
                rankDialog.dismiss();
            }
        });
        Button cancelButton = (Button) rankDialog.findViewById(R.id.rating_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rankDialog.dismiss();
            }
        });
        rankDialog.show();
	}

}
