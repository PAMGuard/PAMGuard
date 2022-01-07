package alfa.clickmonitor;
/* 
 * Adapted from the following by Doug Gillespie. November 2018
 * 
 * Convex hull algorithm - Library (Java)
 * 
 * Copyright (c) 2017 Project Nayuki
 * https://www.nayuki.io/page/convex-hull-algorithm
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (see COPYING.txt and COPYING.LESSER.txt).
 * If not, see <http://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import PamUtils.LatLong;


public final class LatLongHull {
	
	private static LatLongCamparator latLongCamparator = new LatLongCamparator();
	
	// Returns a new list of points representing the convex hull of
	// the given set of points. The convex hull excludes collinear points.
	// This algorithm runs in O(n log n) time.
	public static List<LatLong> makeHull(List<LatLong> points) {
		List<LatLong> newPoints = new ArrayList<>(points);
		Collections.sort(newPoints, latLongCamparator);
		return makeHullPresorted(newPoints);
	}
	
	
	// Returns the convex hull, assuming that each points[i] <= points[i + 1]. Runs in O(n) time.
	public static List<LatLong> makeHullPresorted(List<LatLong> points) {
		if (points.size() <= 1)
			return new ArrayList<>(points);
		
		// Andrew's monotone chain algorithm. Positive y coordinates correspond to "up"
		// as per the mathematical convention, instead of "down" as per the computer
		// graphics convention. This doesn't affect the correctness of the result.
		
		List<LatLong> upperHull = new ArrayList<>();
		for (LatLong p : points) {
			while (upperHull.size() >= 2) {
				LatLong q = upperHull.get(upperHull.size() - 1);
				LatLong r = upperHull.get(upperHull.size() - 2);
				if ((q.getLongitude() - r.getLongitude()) * (p.getLatitude() - r.getLatitude()) >= 
						(q.getLatitude() - r.getLatitude()) * (p.getLongitude() - r.getLongitude()))
					upperHull.remove(upperHull.size() - 1);
				else
					break;
			}
			upperHull.add(p);
		}
		upperHull.remove(upperHull.size() - 1);
		
		List<LatLong> lowerHull = new ArrayList<>();
		for (int i = points.size() - 1; i >= 0; i--) {
			LatLong p = points.get(i);
			while (lowerHull.size() >= 2) {
				LatLong q = lowerHull.get(lowerHull.size() - 1);
				LatLong r = lowerHull.get(lowerHull.size() - 2);
				if ((q.getLongitude() - r.getLongitude()) * (p.getLatitude() - r.getLatitude()) >= 
						(q.getLatitude() - r.getLatitude()) * (p.getLongitude() - r.getLongitude()))
					lowerHull.remove(lowerHull.size() - 1);
				else
					break;
			}
			lowerHull.add(p);
		}
		lowerHull.remove(lowerHull.size() - 1);
		
		if (!(upperHull.size() == 1 && upperHull.equals(lowerHull)))
			upperHull.addAll(lowerHull);
		return upperHull;
	}
	
}

class LatLongCamparator implements Comparator<LatLong> {

	@Override
	public int compare(LatLong ll1, LatLong ll2) {
 /*
  * 
		if (x != other.x)
			return Double.compare(x, other.x);
		else
			return Double.compare(y, other.y);
  */
		if (ll1.getLongitude() != ll2.getLongitude()) {
			return Double.compare(ll1.getLongitude(), ll2.getLongitude());
		}
		else {
			return Double.compare(ll1.getLatitude(), ll2.getLatitude());
		}
	}
	
}

final class LatLongPoint implements Comparable<LatLongPoint> {
	
	public final double x;
	public final double y;
	
	
	public LatLongPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	
	public String toString() {
		return String.format("Point(%g, %g)", x, y);
	}
	
	
	public boolean equals(Object obj) {
		if (!(obj instanceof LatLongPoint))
			return false;
		else {
			LatLongPoint other = (LatLongPoint)obj;
			return x == other.x && y == other.y;
		}
	}
	
	
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	
	public int compareTo(LatLongPoint other) {
		if (x != other.x)
			return Double.compare(x, other.x);
		else
			return Double.compare(y, other.y);
	}
	
}

