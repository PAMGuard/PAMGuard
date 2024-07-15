package rawDeepLearningClassifier.dataSelector;

import PamguardMVC.dataSelector.DataSelectParams;

/**
 * Parameters for filtering by the minimum prediciton value. 
 */
public class DLPredictionFilterParams extends DataSelectParams implements Cloneable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * An array indicating which classes are to be used in data selection
	 */
	public boolean[] classSelect; 
	
	/**
	 * The minimum class prediction. 
	 */
	public double[] minClassPredicton;
	
	/**
	 * Clone the parameters. 
	 */
	@Override
	public DLPredictionFilterParams clone() {
		try {
			return (DLPredictionFilterParams) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
