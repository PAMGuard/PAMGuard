package cpod;

import java.io.File;

import cpod.CPODClassification.CPODSpeciesType;


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
	 * Get the CPOD species from an int flag 
	 * @param species - integer flag representing the species
	 * @return the ENUM species type. 
	 */
	public static CPODSpeciesType getSpecies(short species) {
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
		return CPODSpeciesType.valueOf(species);
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
