package analogarraysensor.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import PamController.StorageOptions;
import PamView.dialog.PamDialog;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamView.dialog.PamDialogPanel;
import analogarraysensor.ArraySensorControl;
import analogarraysensor.ArraySensorParams;

public class AnalogSensorDialog extends PamDialog {
	
	private static AnalogSensorDialog singleInstance;
	
	private ArraySensorControl analogSensorControl;
	
	private ArraySensorParams analogSensorParams;

	private PamDialogPanel analogDialogPanel;
	
	private JTextField streamerText, readInterval;
	
	private JCheckBox logRawData;

	private int nStreamers;

	private AnalogSensorDialog(Window parentFrame, ArraySensorControl analogSensorControl) {
		super(parentFrame, analogSensorControl.getUnitName() + " Settings", false);
		this.analogSensorControl = analogSensorControl;
		JPanel mainPanel = new JPanel(new BorderLayout());
		nStreamers = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		analogDialogPanel = analogSensorControl.getAnalogDevicesManager().getDialogPanel(this, analogSensorControl);
		mainPanel.add(BorderLayout.SOUTH, analogDialogPanel.getDialogComponent());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		topPanel.add(new JLabel("Number of streamers ", JLabel.RIGHT), c);
		c.gridx++;
		topPanel.add(streamerText = new JTextField(5), c);
		streamerText.setEditable(false);
		c.gridx = 0;
		c.gridy++;
		topPanel.add(new JLabel(" Readout interval ", JLabel.RIGHT), c);
		c.gridx++;
		topPanel.add(readInterval = new JTextField(5), c);
		c.gridx++;
		topPanel.add(new JLabel(" s ", JLabel.RIGHT), c);
		c.gridx = 0;
		c.gridwidth = 3;
		c.gridy++;
		topPanel.add(logRawData = new JCheckBox("Log raw sensor data to database"), c);
		JPanel westPanel = new WestAlignedPanel(topPanel);
		westPanel.setBorder(new TitledBorder("Array Control"));
		mainPanel.add(BorderLayout.CENTER, westPanel);
		
		setDialogComponent(mainPanel);
		setHelpPoint(analogSensorControl.getHelpPoint());
	}

	public static ArraySensorParams showDialog(Window parentFrame, ArraySensorControl analogSensorControl) {
		int nStreamers = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		if (singleInstance == null || singleInstance.getParent() != parentFrame || 
				singleInstance.analogSensorControl != analogSensorControl || nStreamers != singleInstance.nStreamers) {
			singleInstance = new AnalogSensorDialog(parentFrame, analogSensorControl);
		}
		singleInstance.setParams(analogSensorControl.getAnalogSensorParams());
		singleInstance.setVisible(true);
		return singleInstance.analogSensorParams;
	}

	private void setParams(ArraySensorParams analogSensorParams) {
		streamerText.setText(String.format("%d", nStreamers));
		this.analogSensorParams = analogSensorParams;
		DecimalFormat df = new DecimalFormat("####.####");
		readInterval.setText(df.format((double) analogSensorParams.readIntervalMillis/1000.));
		analogDialogPanel.setParams();
		boolean logRaw = StorageOptions.getInstance().getStorageParameters().isStoreDatabase(
				analogSensorControl.getAnalogSensorProcess().getSensorDataBlock(), false);
		logRawData.setSelected(logRaw);
	}

	@Override
	public boolean getParams() {
		try {
			double readInt = Double.valueOf(readInterval.getText());
			analogSensorParams.readIntervalMillis = (int) (readInt*1000);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid readout interval");
		}
		boolean ans = analogDialogPanel.getParams();
		boolean logRaw = logRawData.isSelected();
		StorageOptions.getInstance().getStorageParameters().setStorageOptions(
				analogSensorControl.getAnalogSensorProcess().getSensorDataBlock(), logRaw, false);
		return ans;
	}

	@Override
	public void cancelButtonPressed() {
		this.analogSensorParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
