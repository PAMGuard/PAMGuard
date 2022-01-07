package clickTrainDetector.clickTrainAlgorithms.mht;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2ProviderManager.MHTChi2Type;

/**
 * AlgorithmInfo for the MHT algorithm.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class MHTAlgorithmInfo implements  CTAlgorithmInfo{

	private MHTChi2Type mhtChi2Type;

	MHTAlgorithmInfo(MHTChi2Type mhtChi2Type){
		this.mhtChi2Type = mhtChi2Type; 
	}

	/**
	 * Get chi2 calculator type. 
	 * @return the MHTChi2Type;
	 */
	public MHTChi2Type getMhtChi2Type() {
		return mhtChi2Type;
	}

	/**
	 * Set the MHTChi2Type
	 * @param mhtChi2Type - the mht chi2 type;
	 */
	public void setMhtChi2Type(MHTChi2Type mhtChi2Type) {
		this.mhtChi2Type = mhtChi2Type;
	}
}
