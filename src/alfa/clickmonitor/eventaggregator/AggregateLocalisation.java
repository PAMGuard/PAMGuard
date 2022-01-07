package alfa.clickmonitor.eventaggregator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.geometry.euclidean.twod.hull.ConvexHull2D;

import com.sun.javafx.geom.Area;
import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.Shape;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;
import alfa.clickmonitor.LatLongHull;
import javafx.geometry.Rectangle2D;
import pamMaths.PamVector;

public class AggregateLocalisation extends AbstractLocalisation {
	
	private AggLatLong[] sideLatLongs = new AggLatLong[2];

	public AggregateLocalisation(PamDataUnit pamDataUnit) {
		super(pamDataUnit, LocContents.HAS_LATLONG, pamDataUnit.getChannelBitmap());
		addLocalisation(pamDataUnit.getLocalisation());
	}

	public void addLocalisation(AbstractLocalisation localisation) {
		if (localisation == null) {
			return;
		}
		int nLL = localisation.getAmbiguityCount();
		for (int i = 0; i < nLL; i++) {
			LatLong latLong = localisation.getLatLong(i);
			if (latLong != null) {
				if (sideLatLongs[i] == null) {
					sideLatLongs[i] = new AggLatLong(latLong);
				}
				else {
					sideLatLongs[i].add(latLong);
				}
			}
		}
		
	}

	public class AggLatLong {
		
		private ArrayList<LatLong> latLongList = new ArrayList<>();
		
		public AggLatLong(LatLong latLong) {
			latLongList.add(latLong);
		}
		
		public void add(LatLong latLong) {
			latLongList.add(latLong);
		}
		
		public Rectangle2D getOuterRectangle() {
			if (latLongList.size() == 0) {
				return null;
			}
			double minX, maxX, minY, maxY;
			minX = maxX = latLongList.get(0).getLongitude();
			minY = maxY = latLongList.get(0).getLatitude();
			for (int i = 0; i < latLongList.size(); i++) {
				LatLong ll = latLongList.get(i);
				minX = Math.min(minX, ll.getLongitude());
				maxX = Math.max(maxX, ll.getLongitude());
				minY = Math.min(minY, ll.getLatitude());
				maxY = Math.max(maxY, ll.getLatitude());
			}
			return new Rectangle2D(minX, minY, maxX-minX, maxY-minY);
		}
		
//		public ConvexHull2D getOuterHull() {
//			if (latLongList.size() == 0) {
//				return null;
//			}
//			Vector2D[] v2ds = new Vector2D[latLongList.size()];
//			for (int i = 0; i < latLongList.size(); i++) {
//				LatLong ll = latLongList.get(i);
//				v2ds[i] = new Vector2D(ll.getLongitude(), ll.getLatitude());
//			}
//			try {
//			ConvexHull2D ch2d = new ConvexHull2D(v2ds, 0);
//			return ch2d;
//			}
//			catch (Exception e) {
//				return null;
//			}
//		}
		public List<LatLong> getOuterHull() {
			return LatLongHull.makeHull(latLongList);
		}
	}

	/**
	 * @return the sideLatLongs
	 */
	public AggLatLong[] getSideLatLongs() {
		return sideLatLongs;
	}

}
