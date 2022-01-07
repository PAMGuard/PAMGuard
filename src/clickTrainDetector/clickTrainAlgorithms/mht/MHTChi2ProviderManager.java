package clickTrainDetector.clickTrainAlgorithms.mht;

import PamController.SettingsPane;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.layout.mht.StandardMHTChi2Pane;

/**
 * Handles which type of chi2 algorithm is being used and 
 * what settings pane to create.
 * @author Jamie Macaulay 
 *
 */
public class MHTChi2ProviderManager {
	
	public enum MHTChi2Type {STANDARD_MHT_CALC};
	
	
	public MHTChi2Type currentType=MHTChi2Type.STANDARD_MHT_CALC;
	
	/**
	 * The click train algorithm 
	 */
	private MHTClickTrainAlgorithm mhtClickTrainAlgorithm; 

	
	public MHTChi2ProviderManager(MHTClickTrainAlgorithm mhtClickTrainAlgorithm) {
		this.mhtClickTrainAlgorithm=mhtClickTrainAlgorithm; 
	}

	@SuppressWarnings("rawtypes")
	public MHTChi2Provider<PamDataUnit> createMHTChi2(MHTParams mhtParams, PamDataBlock dataBlock,int channelBitMap){
		switch (currentType) {
		case STANDARD_MHT_CALC:
//			return new StandardMHTChi2Provider((StandardMHTChi2Params) mhtParams.chi2Params, mhtParams.mhtKernal, channelBitMap); 
			return new StandardMHTChi2Provider(mhtParams); 
			default:
			break;
		}
		return null;
	}
	
	/**
	 * Create the chi^2 settings pane. If a new chi^2 algorithm is used then this function will 
	 * need to change to set new settings pane. 
	 * @return the chi2 settings pane. 
	 */
	public SettingsPane<? extends MHTChi2Params> createMHTChi2Pane() {
		switch (currentType) {
		case STANDARD_MHT_CALC:
			return new StandardMHTChi2Pane(mhtClickTrainAlgorithm);
		}
		return null;
	}
	
	/**
	 * Get the logging class for saving algorithm info data. 
	 * @return logging info. 
	 */
	public CTAlgorithmInfoLogging getCTAlgorithmLogging(MHTChi2Type currentType) {
		//Note may not be the current type if logging was using a different Chi2 algorithm...
		//this is why the field is not used. 
				switch (currentType) {
		case STANDARD_MHT_CALC:
//			return new StandardMHTChi2Provider((StandardMHTChi2Params) mhtParams.chi2Params, mhtParams.mhtKernal, channelBitMap); 
			return new StandardMHTChi2InfoJSON(); 
			default:
			break;
		}
		return null;
		
	}
	
}