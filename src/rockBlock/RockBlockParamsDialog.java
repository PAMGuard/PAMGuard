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

package rockBlock;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import serialComms.SerialPortPanel;




public class RockBlockParamsDialog extends PamDialog {

	private static final long serialVersionUID = 1;
	private static RockBlockParamsDialog singleInstance;
	private RockBlockParams rockBlockParameters;
	private SerialPortPanel serialPanel;
	private JTextField commCheckTime;
	private JTextField sendDelayTime;
	

	
	public RockBlockParamsDialog(Window parentFrame) {
		super(parentFrame, "RockBlock+ Parameters", true);
		serialPanel = new SerialPortPanel("Serial Port", true, false, false, false, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.add(serialPanel.getPanel());
		JPanel componentsPanel = new JPanel();
		componentsPanel.setLayout(new BoxLayout(componentsPanel, BoxLayout.X_AXIS));
		componentsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        JLabel timingLbl = new JLabel("Check Signal Strength Every  ");
        commCheckTime = new JTextField(5);
        JLabel timingUnitsLbl = new JLabel("  s");
        componentsPanel.add(timingLbl);
        componentsPanel.add(commCheckTime);
        componentsPanel.add(timingUnitsLbl);
        mainPanel.add(componentsPanel);
        componentsPanel = new JPanel();
		componentsPanel.setLayout(new BoxLayout(componentsPanel, BoxLayout.X_AXIS));
		componentsPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        JLabel delayLbl = new JLabel("Wait ");
        sendDelayTime = new JTextField(5);
        JLabel delayUnitsLbl = new JLabel(" s before sending messages");
        componentsPanel.add(delayLbl);
        componentsPanel.add(sendDelayTime);
        componentsPanel.add(delayUnitsLbl);
        mainPanel.add(componentsPanel);
        

		setDialogComponent(mainPanel);
	}

	
	public static RockBlockParams showDialog(Frame parentFrame, RockBlockParams rockBlockParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new RockBlockParamsDialog(parentFrame);
		}
		singleInstance.rockBlockParameters = rockBlockParameters.clone();
		
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.rockBlockParameters;
	}
	
	/**
	 */
	private void setParams() {
		serialPanel.setPort(rockBlockParameters.getPortName());
		serialPanel.setBaudRate(rockBlockParameters.getBaud());
		commCheckTime.setText(String.format("%d",rockBlockParameters.getCommTiming()/1000));
		sendDelayTime.setText(String.format("%d",rockBlockParameters.getSendDelayMillis()/1000));
	}


	/**
	 * takes the values of the labels in the dialog box and sets the
     * roccaParameter object's fields
	 */
	@Override
	public boolean getParams() {
		rockBlockParameters.setPortName(serialPanel.getPort());
		rockBlockParameters.setBaud(serialPanel.getBaudRate());
		rockBlockParameters.setCommTimingSeconds(Integer.valueOf(commCheckTime.getText()));
		rockBlockParameters.setSendDelaySeconds(Integer.valueOf(sendDelayTime.getText()));
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
		rockBlockParameters = null;
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		rockBlockParameters = new RockBlockParams();
		setParams();
	}

}
