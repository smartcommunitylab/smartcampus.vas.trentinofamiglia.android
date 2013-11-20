package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.SelectPoiAdapter;

public class PoiSelectFragment extends DialogFragment implements
		OnItemClickListener {

	private static final String TAG_LABEL = "labels";
	private static final String TAG_ICONS = "icons";

	private Button mCancel;
	private Button mShow;

	public static PoiSelectFragment istantiate(int labelResId, int iconResId) {
		Bundle args = new Bundle();
		args.putInt(TAG_LABEL, labelResId);
		args.putInt(TAG_ICONS, iconResId);
		PoiSelectFragment psf = new PoiSelectFragment();
		psf.setArguments(args);
		return psf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle("Something");
		View v = inflater.inflate(R.layout.fragment_select_poi, container,
				false);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle b = getArguments();
		int labels = b.getInt(TAG_LABEL);
		List<String> elements = Arrays.asList(getResources().getStringArray(
				labels));
		setupView(b, elements);
	}

	private void setupView(Bundle b, List<String> elements) {
		int icons = b.getInt(TAG_ICONS);
		ListView lv = (ListView) getView().findViewById(
				R.id.select_poi_listview);
		lv.setOnItemClickListener(this);

		TextView tv = (TextView) getView().findViewById(R.id.select_poi_header);
		tv.setText(elements.get(0));

		lv.setAdapter(new SelectPoiAdapter(getActivity(), elements.subList(1,
				elements.size()), icons));

		mCancel = (Button) getView().findViewById(R.id.select_poi_cancel);
		mCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}
		});
		mShow = (Button) getView().findViewById(R.id.select_poi_confirm);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		CheckedTextView ctv = (CheckedTextView) arg1
				.findViewById(R.id.select_poi_checkTv);
		ctv.setChecked(!ctv.isChecked());
		ctv.setTag(ctv.isChecked());
	}
}
