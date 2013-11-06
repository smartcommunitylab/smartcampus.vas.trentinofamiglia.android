package eu.trentorise.smartcampus.trentinofamiglia.custom;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

/*
 * there is a bug for the navigation drawer
 * with the map, see the OnDrawerSlideMethod
 */
public class HackActionBarToggle extends ActionBarDrawerToggle {
	
	private ActionBarActivity activity;
	private DrawerLayout mDrawerLayout;

	public HackActionBarToggle(Activity activity, DrawerLayout drawerLayout,
			int drawerImageRes, int openDrawerContentDescRes,
			int closeDrawerContentDescRes) {

		super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,
				closeDrawerContentDescRes);
		this.activity = (ActionBarActivity) activity;
		this.mDrawerLayout=drawerLayout;
	}

	public void onDrawerClosed(View view) {
		activity.supportInvalidateOptionsMenu();
	}

	public void onDrawerOpened(View drawerView) {
		activity.supportInvalidateOptionsMenu();
	}

	/*
	 * this contains the real workaround 
	 * the navigation drawer goes behind the map without 
	 * this hack.
	 */
	public void onDrawerSlide(View drawerView, float slideOffset) {
		if (mDrawerLayout != null)
			mDrawerLayout.bringChildToFront(drawerView);
		activity.supportInvalidateOptionsMenu();
		super.onDrawerSlide(drawerView, slideOffset);
	}

	@Override
	public void onDrawerStateChanged(int arg0) {
		activity.supportInvalidateOptionsMenu();
	}
}
