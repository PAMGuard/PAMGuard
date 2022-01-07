package PamUtils;

import java.io.File;
import java.text.NumberFormat;

/**
 * Functions to get and display disk space in sensible units. 
 * @author dg50
 *
 */
public class DiskSpaceFormat {

	private static final String[] unitStr = {"B", "kB", "MB", "GB", "tB"};
	private static long[] unitZeros;
	
	static {
		unitZeros = new long[unitStr.length];
		long scle = 1;
		for (int i = 0; i < unitStr.length; i++) {
			unitZeros[i] = scle;
			scle <<= 10;
		}
	}
	

	/**
	 * Get the amount of space at the specified path or null if the 
	 * path is not set, etc.
	 * @param file path
	 * @return free space in bytes
	 */
	public static Long getFreeSpace(String path) {
		if (path == null) {
			return null;
		}
		try {
			File fPath = new File(path);
			if (fPath.exists() == false) {
				fPath = fPath.getParentFile();
			}
			if (fPath.exists() == false) {
				return null;
			}
			return fPath.getFreeSpace();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get the file system root. 
	 * @param file path
	 * @return file system root, e.g. C:
	 */
	public static String getFileRoot(String path) {
		if (path == null) {
			return null;
		}
		try {
			File fPath = new File(path);
			int firstSep = path.indexOf(File.separatorChar);
			if (firstSep > 0) {
				return path.substring(0, firstSep);
			}
			else {
				return path;
			}
		}
		catch (Exception e) {
			return null;
		}
		
	}

	
	/**
	 * Format space in bytes,kBytes, etc. 
	 * @param space
	 * @return String or null if space was null
	 */
	public static String formatSpace(Long space) {
		if (space == null) {
			return null;
		}
		int scaleInd = 0;
		for (int i = 0; i < unitZeros.length; i++) {
			if (space > unitZeros[i]) {
				scaleInd = i;
				continue;
			}
			break;
		}
//		NumberFormat nf = NumberFormat.getCompactNumberInstance();
//		String str = nf.format((double) space / unitZeros[scaleInd]) + unitStr[scaleInd];
		String str = String.format("%3.1f%s", (double) space / unitZeros[scaleInd], unitStr[scaleInd]);
		return str;
	}
}
