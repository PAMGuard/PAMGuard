package videoRangePanel.vrmethods.shoreMethod;

import java.awt.Point;
import java.awt.event.MouseEvent;

import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamView.panel.PamPanel;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.VRHeightData;
import videoRangePanel.VRHorzCalcMethod;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.ShoreManager;
import videoRangePanel.vrmethods.VROverlayAWT;
import videoRangePanel.vrmethods.VROverlayFX;

/**
 * AbstractVRMethod contains many useful functions to create panels, update methods , 
 * @author Jamie Macaulay
 *
 */
public class VRShoreMethod extends AbstractVRMethod {
	
	//shore manager
	private  ShoreManager shoreManager;	
	private double[] shoreRanges;

	
	public static final int NEED_GPS_MAP=5;
	public static final int NEED_BEARING=6;
	public static final int MEASURE_SHORE = 4;
	
	
	private int currentStatus=4; 
	
	private Point shorePoint;
	
	private ShoreMethodGUIAWT shoreGUI;




	public VRShoreMethod(VRControl vrControl) {
		super(vrControl);
		this.vrControl=vrControl;
		this.shoreManager = new ShoreManager(vrControl.getMapFileManager()); 
		this.shoreGUI= new ShoreMethodGUIAWT(this); 
	}

	@Override
	public String getName() {
		return "Shore Method";
	}

	
	public PamPanel getSidePanel() {
		return shoreGUI.getSidePanel();
	}

	
	private void clearShore(){
		clearPoints();
		horizonPointsFromShore(horizonTilt);
		setVrSubStatus(MEASURE_SHORE);
	}
	
	@Override
	protected void clearPoints() {
		super.clearPoints(); 
		shorePoint=null; 
	}
	

//	private void selectGPS(){
//	
//	}

	
	/**
	 * JLayer overlay panel for the horizon method. 
	 * @author Jamie Macaulay
	 *
	 */
//	class ShoreMethodUI extends VRAbstractLayerUI {
//		
//		private VRControl vrControl;
//		Point currentMouse; 
//
//		public ShoreMethodUI(VRControl vRControl){
//			super(vRControl);
//			this.vrControl=vRControl; 
//		}
//		
//		 @Override
//		 public void paint(Graphics g, JComponent c) {
//		    super.paint(g, c);
//
//			if (vrControl.getVRParams().getShowShore()) {
//				drawLand(g);
//			}
//			
//			addMeasurementMarks(g);
//			addAnimals(g);
//		}
//
//		@Override
//		public void mouseClick(Point mouseClick) {
//			super.mouseClick(mouseClick);
//			switch (currentStatus) {
//				case MEASURE_SHORE:
//					System.out.println("shore Point clicked: "+currentStatus);
//					shorePoint = new Point(mouseClick);
//					if (horizonPointsFromShore(horizonTilt)) {
//						setVrSubStatus(MEASURE_ANIMAL);
//					}
//					else shorePoint=null; 
//					break;
//				
//				case MEASURE_ANIMAL:
//					System.out.println("Measure Animal from  shore: "+currentStatus);
//					newAnimalMeasurement_Shore(mouseClick);
//				break;
//			}
//			vrControl.getVRPanel().repaint();
//		}
//		 
//	}

	
	protected void setVrSubStatus(int status) {
		this.currentStatus=status;
		this.shoreGUI.setInstruction(status);
	}
	
	
	protected boolean newAnimalMeasurement_Shore(Point animalPoint) {
		return newAnimalMeasurement_Horizon(animalPoint);
	}
	

	protected Point getObjectPoint(LatLong origin, double height, double degreesPerUnit, 
			double imageAngle, LatLong objectLL, VRHorzCalcMethod vrRangeMethod) {
		if (vrRangeMethod == null) {
			return null;
		}
		double range = origin.distanceToMetres(objectLL);
		double angle = vrRangeMethod.getAngle(height, range);
		if (angle < 0) {
			return null;
		}
		double bearing = origin.bearingTo(objectLL);
		double angDiff = PamUtils.constrainedAngle(imageAngle - bearing, 180);
		if (Math.abs(angDiff) > 90) {
			return null;
		}
		int x = bearingTox(imageAngle, bearing);
		int y = (int) (getHorizonPixel(x) + vrRangeMethod.getAngle(height, range) * 180 / Math.PI / degreesPerUnit);
		return new Point(x,y);
	}
	
	
	private void calcShoreRanges(){
		if (imageHeading==null) {
			shoreRanges=null;
			return; 
		}
		shoreRanges = shoreManager.getSortedShoreRanges(getGPSinfo(), imageHeading);
	}

	
	protected boolean horizonPointsFromShore(double tilt) {
		// work out where the horizon should be based on the shore point. 
		horizonPoint1 = horizonPoint2 = null;
		//System.out.println("shore Point: "+shorePoint);
		if (shorePoint == null) {
			return false;
		}
		int imageWidth = vrControl.getVRPanel().getImageWidth();
		if (imageWidth == 0) {
			return false;
		}
		VRCalibrationData calData = vrControl.getVRParams().getCurrentCalibrationData();
		VRHeightData heightData = vrControl.getVRParams().getCurrentheightData();
		VRHorzCalcMethod vrRangeMethod = vrControl.getRangeMethods().getCurrentMethod();
		Double imageBearing = getImageHeading();

		if (vrRangeMethod == null || calData == null || heightData == null || imageBearing == null) {
			return false;
		}
		double pointBearing = imageBearing + (shorePoint.x - imageWidth/2) * calData.degreesPerUnit;
		
		double[] ranges = shoreManager.getSortedShoreRanges(getGPSinfo(), pointBearing);
		
		Double range = getshoreRange(ranges);

		if (range == null) {
			return false;
		}
		// dip from horizon to this point. 
		Double angleTo = vrRangeMethod.getAngle(heightData.height, range);
		if (angleTo < 0) {
			// over horizon
			return false; 
		}
		int y =  (int) (shorePoint.y - angleTo * 180 / Math.PI / calData.degreesPerUnit);
		double xD = shorePoint.x;
		horizonPoint1 = new Point(0, y + (int) (xD * Math.tan(getHorizonTilt() * Math.PI / 180)));
		xD = imageWidth - shorePoint.x;
		horizonPoint2 = new Point(imageWidth, y - (int) (xD * Math.tan(getHorizonTilt() * Math.PI / 180)));
		return true;
	}
	
	/**
	 * Get the shore range we want to use - not necessarily the closest. 
	 * @return shore range to use in VR calculations. 
	 */
	public Double getShoreRange() {
		return getshoreRange(shoreRanges);
	}
	
	public Double getshoreRange(double[] ranges) {
		int want = 0;
		if (ranges == null) {
			return null;
		}
		if (vrControl.getVRParams().ignoreClosest) {
			want = 1;
		}
		if (ranges.length < want+1) {
			return null;
		}
		return ranges[want];
	}
	
	@Override
	public void clearOverlay() {
		clearShore();
		vrControl.getVRPanel().repaint();
	}

	
	@Override
	public void update(int updateType){
		super.update(updateType);
		shoreGUI.update(updateType);
		switch (updateType){
			case VRControl.SETTINGS_CHANGE:
				 calcShoreRanges();
				 vrControl.getVRPanel().repaint();
				 break;
			case VRControl.HEADING_UPDATE:
				imageHeading=shoreGUI.getImageHeading(); 
				calcShoreRanges();
				horizonPointsFromShore(horizonTilt);
				vrControl.getVRPanel().repaint();
				break;
			case VRControl.TILT_UPDATE:
				horizonTilt=shoreGUI.getImageTilt();
				horizonPointsFromShore(horizonTilt);
				vrControl.getVRPanel().repaint();
				break;
			case VRControl.IMAGE_CHANGE:
				break;
		}
	}

	public int getCurrentStatus() {
		return this.currentStatus;
	}

	@Override
	public VROverlayAWT getOverlayAWT() {
		return this.shoreGUI;
	}

	@Override
	public VROverlayFX getOverlayFX() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the shore amanger. Handles shore data. 
	 * @return - the shore manager
	 */
	public ShoreManager getShoreManager() {
		// TODO Auto-generated method stub
		return this.shoreManager;
	}
	
	/**
	 * Get the current shore point
	 * @return the shore point
	 */
	public Point getShorePoint() {
		return shorePoint;
	}

	protected Point getHorizonPoint1() {
		return this.horizonPoint1;
	}
	
	protected Point getHorizonPoint2() {
		return this.horizonPoint2;
	}

	/**
	 * Set the current shore point. 
	 * @param point the point to set. 
	 */
	public void setShorePoint(Point point) {
		this.shorePoint=point;
	}


	

}
