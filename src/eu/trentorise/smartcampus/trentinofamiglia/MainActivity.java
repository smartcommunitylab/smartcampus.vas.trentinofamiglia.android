package eu.trentorise.smartcampus.trentinofamiglia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DrawerItem;
import eu.trentorise.smartcampus.trentinofamiglia.custom.HackActionBarToggle;
import eu.trentorise.smartcampus.trentinofamiglia.custom.NavDrawerAdapter;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.info.InfoListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoisListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.search.SearchFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.track.TrackListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller;
import eu.trentorise.smartcampus.trentinofamiglia.update.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.trentinofamiglia.update.ConnectionUtil;

public class MainActivity extends ActionBarActivity implements OnChildClickListener {

	public static final String TAG_FRAGMENT_MAP = "fragmap";
	public static final String TAG_FRAGMENT_POI_LIST = "fragpopi";
	public static final String TAG_FRAGMENT_EVENT_LIST = "fragewent";
	public static final String TAG_FRAGMENT_TRACK_LIST = "fragtrack";
	public static final String TAG_FRAGMENT_INFO_LIST = "fraginfo";

	private FragmentManager mFragmentManager;
	private DrawerLayout mDrawerLayout;
	private ExpandableListView mListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] navMenuTitles;
	private boolean isLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			setSupportProgressBarIndeterminateVisibility(false);
		}

		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		setupProperties();
		initDataManagement(savedInstanceState);

	}

	public void navDrawerOutItemClick(View v) {
		if (v.getId() == R.id.nav_drawer_map_tv) {
			onChildClick(null, null, -1, -1, -1);
		}
		if (v.getId() == R.id.nav_drawer_info) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.trentinofamiglia_url_credits)));
			startActivity(browserIntent);
		}

	}

	// getting the notification intent update the launcher
	@Override
	public void onNewIntent(Intent arg0) {
		super.onNewIntent(arg0);
		Bundle extras = arg0.getExtras();

		if (extras != null) {
			ApkDownloaderTask mDownloaderTask = new ApkDownloaderTask(this, extras.getString("url"));

			if (ConnectionUtil.isConnected(ConnectionUtil.getConnectivityManager(this))) {
				// Checking url
				if (!TextUtils.isEmpty(extras.getString("url"))) {
					if (mDownloaderTask != null && !mDownloaderTask.isCancelled()) {
						mDownloaderTask.cancel(true);
					}
					mDownloaderTask = new ApkDownloaderTask(this, extras.getString(ApkInstaller.PARAM_URL));
					mDownloaderTask.execute();
				} else {
					Log.d(MainActivity.class.getName(),
							"Empty url for download: " + extras.getString(ApkInstaller.PARAM_NAME));
					Toast.makeText(this, R.string.error_occurs, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, R.string.enable_connection, Toast.LENGTH_SHORT).show();
				Intent intent = ConnectionUtil.getWifiSettingsIntent();
				startActivity(intent);
			}
		}

	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			initGlobalConstants();

			try {
				// if (!SCAccessProvider.getInstance(this).login(this, null)) {
				DTHelper.init(getApplicationContext());
				initData();
				// }

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
				finish();
			}

		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}
	}

	@Override
	protected void onResume() {
		if (DTHelper.getLocationHelper() != null)
			DTHelper.getLocationHelper().start();
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (DTHelper.getLocationHelper() != null)
			DTHelper.getLocationHelper().stop();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		DTHelper.destroy();
		super.onDestroy();
	}

	private void initGlobalConstants() throws NameNotFoundException, NotFoundException {
		GlobalConfig.setAppUrl(this, getResources().getString(R.string.smartcampus_app_url));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean initData() {
		try {
			// to start with the map.
			mFragmentManager.beginTransaction().replace(R.id.frame_content, new MapFragment(), TAG_FRAGMENT_MAP)
					.commit();
			new SCAsyncTask<Void, Void, BaseDTObject>(this, new LoadDataProcessor(this)).execute();
		} catch (Exception e1) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	private void setupProperties() {

		mFragmentManager = getSupportFragmentManager();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// this is a class created to avoid an Android bug
		// see the class for further infos.
		mDrawerToggle = new HackActionBarToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.app_name,
				R.string.app_name);

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mListView = (ExpandableListView) findViewById(R.id.drawer_list);
		navMenuTitles = getResources().getStringArray(R.array.fragments_label_array);
		NavDrawerAdapter nda = buildAdapter();

		View infoBtn = getLayoutInflater().inflate(R.layout.info_drawer_item, null);
		mListView.addFooterView(infoBtn);

		mListView.setAdapter(nda);
		mListView.setOnChildClickListener(this);

	}

	// hack for moving the expandablelistview's arrow from left to right
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mListView.setIndicatorBounds(
					mListView.getRight() - (int) getResources().getDimension(R.dimen.navigation_arrow),
					mListView.getWidth());
		} else {
			mListView.setIndicatorBoundsRelative(
					mListView.getRight() - (int) getResources().getDimension(R.dimen.navigation_arrow),
					mListView.getWidth());
		}
	}

	private NavDrawerAdapter buildAdapter() {

		Map<String, List<DrawerItem>> items = new HashMap<String, List<DrawerItem>>();
		ArrayList<String> headers = new ArrayList<String>();

		// getting items from the xml
		// see the method for further infos
		populateFromXml(items, headers, R.array.drawer_items_events_labels, R.array.drawer_items_events_icons,
				R.array.drawer_items_freetime_labels, R.array.drawer_items_freetime_icons,
				R.array.drawer_items_initiatives_labels, R.array.drawer_items_initiatives_icons,
				R.array.drawer_items_places_labels, R.array.drawer_items_places_icons,
				R.array.drawer_items_babies_labels, R.array.drawer_items_babies_icons,
				R.array.drawer_items_family_labels, R.array.drawer_items_family_icons);
		return new NavDrawerAdapter(this, headers, items);
	}

	/**
	 * ASSUMPTIONS: ids passed should be in this way:
	 * array_of_labels_1,array_of_icons_1,the
	 * array_of_labels_2,array_of_icons_2, and so on.. the first element of each
	 * array that contains labels must be the header
	 * 
	 * @param items
	 *            where to put elements
	 * @param ids
	 *            array of arrays made in xml
	 */
	private void populateFromXml(Map<String, List<DrawerItem>> items, List<String> headers, int... ids) {
		for (int i = 0; i < ids.length; i += 2) {
			String[] labels = getResources().getStringArray(ids[i]);
			TypedArray drawIds = getResources().obtainTypedArray((ids[i + 1]));
			headers.add(labels[0]);
			ArrayList<DrawerItem> tmp = new ArrayList<DrawerItem>();
			for (int j = 1; j < labels.length; j++) {
				int imgd = drawIds.getResourceId(j - 1, -1);
				tmp.add(new DrawerItem(labels[j], ((imgd != -1) ? getResources().getDrawable(imgd) : null)));
			}
			items.put(labels[0], tmp);
			drawIds.recycle();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

		Object[] objects = getFragmentAndTag(groupPosition, childPosition);
		// can't replace the current fragment with nothing or with one of the
		// same type
		if (objects != null) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.setCustomAnimations(R.anim.enter, R.anim.exit);
			ft.replace(R.id.frame_content, (Fragment) objects[0], objects[1].toString());
			clearStack();
			ft.addToBackStack(objects[1].toString());

			ft.commit();
		}
		mDrawerLayout.closeDrawers();
		return true;

	}

	private void clearStack() {
		FragmentManager fm = getSupportFragmentManager();
		for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
			fm.popBackStack();
		}

	}

	private Object[] getFragmentAndTag(int groupPos, int childPos) {
		Object[] out = new Object[2];
		String cat = null;
		Bundle args = new Bundle();
		if (groupPos == -1) {
			if (childPos == -1) { // map
				MapFragment mf = new MapFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				mf.setArguments(args);
				out[0] = mf;
				out[1] = TAG_FRAGMENT_POI_LIST;
			}
		} else if (groupPos == 0) {// Events
			EventsListingFragment elf = null;
			switch (childPos) {
			case 0:
				cat = CategoryHelper.CAT_EVENT_ESTATE_GIOVANI_E_FAMIGLIA;
				args = new Bundle();
				elf = new EventsListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				elf.setArguments(args);
				out[0] = elf;
				out[1] = TAG_FRAGMENT_EVENT_LIST;
				break;
			case 1:
				cat = CategoryHelper.CAT_EVENT_ALTO_GARDA;
				args = new Bundle();
				elf = new EventsListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				elf.setArguments(args);
				out[0] = elf;
				out[1] = TAG_FRAGMENT_EVENT_LIST;
				break;
			default:
				out = null;
				break;
			}
		} else if (groupPos == 1) { // free time and holidays
			switch (childPos) {
			case 0: // piste ciclopedonali
				TrackListingFragment tlf = null;
				cat = CategoryHelper.CAT_TRACK_PISTE_CICLOPEDONALI;
				args = new Bundle();
				tlf = new TrackListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				tlf.setArguments(args);
				out[0] = tlf;
				out[1] = TAG_FRAGMENT_TRACK_LIST;
				break;
			case 1: // passeggiate
				tlf = null;
				cat = CategoryHelper.CAT_TRACK_PASSEGGIATE;
				args = new Bundle();
				tlf = new TrackListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				tlf.setArguments(args);
				out[0] = tlf;
				out[1] = TAG_FRAGMENT_TRACK_LIST;
				break;
			case 2: // vacanze al mare
				PoisListingFragment plf = null;
				cat = CategoryHelper.CAT_POI_VACANZE_AL_MARE;
				args = new Bundle();
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			default:
				out = null;
				break;
			}
		} else if (groupPos == 2) { // News
			InfoListingFragment elf = null;
			switch (childPos) {
			case 0:
				cat = CategoryHelper.CAT_INFO_NOTIZIE;
				args = new Bundle();
				elf = new InfoListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				elf.setArguments(args);
				out[0] = elf;
				out[1] = TAG_FRAGMENT_INFO_LIST;
				break;
			default:
				out = null;
				break;
			}
		} else if (groupPos == 3) {// places
			PoisListingFragment plf = null;
			switch (childPos) {
			case 0:
				cat = CategoryHelper.CAT_POI_FAMILY_IN_TRENTINO;
				args = new Bundle();
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			case 1:
				cat = CategoryHelper.CAT_POI_FAMILY_AUDIT;
				args = new Bundle();
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			default:
				out = null;
				break;

			}
		} else if (groupPos == 4) {// babies
			PoisListingFragment plf = null;
			switch (childPos) {
			case 0:
				cat = CategoryHelper.CAT_POI_PUNTI_ALLATTAMENTO;
				args = new Bundle();
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			case 1:
				cat = "\"Baby little home\"";
				args = new Bundle();
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			default:
				out = null;
				break;
			}
		} else if (groupPos == 5) {// family policies
			InfoListingFragment elf = null;
			args = new Bundle();
			elf = new InfoListingFragment();
			elf.setArguments(args);
			out[0] = elf;
			out[1] = TAG_FRAGMENT_INFO_LIST;
			switch (childPos) {
			case 0:
				cat = CategoryHelper.CAT_INFO_POLITICHE_PROVINCIALI;
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				break;
			case 1:
				cat = CategoryHelper.CAT_INFO_POLITICHE_DEI_DISTRETTI;
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				break;
			case 2:
				cat = CategoryHelper.CAT_POI_TAVOLO_NUOVI_MEDIA;
				args = new Bundle();
				PoisListingFragment plf = null;
				plf = new PoisListingFragment();
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				plf.setArguments(args);
				out[0] = plf;
				out[1] = TAG_FRAGMENT_POI_LIST;
				break;
			case 3:
				cat = CategoryHelper.CAT_INFO_CONSULENTI_AUDIT;
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				break;
			case 4:
				cat = CategoryHelper.CAT_INFO_VALUTATORI_AUDIT;
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				break;
			case 5:
				cat = CategoryHelper.CAT_INFO_DISTRETTI_E_ORGANIZZAZIONI;
				args.putString(SearchFragment.ARG_CATEGORY, cat);
				break;
			default:
				out = null;
				break;
			}
		}

		else {
			out = null;
		}
		return out;
	}

	private class LoadDataProcessor extends AbstractAsyncTaskProcessor<Void, BaseDTObject> {

		private int syncRequired = 0;
		private FragmentActivity currentRootActivity = null;

		public LoadDataProcessor(Activity activity) {
			super(activity);
		}

		@Override
		public BaseDTObject performAction(Void... params) throws SecurityException, Exception {

			Exception res = null;

			try {
				syncRequired = DTHelper.syncRequired();
			} catch (Exception e) {
				res = e;
			}

			if (res != null) {
				throw res;
			}
			return null;
		}

		@Override
		public void handleResult(BaseDTObject result) {
			if (syncRequired != DTHelper.SYNC_NOT_REQUIRED) {
				if (syncRequired == DTHelper.SYNC_REQUIRED_FIRST_TIME) {
					Toast.makeText(MainActivity.this, R.string.initial_data_load, Toast.LENGTH_LONG).show();
				}
				setSupportProgressBarIndeterminateVisibility(true);
				isLoading = true;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							currentRootActivity = DTHelper.start(MainActivity.this);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (currentRootActivity != null) {
								currentRootActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										currentRootActivity.setProgressBarIndeterminateVisibility(false);
										if (MainActivity.this != null) {
											MainActivity.this.setSupportProgressBarIndeterminateVisibility(false);
										}
										isLoading = false;
									}
								});
							}
						}
					}
				}).start();
			} else {
				setSupportProgressBarIndeterminateVisibility(false);
				// DTHelper.activateAutoSync();
			}

		}

	}

}
