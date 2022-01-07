package whistleClassifier.training;


import whistleClassifier.FragmentStore;
import whistleClassifier.WhistleFragmenter;

/**
 * Class containing functions to randomly select suitable groups of
 * training and test data from a TrainingDataCollection
 * 
 * @author Doug Gillespie
 * @see TrainingDataCollection
 * @see WhistleFragmenter
 *
 */
public abstract class TrainingSelector {

	private TrainingDataCollection trainingDataCollection;
	
	private WhistleFragmenter whistleFragmenter;
	
	/**
	 * Sets the training data collection
	 * @param trainingDataCollection training data
	 */
	public void setTrainingDataCollection(TrainingDataCollection trainingDataCollection) {
		this.trainingDataCollection = trainingDataCollection;
	}
	
	/**
	 * get the training data collection
	 * @return training data collection
	 */
	public TrainingDataCollection getTrainingDataCollection() {
		return trainingDataCollection;
	}
	
	/**
	 * Set the whistle fragmenter to use.
	 * @param whistleFragmenter whistle fragmenter
	 */
	public void setWhistleFragmenter(WhistleFragmenter whistleFragmenter) {
		this.whistleFragmenter = whistleFragmenter;
	}
	
	/**
	 * Get the whistle fragmenter
	 * @return fragmenter
	 */
	public WhistleFragmenter getWhistleFragmenter() {
		return whistleFragmenter;
	}
	
	/** 
	 * 
	 * @return
	 */
	public int getNumSpecies() {
		if (trainingDataCollection == null) {
			return 0;
		}
		return trainingDataCollection.getNumTrainingGroups();
	}
	
	/*
	 * Get the number of test sets - may be > 1
	 */
	abstract public int getNumTestSets();
	
	public int getTotalFragments(int iSpecies, double minFreq, double maxFreq, int minContourLength) {
		if (trainingDataCollection == null) {
			return 0;
		}
		return trainingDataCollection.getTrainingDataGroup(iSpecies).
		getNumFragments(whistleFragmenter, minFreq, maxFreq, minContourLength);
	}
	
	/**
	 * Get the total number of training + test sections, each consisting
	 * of sectionLength fragments
	 * @param iSpecies species index in training set
	 * @param sectionLength length of each training or test section
	 * @return number of complete sections
	 */
	abstract int getTotalSections(int iSpecies, int sectionLength,
			double minFreq, double maxFreq, int minContourLength);
	
	/**
	 * Tell the training selector to create a new (hopefully randomised)
	 * set of training and test data for a species. 
	 * <p>
	 * It does not actually have to 
	 * create the data sets at the moment - if it want's to save memory it 
	 * can set up appropriate data structures to define what the sets are
	 * and then create them when they are asked for later. Whether or not this
	 * is possible may depend on the type of randomisation and grouping employed
	 * in the concrete class.  
	 * @param trainingFraction fraction of data to be used in training (usually 2/3)
	 * @return a two element array giving the number of test and the number of training sections (more elements if 
	 * the bumber of test sections is > 1)
	 */
	abstract int[] createSections(int iSpecies, double trainingFraction, int sectionLength,
			double minFreq, double maxFreq, int minContourLength);
	
//	abstract FragmentStore getFragmentStore(boolean training, int iFragmentGroup);
	

	/**
	 * Get a parameter array for feeding straight into the classifier for either the 
	 * training set or the test set.
	 * @param testTrainSet index o set: 0 = training; 1 - n = test.
	 */
	public double[][] getParameterArray(int testTrainSet) {
		java.util.List<FragmentStore> stores;
		if (testTrainSet == 0) {
			stores = getTrainingStoreList();
		}
		else {
			stores = getTestStoreList(testTrainSet-1);
		}
		if (stores == null || stores.size() < 1) {
			return null;
		}
		int nNaN;
		double[] oneArray;
		double[][] params = new double[stores.size()][];
		for (int i = 0; i < stores.size(); i++) {
			params[i] = oneArray = stores.get(i).getParameterArray();
			nNaN = 0;
			for (int j = 0; j < oneArray.length; j++) {
				if (Double.isNaN(oneArray[j])) {
					nNaN++;
				}
			}
			if (nNaN > 0) {
				System.out.println("Null value(s) in parameter array");
				oneArray = stores.get(i).getParameterArray();
			}
		}
		return params;
	}

	abstract java.util.List<FragmentStore> getTestStoreList(int iTest);

	abstract java.util.List<FragmentStore> getTrainingStoreList();
	
	
}
