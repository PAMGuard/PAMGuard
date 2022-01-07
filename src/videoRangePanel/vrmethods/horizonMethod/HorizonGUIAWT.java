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

import PamView.PamColors;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import videoRangePanel.VRControl;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.vrmethods.AbstractVRGUIAWT;
import videoRangePanel.vrmethods.AbstractVRMethod;
import videoRangePanel.vrmethods.ImageAnglePanel;

public class HorizonGUIAWT extends AbstractVRGUIAWT {
	
	//panel components
	PamLabel lab;
	PamLabel calibrationData;
	PamLabel statusText;
	PamLabel instruction;
	JButton clearHorizon;
	JComboBox<String> calibrations;
	PamPanel sidePanel;
	private VRHorizonMethod horizonMethod;
	

	/**
	 * The current mouse location in image co-ordinates
	 */
	private Point currentMouse;

	
	
//	private HorizonMethodUI horizonMethodUI;

	private ImageAnglePanel imageAnglePanel;

	public HorizonGUIAWT(VRHorizonMethod horizonMethod) {
		super(horizonMethod);
		this.horizonMethod=horizonMethod; 
		// TODO Auto-generated constructor stub
	}
	
	
	private PamPanel createSidePanel(){
		
		PamPanel panel=new PamPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		
		c.gridx=0;
		c.gridy=0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, lab=new PamLabel("Horizon Measurement "), c);
		lab.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridy++;
		c.gridx=0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, createCalibrationList(), c);

		c.gridy++;
		PamDialog.addComponent(panel, instruction = new PamLabel("...."), c);
		instruction.setFont(PamColors.getInstance().getBoldFont());
		
		c.gridx=0;
		c.gridy++;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, clearHorizon = new JButton("Clear Horizon"), c);
		clearHorizon.addActionListener(new ClearHorizon());
		c.gridy++;
		c.gridy++;
		
		newCalibration();
		
		setInstruction(horizonMethod.getCurrentStatus());
		
		return panel;
		
	}

	/**
	 * Get the side panel 
	 * @return
	 */
	public PamPanel getSidePanel() {
		if (sidePanel==null) sidePanel=createSidePanel();
		return sidePanel;
	}
	

	public PamPanel getRibbonPanel() {
		if (imageAnglePanel==null) {
			imageAnglePanel=new ImageAnglePanel(vrControl,horizonMethod);
			imageAnglePanel.removeTiltSpinner();
		}
		return imageAnglePanel;
	}

	
	private class ClearHorizon implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			horizonMethod.clearOverlay(); 
		}
	}
	
	
	/**
	 * Sets the text telling the user what to do. 
	 * @param status- the current status
	 */
	public void setInstruction(int status){

		switch (status) {
		case VRHorizonMethod.MEASURE_HORIZON_1:
			instruction.setText("Click horizon point 1");
			break;
		case VRHorizonMethod.MEASURE_HORIZON_2:
			instruction.setText("Click horizon point 2");
			break;
		case AbstractVRMethod.MEASURE_ANIMAL:
			imageAnglePanel.sayHorizonInfo() ;
			imageAnglePanel.setTiltLabel(horizonMethod.getHorizonTilt());
			instruction.setText("Click animal");
			break;
		}
		//check there are no general instructions-e.g. no image
		//super.setInstruction(instruction);
	}
	
	public void update(int updateType){
		super.update(updateType);
		switch (updateType){
			case VRControl.HEADING_UPDATE:
				if (imageAnglePanel!=null){
					horizonMethod.setImageHeading(imageAnglePanel.getImageHeading()); 
				}
			break;
			case VRControl.SETTINGS_CHANGE:
				if (imageAnglePanel!=null){
					imageAnglePanel.newCalibration();
					imageAnglePanel.newRefractMethod();
				}
			break;
		}
	}

	/**
	 * Called whenever the overlay marks are cleared. 
	 */
	public void clearOverlay() {
		imageAnglePanel.clearPanel();
	}


	@Override
	public PamPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void paintMarks(Graphics g) {
		addMeasurementMarks(g);
		addAnimals(g);
	}
	

	private void addMeasurementMarks(Graphics g) {
//		System.out.println("Horizon method: Draw marks: " + currentMouse);
		drawMarksandLine(g, horizonMethod.getHorizonPoint1() , horizonMethod.getHorizonPoint2(), 
				VRPanel.horizonSymbol.getPamSymbol(), vrControl.getVRParams().drawTempHorizon, getCurrentMouse());
	}
	
	
	/**
	 * Get the current mouse points
	 * @return - the current mouse point. 
	 */
	public Point getCurrentMouse() {
		return this.currentMouse;
	}
	
	
	@Override
	public void mouseAction(MouseEvent e, boolean motion) {
//		System.out.println("Mouse action event: "+ MEASURE_HORIZON_1);
		Point mouseClick=vrControl.getVRPanel().screenToImage(e.getPoint());
		currentMouse=e.getPoint();
		if (!motion & e.getID() == MouseEvent.MOUSE_CLICKED) {
			switch (horizonMethod.getCurrentStatus()) {
			case VRHorizonMethod.MEASURE_HORIZON_1:
//				System.out.println("Mouse action event: MEASURE_HORIZON_1"+ MEASURE_HORIZON_1);
				horizonMethod.setHorizonPoint1(new Point(mouseClick));
				horizonMethod.setVrStatus(VRHorizonMethod.MEASURE_HORIZON_2);
				break;
			case VRHorizonMethod.MEASURE_HORIZON_2:
//				System.out.println("Mouse action event: MEASURE_HORIZON_2"+ MEASURE_HORIZON_1);
				horizonMethod.setHorizonPoint2(new Point(mouseClick));
				horizonMethod.calculateHorizonTilt();
				horizonMethod.setVrStatus(VRHorizonMethod.MEASURE_ANIMAL);
				break;
			case VRHorizonMethod.MEASURE_ANIMAL:
//				System.out.println("Mouse action event: MEASURE_ANIMAL"+ MEASURE_HORIZON_1);
				horizonMethod.newAnimalMeasurement_Horizon(mouseClick);
				//			setVrStatus(MEASURE_DONE);
				break;
			}
		}

		//always repaint on mouse motion as well as click. 
		vrControl.getVRPanel().repaint();
		
	}

	

}

