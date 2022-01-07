package difar.trackedGroups;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import difar.DifarControl;
import GPS.GpsData;
import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

/**
 * TrackedGroupOverlayGraphics show a summary of all the bearings
 * to each tracked group (on the map)
 *
 */
public class TrackedGroupOverlayGraphics extends  PamDetectionOverlayGraphics {

	private TrackedGroupDataBlock trackedGroupDataBlock;
	private DifarControl difarControl;
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.black, Color.black);

	private float dash1[] = {5.0f, 5.f};

	public TrackedGroupOverlayGraphics(DifarControl difarControl, TrackedGroupDataBlock trackedGroupDataBlock) {
		super(trackedGroupDataBlock, new PamSymbol(defaultSymbol));
		this.difarControl = difarControl;
		this.trackedGroupDataBlock = trackedGroupDataBlock;
		this.setDefaultRange(50000);
	}

	/**
	 * Draw a wedge that is centred on the mean bearing for the group.
	 * The width of the wedge is +/- 2 standard deviations from the
	 * mean of all the bearings to the group (2 standard deviations 
	 * clockwise and again 2 more standard deviations anticlockwise).
	 * 
	 * TODO: A few things for the future:
	 * 1. Colour lines by groups
	 * 2. Make a key item to show different groups
	 * 3. Improve on the tooltiptext.
	 * @author Brian Miller 
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		TrackedGroupDataUnit trackedGroupDataUnit = (TrackedGroupDataUnit) pamDetection;
		
		if (trackedGroupDataUnit.getGroupName().equals(difarControl.getDifarParameters().DefaultGroup)){
			return null;
		}
		
		Graphics2D g2d = (Graphics2D) g;
		PamSymbol dataSymbol = getPamSymbol(trackedGroupDataUnit, generalProjector);
		Color col = dataSymbol.getLineColor();

		/* 
		 * Presently a hack to make bearing lines narrower 
		 * since lookupItems don't store lineThickness 
		 * independently for each lookupItem. -BSM
		 */
		dataSymbol.setLineThickness(0.5f);
		BasicStroke defaultLine = new BasicStroke(dataSymbol.getLineThickness()+3.0f);
		BasicStroke latestLine = new BasicStroke(dataSymbol.getLineThickness()+1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		
		if(difarControl.getDifarParameters().timeScaledOpacity){
			int minalpha = 30;
			int alpha = (int) Math.round(64.-(64.-minalpha)*((PamCalendar.getTimeInMillis()-trackedGroupDataUnit.getMostRecentDetectionTime())
					/(difarControl.getDifarParameters().timeToFade*60000.)));
			alpha=Math.max(alpha, minalpha);
			alpha=Math.min(alpha, 255);
			col = new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ); 
			g.setColor(col);
			dataSymbol.setLineColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), alpha ));
		}
		else {
			g.setColor(col);
		}

		Stroke oldStroke = g2d.getStroke();

		if (dataSymbol != null) {
			g2d.setStroke(defaultLine);
			g2d.setColor(col);
		}

		GpsData detOrigin = trackedGroupDataUnit.getOriginLatLong(false);
		if (detOrigin == null) return null;
		double range =  difarControl.getDifarParameters().defaultLength;
		Coordinate3d crossPoint = null;

//TODO: Revisit code below after adding feature: crossBearings for TrackedGroups 
		TrackedGroupCrossingInfo crossInfo = trackedGroupDataUnit.getDifarCrossing();
//		if (crossInfo == null) {
//			crossInfo = trackedGroupDataUnit.getTempCrossing();
//		}
		if (crossInfo != null) {
			LatLong crossLatLong = crossInfo.getCrossLocation();
			crossPoint = generalProjector.getCoord3d(crossLatLong.getLatitude(), crossLatLong.getLongitude(), 0);
			dataSymbol.draw(g2d, crossPoint.getXYPoint());
			range = detOrigin.distanceToMetres(crossLatLong);
			generalProjector.addHoverData(crossPoint, pamDetection);
 
			Double[] errors = crossInfo.getErrors();
//			FIXME: Errors is not being set in the viewer is causing a null pointer exception. 
//			For now disable in viewer mode, but really should fix the underlying cause. 

			if (errors.length >= 2 && errors[0] != null && errors[1] != null && !Double.isNaN(errors[0]) && !Double.isNaN(errors[1])) {
				LatLong ell = crossLatLong.addDistanceMeters(errors[0], errors[1]);
				Coordinate3d errPoint = generalProjector.getCoord3d(ell.getLatitude(), ell.getLongitude(), 0);
				double dx = errPoint.x-crossPoint.x;
				double dy = errPoint.y-crossPoint.y;
				g.drawLine((int) (crossPoint.x-dx), (int) (crossPoint.y), (int) errPoint.x, (int) crossPoint.y);
				g.drawLine((int) (crossPoint.x), (int) (crossPoint.y-dy), (int) crossPoint.x, (int) errPoint.y);
			}
			
			g2d.setColor(Color.BLACK); // This should be 100% opaque
			g2d.drawString(trackedGroupDataUnit.getGroupName(), (int) crossPoint.x, (int) crossPoint.y);
			g2d.setColor(col);
			return null;
		}
		// skip the default drawing and do it ourselves. 
		Double bearing = trackedGroupDataUnit.getMeanBearing();
		Double bearingMinusStd = bearing - 2 * trackedGroupDataUnit.getBearingSTD();
		Double bearingPlusStd = bearing + 2 * trackedGroupDataUnit.getBearingSTD();

		Coordinate3d cenPoint = generalProjector.getCoord3d(detOrigin.getLatitude(), detOrigin.getLongitude(), 0);
		if (bearing == null) {
			dataSymbol.draw(g2d, cenPoint.getXYPoint());
			generalProjector.addHoverData(cenPoint, pamDetection);
		}
		else {
			LatLong meanLatLong = detOrigin.travelDistanceMeters(bearing, range);
			LatLong westLatLong = detOrigin.travelDistanceMeters(bearingMinusStd, range);
			LatLong eastLatLong = detOrigin.travelDistanceMeters(bearingPlusStd,  range);
			Coordinate3d endPoint = generalProjector.getCoord3d(meanLatLong.getLatitude(), meanLatLong.getLongitude(), 0);
			Coordinate3d westPoint = generalProjector.getCoord3d(westLatLong.getLatitude(), westLatLong.getLongitude(), 0);
			Coordinate3d eastPoint = generalProjector.getCoord3d(eastLatLong.getLatitude(), eastLatLong.getLongitude(), 0);
			int [] xs = {(int) cenPoint.x, (int) westPoint.x, (int) endPoint.x, (int) eastPoint.x, (int) cenPoint.x};
			int [] ys = {(int) cenPoint.y, (int) westPoint.y, (int) endPoint.y, (int) eastPoint.y, (int) cenPoint.y};
			g.fillPolygon(xs, ys, xs.length);

			// Draw a line instead of a wedge
			if (trackedGroupDataUnit.getNumBearings()<2 || trackedGroupDataUnit.getBearingSTD() < 1){
				g.drawLine((int) cenPoint.x, (int) cenPoint.y, (int) endPoint.x, (int) endPoint.y);
			}
			generalProjector.addHoverData(endPoint, pamDetection);
			g.setColor(Color.BLACK); // This should be 100% opaque
			g.drawString(trackedGroupDataUnit.getGroupName(), (int) endPoint.x, (int) endPoint.y);
			g.setColor(col);
			if (crossPoint != null) {
				g.drawLine((int) crossPoint.x, (int) crossPoint.y, (int) endPoint.x, (int) endPoint.y); 
			}
		}
		
		
		g2d.setStroke(oldStroke);
		return null;
	}
	
	@Override
	protected boolean canDrawOnSpectrogram() {
		return false;
	};

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#getHoverText(PamView.GeneralProjector, PamguardMVC.PamDataUnit, int)
	 */
	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		TrackedGroupDataUnit pd = (TrackedGroupDataUnit) dataUnit;
		return pd.getSummaryString();

	}

}
