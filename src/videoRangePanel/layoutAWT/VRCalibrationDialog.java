package videoRangePanel.layoutAWT;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import videoRangePanel.VRCalibrationData;
import videoRangePanel.VRControl;

@SuppressWarnings("serial")
public class VRCalibrationDialog extends PamDialog {

	private VRCalibrationData vrCalibrationData;
	private VRCalibrationData existingCalibrationData;
	private VRControl vrControl;
	private static VRCalibrationDialog singleInstance;
	
	private JTextField objectSize, objectDistance;
	private JTextField calibrationValue;
	private JTextField name;
	private JButton addButton, replaceButton, closeButton;
	
	private VRCalibrationDialog(Frame parentFrame, VRCalibrationData existingCalibrationData) {
		super(parentFrame, "Video Calibration", false);
		this.existingCalibrationData = existingCalibrationData;
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Object information"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);
		c.gridwidth = 3;
		addComponent(p, name = new JTextField(20), c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Object size "), c);
		c.gridx++;
		addComponent(p, objectSize = new JTextField(6), c);
		objectSize.addKeyListener(new ObjectKeyListener());
		c.gridx++;
		addComponent(p, new JLabel(" cm"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Object distance "), c);
		c.gridx++;
		addComponent(p, objectDistance = new JTextField(6), c);
		objectDistance.addKeyListener(new ObjectKeyListener());
		c.gridx++;
		addComponent(p, new JLabel(" m"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Calibration "), c);
		c.gridx++;
		addComponent(p, calibrationValue = new JTextField(9), c);
		c.gridx++;
		addComponent(p, new JLabel(" \u00B0 / pixel"), c);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.NORTH, p);
		
		JPanel buttonPanel = getButtonPanel();
		if (existingCalibrationData == null) {
			getOkButton().setVisible(false);
			getCancelButton().setVisible(false);
			buttonPanel.add(addButton = new JButton("Add new"));
			buttonPanel.add(replaceButton = new JButton("Replace"));
			buttonPanel.add(closeButton = new JButton("Close"));	
			addButton.addActionListener(new AddButton());
			replaceButton.addActionListener(new ReplaceButton());
			closeButton.addActionListener(new CloseButton());
		}
//		this.get
		setDialogComponent(panel);
		
		pack();
		
	}
	
	/**
	 * 
	 * @param frame owner window
	 * @param vrControl VR controller
	 * @param existingCalibration null if it's a new calibration from the image panel, in which case
	 * there will be a pair of clicked points to get data from. Not null if it's from the edit of 
	 * new buttons on the main VR dialog. 
	 * @return null if cancel pressed. Otherwise calibration data. 
	 */
	public static VRCalibrationData showDialog(Frame frame, VRControl vrControl, VRCalibrationData existingCalibration) {
//		if (singleInstance == null || frame != singleInstance.getOwner()) {
			singleInstance = new VRCalibrationDialog(frame, existingCalibration);
//		}
		if (existingCalibration != null) {
			singleInstance.vrCalibrationData = existingCalibration.clone();
		}
		else if (vrControl.getVRParams().getCurrentCalibrationData() != null) {
			singleInstance.vrCalibrationData = vrControl.getVRParams().getCurrentCalibrationData().clone();
		}
		singleInstance.vrControl = vrControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		
		return singleInstance.vrCalibrationData;
	}

	@Override
	public void cancelButtonPressed() {
		vrCalibrationData = null;
	}
	
	private void setParams() {
		// blank form is true if it's a new dialog 
		if (vrCalibrationData == null) {
			return;
		}
		name.setText(vrCalibrationData.name);
		objectDistance.setText(String.format("%.1f", vrCalibrationData.objectDistance_m));
		objectSize.setText(String.format("%.1f", vrCalibrationData.objectSize_cm));
		
		if (existingCalibrationData == null) {
			updateCalibration();
		}
		else {
			calibrationValue.setText(String.format("%.6f", vrCalibrationData.degreesPerUnit));
		}

		enableButtons();
	}

	public boolean getObjectParams() {
		if (vrCalibrationData == null) {
			vrCalibrationData = new VRCalibrationData();
		}
		try {
			vrCalibrationData.objectDistance_m = Double.valueOf(objectDistance.getText());
			vrCalibrationData.objectSize_cm = Double.valueOf(objectSize.getText());
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean getParams() {
		getObjectParams();
		try {
			vrCalibrationData.name = name.getText();
			vrCalibrationData.degreesPerUnit = Double.valueOf(calibrationValue.getText());
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	void enableButtons() {
		if (replaceButton != null) {
			replaceButton.setEnabled(vrControl.getVRParams().getCurrentCalibrationData() != null);
		}
	}
	
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	class ObjectKeyListener extends KeyAdapter {

		@Override
		public void keyTyped(KeyEvent e) {
			updateCalibration();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			updateCalibration();
		}
		
	}
	void updateCalibration() {
		if (existingCalibrationData != null) {
			return;
		}
		if (getObjectParams() == false) {
			return;
		}
		if (vrCalibrationData.objectDistance_m <= 0 ||
				vrCalibrationData.objectSize_cm <= 0) {
			return;
		}
		Point p1, p2;
		//TODO
		p1 = vrControl.getCalibrationMethod().getCalibrationPoint1();
		p2 = vrControl.getCalibrationMethod().getCalibrationPoint2();
		if (p1 == null || p2 == null) {
			return;
		}
		double pixs = p2.distance(p1);
//		double ang = Math.atan(vrCalibrationData.objectSize_cm / 2 / 100 / vrCalibrationData.objectDistance_m) * 2;
		double ang = Math.atan(vrCalibrationData.objectSize_cm / 100 / vrCalibrationData.objectDistance_m);
		ang *= 180  / Math.PI;
		double degPerPix = ang / pixs;
		
		calibrationValue.setText(String.format("%4g", degPerPix));
	}
	
	class AddButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (getParams()) {
				vrControl.getVRParams().addCalibrationData(vrCalibrationData);
				setVisible(false);
			}
		}
	}
	
	class ReplaceButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (getParams() && vrControl.getVRParams().getCurrentCalibrationData() != null) {
				vrControl.getVRParams().getCurrentCalibrationData().update(vrCalibrationData);
				vrCalibrationData = vrControl.getVRParams().getCurrentCalibrationData();
				setVisible(false);
			}
		}
	}
	
	class CloseButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			closeButtonPress();
		}
	}
	
	void closeButtonPress() {
		vrCalibrationData = null;
		setVisible(false);
	}

}
