package eu.trentorise.smartcampus.trentinofamiglia;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
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
import eu.trentorise.smartcampus.trentinofamiglia.custom.HackActionBarToggle;
import eu.trentorise.smartcampus.trentinofamiglia.custom.NavDrawerAdapter;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		setupProperties();

		// to start with the map.
		mFragmentManager.beginTransaction().replace(R.id.frame_content, new MapFragment(), TAG_FRAGMENT_MAP).commit();

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

}
