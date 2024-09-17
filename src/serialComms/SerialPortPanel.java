package serialComms;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.fazecast.jSerialComm.SerialPort;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import serialComms.jserialcomm.PJSerialComm;

/**
 * Standard panel for entering serial port settings. Should be 
 * combined into larger dialog panels. 
 * @author Douglas Gillespie
 *
 */
public class SerialPortPanel extends Object {

	private JPanel panel;
	
	private JComboBox<String> portList;
	
	private JComboBox<Integer> baudList;
	
	private JComboBox<Integer> bitsList;
	
	private JComboBox<String> stopBitsList;
	
	private JComboBox<String> parityList;
	
	private JComboBox<String> flowControlList;
	
	private boolean canBaud = false, canBits = false, canStopBits = false, canParity = false, canFlowControl = false;
	
	private String title;
		
	private int baudData[] = {110, 300, 1200, 2400, 4800, 9600, 
			19200, 38400, 57600, 115200, 230400, 460800, 921600}; 
	int dataBitData[] = {SerialPortConstants.DATABITS_5, SerialPortConstants.DATABITS_6, SerialPortConstants.DATABITS_7, SerialPortConstants.DATABITS_8};
	int stopBitData[] = {SerialPort.ONE_STOP_BIT, SerialPort.ONE_POINT_FIVE_STOP_BITS, SerialPort.TWO_STOP_BITS};
	String stopBitStrings[] = {"1", "1.5", "2"};
	int parityData[] = {SerialPort.NO_PARITY, SerialPort.ODD_PARITY, SerialPort.EVEN_PARITY, SerialPort.SPACE_PARITY, SerialPort.MARK_PARITY};
	String parityStrings[] = {"NONE", "ODD", "EVEN", "SPACE", "MARK"};
	int flowData[] = {SerialPort.FLOW_CONTROL_DISABLED, SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED, SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED,
			SerialPort.FLOW_CONTROL_RTS_ENABLED, SerialPort.FLOW_CONTROL_CTS_ENABLED};
	String flowStrings[] = {"NONE","XONXOFF on OUTPUT","XONXOFF on INPUT","RTS ENABLED","CTS ENABLED"};
	
	public SerialPortPanel() {
		createPanel();
	}
	
	public SerialPortPanel(String title) {
		this.title = title;
		createPanel();
	}

	public SerialPortPanel(String title, boolean canBaud, boolean canBits, boolean canStopBits, boolean canParity, boolean canFlowControl) {
		super();
		this.title = title;
		this.canBaud = canBaud;
		this.canBits = canBits;
		this.canStopBits = canStopBits;
		this.canParity = canParity;
		this.canFlowControl = canFlowControl;
		createPanel();
	}

	public JPanel getPanel() {
		return panel;
	}
	
	private void createPanel() {
		panel = new JPanel();
		if (title != null) {
			panel.setBorder(new TitledBorder(title));
		}
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
//		c.insets = new Insets(2,5,2,5);
		PamDialog.addComponent(panel, new JLabel("Port Id ", SwingConstants.RIGHT), c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		PamDialog.addComponent(panel, portList = new JComboBox<String>(), c);
		baudList = new JComboBox<Integer>();
		if (canBaud) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(panel, new JLabel("Bits per second (BAUD) ", SwingConstants.RIGHT), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			PamDialog.addComponent(panel, baudList, c);
		}
		bitsList = new JComboBox<Integer>();
		if (canBits) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(panel, new JLabel("Data Bits ", SwingConstants.RIGHT), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			PamDialog.addComponent(panel, bitsList, c);
		}
		parityList = new JComboBox<String>();
		if (canParity) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(panel, new JLabel("Parity ", SwingConstants.RIGHT), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			PamDialog.addComponent(panel, parityList, c);
		}
		stopBitsList = new JComboBox<String>();
		if (canStopBits) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(panel, new JLabel("Stop Bits ", SwingConstants.RIGHT), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			PamDialog.addComponent(panel, stopBitsList, c);
		}
		flowControlList = new JComboBox<String>();
		if (canFlowControl) {
			c.gridy++;
			c.gridx = 0;
			c.anchor = GridBagConstraints.EAST;
			PamDialog.addComponent(panel, new JLabel("Flow Control ", SwingConstants.RIGHT), c);
			c.gridx = 1;
			c.anchor = GridBagConstraints.WEST;
			PamDialog.addComponent(panel, flowControlList, c);
		}
		
		fillLists();
	}
	
	private void fillLists() {
		if (portList != null) {
			portList.removeAllItems();
//			ArrayList<CommPortIdentifier> portStrings = SerialPortCom.getPortArrayList();
			String[] portStrings = PJSerialComm.getSerialPortNames();
			for (int i = 0; i < portStrings.length; i++) {
				portList.addItem(portStrings[i]);
			}
		}
		if (baudList != null) {
			baudList.removeAllItems();
			for (int i = 0; i < baudData.length; i++) {
				baudList.addItem(new Integer(baudData[i]));
			}
		}
		if (bitsList != null) {
			bitsList.removeAllItems();
			for (int i = 0; i < dataBitData.length; i++) {
				bitsList.addItem(dataBitData[i]);
			}
		}
		if (stopBitsList != null) {
			stopBitsList.removeAllItems();
			for (int i = 0; i < stopBitData.length; i++) {
				stopBitsList.addItem(stopBitStrings[i]);
			}
		}
		if (parityList != null) {
			parityList.removeAllItems();
			for (int i = 0; i < parityStrings.length; i++) {
				parityList.addItem(parityStrings[i]);
			}
		}
		if (flowControlList != null) {
			flowControlList.removeAllItems();
			for (int i = 0; i < flowStrings.length; i++) {
				flowControlList.addItem(flowStrings[i]);
			}
		}
	}

	public int getBaudRate() {
		return baudData[baudList.getSelectedIndex()];
	}

	public void setBaudRate(int baudRate) {
		if (baudList == null) return;
		for (int i = 0; i < baudData.length; i++) {
		  if (baudData[i] == baudRate) {
			  baudList.setSelectedIndex(i);
		  }
		}
	}

	public int getDataBits() {
		return dataBitData[bitsList.getSelectedIndex()];
	}

	public void setDataBits(int dataBits) {
		if (bitsList == null) return;
		for (int i = 0; i < dataBitData.length; i++) {
			if (dataBitData[i] == dataBits) {
				bitsList.setSelectedIndex(i);
			}
		}
	}

	public int getFlowControl() {
		return flowData[flowControlList.getSelectedIndex()];
	}

	public void setFlowControl(int flowControl) {
		if (flowControlList == null) return;
		for (int i = 0; i < flowData.length; i++) {
			if (flowData[i] == flowControl) {
				flowControlList.setSelectedIndex(i);
			}
		}
	}

	public int getParity() {
		return parityData[parityList.getSelectedIndex()];
	}

	public void setParity(int parity) {
		if (parityList == null) return;
		for (int i = 0; i < parityData.length; i++) {
			if (parityData[i] == parity) {
				parityList.setSelectedIndex(i);
			}
		}
	}
	
	public String getPort() {
		if (portList == null) return null;
		Object selItem = portList.getSelectedItem();
		if (selItem == null) {
			return null;
		}
		return portList.getSelectedItem().toString();
	}

	public void setPort(String port) {
		if (portList == null) return;
		if (port == null) {
			return;
		}
		for (int i = 0; i < portList.getItemCount(); i++) {
			if (port.equalsIgnoreCase(portList.getItemAt(i).toString())) {
				portList.setSelectedIndex(i);
			}
		}
	}

	public int getStopBits() {
		return stopBitData[stopBitsList.getSelectedIndex()];
	}

	public void setStopBits(int stopBits) {
		for (int i = 0; i < stopBitData.length; i++) {
			if (stopBitData[i] == stopBits) {
				stopBitsList.setSelectedIndex(i);
			}
		}
	}

	public JComboBox<Integer> getBaudList() {
		return baudList;
	}

	public JComboBox<Integer> getBitsList() {
		return bitsList;
	}

	public JComboBox<String> getFlowControlList() {
		return flowControlList;
	}

	public JComboBox<String> getParityList() {
		return parityList;
	}

	public JComboBox<String> getPortList() {
		return portList;
	}

	public JComboBox<String> getStopBitsList() {
		return stopBitsList;
	}
	
	public void setParams(SerialPortParameters serialParams) {
		setPort(serialParams.getCommPortName());
		setBaudRate(serialParams.getBitsPerSecond());
		setStopBits(serialParams.getStopBits());
		setParity(serialParams.getParity());
		setDataBits(serialParams.getDataBits());
		setFlowControl(serialParams.getFlowControl());
	}

	public boolean getParams(SerialPortParameters serialParameters) {
		try {
			serialParameters.setCommPortName(getPort());
			serialParameters.setBitsPerSecond(getBaudRate());
			serialParameters.setStopBits(getStopBits());
			serialParameters.setParity(getParity());
			serialParameters.setDataBits(getDataBits());
			serialParameters.setFlowControl(getFlowControl());
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Get a list of available baud rates
	 * @return the baudData
	 */
	public int[] getAvailableBauds() {
		return baudData;
	}

	/**
	 * Set a list of available baud rates. 
	 * @param baudData the baudData to set
	 */
	public void setAvailableBauds(int[] baudData) {
		this.baudData = baudData;
		fillLists();
	}
	
}
