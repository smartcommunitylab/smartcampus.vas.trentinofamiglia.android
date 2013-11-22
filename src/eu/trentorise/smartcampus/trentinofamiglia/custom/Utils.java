package eu.trentorise.smartcampus.trentinofamiglia.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.location.Address;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion.TYPE;
import eu.trentorise.smartcampus.social.model.Concept;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
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
		return poi.getTitle()
				+ (poi.getPoi().getStreet() == null || poi.getPoi().getStreet().length() == 0 ? "" : (", " + poi
						.getPoi().getStreet()));
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
		// TODO Auto-generated method stub 
		return null;
	}
	
	public static boolean isCreatedByUser(BaseDTObject obj) {
		if (obj.getDomainType() == null || userPoiObject.equals(obj.getDomainType())) {
			return true;
		} else
			return false;
	}

	public static Collection<LocalEventObject> convertToLocalEventFromBean(
			Collection<EventObjectForBean> searchInGeneral) {
		Collection<LocalEventObject> returnCollection = new ArrayList<LocalEventObject>();
		for (EventObjectForBean event : searchInGeneral) {
				LocalEventObject localEvent = DTHelper.findEventById(event.getObjectForBean().getId());
				if (localEvent != null) {

				returnCollection.add(localEvent);
			}
		}
		return returnCollection;
	}

	public static Collection<LocalEventObject> convertToLocalEvent(Collection<EventObject> events) {
		Collection<EventObjectForBean> beanEvents = new ArrayList<EventObjectForBean>();
		Collection<LocalEventObject> returnEvents = new ArrayList<LocalEventObject>();

		for (EventObject event : events) {
			EventObjectForBean newObject = new EventObjectForBean();
			LocalEventObject localObject = new LocalEventObject();
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

}
