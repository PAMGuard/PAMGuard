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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Doug Gillespie, Paul Redmond, David McLaren
 * 
 */
public class FormatGpsData {

	String id;

	private double latitude; // decimal degrees

	private double longitude; // decimal degrees

	private double speed;

	private double heading;

	private double trueCourse;

	double variation;

	int time;

	int date;

	Date dateDate;

	Date dateO;

	FormatGpsData() {
	};

	FormatGpsData(StringBuffer nmeaString) {
		/*
		 * Unpack the string buffer to populate the above datas
		 */

	};

	public static String formatTime(int intTime) {
		SimpleDateFormat gpsTimeFormat = new SimpleDateFormat("HHmmss");
		// //System.out.println("FormatGpsData: formatTime" +
		// gpsTimeFormat.format(new Time((long)intTime)));
		// //System.out.println("FormatGpsData: intTime" + intTime);
		return (gpsTimeFormat.format(new Date(intTime)));
	}

	public static String formatDate(int intDate) {
		SimpleDateFormat gpsTimeFormat = new SimpleDateFormat("ddMMyy");
		return (gpsTimeFormat.format(intDate));
	}

	/*
	 * public String GpsDataToNmea() { SimpleDateFormat gpsDateFormat = new
	 * SimpleDateFormat("ddMMyy"); SimpleDateFormat gpsTimeFormat = new
	 * SimpleDateFormat("HHmmss");
	 * 
	 * StringBuffer nmea = new StringBuffer(); nmea.append(id + ",");
	 *  // 1: TimeStamp nmea.append(gpsTimeFormat.format(dateO) + ",");
	 * 
	 * 
	 *  // 3: Latitude String str; double absLat = Math.abs(latitude); double
	 * lat = Math.floor(absLat) * 100 + (absLat - Math.floor(absLat)) * 100; str =
	 * String.format("%.3f", lat); nmea.append(str + ",");
	 *  // 4: N North/South if(latitude<0.0){ nmea.append("S,"); } else {
	 * nmea.append("N,"); }
	 *  // 5: Longitude double absLong = Math.abs(longitude); double lon =
	 * Math.floor(absLong) * 100 + (absLong - Math.floor(absLong)) * 100; if
	 * (lon < 10000) { str = String.format("0%.3f", lon); } else { str =
	 * String.format("%.3f", lon); } nmea.append(str+",");
	 *  // 6: W East/West if(longitude<0.0){ nmea.append("W,"); } else {
	 * nmea.append("E,"); } // 7: Speed in knots nmea.append(speed + ",");
	 *  // 8: True course nmea.append(trueCourse + ",");
	 *  // 9: Date Stamp nmea.append(gpsDateFormat.format(dateO) + ",");
	 *  // 10: Variation nmea.append(000.0 + ",");
	 *  // 11: W East/West nmea.append("W");
	 *  // 12: checksum nmea.append("*70");
	 *  // //System.out.println("Fabricated String:" + nmea);
	 * return(nmea.toString()); }
	 * 
	 * public void printGpsValues() { //System.out.println("1: TimeStampTime: " +
	 * time); //System.out.println("3: Latitude: " + latitude);
	 * //System.out.println("Longitude: " + longitude);
	 *  }
	 * 
	 * 
	 * 
	 * public int getDate() { return date; } public double getHeading() { return
	 * heading; } public double getLatitude() { return latitude; } public double
	 * getLongitude() { return longitude; } public double getSpeed() { return
	 * speed; } public int getTime() { return time; } public double
	 * getTrueCourse() { return trueCourse; } public double getVariation() {
	 * return variation; } public void setLatitude(double latitude) {
	 * this.latitude = latitude; } public void setLongitude(double longitude) {
	 * this.longitude = longitude; } public void setSpeed(double speed) {
	 * this.speed = speed; } public void setTrueCourse(double trueCourse) {
	 * this.trueCourse = trueCourse; } public void setVariation(double
	 * variation) { this.variation = variation; }
	 * 
	 * 
	 */

}
