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
package eu.trentorise.smartcampus.trentinofamiglia.custom.data;

import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;
import eu.trentorise.smartcampus.territoryservice.model.POIData;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.PoiObjectForBean;

public class POIStorageHelper implements BeanStorageHelper<PoiObjectForBean> {

	@Override
	public PoiObjectForBean toBean(Cursor cursor) {
		PoiObjectForBean returnPoiObjectForBean = new PoiObjectForBean();
		POIObject poi = new POIObject();
		BaseDTStorageHelper.setCommonFields(cursor, poi);
		
		POIData poidata = new POIData();
		poidata.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
		poidata.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
		poidata.setPoiId(cursor.getString(cursor.getColumnIndex("poiId")));
		poidata.setCity(cursor.getString(cursor.getColumnIndex("city")));
		poidata.setCountry(cursor.getString(cursor.getColumnIndex("country")));
		poidata.setPostalCode(cursor.getString(cursor.getColumnIndex("postalCode")));
		poidata.setRegion(cursor.getString(cursor.getColumnIndex("region")));
		poidata.setState(cursor.getString(cursor.getColumnIndex("state")));
		poidata.setStreet(cursor.getString(cursor.getColumnIndex("street")));
		
		poi.setPoi(poidata);
		returnPoiObjectForBean.setObjectForBean(poi);
		return returnPoiObjectForBean;
	}

	@Override
	public ContentValues toContent(PoiObjectForBean bean) {
		POIObject poi = bean.getObjectForBean();
		ContentValues values = BaseDTStorageHelper.toCommonContent(poi);

		values.put("poiId", poi.getPoi().getPoiId());
		values.put("city", poi.getPoi().getCity());
		values.put("country", poi.getPoi().getCountry());
		values.put("postalCode", poi.getPoi().getPostalCode());
		values.put("state", poi.getPoi().getState());
		values.put("street", poi.getPoi().getStreet());
		return values;
	}

	@Override
	public Map<String,String> getColumnDefinitions() {
		Map<String,String> defs = BaseDTStorageHelper.getCommonColumnDefinitions();

		defs.put("poiId", "TEXT");
		defs.put("city", "TEXT");
		defs.put("country", "TEXT");
		defs.put("postalCode", "TEXT");
		defs.put("region", "TEXT");
		defs.put("state", "TEXT");
		defs.put("street", "TEXT");
		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}


}
