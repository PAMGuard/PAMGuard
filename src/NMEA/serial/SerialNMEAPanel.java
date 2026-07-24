package NMEA.serial;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import NMEA.NMEAControl;
import NMEA.NMEAParameters;
import NMEA.NMEAParameters.NmeaSources;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import serialComms.jserialcomm.PJSerialComm;

public class SerialNMEAPanel implements PamDialogPanel {

	private SerialNMEAProvider serialNMEAProvider;

	private JPanel serialPortSelection;

	public int[] bitsPerSecondList = {110, 300, 1200, 2400, 4800, 9600, 
			19200, 38400, 57600, 115200, 230400, 460800, 921600};

	private JComboBox<String> portComboBox = new JComboBox<String>();
	private JComboBox<Integer> bitsPerSecondComboBox = new JComboBox<Integer>();	
	private JCheckBox autoComPort = new JCheckBox("Auto serial port");

	private NMEAControl nmeaControl;

	public SerialNMEAPanel(SerialNMEAProvider serialNMEAProvider) {
		this.serialNMEAProvider =serialNMEAProvider;
		nmeaControl = serialNMEAProvider.getNmeaControl();
		serialPortSelection = new JPanel();
		serialPortSelection.setBorder(BorderFactory
				.createTitledBorder("Serial Settings"));
		serialPortSelection.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridwidth = 2;
		serialPortSelection.add(autoComPort, c);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		serialPortSelection.add(new JLabel("Port", JLabel.RIGHT), c);
		c.gridx++;
		serialPortSelection.add(portComboBox, c);
		c.gridx = 0;
		c.gridy++;
		serialPortSelection.add(new JLabel("BAUD", JLabel.RIGHT), c);
		c.gridx++;
		serialPortSelection.add(bitsPerSecondComboBox, c);
		autoComPort.setToolTipText("Automatically search available serial ports for NMEA data");
		autoComPort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
	}

	protected void enableControls() {
		portComboBox.setEnabled(autoComPort.isSelected() == false);		
	}

	@Override
	public JComponent getDialogComponent() {
		return serialPortSelection;
	}

	@Override
	public void setParams() {

		NMEAParameters nmeaParameters = nmeaControl.getNmeaParameters();
		//simNmeaGpsRadio.setSelected(nmeaParameters.simThread);
		//populatePortList();
		populateComboBoxes();
		
		bitsPerSecondComboBox.setSelectedItem(nmeaParameters.serialPortBitsPerSecond);
//		ArrayList<CommPortIdentifier> commPortIds = SerialPortCom.getPortArrayList();
		String[] commPortIds = PJSerialComm.getSerialPortNames();
		for(int i = 0; i<commPortIds.length; i++){
			//System.out.println("portList " + i + ": " + portList.get(i) + " & serialPortName: " + nmeaParameters.serialPortName + "  " + nmeaParameters.simThread);
			if(commPortIds[i].equals(nmeaParameters.serialPortName)){
				portComboBox.setSelectedIndex(i);
			}
		}
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

	@Override
	public boolean getParams() {

		NMEAParameters nmeaParameters = nmeaControl.getNmeaParameters();

		if (portComboBox.getSelectedIndex() < 0) {
			PamDialog.showWarning(null, "Comm Port Error", "No Comm port selected");
		}
		nmeaParameters.autoSerialPort = autoComPort.isSelected();
		if (nmeaParameters.autoSerialPort == false) {
			nmeaParameters.serialPortName = portComboBox.getSelectedItem().toString();
		}
		nmeaParameters.serialPortBitsPerSecond =bitsPerSecondList[bitsPerSecondComboBox.getSelectedIndex()];

		return nmeaParameters.autoSerialPort | nmeaParameters.serialPortName != null;
	}

}
