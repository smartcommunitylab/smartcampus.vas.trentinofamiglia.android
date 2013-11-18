package eu.trentorise.smartcampus.trentinofamiglia;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.AACException;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;
import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.AbstractAsyncTaskProcessor;
import eu.trentorise.smartcampus.trentinofamiglia.custom.HackActionBarToggle;
import eu.trentorise.smartcampus.trentinofamiglia.custom.NavDrawerAdapter;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.event.EventsListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.fragments.poi.PoisListingFragment;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import eu.trentorise.smartcampus.trentinofamiglia.model.DrawerItem;

public class MainActivity extends ActionBarActivity implements OnItemClickListener {

	private static final String TAG_FRAGMENT_MAP = "fragmap";
	private static final String TAG_FRAGMENT_POI_LIST = "fragpopi";
	private static final String TAG_FRAGMENT_EVENT_LIST = "fragewent";

	private FragmentManager mFragmentManager;
	private DrawerLayout mDrawerLayout;
	private ListView mListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] navMenuTitles;
	private boolean isLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initDataManagement(savedInstanceState);

		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		setupProperties();

		// to start with the map.
		mFragmentManager.beginTransaction().replace(R.id.frame_content, new MapFragment(), TAG_FRAGMENT_MAP).commit();

	}

	private void initDataManagement(Bundle savedInstanceState) {
		try {
			initGlobalConstants();
			DTHelper.init(getApplicationContext());
			String token = DTHelper.getAuthToken();
			if (token != null) {
				initData(token);
			}
			else {
				try {
					if (!SCAccessProvider.getInstance(this).login(this, null)) {
						new TokenTask().execute();
					}
				
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_init, Toast.LENGTH_LONG).show();
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
	
 if (requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
				if (token == null) {
					Toast.makeText(this, R.string.app_failure_security, Toast.LENGTH_LONG).show();
					finish();
				} else {
					initData(token);
				}
			} else if (resultCode == RESULT_CANCELED && requestCode == SCAccessProvider.SC_AUTH_ACTIVITY_REQUEST_CODE) {
				DTHelper.endAppFailure(this, R.string.app_failure_security);
			}
		}
	}


	
	
	private boolean initData(String token) {
		try {
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
		mListView = (ListView) findViewById(R.id.drawer_list);
		navMenuTitles = getResources().getStringArray(R.array.fragments_label_array);
		NavDrawerAdapter nda = buildAdapter();
		mListView.setAdapter(nda);
		mListView.setOnItemClickListener(this);
	}

	private NavDrawerAdapter buildAdapter() {

		List<DrawerItem> items = new ArrayList<DrawerItem>();
		// see the method to add an item without header
		addSingleItems(items);

		// getting items from the xml
		// see the method for further infos
		populateFromXml(items, R.array.drawer_items_events_labels, R.array.drawer_items_events_icons,
				R.array.drawer_items_places_labels, R.array.drawer_items_places_icons,
				R.array.drawer_items_organizations_labels, R.array.drawer_items_organizations_icons);
		return new NavDrawerAdapter(this, items);
	}

	/**
	 * Here you should add elements without header
	 * 
	 * @return the list
	 */
	private void addSingleItems(List<DrawerItem> out) {

		// MAP item
		out.add(new DrawerItem(false, getString(R.string.nav_drawer_map), null));

		// XXX HERE YOU CAN ADD OTHER ELEMENTS

	}

	/**
	 * ASSUMPTIONS: ids passed should be in this way:
	 * array_of_labels_1,array_of_icons_1,the
	 * array_of_labels_2,array_of_icons_2, and so on..
	 * the first element of each array that contains labels
	 * must be the header
	 * 
	 * @param items
	 *            where to put elements
	 * @param ids
	 *            array of arrays made in xml
	 */
	private void populateFromXml(List<DrawerItem> items, int... ids) {
		for (int i = 0; i < ids.length; i += 2) {
			String[] labels = getResources().getStringArray(ids[i]);
			TypedArray drawIds = getResources().obtainTypedArray((ids[i + 1]));
			items.add(new DrawerItem(true, labels[0], null));
			for (int j = 1; j < labels.length; j++) {
				int imgd = drawIds.getResourceId(j - 1, -1);
				items.add(new DrawerItem(false, labels[j], ((imgd != -1) ? getResources().getDrawable(imgd) : null)));
			}
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
	public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {

		Object[] objects = getFragmentAndTag(pos);
		// can't replace the current fragment with one of the same type
		if (!(mFragmentManager.findFragmentByTag(TAG_FRAGMENT_MAP).getClass()
				.equals(objects[0].getClass()))) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.setCustomAnimations(R.anim.enter, R.anim.exit);
			ft.replace(R.id.frame_content, (Fragment) objects[0], objects[1].toString());
			ft.commit();
		}
		mDrawerLayout.closeDrawers();
	}

	private Object[] getFragmentAndTag(int pos) {
		Object[] out = new Object[2];
		switch (pos) {
		case 0:
			out[0] = new MapFragment();
			out[1] = TAG_FRAGMENT_MAP;
			break;
		case 1:
			out[0] = new EventsListingFragment();
			out[1] = TAG_FRAGMENT_EVENT_LIST;
			break;
		case 2:
			out[0] = new EventsListingFragment();
			out[1] = TAG_FRAGMENT_EVENT_LIST;
			break;
		case 3:
			out[0] = new PoisListingFragment();
			out[1] = TAG_FRAGMENT_POI_LIST;
			break;
		case 4:
			out[0] = new PoisListingFragment();
			out[1] = TAG_FRAGMENT_POI_LIST;
			break;
		case 5:
			out[0] = new PoisListingFragment();
			out[1] = TAG_FRAGMENT_POI_LIST;
			break;
		default:
			out[0] = new MapFragment();
			out[1] = TAG_FRAGMENT_MAP;
			break;
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
				DTHelper.activateAutoSync();
			}


		}

	}
	private class TokenTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			SCAccessProvider provider = SCAccessProvider.getInstance(MainActivity.this);
			try {
				return provider.readToken(MainActivity.this);
			} catch (AACException e) {
				Log.e(MainActivity.class.getName(), ""+e.getMessage());
				switch (e.getStatus()) {
				case HttpStatus.SC_UNAUTHORIZED:
					try {
						provider.logout(MainActivity.this);
					} catch (AACException e1) {
						e1.printStackTrace();
					}
				default:
					break;
				}
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				SCAccessProvider provider = SCAccessProvider.getInstance(MainActivity.this);
				try {
					provider.login(MainActivity.this, null);
				} catch (AACException e) {
					Log.e(MainActivity.class.getName(), ""+e.getMessage());
				}
			}
		}
		
	}

}
