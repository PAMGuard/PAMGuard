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
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;

@Deprecated
public class GridOverlayDraw extends PanelOverlayDraw{

	public GridOverlayDraw(PamSymbol defaultSymbol) {
		super(defaultSymbol);
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(0) == ParameterType.LATITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LONGITUDE) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		return null;
	}

	private Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		GridDataUnit gridDataUnit = (GridDataUnit) pamDataUnit;
		Graphics2D g2d = (Graphics2D) g;
		MapRectProjector mapProj = (MapRectProjector) generalProjector;
		BufferedImage image = gridDataUnit.getImage();
		if (image == null) {
			return null;
		}
		double lat[] = gridDataUnit.getLatArray();
		double lon[] = gridDataUnit.getLonArray();
		double gLatMin = lat[0];
		double gLatMax = lat[lat.length-1];
		double gLonMin = lon[0];
		double gLonMax = lon[lon.length-1];
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
		int pxMin = (int) ((pLonMin-gLonMin)/(gLonMax-gLonMin)*lon.length);
		int pxMax = (int) ((pLonMax-gLonMin)/(gLonMax-gLonMin)*lon.length);
		int pyMax = lat.length - (int) ((pLatMin-gLatMin)/(gLatMax-gLatMin)*lat.length);
		int pyMin = lat.length - (int) ((pLatMax-gLatMin)/(gLatMax-gLatMin)*lat.length);
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
		g.drawImage(image, (int) topLeft.x, (int)topLeft.y, (int)botRight.x, (int)botRight.y, pxMin, pyMin, pxMax, pyMax, null);
//		g.drawImage(image, 0,0, 150,200, pxMin, pyMin, pxMax, pyMax, null);
		g2d.setTransform(currentTransform);
		return r;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return null;
	}


}
