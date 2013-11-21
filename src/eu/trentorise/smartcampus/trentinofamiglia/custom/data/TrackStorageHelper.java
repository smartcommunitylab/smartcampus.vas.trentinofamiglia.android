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

import java.util.Collections;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObjectForBean;

public class TrackStorageHelper implements BeanStorageHelper<TrackObjectForBean> {

	@Override
	public TrackObjectForBean toBean(Cursor cursor) {
		

		TrackObjectForBean returnEventObjectForBean = new TrackObjectForBean();
		TrackObject track = new TrackObject();
		BaseDTStorageHelper.setCommonFields(cursor, track);
		
		track.setFromTime(cursor.getLong(cursor.getColumnIndex("fromTime")));
		track.setToTime(cursor.getLong(cursor.getColumnIndex("toTime")));
		track.setTiming(cursor.getString(cursor.getColumnIndex("timing")));

		returnEventObjectForBean.setObjectForBean(track);
		return returnEventObjectForBean;
	}

	@Override
	public ContentValues toContent(TrackObjectForBean bean) {
		TrackObject track = bean.getObjectForBean();
		ContentValues values = BaseDTStorageHelper.toCommonContent(track);
		values.put("fromTime", track.getFromTime());
		values.put("toTime", track.getToTime());
		values.put("timing", track.getTiming());
		
		return values;
	}

	@Override
	public Map<String,String> getColumnDefinitions() {
		Map<String,String> defs = BaseDTStorageHelper.getCommonColumnDefinitions();

		defs.put("fromTime", "INTEGER");
		defs.put("toTime", "INTEGER");
		defs.put("timing", "TEXT");
		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	
}
