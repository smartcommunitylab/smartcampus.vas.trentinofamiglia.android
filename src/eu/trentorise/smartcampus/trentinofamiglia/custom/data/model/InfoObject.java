package eu.trentorise.smartcampus.trentinofamiglia.custom.data.model;

import java.util.List;
import java.util.Map;

import android.content.Context;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;

public class InfoObject extends BaseDTObject{

	/**
	 * 
	 */
	private static final long serialVersionUID = 558360097784617161L;

	public String customDescription(Context ctx) {
		String d = getDescription();
		if (CategoryHelper.CAT_INFO_NOTIZIE.equals(getType()) && getCustomData().containsKey("link")) {
			d += "<br/>"+getCustomData().get("link");
		}
		if (CategoryHelper.CAT_INFO_POLITICHE_PROVINCIALI.equals(getType()) && getCustomData().containsKey("more")) {
			d += "<br/><br/>"+getCustomData().get("more").toString().replace("\n", "<br/>");
		}
		if (CategoryHelper.CAT_INFO_POLITICHE_DEI_DISTRETTI.equals(getType()) && getCustomData().containsKey("actions")) {
			d += getCustomData().get("district name")+"<br/>" + getCustomData().get("program year")+"<br/>"+getCustomData().get("program link")+"<br/>";
			List<Map> list = (List<Map>)getCustomData().get("actions");
			for (Map map : list) {
				d += "<br/><b>"+map.get("title")+".</b> "+ map.get("goal").toString() +"<br/>" + map.get("times") +"<br/>"+ map.get("contact")+"<br/>";
				d = d.replace("<p>", "").replace("</p>", "");
			}
		}
		if (CategoryHelper.CAT_INFO_CERTIFICATORI_AUDIT.equals(getType()) && !getCustomData().isEmpty()) {
			d += "<br/>"+ctx.getString(R.string.audit_person_date,getCustomData().get("date"));
			d += "<br/><br/>email: "+getCustomData().get("email");
		}
		if (CategoryHelper.CAT_INFO_VALUTATORI_AUDIT.equals(getType()) && !getCustomData().isEmpty()) {
			d += "<br/>"+ctx.getString(R.string.audit_person_date,getCustomData().get("date"));
			d += "<br/><br/>email: "+getCustomData().get("email");
		}
		if (CategoryHelper.CAT_INFO_DISTRETTI_E_ORGANIZZAZIONI.equals(getType()) && !getCustomData().isEmpty()) {
			d += "<br/>"+getCustomData().get("district");
			if (getCustomData().containsKey("address")) {
				d += "<br/>"+getCustomData().get("address");
			}
			if (getCustomData().containsKey("email")) {
				d += "<br/>"+getCustomData().get("email");
			}
			if (getCustomData().containsKey("phone")) {
				d += "<br/>"+getCustomData().get("phone")+(getCustomData().containsKey("fax") ? "" : " / "+getCustomData().get("fax"));
			}
			if (getCustomData().containsKey("link")) {
				d += "<br/>"+getCustomData().get("link");
			}
		}
		return d;
	}

	
	
}
