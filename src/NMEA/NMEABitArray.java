package NMEA;


//import com.sun.org.apache.xalan.internal.xsltc.dom.BitArray;

public class NMEABitArray {

//	public static final long serialVersionUID = 0;

	private int[] _bits;
	private int   _bitSize;
	private int   _intSize;
	private int   _mask;

	// This table is used to prevent expensive shift operations
	// (These operations are inexpensive on CPUs but very expensive on JVMs.)
	private final static int[] _masks = {
			0x80000000, 0x40000000, 0x20000000, 0x10000000,
			0x08000000, 0x04000000, 0x02000000, 0x01000000,
			0x00800000, 0x00400000, 0x00200000, 0x00100000,
			0x00080000, 0x00040000, 0x00020000, 0x00010000,
			0x00008000, 0x00004000, 0x00002000, 0x00001000,
			0x00000800, 0x00000400, 0x00000200, 0x00000100,
			0x00000080, 0x00000040, 0x00000020, 0x00000010,
			0x00000008, 0x00000004, 0x00000002, 0x00000001 };

	public NMEABitArray() {
		this(32);
	}

	public NMEABitArray(int size, int[] bits) {
		if (size < 32) size = 32;
		_bitSize = size;
		_intSize = (_bitSize >>> 5) + 1;
		_bits = bits;
	}

	public NMEABitArray(int size) {
		if (size < 32) size = 32;
		_bitSize = size;
		_intSize = (_bitSize >>> 5) + 1;
		_bits = new int[_intSize + 1];
	}

	/**
	 * Returns true if the given bit is set
	 */
	public final boolean getBit(int bit) {
		return((_bits[bit>>>5] & _masks[bit%32]) != 0);
	}

	int _first = Integer.MAX_VALUE; // The index where first set bit is
	int _last  = Integer.MIN_VALUE; // The _INTEGER INDEX_ where last set bit is

	/**
	 * Sets a given bit
	 */
	public final void setBit(int bit) {
		if (bit >= _bitSize) return;
		final int i = (bit >>> 5);
		if (i < _first) _first = i;
		if (i > _last) _last = i;
		_bits[i] |= _masks[bit % 32];
	}

	/**
	 * Returns the size of this bit array (in bits).
	 */
	public final int size() {
		return(_bitSize);
	}

	/**
	 * Get an unsigned integer from the bit array. 
	 * <p>
	 * Integers can be any number of bits. 
	 * The first bit is the most significant.
	 * @param b1 First bit to unpack
	 * @param b2 Last bit to unpack
	 * @return unsigned integer value
	 */
	public int getUnsignedInteger(int b1, int b2) {
		int a = 0;
		int iB = 0;
		for (int i = b2; i >= b1; i--) {
			if (getBit(i)) {
				a += 1<<iB;
			}
			iB++;
		}
//		System.out.println(Integer.toBinaryString(a));
		return a;
	}
	/**
	 * Get a signed integer from the bit array. 
	 * <p>
	 * Integers can be any number of bits and are stored in 
	 * 2's compliment format.
	 * @param b1 First bit to unpack
	 * @param b2 Last bit to unpack
	 * @return signed integer value
	 */
	public int getSignedInteger(int b1, int b2) {
		int a = getUnsignedInteger(b1, b2);
		if (getBit(b1)) {
			a -=  1<<(b2-b1+1);
		}
		return a;
	}
	
	/**
	 * Lookup table to convert from integer to 6 bit ASCII data for
	 * AIS vessel names, destinations, etc. 
	 * Table 23 from page 55 of IEC 61993
	 */
	static public final char[] ASCII6 = {
		'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
		'X', 'Y', 'Z', '[', '\\', ']', '^', '_', 
		' ', '!', '\"', '#', '$', '%', '&', '\'', 
		'(', ')', '*', '+', ',', '-', '.', '/', 
		'0', '1', '2', '3', '4', '5', '6', '7', 
		'8', '9', ':', ';', '<', '=', '>', '?'};
	
	/*
	 * changed from this on 19 June 2008. 
	 * Seems there was an error between '`' and '\''
	 * 
	static public final char[] ASCII6 = {
		'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
		'X', 'Y', 'Z', '[', '\\', ']', '^', '_', 
		' ', '!', '\"', '#', '$', '%', '&', '`', 
		'(', ')', '*', '+', ',', '-', '.', '/', 
		'0', '1', '2', '3', '4', '5', '6', '7', 
		'8', '9', ':', ';', '<', '=', '>', '?'};
	 */

	/**
	 * Gets a string based on packed bits from an AIS string
	 * using the 6 bit ascii character set. 
	 * @param b1 First bit from AIS / NMEA data
	 * @param b2 Last bit from AIS / NMEA data
	 * @return character string
	 */
	public String getString(int b1, int b2) {
		// repacks the data as a six bit character string.
		// take six bits at a time and get theinteger, then look up the character
		String string = new String();
		int i1, i2;
		i1 = b1;
		i2 = i1 + 5;
		int n;
		char ch;
		while (i2 <= b2) {
			n = getUnsignedInteger(i1, i2);
			ch = ASCII6[n];
			string += ch;
			i1 = i2 + 1;
			i2 = i1 + 5;
		}
		
		return string;
	}

	static private byte[] lut6;
	/**
	 * converts a character from an AIS data string
	 * into a six bit integer value packed into an
	 * 8 bit byte. 
	 * @param ch Character from AIS or NMEA string
	 * @return 6 bit integer (0 to 63)
	 */
	public static byte convert628(char ch) {
		// returns a six bit to eight bit LUT - 
		if (lut6 == null) lut6 = createLUT6();
		return lut6[ch];
	}
	/**
	 * Convert a six bit integer value to AIS / NMEA character data
	 * 
	 * @param n an six bit ascii code
	 * @return a six bit character
	 */
	public static char convert826(int n) {
		// returns a six bit to eight bit LUT - 
		return charLUTData[n];
	}
	/**
	 * Lookup table data to go from characters to 6 bit integers. 
	 * The table as is, could be used to convert a number to a character.
	 * When unpacking AIS data, we need to do the opposite, so the data
	 * are flicked around in createLUT6.
	 * <p>
	 * Table G1 from page 171 of  IEC 61993
	 * <p>
	 * 19 June - changed '\'' to 0x60 (opposite of error in ASCII6 array !
	 */
//	static char[] charLUTData = {
//		'0', '1', '2', '3', '4', '5', '6', '7',
//		'8', '9', ':', ';', '<', '=', '>', '?', 
//		'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 
//		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
//		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
//		'\'', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 
//		'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
//		'p', 'q', 'r', 's', 't', 'u', 'v', 'w'};
	static char[] charLUTData = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', ':', ';', '<', '=', '>', '?', 
		'@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 
		'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 
		'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 
		0x60, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 
		'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 
		'p', 'q', 'r', 's', 't', 'u', 'v', 'w'};
	/**
	 * Converts the character lookup data to an integer-to-character
	 * LUT, where the indexing is the standard ASCII character, converted to
	 * an integer. 
	 * The resulting lookup table is sparsely populated and should will return 0 for any 
	 * index not used in charLUTData.
	 * @return a six bit value packed into one byte
	 */
	public static byte[] createLUT6() {
		lut6 = new byte[256];
		int i6;
		for (int i = 0; i < charLUTData.length; i++) {
			i6 = charLUTData[i];
			lut6[i6] = (byte) i;
		}
		return lut6;
	}
	

	@Override
	public String toString() {
		String str = new String();
		for (int i = 0; i <= 38; i++) {
			if (getBit(i)) {
				str += "1";
			}
			else {
				str += "0";
			}
		}
		return str;
	}
	
}
