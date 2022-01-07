package RightWhaleEdgeDetector;


/**
 * Standard classifier to classify right whale sounds. 
 * There will probably only ever be one of these, but 
 * thought I'd abstract it anyway just in case it becomes
 * necessary to have > 1
 * @author Doug Gillespie
 *
 */
interface RWClassifier {

	abstract public void setSoundData(float sampleRate, int fftLength, int fftHop);
	
	abstract public int getSoundClass(RWESound aSound);
	
}
