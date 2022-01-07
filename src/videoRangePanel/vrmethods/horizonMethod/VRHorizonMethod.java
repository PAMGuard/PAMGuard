package videoRangePanel.vrmethods.horizonMethod;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.plaf.LayerUI;

import videoRangePanel.VRControl;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.VROverlayAWT;
import videoRangePanel.vrmethods.VROverlayFX;
import PamView.PamColors;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;

/**
 * The horizon method allows a user to click two points to define a horizon and then click an animal to define a RANGE. 
 * Requires a calibration value and a height. 
 * @author Doug Gillespie and Jamie Macaulay
 *
 */
public class VRHorizonMethod extends AbstractVRMethod {
	
	
	private VRControl vrControl;
	
	
	//current status
	public static final int MEASURE_HORIZON_1 = 0;
	public static final int MEASURE_HORIZON_2 = 1;

	int currentStatus=MEASURE_HORIZON_1;


	private HorizonGUIAWT horizonGUI;
		
	public VRHorizonMethod(VRControl vrControl){
		super(vrControl); 
		this.vrControl=vrControl;
		this.horizonGUI = new HorizonGUIAWT(this); 
//		this.horizonMethodUI=new HorizonMethodUI(vrControl);
	}

	
	public PamPanel getSidePanel() {
		return horizonGUI.getSidePanel(); 
	}

	
	public void update(int updateType){
		this.horizonGUI.update(updateType);
	}
	
	protected void setVrStatus(int status){
		this.currentStatus=status;
		this.horizonGUI.setInstruction(status);
	}
	
	@Override
	public String getName() {
		return "Measure Horizon";
	}


	
	public PamPanel getRibbonPanel() {
		return horizonGUI.getRibbonPanel();
	}
	
	
	/**
	 * JLayer overlay panel for the horizon method. 
	 * @author Jamie Macaulay
	 *
	 */
//	private class HorizonMethodUI extends VRAbstractLayerUI {
//		
//		private VRControl vrControl;
//		Point currentMouse; 
//
//		public HorizonMethodUI(VRControl vRControl){
//			super(vRControl);
//			this.vrControl=vRControl; 
//		}
//		
//		 @Override
//		 public void paint(Graphics g, JComponent c) {
//		    super.paint(g, c);
//			addMeasurementMarks(g);
//			addAnimals(g);
//		}
//
//		@Override
//		public void mouseClick(Point mouseClick) {
//				super.mouseClick(mouseClick);
//				switch (currentStatus) {
//				case MEASURE_HORIZON_1:
//					horizonPoint1 = new Point(mouseClick);
//					setVrStatus(MEASURE_HORIZON_2);
//					break;
//				case MEASURE_HORIZON_2:
//					horizonPoint2 = new Point(mouseClick);
//					calculateHorizonTilt();
//					setVrStatus(MEASURE_ANIMAL);
//					break;
//				case MEASURE_ANIMAL:
//					newAnimalMeasurement_Horizon(mouseClick);
////					setVrStatus(MEASURE_DONE);
//					break;
//				}
//				vrControl.getVRPanel().repaint();
//		}
//		 
//	}
	
	@Override
	public void clearOverlay() {
		//clear points on the screen
		clearPoints();
		setVrStatus(MEASURE_HORIZON_1);
		//clear the image angle panel 
		horizonGUI.clearOverlay(); 
		vrControl.getVRPanel().repaint();

	}


	/**
	 * Get the current status. 
	 * @return the current status flag. 
	 */
	public int getCurrentStatus() {
		return this.currentStatus;
	}

	/**
	 * Set the image heading. 
	 * @param imageHeading - the image heading. 
	 */
	public void setImageHeading(Double imageHeading) {
		this.imageHeading=imageHeading; 
		
	}

	/**
	 * Get the first horizon point on the image
	 * @return - the point in pixels on the image 
	 */
	public Point getHorizonPoint1() {
		return this.horizonPoint1;
	}
	
	/**
	 * Get the second horizon point on the image
	 * @return - the point in pixels on the image 
	 */
	public Point getHorizonPoint2() {
		return this.horizonPoint2;
	}


	@Override
	public VROverlayAWT getOverlayAWT() {
		return horizonGUI;
	}


	@Override
	public VROverlayFX getOverlayFX() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Set the first horizon point.
	 * @param point the new horizon point 1
	 */
	public void setHorizonPoint1(Point point) {
		this.horizonPoint1=point; 
	}

	/**
	 * Set the second horizon point.
	 * @param point the new horizon point 2
	 */
	public void setHorizonPoint2(Point point) {
		this.horizonPoint2=point; 
	}

}



