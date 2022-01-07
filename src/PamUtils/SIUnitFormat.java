package PamUtils;

/**
 * Class to format SI units adding appropriate millis, micros, kilos, etc.
 * @author Doug Gillespie
 *
 */
public class SIUnitFormat {

	/**
	 * Format a value. The value will be displayed to two decimal places and will be 
	 * be displayed with two decimal places. 
	 * @param value SI value to format (not scaled, e.g. 65932.5)
	 * @param baseUnit unit (e.g. Hz)
	 * @return Scaled and formated, e.g. 65.93kHz
	 */
	public static String formatValue(double value, String baseUnit) {
		int nZero = getZeros(value);
		String ch = getScaleChar(nZero);
		return String.format("%3.2f%s%s", value/Math.pow(10, nZero), ch, baseUnit);
	}
	
	/**
	 * 
	 * Format a range of two values. The value will be displayed to two decimal places and will be 
	 * be displayed with two decimal places. Scale always decided by the second value 
	 * @param value SI value to format must be two element array (not scaled, e.g. {65932.5, 1299887.4)
	 * @param baseUnit unit (e.g. Hz)
	 * @return Scaled and formated, e.g. .065-1.30MkHz
	 */
	public static String formatRange(double[] value, String baseUnit) {
		if (value == null || value.length != 2) {
			return "?????" + baseUnit;
		}
		int nZero = getZeros(value[1]);
		String ch = getScaleChar(nZero);
		return String.format("%3.2f-%3.2f%s%s", value[0]/Math.pow(10, nZero), 
				value[1]/Math.pow(10, nZero), ch, baseUnit);
	}
	
	/**
	 * Work out how many zeros there are and round down to nearest multiple of 
	 * three. -ve for values < .001. 
	 * @param value value to scale 
	 * @return numer of zeros (multiple of three)
	 */
	private static int getZeros(double value) {
		if (value == 0) {
			return 0;
		}
		double lv = Math.log10(Math.abs(value));
		int nz = (int) Math.floor(lv/3);
		nz*=3;
		nz = Math.max(-15, Math.min(nz, 15));
		return nz;
	}
	
	/**
	 * Get a character to describe the number of zeros. 
	 * @param zeros number of zeros (multiple of 3)
	 * @return character, e.g. k,m,M,etc. 
	 */
	private static String getScaleChar(int zeros) {
		switch (zeros) {
		case -15:
			return "f";
		case -12:
			return "p";
		case -9:
			return "n";
		case -6:
			return "\u00B5";
		case -3:
			return "m";
		case 0:
			return "";
		case 3:
			return "k";
		case 6:
			return "M";
		case 9:
			return "G";
		case 12:
			return "T";
		case 15:
			return "P";
		default:
			return "?";
		}
	}
	
}
