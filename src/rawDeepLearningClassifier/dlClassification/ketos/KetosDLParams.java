package rawDeepLearningClassifier.dlClassification.ketos;

import rawDeepLearningClassifier.dlClassification.animalSpot.StandardModelParams;

/**Paramters for Ketos classifiers. 
 * 
 */
public class KetosDLParams extends StandardModelParams {

	/**
	 * 
	 */
	static final long serialVersionUID = 1L;
	
	public KetosDLParams() {
		//default should be to use the default ketos paramters. 
		useDefaultTransfroms = true; 
	}
	
	@Override
	public KetosDLParams clone() {
		KetosDLParams newParams = null;
		newParams = (KetosDLParams) super.clone();
//			if (newParams.spectrogramNoiseSettings == null) {
//				newParams.spectrogramNoiseSettings = new SpectrogramNoiseSettings();
//			}
//			else {
//				newParams.spectrogramNoiseSettings = this.spectrogramNoiseSettings.clone();
//			}
		return newParams;
	}
	

}
