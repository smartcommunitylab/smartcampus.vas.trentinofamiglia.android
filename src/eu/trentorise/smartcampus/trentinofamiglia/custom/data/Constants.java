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

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Constants {

	/** Should be set in App metadata in order to properly manage the sync */
	public static final String DT_SYNC_AUTHORITY = "dt-sync-authority";

//	public static final String APP_TOKEN = "discovertrento";
	public static final String SERVICE = "/smartcampus.vas.discovertrento.web";
	public static final String SYNC_SERVICE = "/smartcampus.vas.discovertrento.web/sync";
	public static final int SYNC_INTERVAL = 5;
	public static final String SYNC_DB_NAME = "discovertrentodb";
	public static final String PREFS = "eu.trentorise.smartcampus.dt.preferences";
	public static final String PREFS_USER_ID = "user.profile.id";
	public static final String PREFS_USER_SOCIAL_ID = "user.profile.socialId";
	public static final String PREFS_USER_NAME = "user.profile.name";
	public static final String PREFS_USER_SURNAME = "user.profile.surname";

	public static final String CM_SERVICE = "/smartcampus.vas.community-manager.web";
	
	
	public static final String TYPE_EVENT = "event";
	public static final String TYPE_LOCATION = "location";
	public static final String TYPE_STORY = "story";
	
	public static String getAuthority(Context ctx) throws NameNotFoundException {
		ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
		return ai.metaData.getString(DT_SYNC_AUTHORITY);
	}

}
