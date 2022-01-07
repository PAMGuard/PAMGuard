package analoginput.brainboxes.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamController.PamController;
import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.warn.WarnOnce;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogRangeData;
import analoginput.AnalogSensorUser;
import analoginput.brainboxes.BrainBoxDevices;
import analoginput.brainboxes.BrainBoxParams;
import analoginput.swing.AnalogChannelPanel;

public class BBDialogPanel implements PamDialogPanel {

	private BrainBoxDevices brainBoxDevices;
	
	private AnalogSensorUser analogUser;
	
	private JPanel mainPanel;
	
	private JTextField ipAddr;
	
	private AnalogChannelPanel channelPanel;

	/**
	 * @param brainBoxDevices
	 * @param analogUser
	 */
	public BBDialogPanel(BrainBoxDevices brainBoxDevices, AnalogSensorUser analogUser) {
		super();
		this.brainBoxDevices = brainBoxDevices;
		this.analogUser = analogUser;
		mainPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.NORTH, new JLabel("Device ip address"));
		topPanel.add(BorderLayout.SOUTH, ipAddr = new JTextField(15));
		mainPanel.add(BorderLayout.NORTH, topPanel);
		channelPanel = new AnalogChannelPanel(analogUser.getChannelNames(), brainBoxDevices.getNumChannels(), brainBoxDevices.getAvailableRanges(0), true);
		mainPanel.add(BorderLayout.CENTER, channelPanel.getDialogComponent());
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		BrainBoxParams params = brainBoxDevices.getBrainBoxParams();
		ipAddr.setText(params.ipAddress);
		AnalogDeviceParams deviceParams = params.getAnalogDeviceParams();
		channelPanel.setDeviceParams(deviceParams);
	}

	@Override
	public boolean getParams() {
		BrainBoxParams params = brainBoxDevices.getBrainBoxParams();
		String addr = ipAddr.getText();
		if (addr == null || addr.length() < 9) {
			return PamDialog.showWarning(null, "BrainBox Error", "Invalid ip address");
		}
		params.ipAddress = addr;
		AnalogDeviceParams deviceParams = params.getAnalogDeviceParams();
		deviceParams = channelPanel.getDeviceParams(deviceParams);
		if (deviceParams == null) {
			return PamDialog.showWarning(null, "BrainBox Error", "Invalid input range data");			
		}
		params.setAnalogDeviceParams(deviceParams);
		String msg = "<html>Note that when selecting Voltage and Current ranges for this device, "+
				"it may be necessary to move internal jumpers on the device circuit board.<br><br>"+
				"Please refer to the device manual.</html>";
		WarnOnce.showWarning(PamController.getMainFrame(), brainBoxDevices.getUnitName(), msg, WarnOnce.WARNING_MESSAGE);
		return true;
	}
	

}
