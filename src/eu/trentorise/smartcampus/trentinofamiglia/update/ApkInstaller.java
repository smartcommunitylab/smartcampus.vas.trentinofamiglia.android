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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.trentinofamiglia.MainActivity;
import eu.trentorise.smartcampus.trentinofamiglia.R;

public class ApkInstaller {

	private static final String DATA_TYPE = "application/vnd.android.package-archive";
	private static final String FOLDER = Environment.getExternalStorageDirectory() + "/download/";
	private static final String FILE_EXT = ".apk";
	private static final String KEY_UPDATE_DEV = "update_dev";

	// variable used for forcing refresh coming back from setting activity

	private static final String KEY_UPDATE_REFRESH = "refresh";
	private boolean forced = false;
	private SharedPreferences settings = null;
	private int[] versions;
	private boolean toUpdate = true;
	private ProgressDialog progress = null;
	private AppItem launcher;
	private AppInspector mInspector;
	private String UPDATE_ADDRESS = null;
	private String UPDATE_ADDRESS_DEV = null;
	private String UPDATE_HOST = null;
	private static final String UPDATE = "_updateModel";
	private static final String LAUNCHER = "SmartLAuncher";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_URL = "url";
	public static final String PREFS_NAME = "LauncherPreferences";
	public static  String APP_URL = "";
	public static  String APP_NAME = "";
	static ApkDownloaderTask mDownloaderTask = null;
	private static Context context;

	/**
	 * We have to ask to system certificate signed application for installation.
	 * PackageManager doesn't allow to call installPackage(...) because checks
	 * app UserID and certificate.
	 * 
	 * @param context
	 * @param file
	 */
	public static void promptInstall(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, DATA_TYPE);
		context.startActivity(intent);
	}

	/**
	 * We ask to system to prompt user an un-installation form for that package
	 * 
	 * @param context
	 * @param appPackage
	 */
	public static void promptUnInstall(Context context, String appPackage) {
		Uri packageUri = Uri.parse(appPackage);
		Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		context.startActivity(intent);

	}

	/**
	 * Retrieves an APK from a passed URL and try to store in the preferred
	 * folder.
	 * 
	 * @param urlLocation
	 * @param appLabel
	 * @return
	 */
	public static Uri retrieveApk(String urlLocation, String appLabel) {
		try {
			// Preparing folder and file
			File folder = new File(FOLDER);
			folder.mkdirs();
			File apkFile = new File(folder, appLabel + FILE_EXT);
			FileOutputStream fos = new FileOutputStream(apkFile);
			// Opening connection
			URL url = new URL(urlLocation);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			// Getting InputStream
			InputStream is = c.getInputStream();
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();
			// Return uri
			return Uri.fromFile(apkFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Very simple AsyncTask that in background downloads an APK and then on the
	 * UI thread launch an intent to prompt installation to user.
	 */
	public static class ApkDownloaderTask extends AsyncTask<Void, Void, Uri> {

		private String mAppUrl;
		private Context mContext;
		private ProgressDialog mProgressDialog;

		public ApkDownloaderTask(Context context, String appUrl) {
			mContext = context;
			mAppUrl = appUrl;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(mContext, "", mContext.getString(R.string.getting_apk));
		}

		@Override
		protected Uri doInBackground(Void... params) {
			Uri uri = null;
			uri = retrieveApk(mAppUrl, mContext.getString(R.string.downloaded_app));
			return uri;
		}

		@Override
		protected void onPostExecute(Uri result) {
			mProgressDialog.dismiss();
			// Checking result
			if (result != null) {
				promptInstall(mContext, result);
			} else {
				Toast.makeText(mContext, "Error!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private int[] readUpdateVersions(String[] packageNames, int[] defaultVersions) {

		int[] res = defaultVersions;
		UpdateModel update = null;
		long nextUpdate = -1;
		if (settings != null && settings.contains(UPDATE)) {
			nextUpdate = settings.getLong(UPDATE, -1);
		}

		if (nextUpdate < System.currentTimeMillis() || forced) {
			// if press the button check now and don't the next time
			if (!forced)
				toUpdate = true;

			// try to update

			String destination = new String(UPDATE_ADDRESS);
			if (settings.getBoolean(KEY_UPDATE_DEV, false)) {
				destination = UPDATE_ADDRESS_DEV;
			}
			MessageRequest req = new MessageRequest(UPDATE_HOST, destination);

			req.setMethod(Method.GET);
			ProtocolCarrier pc = new ProtocolCarrier(context, LAUNCHER);
			try {
				MessageResponse mres = pc.invokeSync(req, LAUNCHER, null);
				if (mres != null && mres.getBody() != null) {

					// Update from variable sec
					Calendar dateCal = Calendar.getInstance();
					dateCal.setTime(new Date());
					dateCal.add(Calendar.SECOND, context.getResources().getInteger(R.integer.check_interval));
					nextUpdate = dateCal.getTime().getTime();
					update = new UpdateModel(mres.getBody());
					settings.edit().putLong(UPDATE, nextUpdate).commit();
					for (int i = 0; i < packageNames.length; i++) {
						Integer version = update.getVersion(packageNames[i]);
						res[i] = version == null ? 0 : version;
						settings.edit().putInt(packageNames[i] + "-version", res[i]).commit();
					}

					versions = res;
				}
			} catch (Exception e) {
				Log.e(MapFragment.class.getName(), "Error reading update config: " + e.getMessage());
			}
		} else {
			toUpdate = false;
			for (int i = 0; i < packageNames.length; i++) {
				res[i] = settings.getInt(packageNames[i] + "-version", 0);
			}
			versions = res;
		}

		return res;
	}

	// Task that retrieves applications info
	public class AppTask extends AsyncTask<Void, Void, List<AppItem>> {
		private DialogInterface.OnClickListener updateDialogClickListener;
		private Activity activity;

		public AppTask(Activity activity) {
			context = activity;
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			settings = context.getSharedPreferences(PREFS_NAME, 0);
			UPDATE_ADDRESS = context.getResources().getString(R.string.update_address);
			UPDATE_HOST = context.getResources().getString(R.string.update_host);
			mInspector = new AppInspector(context);

			if (settings.getBoolean(KEY_UPDATE_REFRESH, false)) {
				forced = true;
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(KEY_UPDATE_REFRESH).commit();
			}
			if (((toUpdate) && (progress == null)) || forced)
				progress = ProgressDialog.show(context, "", context.getString(R.string.checking_version), true);

		};

		@Override
		protected List<AppItem> doInBackground(Void... params) {
			// if (context == null) {
			// context = params[0];
			// activity = params[0];
			// }
			List<AppItem> items = new ArrayList<AppItem>();
			List<AppItem> notInstalledItems = new ArrayList<AppItem>();
			// Getting applications names, packages, ...
			String[] labels = context.getResources().getStringArray(R.array.app_labels);
			String[] packages = context.getResources().getStringArray(R.array.app_packages);
			String[] backgrounds = context.getResources().getStringArray(R.array.app_backgrounds);
			String url = context.getResources().getString(R.string.smartcampus_url_apk);
			int[] versions = context.getResources().getIntArray(R.array.app_version);
			String[] filenames = context.getResources().getStringArray(R.array.apk_filename);

			versions = readUpdateVersions(packages, versions);

			Drawable ic_update = context.getResources().getDrawable(R.drawable.ic_app_update);

			TypedArray icons = context.getResources().obtainTypedArray(R.array.app_icons);
			TypedArray grayIcons = context.getResources().obtainTypedArray(R.array.app_gray_icons);
			// They have to be the same length
			assert labels.length == packages.length && labels.length == backgrounds.length
					&& labels.length == icons.length() && labels.length == grayIcons.length();
			// Preparing all items
			for (int i = 0; i < labels.length; i++) {
				AppItem item = new AppItem();
				item.app = new SmartApp();

				item.app.fillApp(labels[i], packages[i],
						buildUrlDownloadApp(url, packages[i], versions[i], filenames[i]), icons.getDrawable(i),
						grayIcons.getDrawable(i), backgrounds[i], versions[i], filenames[i]);
				APP_NAME=item.app.name;
				APP_URL=item.app.url;
				try {
					mInspector.isAppInstalled(item.app.appPackage);
					item.status = eu.trentorise.smartcampus.trentinofamiglia.update.Status.OK;
					if (!mInspector.isAppUpdated(item.app.appPackage, versions[i]))
						item.status = eu.trentorise.smartcampus.trentinofamiglia.update.Status.NOT_UPDATED;
				} catch (LauncherException e) {
					e.printStackTrace();
					// Getting status
					item.status = e.getStatus();
				}
				// Matching just retrieved status
				switch (item.status) {
				case OK:
					items.add(item);
					break;
				case NOT_UPDATED:
					// Installed but not updated
					items.add(item);
					// actually is the same of OK
					break;
				default:
					// Not installed list
					notInstalledItems.add(item);
					break;
				}
			}
			// Concatenation of not installed ones
			items.addAll(notInstalledItems);
			// Returning result
			return items;
		}

		private String buildUrlDownloadApp(String url, String packages, int versions, String filenames) {
			return new String(url + packages + "/" + versions + "/" + filenames);
		}

		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			// se anche il launcher
			if (progress != null) {
				try {
					progress.cancel();
					progress = null;
				} catch (Exception e) {
					Log.w(getClass().getName(), "Problem closing progress dialog: " + e.getMessage());
				}
			}
			int i = 0;
			for (AppItem app : result) {
				if (app.app.name.compareTo(context.getString(R.string.launcher_name)) == 0)
					break;
				i++;
			}
			launcher = result.get(i);
			/* listener to open the dialog for the update */

			if (launcher.status == eu.trentorise.smartcampus.trentinofamiglia.update.Status.NOT_UPDATED)// e
			// non
			// e'
			// nella
			// blacklist;l
			{
				/* update menu button on */
				settings.edit().putBoolean("to_be_updated", true).commit();
				/* create notification if it is a new version */
				initSharedPref();

				if (newversion(launcher)) {
					shownotificationupdate();
				}
			} else {
				settings.edit().putBoolean("to_be_updated", false).commit();
			}

			result.remove(i);
			activity.invalidateOptionsMenu();

			if (forced)
				forced = false;
		}

		private void initSharedPref() {
			if (launcher != null)
				if (!settings.contains(launcher.app.name + "-last")) {
					try {
						settings.edit()
								.putInt(launcher.app.name + "-last",
										context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode)
								.commit();
					} catch (NameNotFoundException e) {
						settings.edit().putInt(launcher.app.name + "-last", 1).commit();
					}
				}
		}

		private boolean newversion(AppItem launcher) {
			/* check if the version is new respect to the last checked */
			SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			if (settings.getInt(launcher.app.name + "-last", 1) < launcher.app.version) {
				editor.putInt(launcher.app.name + "-last", launcher.app.version);
				editor.commit();
				return true;
			} else
				return false;
		}

		private void shownotificationupdate() {

			NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.familyapp_launcher,
					context.getString(R.string.update_application_notification), System.currentTimeMillis());
			Intent notificationIntent = new Intent(context, MainActivity.class);
			notificationIntent.putExtra(PARAM_NAME, launcher.app.name);
			APP_NAME = launcher.app.name;
			notificationIntent.putExtra(PARAM_URL, launcher.app.url);
			APP_URL =  launcher.app.url;
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Notification.FLAG_SHOW_LIGHTS;
			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.update_notification);
			notification.contentIntent = pendingIntent;
			notification.contentView.setTextViewText(R.id.notification_title,
					context.getString(R.string.update_application_notification));
			manager.notify(1, notification);

		}

	}

	// Item wrapper of a smartApp
	public class AppItem {
		SmartApp app;
		Status status = Status.NOT_FOUND;
	}

	public static void update_launcher(final String paramUrl, final String paramName) {
		DialogInterface.OnClickListener updateDialogClickListener;
		final ConnectivityManager mConnectivityManager = ConnectionUtil.getConnectivityManager(context);

		updateDialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// If yes is pressed download the new app
					if (ConnectionUtil.isConnected(mConnectivityManager)) {
						// Checking url
						if (!TextUtils.isEmpty(paramUrl)) {
							if (mDownloaderTask != null && !mDownloaderTask.isCancelled()) {
								mDownloaderTask.cancel(true);
							}
							mDownloaderTask = new ApkDownloaderTask(context, paramUrl);
							mDownloaderTask.execute();
						} else {
							Log.d(getClass().getName(), "Empty url for download: " + paramName);
							Toast.makeText(context, R.string.error_occurs, Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(context, R.string.enable_connection, Toast.LENGTH_SHORT).show();
						Intent intent = ConnectionUtil.getWifiSettingsIntent();
						context.startActivity(intent);
					}

					break;

				case DialogInterface.BUTTON_NEGATIVE:

					break;
				}
			}
		};
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		settings.toString();

		// update
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setMessage(context.getString(R.string.update_application_question))
				.setPositiveButton("Yes", updateDialogClickListener).setNegativeButton("No", updateDialogClickListener)
				.show();

	}
}
