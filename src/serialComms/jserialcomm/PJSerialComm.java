package serialComms.jserialcomm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortTimeoutException;

import PamguardMVC.debug.Debug;
import serialComms.SerialPortParameters;

/**
 * PAMGuard wrapper around some of the jserialcomm functionality in order to make it slightly 
 * easier to use in the PAMGuard swing environment. 
 * 
 * http://fazecast.github.io/jSerialComm/
 * 
 * @author dg50
 *
 */
public class PJSerialComm {

	private static SerialPort[] portList;
	
	private static String[] portNames; 

	private SerialPort serialPort;
	
	private Thread listenThread;
	
	private LineListener lineListener;
	
	private ArrayList<PJSerialLineListener> lineListeners = new ArrayList<>();

	private CharListener charListener;
	
	private OutputStream outputStream;

	static {
		makePortList();
	}
	
	/**
	 * Create and open a serial port with default values of 8 data bits; 1 stop bit; no parity and 
	 * flow control disabled. 
	 * @param portName port name (e.g. COM1)
	 * @param baudRate baud rate
	 * @return valid port object. Never null (throws exception first)
	 * @throws PJSerialException exception if port unavailable or busy. 
	 */
	public static PJSerialComm openSerialPort(String portName, int baudRate) throws PJSerialException {
		return openSerialPort(portName, baudRate, 8, 1, SerialPort.NO_PARITY, SerialPort.FLOW_CONTROL_DISABLED);
	}
	
	/**
	 * Open a serial port with parameters defined in serialPortParameters
	 * @param serialPortParameters
	 * @return valid port object. Never null (throws exception first)
	 * @throws PJSerialException exception if port unavailable or busy. 
	 */
	public static PJSerialComm openSerialPort(SerialPortParameters serialPortParameters) throws PJSerialException {
		return openSerialPort(serialPortParameters.getCommPortName(), serialPortParameters.getBitsPerSecond(), 
				serialPortParameters.getDataBits(), serialPortParameters.getStopBits(), 
				serialPortParameters.getParity(), serialPortParameters.getFlowControl());
	}

	/**
	 * Create and open a serial port.
	 * @param portName port name (e.g. COM1)
	 * @param baudRate baud rate
	 * @param dataBits data bits
	 * @param stopBits stop bits
	 * @param parity parity
	 * @param flowControl flow control
	 * @return valid port object. Never null (throws exception first)
	 * @throws PJSerialException exception if port unavailable or busy. 
	 */
	public static PJSerialComm openSerialPort(String portName, int baudRate, int dataBits, int stopBits, int parity, int flowControl) throws PJSerialException {
		// reset the port list to force the library to query the current port configuration each time we try
		makePortList();

		if (portExists(portName) == false) {
			throw new PJNoPortException("Port " + portName + " does not exist");
		}
		SerialPort serialPort = null;
		try {
			serialPort = SerialPort.getCommPort(portName);
		}
		catch (SerialPortInvalidPortException e) {
			throw new PJNoPortException(e.getMessage());
		}
		if (serialPort == null) {
			throw new PJNoPortException("Port " + portName + " cannot be created");
		}
		serialPort.setBaudRate(baudRate);
		serialPort.setNumDataBits(dataBits);
		serialPort.setNumStopBits(stopBits);
		serialPort.setParity(parity);
		serialPort.setFlowControl(flowControl);
				
		boolean isOpen = serialPort.openPort();
		if (isOpen == false) {
			throw new PJPortBusyException("Port " + portName + " cannot be opened");
		}
		Debug.out.println("PJSerialComm: Port " + portName + " successfully opened");
		
		return new PJSerialComm(serialPort);
	}

	/**
	 * Private constructor which can only be called from the openSerialPort static functions
	 * once a port has been verified as existing and can be opened. 
	 * @param serialPort
	 */
	private PJSerialComm(SerialPort serialPort) {
		this.serialPort = serialPort;
	}
	
	public void addLineListener(PJSerialLineListener serialLineListener) {
		lineListeners.add(serialLineListener);
		// and see if we now also need to start the listening thread. 
		if (listenThread == null) {
			lineListener = new LineListener();
			listenThread = new Thread(lineListener);
			listenThread.start();
		}
	}

	/**
	 * Call through to underlying serial port to add a data listener. 
	 * @param serialPortDataListener
	 */
	public void addDataListener(SerialPortDataListener serialPortDataListener) {
		serialPort.addDataListener(serialPortDataListener);
	}
	
	
	public void addCharListener(PJSerialLineListener serialLineListener) {
		lineListeners.add(serialLineListener);
		// and see if we now also need to start the listening thread. 
		if (listenThread == null) {
			charListener = new CharListener();
			listenThread = new Thread(charListener);
			listenThread.start();
		}
	}
	
	/**
	 * Close the serial port and any associated data buffers. 
	 * @return true if any reading threads close properly. 
	 */
	public boolean closePort() {
		Debug.out.println("   PJSerialComm: attempting to close serial port...");
		boolean ok = serialPort.closePort();
		if (ok) {
			Debug.out.println("   PJSerialComm: Port closed");
		} else {
			Debug.out.println("   PJSerialComm: Port NOT closed");
		}
		if (listenThread == null) {
			return ok;
		}
		try {
			listenThread.join(200);
		} catch (InterruptedException e) {
			return false;
		}
		return ok;
	}
	
	/**
	 * Threaded listener for lines of data. 
	 * @author dg50
	 *
	 */
	private class LineListener implements Runnable {

		@Override
		public void run() {
			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 100);
			InputStream is = serialPort.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
			while (serialPort.isOpen()) {
				try {
					String aLine = bufferedReader.readLine();
					
					if (aLine == null) {
						break;
					}
					else {
						for (PJSerialLineListener sll:lineListeners) {
							sll.newLine(aLine);
						}
					}
				} 
				catch (SerialPortTimeoutException e) {
					/*
					 * Don't do anything here - it's normal if no data are arriving
					 */
				}
				catch (Exception e) { // other exceptions are much more interesting. 
					for (PJSerialLineListener sll:lineListeners) {
						sll.readException(e);
					}
				}
			}
			for (PJSerialLineListener sll:lineListeners) {
				sll.portClosed();
			}

		}

	}
	
	/**
	 * Threaded listener for characters (multi-lines) of data. 
	 * @author dg50
	 *
	 */
	private class CharListener implements Runnable {

		@Override
		public void run() {
			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 100);
			InputStream is = serialPort.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
			while (serialPort.isOpen()) {
				try {
					StringBuffer result = new StringBuffer();
					while (bufferedReader.ready()) {
						int oneChar = bufferedReader.read();
						result.append((char)oneChar);
					}
					String aLine = result.toString();
					
					if (aLine == null) {
						break;
					}
					else if (aLine.isEmpty()) {
						// empty string, just ignore
					}
					else {
						for (PJSerialLineListener sll:lineListeners) {
							sll.newLine(aLine);
						}
					}
				} 
				catch (SerialPortTimeoutException e) {
					/*
					 * Don't do anything here - it's normal if no data are arriving
					 */
				}
				catch (Exception e) { // other exceptions are much more interesting. 
					for (PJSerialLineListener sll:lineListeners) {
						sll.readException(e);
					}
				}
			}
			for (PJSerialLineListener sll:lineListeners) {
				sll.portClosed();
			}

		}

	}
		/**
	 * Check whether a port exists. 
	 * @param portName port name (.e.g 
	 * @return true if it exists
	 */
	public static boolean portExists(String portName) {
		if (portNames == null || portName == null)  {
			return false;
		}
		for (int i = 0; i < portNames.length; i++) {
			if (portNames[i].equals(portName)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Get a list of available comm ports in alphabetical order. 
	 * @return
	 */
	public static String[] getSerialPortNames() {
		if (portNames == null) {
			makePortList();
		}
		return portNames;
	}
	
	public static SerialPort[] getSerialPorts() {
		if (portList == null) {
			makePortList();
		}
		return portList;
	}
	
	private synchronized static boolean makePortList() {
		try {
			portList = SerialPort.getCommPorts();
		}
		catch (Exception e) {
			return false;
		}
		Arrays.sort(portList, new SerialPortComparitor());
		int nPort = portList.length;
		portNames = new String[nPort];
		for (int i = 0; i < nPort; i++) {
			portNames[i] = portList[i].getSystemPortName();
		}

		return true;
	}

	/**
	 * Comparator for sorting the serial ports into alphabetical order. 
	 * @author dg50
	 *
	 */
	private static class SerialPortComparitor implements Comparator<SerialPort>{

		@Override
		public int compare(SerialPort o1, SerialPort o2) {
			return o1.getSystemPortName().compareTo(o2.getSystemPortName());
		}
		
	}
	public OutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = serialPort.getOutputStream();
		}
		return outputStream;
	}
	
	/**
	 * Send the passed string to the output stream.  If checkCRLF is true, a \r\n is
	 * added to the end of the string if it does not already have that
	 * 
	 * @param string the text to send
	 * @param checkCRLF if true, append \n to string if needed
	 * 
	 * @throws IOException
	 */
	public void writeToPort(String string, boolean checkCRLF)  throws IOException {
		if (checkCRLF) {
			string = checkLineFeed(string);
		}
		writeToPort(string);
	}
	
	private String checkLineFeed(String outputString) {
		if (outputString == null || outputString.length() == 0) {
			return outputString;
		}
		char lastCh = outputString.charAt(outputString.length()-1);
		if (lastCh == '\n' || lastCh == '\r') {
			return outputString;
		}
		else {
			return outputString + "\r\n";
		}
	}

	/**
	 * Send the passed string to the output stream
	 *  
	 * @param string the text to send
	 * 
	 * @throws IOException
	 */
	public void writeToPort(String string) throws IOException {
		try {
			getOutputStream().write(string.getBytes());
		} catch (IOException e) {
			System.out.println("Unable to write to serial port " + serialPort.getSystemPortName());
//			System.out.println(e);
			throw e;
		}
	}
	
	public boolean checkPortStatus() {
		return serialPort.isOpen();
	}

	public static String getDefaultSerialPortName() {
		String defaultOsSerialPort;
		PamUtils.PlatformInfo.OSType currentOsType = PamUtils.PlatformInfo.calculateOS();
		switch (currentOsType) { 
        case WINDOWS: {
        	defaultOsSerialPort = "COM1"; 
        	break;
        }
        case MACOSX: {
        	// let's assume will be a Serial via USB as new Macs don't have an inbuilt serial port 
        	defaultOsSerialPort = "/dev/tty.usbserial";
        	break;
        }
        case LINUX: {
        	// linux let's also assume default will be that are using a USB port
    		defaultOsSerialPort = "/dev/ttyUSB0";
        	break;
        }
        default: {
        	System.out.println("Sorry, PAMGUARD doesn't know about a default serial port for your operating system");
			defaultOsSerialPort = "unknown";
			break;
        }
    }
//	System.out.println("defaultOsSerialPort="+defaultOsSerialPort);
	return defaultOsSerialPort;
}

	/**
	 * @return the serialPort
	 */
	public SerialPort getSerialPort() {
		return serialPort;
	}

	
}
