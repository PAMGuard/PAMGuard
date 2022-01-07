package PamUtils.time.ntp;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

public class NTPDialog extends PamDialog {

	private JTextField serverName;
	
	private JTextField updateInterval;

	private NTPTimeParameters ntpTimeParameters;
	
	private static NTPDialog singleInstance;
	
	private NTPDialog(Window parentFrame) {
		super(parentFrame, "NTP Server options", true);

		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("NTP Server options"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("NTP Server ", JLabel.RIGHT), c);
		c.gridx ++;
		c.gridwidth = 2;
		mainPanel.add(serverName = new JTextField(30), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Update interval ", JLabel.RIGHT), c);
		c.gridx ++;
		mainPanel.add(updateInterval = new JTextField(4), c);
		c.gridx++;
		mainPanel.add(new JLabel(" s ", JLabel.LEFT), c);
		
		setDialogComponent(mainPanel);
	}
	
	public static NTPTimeParameters showDialog(Window frame, NTPTimeParameters ntpTimeParameters) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new NTPDialog(frame);
		}
		singleInstance.setParams(ntpTimeParameters);
		singleInstance.setVisible(true);
		return singleInstance.ntpTimeParameters;
	}

	private void setParams(NTPTimeParameters ntpTimeParameters) {
		this.ntpTimeParameters = ntpTimeParameters.clone();
		serverName.setText(ntpTimeParameters.serverName);
		updateInterval.setText(String.format("%d", ntpTimeParameters.intervalSeconds));
	}

	@Override
	public boolean getParams() {
		ntpTimeParameters.serverName = serverName.getText();
		try {
			ntpTimeParameters.intervalSeconds = Integer.valueOf(updateInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Update interval must be a positive whole number");
		}
		if (ntpTimeParameters.intervalSeconds < NTPTimeParameters.MININTERVAL) {
			String message = String.format("Do not make overly frequency calls to the NTP server. \nUpdate intervals should be at least %d seconds",  NTPTimeParameters.MININTERVAL);
			int ans = WarnOnce.showWarning(getOwner(), "NTP Server updates", message, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		return checkServerAddress(ntpTimeParameters.serverName);
	}

	private boolean checkServerAddress(String serverName) {
		InetAddress inetAddress = null;
		try {
			 inetAddress = InetAddress.getByName(serverName);
		} catch (UnknownHostException e) {
			String message = String.format("NTP Server %s cannot be found", serverName);
			int ans = WarnOnce.showWarning(getOwner(), "NTP Server updates", message, WarnOnce.OK_CANCEL_OPTION);
			return (ans == WarnOnce.OK_OPTION);
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ntpTimeParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		setParams(new NTPTimeParameters());
	}

}
