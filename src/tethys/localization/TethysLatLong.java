package tethys.localization;

import PamUtils.PamUtils;
import tethys.pamdata.AutoTethysProvider;

/**
 * Static formatting (rounding and range checking) functions for latlong values. 
 * @author dg50
 *
 */
public class TethysLatLong {

	/*
	 * A degree is 60 minutes and a minute is one mile = 1852m. 
	 * Therefore one metre of latitude is 9e-6 degrees. 
	 * One mm of latitude is therefore 9e-9 degrees. 
	 * GPS Accuracy is generally <3m, so 6dp generally OK for track data. however, 
	 * localisations around a small static array may want mm accuracy, so need 8dp. 
	 * For a double precision number, resolution at 360 degrees is 14dp
	 * 
	 */
	
	/**
	 * Number of decimal places for metre accuracy
	 */
	public static int metreDecimalPlaces = 6;
	
	/**
	 * Number of decimal places for mm accuracy. 
	 */
	public static int mmDecimalPlaces = 9;
	
	/**
	 * Check range of  a latitude value. 
	 * This function does absolutely nothing, but is here for completeness. 
	 * @param latitude
	 * @return
	 */
	public static Double formatLatitude(Double latitude) {
		if (latitude == null) {
			return null;
		}
		return latitude;
	}
	
	/**
	 * Check range and round a latitude value to a set number of decimal places. 
	 * @param latitude
	 * @param decimalPlaces
	 * @return
	 */
	public static Double formatLatitude(Double latitude, int decimalPlaces) {
		if (latitude == null) {
			return null;
		}
		return AutoTethysProvider.roundDecimalPlaces(latitude, decimalPlaces);
	}
	
	/**
	 * format a latitude to metre accuracy
	 * @param latitude
	 * @return
	 */
	public static Double formatLatitude_m(Double latitude) {
		return formatLatitude(latitude, metreDecimalPlaces);
	}
	
	/**
	 * format a latitude to mm accuracy
	 * @param latitude
	 * @return
	 */
	public static Double formatLatitude_mm(Double latitude) {
		return formatLatitude(latitude, mmDecimalPlaces);
	}
	
	/**
	 * Check range of a longitude value which must be between 0 and 360. 
	 * This function does absolutely nothing, but is here for completeness. 
	 * @param latitude
	 * @return
	 */
	public static Double formatLongitude(Double longitude) {
		if (longitude == null) {
			return null;
		}
		return PamUtils.constrainedAngle(longitude);
	}
	
	/**
	 * Check range and round a longitude value to a set number of decimal places. 
	 * @param latitude
	 * @param decimalPlaces
	 * @return
	 */
	public static Double formatLongitude(Double longitude, int decimalPlaces) {
		if (longitude == null) {
			return null;
		}
		longitude = formatLongitude(longitude); // constrain first.  
		return AutoTethysProvider.roundDecimalPlaces(longitude, decimalPlaces);
	}
	
	/**
	 * format a longitude value to m accuracy. 
	 * @param latitude
	 * @return
	 */
	public static Double formatLongitude_m(Double longitude) {
		return formatLongitude(longitude, metreDecimalPlaces);
	}
	
	/**
	 * format a longitude value to mm accuracy. 
	 * @param latitude
	 * @return
	 */
	public static Double formatLongitude_mm(Double longitude) {
		return formatLongitude(longitude, mmDecimalPlaces);
	}

}
