package videoRangePanel.vrmethods.calibration;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import PamView.PamColors;
import PamView.dialog.PamDialog;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import videoRangePanel.layoutAWT.VRPanel;
import videoRangePanel.vrmethods.AbstractVRGUIAWT;

public class CalibrationGUIAWT extends AbstractVRGUIAWT {

	//Side Panel components
	private PamPanel sidePanel;
	private PamLabel lab;
	private PamLabel instruction;
	private JButton clearCalibration;
	private AddCalibrationMethod calibrationMethod;
	
	private Point currentMouse;

	public CalibrationGUIAWT(AddCalibrationMethod calibrationMethod) {
		super(calibrationMethod);
		this.calibrationMethod=calibrationMethod; 
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
		PamDialog.addComponent(panel, lab=new PamLabel("Calibration Measurement "), c);
		lab.setFont(PamColors.getInstance().getBoldFont());

		c.gridy++;
		c.gridx=0;
		c.gridwidth = 1;
		PamDialog.addComponent(panel, createCalibrationList(), c);

		c.gridy++;
		c.insets = new Insets(10,0,0,0);
		//		PamDialog.addComponent(panel, instruction = new PamLabel(""), c);
		//		instruction.setFont(PamColors.getInstance().getBoldFont());

		c.gridx=0;
		c.gridy++;
		c.gridwidth = 2;
		PamDialog.addComponent(panel, clearCalibration = new JButton("Clear Calibration"), c);
		c.insets=new Insets(0,0,0,0);
		clearCalibration.addActionListener(new ClearCalibration());
		c.gridy++;
		c.gridy++;

		newCalibration();

		return panel;	
	}

	private class ClearCalibration implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			calibrationMethod.clearOverlay();
		}
	}
	
	public PamPanel getSidePanel() {
		if (sidePanel==null) sidePanel=createSidePanel();
		return sidePanel;
	}
	
	private void addCalibrationMarks(Graphics g) {
		//System.out.println("Calibration Points: "+calibratePoint1+ "   "+calibratePoint2);
		drawMarksandLine(g, calibrationMethod.getCalibrationPoint1(), calibrationMethod.getCalibrationPoint2(), 
				VRPanel.calibrationSymbol.getPamSymbol(), true, currentMouse);
	}

	@Override
	public PamPanel getRibbonPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamPanel getSettingsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void paintMarks(Graphics g) {
		  addCalibrationMarks(g);
	}
	
	@Override
	public void mouseAction(MouseEvent e, boolean motion) {
		Point mouseClick=vrControl.getVRPanel().screenToImage(e.getPoint());
		currentMouse=e.getPoint();
		switch (calibrationMethod.getCurrentStatus()) {
		case AddCalibrationMethod.CALIBRATE_1:
//			System.out.println("Calibration Point 1: "+calibrationMethod.getCalibrationPoint1());
			calibrationMethod.setCalibratePoint1(new Point(mouseClick));
			calibrationMethod.setCurrentStatus(AddCalibrationMethod.CALIBRATE_2);
			break;
		case AddCalibrationMethod.CALIBRATE_2:
//			System.out.println("Calibration Point 2: "+calibrationMethod.getCalibrationPoint2());
			calibrationMethod.setCalibratePoint2(new Point(mouseClick));
			vrControl.getVRPanel().repaint();
			calibrationMethod.newCalibrationVal();
			calibrationMethod.setCurrentStatus(AddCalibrationMethod.CALIBRATE_1);
			break;

		}
		vrControl.getVRPanel().repaint();
		
	}




}
