package whistleClassifier;

import java.util.ArrayList;

/**
 * A group of training data from a single species
 * <p>
 * Basically a list of TrainingDataSet objects
 * @author Doug Gillespie
 * @see TrainingDataSet
 *
 */
public class TrainingDataGroup {
	
	String species;
	
	ArrayList<TrainingDataSet> trainingDataSets;

	public TrainingDataGroup(String species) {
		super();
		this.species = species;
		trainingDataSets = new ArrayList<TrainingDataSet>();
	}
	
	public void addDataSet(TrainingDataSet trainingDataSet) {
		trainingDataSets.add(trainingDataSet);
	}
	
	public int getNumDataSets() {
		return trainingDataSets.size();
	}
	
	public TrainingDataSet getDataSet(int i) {
		if (i < 0 || i >= trainingDataSets.size()) {
			return null;
		}
		return trainingDataSets.get(i);
	}
	
	

	/**
	 * Get the number of contours in the data group. 
	 * @return number of contours. 
	 */
	public int getNumContours() {
		
		int nContours = 0;
		for (int i = 0; i < trainingDataSets.size(); i++) {
			nContours += trainingDataSets.get(i).getNumContours();
		}
		return nContours;
	}
	
	@Override
	public String toString() {
		return species;
	}

	/**
	 * Get the number of fragments based on the fragment length
	 * @param fragLen length of each fragment
	 * @return number of fragments. 
	 */
	public int getNumFragments(WhistleFragmenter fragmenter, int fragLen) {

		int nFragments = 0;
		for (int i = 0; i < trainingDataSets.size(); i++) {
			nFragments += trainingDataSets.get(i).getNumFragments(fragmenter, fragLen);
		}
		return nFragments;
	}

}
