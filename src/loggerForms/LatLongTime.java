/**
 * 
 */
package loggerForms;

import PamUtils.LatLong;

/**
 * @author GrahamWeatherup
 *
 */
public class LatLongTime extends LatLong{
		
	Long timeInMillis;
	
	/**
	 * dont like this method as lat and long are saved as primitive doubles so cant be null 
	 * @param latLong
	 * @param timeInMillis
	 */
//	public LatLongTime(LatLong latLong,Long timeInMillis){
//		this.latitude = latLong.getLatitude();
//		this.longitude = latLong.getLongitude();
//		this.timeInMillis=timeInMillis;
//	}
	
	/**
	 * 
	 * @param latLong
	 * @param timeInMillis
	 */
	public LatLongTime(double lat, double lon, Long timeInMillis){
		this.latitude = lat;
		this.longitude = lon;
		this.timeInMillis = timeInMillis;
	}
	
	
//	/**
//	 * @return the latLong
//	 */
//	public LatLong getLatLong() {
//		return new LatLong(latitude,longitude);
//	}

	/**
	 * @param latLong the latLong to set
	 */
	public void setLatLong(LatLong latLong) {
		this.latitude = latLong.getLatitude();
		this.longitude = latLong.getLongitude();
	}

	/**
	 * @return the timeInMillis
	 */
	public Long getTimeInMillis() {
		return timeInMillis;
	}

	/**
	 * @param timeInMillis the timeInMillis to set
	 */
	public void setTimeInMillis(Long timeInMillis) {
		this.timeInMillis = timeInMillis;
	}
	
	
}
