package eu.trentorise.smartcampus.trentinofamiglia.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;

public class DrawerItem {
	public String text;
	public Drawable icon;

	public DrawerItem(String text, Drawable icon) {
		super();
		this.text = text;
		this.icon = icon;
	}
}
