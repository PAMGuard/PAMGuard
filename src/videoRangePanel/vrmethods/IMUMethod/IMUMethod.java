package videoRangePanel.vrmethods.IMUMethod;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import angleMeasurement.AngleDataUnit;
import videoRangePanel.VRControl;
import videoRangePanel.VRMeasurement;
import videoRangePanel.layoutAWT.AcceptMeasurementDialog;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.VROverlayAWT;
import videoRangePanel.vrmethods.VROverlayFX;
import videoRangePanel.vrmethods.landMarkMethod.VRLandMarkMethod;
import IMU.IMUDataBlock;
import PamUtils.PamArrayUtils;
import PamView.PamSymbol;
import PamView.panel.PamPanel;

/**
 * Uses sensor information, heading, pitch and roll, plus GPS co-ordinates and height to calculated the position of an animal. Also needs calibration data.
 * Note-Sensor convention
 *  <p>
 * Bearing- 0==north, 90==east 180=south, 270==west
 * <p>
 * Pitch- 90=-g, 0=0g, -90=g
 * <p>
 * Tilt 0->180 -camera turning towards left to upside down 0->-180 camera turning right to upside down
 * <p>
 * All angles are in RADIANS. 
 * @author Jamie Macaulay
 *
 */
public  class IMUMethod extends AbstractVRMethod  {
	
	//current status 
	private int currentStatus=MEASURE_ANIMAL;

	/**
	 * Temporary data unit to hold IMU data. Note that the angles stored in this data unit include any offsets. 
	 */
	private AngleDataUnit currentIMUData=null;

	private IMUMethodGUI imuMethodGUI;

	
	//the interval to average imu units of (milliseconds)
	private static long searchInterval=1000; 


	public IMUMethod(VRControl vrControl) {
		super(vrControl);
		imuMethodGUI = new IMUMethodGUI(this); 
//		this.imuLayerOverlay=new IMUMarkMethodUI(vrControl);
	}

	
	@Override
	public String getName() {
		return "IMU Data";
	}

	
	public PamPanel getSidePanel() {
		return imuMethodGUI.getSidePanel();
	}

	
//	/**
//	 * JLayer overlay panel for the horizon method. 
//	 * @author Jamie Macaulay
//	 *
//	 */
//	@SuppressWarnings("serial")
//	private class IMUMarkMethodUI extends VRAbstractLayerUI {
//		
//		private VRControl vrControl;
//
//		public IMUMarkMethodUI(VRControl vRControl){
//			super(vRControl);
//			this.vrControl=vRControl; 
//		}
//		
//		 @Override
//		 public void paint(Graphics g, JComponent c) {
//		    super.paint(g, c);
//		    addMeasurementMarks(g);
//			addAnimals(g);
//		}
//
//
//		 @Override
//			public void mouseClick(Point mouseClick) {
//				super.mouseClick(mouseClick);
//				switch (currentStatus) {
//				case MEASURE_ANIMAL:
//					measureAnimal(mouseClick);
//					break; 
//
//				}
//				vrControl.getVRPanel().repaint();
//		}
//		
//	}
	
	protected void measureAnimal(Point imPoint){
		VRMeasurement possibleMeasurement=new VRMeasurement(imPoint);
		newAnimalMeasuremnt_IMU(imPoint, possibleMeasurement);
		if (vrControl.getMeasuredAnimals()!=null){
			if (vrControl.getMeasuredAnimals().size()>0)
				imuMethodGUI.setAnimalLabels(vrControl.getMeasuredAnimals().get(vrControl.getMeasuredAnimals().size()-1));
		}
		vrControl.getVRPanel().repaint();
	}
	
	
	private boolean newAnimalMeasuremnt_IMU(Point imPoint, VRMeasurement possibleMeasurement){
		if (currentIMUData==null) return false;
		
		//set image tilt
		possibleMeasurement.imageBearing=Math.toDegrees(currentIMUData.getTrueHeading()); 
		possibleMeasurement.imageBearingErr=Math.toDegrees(currentIMUData.getErrorHeading()); 
		possibleMeasurement.imagePitch=Math.toDegrees(currentIMUData.getPitch()); 
		possibleMeasurement.imagePitchErr=Math.toDegrees(currentIMUData.getErrorPitch()); 
		possibleMeasurement.imageTilt=Math.toDegrees(currentIMUData.getTilt()); 
		possibleMeasurement.imageTiltErr=Math.toDegrees(currentIMUData.getErrorTilt()); 
		
		//set calibration value
		possibleMeasurement.calibrationData=vrControl.getVRParams().getCalibrationDatas().get( vrControl.getVRParams().getCurrentCalibrationIndex());
		Point imageCentre=new Point(vrControl.getVRPanel().getImageWidth()/2, vrControl.getVRPanel().getImageHeight()/2);
		
		//calculate the bearing relative to the centre of the image 
		double animalBearing=VRLandMarkMethod.calcAnimalBearing(currentIMUData.getTilt(), imageCentre, imPoint, 1/vrControl.getVRParams().getCalibrationDatas().get( vrControl.getVRParams().getCurrentCalibrationIndex()).degreesPerUnit);
		possibleMeasurement.locBearing=Math.toDegrees(animalBearing)+possibleMeasurement.imageBearing;
		possibleMeasurement.locBearingError=possibleMeasurement.imageBearingErr;
		possibleMeasurement.angleCorrection=Math.toDegrees(animalBearing);
		
		//calculate the pitch relative to the centre of the image 
		double animalPitch=-VRLandMarkMethod.calcAnimalPitch(currentIMUData.getTilt(), imageCentre,  imPoint, 1/vrControl.getVRParams().getCalibrationDatas().get( vrControl.getVRParams().getCurrentCalibrationIndex()).degreesPerUnit);
		possibleMeasurement.locPitch=Math.toDegrees(animalPitch)+possibleMeasurement.imagePitch;
		possibleMeasurement.locPitchError=possibleMeasurement.imagePitchErr;
		
		//paint the photo with possible measurements.
		candidateMeasurement=possibleMeasurement;
		//add symbols
		vrControl.getVRPanel().repaint();
		
		if (vrControl.getMeasuredAnimals()==null){
			vrControl.setMeasuredAnimals(new ArrayList<VRMeasurement>());
		}
		
		candidateMeasurement.vrMethod=this; 

		candidateMeasurement.imageTime=vrControl.getImageTime();
		candidateMeasurement.imageName = new String(vrControl.getImageName());
		candidateMeasurement.imageAnimal = vrControl.getMeasuredAnimals().size();
		candidateMeasurement.heightData = vrControl.getVRParams().getCurrentheightData().clone();
		candidateMeasurement.rangeMethod = vrControl.getRangeMethods().getCurrentMethod();
		
		//calc and set the range.
		candidateMeasurement.locDistance=vrControl.getRangeMethods().getCurrentMethod().getRange(vrControl.getCurrentHeight(), -Math.toRadians(candidateMeasurement.locPitch));
		
		//set distance errors
		//calc errors due to pitch error. The pitch error is calculated from the standard deviation of the averaged IMU data. 
		double range1Er=candidateMeasurement.locDistance-Math.abs(vrControl.getRangeMethods().getCurrentMethod().getRange(vrControl.getCurrentHeight(), -Math.toRadians(candidateMeasurement.locPitch+candidateMeasurement.imagePitchErr)));
		double range2Er=candidateMeasurement.locDistance-Math.abs(vrControl.getRangeMethods().getCurrentMethod().getRange(vrControl.getCurrentHeight(), -Math.toRadians(candidateMeasurement.locPitch-candidateMeasurement.imagePitchErr)));
		candidateMeasurement.locDistanceError=Math.abs(range1Er-range2Er)/2;
		//calc pixel errors
		double range1Pxl = vrControl.getRangeMethods().getCurrentMethod().getRange(vrControl.getCurrentHeight(), -Math.toRadians(candidateMeasurement.locPitch + candidateMeasurement.calibrationData.degreesPerUnit));
		double range2Pxl = vrControl.getRangeMethods().getCurrentMethod().getRange(vrControl.getCurrentHeight(), -Math.toRadians(candidateMeasurement.locPitch - candidateMeasurement.calibrationData.degreesPerUnit));
		double pxlError = Math.abs(range1Pxl-range2Pxl)/2;
//		System.out.println("pxl Error: "+pxlError);
		
		//add pixel error and error due to averaging imu pitch. 
		candidateMeasurement.locDistanceError=Math.sqrt(Math.pow(pxlError,2)+Math.pow(candidateMeasurement.locDistanceError,2));
		
		//try and work out a location for the animal
		candidateMeasurement.imageOrigin=vrControl.getLocationManager().getLocation(vrControl.getImageTime());
		calcLocLatLong(candidateMeasurement);
		
//		System.out.println("IMU Method: Animal Loc.: Bearing: "+candidateMeasurement.locBearing+ " Pitch: "+candidateMeasurement.locPitch);
		
		VRMeasurement newMeasurement = AcceptMeasurementDialog.showDialog(null, vrControl, candidateMeasurement);

		
		if (newMeasurement != null) {
			vrControl.getMeasuredAnimals().add(newMeasurement);
			vrControl.getVRProcess().newVRLoc(newMeasurement);
		}
		else {
			candidateMeasurement = null;
			return false;
		}
		
		candidateMeasurement = null;
				
		return true; 	
		
	}
	
	
//	@SuppressWarnings("rawtypes")
//	@Override
//	public LayerUI getJLayerOverlay() {
//		return imuLayerOverlay;
//	}

	@Override
	public void clearOverlay() {
		//clear animal measurements in abstract class
		super.clearPoints();
		currentStatus=MEASURE_ANIMAL;
		//clear ribbon panel
		imuMethodGUI.clearRibbonPanel();
		//repaint the vr panel
		vrControl.getVRPanel().repaint();
	}



	public PamPanel getRibbonPanel() {
		return imuMethodGUI.getRibbonPanel();
	}
	

	@Override
	public void update(int updateType) {
		super.update(updateType);
		switch (updateType){
		case VRControl.SETTINGS_CHANGE:
			findIMUData();
			imuMethodGUI.updateLabels();
		break;
		case VRControl.IMAGE_CHANGE:
			findIMUData();
			imuMethodGUI.updateLabels();
		break;
		case VRControl.METHOD_CHANGED:
			findIMUData();
			imuMethodGUI.updateLabels();
			break;
		}
		
	}

	
	/**
	 * Try and find IMU data. Set the currentIMUDData to null if no data found.
	 */
	private void findIMUData(){
		if (vrControl.getCurrentMethod()==this){
			if (vrControl.getIMUListener()!=null){
				if (vrControl.getIMUListener().getIMUDataBlock()!=null){
					 this.currentIMUData=searchIMUDataBlock(vrControl.getIMUListener().getIMUDataBlock(), vrControl.getImageTime(),getSearchInterval());
				}
			}
		}
	}
	
	/**
	 * Search the datablock for angle data. 
	 * @param imuDataBlock- the current IMU datablock
	 * @param timeMillis- the image time
	 * @param searchInterval- the interval to search for units between
	 * @return an AngledatUnit containing average values of units within the search window.
	 */
	public static AngleDataUnit searchIMUDataBlock(IMUDataBlock imuDataBlock, long timeMillis, long searchInterval){
		
		long millisStart=timeMillis-searchInterval/2;
		long millisEnd=timeMillis+searchInterval/2;
		ArrayList<AngleDataUnit> units=imuDataBlock.findUnitsinInterval(millisStart,millisEnd);
		
		if (units==null) return null;
		if (units.size()<=1) return null; 
		else {
			
			double[] calVals=imuDataBlock.getCalibrationVals();
			
			ArrayList<Double> headings=new ArrayList<Double>();
			ArrayList<Double> pitch=new ArrayList<Double>();
			ArrayList<Double> tilts=new ArrayList<Double>();
			
			for (int i=0; i<units.size(); i++){
				headings.add(units.get(i).getTrueHeading());
				pitch.add(units.get(i).getPitch());
				tilts.add(units.get(i).getTilt());
			}
			
//			System.out.println("heading.get(0): "+headings.get(0)+ " pitch.get(0): "+pitch.get(0)+" tilts.get(0): "+tilts.get(0));
			//work out mean values and errors
			Double[] imuVals=new Double[3];
			Double[] imuErrors=new Double[3]; 
			imuVals[0]=PamArrayUtils.mean(headings, 0)+calVals[0];
			imuVals[1]=PamArrayUtils.mean(pitch, 0)+calVals[1];
			imuVals[2]=PamArrayUtils.mean(tilts, 0)+calVals[2];
			
//			System.out.println("heading mean: "+imuVals[0]+ " pitch mean: "+imuVals[1]+" tilts. mean: "+imuVals[2]);

			imuErrors[0]=PamArrayUtils.std(headings, 0);
			imuErrors[1]=PamArrayUtils.std(pitch, 0);
			imuErrors[2]=PamArrayUtils.std(tilts, 0);
			//create a new AngleDataUnit to hold data.
			AngleDataUnit anglDataUnit=new AngleDataUnit(timeMillis, imuVals, imuErrors);
			anglDataUnit.setNUnits(headings.size());
			return anglDataUnit;
	
		}
	}
	
	//TODO-make changeable param; 
	public long getSearchInterval(){
		return searchInterval; 
	}
	

	public AngleDataUnit getCurrentIMUData() {
		return this.currentIMUData;
	}


	@Override
	public VROverlayAWT getOverlayAWT() {
		return this.imuMethodGUI;
	}


	@Override
	public VROverlayFX getOverlayFX() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the current status. 
	 * @return the current status flag. 
	 */
	public int getCurrentStatus() {
		return this.currentStatus;
	}


	

}
