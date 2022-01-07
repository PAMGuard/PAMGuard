package angleMeasurement;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import serialComms.SerialPortPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class FluxgateWorldDialog extends PamDialog implements AngleMeasurementListener {

	private SerialPortPanel serialPortPanel;
	
	private FluxgateWorldParameters fluxgateWorldParameters;
	
	static private FluxgateWorldDialog singleInstance;
	
	private FluxgateWorldAngles fluxgateWorldAngles;
	
	private AngleLoggingDialogPanel angleLoggingDialogPanel = new AngleLoggingDialogPanel();
	
	private JLabel currentRawAngle, currentCorrectedAngle;
	
	private JButton setZero;
	
	private JButton cancelZero;
	
	private JTextField geoReferenceAngle;
	
	private JCheckBox invertAngles;
	
	private JComboBox readRateList;
	
	Timer timer;
	
	private boolean dontGet = true;
	
	public FluxgateWorldDialog(Frame parentFrame, FluxgateWorldAngles fluxgateWorldAngles) {
		super(parentFrame, "Fluxgate World Settings", false);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JPanel portPanel = new JPanel();
		portPanel.setLayout(new GridBagLayout());
		portPanel.setBorder(new TitledBorder("Com Port and data rate"));
		GridBagConstraints c = new PamGridBagContraints();
		serialPortPanel = new SerialPortPanel(null);
		c.gridwidth = 2;
		addComponent(portPanel, serialPortPanel.getPanel(), c);
		serialPortPanel.getPortList().addActionListener(new PortAction());
		c.gridy++;
		c.gridwidth = 1;
		addComponent(portPanel, new JLabel("Encoder read interval"), c);
		c.gridx++;
		addComponent(portPanel, readRateList = new JComboBox(), c);
//		c.gridx++;
//		addComponent(portPanel, new JLabel(" s"), c);

		readRateList.removeAllItems();
		for (int i = 0; i < FluxgateWorldParameters.readRateIntervals.length; i++) {
			readRateList.addItem(String.format("%.1f s", (double)FluxgateWorldParameters.readRateIntervals[i] / 1000));
		}
		
		
		panel.add(BorderLayout.NORTH, portPanel);
		
		JPanel zeroPanel = new JPanel();
		zeroPanel.setBorder(new TitledBorder("Setup Measurement"));
		zeroPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(2,5,2,5);
		c.gridwidth = 4;
		addComponent(zeroPanel, invertAngles = new JCheckBox("Invert angles"), c);
		invertAngles.addActionListener(new InvertAngles());
		c.gridy++;
		c.gridwidth = 1;
		addComponent(zeroPanel, new JLabel("Raw Angle "), c);
		c.gridx++;
		addComponent(zeroPanel, currentRawAngle = new JLabel("No data"), c);
		c.gridx++;
		addComponent(zeroPanel, new JLabel(" Corrected = "), c);
		c.gridx++;
		addComponent(zeroPanel, currentCorrectedAngle = new JLabel("No data"), c);
		c.gridy++;
		c.gridx = 2;
		c.gridwidth = 2;
		addComponent(zeroPanel, cancelZero = new JButton("Clear Offset"), c);
		cancelZero.addActionListener(new ClearOffset());
		c.gridy++;
		addComponent(zeroPanel, setZero = new JButton("Set Offset"), c);
		setZero.addActionListener(new SetOffset());
		c.gridwidth = 1;
		c.gridx = 0;
		addComponent(zeroPanel, new JLabel("Zero to "), c);
		c.gridx++;
		addComponent(zeroPanel, geoReferenceAngle = new JTextField(4), c);
		
		panel.add(BorderLayout.SOUTH, zeroPanel);
		
		tabbedPane.add("Settings", panel);
		tabbedPane.add("Logging", angleLoggingDialogPanel.getPanel());
		
		setDialogComponent(tabbedPane);
		
		fluxgateWorldAngles.addMeasurementListener(this);
		
		dontGet = false;
		
	}
	
	public static FluxgateWorldParameters showDialog(Frame parentFrame, FluxgateWorldAngles fluxgateWorldAngles, FluxgateWorldParameters fluxgateWorldParameters) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new FluxgateWorldDialog(parentFrame, fluxgateWorldAngles);
		}
		if (fluxgateWorldParameters == null) {
			singleInstance.fluxgateWorldParameters = new FluxgateWorldParameters();
		}
		else {
			singleInstance.fluxgateWorldParameters = fluxgateWorldParameters.clone();
		}
		singleInstance.fluxgateWorldAngles  = fluxgateWorldAngles;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.fluxgateWorldParameters;
	}

	@Override
	public void cancelButtonPressed() {

		fluxgateWorldParameters = null;

	}

	private void setParams() {
		dontGet = true;
		readRateList.setSelectedIndex(fluxgateWorldParameters.readRateIndex);
		geoReferenceAngle.setText(String.format("%.1f", fluxgateWorldParameters.geoReferenceAngle));
		serialPortPanel.setPort(fluxgateWorldParameters.portName);
		invertAngles.setSelected(fluxgateWorldParameters.invertAngles);
		angleLoggingDialogPanel.setParams(fluxgateWorldParameters.getAngleLoggingParameters());
		dontGet = false;
	}
	
	@Override
	public boolean getParams() {
		if (dontGet) return false;
		fluxgateWorldParameters.portName = new String(serialPortPanel.getPort());
		Double g = getGeoReferenceAngle();
		if (g == null) {
			fluxgateWorldParameters.geoReferenceAngle = 0;
		}
		else {
			fluxgateWorldParameters.geoReferenceAngle = g;
		}
		fluxgateWorldParameters.invertAngles = invertAngles.isSelected();
		fluxgateWorldParameters.readRateIndex = readRateList.getSelectedIndex();
		AngleLoggingParameters ap = angleLoggingDialogPanel.getParams();
		if (ap == null) {
			return false;
		}
		else {
			fluxgateWorldParameters.setAngleLoggingParameters(ap.clone());
		}
		return true;
	}
	
	private Double getGeoReferenceAngle() {
		try {
			return Double.valueOf(geoReferenceAngle.getText());
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	class PortAction implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if (getParams() == false) {
				return;
			}
			
			fluxgateWorldAngles.setFluxgateWorldParameters(fluxgateWorldParameters.clone());
			fluxgateWorldAngles.start();
			
		}
		
	}
	
	class ClearOffset implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			fluxgateWorldParameters.angleOffset = 0;
			fluxgateWorldAngles.setFluxgateWorldParameters(fluxgateWorldParameters.clone());
		}
	}
	
	class SetOffset implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Double angle = fluxgateWorldAngles.getCalibratedAngle();
			Double geoReference = getGeoReferenceAngle();
			if (angle != null && geoReference != null) {
				fluxgateWorldParameters.angleOffset = angle - geoReference;
				fluxgateWorldAngles.setFluxgateWorldParameters(fluxgateWorldParameters.clone());
			}
		}
	}
	
	class InvertAngles implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (getParams()) {
				fluxgateWorldAngles.setFluxgateWorldParameters(fluxgateWorldParameters.clone());				
			}
		}
		
	}

	Double latestRawAngle;
	public void newAngle(Double rawAngle, Double calibratedAngle, Double correctedAngle) {

		latestRawAngle = rawAngle;
		if (rawAngle != null) {
			currentRawAngle.setText(String.format("%.1f\u00B0", rawAngle));
//			calibrationPanel.rawAngle.setText(String.format("Current raw angle measurement = %.1f\u00B0", rawAngle));
//			calibrationPanel.enableControls(true);
		}
		if (correctedAngle != null) {
			currentCorrectedAngle.setText(String.format("%.1f\u00B0", correctedAngle));
		}
		
		
	}
	
}
