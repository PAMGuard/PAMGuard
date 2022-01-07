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
package GPS;

import geoMag.MagneticVariation;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import Array.sensors.ArraySensorDataUnit;
import Array.sensors.ArraySensorFieldType;
import pamMaths.PamQuaternion;
import pamMaths.PamVector;
import NMEA.AcquireNmeaData;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;

/**
 * Note on times for GPS data ...
 * <p>
 * The GpsDataUnit will contain time from the main PAMCalendar in line with the rest of PAMGuard. However 
 * the GpsData object within the data unit will contain the correct time, hopefully including milliseconds, 
 * unpacked from the GPS data itself. 
 * <p>
 * Position data extends LatLong and provides much of the functionality required for a position in the marine environment. 
 * <p>
 * Position data stores a latitude, longitude, height, heading, pitch and roll. These are the 6 essentially bits of information which allow for the position of an object
 * to be described with reference to the planet. All, some or none of these fields can be stored in this class.
 * <p>
 * PositionData also stores useful functions for predicting position and decoding position data e.g. NMEA.
 * <p>
 * <p>
 * For reference the Euler angle convention in PAMGUARD is as follows.
 * <p>
 * Heading- 0==north, 90==east 180=south, 270==west
 * <p>
 * Pitch- 90=-g, 0=0g, -90=g i.e. pointing downwards is -90, level is 0 and pointing upwards is 90
 * <p>
 * Tilt 0->180 -camera turning towards left to upside down 0->-180 camera turning right to upside down
 * <p>
 * All angles are in DEGREES. 
 *   
 * 
 * @author Doug Gillespie, Paul Redmond, David McLaren
 * 
 */
public class GpsData extends LatLong implements Cloneable, ManagedParameters, ArraySensorDataUnit {
	/*
	 * GpsData implements ArraySensorDataUnit, but GpsDatablock does NOT implement ArraySensorDataBlock 
	 * this is so that GpsData can be passed into the array geometry displays. 
	 */

	/**
	 * Don't ever change this or all configuration files will break !
	 */
	static final long serialVersionUID = -7919081673367069167L;

	//	Scanner parser;

	//	private double latitude; // decimal degrees

	//	private double longitude; // decimal degrees

	private double speed;

	private boolean dataOk = false;
	
	/**
	 * Fix type is supported by newer GPS units which have an additional three
	 * fields after the date - Magnetic variation, E/W indicator and 'Mode' = 
	 * A=Autonomous, D=DGPS, E=DR, N = Output Data Not Valid
	 * The GGA string has a similar position fix indicator: <br>
	 * 0 Fix not available or invalid<br>
1 GPS SPS Mode, fix valid<br>
2 Differential GPS, SPS Mode, fix valid<br>
3-5 Not supported<br>
6 Dead Reckoning Mode, fix valid<br>
	 */
	private String fixType;

	/**
	 * renamed from heading
	 */
	private double courseOverGround; 

	/**
	 * renamed from trueCourse
	 */
	private Double trueHeading;

	private Double magneticHeading;

	private Double magneticVariation;
	
	/**
	 * hydrophone pitch in degrees. 
	 */
	private Double pitch=0.0;
	
	/**
	 * hydrophone roll in degrees. 
	 */
	private Double roll=0.0;

	//	private double variation;

	private int time;

	private int date;

	//private String timeString;

	//Date dateO;

	private int day= 0;

	private int month = 0;

	private int year = 0;

	private int hours = 0;

	private int mins = 0;

	private int secs = 0;

	private int millis = 0;

	private long timeInMillis;

	private Calendar gpsCalendar;

	/**
	 * Can keep this since it only ever references one data (Bug #274)
	 */
	private static GpsData lastGlobalGpsData = null; 
//
	/*
	 * Had to get rid of this - it set up an unbroken chain of links between all GPS data 
	 * which created a memory leak - bug #274
	 */
//	private GpsData lastGpsData = null;

	private double distanceFromLast;

	public static final double METERSPERMILE = 1852.;

	private static final long millisPerHalfDay = 3600 * 1000 * 12;
	private static final long millisPerHour = 3600 * 1000;

	public GpsData() {

//		sortDistanceFromLast();

	};
	
	/**
	 * Constructor to create a GPSData object from a lat long. 
	 * @param latLong LatLong object. 
	 */
	public GpsData(LatLong latLong) {
		if (latLong != null) {
			this.latitude = latLong.getLatitude();
			this.longitude = latLong.getLongitude();
		}
	}
	
	/**
	 * Create a GPS data unit with a time and a lat long
	 * @param timeMillis time in milliseconds
	 * @param latLong LatLong object
	 */
	public GpsData(long timeMillis, LatLong latLong) {
		this(latLong);
		timeInMillis = timeMillis;
	}

	/**
	 * Constructor used in viewer and Mixed Mode 
	 * @param latitude latitude
	 * @param longitude longitude
	 * @param speed speed (knots)
	 * @param courseOverGround course over ground
	 * @param timeInMillis java millisecond time
	 * @param trueHeading true heading
	 * @param magneticHeading magnetic heading
	 * 
	 */
	public GpsData(double latitude, double longitude, double speed, 
			double courseOverGround, long timeInMillis, 
			Double trueHeading, Double magneticHeading) {
		super(latitude, longitude);
		this.speed = speed;
		this.courseOverGround = courseOverGround;
		this.timeInMillis = timeInMillis;
		this.trueHeading = trueHeading;
		this.magneticHeading = magneticHeading;
		dataOk = true;

		sortDistanceFromLast();
	}
	
	/**
	 * Used for buoy data received over the network. 
	 * @param latitude
	 * @param longitude
	 * @param timeInMillis
	 */
	public GpsData(double latitude, double longitude,  double height, long timeInMillis) {
		super(latitude, longitude, height);
		this.timeInMillis = timeInMillis;
		this.speed = 0;
		this.courseOverGround = 0;
		this.trueHeading = 0.;
		this.magneticHeading = 0.;
		dataOk = true;
	}
	
	/**
	 * 
	 * @param latitude- latitude 
	 * @param longitude-longitude
	 * @param height - height
	 * @param heading - true heading in DEGREES
	 * @param pitch - pitch in DEGREES
	 * @param roll - roll in DEGRESS
	 * @param timeInMillis time in millis.
	 */
	public GpsData(double latitude, double longitude,  double height, double heading, double pitch, double roll, long timeInMillis) {
		super(latitude, longitude, height);
		this.timeInMillis = timeInMillis;
		this.speed = 0;
		this.courseOverGround = 0;
		this.trueHeading = 0.;
		this.magneticHeading = 0.;
		this.trueHeading = heading;
		this.pitch = pitch;
		this.roll = roll;
		dataOk = true;
	}

	public GpsData(StringBuffer nmeaString, int stringType) {
		/*
		 * Unpack the string buffer to populate the above datas
		 */
		switch (stringType) {
		case GPSParameters.READ_GGA:
			unpackGGAString(nmeaString);
			break;
		default:
			unpackRMCString(nmeaString);
			break;
		}

		if (dataOk == false) return;

	};

	protected void sortDistanceFromLast() {

//		lastGpsData = lastGlobalGpsData;
//
		if (lastGlobalGpsData != null) {
			distanceFromLast = this.distanceToMetres(lastGlobalGpsData);
		}

		lastGlobalGpsData = this;
	}


	private static GpsData previousGgaGps = null;
	private void unpackGGAString(StringBuffer nmeaString) {

		char[] nmeaSentence = new char[nmeaString.length()];
		nmeaString.getChars(0, nmeaString.length(), nmeaSentence, 0);
		int delimeterCount = 0;
		int[] delimeters = new int[nmeaSentence.length];

		delimeterCount = 0;
		for (int i = 0; i < nmeaSentence.length; i++) {
			if (nmeaSentence[i] == ',') {
				delimeters[delimeterCount++] = i;
			}
		}
		if (delimeterCount < 13) {
			return;
		}

		// pick out time
		dataOk = (unpackTime(nmeaSentence, delimeters[0], delimeters[1]) &&
				unpackLatitude(nmeaSentence, delimeters[1], delimeters[2], delimeters[3]) &&
				unpackLongitude(nmeaSentence, delimeters[3], delimeters[4], delimeters[5]));

		gpsCalendar = Calendar.getInstance();
		gpsCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		long now; 
		gpsCalendar.setTimeInMillis(now = PamCalendar.getTimeInMillis());
		gpsCalendar.set(Calendar.HOUR_OF_DAY, hours);
		gpsCalendar.set(Calendar.MINUTE, mins);
		gpsCalendar.set(Calendar.SECOND, secs);
		gpsCalendar.set(Calendar.MILLISECOND, millis);
		timeInMillis = gpsCalendar.getTimeInMillis();

		// check it's not a day out as we roll past midnight. 
		if (timeInMillis - now > millisPerHalfDay) {
			timeInMillis -= (millisPerHalfDay * 2);
		}
		if (timeInMillis - now < -millisPerHalfDay) {
			timeInMillis += (millisPerHalfDay * 2);
		}

		String fixIndicator = new String(nmeaSentence, delimeters[5]+1, delimeters[6]-delimeters[5]-1);
		fixType = interpretGGAFixIndicator(fixIndicator);

		if (previousGgaGps != null) {
			double dist = previousGgaGps.distanceToMiles(this);
			double bear = previousGgaGps.bearingTo(this);
			speed = dist / (this.timeInMillis - previousGgaGps.timeInMillis) * millisPerHour;

			courseOverGround = bear;
		}
		previousGgaGps = this;
	}
	
	private String interpretGGAFixIndicator(String fixIndicator) {
		if (fixIndicator == null) {
			return null;
		}
		switch (fixIndicator) {
		case "0": // invalid
			return "N";
		case "1":
			return "A";
		case "2":
			return "D";
		case "6":
			return "E";
		}
		return null;
	}

	private boolean unpackTime(char[] nmeaSentence, int d1, int d2) {
		if (d2 - d1 < 7) {
			return false;
		}	

		time = (nmeaSentence[d1 + 1] - 48) * 100000
		+ (nmeaSentence[d1 + 2] - 48) * 10000
		+ (nmeaSentence[d1 + 3] - 48) * 1000
		+ (nmeaSentence[d1 + 4] - 48) * 100
		+ (nmeaSentence[d1 + 5] - 48) * 10
		+ (nmeaSentence[d1 + 6] - 48);

		hours = (nmeaSentence[d1 + 1] - 48) * 10
		+ (nmeaSentence[d1 + 2] - 48) * 1;

		mins = (nmeaSentence[d1 + 3] - 48) * 10
		+ (nmeaSentence[d1 + 4] - 48) * 1;

		secs = (nmeaSentence[d1 + 5] - 48) * 10
		+ (nmeaSentence[d1 + 6] - 48);

		int timeFac = 100;
		int dPos = d1+8;
		while (dPos < d2) {
			millis += (nmeaSentence[dPos]-48) * timeFac;
			timeFac/= 10;
			dPos++;
		}
//		if ((d2-d1) >= 10) {
//			millis = (nmeaSentence[d1 + 8] - 48) * 100
//			+ (nmeaSentence[d1 + 9] - 48) * 10;
//		}

		return true;
	}
	private boolean unpackLatitude(char[] nmeaSentence, int d1, int d2, int d3) {

		double degrees, minutes;
		double scaleFac = 10;
		degrees = ((nmeaSentence[d1 + 1] - 48) * 10. 
				+(nmeaSentence[d1 + 2] - 48));
		minutes = (nmeaSentence[d1 + 3] - 48) * 10.
		+ (nmeaSentence[d1 + 4] - 48);
		for (int i = d1+6; i < d2; i++) {
			minutes += (nmeaSentence[i] - 48) / scaleFac;
			scaleFac *= 10;
		}
		latitude = degrees + minutes / 60.;

		// If there is one character between commas 3 and 4, set
		// latitude North or South
		if ('S' == nmeaSentence[d2 + 1]) {
			latitude = latitude * -1;
		}

		return true;
	}
	private boolean unpackLongitude(char[] nmeaSentence, int d1, int d2, int d3) {

		double degrees, minutes;
		double scaleFac = 10;
		degrees = (nmeaSentence[d1 + 1] - 48) * 100. 
		+ (nmeaSentence[d1 + 2] - 48) * 10. 
		+(nmeaSentence[d1 + 3] - 48);
		minutes = (nmeaSentence[d1 + 4] - 48) * 10.
		+ (nmeaSentence[d1 + 5] - 48);
		for (int i = d1+7; i < d2; i++) {
			minutes += (nmeaSentence[i] - 48) / scaleFac;
			scaleFac *= 10;
		}
		longitude = degrees + minutes / 60.;

		if (true) { //(delimeters[6] - delimeters[5]) == 2) {
			if ('W' == nmeaSentence[d2 + 1]) {
				longitude = longitude * -1;
			}
		}
		return true;
	}
	private boolean unpackVariation(char[] nmeaSentence, int d1, int d2) {
		if (d1 - d2 >= 4) {
			magneticVariation = unpackFloat(nmeaSentence, d1, d2);
			return true;
		}
		magneticVariation = null;
		return false;		
	}

	/**
	 * Unpack a floating point number between two deliminators
	 * @param nmeaSentence nmea Sentence
	 * @param d1 position of first ,
	 * @param d2 position of second ,
	 * @return unpacked number
	 */
	private double unpackFloat(char[] nmeaSentence, int d1, int d2) {
		//		work from d1 + 1 to d2 -1 watching out for the decimal point. 
		//		in fact, start by searching for the decimal point so we know how
		//		much to scale numbers in front of it by
		double number = 0;
		int newDigit;
		boolean foundPoint = false;
		double scaleFac = 10;
		for (int i = d1 + 1; i < d2; i++) {
			if (nmeaSentence[i] == '.') {
				foundPoint = true;
				continue;
			}
			if (foundPoint == false) {
				newDigit = (nmeaSentence[i] - 48);
				number = (number * 10 + newDigit);
			}
			else {
				number += ((nmeaSentence[i] - 48) / scaleFac);
				scaleFac *= 10;
			}
		}
		return number;
	}

	private void unpackRMCString(StringBuffer nmeaString) {


		char[] nmeaSentence = new char[nmeaString.length()];
		nmeaString.getChars(0, nmeaString.length(), nmeaSentence, 0);

		char[] timeChars = new char[6];

		String id = nmeaString.substring(0, 6);

		int delimeterCount = 0;
		int[] delimeters = new int[nmeaSentence.length]; 

		delimeterCount = 0;
		for (int i = 0; i < nmeaSentence.length; i++) {
			if (nmeaSentence[i] == ',' || nmeaSentence[i] == '*') {
				delimeters[delimeterCount++] = i;
			}
		}
		if (delimeterCount < 11) {
			return;
		}

		gpsCalendar = Calendar.getInstance();

		// pick out time
		if ((delimeters[1] - delimeters[0]) >= 7) {
			time = (nmeaSentence[delimeters[0] + 1] - 48) * 100000
			+ (nmeaSentence[delimeters[0] + 2] - 48) * 10000
			+ (nmeaSentence[delimeters[0] + 3] - 48) * 1000
			+ (nmeaSentence[delimeters[0] + 4] - 48) * 100
			+ (nmeaSentence[delimeters[0] + 5] - 48) * 10
			+ (nmeaSentence[delimeters[0] + 6] - 48);

			hours = (nmeaSentence[delimeters[0] + 1] - 48) * 10
			+ (nmeaSentence[delimeters[0] + 2] - 48) * 1;

			mins = (nmeaSentence[delimeters[0] + 3] - 48) * 10
			+ (nmeaSentence[delimeters[0] + 4] - 48) * 1;

			secs = (nmeaSentence[delimeters[0] + 5] - 48) * 10
			+ (nmeaSentence[delimeters[0] + 6] - 48);

			int timeFac = 100;
			int dPos = delimeters[0]+8;
			while (dPos < delimeters[1]) {
				millis += (nmeaSentence[dPos]-48) * timeFac;
				timeFac/= 10;
				dPos++;
			}
//			if ((delimeters[1] - delimeters[0]) >= 10) {
//				millis = (nmeaSentence[delimeters[0] + 8] - 48) * 100
//				+ (nmeaSentence[delimeters[0] + 9] - 48) * 10;
//			}
		}

		// pick out latitude
		//		if (true) { //(delimeters[3] - delimeters[2]) == 9) {
		double minFac = 10;
		double minutes;
		unpackLatitude(nmeaSentence, delimeters[2], delimeters[3], delimeters[4]);

		unpackLongitude(nmeaSentence, delimeters[4], delimeters[5], delimeters[6]);


		// PR: SPEED -  This version handles extra decimal precision found in the darwin DGPS.
		// Needs further testing.
		//		if (true) { //(delimeters[7] - delimeters[6]) > 0) {

		//		int numDigits = delimeters[7] - delimeters[6];
		speed = unpackFloat(nmeaSentence, delimeters[6], delimeters[7]);

		courseOverGround = unpackFloat(nmeaSentence, delimeters[7], delimeters[8]);
		
		if (delimeterCount >= 13) {
			fixType = new String(nmeaSentence, delimeters[11]+1, delimeters[12]-delimeters[11]-1);
		}
		else {
			fixType = null;
		}

		//		int fpPosition = 0; 
		//		for (int i = 0; i< numDigits; i++) {
		//			if (nmeaSentence[delimeters[6] + i] == '.') {
		//				fpPosition = i;
		//			}
		//		}
		//
		//
		//		int decimalShift = 0;
		//		speed = 0.0;
		//		for (int i = 1; i< numDigits; i++) {
		//			if(i != fpPosition) {
		//
		//				decimalShift = (fpPosition - i);						
		//				if(i< fpPosition)
		//					decimalShift = decimalShift -1;
		//
		//				//System.out.println("decimal Shift: " + decimalShift);
		//				//System.out.print("value:" +nmeaSentence[delimeters[6] + i]);
		//				//System.out.println(", = " + ((nmeaSentence[delimeters[6] + i])-48) * (Math.pow(10, decimalShift)));
		//
		//				speed += ((nmeaSentence[delimeters[6] + i])-48) * (Math.pow(10.0, decimalShift));
		//			}
		//		}

		//System.out.println("gpsSpeed: " + speed);

		//		}

		//		// Darwin GPS
		//		// $GPRMC,084238,A,3519.490993,N,00636.062753,W,2.53,42,260306,,*11
		//		// PR: TRUE COURSE - REPLACES the version for speed but no time to check during Darwin cruise.
		//		// This version handles decimal place. But hasnt been tested with values after the dp. Suspect 
		//		// it might fail. Need to review all after Darwin anyway cause this is messy.
		////		if (true) { //(delimeters[8] - delimeters[7]) > 0) {
		//		numDigits = (delimeters[8] - delimeters[7])-1;
		//		fpPosition = numDigits+1; 
		//		for (int i = 0; i< numDigits; i++) {
		//			if (nmeaSentence[delimeters[7] + i] == '.') {
		//				fpPosition = i;
		//			}
		//		}
		//
		//		//	System.out.println("fpPosition: " + fpPosition);
		//		//	System.out.println("numDigits:" + numDigits);
		//		decimalShift = 0;
		//
		//		trueCourse = 0.0;
		//		decimalShift = fpPosition;	
		//		for (int i = 1; i <= numDigits; i++) {		
		//			if(i != fpPosition) {
		//				decimalShift--;// = (fpPosition - i);						
		//
		//				//System.out.println("decimal Shift: " + decimalShift);
		//				//System.out.print("value:" +nmeaSentence[delimeters[7] + i]);
		//				//System.out.println(", = " + ((nmeaSentence[delimeters[7] + i])-48) * (Math.pow(10, decimalShift)));
		//
		//				trueCourse += ((nmeaSentence[delimeters[7] + i])-48) * (Math.pow(10.0, decimalShift));
		//
		//			}
		//		}
		//		heading = trueCourse=trueCourse/10.0;


		magneticVariation = null;
		if (delimeters[10] - delimeters[9] >= 3) {
			unpackVariation(nmeaSentence, delimeters[9], delimeters[10]);
		}

		//		if (true) { // (delimeters[9] - delimeters[8]) == 7) {
		date = (nmeaSentence[delimeters[8] + 1] - 48) * 100000
		+ (nmeaSentence[delimeters[8] + 2] - 48) * 10000
		+ (nmeaSentence[delimeters[8] + 3] - 48) * 1000
		+ (nmeaSentence[delimeters[8] + 4] - 48) * 100
		+ (nmeaSentence[delimeters[8] + 5] - 48) * 10
		+ (nmeaSentence[delimeters[8] + 6] - 48);
		//	System.out.println("date: " + date);

		day = (nmeaSentence[delimeters[8] + 1] - 48) * 10
		+ (nmeaSentence[delimeters[8] + 2] - 48) * 1;

		month = (nmeaSentence[delimeters[8] + 3] - 48) * 10
		+ (nmeaSentence[delimeters[8] + 4] - 48) * 1 - 1;

		year = (nmeaSentence[delimeters[8] + 5] - 48) * 10
		+ (nmeaSentence[delimeters[8] + 6] - 48) + 2000;

		// //System.out.println("GpsData: day: " + day + " month: " +
		// month + " year:" + year);

		//		}

		gpsCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		gpsCalendar.set(year, month, day, hours, mins, secs);
		gpsCalendar.set(Calendar.MILLISECOND, millis);
		// gpsCalendar.set(5, 12, 8, 12, 32, 43);
		timeInMillis = gpsCalendar.getTimeInMillis();


		// //System.out.println(gpsCalendar.getTime());
		/**
		 * Note that some GPS models (Garmin eTrex put an 'S' at the end
		 * not an 'A', so this will fail ! Simulated data ok though with 
		 * GGA string, so won't fix. 
		 */
		dataOk = (nmeaSentence[delimeters[1]+1] == 'A');

	}

	// TODO change to return char[] ?
	/*
	 * eg3. $GPRMC,220516,A,5133.82,N,00042.24,W,173.8,231.8,130694,004.2,W*70 1
	 * 2 3 4 5 6 7 8 9 10 1112  1:220516 Time Stamp  2:A validity - A-ok,
	 * V-invalid  3:5133.82 current Latitude  4:N North/South  5:00042.24 current
	 * Longitude  6: W East/West  7:173.8 Speed in knots  8:231.8 True course 
	 * 9:130694 Date Stamp  10: 004.2 Variation  11:W East/West 12:*70 checksum
	 */
	public String gpsDataToRMC(int nDecPlaces) {
		gpsDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		gpsTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		// System.out.println
		StringBuffer nmea = new StringBuffer("$GPRMC,");

		// 0: ID
		//		nmea.append("GPRMC,");

		// 1: TimeStamp
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));

		nmea.append(gpsTimeFormat.format(c.getTime()) + ",");

		// 2: Validity: A:OK, V:Invalid
		nmea.append("A,");

		nmea.append(formatNMEALatitude(nDecPlaces) + ',');
		nmea.append(formatNMEALongitude(nDecPlaces) + ',');


		// 7: Speed in knots
		nmea.append(String.format("%05.1f,", speed ));

		// 8: True course
		if (speed > -10) {
			nmea.append(String.format("%05.1f,", courseOverGround));
		}
		else {
			double randomCourse = Math.random() * 360;
			nmea.append(String.format("%.1f,", randomCourse));
		}

		// 9: Date Stamp
		nmea.append(gpsDateFormat.format(c.getTime()) + ",");

		// 10: Variation
		nmea.append(000.0 + ",");

		// 11: W East/West
		nmea.append("W");

		// 12: checksum
		int checkSum = AcquireNmeaData.createStringChecksum(nmea);
		nmea.append(String.format("*%02X", checkSum));

		//System.out.println("Fabricated String:" + nmea);
		return (nmea.toString());
	}

	static SimpleDateFormat gpsDateFormat = new SimpleDateFormat("ddMMyy");
	static SimpleDateFormat gpsTimeFormat = new SimpleDateFormat("HHmmss.SSS");

	/**
	 * Get the time formatted in the simple ddmmyy way
	 * @return formatted time
	 */
	private String getGpsTimeString() {
		gpsTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		return gpsTimeFormat.format(c.getTime());
	}

	/**
	 * Get the date formatted in the simple ddmmyy way
	 * @return formatted date
	 */
	private String getGpsDateString() {
		gpsDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		c.setTimeZone(TimeZone.getTimeZone("GMT"));
		return gpsDateFormat.format(c.getTime());
	}

	/**
	 * Get the whole degrees
	 * @param latLong lat or long in decimal degrees
	 * @return integer degrees
	 */
	private int getWholeDegrees(double latLong) {
		return (int) Math.floor(Math.abs(latLong));
	}

	/**
	 * Get the whole number of degrees
	 * @param latLong
	 * @return
	 */
	private double getMinutes(double latLong) {
		latLong = Math.abs(latLong);
		return 60. * (latLong - getWholeDegrees(latLong));
	}

	/**
	 * 
	 * @return N or S
	 */
	private char getLatDirection() {
		if (latitude >= 0) {
			return 'N';
		}
		else {
			return 'S';
		}
	}

	/**
	 * 
	 * @return E or W
	 */
	private char getLongDirection() {
		if (longitude >= 0) {
			return 'E';
		}
		else {
			return 'W';
		}
	}

	/**
	 * formatted latitude string for simulated NMEA data. 
	 * @return formatted string
	 */
	private String formatNMEALatitude(int nDecPlaces) {
		// want something like "%02d%06.3f,%c"
		String formatString = String.format("%%02d%%0%d.%df,%%c", nDecPlaces+3, nDecPlaces);
		return String.format(formatString, getWholeDegrees(latitude), 
				getMinutes(latitude), getLatDirection());
	}

	private String formatNMEALongitude(int nDecPlaces) {
		// want something like "%03d%02.3f,%c"
		String formatString = String.format("%%03d%%0%d.%df,%%c", nDecPlaces+3, nDecPlaces);
		return String.format(formatString, getWholeDegrees(longitude), 
				getMinutes(longitude), getLongDirection());
	}


	public String gpsDataToGGA(int nDecPlaces) {

		// System.out.println
		StringBuffer nmea = new StringBuffer("$GPGGA,");

		nmea.append(getGpsTimeString() + ",");
		nmea.append(formatNMEALatitude(nDecPlaces) + ',');
		nmea.append(formatNMEALongitude(nDecPlaces) + ',');
		// fix quality
		nmea.append("8,");
		// n satellites
		nmea.append("00,");
		// horizontal dilution
		nmea.append("0.0,");
		// altitude, Meters, above mean sea level
		nmea.append("0.0,M,");
		// altitude, Height of geoid (mean sea level) above WGS84  ellipsoid
		nmea.append("0.0,M,");
		// (empty field) time in seconds since last DGPS update
		nmea.append(",");
		// (empty field) DGPS station ID number
		//		nmea.append(","); // don't need a ',' after the last field !

		// 12: checksum
		int checkSum = AcquireNmeaData.createStringChecksum(nmea);
		nmea.append(String.format("*%02X", checkSum));

		//System.out.println("Fabricated String:" + nmea);
		return (nmea.toString());
	}

	public void printGpsValues() {
		// System.out.println("1: TimeStampTime: " + time);
		// System.out.println("3: Latitude: " + latitude);
		// System.out.println("Longitude: " + longitude);

	}

	public long getTimeInMillis() {
		return timeInMillis;
	}

	public Calendar getGpsCalendar() {
		return gpsCalendar;
	}

	public int getDate() {
		return date;
	}

	/**
	 * Gets the best available data on the vessels true heading. If true heading
	 * data is available (e.g. from a Gyro compass), then that is returned. If 
	 * true heading is not available, then attempt to use magnetic heading (e.g. 
	 * from a fluxgate compass, which should be automatically corrected for magnetic 
	 * variation. Finally, if neither true or magnetic heading data are available, 
	 * just return course over ground from the GPS. Note that in this last case, the 
	 * data may be inaccurate at low speeds or in a cross current. 
	 * @return Best Heading in degrees relative to true North. 
	 */
	public double getHeading() {
		Double h = getHeading(true);
		if (h == null) {
			return courseOverGround;
		}
		else {
			return h;
		}
//		if (trueHeading != null && !Double.isNaN(trueHeading)) {
//			return trueHeading;
//		}
//		else if (magneticHeading != null && !Double.isNaN(magneticHeading)) {
//			if (magneticVariation != null) {
//				return (magneticHeading + magneticVariation); 
//			}
//			else {
//				return magneticHeading;
//			}
//		}
//		else {
//			return courseOverGround;
//		}
	}
	/**
	 * Gets the best available data on the vessels true heading. If true heading
	 * data is available (e.g. from a Gyro compass), then that is returned. If true
	 * heading is not available, then attempt to use magnetic heading (e.g. from a
	 * fluxgate compass, which should be automatically corrected for magnetic
	 * variation. Finally, if neither true or magnetic heading data are available,
	 * this function will either return course over ground from the GPS of null
	 * depending on the value of the noGPSCourse parameter. Note that in this last
	 * case, the data may be inaccurate at low speeds or in a cross current.
	 * 
	 * NB: Some modules (e.g. DIFAR, and maybe others?) create and allow for 
	 * Headings on the interval of (-180, 180).
	 * 
	 * @param noGPSCourse
	 *            set true if you only want courses from proper heading sensors. If
	 *            noGPSSource is true and gyro or fluxgate data are unavailable null
	 *            will be returned.
	 * 
	 * @return heading
	 */
	public Double getHeading(boolean noGPSCourse) {
//		if (trueHeading != null) {
		if (trueHeading != null && trueHeading>=-360 && trueHeading<=360) {
			return trueHeading;
		}
//		else if (magneticHeading != null) {
		else if (magneticHeading != null && magneticHeading>=-180 && magneticHeading<=360) {
			if (magneticVariation != null) {
				return (magneticHeading + magneticVariation); 
			}
			else {
				return magneticHeading;
			}
		}
		if (noGPSCourse) {
			return null;
		}
		else {
			return courseOverGround;
		}
	}

	/**
	 * @return  true heading read from a gyro or similar device. 
	 */
	public Double getTrueHeading() {
		return trueHeading;
	}

	/**
	 * @param trueHeading the trueHeading to set
	 */
	public void setTrueHeading(Double trueHeading) {
		this.trueHeading = trueHeading;
	}

	/**
	 * @param magneticHeading the magneticHeading to set
	 */
	public void setMagneticHeading(Double magneticHeading) {
		this.magneticHeading = magneticHeading;
		if (magneticVariation == null) {
			magneticVariation = MagneticVariation.getInstance().getVariation(this);
		}
	}

	/**
	 * 
	 * @return the magnetic heading (read from a fluxgate compass)
	 */
	public Double getMagneticHeading() {
		return magneticHeading;
	}

	/**
	 * Return the course over ground from the GPS. Note that this is the 
	 * direction the vessel is moving in relative to the earth and may not
	 * be the direction the vessel is pointing in. 
	 * @return the course over ground.
	 */
	public double getCourseOverGround() {
		return courseOverGround;
	}

	/**
	 * @param courseOverGround the courseOverGround to set
	 */
	public void setCourseOverGround(double courseOverGround) {
		this.courseOverGround = courseOverGround;
	}

	/**
	 * Returns the speed in knots
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Gets the speed in metres per second
	 * @return speed in metres per second
	 */
	public double getSpeedMetric() {
		return speed * METERSPERMILE / 3600;
	}

	public int getTime() {
		return time;
	}

	//	public double getTrueCourse() {
	//		return trueCourse;
	//	}


	//	public void setLatitude(double latitude) {
	//	this.latitude = latitude;
	//	}

	//	public void setLongitude(double longitude) {
	//	this.longitude = longitude;
	//	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	//
	//	public void setTrueCourse(double trueCourse) {
	//		this.trueCourse = trueCourse;
	//	}


	//	public int getDay() {
	//	return day;
	//	}

	//	public void setDay(int day) {
	//	this.day = day;
	//	}

	/**
	 * Get hours from NMEA date, not system time
	 * @return hours from NMEA data. 
	 */
	public int getHours() {
		return hours;
	}

	//	public void setHours(int hours) {
	//	this.hours = hours;
	//	}

	/**
	 * @return the magneticVariation
	 */
	public Double getMagneticVariation() {
		return magneticVariation;
	}

	/**
	 * @param magneticVariation the magneticVariation to set
	 */
	public void setMagneticVariation(Double magneticVariation) {
		this.magneticVariation = magneticVariation;
	}

	/**
	 * Get minutes from NMEA date, not system time
	 * @return mins from NMEA data. 
	 */
	public int getMins() {
		return mins;
	}

	//	public void setMins(int mins) {
	//	this.mins = mins;
	//	}

	//	public int getMonth() {
	//	return month;
	//	}

	//	public void setMonth(int month) {
	//	this.month = month;
	//	}

	/**
	 * Get seconds from NMEA date, not system time
	 * @return seconds from NMEA data. 
	 */
	public int getSecs() {
		return secs;
	}
	
	/**
	 * Get seconds from NMEA date, not system time
	 * @return seconds from NMEA data. 
	 */
	public int getMillis() {
		return millis;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public void setGpsCalendar(Calendar gpsCalendar) {
		this.gpsCalendar = gpsCalendar;
	}

	//	public void setHeading(double heading) {
	//		this.heading = heading;
	//	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setTimeInMillis(long timeInMillis) {
		this.timeInMillis = timeInMillis;
	}

	public double getDistanceFromLast() {
		return distanceFromLast;
	}

	public boolean isDataOk() {
		return dataOk;
	}

	public void setDataOk(boolean dataOk) {
		this.dataOk = dataOk;
	}

	/*
	 * Predict where the gps position will be at a given time
	 * based on the current speed and heading. 
	 */
	public GpsData getPredictedGPSData(long predictionTime) {
		GpsData predictedData = this.clone();
		if (getCourseOverGround() > 360) {
			// this happens with a lot of AIS data that does not have heading info.
			return predictedData;
		}
		double dt = (predictionTime - getTimeInMillis()) / 1000. / 3600;
		LatLong newPos = this.TravelDistanceMiles(getCourseOverGround(), getSpeed() * dt);
		predictedData.latitude = newPos.getLatitude();
		predictedData.longitude = newPos.getLongitude();
		predictedData.timeInMillis = predictionTime;
		return predictedData;
	}
	
	/**
	 * 
	 * Predict where the gps position will be at a given time
	 * based on the current speed and heading, but interpolating to some maximum 
	 * amount. 
	 * @param predictionTime time of prediction
	 * @param maxInterpMillis max milliseconds to interpolate. 
	 * @return Interpolated GPS position. 
	 */
	public GpsData getPredictedGPSData(long predictionTime, long maxInterpMillis) {
		GpsData predictedData = this.clone();
		if (getCourseOverGround() > 360) {
			// this happens with a lot of AIS data that does not have heading info.
			return predictedData;
		}
//		System.out.println(String.format("Predict GPS data from %s to %s", 
//				PamCalendar.formatDateTime(this.getTimeInMillis()), PamCalendar.formatDateTime(predictionTime)));
//		time difference in milliseconds
		double dt = (predictionTime - getTimeInMillis());
		if (dt > maxInterpMillis) {
			dt = maxInterpMillis;
		}
		if (dt < -maxInterpMillis) {
			dt = -maxInterpMillis;
		}
//		time difference in hours
		dt /= (1000. * 3600.);
		
		LatLong newPos = this.TravelDistanceMiles(getCourseOverGround(), getSpeed() * dt);
		predictedData.latitude = newPos.getLatitude();
		predictedData.longitude = newPos.getLongitude();
		predictedData.timeInMillis = predictionTime;
		return predictedData;
	}

	@Override
	public GpsData clone() {
		return (GpsData) super.clone();
	}

	/**
	 * Hydrophone pitch in degrees, +ve means that the front 
	 * of the hydrophone is up, -ve is down. Values will be between -90 and + 90. 
	 * @return the pitch in degrees. 
	 */
	public Double getPitchD() {
		return pitch;
	}
	
	/**
	 * Get the pitch value, returning 0 if it's null. 
	 * @return pitch in degrees
	 */
	public double getPitch() {
		if (pitch == null) {
			return 0;
		}
		return pitch;
	}

	/**
	 * Hydrophone pitch in degrees, +ve means that the front 
	 * of the hydrophone is up, -ve is down. Values will be between -90 and + 90. 
	 * @param pitch the pitch to set
	 */
	public void setPitch(Double pitch) {
		this.pitch = pitch;
	}

	/**
	 * Hydrophone roll in degrees, +ve is roll to the left, -ve to the right. 
	 * @return the roll
	 */
	public Double getRollD() {
		return roll;
	}
	
	/**
	 * 
	 * @return the roll and 0 if it's null. 
	 */
	public double getRoll() {
		if (roll == null) {
			return 0;
		}
		return roll;
	}

	/**
	 * Hydrophone roll in degrees, +ve is roll to the left, -ve to the right.
	 * @param roll the roll to set
	 */
	public void setRoll(Double roll) {
		this.roll = roll;
	}
	
	/**
	 * Get the Euler angles which describe the rotation of this position. m
	 * @return Euler angles [heading, pitch, roll] in RADIANS. See class description for angle conventions
	 */
	public double[] getEulerAngles() {
		double[] eulerAngle={Math.toRadians(trueHeading), Math.toRadians(pitch), Math.toRadians(roll)};
		return eulerAngle;
	}
	
	/**
	 * Get the quaternion which describe the rotation of this position. m
	 * @return quaternion. 
	 */
	public PamQuaternion getQuaternion(){
		PamQuaternion quaternion=new PamQuaternion(Math.toRadians(getHeading()), Math.toRadians(getPitch()), Math.toRadians(getRoll()));
		return quaternion;
	}
		/**
	 * Set the Euler angles which describe the rotation of this position. m
	 * @param angles-Euler angles [heading, pitch, roll] in Degrees. See class description for angle conventions
	 */
	public void setEulerAngles(double[] angles) {
		trueHeading=angles[0];
		pitch=angles[1];
		roll=angles[2];
	}

	public static GpsData getAverage(GpsData gpsData1, GpsData gpsData2) {
		if (gpsData1 == null) {
			return gpsData2;
		}
		if (gpsData2 == null) {
			return gpsData1;
		}
		GpsData newData = new GpsData();
		// need to be careful averaging all the angles !
		// however it's very unlikely we'll get points either side of a pole or 
		// 180 degreed E, so no need to be too careful !
		newData.latitude = (gpsData1.latitude + gpsData2.latitude)/2.;
		newData.longitude = (gpsData1.longitude + gpsData2.longitude)/2.;
		newData.height = (gpsData1.height + gpsData2.height)/2.;
		newData.timeInMillis = (gpsData1.timeInMillis + gpsData2.timeInMillis)/2;
		newData.courseOverGround = (gpsData1.courseOverGround + gpsData2.courseOverGround)/2.;
		newData.speed = (gpsData1.speed + gpsData2.speed)/2.;
		newData.trueHeading = PamUtils.doubleAverage(gpsData1.trueHeading, gpsData2.trueHeading);
		newData.magneticHeading = PamUtils.doubleAverage(gpsData1.magneticHeading, gpsData2.magneticHeading);
		newData.magneticVariation = PamUtils.doubleAverage(gpsData1.magneticVariation, gpsData2.magneticVariation);
		newData.pitch = PamUtils.doubleAverage(gpsData1.pitch, gpsData2.pitch);
		newData.roll = PamUtils.doubleAverage(gpsData1.roll, gpsData2.roll);
		
		return newData;
	}

	public String toFullString() {
		return String.format("%s, %s, Depth %3.1f, Head %3.1f, Pitch %3.1f, Roll %3.1f", 
				formatLatitude(), formatLongitude(), -getHeight(), getHeading(), getPitch(), getRoll());
	}

	public String summaryString() {
		return String.format("<html>%s, %s<br>Depth %3.1f<br> Head %3.1f, Pitch %3.1f, Roll %3.1f<br>", 
				formatLatitude(), formatLongitude(), -getHeight(), getHeading(), getPitch(), getRoll());
	}

	/**
	 * @return the fixType
	 */
	public String getFixType() {
		return fixType;
	}

	/**
	 * @param fixType the fixType to set
	 */
	public void setFixType(String fixType) {
		this.fixType = fixType;
	}
	
	/**
	 * Set a numeric value in a gps data orientation field. 
	 * @param fieldType
	 * @param val
	 * @return
	 */
	public boolean setOrientationField(ArraySensorFieldType fieldType, Double val) {
		switch (fieldType) {
		case HEADING:
			setTrueHeading(val);
			break;
		case HEIGHT:
			if (val != null) {
				setHeight(val);
			}
			break;
		case PITCH:
			setPitch(val);
			break;
		case ROLL:
			setRoll(val);
			break;
		default:
			return false;
		}
		return true;		
	}
	/**
	 * Get a longer string to describe the fix type. 
	 * This will be one of A=Autonomous, D=DGPS, E=DR, N = Output Data Not Valid
	 * @param fixType
	 * @return longer descriptive string
	 */
	public static String getLongFixType(String fixType) {
		if (fixType == null) {
			return null;
		}
		switch (fixType) {
		case "A":
			return "Autonomous";
		case "D":
			return "Differential";
		case "E":
			return "Dead Reckon";
		case "N":
			return "Invalid";
		}
		return "Unknown";
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		try {
			Field field = this.getClass().getDeclaredField("month");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return month;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("year");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return year;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	@Override
	public Double getField(int streamer, ArraySensorFieldType fieldtype) {
		switch (fieldtype) {
		case HEADING:
			return getHeading();
		case HEIGHT:
			return getHeight();
		case PITCH:
			return getPitchD();
		case ROLL:
			return getRollD();
		default:
			break;
		
		}
		return null;
	}

	@Override
	public long getTimeMilliseconds() {
		return getTimeInMillis();
	}

	@Override
	public GpsData addDistanceMeters(double addX, double addY, double addZ) {
		return addDistanceMeters(super.addDistanceMeters(addX, addY, addZ));
	}

	@Override
	public GpsData addDistanceMeters(double addX, double addY) {
		return addDistanceMeters(super.addDistanceMeters(addX, addY));
	}

	@Override
	public GpsData addDistanceMeters(PamVector movementVector) {
		return addDistanceMeters(super.addDistanceMeters(movementVector));
	}
	
	private GpsData addDistanceMeters(LatLong updatedLatLong) {
		GpsData newGps = this.clone();
		newGps.latitude = updatedLatLong.getLatitude();
		newGps.longitude = updatedLatLong.getLongitude();
		newGps.height = updatedLatLong.getHeight();
		return newGps;
	}


}
