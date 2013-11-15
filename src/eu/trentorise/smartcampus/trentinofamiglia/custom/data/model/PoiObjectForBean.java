package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import eu.trentorise.smartcampus.territoryservice.model.POIObject;

public class PoiObjectForBean extends GenericObjectForBean<POIObject> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6366406943702317104L;


	public PoiObjectForBean() {
		setObjectForBean(new POIObject());
	}
}
