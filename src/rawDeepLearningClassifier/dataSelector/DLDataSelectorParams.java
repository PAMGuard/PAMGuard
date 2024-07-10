package rawDeepLearningClassifier.dataSelector;

import java.io.Serializable;

import PamguardMVC.dataSelector.DataSelectParams;

/**
 * Paramters for the DL data seelctor	
 */
public class DLDataSelectorParams extends DataSelectParams implements Serializable, Cloneable{
	
	private static final long serialVersionUID = 1L;

	public int dataSelectorIndex = 0;
	
	/**
	 * List of data selector parameters for different deep learning selectors. 
	 */
	public DataSelectParams[] dataSelectorParams;
	
	
}
