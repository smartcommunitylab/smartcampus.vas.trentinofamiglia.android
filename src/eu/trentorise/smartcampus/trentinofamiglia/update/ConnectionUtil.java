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
package eu.trentorise.smartcampus.trentinofamiglia.update;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Utility class that allows you to receive about phone connection
 * 
 * @author Simone Casagranda
 *
 */
public class ConnectionUtil {

	private ConnectionUtil(){
		throw new AssertionError("You must use static methods!");
	}
	
	public static Intent getWifiSettingsIntent(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setAction(Settings.ACTION_WIFI_SETTINGS);
		return intent;
	}
	
	/**
	 * Retrieves the connectivity manager
	 * @param context
	 * @return
	 */
	public static ConnectivityManager getConnectivityManager(Context context){
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	
	/**
	 * Check if we are connected to a WiFi network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectedToWiFi(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI ;
	}
	
	/**
	 * Check if we are connected to a 3G network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectedTo3G(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE ;
	}
	
	/**
	 * Check if we are connected to a network
	 * @param cm
	 * @return
	 */
	public static boolean isConnected(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	/**
	 * Check if we are connecting or connected to a WiFi network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnectedToWiFi(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnectedOrConnecting();
	}
	
	/**
	 * Check if we are connecting or connected to a 3G network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnectedTo3G(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE && info.isConnectedOrConnecting();
	}

	/**
	 * Check if we are connecting or connected to a network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnected(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
	
	/**
	 * Check if we are connected in roaming
	 * @param cm
	 * @return
	 */
	public static boolean isRoaming(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isRoaming();
	}
}
