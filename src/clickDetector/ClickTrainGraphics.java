package clickDetector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;

public class ClickTrainGraphics extends PamDetectionOverlayGraphics {

	ClickControl clickControl;
	
	ClickGroupDataBlock<ClickTrainDetection> clickTrains; // need this to check on first train at each draw

	ClickTrainDetection lastPlottedTrain = null;
	
	
	ClickTrainGraphics(ClickControl clickControl, ClickGroupDataBlock<ClickTrainDetection> clickTrains) {
		super(clickTrains, new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 15, 15, true, Color.RED, Color.RED));
		this.clickControl = clickControl;
		this.clickTrains = clickTrains;
	}


	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		
		ClickTrainDetection clickTrainDetection = (ClickTrainDetection) pamDetection;
		Color col = PamColors.getInstance().getWhaleColor(clickTrainDetection.getTrainId());
		setLocColour(col);
		setLineColour(col);
		if (shouldPlot(clickTrainDetection)) {
			return super.drawOnMap(g, pamDetection, generalProjector);
		}
		return null;
	}
	
	private boolean shouldPlot(ClickTrainDetection clickTrainDetection) {
		if (clickTrainDetection.getGroupDetectionLocalisation().getAmbiguityCount() > 0) {
			return true;
		}
		switch (clickControl.clickParameters.showShortTrains) {
		case ClickParameters.LINES_SHOW_ALL:
			return true;
		case ClickParameters.LINES_SHOW_NONE:
			return false;
		case ClickParameters.LINES_SHOW_SOME:
			return clickTrainDetection.isShouldPlot();
		}
		return clickTrainDetection.isShouldPlot();
	}


//	@Override
//	public double getDefaultRange() {
//		return clickControl.clickParameters.defaultRange;
//	}


	
/*
	@Override
	public boolean canDraw(GeneralProjector generalProjector) {
		// Thsi currently knows how to draw on the map
		if (generalProjector.getParmeterType(0) == ParameterType.LONGITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LATITUDE)
			return true;
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING
				&& generalProjector.getParmeterType(1) == ParameterType.RANGE)
			return true;
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING
				&& generalProjector.getParmeterType(1) == ParameterType.AMPLITUDE)
			return true;
		return false;
	}

	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {

		ClickTrainDetection clickTrainDataUnit = (ClickTrainDetection) pamDataUnit;
		
		if (clickTrainDataUnit.getTrainStatus() == ClickTrainDetection.STATUS_STARTING) return null;
		if (clickTrainDataUnit.getTrainStatus() == ClickTrainDetection.STATUS_BINME) return null;
		
		if (clickTrainDataUnit.getSubDetectionsCount() < clickControl.clickParameters.minTrainClicks) return null;
		
		if (generalProjector.getParmeterType(0) == ParameterType.LONGITUDE
				&& generalProjector.getParmeterType(1) == ParameterType.LATITUDE)
			return drawDataUnitOnMap(g, clickTrainDataUnit, generalProjector);
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING
				&& generalProjector.getParmeterType(1) == ParameterType.RANGE)
			return drawRangeOnRadar(g, clickTrainDataUnit, generalProjector);
		else if (generalProjector.getParmeterType(0) == ParameterType.BEARING
				&& generalProjector.getParmeterType(1) == ParameterType.AMPLITUDE)
			return drawAmplitudeOnRadar(g, clickTrainDataUnit, generalProjector);
		return null;
	}
	public Rectangle drawDataUnitOnMap(Graphics g, ClickTrainDetection clickTrain, GeneralProjector generalProjector){
		return drawOnMap(g, clickTrain, generalProjector);
	}

//	public Rectangle drawDataUnitOnMap(Graphics g, ClickTrainDetection clickTrain, GeneralProjector generalProjector) {
//		
//		if (canDraw(generalProjector) == false) return null;
//				
//		/*
//		 * Do a check to see if this is the first train in the list 
//		 * and if it is, reset lastPlottedTrain
//		 */
//		ClickTrainDetection fU = clickTrains.getDataUnit(0, PamDataBlock.REFERENCE_CURRENT);
//		if (fU != null && fU == clickTrain) {
//			lastPlottedTrain = null;
//		}
//				
//		if (clickTrain.getTrainStatus() == ClickTrainDetection.STATUS_STARTING) return null;
//		
//		if (shouldPlot(clickTrain) == false) return null;
//		
//		/*
//		 * If the train has a fit, then it may be possible to draw a point or an oval based on the 
//		 * errors. Otherwise, just draw lines from around the middle ot the train outwards.
//		 * 
//		 * The position is given as a time in ms relative to the first click and a distance off the beam. 
//		 * But the calculation used speed at the last click.
//		 */
//		int nClicks = clickTrain.getSubDetectionsCount();
//		//int middleClick = Math.max(1, Math.min(nClicks-1, nClicks / 2));
//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//
//		ClickDetection firstClickUnit = clickTrain.getSubDetection(0);
//		ClickDetection lastClickUnit = clickTrain.getSubDetection(nClicks-1);
//		int channels = clickTrain.getChannelBitmap();
//		int firstChannel = PamUtils.getLowestChannel(channels);
//		int lastChannel = PamUtils.getHighestChannel(channels);
//		int phone1 = array.getHydrophone(firstChannel).getID();
//		int phone2 = array.getHydrophone(lastChannel).getID();
//		LatLong firstLatLong = array.getHydrophoneLocator().getPhoneLatLong(firstClickUnit.getTimeMilliseconds(), phone1);
//		LatLong lastLatLong = array.getHydrophoneLocator().getPhoneLatLong(lastClickUnit.getTimeMilliseconds(), phone1);
//		double firstHeading = array.getHydrophoneLocator().getPairAngle(firstClickUnit.getTimeMilliseconds(), 
//				phone2, phone1, HydrophoneLocator.ANGLE_RE_NORTH);		
//		double lastHeading = array.getHydrophoneLocator().getPairAngle(lastClickUnit.getTimeMilliseconds(), 
//						phone2, phone1, HydrophoneLocator.ANGLE_RE_NORTH);
////		GpsData firstGps = firstClickUnit.GetGpsData();
////		GpsData lastGps = lastClickUnit.GetGpsData();
////		double firstSpeed = firstClickUnit.GetSpeedm();
////		double lastSpeed = lastClickUnit.GetSpeedm();
////		double firtsheading = array.getHydrophoneLocator().getPairAngle(clickDataUnit.getTimeMilliseconds(), 
////				1, 0, HydrophoneLocator.ANGLE_RE_NORTH);
//		/*
//		 * This is where the old system of not having a gps reference to every data unit falls apart
//		 * a bit - I need the local speed and heading in order to plot the data correctly. 
//		 * For now, I'll try calculating the speed based on the lat long, and will se where we
//		 * go from there - would have been better to calculate distances rather than relative
//		 * times in the click train detector, but no time ....
//		 */
//		double distanceTravelled = firstLatLong.distanceToMetres(lastLatLong);
//		double speed = distanceTravelled / (lastClickUnit.getTimeMilliseconds() - firstClickUnit.getTimeMilliseconds()) * 1000;
//		if (distanceTravelled <= 0) return null;
//		double perpDist = clickTrain.eRange;
//		double msFromFirstClick = clickTrain.eT0;
//		double msFromLastClick = msFromFirstClick - (lastClickUnit.getTimeMilliseconds() - firstClickUnit.getTimeMilliseconds());
//		double firstAngle = Math.atan2(perpDist, msFromFirstClick / 1000. * speed) * 180/Math.PI;
//		double lastAngle = Math.atan2(perpDist, msFromLastClick / 1000. * speed) * 180/Math.PI;
////		double firstHeading = array.getHydrophoneLocator().getPairAngle(firstClickUnit.getTimeMilliseconds(), 
////				1, 0, HydrophoneLocator.ANGLE_RE_NORTH);
////		double lastHeading = array.getHydrophoneLocator().getPairAngle(lastClickUnit.getTimeMilliseconds(), 
////				1, 0, HydrophoneLocator.ANGLE_RE_NORTH);
//		double firstRange = clickControl.clickParameters.defaultRange;
//		double lastRange = firstRange;
//		if (clickTrain.hasFit) {
//			firstRange = Math.sqrt(Math.pow(perpDist,2) + Math.pow(msFromFirstClick / 1000. * speed,2));
//			lastRange = Math.sqrt(Math.pow(perpDist,2) + Math.pow(msFromLastClick / 1000. * speed,2));
//		}
//		// now a load of stuff copied from the Click Projector.
//		// shoudl really combine some of this functionality !
//
//		Rectangle r;
//
//		Graphics2D g2 = (Graphics2D) g;
//		g2.setStroke(new BasicStroke(1));
//		g2.setColor(PamColors.getInstance().getWhaleColor(clickTrain.getTrainId()));
//		r = drawBearingLines (g, generalProjector, clickTrain, firstLatLong, firstHeading, firstAngle, firstRange);
//		r.add(drawBearingLines (g, generalProjector, clickTrain, lastLatLong, lastHeading, lastAngle, lastRange));
//		
//		return r;
//	}
//	@Override
//	protected boolean shouldPlot(PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
//		ClickTrainDetection clickTrain = (ClickTrainDetection) pamDataUnit;
//		if (clickTrain.getTrainStatus() == ClickTrainDetection.STATUS_STARTING) return false;
//		if (clickTrain.getSubDetectionsCount() < 10) return false;
//		if (clickTrain.isHasFit()) return true;
//		if (clickControl.clickParameters.showShortTrains == ClickParameters.LINES_SHOW_NONE) return false;
//		if (clickControl.clickParameters.showShortTrains == ClickParameters.LINES_SHOW_ALL) return true;
//		else {
//			/*
//			 * compare with last plotted train 
//			 * lastPlotted train gets nulled when overlal graphics is called for
//			 * the first train in the list, so time should only increase.
//			 */ 
//			if (lastPlottedTrain == null) {
//				lastPlottedTrain = clickTrain;
//				return true;
//			}
//			double dT = (double) (clickTrain.getTimeMilliseconds() - lastPlottedTrain.getTimeMilliseconds()) / 1000.;
//			if (dT < clickControl.clickParameters.minTimeSeparation) return false;
//			dT = Math.abs(clickTrain.firstClickAngle - lastPlottedTrain.firstClickAngle);
//			if (dT >= 0 && dT < clickControl.clickParameters.minBearingSeparation) return false;
//			lastPlottedTrain = clickTrain;
//			return true;
//		}
//	}
//	
//	public Rectangle drawBearingLines(Graphics g, GeneralProjector projector, PamDataUnit pamDataUnit, LatLong gpsData, double heading, double angle, double range) {
//
//		Rectangle r = new Rectangle();
//		Graphics2D g2 = (Graphics2D) g;
//		Coordinate3d coord = projector.getCoord3d(gpsData.getLatitude(),
//				gpsData.getLongitude(), 0);
//
//		// Graphics g = image.getGraphics();
//		/*
//		 * Need to work out two new lat longs for a given line length and get
//		 * thier positions as well, then draw the two lines.
//		 */
////		double heading = gpsData.getTrueCourse();// getHeading();
//		LatLong latlong = new LatLong(gpsData.getLatitude(),
//				gpsData.getLongitude());
//		LatLong left = latlong.travelDistanceMeters(
//				heading - angle, range);
//		LatLong right = latlong.travelDistanceMeters(heading
//				+ angle, range);
//		// double lineLength = 10000; // in meters
//		// double lineLengthNMI = lineLength / 1852.
//		Point origin = new Point((int) coord.x, (int) coord.y);
//		coord = projector.getCoord3d(left.getLatitude(), left.getLongitude(), 0);
//		projector.addHoverData(coord, pamDataUnit);
//
//		g.drawLine(origin.x, origin.y, (int) coord.x, (int) coord.y);
//		coord = projector.getCoord3d(right.getLatitude(), right.getLongitude(), 0);
//		projector.addHoverData(coord, pamDataUnit);
//		g.drawLine(origin.x, origin.y, (int) coord.x, (int) coord.y);
//		((Graphics2D) g).setStroke(new BasicStroke(0.5f));
//
//		return r;
//	}
////	public Rectangle drawRangeOnRadar(Graphics g, ClickTrainDetection clickTrain, GeneralProjector generalProjector) {
////
////		Rectangle r = new Rectangle();
//////		double bearing = clickTrain.firstClickAngle;
////		double angle, range;
////		if (clickTrain.isHasFit() == false) {
////			return null;
//////			angle = clickTrain.lastFittedAngle;
//////			range = 1000000;
////		}
////		else {
////			long timeNow = PamCalendar.getTimeInMillis();
//////			angle = clickTrain.expectedAngle(timeNow);
////			angle = clickTrain.lastFittedAngle;
////			range = clickTrain.getPerpendiculaError(0);
////		}
////		Coordinate3d c3d = generalProjector.getCoord3d(angle, range, 0);
////		getPamSymbol().setFillColor(PamColors.getInstance().getWhaleColor(clickTrain.getTrainId()));
////		getPamSymbol().setLineColor(PamColors.getInstance().getWhaleColor(clickTrain.getTrainId()));
////		getPamSymbol().draw(g, c3d.getXYPoint());
//////		radarSymbol.draw(g, new Point(10,10));
////		generalProjector.addHoverData(c3d, clickTrain);
////		return r;
////	}
////	public Rectangle drawAmplitudeOnRadar(Graphics g, ClickTrainDetection clickTrain, GeneralProjector generalProjector) {
////
////		Rectangle r = new Rectangle();
//////		double bearing = clickTrain.firstClickAngle;
//////		if (clickTrain.hasFit == false) return null;
////		double angle = clickTrain.lastFittedAngle;
////		ClickDetection lastClick;
//////		ArrayList<ClickDetection> clickList = clickTrain.clickList; 
////		if (clickTrain.getSubDetectionsCount() == 0) return null;
////		lastClick = clickTrain.getSubDetection(clickTrain.getSubDetectionsCount()-1);
////		double amplitude = lastClick.getAmplitudeDB();
////		Coordinate3d c3d = generalProjector.getCoord3d(angle, amplitude, 0);
////		getPamSymbol().setFillColor(PamColors.getInstance().getWhaleColor(clickTrain.getTrainId()));
////		getPamSymbol().setLineColor(PamColors.getInstance().getWhaleColor(clickTrain.getTrainId()));
////		getPamSymbol().draw(g, c3d.getXYPoint());
////		generalProjector.addHoverData(c3d, clickTrain);
////		return r;
////	}
//
//	/* (non-Javadoc)
//	 * @see clickDetector.NewClickOverlayGraphics#createMapKey(int)
//	 */
////	@Override
////	protected PamKeyItem createMapKey(int keyType) {
////		// TODO Auto-generated method stub
////		return super.createMapKey(keyType);
////	}
//
////
////	/* (non-Javadoc)
////	 * @see clickDetector.NewClickOverlayGraphics#createRadarKey(int)
////	 */
////	@Override
////	protected PamKeyItem createRadarKey(int keyType) {
//////		if (keyType == PamKeyItem.KEY_SHORT) {
//////			return new BasicKeyItem(ClickBTDisplay.getClickSymbol(0).clone(), getName());
//////		}
//////		else {
////			return new BasicKeyItem(makeMultiColourIcon(PamSymbol.ICON_STYLE_SYMBOL, getPamSymbol()), getName());
//////		}
////	}
//
//
////	/* (non-Javadoc)
////	 * @see clickDetector.NewClickOverlayGraphics#createSpectrogramKey(int)
////	 */
////	@Override
////	protected PamKeyItem createSpectrogramKey(int keyType) {
////		// TODO Auto-generated method stub
////		return super.createSpectrogramKey(keyType);
////	}
//
//
//	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit) {
//		ClickTrainDetection clickTrain = (ClickTrainDetection) dataUnit;
//		String str = "<html>Click Train";
//		str += String.format("<br> Time: %s to %s", PamCalendar.formatTime(clickTrain.getTimeMilliseconds()),
//				PamCalendar.formatTime(clickTrain.getTimeMilliseconds()));
//		if (clickTrain.isHasFit()) {
//			str += String.format("<br>Range: %3.1fm<br>Last Angle: %3.1f\u00B0", 
//					clickTrain.getPerpendiculaError(0), clickTrain.lastFittedAngle);
//		}
//		else {
//			str += String.format("<br>Last Angle: %3.1f\u00B0<br>Range unknown", 
//					clickTrain.lastFittedAngle);
//		}
//		str += String.format("<br> Channels: %s", PamUtils.getChannelList(clickTrain.getChannelBitmap()));
//		str += "</html>";
//		return str;
//	}*/
}
