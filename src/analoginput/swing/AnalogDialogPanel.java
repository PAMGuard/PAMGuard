package analoginput.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogDeviceType;
import analoginput.AnalogDevicesManager;
import analoginput.AnalogInputParams;

public class AnalogDialogPanel implements PamDialogPanel {

	private AnalogDevicesManager analogDevicesManager;
	
	private PamDialog pamDialog;
	
	private JPanel mainPanel;
	
	private JComboBox<String> deviceType;

	private AnalogInputParams analogParams;

	private AnalogDeviceType selectedType;

	private PamDialogPanel selectedTypePanel;
	
	public AnalogDialogPanel(AnalogDevicesManager analogDevicesManager, PamDialog pamDialog) {
		this.analogDevicesManager = analogDevicesManager;
		this.pamDialog = pamDialog;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Input Device Settings"));
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.NORTH, new JLabel("Device type ..."));
		topPanel.add(BorderLayout.SOUTH, deviceType = new JComboBox<>());
		mainPanel.add(BorderLayout.NORTH, topPanel);
		deviceType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeDeviceType();
			}
		});
	}

	protected void changeDeviceType() {
		String dev = (String) deviceType.getSelectedItem();
		selectedType = analogDevicesManager.findDeviceType(dev);
		if (selectedTypePanel != null && selectedTypePanel.getDialogComponent() != null) {
			mainPanel.remove(selectedTypePanel.getDialogComponent());
		}
		if (selectedType == null) {
			return;
		}
		selectedTypePanel = selectedType.getDevicePanel();
		if (selectedTypePanel != null) {
			mainPanel.add(BorderLayout.CENTER, selectedTypePanel.getDialogComponent());
			selectedTypePanel.setParams();
			pamDialog.pack();
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		analogParams = analogDevicesManager.getAnalogInputParams();
		deviceType.removeAllItems();
		ArrayList<AnalogDeviceType> devices = analogDevicesManager.getAvailableTypes();
		for (AnalogDeviceType adt : devices) {
			deviceType.addItem(adt.getDeviceType());
		}
		try {
			if (analogParams.selectedType!=null) {
				deviceType.setSelectedItem(analogParams.selectedType);
			} else {
				deviceType.setSelectedIndex(0);	// if nothing has been selected, just use the first index
			}
		}
		catch (Exception e) {
			
		}
		changeDeviceType();
	}

	@Override
	public boolean getParams() {
		String dev = (String) deviceType.getSelectedItem();
		analogParams.selectedType = dev;
		if (dev == null) {
			return pamDialog.showWarning("No selected device");
		}
		if (selectedTypePanel != null) {
			return selectedTypePanel.getParams();
		}
		return dev != null;
	}

}
