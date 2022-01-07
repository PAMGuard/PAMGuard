package qa.generator.sequence;

import PamUtils.LatLong;
import qa.generator.testset.QATestSet;


/**
 * Abstract class to generate sound sequences
 * @author dg50
 * @see SoundSequence
 *
 */
public abstract class QASequenceGenerator {

	public QASequenceGenerator() {
	}
	
	/**
	 * Get the name, defaults to class name, but should be overridden. 
	 * @return get the name of the sequence generator. 
	 * 
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	abstract public SoundSequence createSequence(QATestSet qaTestSet, LatLong location, LatLong rxLocation, double sampleRate, long startSample);

	/**
	 * @return the nSounds
	 */
	abstract public int getnSounds();

	/**
	 * @param nSounds the nSounds to set
	 */
	abstract public void setnSounds(int nSounds);
}
