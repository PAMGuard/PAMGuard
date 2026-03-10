package PamController.soundMedium;

/**
 * Defines the assumed medium that sound is propogating through i.e. water or
 * air.
 * 
 * Other mediums may be added by adding a new enum and modifying switch
 * statements. For example if PAMGuard is used in any space missions to the water
 * moons of Saturn or perhaps someone wants to see how well a dolphin swims
 * through custard.
 * 
 * @author Jamie Macaulay
 *
 */
public class GlobalMedium {

	/**
	 * The type of medium
	 * @author Jamie Macaulay
	 *
	 */
	public enum SoundMedium{Water, Air}; 


	/**
	 * Get the default sound speed for the medium.
	 * @param currentMedium - the current medium. 
	 * @return the default sound speed in meters per second. 
	 */
	public static double getDefaultSoundSpeed(SoundMedium currentMedium) {
		switch (currentMedium) {
		case Air:
			return 343; 
		case Water:
			return 1500; 
		default:
			return 1500; 
		}
	}


	/**
	 * Get the reference unit commonly used for this medium e.g. 1uPa in water and 20uPa in air
	 * @return the reference unit commonly used for the medium. 
	 */
	public static double getdBreference(SoundMedium currentMedium) {
		switch (currentMedium) {
		case Air:
			return 20e-6; 
		case Water:
			return 1e-6; 
		default:
			return 1e-6; 
		}
	}

	/**
	 * Get the default sensitivity of a reciever in dB referenced to the medium
	 * dependent reference value. This will be highly variable but just to get in a
	 * few tens of dB of where it should. 
	 * 
	 * @return the reference unit commonly used for the medium.
	 */
	public static double getDefaultSens(SoundMedium currentMedium) {
		switch (currentMedium) {
		case Air:
			//note that the reference unit is dB re 1V/Pa but stored in dB re 1V/20uPa
			//here the first number is in dB re 1V/Pa for clarity and rthwen converted to dB re 1V/20uPa
			return -30 +  getdBSensOffset(SoundMedium.Air) ; 
		case Water:
			return -170; 
		default:
			return 1e-6; 
		}
	}

	/**
	 * Hydrophones are used under water and microphones in air. Need to change these words around...
	 * @param currentMedium - the current medium e.g. water, air. 
	 * @param caps true to capitalise the first letter of the string. 
	 * @param plural - true to pluralise the string. 
	 * @return the string describing the reciever. 
	 */
	public static String getRecieverString(SoundMedium currentMedium, boolean caps, boolean plural) {
		String recieverString; 

		switch (currentMedium) {
		case Air:
			recieverString= "Microphone"; 
			break;
		case Water:
			recieverString ="Hydrophone"; 
			break;
		default:
			recieverString ="Hydrophone"; 
			break;
		}

		recieverString = capsPlural( recieverString,  caps,  plural);

		return recieverString; 
	}
	
	public static String getToolTip(SoundMedium currentMedium) {
			return String.format("Vertical measurement = %s; Sensitivities in %s", 
					getZString(currentMedium, false, false), getdBRefString(currentMedium));
	}

	/**
	 * Get a string description of the z dimensions e.g. depth or height. 
	 * @param currentMedium - the current medium e.g. water, air. 
	 * @param caps true to capitalise the first letter of the string. 
	 * @param plural - true to pluralise the string. 
	 * @return the string description of the Z dimensions 
	 */
	public static String getZString(SoundMedium currentMedium, boolean caps, boolean plural) {
		String zString; 
		switch (currentMedium) {
		case Air:
			zString =  "Height"; 
			break;
		case Water:
			zString =  "Depth"; 
			break;
		default:
			zString =  "Depth"; 
			break;
		}

		zString = capsPlural( zString,  caps,  plural);

		return zString;
	}


	/**
	 * Get a string description of the Pascal reference for receiver sensitivities in dB. 
	 * @param currentMedium - the current medium. 
	 * return string of dB reference
	 */
	public static String getdBSensRefString(SoundMedium currentMedium) {
		String zString; 
		switch (currentMedium) {
		case Air:
			//this is different from the dB ref...uuurgh
			zString =  "dB re 1V/Pa"; 
			break;
		case Water:
			zString =  "dB re 1V/\u00B5Pa"; 
			break;
		default:
			zString =  "dB re 1V/\u00B5Pa"; 
			break;
		}

		return zString;
	}


	/**
	 * Get the dB offset to convert form sensitivity value units to received amplitude units.
	 * Sensitivity values in PG are always stored in the same reference as received amplitude. However
	 * they may be different units in the display because terrestrial acoustics is annoying. 
	 * <p>
	 *  Add this to input from display to get stored unit. Subtract to get display units. 
	 * @param currentMedium - the current medium. 
	 * @return the daB offset.
	 */
	public static double getdBSensOffset(SoundMedium currentMedium) {
		switch (currentMedium) {
		case Air:
			// convert from dB re 1V/Pa to dB re 1V/20uPa
			return -93.974; 
		case Water:
			return 0; 
		default:
			return 0; 
		}
	}

	/**
	 * Get a string description of the Pascal reference for the dB
	 * @param currentMedium - the current medium. 
	 * @return string of dB reference
	 */
	public static String getdBRefString(SoundMedium currentMedium) {
		String zString; 
		switch (currentMedium) {
		case Air:
			zString =  "dB re 20\u00B5Pa"; 
			break;
		case Water:
			zString =  "dB re 1\u00B5Pa"; 
			break;
		default:
			zString =  "dB re 1\u00B5Pa"; 
			break;
		}

		return zString;
	}

	/**
	 * Get a string description of the dB reference for a power spectral density.
	 * @param currentMedium - the current medium.
	 * @return string of dB reference
	 */
	public static String getdBPSDString(SoundMedium currentMedium) {
		String zString;
		switch (currentMedium) {
			case Air:
				zString =  "dB re 400\u00B5Pa\u00B2/Hz";
				break;
			case Water:
				zString =  "dB re 1\u00B5Pa\u00B2/Hz";
				break;
			default:
				zString =  "dB re 1\u00B5Pa\u00B2/Hz";
				break;
		}

		return zString;
	}

	/**
	 * Get a string description of the dB reference for a sound exposure level.
	 * @param currentMedium - the current medium.
	 * @return string of dB reference
	 */
	public static String getdBSELString(SoundMedium currentMedium) {
		String zString;
		switch (currentMedium) {
			case Air:
				zString =  "dB re 400\u00B5Pa\u00B2s";
				break;
			case Water:
				zString =  "dB re 1\u00B5Pa\u00B2s";
				break;
			default:
				zString =  "dB re 1\u00B5Pa\u00B2s";
				break;
		}

		return zString;
	}

	/**
	 * Get the z coefficient value for displays. Height are always stored so
	 * that +z points up (i.e. -g). In display height is input as depth as so must
	 * be multiplied by -1 before being stored. In air height is more sensible to
	 * think of as +z and so does not need multiplied (or multiplied by 1). The
	 * height coefficient is the value height inputs are multiplied before being
	 * stored. @see getZString
	 * 
	 * @param currentMedium - the current medium.
	 * @return the height coefficient.
	 */
	public static double getZCoeff(SoundMedium currentMedium) {
		switch (currentMedium) {
		case Air:
			return 1; 
		case Water:
			return -1; 
		default:
			return -1; 
		}
	}

	/**
	 * Convert a string to be lower case
	 * @param zString - the string
	 * @param caps - true sets the first  
	 * @param plural - true to pluralise a string
	 * @return the updated string. 
	 */
	private static String capsPlural(String zString, boolean caps, boolean plural) {
		if (caps) {
			zString = zString.substring(0, 1).toUpperCase() + zString.substring(1);
		}
		else {
			zString = zString.toLowerCase(); 
		}

		if (plural) {
			zString = zString + "s"; 
		}

		//TODO - de pluralise?

		return zString;
	}

	/**
	 * Get default amplitude scales for display for different medium 
	 * @param currentMedium - the current medium.
	 * @return the minimum and maximum amplitude in dB 
	 */
	public static double[] getDefaultAmplitudeScales(SoundMedium currentMedium) {
		//TODO - could we make this a little better by calculating clip level. 
		double[] ampScales = new double[] {0,0};
		switch (currentMedium) {
		case Air:
			//dB re 20uPa
			ampScales[0]  = 0; 
			ampScales[1]  = 80; 
			return ampScales; 
		case Water:
			//dB re 1uPa
			ampScales[0]  = 80; 
			ampScales[1]  = 170; 
			return ampScales; 
		}
		return ampScales; 
	}


	/**
	 * Get default dB/Hz scales for display for different medium 
	 * @param currentMedium - the current medium.
	 * @return the minimum  and maximum amplitude in dB/Hz
	 */
	public static double[] getDefaultdBHzScales(SoundMedium currentMedium) {
		double[] ampScales = new double[] {0,0};
		switch (currentMedium) {
		case Air:
			//dB re 20uPa
			ampScales[0]  = 0; 
			ampScales[1]  = 120; 
			return ampScales; 
		case Water:
			//dB re 1uPa
			ampScales[0]  = 20; 
			ampScales[1]  = 150; 
			return ampScales; 
		}
		return ampScales; 
	}


}
