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

import eu.trentorise.smartcampus.trentinofamiglia.R;

public class CategoryHelper {
	private final static String TAG = "CategoryHelper";
	private static final String POI_NONCATEGORIZED = "Other place";
	private static final String EVENT_NONCATEGORIZED = "Other event";
	private static final String STORY_NONCATEGORIZED = "Other story";

	public static final String CATEGORY_TYPE_POIS = "pois";
	public static final String CATEGORY_TYPE_EVENTS = "events";
	public static final String CATEGORY_TYPE_STORIES = "stories";
	public static final String FAMILY_CATEGORY_POI = "Family - Organizations";
	public static final String FAMILY_CATEGORY_EVENT = "Family";

	public static final String CATEGORY_TODAY = "Today";
	public static final String CATEGORY_MY = "My";

	public static CategoryDescriptor EVENTS_TODAY = new CategoryDescriptor(R.drawable.ic_marker_e_generic,
			R.drawable.ic_e_todaysevents, CATEGORY_TODAY, R.string.categories_event_today);

	public static CategoryDescriptor EVENTS_MY = new CategoryDescriptor(R.drawable.ic_marker_e_generic,
			R.drawable.ic_e_myevents, CATEGORY_MY, R.string.categories_event_my);

	public static CategoryDescriptor[] EVENT_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_marker_e_concert, R.drawable.ic_e_concerts, "Concerts",
					R.string.categories_event_concert),
			/* 2 */new CategoryDescriptor(R.drawable.ic_marker_e_happyhour, R.drawable.ic_e_happyhours, "Happy hours",
					R.string.categories_event_happyhour),
			/* 3 */new CategoryDescriptor(R.drawable.ic_marker_e_movie, R.drawable.ic_e_movies, "Movies",
					R.string.categories_event_movie),
			/* 4 */new CategoryDescriptor(R.drawable.ic_marker_e_party, R.drawable.ic_e_parties, "Parties",
					R.string.categories_event_party),
			/* 5 */new CategoryDescriptor(R.drawable.ic_marker_e_seminar, R.drawable.ic_e_seminars, "Seminars",
					R.string.categories_event_seminar),
			/* 6 */new CategoryDescriptor(R.drawable.ic_marker_e_performance, R.drawable.ic_e_performances, "Theaters",
					R.string.categories_event_theater),
			/* 7 */new CategoryDescriptor(R.drawable.ic_marker_e_exhibition, R.drawable.ic_e_exhibitions, "Exhibitions",
					R.string.categories_event_exhibition),
			/* 8 */new CategoryDescriptor(R.drawable.ic_marker_e_family, R.drawable.ic_e_family, "Family",
					R.string.categories_event_family),
			/* 9 */new CategoryDescriptor(R.drawable.ic_marker_e_generic, R.drawable.ic_e_other, EVENT_NONCATEGORIZED,
					R.string.categories_event_generic), };

	public static CategoryDescriptor[] POI_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_marker_p_museum, R.drawable.ic_p_museums, "Museums",
					R.string.categories_poi_museum),
			/* 2 */new CategoryDescriptor(R.drawable.ic_marker_p_mobility, R.drawable.ic_p_mobility, "Mobility",
					R.string.categories_poi_mobility),
			/* 3 */new CategoryDescriptor(R.drawable.ic_marker_p_parking, R.drawable.ic_p_parkings, "Parking",
					R.string.categories_poi_parking),
			/* 4 */new CategoryDescriptor(R.drawable.ic_marker_p_office, R.drawable.ic_p_offices, "Offices",
					R.string.categories_poi_office),
			/* 5 */new CategoryDescriptor(R.drawable.ic_marker_p_theatre, R.drawable.ic_p_theaters, "Theater",
					R.string.categories_poi_theater),
			/* 6 */new CategoryDescriptor(R.drawable.ic_marker_p_university, R.drawable.ic_p_university, "University",
					R.string.categories_poi_university),
			/* 7 */new CategoryDescriptor(R.drawable.ic_marker_p_accomodation, R.drawable.ic_p_accomodation, "Accomodation",
					R.string.categories_poi_accommodation),
			/* 8 */new CategoryDescriptor(R.drawable.ic_marker_p_library, R.drawable.ic_p_libraries, "Libraries",
					R.string.categories_poi_library),
			/* 9 */new CategoryDescriptor(R.drawable.ic_marker_p_food, R.drawable.ic_p_food, "Food",
					R.string.categories_poi_food),
			/* 10 */new CategoryDescriptor(R.drawable.ic_marker_p_drink, R.drawable.ic_p_drink, "Drink",
					R.string.categories_poi_drink),
			/* 11 */new CategoryDescriptor(R.drawable.ic_marker_p_cinema, R.drawable.ic_p_cinemas, "Cinemas",
					R.string.categories_poi_cinema),
			/* 12 */new CategoryDescriptor(R.drawable.ic_marker_p_family, R.drawable.ic_p_family, "Family - Organizations",
					R.string.categories_poi_family),
			/* 13 */new CategoryDescriptor(R.drawable.ic_marker_p_generic, R.drawable.ic_p_other, POI_NONCATEGORIZED,
					R.string.categories_poi_generic), };

	public static CategoryDescriptor STORIES_MY = new CategoryDescriptor(R.drawable.ic_marker_p_generic,
			R.drawable.ic_s_mystories, CATEGORY_MY, R.string.categories_story_my);
	
	public static CategoryDescriptor[] STORY_CATEGORIES = new CategoryDescriptor[] {
			/* 1 */new CategoryDescriptor(R.drawable.ic_marker_e_generic, R.drawable.ic_s_leisure, "Leisure",
					R.string.categories_story_leisure),
			/* 2 */new CategoryDescriptor(R.drawable.ic_marker_e_generic, R.drawable.ic_s_organizations,
					"Offices and Services", R.string.categories_story_offices_and_services),
			/* 3 */new CategoryDescriptor(R.drawable.ic_marker_p_university, R.drawable.ic_s_university, "University",
					R.string.categories_story_university),
			/* 4 */new CategoryDescriptor(R.drawable.ic_marker_e_generic, R.drawable.ic_s_cultural, "Culture",
					R.string.categories_story_culture),
			/* 5 */new CategoryDescriptor(R.drawable.ic_marker_e_generic, R.drawable.ic_s_other, STORY_NONCATEGORIZED,
					R.string.categories_story_generic), };

	private static Map<String, String> categoryMapping = new HashMap<String, String>();

	private static Map<String, CategoryDescriptor> descriptorMap = new LinkedHashMap<String, CategoryHelper.CategoryDescriptor>();
	static {
		for (CategoryDescriptor event : EVENT_CATEGORIES) {
			descriptorMap.put(event.category, event);
		}

		for (CategoryDescriptor poi : POI_CATEGORIES) {
			descriptorMap.put(poi.category, poi);
		}

		for (CategoryDescriptor story : STORY_CATEGORIES) {
			descriptorMap.put(story.category, story);
		}

		for (String s : descriptorMap.keySet()) {
			categoryMapping.put(s, s);
		}
		// custom categories for events
		categoryMapping.put("Dances", "Theaters");
		// custom categories for POIs
		categoryMapping.put("biblioteca", "Libraries");
		categoryMapping.put("museo", "Museums");
		categoryMapping.put("esposizione", "Museums");
		categoryMapping.put("arte", "Museums");
		categoryMapping.put("luogo", POI_NONCATEGORIZED);
		categoryMapping.put("ufficio", "Offices");
		categoryMapping.put("sala", POI_NONCATEGORIZED);
		categoryMapping.put("teatro", "Theater");
		categoryMapping.put("musica", "Theater");
		categoryMapping.put("universita", "University");
		categoryMapping.put("bar", "Drink");
		categoryMapping.put("ristorante", "Food");
		categoryMapping.put("Lodging", "Accomodation");
		categoryMapping.put("Other", POI_NONCATEGORIZED);
		categoryMapping.put("ou", POI_NONCATEGORIZED);
	}

	public static String[] getAllCategories(Set<String> set) {
		List<String> result = new ArrayList<String>();
		for (String key : categoryMapping.keySet()) {
			if (set.contains(categoryMapping.get(key))) {
				if (key.equals(EVENT_NONCATEGORIZED) || key.equals(POI_NONCATEGORIZED) || key.equals(STORY_NONCATEGORIZED)) {

					result.add(null);
				}
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
		return R.drawable.ic_marker_e_generic;
	}

	public static int getIconByType(String type) {
		if (categoryMapping.containsKey(type))
			return descriptorMap.get(categoryMapping.get(type)).thumbnail;
		return R.drawable.ic_e_other;
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

	public static CategoryDescriptor[] getStoryCategoryDescriptors() {
		return STORY_CATEGORIES;
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

	public static String[] getStoryCategories() {
		String[] res = new String[STORY_CATEGORIES.length];
		for (int i = 0; i < STORY_CATEGORIES.length; i++) {
			res[i] = STORY_CATEGORIES[i].category;
		}
		return res;
	}

	// public static CategoryDescriptor getCategoryDescriptorByCategory(String
	// type, String cat) {
	// CategoryDescriptor[] cdarray = null;
	//
	// if (type.equalsIgnoreCase(CATEGORY_TYPE_POIS)) {
	// cdarray = POI_CATEGORIES;
	// } else if (type.equalsIgnoreCase(CATEGORY_TYPE_EVENTS)) {
	// cdarray = EVENT_CATEGORIES;
	// } else if (type.equalsIgnoreCase(CATEGORY_TYPE_STORIES)) {
	// cdarray = STORY_CATEGORIES;
	// }
	//
	// if (cdarray != null) {
	// for (int i = 0; i < cdarray.length; i++) {
	// CategoryDescriptor cd = cdarray[i];
	// if (cd.category.equalsIgnoreCase(cat)) {
	// return cd;
	// }
	// }
	// }
	//
	//
	// return null;
	// }

	public static CategoryDescriptor[] getEventCategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(EVENT_CATEGORIES, CATEGORY_TYPE_EVENTS);
	}

	public static CategoryDescriptor[] getPOICategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(POI_CATEGORIES, CATEGORY_TYPE_POIS);
	}

	public static CategoryDescriptor[] getStoryCategoryDescriptorsFiltered() {
		return DTParamsHelper.getInstance().getFilteredArrayByParams(STORY_CATEGORIES, CATEGORY_TYPE_STORIES);
	}

	public static CategoryDescriptor getCategoryDescriptorByCategoryFiltered(String type, String cat) {
		CategoryDescriptor[] cdarray = null;

		if (type.equalsIgnoreCase(CATEGORY_TYPE_POIS)) {
			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(POI_CATEGORIES, type);
		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_EVENTS)) {
			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(EVENT_CATEGORIES, type);
		} else if (type.equalsIgnoreCase(CATEGORY_TYPE_STORIES)) {
			cdarray = DTParamsHelper.getInstance().getFilteredArrayByParams(STORY_CATEGORIES, type);
		}

		if (cdarray != null) {
			for (int i = 0; i < cdarray.length; i++) {
				CategoryDescriptor cd = cdarray[i];
				if (cd.category.equalsIgnoreCase(cat)) {
					return cd;
				}
			}
		}

		return null;
	}
	

}
