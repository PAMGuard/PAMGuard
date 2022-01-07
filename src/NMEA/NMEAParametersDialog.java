/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package NMEA;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import serialComms.jserialcomm.PJSerialComm;
import NMEA.NMEAParameters.NmeaSources;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;

public class NMEAParametersDialog extends PamDialog implements ActionListener {
	
	private static NMEAParametersDialog singleInstance;
	private NMEAParameters nmeaParameters;
	private JTextField portTextField, groupTextField;
	private PamCheckBox multicastCheckBox;
	private ButtonGroup gpsRadioGroup;
	private JRadioButton udpNmeaGpsRadio;
	private JRadioButton serialNmeaGpsRadio;
	private JRadioButton simNmeaGpsRadio;
	private JLabel portSettingsLabel;
	public int[] bitsPerSecondList = {110, 300, 1200, 2400, 4800, 9600, 
		19200, 38400, 57600, 115200, 230400, 460800, 921600};
	private JComboBox<String> portComboBox = new JComboBox<String>();
	private JComboBox<Integer> bitsPerSecondComboBox = new JComboBox<Integer>();	

	public static NMEAParameters showDialog(Frame parentFrame, NMEAParameters nmeaParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new NMEAParametersDialog(parentFrame, nmeaParameters);
		}
		singleInstance.nmeaParameters = nmeaParameters.clone();
		
		singleInstance.SetParams(nmeaParameters);
		//singleInstance.
		singleInstance.setVisible(true);
		return singleInstance.nmeaParameters;
	}
	
	void SetParams(NMEAParameters nmeaParameters){
		// fill in the details in dialog fields
		//ystem.out.println("NMEAParametersDialog:nmeaParametersSetParameters " + nmeaParameters.port);
		portTextField.setText(String.format("%d", nmeaParameters.port));
		//simNmeaGpsRadio.setSelected(nmeaParameters.simThread);
		//populatePortList();
		populateComboBoxes();
		
		
		if(nmeaParameters.sourceType == NmeaSources.SERIAL){
			System.out.println("setting = choice == serial");
			serialNmeaGpsRadio.setSelected(true);
		}else if(nmeaParameters.sourceType == NmeaSources.UDP){
			System.out.println("setting = choice == UDP");
			udpNmeaGpsRadio.setSelected(true);
		}else{
			System.out.println("setting = choice == Sim");
			simNmeaGpsRadio.setSelected(true);
		}
		
		bitsPerSecondComboBox.setSelectedItem(nmeaParameters.serialPortBitsPerSecond);
//		ArrayList<CommPortIdentifier> commPortIds = SerialPortCom.getPortArrayList();
		String[] commPortIds = PJSerialComm.getSerialPortNames();
		for(int i = 0; i<commPortIds.length; i++){
			//System.out.println("portList " + i + ": " + portList.get(i) + " & serialPortName: " + nmeaParameters.serialPortName + "  " + nmeaParameters.simThread);
			if(commPortIds[i].equals(nmeaParameters.serialPortName)){
				portComboBox.setSelectedIndex(i);
			}
		}
		
		enableControls();
	}

	private JPanel udpPortSelection;
	private JPanel serialPortSelection;
	private JPanel simulatedPortSettings;
	private NMEAParametersDialog(Frame parentFrame, NMEAParameters nmeaParameters) {
		
		super(parentFrame, "NMEA Parameters", false);

		udpPortSelection = new JPanel();
		serialPortSelection = new JPanel();
		simulatedPortSettings = new JPanel();
		JPanel simSelection = new JPanel();
		JPanel outerSettingsPanel = new JPanel();
		outerSettingsPanel.setLayout(new BorderLayout());
		portSettingsLabel = new JLabel();
		portSettingsLabel.setText("UDP Port Number");

		serialPortSelection.setBorder(BorderFactory
				.createTitledBorder("Serial Settings"));
		udpPortSelection.setBorder(BorderFactory
				.createTitledBorder("UDP Settings"));
		simSelection.setBorder(BorderFactory
				.createTitledBorder("NMEA Source"));

		JPanel nmeaSettingsPanel = new JPanel();
		
//		nmeaSettingsPanel.setLayout(new BoxLayout(nmeaSettingsPanel, BoxLayout.Y_AXIS));
//		nmeaSettingsPanel.setLayout(new BorderLayout());
		nmeaSettingsPanel.setLayout(new BoxLayout(nmeaSettingsPanel, BoxLayout.Y_AXIS));

		udpPortSelection.setLayout(new GridLayout(3,2));
		udpPortSelection.add(portSettingsLabel);
		udpPortSelection.add(portTextField = new JTextField(4));
		udpPortSelection.add(new PamLabel("Multicast"));
		udpPortSelection.add(multicastCheckBox = new PamCheckBox());
		multicastCheckBox.addActionListener(this);
		udpPortSelection.add(new PamLabel("Group IP address"));
		udpPortSelection.add(groupTextField = new PamTextField(12));
		
		serialPortSelection.setLayout(new GridLayout(2,2));
		serialPortSelection.add(new JLabel("Port"));
		serialPortSelection.add(portComboBox);
		serialPortSelection.add(new JLabel("BAUD"));
		serialPortSelection.add(bitsPerSecondComboBox);

		gpsRadioGroup = new ButtonGroup(); // for logical association of rad buttons 
		//JRadioButton serialNmeaGpsRadio;
		gpsRadioGroup.add(serialNmeaGpsRadio = new JRadioButton("Serial NMEA data  "));
		gpsRadioGroup.add(simNmeaGpsRadio = new JRadioButton("Simulated NMEA data  "));
		gpsRadioGroup.add(udpNmeaGpsRadio = new JRadioButton("External NMEA server"));
		simNmeaGpsRadio.addActionListener(this);
		udpNmeaGpsRadio.addActionListener(this);
		serialNmeaGpsRadio.addActionListener(this);
		
		
//		simSelection.setLayout(new BoxLayout(simSelection, BoxLayout.Y_AXIS));
//		simSelection.add(serialNmeaGpsRadio);
//		simSelection.add(udpNmeaGpsRadio);
//		simSelection.add(simNmeaGpsRadio);
		
		simSelection.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(simSelection, serialNmeaGpsRadio, c);
		c.gridy++;
		addComponent(simSelection, udpNmeaGpsRadio, c);
		c.gridy++;
		addComponent(simSelection, simNmeaGpsRadio, c);
		c.gridy++;

		//serialNmeaGpsRadio.addActionListener(enableControlsListener);
		simulatedPortSettings.setBorder(new TitledBorder("Simulation Settings"));
		simulatedPortSettings.setLayout(new BorderLayout());
		JButton simButton = new JButton("Settings ...");
		simulatedPortSettings.add(BorderLayout.EAST, simButton);
		simButton.addActionListener(new SimButton());
		
		
		

//		nmeaSettingsPanel.add(BorderLayout.NORTH, simSelection);
//		nmeaSettingsPanel.add(BorderLayout.CENTER, serialSelection);
//		nmeaSettingsPanel.add(BorderLayout.SOUTH, portSelection);
		nmeaSettingsPanel.add(simSelection);
		nmeaSettingsPanel.add(serialPortSelection);
		nmeaSettingsPanel.add(udpPortSelection);
		nmeaSettingsPanel.add(simulatedPortSettings);
		
		setHelpPoint("mapping.NMEA.docs.configuringNMEADataSource");
		setDialogComponent(nmeaSettingsPanel);
	}
	
	class SimButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			NMEAParameters newParams = NMEASimDialog.showDialog(null, nmeaParameters);
			if (newParams != null) {
				nmeaParameters = newParams.clone();
			}
		}
		
	}

	@Override
	public boolean getParams() {
		try {
			if (serialNmeaGpsRadio.isSelected()) {
				nmeaParameters.sourceType = NmeaSources.SERIAL;
				if (portComboBox.getSelectedIndex() < 0) {
					return showWarning("Comm Port Error", "No Comm port selected");
				}
				nmeaParameters.serialPortName = portComboBox.getSelectedItem().toString();
				nmeaParameters.serialPortBitsPerSecond =bitsPerSecondList[bitsPerSecondComboBox.getSelectedIndex()];
			}
			else if (udpNmeaGpsRadio.isSelected()) {
				nmeaParameters.sourceType = NmeaSources.UDP;
				nmeaParameters.port = Integer.valueOf(portTextField.getText());
				nmeaParameters.multicast = multicastCheckBox.isSelected();
				if (multicastCheckBox.isSelected()) 
					nmeaParameters.multicastGroup = groupTextField.getText();
			}
			else if (simNmeaGpsRadio.isSelected()) {
				nmeaParameters.sourceType = NmeaSources.SIMULATED;
			}
			nmeaParameters.simThread = nmeaParameters.sourceType == NmeaSources.SIMULATED;
			
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
		
//	 This method returns the selected radio button in a button group
    public static JRadioButton getSelection(ButtonGroup group) {
    	int count = 0;
        for (Enumeration<?> e=group.getElements(); e.hasMoreElements(); ) {
            JRadioButton b = (JRadioButton)e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
        }
        return null;
    }
	
	@Override
	public void cancelButtonPressed() {
		nmeaParameters = null;
	}
	
	@Override
	public void restoreDefaultSettings() {
		SetParams(new NMEAParameters());
	}
	
	public void enableControls() {
		portTextField.setEnabled(udpNmeaGpsRadio.isSelected());
		portComboBox.setEnabled(serialNmeaGpsRadio.isSelected());
		bitsPerSecondComboBox.setEnabled(serialNmeaGpsRadio.isSelected());
		
		udpPortSelection.setVisible(udpNmeaGpsRadio.isSelected());
		serialPortSelection.setVisible(serialNmeaGpsRadio.isSelected());
		simulatedPortSettings.setVisible(simNmeaGpsRadio.isSelected());
		
		groupTextField.setEnabled(multicastCheckBox.isSelected());
		pack();
		
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		enableControls();
	}
	

	
	private void populateComboBoxes(){
		portComboBox.removeAllItems();
//		ArrayList<CommPortIdentifier> portIds = SerialPortCom.getPortArrayList();
		String[] portIds = PJSerialComm.getSerialPortNames();
		for(int i = 0; i<portIds.length; i++){
			portComboBox.addItem(portIds[i]);
		}
		bitsPerSecondComboBox.removeAllItems();
		for(int i = 0; i<bitsPerSecondList.length; i++){
			bitsPerSecondComboBox.addItem(bitsPerSecondList[i]);
		}
		
	}
	

}
