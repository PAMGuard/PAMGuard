package NMEA;

import java.util.ListIterator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

/**
 * Special type of data block, just for NMEA data. Instead of deleting data on a timer
 * or owing to the wishes of subscribers, it keeps exactly one copy of each NMEA sentence.
 * 
 * @author Doug Gillespie
 * @see PamguardMVC.PamDataBlock
 *
 */
public class NMEADataBlock extends PamDataBlock<NMEADataUnit> {

	/* (non-Javadoc)
	 * @see PamguardMVC.RecyclingDataBlock#RemoveOldUnitsT(long)
	 */

	public NMEADataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(NMEADataUnit.class, dataName, parentProcess, channelMap);
/*		 leave the remove units stuff in the subclass NMEADataBlock, Normally, units will get removed
		 when the next scentence of that id arrives. But leave the timer running to delete them anyway
		 after a minute.
		*/
		setNaturalLifetime(20);
	}
	

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#addPamData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public void addPamData(NMEADataUnit pamDataUnit) {
		// ideally we'll replace the existing unit in the data table so as to maintain thier
		// order in the view.
		// need to start by getting the first five characters for the scentence identifier.
		StringBuffer nmeaString = pamDataUnit.getCharData();
		if (nmeaString == null) return;
		if (nmeaString.length() < 6 || isNmeaStartCharacter(nmeaString.charAt(0)) == false) return;
		String newId = getSubString(nmeaString, 0);
		if (newId == null) return;
		String oldId;
		for (int i = 0; i < pamDataUnits.size(); i++) {
			oldId = pamDataUnits.get(i).getStringId();
			if (oldId == null) continue;
			if (newId.equals(oldId)) {
				replaceOldString(i, pamDataUnit);
				notifyObservers(pamDataUnit);
				getParentProcess().getProcessCheck().newOutput(this, pamDataUnit);
				return;
			}
		}
		// if it gets here, then no similar string was found, so just add it in the nnormal way.
		super.addPamData(pamDataUnit);
//		System.out.println("Add new string : " + newId);
	}
	
	public static boolean isNmeaStartCharacter(char ch) {
		switch (ch) {
		case '$':
		case '!':
			return true;
		default:
			return false;
		}
	}

	/**
	 * Standard NMEA separator characters. 
	 */
	private static final Character[] NMEADELIMINATORS = {',', '*'};
	
	/**
	 * Is a character in a deliminator list. 
	 * @param ch character to test 
	 * @param deliminators list to test against
	 * @return true if character in in list
	 */
	private static boolean isDeliminator(Character ch, Character[] deliminators) {
		for (int i = 0; i < deliminators.length; i++) {
			if (ch == deliminators[i]) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Extract a sub string only using standard NMEA separator characters , and * 
	 * @param stringBuffer string to separate
	 * @param pos substring position
	 * @return sub string
	 */
	public static String getSubString(StringBuffer stringBuffer, int pos) {
		return getSubString(stringBuffer, pos, NMEADELIMINATORS);
	}
	
	/**
	 * Can call with a different list of separators for sub string. Used in 
	 * some of the Network REceive functions which include ; which must not 
	 * be used in the standard data or it messes up AIS. 
	 * @param stringBuffer string buffer to separate
	 * @param pos sub string number
	 * @param deliminators list of separator characters. 
	 * @return sub string. 
	 */
	public static String getSubString(StringBuffer stringBuffer, int pos, Character[] deliminators) {
		// go through the string and return characters between the pos'th and pos'th + 1 comma
		// i.e. pos = 0 will return the sentence name including the $
		int cPos1 = 0, cPos2 = 0;
		int nFound = 0;

		for (int i = 0; i < stringBuffer.length(); i++) {
			if (isDeliminator(stringBuffer.charAt(i), deliminators)) {
				++nFound;
				if (nFound == pos) {
					cPos1 = i+1;
				}
				if (nFound == pos+1) {
					cPos2 = i;
					break;
				}
			}
		}
		if (nFound == 0) return new String(stringBuffer);
		if (cPos2 == 0) {
			cPos2 = stringBuffer.length();
		}
		if (cPos2 > 0) return stringBuffer.substring(cPos1, cPos2);
		else return null;
	}
	
	public synchronized NMEADataUnit findNMEADataUnit(String stringName) {

		String oldId;
		NMEADataUnit aUnit;
		ListIterator<NMEADataUnit> it = this.getListIterator(PamDataBlock.ITERATOR_END);
		while (it.hasPrevious()) {
			aUnit = it.previous();
			oldId = aUnit.getStringId();
			if (oldId.endsWith(stringName)) {
				return aUnit;
			}
		}
		
		return null;
	}
	
	
	private void replaceOldString(int pos, NMEADataUnit pamDataUnit) {
		pamDataUnits.remove(pos);
		pamDataUnits.add(pos, pamDataUnit);
	}
	
	public static NMEABitArray six2eight(String charData, int fillBits) {
		//byte[] newBuffer;
		NMEABitArray bitArray;
		int nChars = charData.length();
		int nBits = nChars * 6 - fillBits;
		int nBytes = nBits / 8;
		int leftOvers = nBits % 8;
		if (leftOvers > 0) {
			System.out.println("Non integer number of bytes with " + leftOvers + " spare bits");
			return null;
		}
		//newBuffer = new byte[nBytes];
		bitArray = new NMEABitArray(nBits);
		int nBit = 0;
		//int nByte = 0;
		byte byte6;
		//byte newByte = 0;
		for (int i = 0; i < nChars; i++) {
			byte6 = NMEABitArray.convert628(charData.charAt(i));
//			System.out.print(charData.charAt(i));
//			System.out.print(byte6);
//			System.out.print(" ");
			// copy six bits across to newByte, when it's done the 8th bit, move to the next byte
			for (int iBit = 5; iBit >=0; iBit --) {
				if ((byte6 & (1 << iBit)) > 0){
					bitArray.setBit(nBit);
				}
				nBit++;
			}
		}
//		System.out.println("");
		//System.out.println(newBuffer.asCharBuffer());
		
		return bitArray;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.RecyclingDataBlock#RemoveOldUnitsT(long)
	 */
	@Override
	synchronized protected int removeOldUnitsT(long currentTimeMS) {
		/**
		 * need to rewrite this so that it doesn't crap out of the loop 
		 * as soon as it gets an OK unit since the units are no longer in order.
		 */
		super.removeOldUnitsT(currentTimeMS);
		int unitsRemoved = 0;
		PamDataUnit pamUnit;
		long firstWantedTime = currentTimeMS - this.naturalLifetime * 1000;
		// go through the units in reverse order - that way when they are 
		// deleted the indec of others will be OK.
		for (int i = this.getUnitsCount()-1; i >= 0; i--) {
			pamUnit = pamDataUnits.get(i);
			if (pamUnit.getTimeMilliseconds() < firstWantedTime) {
				pamDataUnits.remove(i);
				unitsRemoved++;
			}
		}
		return unitsRemoved;
	}

}
