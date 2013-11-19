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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.LocationHelper;
import eu.trentorise.smartcampus.android.common.tagging.SemanticSuggestion;
import eu.trentorise.smartcampus.android.common.tagging.SuggestionHelper;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteConnector.CLIENT_TYPE;
import eu.trentorise.smartcampus.profileservice.BasicProfileService;
import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.storage.BasicObject;
import eu.trentorise.smartcampus.storage.DataException;
import eu.trentorise.smartcampus.storage.StorageConfigurationException;
import eu.trentorise.smartcampus.storage.db.StorageConfiguration;
import eu.trentorise.smartcampus.storage.sync.Utils;
import eu.trentorise.smartcampus.territoryservice.TerritoryService;
import eu.trentorise.smartcampus.territoryservice.TerritoryServiceException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.EventObject;
import eu.trentorise.smartcampus.territoryservice.model.ObjectFilter;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.territoryservice.model.StoryObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DTParamsHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.EventObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.GenericObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.PoiObjectForBean;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhenForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.WhereForSearch;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapManager;

public class DTHelper {

	public static final int SYNC_REQUIRED = 2;
	public static final int SYNC_NOT_REQUIRED = 0;
	public static final int SYNC_REQUIRED_FIRST_TIME = 3;
	public static final int SYNC_ONGOING = 1;
	private static final int CURR_DB = 4;

	// tutorial's stuff

	private static final String TUT_PREFS = "dt_tut_prefs";
	private static final String TOUR_PREFS = "dt_wantTour";
	private static final String FIRST_LAUNCH_PREFS = "dt_firstLaunch";
	private static SCAccessProvider accessProvider = null;
	private static TerritoryService tService;

	public static TerritoryService gettService() {
		return tService;
	}

	public static enum Tutorial {
		NOTIF("notifTut"), PLACES("placesTut"), EVENTS("eventsTut"), STORIES("storyTut"), MENU("menuTut"), RATING(
				"ratingTut");
		/**
		 * @param text
		 */
		private Tutorial(final String text) {
			this.text = text;
		}

		private final String text;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return text;
		}
	}

	private static DTHelper instance = null;

	// private static SCAccessProvider accessProvider = new
	// EmbeddedSCAccessProvider();

	// private SyncManager mSyncManager;
	private static Context mContext;
	private StorageConfiguration config = null;
	// private SyncStorageConfiguration config = null;
	private DTSyncStorage storage = null;
//	private static RemoteStorage remoteStorage = null;

//	private ProtocolCarrier mProtocolCarrier = null;

	private static LocationHelper mLocationHelper;

	private boolean syncInProgress = false;
	private FragmentActivity rootActivity = null;
	static BasicProfile bp = null;

	// private String myToken = null;
	// private UserProfile userProfile = null;

	public static void init(final Context mContext) {
		if (instance == null)
			instance = new DTHelper(mContext);
		
		tService = new TerritoryService(getAppUrl() + "core.territory");

		new AsyncTask<Void, Void, BasicProfile>() {
			@Override
			protected BasicProfile doInBackground(Void... params) {
				try {
					String token = SCAccessProvider.getInstance(mContext).readToken(mContext);
					BasicProfileService service = new BasicProfileService(getAppUrl() + "aac");
					bp = service.getBasicProfile(token);
					return bp;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}.execute();
	}

	private static String getAppUrl() {
		String returnAppUrl = "";
		try {
			returnAppUrl = GlobalConfig.getAppUrl(mContext);
			if (!returnAppUrl.endsWith("/"))
				returnAppUrl = returnAppUrl.concat("/");
		} catch (Exception e) { //protocolexception
			e.printStackTrace();
		}
		return returnAppUrl;
	}

	public static SCAccessProvider getAccessProvider() {
		if (accessProvider == null)
			accessProvider = SCAccessProvider.getInstance(mContext);
		return accessProvider;
	}

	public static String getAuthToken() {
		String mToken = null;
		try {
			mToken = getAccessProvider().readToken(mContext);
		} catch (AACException e) {
			e.printStackTrace();
		}
		return mToken;
	}

	// public static String getAuthToken() {
	// try {
	// return SCAccessProvider.getInstance(mContext).readToken(mContext);
	// }
	// catch (AACException e) {
	// return null;
	// }
	// }

	public static String getUserId() {
		// UserData data = getAccessProvider().readUserData(instance.mContext,
		// null);
		if (bp != null) {
			return bp.getUserId();
		} else
			getUserProfile();
		return null;
	}

	private static void getUserProfile() {

	}

	private static DTHelper getInstance() throws DataException {
		if (instance == null)
			throw new DataException("DTHelper is not initialized");
		return instance;
	}

	protected DTHelper(Context mContext) {
		super();

		DTHelper.mContext = mContext;
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			RemoteConnector.setClientType(CLIENT_TYPE.CLIENT_WILDCARD);
		}
		accessProvider = SCAccessProvider.getInstance(mContext);
		DTParamsHelper.init(mContext);
		MapManager.initWithParam();
		// this.mSyncManager = new SyncManager(mContext,
		// DTSyncStorageService.class);
		config = new DTStorageConfiguration();

		// this.config = new SyncStorageConfiguration(sc,
		// GlobalConfig.getAppUrl(mContext), Constants.SYNC_SERVICE,
		// Constants.SYNC_INTERVAL);
		if (Utils.getDBVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) != CURR_DB) {
			Utils.writeObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME, 0);
		}
		this.storage = new DTSyncStorage(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME, CURR_DB,
				config);
//		this.mProtocolCarrier = new ProtocolCarrier(mContext, DTParamsHelper.getAppToken());

		// LocationManager locationManager = (LocationManager)
		// mContext.getSystemService(Context.LOCATION_SERVICE);
		// locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
		// 0, 0, new DTLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
		// 0, 0, new DTLocationListener());
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0, new DTLocationListener());
		setLocationHelper(new LocationHelper(mContext));
	}

	/**
	 * @return synchronization required status:
	 *         <ul>
	 *         <li>0 if no sync needed</li>
	 *         <li>1 if sync is ongoing</li>
	 *         <li>2 if sync is required</li>
	 *         <li>3 if sync is required first time</li>
	 *         </ul>
	 *         0 if no if the DB synchronization is required: the last
	 *         synchronization happened more than
	 *         {@link Constants#SYNC_INTERVAL} minutes ago or is ongoing.
	 * @throws DataException
	 * @throws NameNotFoundException
	 */
	public static int syncRequired() throws DataException, NameNotFoundException {
		if (getInstance().syncInProgress)
			return SYNC_ONGOING;
		long last = Utils.getLastObjectSyncTime(mContext, DTParamsHelper.getAppToken(),
				Constants.SYNC_DB_NAME);
		if (System.currentTimeMillis() - last > Constants.SYNC_INTERVAL * 60 * 1000) {
			if (last > 0)
				return SYNC_REQUIRED;
			return SYNC_REQUIRED_FIRST_TIME;
		}
		return SYNC_NOT_REQUIRED;
	}

	/**
	 * Enable auot sync for the activity life-cycle
	 * 
	 * @throws NameNotFoundException
	 * @throws DataException
	 */
	public static void activateAutoSync() {
		try {
			String authority = Constants.getAuthority(mContext);
			Account account = new Account(
					eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
					eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));

			ContentResolver.setIsSyncable(account, authority, 1);
			ContentResolver.setSyncAutomatically(account, authority, true);
			ContentResolver.addPeriodicSync(account, authority, new Bundle(), Constants.SYNC_INTERVAL * 60);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FragmentActivity start(FragmentActivity activity) throws RemoteException,
			DataException, StorageConfigurationException, SecurityException, ConnectionException, ProtocolException,
			NameNotFoundException, AACException {
		getInstance().rootActivity = activity;
		try {
			if (getInstance().syncInProgress)
				return null;

			if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) <= 0) {
				Utils.writeObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME,
						1L);
			}

			getInstance().syncInProgress = true;
			getInstance().storage.synchronize(getAuthToken(), tService);
			activateAutoSync();

			// getInstance().storage.synchronize(getAuthToken(),
			// GlobalConfig.getAppUrl(mContext),
			// Constants.SYNC_SERVICE);

		} finally {
			getInstance().syncInProgress = false;
		}
		return getInstance().rootActivity;
	}

	public static void synchronize() throws RemoteException, DataException, StorageConfigurationException,
			SecurityException, ConnectionException, ProtocolException, AACException {
		getInstance().storage.synchronize(getAuthToken(), tService);
		// getInstance().storage.synchronize(getAuthToken(),
		// GlobalConfig.getAppUrl(mContext),
		// Constants.SYNC_SERVICE);
		// ContentResolver.requestSync(new
		// Account(eu.trentorise.smartcampus.ac.Constants.ACCOUNT_NAME,
		// eu.trentorise.smartcampus.ac.Constants.ACCOUNT_TYPE),
		// "eu.trentorise.smartcampus.dt", new Bundle());
	}

	public static void destroy() {
		try {
			String authority = Constants.getAuthority(mContext);
			Account account = new Account(
					eu.trentorise.smartcampus.ac.Constants.getAccountName(mContext),
					eu.trentorise.smartcampus.ac.Constants.getAccountType(mContext));
			ContentResolver.removePeriodicSync(account, authority, new Bundle());
			ContentResolver.setSyncAutomatically(account, authority, false);
			ContentResolver.setIsSyncable(account, authority, 0);
		} catch (Exception e) {
			Log.e(DTHelper.class.getName(), "Failed destroy: " + e.getMessage());
		}
	}

	// public static Collection<POIObject> getAllPOI() throws DataException,
	// StorageConfigurationException, ConnectionException, ProtocolException,
	// SecurityException {
	// if (Utils.getObjectVersion(instance.mContext,
	// DTParamsHelper.getAppToken()) > 0) {
	// return getInstance().storage.getObjects(POIObject.class);
	// } else {
	// return Collections.emptyList();
	// }
	// }
	public static List<String> getAllPOITitles() {

		Cursor cursor = null;
		try {
			cursor = getInstance().storage.rawQuery("select title from pois", null);
			if (cursor != null) {
				List<String> result = new ArrayList<String>();
				cursor.moveToFirst();
				int i = 0;
				while (cursor.getPosition() < cursor.getCount()) {
					String v = cursor.getString(0);
					if (v != null && v.trim().length() > 0) {
						result.add(v.trim());
					}
					cursor.moveToNext();
					i++;
				}
				return result;
			}
		} catch (Exception e) {
			Log.e(DTHelper.class.getName(), "" + e.getMessage());
		} finally {
			try {
				getInstance().storage.cleanCursor(cursor);
			} catch (DataException e) {
			}
		}
		return Collections.emptyList();
	}

	public static POIObject findPOIByTitle(String text) {
		try {

			/* need conversion */
			Collection<PoiObjectForBean> poiCollection = getInstance().storage.query(PoiObjectForBean.class,
					"title = ?", new String[] { text });

			if (poiCollection.size() > 0)
				return poiCollection.iterator().next().getObjectForBean();
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public static POIObject findPOIById(String poiId) {
		try {
			PoiObjectForBean poi = getInstance().storage.getObjectById(poiId, PoiObjectForBean.class);
			return poi.getObjectForBean();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * return true if the object was created and false if updated
	 * 
	 * @param poi
	 * @return
	 * @throws DataException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 * @throws RemoteException
	 * @throws StorageConfigurationException
	 */
	/*
	 * public static boolean savePOI(POIObject poi) throws DataException,
	 * ConnectionException, ProtocolException, SecurityException,
	 * RemoteException, StorageConfigurationException { String requestService =
	 * null; Method method = null; Boolean result = null; if (poi.getId() ==
	 * null) { if (poi.createdByUser()) requestService = Constants.SERVICE +
	 * "/eu.trentorise.smartcampus.dt.model.UserPOIObject"; else throw new
	 * DataException("cannot create service object"); method = Method.POST;
	 * result = true; } else { if (poi.createdByUser()) requestService =
	 * Constants.SERVICE + "/eu.trentorise.smartcampus.dt.model.UserPOIObject/"
	 * + poi.getId(); else requestService = Constants.SERVICE +
	 * "/eu.trentorise.smartcampus.dt.model.ServicePOIObject/" + poi.getId();
	 * method = Method.PUT; result = false; } MessageRequest request = new
	 * MessageRequest(GlobalConfig.getAppUrl(mContext),
	 * requestService); request.setMethod(method); String json =
	 * eu.trentorise.smartcampus.android.common.Utils.convertToJSON(poi);
	 * request.setBody(json);
	 * 
	 * // getRemote(instance.mContext, instance.token).create(poi);
	 * 
	 * synchronize(); return result; }
	 */

	/**
	 * return the POI created or updated. Null if is not created
	 * 
	 * @param poi
	 * @return
	 * @throws DataException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 * @throws RemoteException
	 * @throws StorageConfigurationException
	 * @throws TerritoryServiceException
	 * @throws AACException
	 */

	public static POIObject savePOI(POIObject poi) throws DataException, ConnectionException, ProtocolException,
			SecurityException, RemoteException, StorageConfigurationException, TerritoryServiceException, AACException {
		if (poi.getId() == null)
			poi = tService.createPOI(poi, getAuthToken());
		else
			poi = tService.updatePOI(poi.getId(), poi, getAuthToken());
		synchronize();
		return poi;
	}

	/**
	 * return true in case of create and false in case of update
	 * 
	 * @param event
	 * @return
	 * @throws RemoteException
	 * @throws DataException
	 * @throws StorageConfigurationException
	 * @throws ConnectionException
	 * @throws ProtocolException
	 * @throws SecurityException
	 * @throws TerritoryServiceException
	 * @throws AACException
	 */
	public static Boolean saveEvent(EventObject event) throws RemoteException, DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
			TerritoryServiceException, AACException {
		Boolean result = null;
		if (event.getId() == null) {
			event = tService.createEvent(event, getAuthToken());
			result = true;
		} else {
			event = tService.updateEvent(event.getId(), event, getAuthToken());
			result = false;
		}
		// String requestService = null;
		// Method method = null;
		// Boolean result = null;
		// if (event.getId() == null) {
		// if (event.createdByUser())
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserEventObject";
		// else
		// throw new DataException("cannot create service object");
		// method = Method.POST;
		// result = true;
		// } else {
		// if (event.createdByUser())
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserEventObject/"
		// + event.getId();
		// else
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.ServiceEventObject/"
		// + event.getId();
		// method = Method.PUT;
		// result = false;
		// }
		// MessageRequest request = new
		// MessageRequest(GlobalConfig.getAppUrl(mContext),
		// requestService);
		// request.setMethod(method);
		// String json =
		// eu.trentorise.smartcampus.android.common.Utils.convertToJSON(event);
		// request.setBody(json);
		//
		// MessageResponse msg =
		// getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(),
		// getAuthToken());
		// // getRemote(instance.mContext, instance.token).create(poi);
		// EventObject eventreturn =
		// eu.trentorise.smartcampus.android.common.Utils.convertJSONToObject(msg.getBody(),
		// EventObject.class);
		synchronize();
		return result;
	}

	public static Collection<BaseDTObject> getMostPopular() throws DataException, StorageConfigurationException,
			ConnectionException, ProtocolException, SecurityException, TerritoryServiceException, AACException {
		ArrayList<BaseDTObject> list = new ArrayList<BaseDTObject>();
		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			Collection<PoiObjectForBean> pois = getInstance().storage.getObjects(PoiObjectForBean.class);
			for (PoiObjectForBean poiBean : pois) {
				list.add(poiBean.getObjectForBean());
			}
			if (pois.size() > 20) {
				return list.subList(0, 20);
			}
			return list;
		} else {
			ObjectFilter filter = new ObjectFilter();
			filter.setLimit(20);
			List<POIObject> pois = tService.getPOIs(filter, getAuthToken());
			for (POIObject poiObject : pois) {
				list.add(poiObject);
			}
			return list;
		}
	}

	public static Collection<POIObject> getPOIByCategory(int position, int size, String... inCategories)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException, TerritoryServiceException, AACException {
		ArrayList<POIObject> returnlist = new ArrayList<POIObject>();

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " type = ?";
				} else {
					where += " type is null";
				}
			}

			Collection<PoiObjectForBean> pois = getInstance().storage.query(PoiObjectForBean.class, where,
					nonNullCategories.toArray(new String[nonNullCategories.size()]), position, size, "title ASC");
			for (PoiObjectForBean poiBean : pois) {
				returnlist.add(poiBean.getObjectForBean());
			}
			return returnlist;
		} else {
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setSkip(position);
				filter.setLimit(size);
				filter.setTypes(Arrays.asList(categories));
				returnlist.addAll(tService.getPOIs(filter, getAuthToken()));
			}
			return returnlist;
		}
	}

	public static Collection<POIObject> searchPOIs(int position, int size, String text) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
			TerritoryServiceException, AACException {
		ArrayList<POIObject> returnlist = new ArrayList<POIObject>();

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			if (text == null || text.trim().length() == 0) {
				Collection<PoiObjectForBean> pois = getInstance().storage.getObjects(PoiObjectForBean.class);
				for (PoiObjectForBean poiBean : pois) {
					returnlist.add(poiBean.getObjectForBean());
				}
				return returnlist;
			}
			Collection<PoiObjectForBean> pois = getInstance().storage.query(PoiObjectForBean.class, "pois MATCH ?",
					new String[] { text }, position, size, "title ASC");
			for (PoiObjectForBean poiBean : pois) {
				returnlist.add(poiBean.getObjectForBean());
			}
			return returnlist;
		} else {
			ObjectFilter filter = new ObjectFilter();
			Map<String, Object> criteria = new HashMap<String, Object>(1);
			criteria.put("text", text);
			filter.setCriteria(criteria);
			filter.setSkip(position);
			filter.setLimit(size);
			returnlist.addAll(tService.getPOIs(filter, getAuthToken()));
			return returnlist;
		}
	}

	public static Collection<POIObject> searchPOIsByCategory(int position, int size, String text,
			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException, TerritoryServiceException, AACException {
		ArrayList<POIObject> returnlist = new ArrayList<POIObject>();

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " type = ?";
				} else {
					where += " type is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
			List<String> parameters = nonNullCategories;

			if (text != null) {
				where += "and ( pois MATCH ? )";
				parameters.add(text);
			}
			Collection<PoiObjectForBean> pois = getInstance().storage.query(PoiObjectForBean.class, where,
					parameters.toArray(new String[parameters.size()]), position, size, "title ASC");
			for (PoiObjectForBean poiBean : pois) {
				returnlist.add(poiBean.getObjectForBean());
			}
			return returnlist;
		} else {
			ArrayList<POIObject> result = new ArrayList<POIObject>();
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				returnlist.addAll(tService.getPOIs(filter, getAuthToken()));
			}
			return result;
		}
	}

	public static Collection<EventObject> searchEventsByCategory(int position, int size, String text,
			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException, TerritoryServiceException, AACException {
		ArrayList<EventObject> returnlist = new ArrayList<EventObject>();

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " type = ?";
				} else {
					where += " type is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
			List<String> parameters = nonNullCategories;

			if (text != null) {
				where += "AND ( events MATCH ? ) AND fromTime > " + getCurrentDateTimeForSearching();
				parameters.add(text);
			}
			Collection<EventObjectForBean> events = getInstance().storage.query(EventObjectForBean.class, where,
					parameters.toArray(new String[parameters.size()]), position, size, "fromTime ASC");
			for (EventObjectForBean eventBean : events) {
				returnlist.add(eventBean.getObjectForBean());
			}
			return returnlist;
		} else {
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				returnlist.addAll(tService.getEvents(filter, getAuthToken()));
			}
			return returnlist;
		}
	}

	public static long getCurrentDateTimeForSearching() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.add(Calendar.DATE, -1);
		return c.getTimeInMillis();
	}

//	public static Collection<StoryObject> searchStoriesByCategory(int position, int size, String text,
//			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
//			ProtocolException, SecurityException, TerritoryServiceException, AACException {
//		ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
//
//		if (inCategories == null || inCategories.length == 0)
//			return Collections.emptyList();
//
//		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
//
//		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
//			List<String> nonNullCategories = new ArrayList<String>();
//			String where = "";
//			for (int i = 0; i < categories.length; i++) {
//				if (where.length() > 0)
//					where += " or ";
//				if (categories[i] != null) {
//					nonNullCategories.add(categories[i]);
//					where += " type = ?";
//				} else {
//					where += " type is null";
//				}
//			}
//			if (where.length() > 0) {
//				where = "(" + where + ")";
//			}
//			List<String> parameters = nonNullCategories;
//
//			if (text != null) {
//				where += "and ( stories MATCH ? )";
//				parameters.add(text);
//			}
//			Collection<StoryObjectForBean> stories = getInstance().storage.query(StoryObjectForBean.class, where,
//					parameters.toArray(new String[parameters.size()]), position, size, "title ASC");
//			for (StoryObjectForBean storyBean : stories) {
//				returnlist.add(storyBean.getObjectForBean());
//			}
//			return returnlist;
//
//		} else {
//			for (int c = 0; c < categories.length; c++) {
//				ObjectFilter filter = new ObjectFilter();
//				filter.setTypes(Arrays.asList(categories));
//				filter.setSkip(position);
//				filter.setLimit(size);
//				returnlist.addAll(tService.getStories(filter, getAuthToken()));
//			}
//			return returnlist;
//		}
//	}

	public static Collection<LocalEventObject> getEventsByCategories(int position, int size, String... inCategories)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException, TerritoryServiceException, AACException {
		ArrayList<LocalEventObject> returnlist = new ArrayList<LocalEventObject>();

		if (inCategories == null || inCategories.length == 0)
			return Collections.emptyList();

		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			List<String> nonNullCategories = new ArrayList<String>();
			String where = "";
			for (int i = 0; i < categories.length; i++) {
				if (where.length() > 0)
					where += " or ";
				if (categories[i] != null) {
					nonNullCategories.add(categories[i]);
					where += " type = ?";
				} else {
					where += " type is null";
				}
			}
			if (where.length() > 0) {
				where = "(" + where + ")";
			}
//			if (where.length() > 0) where += " AND ";
//			where += "fromTime > " + getCurrentDateTimeForSearching();
			Collection<EventObjectForBean> events = getInstance().storage.query(EventObjectForBean.class, where,
					nonNullCategories.toArray(new String[nonNullCategories.size()]), position, size, "fromTime ASC");
			for (EventObjectForBean eventBean : events) {
				LocalEventObject event = new LocalEventObject();
				event.setEventFromEventObjectForBean(eventBean);
				returnlist.add(event);
			}
			return returnlist;
		} else {
			for (int c = 0; c < categories.length; c++) {
				ObjectFilter filter = new ObjectFilter();
				filter.setTypes(Arrays.asList(categories));
				filter.setSkip(position);
				filter.setLimit(size);
				List<EventObject> events = tService.getEvents(filter, getAuthToken());
				returnlist.addAll(eu.trentorise.smartcampus.trentinofamiglia.custom.Utils.convertToLocalEvent(events));

			}
			return returnlist;

		}
	}

	public static Collection<LocalEventObject> searchTodayEvents(int position, int size, String text)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException, TerritoryServiceException, AACException {
		ArrayList<LocalEventObject> returnlist = new ArrayList<LocalEventObject>();

		// Date now = new Date();
		Calendar cal = Calendar.getInstance();
		// cal.setTime(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date tomorrow = cal.getTime();

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			Collection<EventObjectForBean> events = getInstance().storage.query(EventObjectForBean.class, "( toTime > "
					+ getCurrentDateTimeForSearching() + " AND fromTime < " + tomorrow.getTime() + " ) ", null,
					position, size, "fromTime ASC");
			/* convert from eventobj to localeventobj */
			for (EventObjectForBean eventBean : events) {
				LocalEventObject event = new LocalEventObject();
				event.setEventFromEventObjectForBean(eventBean);
				returnlist.add(event);
			}
			return returnlist;
		} else {
			ObjectFilter filter = new ObjectFilter();
			Map<String, Object> criteria = new HashMap<String, Object>(1);
			criteria.put("text", text);
			filter.setCriteria(criteria);
			filter.setSkip(position);
			filter.setLimit(size);
			List<EventObject> events = tService.getEvents(filter, getAuthToken());
			returnlist.addAll(eu.trentorise.smartcampus.trentinofamiglia.custom.Utils.convertToLocalEvent(events));
			return returnlist;
		}
	}

	public static Collection<EventObject> getEventsByPOI(int position, int size, String poiId) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
			TerritoryServiceException, AACException {
		ArrayList<EventObject> returnlist = new ArrayList<EventObject>();

		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
			Collection<EventObjectForBean> events = getInstance().storage.query(EventObjectForBean.class,
					"poiId = ? AND fromTime > " + getCurrentDateTimeForSearching(), new String[] { poiId }, position,
					size, "fromTime ASC");
			for (EventObjectForBean eventBean : events) {
				returnlist.add(eventBean.getObjectForBean());
			}
			return returnlist;
		} else {
			ObjectFilter filter = new ObjectFilter();
			Map<String, Object> criteria = new HashMap<String, Object>(1);
			criteria.put("poiId", poiId);
			filter.setCriteria(criteria);
			filter.setSkip(position);
			filter.setLimit(size);
			return tService.getEvents(filter, getAuthToken());
		}
	}

	public static List<SemanticSuggestion> getSuggestions(CharSequence suggest) throws ConnectionException,
			ProtocolException, SecurityException, DataException, AACException {
		return SuggestionHelper.getSuggestions(suggest, mContext,
				GlobalConfig.getAppUrl(mContext), getAuthToken(), DTParamsHelper.getAppToken());
	}

//	private static RemoteStorage getRemote(Context mContext, String token) throws ProtocolException, DataException {
//		if (remoteStorage == null) {
//			remoteStorage = new RemoteStorage(mContext, DTParamsHelper.getAppToken());
//		}
//		remoteStorage.setConfig(token, GlobalConfig.getAppUrl(mContext), Constants.SERVICE);
//		return remoteStorage;
//	}

	public static void endAppFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
		activity.finish();
	}

	public static void showFailure(Activity activity, int id) {
		Toast.makeText(activity, activity.getResources().getString(id), Toast.LENGTH_LONG).show();
	}

	public static boolean deleteEvent(EventObject eventObject) throws DataException, ConnectionException,
			ProtocolException, SecurityException, RemoteException, StorageConfigurationException, AACException,
			TerritoryServiceException {
		if (eventObject.getId() != null) {
			tService.deleteEvent(eventObject.getId(), getAuthToken());
			synchronize();
			return true;
		}
		return false;
	}

	public static boolean deletePOI(POIObject poiObject) throws DataException, ConnectionException, ProtocolException,
			SecurityException, RemoteException, StorageConfigurationException, AACException, TerritoryServiceException {
		if (poiObject.getId() != null) {
			tService.deletePOI(poiObject.getId(), getAuthToken());
			synchronize();
			return true;
		}
		return false;
	}

	public static int rate(BaseDTObject event, int rating) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException,
			TerritoryServiceException, AACException {
		int returnValue = tService.rate(event.getId(), rating, getAuthToken());
		// MessageRequest request = new
		// MessageRequest(GlobalConfig.getAppUrl(mContext),
		// Constants.SERVICE
		// + "/objects/" + event.getId() + "/rate");
		// request.setMethod(Method.PUT);
		// String query = "rating=" + rating;
		// request.setQuery(query);
		// String response = getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(),
		// getAuthToken()).getBody();
		synchronize();
		return returnValue;
	}

	public static BaseDTObject follow(BaseDTObject object) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException, AACException {
		BaseDTObject returnObj = null;

		try {
			if (object instanceof POIObject) {

				returnObj = tService.followPOI(object.getId(), getAuthToken());

			} else if (object instanceof EventObject) {
				returnObj = tService.followEvent(object.getId(), getAuthToken());

			} else if (object instanceof StoryObject) {
				returnObj = tService.followStory(object.getId(), getAuthToken());
			}
		} catch (TerritoryServiceException e) {
			e.printStackTrace();
		}
		// MessageRequest request = new MessageRequest(
		// GlobalConfig.getAppUrl(mContext),
		// Constants.SERVICE + "/objects/" + event.getId() + "/follow");
		// request.setMethod(Method.PUT);
		// String query = "idTopic=" + idTopic;
		// request.setQuery(query);
		// getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(), getAuthToken());
		synchronize();
		return returnObj;
	}

	public static void unfollow(BaseDTObject object) throws ConnectionException, ProtocolException, SecurityException,
			DataException, RemoteException, StorageConfigurationException, AACException {
		try {
			if (object instanceof POIObject) {

				tService.unfollowPOI(object.getId(), getAuthToken());

			} else if (object instanceof EventObject) {
				tService.unfollowEvent(object.getId(), getAuthToken());

			} else if (object instanceof StoryObject) {
				tService.unfollowStory(object.getId(), getAuthToken());

			}
		} catch (TerritoryServiceException e) {
			e.printStackTrace();
		}
		// MessageRequest request = new MessageRequest(
		// GlobalConfig.getAppUrl(mContext),
		// Constants.SERVICE + "/objects/" + event.getId() + "/unfollow");
		// request.setMethod(Method.PUT);
		// getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(), getAuthToken());
		synchronize();
	}

	public static LocalEventObject attend(BaseDTObject event) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException,
			TerritoryServiceException, AACException {
		LocalEventObject returnEvent = new LocalEventObject();
		EventObject newEvent = tService.myEvent(event.getId(), true, getAuthToken());
		EventObjectForBean newEventBean = new EventObjectForBean();
		newEventBean.setObjectForBean(newEvent);
		returnEvent.setEventFromEventObjectForBean(newEventBean);
		synchronize();
		return returnEvent;
	}

	public static LocalEventObject notAttend(BaseDTObject event) throws ConnectionException, ProtocolException,
			SecurityException, DataException, RemoteException, StorageConfigurationException,
			TerritoryServiceException, AACException {
		LocalEventObject returnEvent = new LocalEventObject();
		EventObject newEvent = tService.myEvent(event.getId(), false, getAuthToken());
		EventObjectForBean newEventBean = new EventObjectForBean();
		newEventBean.setObjectForBean(newEvent);
		returnEvent.setEventFromEventObjectForBean(newEventBean);
		synchronize();
		return returnEvent;
	}

	public static EventObjectForBean findEventByEntityId(String entityId) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
		return findDTObjectByEntityId(EventObjectForBean.class, entityId);
	}

	public static PoiObjectForBean findPOIByEntityId(String entityId) throws DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
		return findDTObjectByEntityId(PoiObjectForBean.class, entityId);
	}

	@SuppressWarnings("rawtypes")
	private static <T extends GenericObjectForBean> T findDTObjectByEntityId(Class<T> cls, String entityId)
			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
			SecurityException {
		T returnObject = null;
		String where = "entityId = '" + entityId+"'";
		Collection<T> coll = getInstance().storage.query(cls, where, null);
		if (coll != null && coll.size() == 1)
			returnObject = coll.iterator().next();
		if (returnObject == null)
			returnObject = findLocalDTOObjectByEntityId(cls, entityId);
		return returnObject;

	}

	@SuppressWarnings("rawtypes")
	private static <T extends GenericObjectForBean> T findLocalDTOObjectByEntityId(Class<T> cls, String entityId) {
		try {
			DTHelper.synchronize();
			T returnObject = null;
			String where = "entityId = '" + entityId+"'";
			Collection<T> coll = getInstance().storage.query(cls, where, null);
			if (coll != null && coll.size() == 1)
				returnObject = coll.iterator().next();
			return returnObject;
		} catch (Exception e) {
			return null;
		}

	}

	public static Boolean saveStory(StoryObject storyObject) throws RemoteException, DataException,
			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
			TerritoryServiceException, AACException {
		boolean returnvalue = true;
		if (storyObject.getId() == null) {
			tService.createStory(storyObject, getAuthToken());
			returnvalue = true;
		} else {
			storyObject = tService.updateStory(storyObject.getId(), storyObject, getAuthToken());
			returnvalue = false;
		}
		// String requestService = null;
		// Method method = null;
		// Boolean result = null;
		// if (storyObject.getId() == null) {
		// // create
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserStoryObject";
		// method = Method.POST;
		// result = true;
		// } else {
		// // update
		// requestService = Constants.SERVICE +
		// "/eu.trentorise.smartcampus.dt.model.UserStoryObject/"
		// + storyObject.getId();
		// method = Method.PUT;
		// result = false;
		// }
		// MessageRequest request = new
		// MessageRequest(GlobalConfig.getAppUrl(mContext),
		// requestService);
		// request.setMethod(method);
		// String json =
		// eu.trentorise.smartcampus.android.common.Utils.convertToJSON(storyObject);
		// request.setBody(json);
		//
		// getInstance().mProtocolCarrier.invokeSync(request,
		// DTParamsHelper.getAppToken(), getAuthToken());
		// getRemote(mContext, instance.token).create(poi);
		synchronize();
		return returnvalue;
	}

//	public static Collection<StoryObject> getStoryByCategory(int position, int size, String... inCategories)
//			throws DataException, StorageConfigurationException, ConnectionException, ProtocolException,
//			SecurityException, TerritoryServiceException, AACException {
//		ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
//
//		if (inCategories == null || inCategories.length == 0)
//			return Collections.emptyList();
//
//		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));
//
//		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
//			List<String> nonNullCategories = new ArrayList<String>();
//			String where = "";
//			for (int i = 0; i < categories.length; i++) {
//				if (where.length() > 0)
//					where += " or ";
//				if (categories[i] != null) {
//					nonNullCategories.add(categories[i]);
//					where += " type = ?";
//				} else {
//					where += " type is null";
//				}
//			}
//			if (where.length() > 0) {
//				where = "(" + where + ")";
//			}
//			Collection<StoryObjectForBean> stories = getInstance().storage.query(StoryObjectForBean.class, where,
//					nonNullCategories.toArray(new String[nonNullCategories.size()]), position, size, "title ASC");
//			for (StoryObjectForBean storyBean : stories) {
//				returnlist.add(storyBean.getObjectForBean());
//			}
//			return returnlist;
//
//		} else {
//			for (int c = 0; c < categories.length; c++) {
//				ObjectFilter filter = new ObjectFilter();
//				filter.setTypes(Arrays.asList(categories));
//				filter.setSkip(position);
//				filter.setLimit(size);
//				returnlist.addAll(tService.getStories(filter, getAuthToken()));
//			}
//			return returnlist;
//		}
//	}

//	public static Collection<StoryObject> searchStories(int position, int size, String text) throws DataException,
//			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
//			TerritoryServiceException, AACException {
//		ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
//
//		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
//			if (text == null || text.trim().length() == 0) {
//				Collection<StoryObjectForBean> stories = getInstance().storage.getObjects(StoryObjectForBean.class);
//				for (StoryObjectForBean storyBean : stories) {
//					returnlist.add(storyBean.getObjectForBean());
//				}
//				return returnlist;
//			}
//			Collection<StoryObjectForBean> stories = getInstance().storage.query(StoryObjectForBean.class,
//					"stories MATCH ? ", new String[] { text }, position, size, "title ASC");
//			for (StoryObjectForBean storyBean : stories) {
//				returnlist.add(storyBean.getObjectForBean());
//			}
//			return returnlist;
//		} else {
//			ObjectFilter filter = new ObjectFilter();
//			Map<String, Object> criteria = new HashMap<String, Object>(1);
//			criteria.put("text", text);
//			filter.setCriteria(criteria);
//			filter.setSkip(position);
//			filter.setLimit(size);
//			return tService.getStories(filter, getAuthToken());
//		}
//	}

	public static Boolean deleteStory(StoryObject storyObject) throws DataException, ConnectionException,
			ProtocolException, SecurityException, RemoteException, StorageConfigurationException,
			TerritoryServiceException, AACException {
		if (storyObject.getId() != null) {
			tService.deleteStory(storyObject.getId(), getAuthToken());
			synchronize();
			return true;
		}
		return false;
	}

//	public static StoryObjectForBean findStoryByEntityId(String storyId) throws DataException,
//			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
//		return findDTObjectByEntityId(StoryObjectForBean.class, storyId);
//	}
//
//	public static StoryObject findStoryById(String storyId) {
//		try {
//			StoryObjectForBean story = getInstance().storage.getObjectById(storyId, StoryObjectForBean.class);
//			return story.getObjectForBean();
//		} catch (Exception e) {
//			return null;
//		}
//	}

	// try {
	// StoryObject story = tService.getStory(storyId, getAuthToken());
	// return story;
	// } catch (Exception e) {
	// return null;
	// }

//	public static ArrayList<POIObject> getPOIBySteps(List<StepObject> steps) throws DataException,
//			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException {
//
//		// usare findpoibyid nella lista steps
//
//		ArrayList<POIObject> poiList = new ArrayList<POIObject>();
//		for (StepObject step : steps) {
//			POIObject poiStep = findPOIById(step.getPoiId());
//			poiList.add(poiStep);
//		}
//		return poiList;
//
//	}
//
//	public static StoryObject addToMyStories(BaseDTObject story) throws ConnectionException, ProtocolException,
//			SecurityException, DataException, RemoteException, StorageConfigurationException,
//			TerritoryServiceException, AACException {
//		// LocalEventObject returnEvent = new LocalEventObject();
//		// EventObject newEvent = tService.myEvent(event.getId(), true,
//		// getAuthToken());
//		// EventObjectForBean newEventBean = new EventObjectForBean();
//		// newEventBean.setObjectForBean(newEvent);
//		// returnEvent.setEventFromEventObjectForBean(newEventBean);
//		// return returnEvent;
//
//		StoryObject returnObject = tService.myStory(story.getId(), true, getAuthToken());
//		synchronize();
//		return returnObject;
//	}
//
//	public static StoryObject removeFromMyStories(BaseDTObject story) throws ConnectionException, ProtocolException,
//			SecurityException, DataException, RemoteException, StorageConfigurationException,
//			TerritoryServiceException, AACException {
//		StoryObject returnObject = tService.myStory(story.getId(), false, getAuthToken());
//		synchronize();
//		return returnObject;
//	}
//
//	public static Collection<StoryObject> getMyStories(int position, int size) throws DataException,
//			StorageConfigurationException, ConnectionException, ProtocolException, SecurityException,
//			TerritoryServiceException, AACException {
//		ArrayList<StoryObject> returnlist = new ArrayList<StoryObject>();
//
//		if (Utils.getObjectVersion(mContext, DTParamsHelper.getAppToken(), Constants.SYNC_DB_NAME) > 0) {
//			Collection<StoryObjectForBean> stories = getInstance().storage.query(StoryObjectForBean.class,
//					"attending IS NOT NULL", null, position, size, "title ASC");
//			for (StoryObjectForBean storyBean : stories) {
//				returnlist.add(storyBean.getObjectForBean());
//			}
//			return returnlist;
//		} else {
//			ObjectFilter filter = new ObjectFilter();
//			filter.setMyObjects(true);
//			filter.setSkip(position);
//			filter.setLimit(size);
//			return tService.getStories(filter, getAuthToken());
//		}
//	}

	public static LocationHelper getLocationHelper() {
		return mLocationHelper;
	}

	public static void setLocationHelper(LocationHelper mLocationHelper) {
		DTHelper.mLocationHelper = mLocationHelper;
	}

	public class DTLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public static DTSyncStorage getSyncStorage() throws DataException {
		return getInstance().storage;
	}

	public static boolean isOwnedObject(BaseDTObject obj) {
		if (obj.getId() == null)
			return true;
		// UserData p = null;
		// try {
		// p = accessProvider.readUserData(mContext, null);
		// } catch (DataException e) {
		//
		// }
		if (bp != null)
			return bp.getUserId().equals(obj.getCreatorId());
		else
			getUserProfile();
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static <T extends GenericObjectForBean> Collection<T> searchInGeneral(int position, int size, String what,
			WhereForSearch distance, WhenForSearch when, boolean my, Class<T> cls, SortedMap<String, Integer> sort,
			String... inCategories) throws DataException, StorageConfigurationException, ConnectionException,
			ProtocolException, SecurityException {
		/* calcola when */
		String[] argsArray = null;
		ArrayList<String> args = null;

		if (distance != null) {
			/* search online */
			return getObjectsFromServer(position, size, what, distance, when, my, cls, inCategories, sort);
		} else {
			/* search offline */

			// if (Utils.getObjectVersion(mContext,
			// DTParamsHelper.getAppToken()) > 0) {

			/* if sync create the query */
			String where = "";
			if (inCategories[0] != null) {
				args = new ArrayList<String>();
				where = addCategoriesToWhere(where, inCategories, args);
			}
			if (what != null && what.length() > 0) {
				where = addWhatToWhere(cls, where, what);
				if (args == null)
					args = new ArrayList<String>(Arrays.asList(what));
				else
					args.add(what);
			}
			if (EventObject.class.getCanonicalName().equals(cls.getCanonicalName())) {
				if (when != null)
					where = addWhenToWhere(where, when.getFrom(), when.getTo());

				/* se sono con gli eventi setto la data a oggi */
				else
					where = addWhenToWhere(where, getCurrentDateTimeForSearching(), 0);
			}
			if (my)
				where = addMyEventToWhere(where);
			if (args != null)
				argsArray = args.toArray(new String[args.size()]);
			/*
			 * se evento metti in ordine di data ma se place metti in ordine
			 * alfabetico
			 */
			if (EventObject.class.getCanonicalName().equals(cls.getCanonicalName())) {
				return getInstance().storage.query(cls, where, argsArray, position, size, "fromTime ASC");
			} else {
				return getInstance().storage.query(cls, where, argsArray, position, size, "title ASC");
			}
			// } else {
			// /* if not sync... (not used anymore) */
			// ArrayList<T> result = new ArrayList<T>();
			// for (String category : inCategories) {
			// ObjectFilter filter = new ObjectFilter();
			// if (what != null) {
			// Map<String, Object> criteria = new HashMap<String, Object>(1);
			// criteria.put("text", what);
			// filter.setCriteria(criteria);
			// }
			// if (when != null) {
			// filter.setFromTime(when.getFrom());
			// filter.setToTime(when.getTo());
			// }
			// if (category != null) {
			// Arrays.asList(categories);
			// }
			// if (my)
			// filter.setMyObjects(true);
			//
			// filter.setSkip(position);
			// filter.setLimit(size);
			// result.addAll(getRemote(mContext,
			// getAuthToken()).searchObjects(filter, cls));
			// }
			// return result;
			// }

		}

	}

	@SuppressWarnings("unchecked")
	private static <T extends BasicObject> Collection<T> getObjectsFromServer(int position, int size, String what,
			WhereForSearch distance, WhenForSearch when, boolean myevent, Class<?> cls, String[] inCategories,
			SortedMap<String, Integer> sort) {
		try {

			ObjectFilter filter = new ObjectFilter();

			/* get position */
			// long currentDate = getCurrentDateTimeForSearching();
			if (when != null)
				filter.setFromTime(when.getFrom());
			if ((when != null) && (when.getTo() != 0))
				filter.setToTime(when.getTo());

			if (distance != null) {
				GeoPoint mypos = MapManager.requestMyLocation(mContext);
				filter.setCenter(new double[] { (double) mypos.getLatitudeE6() / 1000000,
						(double) mypos.getLongitudeE6() / 1000000 });
				filter.setRadius(distance.getFilter());
			}
			if (what != null && what.length() > 0) {
				filter.setText(what);
			}
			if (inCategories[0] != null) {
				filter.setTypes(Arrays.asList(CategoryHelper.getAllCategories(new HashSet<String>(Arrays
						.asList(inCategories)))));
			}
			filter.setSkip(position);
			filter.setLimit(size);
			// filter.setClassName(cls.getCanonicalName());
			if (sort != null)
				filter.setSort(sort);
			// Collection<T> result = getRemote(mContext,
			// getAuthToken()).searchObjects(filter, cls);
			Collection<T> result = new ArrayList<T>();

			if (cls == PoiObjectForBean.class) {
				Collection<POIObject> pois = null;
				Collection<PoiObjectForBean> poisbean = new ArrayList<PoiObjectForBean>();
				pois = tService.getPOIs(filter, getAuthToken());

				for (POIObject poi : pois) {
					PoiObjectForBean poiBean = new PoiObjectForBean();
					poiBean.setObjectForBean(poi);
					poisbean.add(poiBean);
				}
				result = (Collection<T>) poisbean;
			} else if (cls == EventObjectForBean.class) {
				Collection<EventObject> events = null;
				Collection<EventObjectForBean> eventsbean = new ArrayList<EventObjectForBean>();
				events = tService.getEvents(filter, getAuthToken());

				for (EventObject poi : events) {
					EventObjectForBean eventBean = new EventObjectForBean();
					eventBean.setObjectForBean(poi);
					eventsbean.add(eventBean);
				}
				result = (Collection<T>) eventsbean;

			} 
//			else if (cls == StoryObjectForBean.class) {
//				Collection<StoryObject> stories = null;
//				Collection<StoryObjectForBean> storiesbean = new ArrayList<StoryObjectForBean>();
//				stories = tService.getStories(filter, getAuthToken());
//
//				for (StoryObject poi : stories) {
//					StoryObjectForBean storyBean = new StoryObjectForBean();
//					storyBean.setObjectForBean(poi);
//					storiesbean.add(storyBean);
//				}
//				result = (Collection<T>) storiesbean;
//			}
			if (result != null) {
				synchronize();
			}
			return result;

		} catch (Exception e) {
			return null;
		}
	}

	private static String addMyEventToWhere(String where) {
		String whereReturns = new String(" attending IS NOT NULL ");
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += whereReturns;
	}

	private static String addWhenToWhere(String where, long whenFrom, long whenTo) {
		String whereReturns = null;
		if ((whenTo != 0)) {
			whereReturns = new String("( fromTime > " + whenFrom + " AND fromTime < " + whenTo + " ) OR (  toTime < "
					+ whenTo + " AND toTime > " + whenFrom + " )");
			// whereReturns = " (  fromTime <= " + whenTo + " AND toTime >= " +
			// whenFrom + " )";+
		} else
			whereReturns = new String(" ( fromTime > " + whenFrom + "  ) OR ( toTime > " + whenFrom + " )");

		// whereReturns = " ( toTime >= " + whenFrom + " )";

		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return "(" + whereReturns + ")";

	}

	@SuppressWarnings("rawtypes")
	private static <T extends GenericObjectForBean> String addWhatToWhere(Class<T> cls, String where, String what)
			throws StorageConfigurationException, DataException {
		String whereReturns = "";

		whereReturns = " " + getInstance().config.getTableName(cls) + " MATCH ? ";
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += whereReturns;

	}

	private static String addCategoriesToWhere(String where, String[] inCategories, List<String> nonNullCategories) {
		String whereReturns = new String();
		String[] categories = CategoryHelper.getAllCategories(new HashSet<String>(Arrays.asList(inCategories)));

		for (int i = 0; i < categories.length; i++) {
			if (whereReturns.length() > 0)
				whereReturns += " or ";
			if (categories[i] != null) {
				nonNullCategories.add(categories[i]);
				whereReturns += " type = ?";
			} else {
				whereReturns += " type is null";
			}
		}
		if (where.length() > 0) {
			return where += " and (" + whereReturns + ")";
		} else
			return where += "( " + whereReturns + " ) ";

	}

	public static boolean checkInternetConnection(Context context) {

		ConnectivityManager con_manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (con_manager.getActiveNetworkInfo() != null && con_manager.getActiveNetworkInfo().isAvailable()
				&& con_manager.getActiveNetworkInfo().isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public static LocalEventObject findEventById(String eventId) {
		LocalEventObject returnEvent = new LocalEventObject();
		try {
			EventObjectForBean event = getInstance().storage.getObjectById(eventId, EventObjectForBean.class);
			returnEvent.setEventFromEventObjectForBean(event);
			return returnEvent;
		} catch (Exception e) {
			return null;
		}
	}

	public static SharedPreferences getTutorialPreferences(Context ctx) {
		SharedPreferences out = ctx.getSharedPreferences(TUT_PREFS, Context.MODE_PRIVATE);
		return out;
	}

	public static boolean isFirstLaunch(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(FIRST_LAUNCH_PREFS, true);
	}

	public static void disableFirstLaunch(Context ctx) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(FIRST_LAUNCH_PREFS, false);
		edit.commit();
	}

	public static boolean wantTour(Context ctx) {
		return getTutorialPreferences(ctx).getBoolean(TOUR_PREFS, false);
	}

	public static void setWantTour(Context ctx, boolean want) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(TOUR_PREFS, want);
		edit.commit();
	}

	public static boolean isTutorialShowed(Context ctx, Tutorial t) {
		return getTutorialPreferences(ctx).getBoolean(t.toString(), false);
	}

	public static void setTutorialAsShowed(Context ctx, Tutorial t) {
		Editor edit = getTutorialPreferences(ctx).edit();
		edit.putBoolean(t.toString(), true);
		edit.commit();
	}

	/**
	 * With this method you can get the last tutorial that was not showed
	 * 
	 * @param ctx
	 *            the activity
	 * @return the last Tutorial not showed to the user otherwise null
	 */
	public static Tutorial getLastTutorialNotShowed(Context ctx) {
		for (Tutorial t : Tutorial.values()) {
			if (!isTutorialShowed(ctx, t))
				return t;
		}
		return null;
	}

	public static String poiGetShortAddress(POIObject poi) {
		return poi.getTitle()
				+ (poi.getPoi().getStreet() == null || poi.getPoi().getStreet().length() == 0 ? "" : (", " + poi
						.getPoi().getStreet()));
	}

}
