package videoRangePanel.vrmethods;

import java.util.Arrays;
import java.util.Vector;

import Map.MapContour;
import Map.MapFileManager;
import PamUtils.LatLong;
import PamUtils.PamUtils;

/**
 * Separate class to manage shore functions for the video range, all this could
 * go in the controller, but might as well keep it separate.
 * 
 * @author Douglas Gillespie
 *
 */
public class ShoreManager {

	private MapFileManager mapFileManager;

	public ShoreManager() {
		super();
	}

	public ShoreManager(MapFileManager mapFileManager) {
		super();
		this.mapFileManager = mapFileManager;
	}

	public MapFileManager getMapFileManager() {
		return mapFileManager;
	}

	public void setMapFileManager(MapFileManager mapFileManager) {
		this.mapFileManager = mapFileManager;
	}
	
	public Vector<LatLong> getShoreInterceptList(LatLong origin, double trueBearing) {
		if (mapFileManager == null) {
			return null;
		}
		Vector<LatLong> crossList = new Vector<LatLong>();
		MapContour mapContour;
		Vector<LatLong> latLongs;
		LatLong lastLatLong = null;
		LatLong interceptLatLong;
		for (int i = 0; i < mapFileManager.getContourCount(); i++) {
			mapContour = mapFileManager.getMapContour(i);
			latLongs = mapContour.getLatLongs();
			for (int l = 0; l < latLongs.size()-1; l++) {
				interceptLatLong = getIntercept(origin, trueBearing, latLongs.get(l), latLongs.get(l+1));
				if (interceptLatLong != null) {
					if (lastLatLong != null && interceptLatLong.equals(lastLatLong)) {
						continue;
					}
					crossList.add(interceptLatLong);
					lastLatLong = interceptLatLong;
				}
			}
		}
		if (crossList.size() == 0) {
			return null;
		}
		return crossList;
	}
	
	/**
	 * See if a line drawn at a given bearing from some origin intercepts a line
	 * between two other points. If they do, then return the LatLong of the intercept
	 * point, otherwise return null. 
	 * @param origin origin of bearing line
	 * @param bearing true bearing (degrees clockwise from North) 
	 * @param ll1 lat long of start of line segment
	 * @param ll2 lat long of end of line segment
	 * @return LatLong of intercept or null
	 */
	public LatLong getIntercept(LatLong origin, double bearing, LatLong ll1, LatLong ll2) {
		double ang1 = bearing - origin.bearingTo(ll1);
		double ang2 = bearing - origin.bearingTo(ll2);
		ang1 = PamUtils.constrainedAngle(ang1, 180);
		ang2 = PamUtils.constrainedAngle(ang2, 180);
		/*
		 * Don't know which way around the two angles are, so the test, to see if there
		 * is an intercept is that one of the angles is bigger than bearing and the
		 * other is < bearing. There is still a risk of backbearings, so once
		 * a location has been found, check that the bearing to the location
		 * is in the same direction as the original bering and is not behind it. 
		 */
		if (ang1 * ang2 > 0) {
			return null; // both are the same sign, so no crossing
		}
		/*
		 * Calculate a crossing point by interpolation
		 */
		double f1 = Math.abs(ang1);
		double f2 = Math.abs(ang2);
		double newLat = (f1 * ll2.getLatitude() + f2 * ll1.getLatitude()) / (f1 + f2);
		double newLong = (f1 * ll2.getLongitude() + f2 * ll1.getLongitude()) / (f1 + f2);
		
		LatLong crossPoint = new LatLong(newLat, newLong);
		
		double checkBearing = origin.bearingTo(crossPoint);
		double angDiff = PamUtils.constrainedAngle(checkBearing - bearing, 180);
		if (Math.abs(angDiff) > 90) {
			return null;
		}
		return crossPoint;
	}
	
	public double[] getSortedShoreRanges(LatLong origin, double trueBearing) {
		Vector<LatLong> intercepts = getShoreInterceptList(origin, trueBearing);
		if (intercepts == null) {
			return null;
		}
		double[] ranges = new double[intercepts.size()];
		for (int i = 0; i < intercepts.size(); i++) {
			ranges[i] = origin.distanceToMetres(intercepts.get(i));
		}
		
		Arrays.sort(ranges);
		
		return ranges;
	}
	
	
	
}
