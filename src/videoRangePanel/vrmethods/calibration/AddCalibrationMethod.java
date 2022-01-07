package videoRangePanel.vrmethods.calibration;

import java.awt.Point;
import java.awt.event.MouseEvent;

import PamView.panel.PamPanel;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;
import videoRangePanel.layoutAWT.VRCalibrationDialog;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.VROverlayAWT;
import videoRangePanel.vrmethods.VROverlayFX;

public class AddCalibrationMethod extends AbstractVRMethod {
	
	private VRControl vrControl; 

	private Point calibratePoint1, calibratePoint2;
	
	public static final int CALIBRATE_1 = 4;
	public static final int CALIBRATE_2 = 5;
	
	private int currentStatus=CALIBRATE_1;


	private CalibrationGUIAWT calibrationGUI;
	
	public AddCalibrationMethod(VRControl vrControl) {
		super(vrControl);
		this.vrControl=vrControl; 
		this.calibrationGUI=new CalibrationGUIAWT(this); 
	}
	

	
	@Override
	public String getName() {
		return "Add Calibration";
	}

	
	public PamPanel getSidePanel() {
		return this.calibrationGUI.getSidePanel(); 
	}
	
	/**
	 * JLayer overlay panel for the horizon method. 
	 * @author Jamie Macaulay
	 *
	 */
//	class CalibrateMethodUI extends VRAbstractLayerUI {
//		
//		private VRControl vrControl;
//		Point currentMouse;
//
//		public CalibrateMethodUI(VRControl vRControl){
//			super(vRControl);
//			this.vrControl=vRControl; 
//		}
//		
//		 @Override
//		 public void paint(Graphics g, JComponent c) {
//		    super.paint(g, c);
//		    addCalibrationMarks(g);
//		}
//
//		@Override
//		public void mouseClick(Point mouseClick) {
//			super.mouseClick(mouseClick);
//			switch (currentStatus) {
//			case CALIBRATE_1:
//				System.out.println("Calibration Point 1: "+calibratePoint1);
//				calibratePoint1 = new Point(mouseClick);
//				setCurrentStatus(CALIBRATE_2);
//				break;
//			case CALIBRATE_2:
//				System.out.println("Calibration Point 2: "+calibratePoint2);
//				calibratePoint2 = new Point(mouseClick);
//				vrControl.getVRPanel().repaint();
//				newCalibrationVal();
//				setCurrentStatus(CALIBRATE_1);
//				break;
//	
//			}
//			vrControl.getVRPanel().repaint();
//		}
//	}
	
	void setCurrentStatus(int status){
		this.currentStatus=status;
	}  

	
	void newCalibrationVal() {
		if (calibratePoint1 == null || calibratePoint2 == null) {
			return;
		}
		VRCalibrationData newCalibration = VRCalibrationDialog.showDialog(null, vrControl, null);
		if (newCalibration != null){
			vrControl.getVRParams().setCurrentCalibration(newCalibration);
		}
		clearOverlay();
		vrControl.update(VRControl.SETTINGS_CHANGE);
	}

	@Override
	public void clearOverlay() {
		calibratePoint1= calibratePoint2=null;
		setCurrentStatus(CALIBRATE_1);
		vrControl.getVRPanel().repaint();
	}


	@Override
	public void update(int updateType) {
		super.update(updateType);
		this.calibrationGUI.update(updateType);
	}
	
	public Point getCalibrationPoint1(){
		return calibratePoint1;
	}
	
	public Point getCalibrationPoint2(){
		return calibratePoint2;
	}



	@Override
	public VROverlayAWT getOverlayAWT() {
		return this.calibrationGUI;
	}



	@Override
	public VROverlayFX getOverlayFX() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Get the current status. 
	 * @return flag of the current status. 
	 */
	public int getCurrentStatus() {
		return this.currentStatus;
	}

	/**
	 * Set calibrate point 1. 
	 * @param point - the point to set. 
	 */
	public void setCalibratePoint1(Point point) {
		this.calibratePoint1=point; 	
	}

	/**
	 * Set calibrate point 2 
	 * @param point - the point to set. 
	 */
	public void setCalibratePoint2(Point point) {
		this.calibratePoint2=point; 	
	}


}
