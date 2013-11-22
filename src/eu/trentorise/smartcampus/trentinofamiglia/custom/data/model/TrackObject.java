package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.Utils;

public class TrackObject extends BaseDTObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3399886172947825575L;

	private List<LatLng> decodedLine = null;
	
	private LatLng start = null;

	private String track;
	
	public List<LatLng> decodedLine() {
		if (decodedLine == null) {
			decodedLine = Utils.decodePolyline(track);
		}
		return decodedLine;
	}
	
	public LatLng startingPoint() {
		if (decodedLine() != null && !decodedLine.isEmpty()) {
			return decodedLine.get(0);
		}
		return null;
	}

	/**
	 * @return
	 */
	public String getTrack() {
		return track;
	}

	public void setTrack(String track) {
		this.track = track;
	}
	
}
