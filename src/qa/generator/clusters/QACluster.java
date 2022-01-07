package qa.generator.clusters;

import qa.generator.QASoundGenerator;
import qa.generator.distributions.QADistribution;
import qa.generator.sequence.QASequenceGenerator;

/**
 * A cluster contains all the information required to generate a sound or a sequence of 
 * sounds. It consists of a sound generator which creates single sounds and a 
 * sequence generator which creates sequences of potentially autocorrelated sounds (e.g. 
 * sequences of clicks). 
 * @author dg50
 *
 */
public interface QACluster {

	/**
	 * @return The name of the cluster. 
	 */
	public String getName();
		
	/**
	 * The sound generator can generate single sounds with the correct phase on each hydrophone
	 * @return The sound generator
	 */
	public QASoundGenerator getSoundGenerator();
	
	/**
	 * The sequence generator can generate the precise times and source amplitudes for a sequence
	 * of (possibly) autocorrelated sounds, e.g. click trains, totally random whistles, etc. 
	 * @return The sequence generator
	 */
	public QASequenceGenerator getSequenceGenerator();
	
	/**
	 * The depth distribution for the species cluster. This can be null
	 * in which case 0 will be used. Remember that PAMGuard uses height, not depth, however, so 
	 * that distributions that don't allow negative numbers (gamma) can be used, this will genuinely
	 * produce a positive number for a real animal depth and whatever uses this distribution will flip it's sign. <br>
	 * Depths will be generated independently for each sequence and will be constant within 
	 * each sequence. 
	 * @return the depth distribution
	 */
	public QADistribution getDepthDistribution();
	
	/**
	 * 
	 * @return Version number for this sound sequence. 
	 */
	public String getVersion();
	
	/**
	 * Get the primary detection type in the form of a 
	 * class type for a PamDataUnit. <br>
	 * This will be used to set a default primary detector prior to the 
	 * user selecting one. 
	 * @return type of primary detector (generally whistles or clicks). 
	 */
	public Class getPrimaryDetectorType();
	
}
