package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;

public class InfoObject extends BaseDTObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 558360097784617161L;

	@Override
	public String getDescription() {
		if (getCustomData().containsKey("link")) {
			return super.getDescription()+"\n\n"+getCustomData().get("link");
		}
		return super.getDescription();
	}

	
	
}
