package whistleClassifier.training;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import whistleClassifier.BasicFragmentStore;
import whistleClassifier.FragmentStore;
import whistleClassifier.WhistleFragment;
import whistleClassifier.WhistleFragmenter;

/**
 * Selects groups of fragments into training / test groups
 * simply by concatenating all the files together from some random start point.
 * @author Doug Gillespie
 *
 */
public class SequentialTrainingSelector extends TrainingSelector {

	private Random random = new Random();

	private Vector<FragmentStore> trainingFragmentStores;

	private Vector<FragmentStore> testFragmentStores;

	@Override
	int getTotalSections(int iSpecies, int sectionLength, 
			double minFreq, double maxFreq, int minContourLength) {
		return (int) Math.floor(getTrainingDataCollection().getTrainingDataGroup(iSpecies).
				getNumFragments(getWhistleFragmenter(), minFreq, maxFreq, minContourLength) / sectionLength);
	}

	@Override
	public int[] createSections(int iSpecies, double trainingFraction, int sectionLength,
			double minFreq, double maxFreq, int minContourLength) {

		int n[] = createTrainingSection(getTrainingDataCollection().getTrainingDataGroup(iSpecies), 
				trainingFraction, sectionLength, minFreq, maxFreq, minContourLength);

		return n;
	}

	/**
	 * Create a training and a test data selection, randomly selected from within the data for a single
	 * species.  
	 * @param trainingDataGroup data group (one per species)
	 * @param trainingFraction fraction of the data to use. 
	 * @param sectionLength number of fragments in a classification group
	 * @return 2 element int array of the number of fragments in the training and in the test sections. 
	 */
	protected int[] createTrainingSection(TrainingDataGroup trainingDataGroup, 
			double trainingFraction, int sectionLength, double minFreq, double maxFreq,
			int minContourLength) {
		//		int nSections = getTotalSections(iSpecies, sectionLength)
		WhistleFragmenter fragmenter = getWhistleFragmenter();
		if (fragmenter == null) {
			return null;
		}
		int nFrag = trainingDataGroup.getNumFragments(getWhistleFragmenter(), minFreq, maxFreq,
				minContourLength);
		int nSections = (int) Math.floor(nFrag / sectionLength);
		int nTrain = (int) (nSections * trainingFraction);
		int nTest = nSections - nTrain;
		int nContours = trainingDataGroup.getNumContours();
		int startContour = random.nextInt(nContours); // pick a random start point. 
		// then loop around all the contours building training sets as we go. 
		int iContour = startContour;
		trainingFragmentStores = new Vector<FragmentStore>();
		testFragmentStores = new Vector<FragmentStore>();
		iContour = fillStores(trainingFragmentStores, trainingDataGroup, 
				nTrain, sectionLength, iContour, minFreq, maxFreq, minContourLength);
		fillStores(testFragmentStores, trainingDataGroup, 
				nTest, sectionLength, iContour, minFreq, maxFreq, minContourLength);
		int[] ans = new int[2];
		ans[0] = trainingFragmentStores.size();
		ans[1] = testFragmentStores.size();
		return ans;
	}

	/**
	 * Fill a training store
	 * @param store store to fill
	 * @param trainingDataGroup training data group (for one species)
	 * @param nStores  target number of fragment groups. 
	 * @param sectionLength number of fragments in a classification group
	 * @param iContour start contour in the data group
	 * @param minFreq minimum frequency allowed in a fragment
	 * @param maxFreq maximum frequency allowed in a fragment
	 * @return
	 */
	private int fillStores(Vector<FragmentStore> store, TrainingDataGroup trainingDataGroup, 
			int nStores, int sectionLength, int iContour, double minFreq, double maxFreq,
			int minContourLength) {

		WhistleFragmenter fragmenter = getWhistleFragmenter();
		Vector<FragmentStore> currentList = trainingFragmentStores;
		FragmentStore currentStore = null;
		TrainingContour aContour;
		WhistleFragment[] contourFragments;

		double loFreq, hiFreq;
		while (store.size() < nStores) {
			aContour = trainingDataGroup.getContour(iContour);
			if (aContour.getLength() >= minContourLength) {
				contourFragments = fragmenter.getFragments(aContour);
				if (contourFragments != null) {
					for (int iFrag = 0; iFrag < contourFragments.length; iFrag++) {
						if (currentStore == null) {
							currentStore = new BasicFragmentStore(trainingDataGroup.trainingDataSets.get(0).getSampleRate());
						}
						/**
						 * Check for contours with lots of zeros
						 */
						double wf[] = contourFragments[iFrag].getFreqsHz();
						loFreq = hiFreq = wf[0];
						int nZero = 0;
						for (int f = 0; f < wf.length; f++) {
							loFreq = Math.min(loFreq, wf[f]);
							hiFreq = Math.max(loFreq, wf[f]);
							if (wf[f] == 0) {
								nZero++;
							}
						}
						/**
						 * skip any fragments with a loFreq which is too low or a high freq which is too high !
						 */
						if (loFreq < minFreq || (hiFreq > maxFreq && maxFreq > 0)) {
							continue;
						}			

						if (nZero == wf.length) {
							System.out.println("Zero fragment in classifier for species " +
									trainingDataGroup.species + " contour number " + iContour);
							System.out.println("Consider restricting the frequency range of the classifier.");
						}		

						currentStore.addFragemnt(contourFragments[iFrag]);
						if (currentStore.getFragmentCount() == sectionLength) {
							store.add(currentStore);
							currentStore = null;
							if (store.size() == nStores) {
								return iContour;
							}
						}
					}
				}
			}
			if (++iContour >= trainingDataGroup.getNumContours()) {
				iContour = 0;
			}
		}

		return iContour;
	}

//	@Override
//	public FragmentStore getFragmentStore(boolean training, int iFragmentGroup) {
//
//		if (training) {
//			return trainingFragmentStores.get(iFragmentGroup);
//		}
//		else {
//			return testFragmentStores.get(iFragmentGroup);
//		}
//	}

	@Override
	List<FragmentStore> getTrainingStoreList() {
		return trainingFragmentStores;
	}


	@Override
	List<FragmentStore> getTestStoreList(int iTest) {
		return testFragmentStores;
	}


	@Override
	public int getNumTestSets() {
		return 1;
	}


}
