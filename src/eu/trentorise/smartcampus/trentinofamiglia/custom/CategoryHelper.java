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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObject;

public class CategoryHelper {
	
	public static final String CAT_TRACK_PISTA_CICLABILE = "Pista ciclabile";
	public static final String CAT_TRACK_PASSEGGIATE = "Passeggiate";
	public static final String CAT_TRACK_PISTE_CICLOPEDONALI = "Piste ciclopedonali";
	
	public static final String CAT_INFO_DISTRETTI_E_ORGANIZZAZIONI = "Distretti e organizzazioni";
	public static final String CAT_INFO_POLITICHE_DEI_DISTRETTI = "Politiche dei distretti";
	public static final String CAT_INFO_VALUTATORI_AUDIT = "Valutatori \"Audit\"";
	public static final String CAT_INFO_CONSULENTI_AUDIT = "Consulenti \"Audit\"";
	public static final String CAT_INFO_POLITICHE_PROVINCIALI = "Politiche provinciali";
	public static final String CAT_INFO_NOTIZIE = "Notizie";

	public static final String CAT_POI_FAMILY_IN_TRENTINO = "\"Family in Trentino\"";
	public static final String CAT_POI_TAVOLO_NUOVI_MEDIA = "Tavolo \"Nuovi Media\"";
	public static final String CAT_POI_PUNTI_ALLATTAMENTO = "Punti allattamento";
	public static final String CAT_POI_VACANZE_AL_MARE = "Vacanze al mare";
	public static final String CAT_POI_FAMILY_AUDIT = "\"Family Audit\"";

	public static final String CAT_EVENT_ALTO_GARDA = "Alto Garda";
	public static final String CAT_EVENT_ESTATE_GIOVANI_E_FAMIGLIA = "Estate giovani e famiglia";
	
	private final static String TAG = "CategoryHelper";
	// private static final String POI_NONCATEGORIZED = "Other place";
	// private static final String EVENT_NONCATEGORIZED = "Other event";
	// private static final String STORY_NONCATEGORIZED = "Other story";

	public static final String CATEGORY_TYPE_POIS = "pois";
	public static final String CATEGORY_TYPE_EVENTS = "events";
	public static final String CATEGORY_TYPE_INFOS = "infos";
	public static final String CATEGORY_TYPE_TRACKS = "tracks";

	public static final String FAMILY_CATEGORY_POI = "Family - Organizations";
	public static final String FAMILY_CATEGORY_EVENT = "Family";

	public static final String CATEGORY_TODAY = "Today";
	public static final String CATEGORY_MY = "My";

	public static CategoryDescriptor[] EVENT_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_estate_map, R.drawable.ic_summer,
					CAT_EVENT_ESTATE_GIOVANI_E_FAMIGLIA, R.string.categories_event_summer_family),
			/* 2 */new CategoryDescriptor(R.drawable.ic_altogarda_map, R.drawable.ic_altogarda, 
					CAT_EVENT_ALTO_GARDA, R.string.categories_event_alto_garda), };

	public static CategoryDescriptor[] POI_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_familyaudit_map, R.drawable.family_audit, CAT_POI_FAMILY_AUDIT,
					R.string.categories_poi_family_audit),
			/* 2 */new CategoryDescriptor(R.drawable.ic_mare_map, R.drawable.ic_beach, CAT_POI_VACANZE_AL_MARE,
					R.string.categories_poi_summer_holyday),
			/* 3 */new CategoryDescriptor(R.drawable.ic_poi, R.drawable.ic_media,
					CAT_POI_TAVOLO_NUOVI_MEDIA, R.string.categories_poi_new_media),
			/* 4 */new CategoryDescriptor(R.drawable.ic_allattamento_map, R.drawable.ic_breastfeeding, CAT_POI_PUNTI_ALLATTAMENTO,
					R.string.categories_poi_breast_point),
			/* 5 */new CategoryDescriptor(R.drawable.ic_family_tn_map, R.drawable.family_trentino,
					CAT_POI_FAMILY_IN_TRENTINO, R.string.categories_poi_trentino_family), }; 

	public static CategoryDescriptor[] INFO_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_provincia,
					CAT_INFO_POLITICHE_PROVINCIALI, R.string.categories_info_provincial_politics),
			/* 2 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_consulenti,
					CAT_INFO_CONSULENTI_AUDIT, R.string.categories_info_certifiers),
			/* 3 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_valutatori,
					CAT_INFO_VALUTATORI_AUDIT, R.string.categories_info_evaluators),
			/* 4 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_politics_district,
					CAT_INFO_POLITICHE_DEI_DISTRETTI, R.string.categories_info_distrectual_politics),
			/* 5 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_news, CAT_INFO_NOTIZIE,
					R.string.categories_info_news),
			/* 6 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_district,
					CAT_INFO_DISTRETTI_E_ORGANIZZAZIONI, R.string.categories_info_organizations), };

	public static CategoryDescriptor[] TRACK_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_bici_map, R.drawable.ic_bici, CAT_TRACK_PISTE_CICLOPEDONALI,
					R.string.categories_track_pedestrian),
			/* 2 */new CategoryDescriptor(R.drawable.ic_passeggiata_map, R.drawable.ic_passeggiata, CAT_TRACK_PASSEGGIATE,
					R.string.categories_track_trekking),
			/* 3 */new CategoryDescriptor(R.drawable.ic_bici_map, R.drawable.ic_bici,
					CAT_TRACK_PISTA_CICLABILE, R.string.categories_track_bicycle), };

	private static Map<String, String> categoryMapping = new HashMap<String, String>();

	private static Map<String, CategoryDescriptor> descriptorMap = new LinkedHashMap<String, CategoryHelper.CategoryDescriptor>();
	static {
		for (CategoryDescriptor event : EVENT_CATEGORIES) {
			descriptorMap.put(event.category, event);
		}

		for (CategoryDescriptor poi : POI_CATEGORIES) {
			descriptorMap.put(poi.category, poi);
		}
		
		for (CategoryDescriptor info : INFO_CATEGORIES) {
			descriptorMap.put(info.category, info);
		}
		
		for (CategoryDescriptor track : TRACK_CATEGORIES) {
			descriptorMap.put(track.category, track);
		}

		for (String s : descriptorMap.keySet()) {
			categoryMapping.put(s, s);
		}
//		// custom categories for events
//		categoryMapping.put("Dances", "Theaters");
//		// custom categories for POIs
//		categoryMapping.put("biblioteca", "Libraries");
//		categoryMapping.put("museo", "Museums");
//		categoryMapping.put("esposizione", "Museums");
//		categoryMapping.put("arte", "Museums");
//		// categoryMapping.put("luogo", POI_NONCATEGORIZED);
//		categoryMapping.put("ufficio", "Offices");
//		// categoryMapping.put("sala", POI_NONCATEGORIZED);
//		categoryMapping.put("teatro", "Theater");
//		categoryMapping.put("musica", "Theater");
//		categoryMapping.put("universita", "University");
//		categoryMapping.put("bar", "Drink");
//		categoryMapping.put("ristorante", "Food");
//		categoryMapping.put("Lodging", "Accomodation");
//		// categoryMapping.put("Other", POI_NONCATEGORIZED);
//		// categoryMapping.put("ou", POI_NONCATEGORIZED);
	}

	public static String[] getAllCategories(Set<String> set) {
		List<String> result = new ArrayList<String>();
		for (String key : categoryMapping.keySet()) {
			if (set.contains(categoryMapping.get(key))) {
				// if (key.equals(EVENT_NONCATEGORIZED) ||
				// key.equals(POI_NONCATEGORIZED) ||
				// key.equals(STORY_NONCATEGORIZED)) {
				//
				// result.add(null);
				// }
				result.add(key);
				// set.remove(categoryMapping.get(key));
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public static String getMainCategory(String category) {
		return categoryMapping.get(category);
	}

	public static int getMapIconByType(String type) {
		if (categoryMapping.containsKey(type))
			return descriptorMap.get(categoryMapping.get(type)).map_icon;
		return R.drawable.ic_p_other;
	}

	public static int getIconByType(String type) {
		if (categoryMapping.containsKey(type))
			return descriptorMap.get(categoryMapping.get(type)).thumbnail;
		return R.drawable.ic_p_other;
	}

	public static class CategoryDescriptor {
		public int map_icon;
		public int thumbnail;
		public String category;
		public int description;

		public CategoryDescriptor(int map_icon, int thumbnail, String category, int description) {
			super();
			this.map_icon = map_icon;
			this.thumbnail = thumbnail;
			this.category = category;
			this.description = description;
		}
	}

	public static CategoryDescriptor[] getPOICategoryDescriptors() {
		return POI_CATEGORIES;
	}

	public static CategoryDescriptor[] getEventCategoryDescriptors() {
		return EVENT_CATEGORIES;
	}

	public static CategoryDescriptor[] getInfoCategoryDescriptors() {
		return INFO_CATEGORIES;
	}
	public static CategoryDescriptor[] getTrackCategoryDescriptors() {
		return TRACK_CATEGORIES;
	}

	public static String[] getPOICategories() {
		String[] res = new String[POI_CATEGORIES.length];
		for (int i = 0; i < POI_CATEGORIES.length; i++) {
			res[i] = POI_CATEGORIES[i].category;
		}
		return res;
	}

	public static String[] getEventCategories() {
		String[] res = new String[EVENT_CATEGORIES.length];
		for (int i = 0; i < EVENT_CATEGORIES.length; i++) {
			res[i] = EVENT_CATEGORIES[i].category;
		}
		return res;
	}

	public static String[] getInfoCategories() {
		String[] res = new String[INFO_CATEGORIES.length];
		for (int i = 0; i < INFO_CATEGORIES.length; i++) {
			res[i] = INFO_CATEGORIES[i].category;
		}
		return res;
	}
	
	public static String[] getTrackCategories() {
		String[] res = new String[TRACK_CATEGORIES.length];
		for (int i = 0; i < TRACK_CATEGORIES.length; i++) {
			res[i] = TRACK_CATEGORIES[i].category;
		}
		return res;
	}

	
	public static CategoryDescriptor[] getEventCategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(EVENT_CATEGORIES, CATEGORY_TYPE_EVENTS);
	}

	public static CategoryDescriptor[] getPOICategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(POI_CATEGORIES, CATEGORY_TYPE_POIS);
	}

	public static CategoryDescriptor[] getInfoCategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(INFO_CATEGORIES, CATEGORY_TYPE_INFOS);
	}

	public static CategoryDescriptor[] getTrackCategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(TRACK_CATEGORIES, CATEGORY_TYPE_TRACKS);
	}
	public static CategoryDescriptor getCategoryDescriptorByCategoryFiltered(String type, String cat) {
		return descriptorMap.get(cat);
		
//		CategoryDescriptor[] cdarray = null;
//
//		if (type.equalsIgnoreCase(CATEGORY_TYPE_POIS)) {
//			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(POI_CATEGORIES, type);
//		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_EVENTS)) {
//			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(EVENT_CATEGORIES, type);
//		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_INFOS)) {
//			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(INFO_CATEGORIES, type);
//		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_TRACKS)) {
//			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(TRACK_CATEGORIES, type);
//		}
//
//		if (cdarray != null) {
//			for (int i = 0; i < cdarray.length; i++) {
//				CategoryDescriptor cd = cdarray[i];
//				if (cd.category.equalsIgnoreCase(cat)) {
//					return cd;
//				}
//			}
//		}
//
//		return null;
	}
	public static boolean hasRatingForm(BaseDTObject obj) {
		if (CAT_POI_TAVOLO_NUOVI_MEDIA.equals(obj.getType())) return false;
		return !(obj instanceof InfoObject);
	}
}
