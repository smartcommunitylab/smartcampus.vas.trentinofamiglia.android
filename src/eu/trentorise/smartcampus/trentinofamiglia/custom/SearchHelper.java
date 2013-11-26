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

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhereForSearch;

public class SearchHelper {

	private static long oneDay = 1 * 24 * 60 * 60 * 1000;
	private static WhenForSearch[] WHEN_CATEGORIES = null;
	private static WhereForSearch[] WHERE_CATEGORIES =null;
	public interface OnSearchListener {
		void onSearch(String query);
	}

	public static void initSpinners(Context context) {
		WHEN_CATEGORIES = new WhenForSearch[] {
			new WhenForSearch(context.getString(R.string.search_anytime),0,0),
			new WhenForSearch(context.getString(R.string.search_today),0,oneDay),
			new WhenForSearch(context.getString(R.string.search_tomorrow),oneDay,oneDay),
			new WhenForSearch(context.getString(R.string.search_thisweek),0,7*oneDay),
			new WhenForSearch(context.getString(R.string.search_intwoweeks),0,14 * oneDay),
			new WhenForSearch(context.getString(R.string.search_inonemonth),0,30 * oneDay)};
		WHERE_CATEGORIES = new WhereForSearch[] {
			new WhereForSearch(context.getString(R.string.search_anywhere),0.00),
			new WhereForSearch(context.getString(R.string.search_within,"100m"),0.001),
			new WhereForSearch(context.getString(R.string.search_within,"200m"),0.002),
			new WhereForSearch(context.getString(R.string.search_within,"500m"),0.005),
			new WhereForSearch(context.getString(R.string.search_within,"1km"),0.01),
			new WhereForSearch(context.getString(R.string.search_within,"10km"),0.1),
			new WhereForSearch(context.getString(R.string.search_within,"50km"),0.5)};
	}

	
	public static List<WhenForSearch> getWhenList() {
		return Arrays.asList(WHEN_CATEGORIES);
	}
	public static List<WhereForSearch> getWhereList() {
		return Arrays.asList(WHERE_CATEGORIES);
	}
}
