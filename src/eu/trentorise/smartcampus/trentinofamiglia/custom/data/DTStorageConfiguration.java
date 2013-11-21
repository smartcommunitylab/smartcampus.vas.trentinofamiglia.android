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

import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.BeanStorageHelper;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.InfoObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.PoiObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObjectForBean;

public class DTStorageConfiguration implements StorageConfiguration {
	private static final long serialVersionUID = 906503482979452854L;

	@SuppressWarnings("unchecked")
	private static Class<? extends BasicObject>[] classes = (Class<? extends BasicObject>[])new Class<?>[]{POIObject.class, LocalEventObject.class,InfoObject.class, TrackObject.class};												
	private static BeanStorageHelper<PoiObjectForBean> poiHelper = new POIStorageHelper();
	private static BeanStorageHelper<EventObjectForBean> eventHelper = new EventStorageHelper();
	private static BeanStorageHelper<TrackObjectForBean> trackHelper = new TrackStorageHelper();
	private static BeanStorageHelper<InfoObjectForBean> infotHelper = new InfoStorageHelper();	
	@Override
	public Class<? extends BasicObject>[] getClasses() {
		return classes;
	}

	@Override
	public String getTableName(Class<? extends BasicObject> cls) throws StorageConfigurationException {
		if (cls.equals(PoiObjectForBean.class)||cls.equals(POIObject.class)) {
			return "pois";
		}
		if (cls.equals(EventObjectForBean.class)||cls.equals(LocalEventObject.class)) {
			return "events";
		}
		if (cls.equals(TrackObjectForBean.class)||cls.equals(TrackObject.class)) {
			return "tracks";
		}
		if (cls.equals(InfoObjectForBean.class)||cls.equals(InfoObject.class)) {
			return "infos";
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BasicObject> BeanStorageHelper<T> getStorageHelper(Class<T> cls) throws StorageConfigurationException {
		if (cls.equals(POIObject.class)||(cls.equals(PoiObjectForBean.class))) {
			return (BeanStorageHelper<T>) poiHelper;
		}
		if (cls.equals(LocalEventObject.class)||(cls.equals(EventObjectForBean.class))) {
			return (BeanStorageHelper<T>) eventHelper;
		}
		if (cls.equals(TrackObject.class)||(cls.equals(TrackObjectForBean.class))) {
			return (BeanStorageHelper<T>) trackHelper;
		}
		if (cls.equals(InfoObject.class)||(cls.equals(InfoObjectForBean.class))) {
			return (BeanStorageHelper<T>) infotHelper;
		}
		return null;
	}

}
