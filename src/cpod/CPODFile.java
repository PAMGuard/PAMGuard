package cpod;

import java.io.File;

import cpod.CPODUtils.CPODFileType;

/**
 * A CP1 along with it's CP3 file 
 * @author Jamie Macaulay
 *
 */
public class CPODFile {

	/**
	 * A CP1 or FP1 file
	 */
	public File cp1File = null;

	/**
	 * A CP3 or CP3 file
	 */
	public File cp3File = null;

	/**
	 * Get the name of the file
	 * @return the name of the file; 
	 */
	public String getName() {
		String fileName = "";
		if (cp1File!=null) fileName+=cp1File.getName() + " ";
		if (cp3File!=null) fileName+=cp3File.getName() + " ";
		return fileName;
	}

	/**
	 * Check whether the current CPOD file is a CPOD or FPOD
	 * @return true if FPOD. 
	 */
	public boolean isFPOD() {
		if (cp1File !=null) return CPODUtils.getFileType(cp1File).equals(CPODFileType.FP1);
		if (cp3File !=null) return CPODUtils.getFileType(cp3File).equals(CPODFileType.FP3);
		return false;
	}

}
