package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;

public class InfoObject extends BaseDTObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 558360097784617161L;

	@Override
	public String getDescription() {
		String d = super.getDescription();
		if (getCustomData().containsKey("link")) {
			d += "<br/><a href=\""+getCustomData().get("link")+"\">"+getCustomData().get("link")+"</a>";
		}
		return d;
	}

	
	
}
