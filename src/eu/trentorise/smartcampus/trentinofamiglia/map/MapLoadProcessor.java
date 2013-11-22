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
package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.Collection;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;

import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;

public abstract class MapLoadProcessor extends AbstractAsyncTaskProcessor<Void, Collection<? extends BaseDTObject>> {
	
	protected GoogleMap map = null;
	private MapObjectContainer container;

	public MapLoadProcessor(Activity activity, MapObjectContainer container, GoogleMap map) {
		super(activity);
		this.map = map;
		this.container = container;
	}

	
	@Override
	public Collection<? extends BaseDTObject> performAction(Void... params) throws SecurityException, Exception {
		return getObjects();
	}

	@Override
	public void handleResult(Collection<? extends BaseDTObject> objects) {
		if (objects != null) {
			container.addObjects(objects);
		}
	}

	protected abstract Collection<? extends BaseDTObject> getObjects()  throws SecurityException, Exception;
	
}
