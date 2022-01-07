package alarm.actions.serial;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import serialComms.SerialPortPanel;

public class AlarmSerialDialog extends PamDialog {

	private AlarmSerialSettings serialSettings;
	
	private static AlarmSerialDialog singleInstance;
	
	private SerialPortPanel serialPanel;
	
	private JCheckBox printStrings;
	
	private AlarmSerialDialog(Window parentFrame) {
		super(parentFrame, "Alarm Serial Output", true);
		serialPanel = new SerialPortPanel("Serial Port", true, true, true, false, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(serialPanel.getPanel());
		
		JPanel debugPanel = new JPanel(new BorderLayout());
		debugPanel.setBorder(new TitledBorder("Debug"));
		debugPanel.add(BorderLayout.CENTER, printStrings = new JCheckBox("Print serial strings to terminal"));
		
		mainPanel.add(debugPanel);
		
		setDialogComponent(mainPanel);
	}

	public static AlarmSerialSettings showDialog(Window window, AlarmSerialSettings alarmSerialSettings) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new AlarmSerialDialog(window);
		}
		singleInstance.serialSettings = alarmSerialSettings.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.serialSettings;
	}
	
	private void setParams() {
		serialPanel.setPort(serialSettings.portName);
		serialPanel.setBaudRate(serialSettings.bitsPerSecond);
		serialPanel.setStopBits(serialSettings.stopBits);
		serialPanel.setParity(serialSettings.parity);
		serialPanel.setDataBits(serialSettings.dataBits);
		serialPanel.setFlowControl(serialSettings.flowControl);
		
		printStrings.setSelected(serialSettings.printStrings);
	}

	@Override
	public boolean getParams() {
		serialSettings.portName = serialPanel.getPort();
		serialSettings.bitsPerSecond = serialPanel.getBaudRate();
		serialSettings.stopBits = serialPanel.getStopBits();
		serialSettings.parity = serialPanel.getParity();
		serialSettings.dataBits = serialPanel.getDataBits();
		serialSettings.flowControl = serialPanel.getFlowControl();
		
		serialSettings.printStrings = printStrings.isSelected();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		serialSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		serialSettings = new AlarmSerialSettings();
		setParams();
	}

}
