package cpod;

/**
 * Some useful utility function for CPOD and FPOD data
 */
public class CPODUtils {
	
	/**
	 * Java will only have read signed bytes. Nick clearly
	 * uses a lot of unsigned data, so convert and inflate to int16. 
	 * @param signedByte
	 * @return unsigned version as int16. 
	 */
	public static short toUnsigned(byte signedByte) {
//		short ans = signedByte;
		
		short ans = (short) (signedByte & 0xff);
		
//		if (ans < 0) {
//			ans += 256;
//		}
		
		return ans;
	}
	
	/**
	 * Convert POD time to JAVA millis - POD time is 
	 * integer minutes past the same epoc as Windows uses
	 * i.e. 0th January 1900.
	 * @param podTime
	 * @return milliseconds. 
	 */
	public static long podTimeToMillis(long podTime) {
		return podTime * 60L * 1000L - (25569L*3600L*24000L);
	}

}
