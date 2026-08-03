package NMEA;

import java.awt.Window;

import PamView.dialog.PamDialogPanel;

/**
 * Abstract the different types of NMEA source into a set of providers. 
 * This will pave the way to being able to add plugins for bespoke NMEA acquisition
 * types. 
 */
public abstract class NMEAProvider {

	private NMEAControl nmeaControl;
	
	
	/**
	 * @param nmeaControl
	 */
	public NMEAProvider(NMEAControl nmeaControl) {
		super();
		this.nmeaControl = nmeaControl;
	}
	
	public AcquireNmeaData getNMEAProcess() {
		return nmeaControl.getAcquireNmeaData();
	}

	public void sayErrorString(String string) {
		getNMEAProcess().sayErrorString(string);
	}
	/**
	 * Checks the checksum of an NMEA data string. 
	 * <p>the checksum is an exclusive OR of all characters
	 * between the $ or ! that starts the string and the *
	 * that preceeds the checksum.  
	 * @param nmeaString
	 * @return
	 */
	public static boolean checkStringCheckSum(StringBuffer nmeaString) {
		byte sum = createStringChecksum(nmeaString);
		byte checkSum = getStringChecksum(nmeaString);
		return (checkSum == sum);
		
	}
	/**
	 * Calculate the correct string buffer for an NMEA sentence.
	 * <p>The checksum is an exclusive OR of all characters between, but 
	 * not including the first ($ or !) and the * preceding the checksum 
	 * @param nmeaString NMEA sentence
	 * @return checksum  value. 
	 */
	public static byte createStringChecksum(StringBuffer nmeaString) {
		char[] nmeaSentence = new char[nmeaString.length()];
		nmeaString.getChars(0, nmeaString.length(), nmeaSentence, 0);
		char[] checkSumChars = null;
		byte sum = (byte) nmeaSentence[1]; // ignore the 0'th character. 
		byte b;
		for (int i = 2; i < nmeaString.length(); i++) {
			b = (byte) nmeaSentence[i];
			if (b == '*') {
//				if (i < nmeaSentence.length - 2) {
//					//				int nChar = nmeaSentence.length - 1 - i;
//					checkSumChars = new char[2];
//					nmeaString.getChars(i+1, i+3, checkSumChars, 0);
//				}
				break;
			}
			sum ^= b;
		}		
		return sum;
	}
	
	/**
	 * Gets the checksum from the end of a string. 
	 * The is the two characters that follow the *
	 * @param nmeaString NMEA string
	 * @return Checksum value
	 */
	static public byte getStringChecksum(StringBuffer nmeaString) {
		int starPos = nmeaString.lastIndexOf("*");
		if (starPos < 0) {
			return 0;
		}
		char[] checkSumChars = new char[2];
		nmeaString.getChars(starPos+1, starPos+3, checkSumChars, 0);
		int checkSum;
		try {
			checkSum = Integer.parseInt(new String(checkSumChars), 16);
		}
		catch (NumberFormatException e) {
			return 0;
		}
		return (byte) checkSum;
	}

	/**
	 * Get a name for the type of NMEA provider
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Get a dialog panel
	 * @param frame
	 * @return dialog panel
	 */
	public abstract PamDialogPanel getDialogPanel(Window frame);
	
	/**
	 * Start acquiring data
	 * @return
	 */
	public abstract boolean startAcquisition();
	
	/**
	 * Stop acquiring data
	 */
	public abstract void stopAcquisition();

	/**
	 * @return the nmeaControl
	 */
	public NMEAControl getNmeaControl() {
		return nmeaControl;
	}
}
