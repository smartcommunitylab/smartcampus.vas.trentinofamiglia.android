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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import eu.trentorise.smartcampus.android.common.Utils;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.ISynchronizer;
import eu.trentorise.smartcampus.storage.sync.SyncData;
import eu.trentorise.smartcampus.storage.sync.SyncStorageHelper;
import eu.trentorise.smartcampus.storage.sync.SyncStorageHelperWithPaging;
import eu.trentorise.smartcampus.storage.sync.SyncStorageWithPaging;
import eu.trentorise.smartcampus.territoryservice.TerritoryService;
import eu.trentorise.smartcampus.territoryservice.TerritoryServiceException;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DTParamsHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.GenericObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.PoiObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObjectForBean;

/**
 * Specific storage that deletes the old data upon sync complete
 * 
 * @author raman
 * 
 */
public class DTSyncStorage extends SyncStorageWithPaging {

	private static final Map<String, Object> exclude = new HashMap<String, Object>();
	private static final Map<String, Object> include = new HashMap<String, Object>();

	public DTSyncStorage(Context context, String appToken, String dbName, int dbVersion, StorageConfiguration config) {
		super(context, appToken, dbName, dbVersion, config);
		Map<String, Object> map = null;
		map = DTParamsHelper.getExcludeArray();
		if (map != null)
			exclude.putAll(map);

		map = DTParamsHelper.getIncludeArray();
		if (map != null)
			include.putAll(map);
	}

	@Override
	protected SyncStorageHelper createHelper(Context context, String dbName, int dbVersion, StorageConfiguration config) {
		return new DTSyncStorageHelper(context, dbName, dbVersion, config);
	}

	public void synchronize(final String token, final TerritoryService tService) throws StorageConfigurationException,
			DataException, SecurityException, ConnectionException, ProtocolException {
		synchronize(new ISynchronizer() {

			@Override
			public SyncData fetchSyncData(Long version, SyncData in) throws SecurityException, ConnectionException,
					ProtocolException {
				try {
					eu.trentorise.smartcampus.territoryservice.model.SyncData data = tService.synchronize(version,
							include, exclude, token);

					SyncData dbData = new SyncData();
					dbData.setVersion(data.getVersion());

					dbData.setInclude(data.getInclude());

					dbData.setExclude(data.getExclude());

					dbData.setDeleted(convertToBasicObjectDeleted(data.getDeleted()));

					dbData.setUpdated(convertToBasicObject(data.getUpdated()));

					((DTSyncStorageHelper) helper).removeOld();
					return dbData;
				} catch (TerritoryServiceException e) {
					throw new ProtocolException(e.getMessage());
				}
			}
		});

	}

	protected Map<String, List<String>> convertToBasicObjectDeleted(Map<String, List<String>> deleted) {
		Map<String, List<String>> returnDTOObjects = new HashMap<String, List<String>>();
		Iterator it = deleted.entrySet().iterator();
		while (it.hasNext()) {
			// for every map element iterate the entire list
			{
				Map.Entry pairs = (Map.Entry) it.next();
				String key = (String) pairs.getKey();
				Class<? extends GenericObjectForBean> cls = getClassFromName(key);

				List<String> dtoObjects = (List<String>) pairs.getValue();
				// List<Object> basicobjects = new ArrayList<Object>();
				// for (Object object: dtoObjects)
				// {
				// GenericObjectForBean newObject = null;
				// if (PoiObjectForBean.class.equals(cls)){
				// newObject = new PoiObjectForBean();
				// newObject.setObjectForBean(Utils.convertObjectToData(POIObject.class,
				// object));
				// } else if (EventObjectForBean.class.equals(key)) {
				// newObject = new EventObjectForBean();
				// newObject.setObjectForBean(Utils.convertObjectToData(EventObject.class,
				// object));
				// } else if (StoryObjectForBean.class.equals(key)) {
				// newObject = new StoryObjectForBean();
				// newObject.setObjectForBean(
				// Utils.convertObjectToData(StoryObject.class, object));
				// }
				// //convert the single element
				// basicobjects.add(newObject);
				// //add the element to the return list
				// }
				// add the list to the return map
				// key or the new one???
				returnDTOObjects.put(cls.getCanonicalName(), dtoObjects);
			}
			// System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
		return returnDTOObjects;
	}

	private Class<? extends GenericObjectForBean> getClassFromName(String key) {
		Class<? extends GenericObjectForBean> cls = null;
		if ("eu.trentorise.smartcampus.dt.model.POIObject".equals(key)) {
			cls = PoiObjectForBean.class;
		} else if ("eu.trentorise.smartcampus.dt.model.EventObject".equals(key)) {
			cls = EventObjectForBean.class;

		} else if ("eu.trentorise.smartcampus.dt.model.InfoObject".equals(key)) {
			cls = InfoObjectForBean.class;

		} else if ("eu.trentorise.smartcampus.dt.model.TrackObject".equals(key)) {
			cls = TrackObjectForBean.class;

		}
		return cls;
	}

	protected Map<String, List<Object>> convertToBasicObject(Map<String, List<Object>> map) {
		Map<String, List<Object>> returnDTOObjects = new HashMap<String, List<Object>>();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			// for every map element iterate the entire list
			{
				Map.Entry pairs = (Map.Entry) it.next();
				String key = (String) pairs.getKey();
				Class<? extends GenericObjectForBean> cls = getClassFromName(key);

				List<Object> dtoObjects = (List<Object>) pairs.getValue();
				List<Object> basicobjects = new ArrayList<Object>();
				for (Object object : dtoObjects) {
					GenericObjectForBean newObject = null;
					if (PoiObjectForBean.class.equals(cls)) {
						newObject = new PoiObjectForBean();
						newObject.setObjectForBean(Utils.convertObjectToData(POIObject.class, object));
					} else if (EventObjectForBean.class.equals(cls)) {
						newObject = new EventObjectForBean();
						newObject.setObjectForBean(Utils.convertObjectToData(EventObject.class, object));
					} else if (InfoObjectForBean.class.equals(cls)) {
						newObject = new InfoObjectForBean();
						newObject.setObjectForBean(Utils.convertObjectToData(InfoObject.class, object));
					} else if (TrackObjectForBean.class.equals(cls)) {
						newObject = new TrackObjectForBean();
						newObject.setObjectForBean(Utils.convertObjectToData(TrackObject.class, object));
					}

					// convert the single element
					basicobjects.add(newObject);
					// add the element to the return list
				}
				// add the list to the return map
				// key or the new one???
				returnDTOObjects.put(cls.getCanonicalName(), basicobjects);
			}
			// System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
		return returnDTOObjects;
	}

	private static class DTSyncStorageHelper extends SyncStorageHelperWithPaging {

		// sync filtering: exclude transit stops

		// static {
		// exclude.put("source", "smartplanner-transitstops");
		// }

		public DTSyncStorageHelper(Context context, String dbName, int version, StorageConfiguration config) {
			super(context, dbName, version, config);
		}

		@Override
		public SyncData getDataToSync(long version) throws StorageConfigurationException {
			SyncData data = super.getDataToSync(version);
			data.setExclude(exclude);
			data.setInclude(include);
			return data;
		}

		private void removeOld() {
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 0);
			try {
				db.delete("events", "attending IS NULL AND toTime < " + calendar.getTimeInMillis(), null);
				// c.moveToNext();
				// int total = c.getInt(0);
				// if (total > num) {
				// int toDelete = total - num;
				// c =
				// db.rawQuery("SELECT id FROM notifications WHERE starred = 0 ORDER BY timestamp ASC",
				// null);
				// c.moveToFirst();
				// for (int i = 0; i < toDelete; i++) {
				// db.delete("notifications", "id = '" + c.getString(0) + "'",
				// null);
				// c.moveToNext();
				// }
				// }
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

	}

}
