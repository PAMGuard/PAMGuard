package noiseMonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import fftManager.FFTDataBlock;

public class UserBandDialog extends PamDialog {
	
	private static UserBandDialog singleInstance;
	
	private NoiseMeasurementBand noiseMeasurementBand;
	
	private FFTDataBlock sourceDataBlock;
	
	private JTextField name, startFreq, endFreq;
	
	private ResolutionPanel resolutionPanel = new ResolutionPanel();
	
	private JTextArea errMess=new JTextArea(" \n \n \n \n");
	
	private JPanel resE;
	
	private JPanel d;
	
	private JPanel err;

	private UserBandDialog(Window parentFrame) {
		super(parentFrame, "User Band", false);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
//		mainPanel.setBorder(new TitledBorder(""));

		resE = new JPanel(new BorderLayout());
		resE.setBorder(new TitledBorder("Frequency Resolution"));
		resE.add(BorderLayout.WEST, resolutionPanel.getPanel());
		mainPanel.add(BorderLayout.NORTH, resE);

		d = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(d, new JLabel("Name "), c);
		c.gridx++;
		c.gridwidth = 4;
		addComponent(d, name = new JTextField(20), c);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(d, new JLabel("Range "), c);
		c.gridx++;
		addComponent(d, startFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(d, new JLabel(" to "), c);
		c.gridx++;
		addComponent(d, endFreq = new JTextField(5), c);
		c.gridx++;
		addComponent(d, new JLabel(" Hz"), c);

		mainPanel.add(BorderLayout.CENTER, d);

		err = new JPanel(new BorderLayout());
		err.setBorder(new EmptyBorder(5, 5, 5, 5));
		errMess.setWrapStyleWord(true);
		errMess.setLineWrap(true);
		errMess.setEditable(false);
		errMess.setFocusable(false);
		errMess.setBackground(UIManager.getColor("Label.background"));
		errMess.setFont(UIManager.getFont("Label.font"));
		errMess.setBorder(UIManager.getBorder("Label.border"));
		err.add(BorderLayout.CENTER,errMess);
		mainPanel.add(BorderLayout.SOUTH, err);
		
		setDialogComponent(mainPanel);
	}
	
	public static NoiseMeasurementBand showDialog(Window parentFrame, 
			NoiseMeasurementBand noiseMeasurementBand, FFTDataBlock sourceDataBlock) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new UserBandDialog(parentFrame);
		}
		if (noiseMeasurementBand == null) {
			singleInstance.noiseMeasurementBand = null;
		}
		else {
			singleInstance.noiseMeasurementBand = noiseMeasurementBand.clone();
		}
		singleInstance.sourceDataBlock = sourceDataBlock;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.noiseMeasurementBand;
	}

	private void setParams() {
		resolutionPanel.setParams(sourceDataBlock);
		if (noiseMeasurementBand == null) {
			name.setText("");
			startFreq.setText("");
			endFreq.setText("");
		}
		else {
			name.setText(noiseMeasurementBand.name);
			startFreq.setText(String.format("%.1f", noiseMeasurementBand.f1));
			endFreq.setText(String.format("%.1f", noiseMeasurementBand.f2));
		}
		
	}

	@Override
	public void cancelButtonPressed() {
		noiseMeasurementBand = null;
	}

	@Override
	public boolean getParams() {
		if (noiseMeasurementBand == null) {
			noiseMeasurementBand = new NoiseMeasurementBand(null);
		}
		try {
			noiseMeasurementBand.name = name.getText();
			noiseMeasurementBand.f1 = Double.valueOf(startFreq.getText());
			noiseMeasurementBand.f2 = Double.valueOf(endFreq.getText());
		}
		catch (NumberFormatException e) {
			String theMessage = "Error reading frequency value - please check.";
			showErrorMessage(theMessage);
			return false;
		}
		if (!checkFreq(noiseMeasurementBand.f1)) {
			String theMessage = "Error - lower frequency limit is too high.";
			showErrorMessage(theMessage);
			return false; 
		}
		if (!checkFreq(noiseMeasurementBand.f2)) {
			String theMessage = "Error - upper frequency limit is too high.";
			showErrorMessage(theMessage);
			return false; 
		}
		if (noiseMeasurementBand.f2 <= noiseMeasurementBand.f1) {
			String theMessage = "Error - upper frequency limit is smaller than lower frequency limit.";
			showErrorMessage(theMessage);
			return false;
		}
		if (noiseMeasurementBand.f2-noiseMeasurementBand.f1<resolutionPanel.getFreqRes()) {
			String theMessage = "Error - frequency band range is smaller than frequency resolution.  Either ";
			theMessage += "increase the range selected, or decrease the resolution by increasing the FFT length.";
			showErrorMessage(theMessage);
			return false;
		}
		return true;
	}
	
	private void showErrorMessage(String message) {
		int dialogWidth = d.getWidth();
		errMess.setText(message);
		errMess.setPreferredSize(new Dimension(dialogWidth, errMess.getHeight()));
	}

	private boolean checkFreq(double f) {
		if (sourceDataBlock == null) {
			return true;
		}
		
		if (f > sourceDataBlock.getSampleRate()/2) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
