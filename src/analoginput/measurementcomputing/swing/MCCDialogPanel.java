package analoginput.measurementcomputing.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogSensorUser;
import analoginput.measurementcomputing.MCCAnalogDevices;
import analoginput.measurementcomputing.MCCParameters;
import analoginput.swing.AnalogChannelPanel;

public class MCCDialogPanel implements PamDialogPanel {

	private MCCAnalogDevices mccAnalogDevices;
	
	private JComboBox<String> deviceNames;
	
	private JPanel mainPanel;
	
	private AnalogChannelPanel channelPanel;

	private AnalogSensorUser sensorUser;

	public MCCDialogPanel(MCCAnalogDevices mccAnalogDevices) {
		this.mccAnalogDevices = mccAnalogDevices;
		deviceNames = new JComboBox<>();
		fillNames();
		mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.NORTH, new JLabel("Selected device ..."));
		topPanel.add(BorderLayout.SOUTH, deviceNames);
		mainPanel.add(BorderLayout.NORTH, topPanel);
		deviceNames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDevice();
			}
		});
		sensorUser = mccAnalogDevices.getSensorUser();
		channelPanel = new AnalogChannelPanel(sensorUser.getChannelNames(), 8, mccAnalogDevices.getAvailableRanges(0), true);
		mainPanel.add(BorderLayout.SOUTH, channelPanel.getDialogComponent());
	}

	private void fillNames() {
		String[] devNames = mccAnalogDevices.getDeviceNames();
		deviceNames.setEnabled(true);
		for (int i = 0; i < devNames.length; i++) {
			deviceNames.addItem(devNames[i]);
		}
	}

	/**
	 * New specific device selected. ...
	 */
	private void selectDevice() {
		int devIndex = deviceNames.getSelectedIndex();
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		MCCParameters params = mccAnalogDevices.getMccParameters();
		if (deviceNames.getItemCount()>0 && params.deviceNumber < deviceNames.getItemCount()) {
			deviceNames.setSelectedIndex(params.deviceNumber);
		}
		AnalogDeviceParams deviceParams = params.getAnalogDeviceParams();
		channelPanel.setDeviceParams(deviceParams);
	}

	@Override
	public boolean getParams() {
		MCCParameters params = mccAnalogDevices.getMccParameters();
		params.deviceNumber = deviceNames.getSelectedIndex();
		AnalogDeviceParams deviceParams = params.getAnalogDeviceParams();
		deviceParams = channelPanel.getDeviceParams(deviceParams);
		if (deviceParams == null) {
			return PamDialog.showWarning(null, "MeasurementComputing Error", "Invalid input range data");			
		}
		params.setAnalogDeviceParams(deviceParams);
		return true;
	}

}
