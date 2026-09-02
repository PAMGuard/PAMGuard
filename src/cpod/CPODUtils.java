package cpod;

import java.io.File;

import cpod.CPODClassification.CPODSpeciesType;
import cpod.CPODReader.CPODHeader;
import cpod.FPODReader.FPODHeader;


/**
 * Some useful utility function for CPOD and FPOD data
 */
public class CPODUtils {
	
	/**
	 * CPOD file types
	 * @author Jamie Macaulay
	 *
	 */
	public enum CPODFileType {
		CP1("CP1"),
		CP3("CP3"),
		FP1("FP1"),
		FP3("FP3");

		private String text;

		CPODFileType(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public static CPODFileType fromString(String text) {
			for (CPODFileType b : CPODFileType.values()) {
				if (b.text.equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}
	
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
	
	public static CPODFileType getFileType(File cpFile) {
		for (int i=0; i<CPODFileType.values().length; i++) {
			if (cpFile.getAbsolutePath().toLowerCase().endsWith(CPODFileType.values()[i].getText().toLowerCase())) {
				return CPODFileType.values()[i];
			}
		}

		return null;
	}

	/**
	 * Check that a CP1, CP3, FP1 or FP3 file looks like it holds valid data before trying to
	 * import it. This is a quick check on the file size and the header only - it does not read
	 * the detection data. It mainly catches empty or corrupt files, e.g. files which have been
	 * created but never written to, which otherwise import as a large number of nonsense
	 * detections dated 1899.
	 *
	 * @param cpxFile - a CP1, CP3, FP1 or FP3 file. A null file is not an error - it simply means
	 * that there is no file of that type to import.
	 * @return null if the file is OK, otherwise a short description of why it cannot be imported.
	 */
	public static String checkFileOK(File cpxFile) {
		if (cpxFile==null) {
			return null;
		}
		if (!cpxFile.exists()) {
			return "the file does not exist";
		}

		CPODFileType fileType = getFileType(cpxFile);
		if (fileType==null) {
			return "this is not a recognised CPOD or FPOD file type";
		}

		if (cpxFile.length() <= CPODReader.getHeadSize(fileType)) {
			return "the file is empty - it contains a header but no data";
		}

		//the header holds the time the POD started logging. If that's not sensible then
		//there is no point in reading any of the data.
		long fileStart;
		switch (fileType) {
		case CP1:
		case CP3:
			CPODHeader cpodHeader = CPODReader.readHeader(cpxFile);
			if (cpodHeader==null) {
				return "the file header could not be read";
			}
			fileStart = cpodHeader.fileStart;
			break;
		case FP1:
		case FP3:
			FPODHeader fpodHeader = FPODReader.readHeader(cpxFile);
			if (fpodHeader==null) {
				return "the file header could not be read";
			}
			fileStart = podTimeToMillis(fpodHeader.FirstLoggedMin);
			break;
		default:
			return null;
		}

		if (fileStart<=0) {
			return "the file header has no valid start time - the file is probably empty or corrupt";
		}

		return null;
	}

	/**
	 * Get the species from the SpClass field of a CP3 or FP3 file.
	 * <p>
	 * CP3 files hold this in bits 4-6 of byte 36 of the click record, FP3 files in bits 2-3
	 * of byte 14 of the click train (249) record. Both use the same codes, which are the
	 * ordinal values of the Pascal tSpClass enumeration (spNBHF, spDOL, spUnClassed, spSON).
	 *
	 * @param spClass - the SpClass value from the file, 0-3.
	 * @return the ENUM species type.
	 */
	public static CPODSpeciesType getSpClassSpecies(short spClass) {
		switch (spClass) {
		case 0:
			return CPODSpeciesType.NBHF;
		case 1:
			return CPODSpeciesType.DOLPHIN;
		case 2:
			return CPODSpeciesType.UNKNOWN;
		case 3:
			return CPODSpeciesType.SONAR;
		}
		return CPODSpeciesType.UNKNOWN;
	}

	/**
	 * Get the species which corresponds to an index in the species selection menu of the
	 * CPOD data selector, i.e. 0 = Unknown, 1 = NBHF, 2 = Dolphins, 3 = Sonar.
	 * <p>
	 * Note that this is NOT the coding used within CP3 and FP3 files - use
	 * {@link #getSpClassSpecies(short)} for that.
	 *
	 * @param species - the menu index.
	 * @return the ENUM species type. 
	 */
	public static CPODSpeciesType getCPODSpecies(short species) {
		CPODSpeciesType type= CPODSpeciesType.UNKNOWN;
		switch (species) {
		case 1:
			type = CPODSpeciesType.NBHF;
			break;
		case 2:
			type = CPODSpeciesType.DOLPHIN;
			break;
		case 3:
			type = CPODSpeciesType.SONAR;
			break;
		case 4:
			break;
		}
		return type;
	}

	/**
	 * Get the species type from an enum value. 
	 * @param species - the species type. 
	 * @return the species type. 
	 */
	public static CPODSpeciesType getSpecies(String species) {
		if (species == null) {
			return CPODSpeciesType.UNKNOWN;
		}
		try {
			return CPODSpeciesType.valueOf(species.trim());
		}
		catch (IllegalArgumentException e) {
			return CPODSpeciesType.UNKNOWN;
		}
	}
	
	public static short getBits(short data, short bitMap) {
		short firstBit = 0;
		for (int i = 0; i < 8; i++) {
			if ((bitMap & (1<<i)) != 0) {
				break;
			}
			firstBit++;
		}
		data &= bitMap;
		return (short) (data>>firstBit);
	}
	
}
