package PamguardMVC;


/**
 * Some constants to be used throughout PAMGuard
 * 
 * @author Doug Gillepsie
 */
public class PamConstants {
   
	/**
	 * The maximum number of allowed channels
	 */
	static public final int MAX_CHANNELS = 32;
	
	/**
	 * The number of meters in a mile. 
	 */
	static public final double METERS_PER_MILE = 1852.0;
	
	/**
	 * The maximum item character length allowed in the database
	 */
	static public final int MAX_ITEM_NAME_LENGTH = 50;
	
	/**
	 * The radius of the earth in meters.
	 */
	static public final int EARTH_RADIUS_METERS = 6371000;

   
	/**
	 * The maximum date PAMGuard supports. This prevents multi millenia dates in the future being used if 
	 * numbers in database or binary files have been corrupted. 
	 */
	public static final long MAX_DATE_TIME = 4102444800000L; //2100 

   
}
