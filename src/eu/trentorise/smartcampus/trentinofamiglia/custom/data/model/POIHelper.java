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

package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import android.content.Context;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;

/**
 * @author raman
 *
 */
public class POIHelper {

	public static String customDescription(POIObject poi, Context ctx) {
		String d = poi.getDescription();
		if (CategoryHelper.CAT_POI_VACANZE_AL_MARE.equals(poi.getType())) {
			d = ctx.getString(R.string.hotel_level, poi.getCustomData().get("levelFamily"));
			d += "<br/>" + ctx.getString(R.string.hotel_cat, poi.getCustomData().get("stars"));
			d += "<br/>" + poi.getCustomData().get("guide")+"<br/>";
			d += "<br/>" + poi.getCustomData().get("bookingHow") +
				 "<br/>" + poi.getCustomData().get("bookingHow") +
				 "<br/>" + poi.getCustomData().get("bookingWhere") +
				 "<br/>" + poi.getCustomData().get("bookingAddress") + ", " + poi.getCustomData().get("bookingZipCode") + ", "+ poi.getCustomData().get("bookingTown") +
				 "<br/>" + poi.getCustomData().get("bookingLink") +
				 "<br/>" + poi.getCustomData().get("bookingEmail") +
				 "<br/>" + poi.getCustomData().get("bookingPhone");
		}
		if (CategoryHelper.CAT_POI_FAMILY_AUDIT.equals(poi.getType())) {
			d +=poi.getCustomData().get("link")+"<br/>";
		}	
		if (CategoryHelper.CAT_POI_FAMILY_IN_TRENTINO.equals(poi.getType())) {
			d +=poi.getCustomData().get("subtype")+"<br/>";
			if (poi.getCustomData().containsKey("email")) {
				d +=poi.getCustomData().get("email")+"<br/>";
			}
			if (poi.getCustomData().containsKey("phone")) {
				d +=poi.getCustomData().get("phone")+"<br/>";
			}
			if (poi.getCustomData().containsKey("web")) {
				d +=poi.getCustomData().get("web");
			}
		}
		if (CategoryHelper.CAT_POI_PUNTI_ALLATTAMENTO.equals(poi.getType())) {
			// TODO
		}	
		if (CategoryHelper.CAT_POI_TAVOLO_NUOVI_MEDIA.equals(poi.getType())) {
			d +=poi.getCustomData().get("role");
			if (poi.getCustomData().containsKey("contact")) {
				d += "<br/>"+ poi.getCustomData().get("contact");
			}
			if (poi.getCustomData().containsKey("address")) {
				d +="<br/>"+ poi.getCustomData().get("address");
			}
			if (poi.getCustomData().containsKey("email")) {
				d +="<br/>"+ poi.getCustomData().get("email");
			}
			if (poi.getCustomData().containsKey("phone")) {
				d +="<br/>"+ poi.getCustomData().get("phone");
			}
			if (poi.getCustomData().containsKey("link")) {
				d +="<br/>"+ poi.getCustomData().get("link");
			}
			
		}	
		
		return d;
	}
}
