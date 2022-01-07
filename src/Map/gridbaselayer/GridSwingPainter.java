package Map.gridbaselayer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import GPS.GpsData;
import Map.MapRectProjector;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;

public class GridSwingPainter {

	public GridSwingPainter() {
	}
	
	public boolean paintMapImage(Graphics g, MapRectProjector mapProj, MapRasterImage mapImage) {

		Graphics2D g2d = (Graphics2D) g;
		if (mapImage == null) {
			return false;
		}
		BufferedImage image = mapImage.getImage();
		if (image == null) {
			return false;
		}
		double lat[] = mapImage.getLatRange();
		double lon[] = mapImage.getLonRange();
		double gLatMin = lat[0];
		double gLatMax = lat[1];
		double gLonMin = lon[0];
		double gLonMax = lon[1];
		
		int nLat = image.getHeight();
		int nLon = image.getWidth();
//		Coordinate3d botRight = mapProj.getCoord3dNoRotate(lat[0], lon[lon.length-1], 0);
//		Coordinate3d topLeft = mapProj.getCoord3dNoRotate(lat[lat.length-1], lon[0], 0);
		
		LatLong mapCent = mapProj.getMapCentreDegrees();
		double mapScale = mapProj.getPixelsPerMetre() * 60 * GpsData.METERSPERMILE;
		double lonScale = 1./Math.cos(Math.toRadians(mapCent.getLatitude()));
		double mLatMax = mapCent.getLatitude() + mapProj.getPanelHeight() / 2 / mapScale;
		double mLatMin = mapCent.getLatitude() - mapProj.getPanelHeight() / 2 / mapScale;
		double mLonMax = mapCent.getLongitude() + mapProj.getPanelWidth() / 2 / mapScale * lonScale;
		double mLonMin = mapCent.getLongitude() - mapProj.getPanelWidth() / 2 / mapScale * lonScale;
//		Debug.out.printf("wid %3.1f hei %3.1f, Lon range %s to %s\n", mapProj.getPanelWidth(), mapProj.getPanelHeight(),
//				LatLong.formatLongitude(mLonMin), LatLong.formatLongitude(mLonMax));
		
//		plot bounds are now the min and max of these two sets ...
		double pLatMax = Math.min(gLatMax, mLatMax);
		double pLatMin = Math.max(gLatMin, mLatMin);
		double pLonMax = Math.min(gLonMax, mLonMax);
		double pLonMin = Math.max(gLonMin, mLonMin);
	
		
		/*
		 * Calculate pixel values in the base image for the selected area
		 */
		int pxMin = (int) ((pLonMin-gLonMin)/(gLonMax-gLonMin)*nLon);
		int pxMax = (int) ((pLonMax-gLonMin)/(gLonMax-gLonMin)*nLon);
		int pyMax = nLat - (int) ((pLatMin-gLatMin)/(gLatMax-gLatMin)*nLat);
		int pyMin = nLat - (int) ((pLatMax-gLatMin)/(gLatMax-gLatMin)*nLat);
		
		/*
		 *  now go back and recalculate the latlongs of those pixel values since
		 *  we need the lat longs of those bounds to be more precise. 
		 */
		double pixsOffs = -.5;
		pLatMin = lat[0] + (lat[1]-lat[0]) * (double) (nLat-pyMax-pixsOffs) / (nLat-1);
		pLatMax = lat[0] + (lat[1]-lat[0]) * (double) (nLat-pyMin-pixsOffs) / (nLat-1);
		pLonMin = lon[0] + (lon[1]-lon[0]) * (double) (pxMin+pixsOffs) / (nLon-1);
		pLonMax = lon[0] + (lon[1]-lon[0]) * (double) (pxMax+pixsOffs) / (nLon-1);
//		pxMin = Math.max(0, Math.min(pxMin, lon.length-1));
//		pxMax = Math.max(0, Math.min(pxMax, lon.length-1));
//		pyMin = Math.max(0, Math.min(pyMin, lat.length-1));
//		pyMax = Math.max(0, Math.min(pyMax, lat.length-1));
		
		AffineTransform mapTransform = mapProj.getAffineTransform();
		AffineTransform currentTransform = g2d.getTransform();
		if (mapTransform != null) {
			g2d.setTransform(mapTransform);
		}
		
		Coordinate3d botRight = mapProj.getCoord3dNoRotate(pLatMin, pLonMax, 0);
		Coordinate3d topLeft = mapProj.getCoord3dNoRotate(pLatMax, pLonMin, 0);
		
	
		Rectangle r = g2d.getClipBounds();
		g.drawImage(mapImage.getImage(), (int) topLeft.x, (int)topLeft.y, (int)botRight.x, (int)botRight.y, pxMin, pyMin, pxMax, pyMax, null);
//		g.drawImage(image, 0,0, 150,200, pxMin, pyMin, pxMax, pyMax, null);
		g2d.setTransform(currentTransform);
		return true;
	}

}
