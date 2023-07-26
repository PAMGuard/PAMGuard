package Array.layoutFX;

/**
 * Default hydrophone parameters.
 *  
 * @author Jamie Macaulay
 *
 */
public enum DefaultHydrophone {
	
	SoundTrap600HF("SoundTrap 600 HF", -176., 0), SoundTrap300HF("SoundTrap 300 HF",  -176., 0), HydroMoth_1_0_0("HydroMoth 1.0.0",  -180., 0);

	/**
	 * The name of the hydrophones.
	 */
	private String name;
	
	/**
	 * The sensitivity of the hydrophone in dB re 1V/uPa. 
	 */
	private double sens;
	
	/**
	 * The gain in dB. 
	 */
	private double gain;

	/**
	 * The name of the hydrophone.
	 * @param name - the name of the hydrophone.
	 * @param sens - the sensitivity of the hydrophone.
	 * @param gain - the gain of the hydrophone.
	 */
	DefaultHydrophone(String name, double sens, double gain) {
		this.name = name; 
		this.sens = sens; 
		this.gain = gain;
		
	}
	

}
