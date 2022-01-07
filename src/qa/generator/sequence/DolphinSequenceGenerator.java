package qa.generator.sequence;

import qa.generator.distributions.QACorrelatedSequence;

public class DolphinSequenceGenerator extends RandomSequenceGenerator {
	
	/**
	 * Wahlberg paper give range of 177 - 228SPLp-p, but this is for on axis clicks. 
	 * Gives means and SDS of 205,199,212, SD's 7, 6, 11 for various locations. 
	 * So could give a range from 199-6 to 212+11 = 193 - 223, centre = 208
	 * or Aarons summary of range 177 - 228 which has centre at 202. width/4 of 13. 
	 * All of this WAY too high for off axis clicks, which from finnermans paper are likely
	 * to be 30 - 40dB down. This also would make DO clicks louder than the sperm clicks.  
	 * whale values used in this simulation.
	 * Suggesting we use a value 30dB down at 208-30 = 178, then take of 6 more since these were p-p measures
	 * so use 172 +/- 10    
	 */
	private static final double meanAmplitude = 172;
	private static final double amplitudeSD = 10;

	public DolphinSequenceGenerator(int nSounds) {
		super(new QACorrelatedSequence(true, 0.1, 0.01, 10), new QACorrelatedSequence(false, meanAmplitude, amplitudeSD, 6), nSounds);
		// TODO Auto-generated constructor stub
	}


}
