package videoRangePanel.layoutAWT;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import videoRangePanel.VRControl;
import videoRangePanel.VRMeasurement;

import PamView.DBTextArea;
import PamView.dialog.PamDialog;

public class AcceptMeasurementDialog extends PamDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static AcceptMeasurementDialog singleInstance; 
	
	VRMeasurement vrMeasurement;
	
	VRControl vrControl;
	
	JTextField range;
	
	JTextField accuracy;
	
	JTextField frameAngle;
	
	JTextField groupNumber;
	
	JTextField individualNumber;
		
	JLabel imageName;
	
	JLabel imageItem;
	
	JLabel angleLabel;
	
	JPanel angleRow;
	
	JTextField cameraAngle, angleCorrection, angleTotal;
	
	DBTextArea comment;
	
	private AcceptMeasurementDialog(Frame parentFrame) {
		super(parentFrame, "Store Location", false);
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Accept Location"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(2,2,2,2);
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(p, imageName = new JLabel(" "), c);
		c.gridy++;
		addComponent(p, imageItem = new JLabel(" "), c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Range : "), c);
		c.gridx++;
		addComponent(p, range = new JTextField(12), c);
		range.setEditable(false);
		range.setToolTipText("Estimated range to animal");
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Pixel accuracy : "), c);
		c.gridx++;
		addComponent(p, accuracy = new JTextField(12), c);
		accuracy.setEditable(false);//		
		accuracy.setToolTipText("Range error based on single pixel accuracy");
		// angle stuff
		c.gridx = 0;
		c.gridy++;
		addComponent(p, angleLabel = new JLabel("Bearing (\u00B0)"), c);
		c.gridx++;
		angleRow = new JPanel();
		angleRow.setLayout(new GridBagLayout());
		GridBagConstraints aa = new GridBagConstraints();
		aa.gridx = aa.gridy = 0;
		addComponent(angleRow,cameraAngle = new JTextField(5),aa);
		aa.gridx++;
		addComponent(angleRow,new JLabel("+"),aa);
		aa.gridx++;
		addComponent(angleRow,angleCorrection = new JTextField(3),aa);
		aa.gridx++;
		addComponent(angleRow,new JLabel("="),aa);
		aa.gridx++;
		addComponent(angleRow,angleTotal = new JTextField(5),aa);
		addComponent(p, angleRow, c);
		cameraAngle.setToolTipText("Pointing angle of camera");
		angleCorrection.setToolTipText("Correction based on position of animal in image");
		angleTotal.setToolTipText("Best estimate of angle to animal");
		cameraAngle.addKeyListener(new AngleListener());
		angleCorrection.addKeyListener(new AngleListener());
		
		c.gridx = 0;
		c.gridy++;
		addComponent(p, new JLabel("Comment ... "), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		comment = new DBTextArea(5,5,VRControl.DBCOMMENTLENGTH);
		addComponent(p, comment.getComponent(), c);
		comment.getComponent().setPreferredSize(new Dimension(80, 50));
		
		getOkButton().setText("Accept");
		getCancelButton().setText("Reject");
		
		setDialogComponent(p);

		pack();
	}
	
	public static final VRMeasurement showDialog(Frame frame, VRControl vrControl, VRMeasurement vrMeasurement) {
		//repaint the panel for canditate measurment info
		vrControl.getVRPanel().repaint();
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new AcceptMeasurementDialog(frame);
		}
		singleInstance.vrControl = vrControl;
		singleInstance.vrMeasurement = vrMeasurement.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.vrMeasurement;
	}

	@Override
	public void cancelButtonPressed() {
		vrMeasurement = null;
	}

	public void setParams() {
				
		imageName.setText("Image: " + vrMeasurement.imageName);
		imageItem.setText("Animal # " + vrMeasurement.imageAnimal);
		range.setText(String.format("%.3f m", vrMeasurement.locDistance));
		accuracy.setText(String.format("± %.3f m/pix", vrMeasurement.pixelAccuracy));
		
		if (vrMeasurement.imageBearing != null) {
			cameraAngle.setText(String.format("%.3f", vrMeasurement.imageBearing));
//			angleValue.setToolTipText("Absolute angle including image pos correction");
		}
		else {
			cameraAngle.setText("");
//			angleValue.setToolTipText("image pos correction only");
		}
		angleCorrection.setText(String.format("%.3f", vrMeasurement.angleCorrection));
		
		setAngletotal();
		
		comment.setText("");
	}
	
	private void setAngletotal() {
		double shaftAngle = 0, correction = 0;
		try {
			shaftAngle = Double.valueOf(cameraAngle.getText());
		}
		catch (NumberFormatException e) {
			shaftAngle = 0;
		}
		try {
			correction = Double.valueOf(angleCorrection.getText());
		}
		catch (NumberFormatException e) {
			correction = 0;
		}
		angleTotal.setText(String.format("%.1f", shaftAngle + correction));
	}
	
	class AngleListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
			setAngletotal();			
		}

		public void keyReleased(KeyEvent e) {
			setAngletotal();			
		}

		public void keyTyped(KeyEvent e) {
			setAngletotal();			
		}
		
	}
	
	@Override
	public boolean getParams() {
//		vrMeasurement.comment = comment.getText();
		vrMeasurement.comment = comment.getText();
		try {
			vrMeasurement.angleCorrection = Double.valueOf(angleCorrection.getText());
			vrMeasurement.locBearing = Double.valueOf(angleTotal.getText());
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		try {
			if (cameraAngle.getText().isEmpty())  vrMeasurement.imageBearing=0.;
			else vrMeasurement.imageBearing = Double.valueOf(cameraAngle.getText());
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			vrMeasurement.imageBearing = null;
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
