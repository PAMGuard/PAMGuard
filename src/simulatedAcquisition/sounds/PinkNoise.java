package simulatedAcquisition.sounds;

/**
 * Pink noise source - that's noise that has roughly the same amount 
 * of energy in each octave band. 
 * @author Doug Gillespie
 *
 */
public class PinkNoise extends WhiteNoise {

	private double cumSum; 
	private double dc1 = 0;
	private double dc2 = 0;
	
	public PinkNoise() {
		super();
	}

	/* (non-Javadoc)
	 * @see simulatedAcquisition.sounds.WhiteNoise#getName()
	 */
	@Override
	public String getName() {
		return "Localised pink noise (one second pulse)";
	}

	/* (non-Javadoc)
	 * @see simulatedAcquisition.sounds.WhiteNoise#getSignal(int, float, double)
	 */
	@Override
	public double[] getSignal(int channel, float sampleRate, double sampleOffset) {
		double[] whiteNoise = super.getSignal(channel, sampleRate, sampleOffset);
		double alp = 1./sampleRate;
		/**
		 * Subtract off 1Hz DC both before and after low pass filtering so DC component of signal remains sane. . 
		 */
		for (int i = 0; i < whiteNoise.length; i++) {
			dc1 += (whiteNoise[i]-dc1)*alp;
			whiteNoise[i] -= dc1;
			cumSum += whiteNoise[i]; // low pass filters the data. 
			dc2 += (cumSum-dc2)*alp;
			whiteNoise[i] = cumSum - dc2;
		}
		return whiteNoise;
	}

}
