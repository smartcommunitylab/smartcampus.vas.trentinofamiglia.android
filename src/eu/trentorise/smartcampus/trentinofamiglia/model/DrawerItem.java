package eu.trentorise.smartcampus.trentinofamiglia.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;

public class DrawerItem {
	public boolean header;
	public String text;
	public Drawable icon;

	public DrawerItem(boolean header, String text, Drawable icon) {
		super();
		this.header = header;
		this.text = text;
		this.icon = icon;
	}
}