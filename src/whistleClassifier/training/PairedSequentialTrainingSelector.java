package whistleClassifier.training;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import whistleClassifier.BasicFragmentStore;
import whistleClassifier.FragmentStore;
import whistleClassifier.WhistleFragment;

public class PairedSequentialTrainingSelector extends
		SequentialTrainingSelector {

	private Vector<FragmentStore> testFragmentStores1;
	private Vector<FragmentStore> testFragmentStores2;

	@Override
	protected int[] createTrainingSection(TrainingDataGroup trainingDataGroup,
			double trainingFraction, int sectionLength, double minFreq,
			double maxFreq, int minContourLength) {
		trainingFraction = 0.1250; // this can be anything between 0 and 1
		// create a training and a single test section using the superclass
		int[] n = super.createTrainingSection(trainingDataGroup, trainingFraction,
				sectionLength, minFreq, maxFreq, minContourLength);
		// then split the test section into two
		int[] n2 = new int[3];
		n2[0] = n[0]; // never change this number ! Use the trainingFraction variable above !
		n2[1] = n[1]/2; // this can be anything <= n[1]/2.
		n2[2] = n2[1]; // this should always be n2[2] = n2[1]; to generate two equal sized pairs. 
		/*
		 * 22 May. 
		 * Are going to change the points at which data are taken from when using 
		 * less than all the data, e.g. 1/, 1/8, will now gradually shrink the 
		 * used data regions so that they stay approximately in the middle of 
		 * each half of the test section. 
		 * n[1] is the total amount of training data. 
		 */
		int[] offset = new int[2];
		/*		
		 * nominal additional offsets for second half of test data when it's 1/4,1/4 (ie.
		 * the test data is split 50/50  
		 */
		offset[0] = 0;
		offset[1] = n[1]/2; 
		if (n2[1] < n[0]/2) {
			/*
			 * Now we're using less than half the test data, so want to offset the start 
			 * points slightly in order to place the used data in the middle of 
			 * the appropriate half.  
			 */
			int off = (n[1]/2-n2[1]) / 2; // offset by half the difference to get in middle. 
			for (int i = 0 ; i < 2; i++) {
				offset[i] += off;
			}
		}
		
		// now split the contents of the testFragmentStores
		List<FragmentStore> allTest = super.getTestStoreList(0);
		testFragmentStores1 = new Vector<FragmentStore>();
		testFragmentStores2 = new Vector<FragmentStore>();
		/*
		 * Each fragment store is a set of data for one classification. Therefore 
		 * don't split these in half, but share them out between the two test sets.
		 */
		for (int i = 0; i < n2[1]; i++) {
			testFragmentStores1.add(allTest.get(i + offset[0]));
			testFragmentStores2.add(allTest.get(i + offset[1]));
		}
		
		return n2;
	}

	@Override
	List<FragmentStore> getTestStoreList(int iTest) {
		if (iTest == 0) {
			return testFragmentStores1;
		}
		else {
			return testFragmentStores2;
		}
	}

	@Override
	public int getNumTestSets() {
		return 2;
	}

}
