package PamView.symbol.modifier;


/*
 * List of types of possible symbol modification. 
 */
public class SymbolModType {
	
	public static final int EVERYTHING = 0x7;
	
	public static final int FILLCOLOUR = 0x1;
	public static final int LINECOLOUR = 0x2;
	public static final int SHAPE = 0x4;
	
	public static int maxBit = 3;
	
	/**
	 * Get the name of the modification for the given bit or bits. 
	 * @param modTypeBit
	 * @return
	 */
	public static String getName(int modTypeBit) {
		int nBit = Integer.bitCount(modTypeBit);
		if (nBit == 0) {
			return "No Modifications";
		}
		String str = null;
		for (int i = 0; i < maxBit; i++) {
			if ((modTypeBit & (1<<i)) == 0) {
				continue;
			}
			String aStr = getBitName(1<<i);
			if (aStr == null) {
				continue;
			}
			if (str == null) {
				str = aStr;
			}
			else {
				str += "," + aStr;
			}
		}
		return str;
	}
	
	private static String getBitName(int oneBit) {
		switch (oneBit) {
		case FILLCOLOUR:
			return "Fill colour";
		case LINECOLOUR:
			return "Line colour";
		case SHAPE:
			return "Shape";
		}
		return null;
	}
}
