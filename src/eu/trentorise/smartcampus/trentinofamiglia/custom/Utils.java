package eu.trentorise.smartcampus.trentinofamiglia.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.view.inputmethod.InputMethodManager;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion.TYPE;
import eu.trentorise.smartcampus.social.model.Concept;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.ExplorerObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;

public class Utils {
	public static final String userPoiObject = "eu.trentorise.smartcampus.dt.model.UserPOIObject";
	public static final String servicePoiObject = "eu.trentorise.smartcampus.dt.model.ServicePOIObject";



	public static List<Concept> conceptConvertSS(Collection<SemanticSuggestion> tags) {
		List<Concept> result = new ArrayList<Concept>();
		for (SemanticSuggestion ss : tags) {
			if (ss.getType() == TYPE.KEYWORD) {
				result.add(new Concept(null, ss.getName()));
			} else if (ss.getType() == TYPE.SEMANTIC) {
				Concept c = new Concept();
				// c.setId(ss.getId());
				c.setName(ss.getName());
				c.setDescription(ss.getDescription());
				c.setSummary(ss.getSummary());
				result.add(c);
			}
		}
		return result;
	}

	public static ArrayList<SemanticSuggestion> conceptConvertToSS(List<Concept> tags) {
		if (tags == null)
			return new ArrayList<SemanticSuggestion>();
		ArrayList<SemanticSuggestion> result = new ArrayList<SemanticSuggestion>();
		for (Concept c : tags) {
			SemanticSuggestion ss = new SemanticSuggestion();
			if (c.getId() == null) {
				ss.setType(TYPE.KEYWORD);
			} else {
				// ss.setId(c.getId());
				ss.setDescription(c.getDescription());
				ss.setSummary(c.getSummary());
				ss.setType(TYPE.SEMANTIC);
			}
			ss.setName(c.getName());
			result.add(ss);
		}
		return result;
	}

	public static String conceptToSimpleString(List<Concept> tags) {
		if (tags == null)
			return null;
		String content = "";
		for (Concept s : tags) {
			if (content.length() > 0)
				content += ", ";
			content += s.getName();
		}
		return content;
	}

	public static String getPOIshortAddress(POIObject poi) {
		String res = (poi.getPoi().getStreet() == null || poi.getPoi().getStreet().length() == 0 ? 
				"" : poi .getPoi().getStreet());
		
		String city = (poi.getPoi().getCity() == null || poi.getPoi().getCity().length() == 0 ? 
				"" : poi .getPoi().getCity());
		if (city != null && city.length() > 0) res += (res.length() > 0 ? " " : "") + city;

		if (res.length() > 0) return res;
		return poi.getTitle();
	}

	public static Address getPOIasGoogleAddress(POIObject poi) {
		Address a = new Address(Locale.getDefault());
		a.setLatitude(poi.getLocation()[0]);
		a.setLongitude(poi.getLocation()[1]);
		a.setAddressLine(0, poi.getPoi().getStreet());
		a.setCountryCode(poi.getPoi().getCountry());
		a.setCountryName(poi.getPoi().getState());
		a.setLocality(poi.getPoi().getCity());
		a.setPostalCode(poi.getPoi().getPostalCode());
		a.setAdminArea(poi.getPoi().getRegion());
		return a;
	}

	/**
	 * @param mTrack
	 * @return
	 */
	public static Address getTrackAsGoogleAddress(TrackObject mTrack) {
		Address a = new Address(Locale.getDefault());
		a.setLatitude(mTrack.startingPoint().latitude);
		a.setLongitude(mTrack.startingPoint().longitude);
		a.setAddressLine(0, mTrack.getTitle());
		return a;
	}
	
	public static boolean isCreatedByUser(BaseDTObject obj) {
		if (obj.getDomainType() == null || userPoiObject.equals(obj.getDomainType())) {
			return true;
		} else
			return false;
	}

	public static Collection<ExplorerObject> convertToLocalEventFromBean(
			Collection<EventObjectForBean> searchInGeneral) {
		Collection<ExplorerObject> returnCollection = new ArrayList<ExplorerObject>();
		for (EventObjectForBean event : searchInGeneral) {
				ExplorerObject localEvent = DTHelper.findEventById(event.getObjectForBean().getId());
				if (localEvent != null) {

				returnCollection.add(localEvent);
			}
		}
		return returnCollection;
	}

	public static Collection<ExplorerObject> convertToLocalEvent(Collection<EventObject> events) {
		Collection<EventObjectForBean> beanEvents = new ArrayList<EventObjectForBean>();
		Collection<ExplorerObject> returnEvents = new ArrayList<ExplorerObject>();

		for (EventObject event : events) {
			EventObjectForBean newObject = new EventObjectForBean();
			ExplorerObject localObject = new ExplorerObject();
			newObject.setObjectForBean(event);
			localObject.setEventFromEventObjectForBean(newObject);
			returnEvents.add(localObject);
		}

		return returnEvents;
	}

	public static List<LatLng> decodePolyline(String encoded) {
		List<LatLng> polyline = new ArrayList<LatLng>();

		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			if (index >= len) {
				break;
			}
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			polyline.add(p);
		}
        return polyline;
	}

	/**
	 * @param event
	 * @return
	 */
	public static String getEventShortAddress(ExplorerObject event) {
		if (event.getCustomData() != null && event.getCustomData().get("place")!=null) {
			return event.getCustomData().get("place").toString();
		} else {
			return null;
		}
	}
	public static void hideKeyboard (Activity activity){
		// hide keyboard if it is still open
		InputMethodManager imm = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(
				activity.findViewById(R.id.frame_content).getWindowToken(),
				0);
	}

}
