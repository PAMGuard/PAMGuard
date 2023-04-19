package nmeaEmulator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import com.fazecast.jSerialComm.SerialPort;

import serialComms.SerialPortConstants;
import serialComms.jserialcomm.PJSerialComm;
import serialComms.jserialcomm.PJSerialException;
import NMEA.NMEAParameters;

public class SerialOutput {

	private NMEAParameters serialOutputParameters = new NMEAParameters();

	public NMEAParameters getSerialOutputParameters() {
		return serialOutputParameters;
	}

	public void setSerialOutputParameters(NMEAParameters serialOutputParameters) {
		this.serialOutputParameters = serialOutputParameters;
	}

//	private SerialOutputComm serialOutputComm;

	private BufferedWriter serialWriter;

	/** communication object */
	private PJSerialComm serialPort;


	
	public SerialOutput(String unitName) {
				
//		openSerialPort();
	}
	
	
	public boolean openSerialPort() {
		closeSerialPort();

		if (serialOutputParameters.serialPortName == null) {
			System.out.println("No COM Port selected");
			return false;
		}
		
		String[] ports = PJSerialComm.getSerialPortNames();
		boolean portFound = false;
		for (int i = 0; i < ports.length; i++) {
			if (ports[i].equals(serialOutputParameters.serialPortName)) {
				portFound = true;
				break;
			}
		}

		if (!portFound) {
			System.out.println("Unable to locate com port " + serialOutputParameters.serialPortName);
			return false;
		}
		
		try {
			serialPort = PJSerialComm.openSerialPort(serialOutputParameters.serialPortName,
					serialOutputParameters.serialPortBitsPerSecond,
					SerialPortConstants.DATABITS_8,
					SerialPort.ONE_STOP_BIT, 
					SerialPort.NO_PARITY,
					SerialPort.FLOW_CONTROL_DISABLED); // disable flow control, since this is the RXTX default and it's not specified above
		} catch (PJSerialException e) {
			System.out.println("PJSerialException in SerialOutput: " + e.getMessage());
			e.printStackTrace();
			return false;
		}

		
		try {
			serialWriter = new BufferedWriter(new OutputStreamWriter(serialPort.getOutputStream(), "ASCII"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		System.out.println(String.format("Serial port %s open for writing at %d bps",
				serialOutputParameters.serialPortName, serialOutputParameters.serialPortBitsPerSecond));
		return true;
	}
	
	public void closeSerialPort() {
		if (serialWriter != null) {
			try {
				serialWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			serialWriter = null;
		}
		if (serialPort != null) {
			serialPort.closePort();
			serialPort = null;
		}
	}
	
	/*
	 * Send a new string to the serial port. 
	 * A return and line feed will be added automatically
	 */
	public synchronized boolean sendSerialString(String string) {
		if (serialWriter == null) {
			return false;
		}
		try {
			serialWriter.write(string);
			serialWriter.write('\r');
			serialWriter.write('\n');
			serialWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

//	class SerialOutputComm implements SerialPortEventListener {
//
//		@Override
//		public void serialEvent(SerialPortEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}
	
//	class SerialOutFileMenuAction implements ActionListener {
//		SerialOutput serialOutput;
//		JFrame parentFrame;
//		public SerialOutFileMenuAction(SerialOutput serialOutput,
//				JFrame parentFrame) {
//			this.serialOutput = serialOutput;
//			this.parentFrame = parentFrame;
//		}
//
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			SerialOutputParameters newParams = SerialOutputdialog.showDialog(parentFrame, serialOutputParameters);
//			if (newParams != null) {
//				serialOutputParameters = newParams.clone();
//				openSerialPort();
//			}
//		}
//	}

}
