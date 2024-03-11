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

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import serialComms.jserialcomm.PJSerialComm;

public class NMEAParameters implements Serializable, Cloneable, ManagedParameters {
	
	static public final long serialVersionUID = 2;
	
	String name;

	public int port;
	
	public boolean multicast;
	
	public String multicastGroup;

	public int channelMap ;

	public boolean simThread;
	
	/*
	 * Settings for simulating data
	 */
	public double simStartLatitude = 51.0;
	
	public double simStartLongitude = 1.5;
	
	public double simStartHeading = 232;
	
	public double simStartSpeed = 8; //knots
	
	public double simTimeInterval = 2; // seconds
	
	public double drunkenness = 0; // causes random course changes. 
	
	public boolean  continousChange = false; //change the heading by a continous amount based on 'drunkenness' value...
	
	private int latLongDecimalPlaces = 3; // decimal places for minutes in Lat Long data. 

	public boolean generateAIS;
	
	//value now gets set in the constructor CJB 2009-06-09
	public String serialPortName;
	
	public int serialPortBitsPerSecond = 4800;
	
	public String nmeaSource = "Sim";
	
	public int simHeadingData;
	
	public static final int SIM_HEADING_NONE = 0;
	public static final int SIM_HEADING_MAGNETIC = 1;
	public static final int SIM_HEADING_TRUE = 2;

	
	static public enum NmeaSources {
		SERIAL (0),
		UDP (1),
		SIMULATED (2), 
		MULTICAST (3);
		NmeaSources(int value){this.value= value;}
		private final int value;
		public int value(){return value;}
		public final int serialValue = 0;
		public final int udpValue = 1;
		public final int simulatedValue = 2;
		public final int multicastValue = 3;
	}
	
	public NmeaSources sourceType = NmeaSources.SIMULATED;

	public String getName() {
		return name;
	}
	
	public NMEAParameters() {	
		port = 8060;		// default
		channelMap = 1;		// default
		simThread = true;	// default
		serialPortName = PJSerialComm.getDefaultSerialPortName();
		name = "";
		serialPortBitsPerSecond = 4800;
		sourceType = NmeaSources.SIMULATED;
		multicast = false;
		multicastGroup = "230.0.0.0";
	}
	


	
	public void setName(String name) {
		this.name = name;
	}

	public static boolean isValidLength(int len) {
		int nBits = 0;
		for (int i = 0; i < 32; i++) {
			if ((len & 1 << i) > 0)
				nBits++;
		}
		return (nBits == 1);
	}

	@Override
	public NMEAParameters clone() {
		try {
			return (NMEAParameters) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public int getLatLongDecimalPlaces() {
		if (latLongDecimalPlaces <= 0) {
			latLongDecimalPlaces = 3;
		}
		return latLongDecimalPlaces;
	}

	public void setLatLongDecimalPlaces(int latLongDecimalPlaces) {
		this.latLongDecimalPlaces = latLongDecimalPlaces;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
