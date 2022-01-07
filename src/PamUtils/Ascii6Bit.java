package PamUtils;

import java.util.Arrays;

import NMEA.NMEABitArray;

/**
 * Utilities for converting data to and from an Ascii 6 bit format.
 * @author Doug
 *
 */
public class Ascii6Bit {

	private byte[] byteData;
	private String stringData;
	private int spareBits;
	
	/*
	 * Whoever decided that Java wouldn't support unsigned numeric data should be shot.
	 */
	private static final byte[] masks = {0x01, 0x02, 0x04, 0x08,
		                                 0x10, 0x20, 0x40, -0x80};  
	
	public Ascii6Bit(byte[] byteData) {
		this.byteData = byteData;
	}
	
	public Ascii6Bit(String stringData, int spareBits) {
		super();
		this.stringData = stringData;
		this.spareBits = spareBits;
		fixStringData();
	}

	public String getStringData() {
		if (stringData == null) {
			createStringData();
		}
		return stringData;
	}
	public void setStringData(String stringData) {
		this.stringData = stringData;
		fixStringData();
		byteData = null;
	}
	
	/**
	 * fix an error in string data that crept in in the first half of 2008
	 * when an error in the NMEABitArray.charLUTData caused invalid strings to
	 * be written with a 0x27 ' character instead of 0x60 (back sloping ')
	 */
	private void fixStringData() {
		if (stringData == null) {
			return;
		}
		
		stringData = stringData.replace((char)0x27, (char)0x60);
	}

	public int getSpareBits() {
		return spareBits;
	}
	private void createStringData() {
		int totalBits = byteData.length * 8;
		int nChar = byteData.length * 8 / 6;
		spareBits = nChar * 6 - totalBits;
		while (spareBits < 0) {
			nChar++;
			spareBits = nChar * 6 - totalBits;
		}
		int firstBit = 0;
		int lastBit = 0;
		char ch;
		stringData = new String();
		while (firstBit < totalBits) {
			lastBit = Math.min(totalBits-1, firstBit+5);
			ch = getChar(firstBit, lastBit);
			stringData += ch;
			firstBit = lastBit + 1;
		}
		/*
		 * At this point, we need a fix, since some data were recorded into
		 * the database with an error in the AIS Lookup table used for the encoding
		 * we need to change 
		 */
	}
	private void createByteData() {
		int totalBits = stringData.length() * 6 - spareBits;
		if (totalBits%8 != 0) {
			System.out.println("Error in byte length in Ascii6Bit.createByteData");
		}
		int totalBytes = totalBits / 8;
		byteData = new byte[totalBytes];
		int val;
		int bit = 0;
		for (int i = 0; i < stringData.length(); i++) {
			val = NMEABitArray.convert628(stringData.charAt(i));
			for (int iBit = 0; iBit < 6; iBit++) {
				setBit(bit++, (val & masks[iBit]) != 0);
				if (bit == totalBits) {
					break; // last part of last character will not exist
				}
			}
		}
		bit = 0;
	}
	public boolean getBit(int bit) {
//		int byteNo = bit>>>3;
//		int bitNo = bit%8;
		return ((byteData[bit>>>3] & masks[bit%8]) != 0);
	}
	
	public void setBit(int bit, boolean set) {
		int byteNo = bit>>>3;
		int bitNo = bit%8;
		if (byteData.length <= byteNo) {
			byteData = Arrays.copyOf(byteData, byteNo+1);
		}
		if (set) {
			byteData[byteNo] |= masks[bitNo];
		}
		else {
			byteData[byteNo] &= ~masks[bitNo];
		}
	}
	
	public int getBits(int b1, int b2) {

		int val = 0;
		int bit = 0;
		for (int i = b1; i <= b2; i++) {
			if (getBit(i)) {
				val |= masks[bit];
			}
			bit++;
		}
		
		return val;
	}
	
	public char getChar(int b1, int b2) {
		return NMEABitArray.convert826(getBits(b1, b2));
	}

	public byte[] getByteData() {
		if (byteData == null) {
			createByteData();
		}
		return byteData;
	}

	public void setByteData(byte[] byteData) {
		this.byteData = byteData;
		stringData = null;
	}
}
