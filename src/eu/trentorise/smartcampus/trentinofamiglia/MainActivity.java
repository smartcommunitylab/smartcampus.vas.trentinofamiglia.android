package eu.trentorise.smartcampus.trentinofamiglia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.trentorise.smartcampus.trentinofamiglia.custom.HackActionBarToggle;
import eu.trentorise.smartcampus.trentinofamiglia.custom.NavDrawerAdapter;
import eu.trentorise.smartcampus.trentinofamiglia.map.MapFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/* implements the drawer listener because
 * there is a bug for the navigation drawer
 * with the map, see the OnDrawerSlideMethod
 */
public class MainActivity extends ActionBarActivity implements
		OnItemClickListener {

	private static final String TAG_FRAGMENT_MAP = "fragmap";

	private FragmentManager mFragmentManager;
	private DrawerLayout mDrawerLayout;
	private ListView mListView;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		setupProperties();

		// to start with the map.
		mFragmentManager
				.beginTransaction()
				.replace(R.id.frame_content, new MapFragment(),
						TAG_FRAGMENT_MAP).commit();

	}

	private void setupProperties() {

		mFragmentManager = getSupportFragmentManager();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new HackActionBarToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.app_name, R.string.app_name);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mListView = (ListView) findViewById(R.id.drawer_list);
		NavDrawerAdapter nda = buildAdapter();	
		mListView.setAdapter(nda);
		mListView.setOnItemClickListener(this);
	}

	private NavDrawerAdapter buildAdapter() {
		String[] events = getResources()
				.getStringArray(R.array.drawer_items_events_labels);
		String[] places = getResources()
				.getStringArray(R.array.drawer_items_places_labels);
		List<String> items = new ArrayList<String>();
		Collections.addAll(items, events);
		Collections.addAll(items, places);
		return new NavDrawerAdapter(this, items, new Integer[]{0,events.length});
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

		// can't replace the current fragment with the same type
		if (!(mFragmentManager.findFragmentByTag(TAG_FRAGMENT_MAP).getClass()
				.equals(objects[0].getClass()))) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			ft.setCustomAnimations(R.anim.enter, R.anim.exit);
			ft.replace(R.id.frame_content, (Fragment) objects[0],
					objects[1].toString());
			ft.commit();
		}
		mDrawerLayout.closeDrawers();
	}

	private Object[] getFragmentAndTag(int pos) {
		Object[] out = new Object[2];
		switch (pos) {
		default:
			out[0] = new MapFragment();
			out[1] = TAG_FRAGMENT_MAP;
			break;
		}
		return out;
	}

}
