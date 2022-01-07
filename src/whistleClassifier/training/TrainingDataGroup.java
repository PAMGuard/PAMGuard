package whistleClassifier.training;

import java.util.ArrayList;

import whistleClassifier.WhistleFragmenter;

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
	
	private int[] contoursPerSet;
	
	private int[] cumulativeContourCount;

	public TrainingDataGroup(String species) {
		super();
		this.species = species;
		trainingDataSets = new ArrayList<TrainingDataSet>();
	}
	
	public void addDataSet(TrainingDataSet trainingDataSet) {
		trainingDataSets.add(trainingDataSet);
		setContoursPerSet();
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
	
	/**
	 * After a data set has been added, work out the number of 
	 * contours in each data set. This can then be accessed
	 * quickly later using getContoursPerSet();
	 * @return number of contours per set. 
	 */
	private void setContoursPerSet() {
		if (trainingDataSets.size() == 0) {
			contoursPerSet = cumulativeContourCount = null;
			return;
		}
		contoursPerSet = new int[trainingDataSets.size()];
		cumulativeContourCount = new int[trainingDataSets.size()+1];
		int cumSum = 0;
		for (int i = 0; i < trainingDataSets.size(); i++) {
			contoursPerSet[i] = trainingDataSets.get(i).getNumContours();
			cumulativeContourCount[i] = cumSum;
			cumSum += contoursPerSet[i];
		}
		cumulativeContourCount[trainingDataSets.size()] = cumSum;
	}
	
	public int[] getContoursPerSet() {
		return contoursPerSet;
	}

	public int[] getCumulativeContourCount() {
		return cumulativeContourCount;
	}

	public void setCumulativeContourCount(int[] cumulativeContourCount) {
		this.cumulativeContourCount = cumulativeContourCount;
	}

	@Override
	public String toString() {
		return species + "; nC=" + getNumContours();
	}

	/**
	 * Get the number of fragments based on the fragment length
	 * @param fragLen length of each fragment
	 * @return number of fragments. 
	 */
	public int getNumFragments(WhistleFragmenter fragmenter, double minFreq, double maxFreq,
			int minContourLength) {

		int nFragments = 0;
		for (int i = 0; i < trainingDataSets.size(); i++) {
			nFragments += trainingDataSets.get(i).getNumFragments(fragmenter, minFreq,  maxFreq,
					 minContourLength);
		}
		return nFragments;
	}
	
	public TrainingContour getContour(int iContour) {
		// get the ith contour of the dataset, starting sequentially from 0
		int iSet = 0;
		while (iSet < cumulativeContourCount.length-1) {
			if (iContour < cumulativeContourCount[iSet+1]) {
				break;
			}
			iSet++;
		}
		int iCont = iContour - cumulativeContourCount[iSet];
		return trainingDataSets.get(iSet).getTrainingContour(iCont);
	}

}
