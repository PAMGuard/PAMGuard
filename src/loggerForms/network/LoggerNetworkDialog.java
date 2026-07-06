package loggerForms.network;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;

public class LoggerNetworkDialog extends PamDialog {

	
	private static LoggerNetworkDialog singleInstance;
	
	private LoggerNetworkSettings settings;
	
	private JRadioButton none, mqtt, udp;
	
	private LoggerNetworkDialog(Window parentFrame) {
		super(parentFrame, "Logger Network", true);
		none = new JRadioButton("None");
		mqtt = new JRadioButton("MQTT");
		udp = new JRadioButton("UDP");
		ButtonGroup bg = new ButtonGroup();
		bg.add(none);
		bg.add(mqtt);
		bg.add(udp);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Network Options"));
		mainPanel.add(none, c);
		c.gridy++;
		mainPanel.add(mqtt, c);
		c.gridy++;
		mainPanel.add(udp, c);
		c.gridy++;
		
		setDialogComponent(new PamAlignmentPanel(mainPanel, BorderLayout.WEST, true));
	}
	
	public static LoggerNetworkSettings showDialog(Window frame, LoggerNetworkSettings settings) {
//		if (singleInstance == null) {
			singleInstance = new LoggerNetworkDialog(frame);
//		}
		singleInstance.setParams(settings);
		singleInstance.setVisible(true);
		return singleInstance.settings;
	}

	private void setParams(LoggerNetworkSettings settings) {
		this.settings = settings;
		none.setSelected(settings.loggerNetworkType == LoggerNetworkSettings.LOG_NET_NONE);
		mqtt.setSelected(settings.loggerNetworkType == LoggerNetworkSettings.LOG_NET_MQTT);
		udp.setSelected(settings.loggerNetworkType == LoggerNetworkSettings.LOG_NET_UDP);
	}

	@Override
	public boolean getParams() {
		if (none.isSelected()) {
			settings.loggerNetworkType = LoggerNetworkSettings.LOG_NET_NONE;
		}
		if (mqtt.isSelected()) {
			settings.loggerNetworkType = LoggerNetworkSettings.LOG_NET_MQTT;
		}
		if (udp.isSelected()) {
			settings.loggerNetworkType = LoggerNetworkSettings.LOG_NET_UDP;
		}
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		settings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new LoggerNetworkSettings());
	}

}
