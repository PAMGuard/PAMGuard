/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package alarm.actions.email;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * @author mo55
 *
 */
public class SendEmailSettingsDialog extends PamDialog {

	private static final long serialVersionUID = 1L;
	private static SendEmailSettingsDialog singleInstance;
	private SendEmailSettings emailSettings;
	private SendEmailAction sendEmailAction;
	private JTextField host, port, username, toAddress, fromAddress;
	private JCheckBox sendOnAmber, sendOnRed;
	private JCheckBox attachScreenshot;

	/**
	 * @param parentFrame
	 * @param title
	 * @param hasDefault
	 */
	private SendEmailSettingsDialog(Window parentFrame, SendEmailAction sendEmailAction) {
		super(parentFrame, "Set Email Parameters", false);
		this.sendEmailAction = sendEmailAction;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(mainPanel, new JLabel("SMTP Host"), c);
		c.gridx++;
		addComponent(mainPanel, host = new JTextField(20), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("SMTP Port"), c);
		c.gridx++;
		addComponent(mainPanel, port = new JTextField(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Account Username"), c);
		c.gridx++;
		addComponent(mainPanel, username = new JTextField(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("To Address"), c);
		c.gridx++;
		addComponent(mainPanel, toAddress = new JTextField(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("From Address"), c);
		c.gridx++;
		addComponent(mainPanel, fromAddress = new JTextField(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Send Email on Amber Alarm"), c);
		c.gridx++;
		addComponent(mainPanel, sendOnAmber = new JCheckBox(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Send Email on Red Alarm"), c);
		c.gridx++;
		addComponent(mainPanel, sendOnRed = new JCheckBox(), c);
		c.gridx=0;
		c.gridy++;
		addComponent(mainPanel, new JLabel("Attach Screenshot to Email"), c);
		c.gridx++;
		addComponent(mainPanel, attachScreenshot = new JCheckBox(), c);
		setDialogComponent(mainPanel);
	}

	public static SendEmailSettings showDialog(Window parentFrame, SendEmailAction sendEmailAction) {
		if (singleInstance == null || parentFrame != singleInstance.getOwner() || sendEmailAction != singleInstance.sendEmailAction) {
			singleInstance = new SendEmailSettingsDialog(parentFrame, sendEmailAction);
		}
		singleInstance.emailSettings = sendEmailAction.getEmailSettings();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.emailSettings;
	}
	
	
	/**
	 * 
	 */
	private void setParams() {
		host.setText(emailSettings.getHost());
		port.setText(emailSettings.getPort());
		username.setText(emailSettings.getUsername());
		toAddress.setText(emailSettings.getToAddress());
		fromAddress.setText(emailSettings.getFromAddress());
		sendOnAmber.setSelected(emailSettings.isSendOnAmber());
		sendOnRed.setSelected(emailSettings.isSendOnRed());
		attachScreenshot.setSelected(emailSettings.isAttachScreenshot());
	}


	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		emailSettings.setHost(host.getText());
		emailSettings.setPort(port.getText());
		emailSettings.setUsername(username.getText());
		emailSettings.setToAddress(toAddress.getText());
		emailSettings.setFromAddress(fromAddress.getText());
		emailSettings.setSendOnAmber(sendOnAmber.isSelected());
		emailSettings.setSendOnRed(sendOnRed.isSelected());
		emailSettings.setAttachScreenshot(attachScreenshot.isSelected());
		return true;
	}

}
