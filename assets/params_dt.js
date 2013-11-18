/*
 * POI CATEGORIES
 * 1 Museums
 * 2 Mobility
 * 3 Parking
 * 4 Offices
 * 5 Theater
 * 6 University
 * 7 Accomodation
 * 8 Libraries
 * 9 Food
 * 10 Drink
 * 11 Cinemas
 * 12 Family - Organizations
 * 13 other POIs
 * 
 * EVENT CATEGORIES
 * 1 Concerts
 * 2 Happy hours
 * 3 Movies
 * 4 Parties
 * 5 Seminars
 * 6 Theaters
 * 7 Exhibitions
 * 8 Family
 * 9 other events
 * 
 * DEFAULT EVENTS
 * ********************
 * 0 Today's event	*** special
 * ********************
 * 1 Concerts
 * 2 Happy hours
 * 3 Movies
 * 4 Parties
 * 5 Seminars
 * 6 Theaters
 * 7 Exhibitions
 * 8 Family
 * 9 other events
 * 
 * DEFAULT POI
 * 1 Museums
 * 2 Mobility
 * 3 Parking
 * 4 Offices
 * 5 Theater
 * 6 University
 * 7 Accomodation
 * 8 Libraries
 * 9 Food
 * 10 Drink
 * 11 Cinemas
 * 12 Family - Organizations
 * 13 other POIs
 * 
 * STORY CATEGORIES
 * 1 Leisure
 * 2 Offices and Services
 * 3 University
 * 4 Culture
 * 5 other stories
 * 
 * COORDINATES
 * Trento 46.069672, 11.121270
 * Rovereto 45.890919, 11.040184
 */

{
	"app_token" : "vivitrento",
	"title" : "ViviTrento",
	"poi_categories" : [
	    1,
	    2,
	    3,
	    4,
	    5,
	    6,
	    7,
	    8,
	    9,
	    10,
	    11,
	    13
	],
	"events_categories" : [
	    1,
        2,
        3,
        4,
        5,
        6,
        7,
        9
   ],
   "story_categories" : [
        1,
        2,
        4,
        5
	],
	"events_default" : [0],

	"exclude" : {
		"source" : [
	        "smartplanner-transitstops",
	        "TrentinoFamiglia"
	    ],
	    "type" : ["Comune", "Family", "Family - Organizations"]
	},
   "center_map":[
                     46.0696727540531,
                     11.1212700605392
                     ],
   "zoom_map":15
  
  
}