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

public class EventStorageHelper implements BeanStorageHelper<EventObjectForBean> {

	@Override
	public EventObjectForBean toBean(Cursor cursor) {
		
//		for (int i = 0; i < cursor.getCount(); i++) {
//			Log.e("helper",cursor.getString(cursor.getColumnIndex("title")));
//			cursor.moveToNext();
//		}
		EventObjectForBean returnEventObjectForBean = new EventObjectForBean();
		EventObject event = new EventObject();
		BaseDTStorageHelper.setCommonFields(cursor, event);
		
		event.setPoiId(cursor.getString(cursor.getColumnIndex("poiId")));
		event.setFromTime(cursor.getLong(cursor.getColumnIndex("fromTime")));
		event.setToTime(cursor.getLong(cursor.getColumnIndex("toTime")));
		event.setTiming(cursor.getString(cursor.getColumnIndex("timing")));
		event.setAttendees(cursor.getInt(cursor.getColumnIndex("attendees")));
		String attending = cursor.getString(cursor.getColumnIndex("attending"));
		event.setAttending(attending == null ? Collections.<String>emptyList() : Collections.singletonList(attending));


		returnEventObjectForBean.setObjectForBean(event);
		return returnEventObjectForBean;
	}

	@Override
	public ContentValues toContent(EventObjectForBean bean) {
		EventObject event = bean.getObjectForBean();

		ContentValues values = BaseDTStorageHelper.toCommonContent(event);
		
		values.put("poiId", event.getPoiId());
		values.put("fromTime", event.getFromTime());
		values.put("toTime", event.getToTime());
//		values.put("timing", bean.getTimingFormatted());
		values.put("timing", event.getTiming());
		values.put("attendees", event.getAttendees());
		values.put("attending", event.getAttending() != null && ! event.getAttending().isEmpty() ? event.getAttending().get(0) : null);
//		values.put("poiIdUserDefined", bean.isPoiIdUserDefined() ? 1 : 0);
//		values.put("fromTimeUserDefined", bean.isFromTimeUserDefined() ? 1 : 0);
//		values.put("toTimeUserDefined", bean.isToTimeUserDefined() ? 1 : 0);
		
		return values;
	}

	@Override
	public Map<String,String> getColumnDefinitions() {
		Map<String,String> defs = BaseDTStorageHelper.getCommonColumnDefinitions();

		defs.put("poiId", "TEXT");
		defs.put("fromTime", "INTEGER");
		defs.put("toTime", "INTEGER");
		defs.put("timing", "TEXT");
		defs.put("attendees", "INTEGER");
		defs.put("attending", "TEXT");

		defs.put("poiIdUserDefined", "INTEGER");
		defs.put("fromTimeUserDefined", "INTEGER");
		defs.put("toTimeUserDefined", "INTEGER");

		return defs;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	
}
