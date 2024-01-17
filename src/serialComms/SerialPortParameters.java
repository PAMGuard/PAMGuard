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

package serialComms;


import java.io.Serializable;
import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * @author David McLaren, Paul Redmond
 * 
 */

public class SerialPortParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private String commPortName; // name for the comm port

	private int bitsPerSecond = 4800; // bits per second

	private int dataBits = 8; // # of data bits (5,6,7 or8)

	private int parity = SerialPort.NO_PARITY; // parity setting (even,odd,none)

	private int stopBits = 1; // stop bits

	private int flowControl = SerialPort.FLOW_CONTROL_DISABLED;// flow control Xon/Xoff, hardware, none

	public SerialPortParameters() {

	}

	/**
	 * @param port
	 * @param bitsPerSecond
	 * @param parity
	 * @return true if connection sucessful
	 */
	public boolean connect(String port, int bitsPerSecond, int parity) {

		return false;
	}

	/**
	 * 
	 */
	public void disconnect() {

	}

	/**
	 * @param charToSend
	 */
	public void sendChar(char charToSend) {

	}

	public void sendString(String stringToSend) {

	}

	public char getChar() {

		return 'a';
	}

	public String getString() {

		return "1234";
	}

	public ArrayList<String> getPortList() {

		return new ArrayList<String>();
	}

	public int getBitsPerSecond() {
		return this.bitsPerSecond;
	}

	public void setBitsPerSecond(int bitsPerSecond) {
		this.bitsPerSecond = bitsPerSecond;
	}

	public String getCommPortName() {
		return this.commPortName;
	}

	public void setCommPortName(String commPortName) {
		this.commPortName = commPortName;
	}

	public int getDataBits() {
		return this.dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getParity() {
		return this.parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getStopBits() {
		return this.stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	@Override
	public SerialPortParameters clone() {
		try {
			return (SerialPortParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the flowControl
	 */
	public int getFlowControl() {
		return flowControl;
	}

	/**
	 * @param flowControl the flowControl to set
	 */
	public void setFlowControl(int flowControl) {
		this.flowControl = flowControl;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
