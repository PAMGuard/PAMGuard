package alarm.actions.serial;

//import gnu.io.SerialPort;

import java.io.Serializable;

import com.fazecast.jSerialComm.SerialPort;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import serialComms.SerialPortConstants;
import serialComms.jserialcomm.PJSerialComm;

public class AlarmSerialSettings  implements Serializable, Cloneable, ManagedParameters {


	public static final long serialVersionUID = 1L;
	
	public int stopBits = SerialPort.ONE_STOP_BIT;
	public int bitsPerSecond = 19200;
	public String portName = PJSerialComm.getDefaultSerialPortName();
	public int parity = SerialPort.NO_PARITY;
	public int dataBits = SerialPortConstants.DATABITS_8;
	public int flowControl = SerialPort.FLOW_CONTROL_DISABLED;
	
	public boolean printStrings = false;
	
	/**
	 * Flag to say always put a line feed on a string. 
	 */
	private Boolean makeLineFeed;
	
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected AlarmSerialSettings clone() {
		try {
			return (AlarmSerialSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * @return the makeLineFeed
	 */
	public boolean getMakeLineFeed() {
		if (makeLineFeed == null) {
			makeLineFeed = true;
		}
		return makeLineFeed;
	}



	/**
	 * @param makeLineFeed the makeLineFeed to set
	 */
	public void setMakeLineFeed(boolean makeLineFeed) {
		this.makeLineFeed = makeLineFeed;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
