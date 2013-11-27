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
package eu.trentorise.smartcampus.trentinofamiglia.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import eu.trentorise.smartcampus.territoryservice.model.BaseDTObject;
import eu.trentorise.smartcampus.territoryservice.model.POIObject;
import eu.trentorise.smartcampus.trentinofamiglia.R;
import eu.trentorise.smartcampus.trentinofamiglia.custom.CategoryHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.DTParamsHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.DTHelper;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.LocalEventObject;
import eu.trentorise.smartcampus.trentinofamiglia.custom.data.model.TrackObject;

public class MapManager {

	private static MapView mapView;

	public static int ZOOM_DEFAULT = 15;
	public static LatLng DEFAULT_POINT = new LatLng(46.0696727540531, 11.1212700605392); // Trento

	public static void initWithParam() {
		int zoom = DTParamsHelper.getZoomLevelMap();
		if (zoom != 0) {
			ZOOM_DEFAULT = zoom;
		}

		List<Double> centerMap = DTParamsHelper.getCenterMap();
		if (centerMap != null) {
			Double latitute = centerMap.get(0);
			Double longitude = centerMap.get(1);
			DEFAULT_POINT = new LatLng(latitute, longitude);
		}
	}

	public static MapView getMapView() {
		return mapView;
	}

	public static void setMapView(MapView mapView) {
		MapManager.mapView = mapView;
		MapManager.mapView.setClickable(true);
		MapManager.mapView.setBuiltInZoomControls(true);
	}

	public static GeoPoint requestMyLocation(Context ctx) {
		return DTHelper.getLocationHelper().getLocation();
	}

	public static void fitMapWithOverlays(Collection<? extends BaseDTObject> objects, GoogleMap map) {
		double[] llrr = null;
		if (objects != null && !objects.isEmpty()) {
			for (BaseDTObject o : objects) {
				if (o instanceof TrackObject && objects.size()==1) {
					double[] point = new double[2];
					for (LatLng ll : ((TrackObject) o).decodedLine()) {
						point[0] = ll.latitude;
						point[1] = ll.longitude;
						llrr = fit(llrr, point);
					}
				} else {
					llrr = fit(llrr, o.getLocation());
				}
			}
		}
		if (llrr != null) {
			fit(map, new double[]{llrr[0], llrr[1]}, new double[]{llrr[2], llrr[3]}, objects != null && objects.size() > 1);
		} else {
			fit(map, null, null, objects != null && objects.size() > 1);
		}
	}

	/**
	 * @param llrr
	 * @param location
	 * @return
	 */
	private static double[] fit(double[] llrr, double[] location) {
		if (llrr == null) {
			llrr = new double[4];
			llrr[0] = location[0];
			llrr[1] = location[1];
			llrr[2] = location[0];
			llrr[3] = location[1];
		} else {
			llrr[0] = Math.min(llrr[0], location[0]);
			llrr[1] = Math.max(llrr[1], location[1]);

			llrr[2] = Math.max(llrr[2], location[0]);
			llrr[3] = Math.min(llrr[3], location[1]);
		}
		return llrr;
	}

	private static void fit(GoogleMap map, double[] ll, double[] rr, boolean zoomIn) {
		if (ll != null && rr != null) {
			LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(rr[0], rr[1]))
					.include(new LatLng(ll[0], ll[1])).build();
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 64));
		}
	}

//	public static MarkerOptions createStoryStepMarker(Context ctx, BaseDTObject obj, int pos, boolean selected) {
//		LatLng latLng = getLatLngFromBasicObject(obj);
//
//		int markerIcon = selected ? R.drawable.selected_step : R.drawable.step;
//
//		BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(writeOnStoryMarker(ctx, markerIcon,
//				Integer.toString(pos)));
//		MarkerOptions marker = new MarkerOptions().anchor(0.5f, 0.5f).position(latLng).icon(bd).title("" + pos);
//		return marker;
//	}
//
//	public static PolylineOptions createStoryStepLine(Context ctx, BaseDTObject from, BaseDTObject to) {
//		LatLng latLngFrom = getLatLngFromBasicObject(from);
//		LatLng latLngTo = getLatLngFromBasicObject(to);
//		PolylineOptions line = new PolylineOptions().add(latLngFrom, latLngTo)
//				.color(Color.parseColor(ctx.getString(R.color.dtappcolor))).width(6);
//		return line;
//	}

	/*
	 * CLUSTERING
	 */
	public static class ClusteringHelper {
		private static final String TAG = "MapManager.ClusteringHelper";

		private static final int DENSITY_X = 5;
		private static final int DENSITY_Y = 5;

		private static List<List<List<BaseDTObject>>> grid = new ArrayList<List<List<BaseDTObject>>>();
		private static SparseArray<int[]> item2group = new SparseArray<int[]>();

		public synchronized static <T extends BaseDTObject> List<MarkerOptions> cluster(Context mContext,
				GoogleMap map, Collection<T> objects) {
			item2group.clear();
			// 2D array with some configurable, fixed density
			grid.clear();

			for (int i = 0; i <= DENSITY_X; i++) {
				ArrayList<List<BaseDTObject>> column = new ArrayList<List<BaseDTObject>>(DENSITY_Y + 1);
				for (int j = 0; j <= DENSITY_Y; j++) {
					column.add(new ArrayList<BaseDTObject>());
				}
				grid.add(column);
			}

			LatLng lu = map.getProjection().getVisibleRegion().farLeft;
			LatLng rd = map.getProjection().getVisibleRegion().nearRight;
			int step = (int) (Math.abs((lu.longitude * 1E6) - (rd.longitude * 1E6)) / DENSITY_X);

			// compute leftmost bound of the affected grid:
			// this is the bound of the leftmost grid cell that intersects
			// with the visible part
			int startX = (int) ((lu.longitude * 1E6) - ((lu.longitude * 1E6) % step));
			if (lu.longitude < 0) {
				startX -= step;
			}
			// compute bottom bound of the affected grid
			int startY = (int) ((rd.latitude * 1E6) - ((rd.latitude * 1E6) % step));
			if (lu.latitude < 0) {
				startY -= step;
			}
			int endX = startX + (DENSITY_X + 1) * step;
			int endY = startY + (DENSITY_Y + 1) * step;

			int idx = 0;
			try {
				for (BaseDTObject basicObject : objects) {
					LatLng objLatLng = getLatLngFromBasicObject(basicObject);

					if (objLatLng != null && (objLatLng.longitude * 1E6) >= startX
							&& (objLatLng.longitude * 1E6) <= endX && (objLatLng.latitude * 1E6) >= startY
							&& (objLatLng.latitude * 1E6) <= endY) {
						int binX = (int) (Math.abs((objLatLng.longitude * 1E6) - startX) / step);
						int binY = (int) (Math.abs((objLatLng.latitude * 1E6) - startY) / step);

						item2group.put(idx, new int[] { binX, binY });
						// just push the reference
						grid.get(binX).get(binY).add(basicObject);
					}
					idx++;
				}
			} catch (ConcurrentModificationException ex) {
				Log.e(TAG, ex.toString());
			}

			// if (mapView.getZoomLevel() == mapView.getMaxZoomLevel()) {
			if (map.getCameraPosition().zoom == map.getMaxZoomLevel()) {
				for (int i = 0; i < grid.size(); i++) {
					for (int j = 0; j < grid.get(0).size(); j++) {
						List<BaseDTObject> curr = grid.get(i).get(j);
						if (curr.size() == 0)
							continue;

						if (i > 0) {
							if (checkDistanceAndMerge(i - 1, j, curr))
								continue;
						}
						if (j > 0) {
							if (checkDistanceAndMerge(i, j - 1, curr))
								continue;
						}
						if (i > 0 && j > 0) {
							if (checkDistanceAndMerge(i - 1, j - 1, curr))
								continue;
						}
					}
				}
			}

			// generate markers
			List<MarkerOptions> markers = new ArrayList<MarkerOptions>();

			for (int i = 0; i < grid.size(); i++) {
				for (int j = 0; j < grid.get(i).size(); j++) {
					List<BaseDTObject> markerList = grid.get(i).get(j);
					if (markerList.size() > 1) {
						markers.add(createGroupMarker(mContext, map, markerList, i, j));
					} else if (markerList.size() == 1) {
						// draw single marker
						markers.add(createSingleMarker(markerList.get(0), i, j));
					}
				}
			}

			return markers;
		}

		/**
		 * Render markers on the map
		 * @param map
		 * @param markers
		 */
		public static void render(GoogleMap map, List<MarkerOptions> markers) {
			for (MarkerOptions mo : markers) {
				map.addMarker(mo);
			}
		}

		/**
		 * Render clustered object markers and eventual lines of the {@link TrackObject}
		 * @param map
		 * @param markers
		 * @param objects
		 */
		public static void render(Context ctx, GoogleMap map, List<MarkerOptions> markers, Collection<? extends BaseDTObject> objects) {
			for (MarkerOptions mo : markers) {
				map.addMarker(mo);
			}
			List<List<LatLng>> paths = new ArrayList<List<LatLng>>();
			for (BaseDTObject o : objects) {
				if (o instanceof TrackObject) {
					paths.add(((TrackObject)o).decodedLine());
				}
			}
			if (paths.size() == 1) {
				draw(map, paths, ctx);
			}
		}

		private static MarkerOptions createTrackMarker(TrackObject item) {
			LatLng latLng = item.startingPoint();
			int markerIcon = CategoryHelper.getMapIconByType(item.getType());

			MarkerOptions marker = new MarkerOptions().position(latLng)
					.icon(BitmapDescriptorFactory.fromResource(markerIcon)).title(item.getId());
			return marker;
		}

		private static MarkerOptions createSingleMarker(BaseDTObject item, int x, int y) {
			LatLng latLng = getLatLngFromBasicObject(item);

			int markerIcon = CategoryHelper.getMapIconByType(item.getType());
			if (CategoryHelper.FAMILY_CATEGORY_POI.equals(item.getType())
					|| (CategoryHelper.FAMILY_CATEGORY_EVENT.equals(item.getType())))
				markerIcon = objectCertified(item);

			MarkerOptions marker = new MarkerOptions().position(latLng)
					.icon(BitmapDescriptorFactory.fromResource(markerIcon)).title(x + ":" + y);
			return marker;
		}

		private static MarkerOptions createGroupMarker(Context mContext, GoogleMap map, List<BaseDTObject> markerList,
				int x, int y) {
			BaseDTObject item = markerList.get(0);
			LatLng latLng = getLatLngFromBasicObject(item);

			int markerIcon = R.drawable.ic_marker_p_generic;

			BitmapDescriptor bd = BitmapDescriptorFactory.fromBitmap(writeOnMarker(mContext, markerIcon,
					Integer.toString(markerList.size())));
			MarkerOptions marker = new MarkerOptions().position(latLng).icon(bd).title(x + ":" + y);
			return marker;
		}

		public static List<BaseDTObject> getFromGridId(String id) {
			try {
				String[] parsed = id.split(":");
				int x = Integer.parseInt(parsed[0]);
				int y = Integer.parseInt(parsed[1]);

				return grid.get(x).get(y);
			} catch (Exception e) {
				return null;
			}
		}

		private static boolean checkDistanceAndMerge(int i, int j, List<BaseDTObject> curr) {
			List<BaseDTObject> src = grid.get(i).get(j);
			if (src.size() == 0) {
				return false;
			}

			LatLng srcLatLng = getLatLngFromBasicObject(src.get(0));
			LatLng currLatLng = getLatLngFromBasicObject(curr.get(0));

			if (srcLatLng != null && currLatLng != null) {
				float[] dist = new float[3];

				Location.distanceBetween(srcLatLng.latitude, srcLatLng.longitude, currLatLng.latitude,
						currLatLng.longitude, dist);

				if (dist[0] < 20) {
					src.addAll(curr);
					curr.clear();
					return true;
				}
			}
			return false;
		}

	}

	public static void switchToMapView(ArrayList<BaseDTObject> list, Fragment src) {
		FragmentTransaction fragmentTransaction = src.getActivity().getSupportFragmentManager().beginTransaction();
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putSerializable(MapFragment.ARG_OBJECTS, list);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// fragmentTransaction.detach(src);
		fragmentTransaction.replace(R.id.frame_content, fragment, src.getTag());
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();

	}

	public static void switchToMapView(String category, String argType, Fragment src) {
		FragmentTransaction fragmentTransaction = src.getActivity().getSupportFragmentManager().beginTransaction();
		MapFragment fragment = new MapFragment();
		Bundle args = new Bundle();
		args.putString(argType, category);
		fragment.setArguments(args);
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// fragmentTransaction.detach(src);
		fragmentTransaction.replace(R.id.frame_content, fragment, src.getTag());
		fragmentTransaction.addToBackStack(fragment.getTag());
		fragmentTransaction.commit();

	}

	private static int objectCertified(BaseDTObject o) {
		if (o.getCustomData() != null) {
			if ((o instanceof LocalEventObject) && ((Boolean) o.getCustomData().get("certified"))) {
				/* se ceretificato e evento */
				return R.drawable.ic_marker_e_family_certified;
			}

			/* se certificato e poi */
			String status = (String) o.getCustomData().get("status");
			if ((o instanceof POIObject)
					&& (("Certificato finale").equals(status) || ("Certificato base").equals(status))) {
				return R.drawable.ic_marker_p_family_certified;
			}
		}

		return CategoryHelper.getMapIconByType(o.getType());
	}

	private static Bitmap writeOnMarker(Context mContext, int drawableId, String text) {
		float scale = mContext.getResources().getDisplayMetrics().density;

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888,
				true);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(scale * 14);
		paint.setAntiAlias(true);
		paint.setARGB(255, 255, 255, 255);

		Canvas canvas = new Canvas(bitmap);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		float x = bitmap.getWidth() / 2;
		float y = bitmap.getHeight() / 2;
		canvas.drawText(text, x, y, paint);

		return bitmap;
	}

	private static Bitmap writeOnStoryMarker(Context mContext, int drawableId, String text) {
		float scale = mContext.getResources().getDisplayMetrics().density;

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888,
				true);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(scale * 14);
		paint.setAntiAlias(true);
		paint.setARGB(255, 255, 255, 255);

		Canvas canvas = new Canvas(bitmap);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		float x = bitmap.getWidth() / 2;
		float y = bitmap.getHeight() / 2 - ((paint.descent() + paint.ascent()) / 2);

		canvas.drawText(text, x, y, paint);

		return bitmap;
	}

	private static LatLng getLatLngFromBasicObject(BaseDTObject object) {
		LatLng latLng = null;
		if (object instanceof TrackObject) {
			latLng = ((TrackObject) object).startingPoint();
		} else { 
			latLng = new LatLng(object.getLocation()[0], object.getLocation()[1]);
		}
		return latLng;
	}

	private static boolean draw(GoogleMap map, List<List<LatLng>> legsPoints, Context ctx) {
		for (int i = 0; i < legsPoints.size(); i++) {
			// default
			int color = ctx.getResources().getColor(R.color.path);
			List<LatLng> legPoints = legsPoints.get(i);
			drawPath(map, legPoints, color);
		}
		return true;
	}

	private static void drawPath(GoogleMap map, List<LatLng> points, int color) {
		// int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(6);

		PolylineOptions po = new PolylineOptions().addAll(points).width(6).color(color);
		Polyline pl = map.addPolyline(po);
		pl.setVisible(true);
	}

}
