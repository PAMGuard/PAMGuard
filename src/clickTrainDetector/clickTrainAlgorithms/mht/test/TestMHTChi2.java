package clickTrainDetector.clickTrainAlgorithms.mht.test;

import java.util.BitSet;
import java.util.ListIterator;

import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernelParams;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.TrackBitSet;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;
import clickTrainDetector.clickTrainAlgorithms.mht.test.ExampleClickTrains.SimClickImport;

/**
 * Quick program which performs some simple tests the standard chi^2 
 * algorithm. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class TestMHTChi2  {

	/**
	 * Standard MHT params.
	 */
	private static StandardMHTChi2Params pamMHTChi2Params = new StandardMHTChi2Params(); 

	/**
	 * The MHTKernal params. 
	 */
	private static MHTKernelParams mHTkernalParams= new MHTKernelParams(); 
	
	private static double calcChi2(SimpleClickDataBlock simclicks, StandardMHTChi2 mhtChi2, BitSet bitSet, int nClicks) {
		
		mhtChi2.clear(); //make sure the thing is reset properly. 
		
		ListIterator<SimpleClick> clicks  = simclicks.getListIterator(0);
		int n=0; 
		SimpleClick simpleClick; 
		while (clicks.hasNext() && n<nClicks) {
			//remember the kcount is the total current number of detections.
			simpleClick=clicks.next(); 
			mhtChi2.getChi2Provider().addDetection(simpleClick, n+1);
			mhtChi2.update(simpleClick, new TrackBitSet(bitSet), n+1);
			//System.out.println("Chi2: " + mhtChi2.getChi2() + " Simple click: " + simpleClick.amplitude);
			n++; 
		}
		
		return mhtChi2.getChi2();
	
	}

	/**
	 * Run some tests on the chi2 algorithm. 
	 */
	private static void testChiCalculation() {


		ExampleClickTrains simClicks = new ExampleClickTrains(); 
		simClicks.importSimClicks(SimClickImport.SIMCLICKS_1); 
		
		//make the same as MATLAB. 
//		pamMHTChi2Params.maxICI=0.4; 
//		pamMHTChi2Params.newTrackPenalty=100; 
//		pamMHTChi2Params.newTrackN=1;
//		mHTkernalParams.maxCoast=3;
//		mHTkernalParams.nPruneback=3; 
		
		pamMHTChi2Params.maxICI=0.5; 
		pamMHTChi2Params.newTrackPenalty=50;
		pamMHTChi2Params.lowTrackICIBonus=1; 

		pamMHTChi2Params.coastPenalty=5;
		mHTkernalParams.nPruneback=5; 
		mHTkernalParams.nPruneBackStart=7; 
		mHTkernalParams.maxCoast=5; 
		mHTkernalParams.nHold=20; 

		//create the MHT chi2
		StandardMHTChi2 mhtChi2 = new StandardMHTChi2(pamMHTChi2Params,  mHTkernalParams); 
		
		//bit of a hack but remove bearing and correlation. 
		pamMHTChi2Params.enable=new boolean[] {true, true, false, false, false, false}; 
		pamMHTChi2Params.printSettings();
		mHTkernalParams.printSettings();
		
		System.out.println("/*****Loading sim clicks 2******/");

		
		/****Test standard simulated data set***/
		double chi2;
		
		int nclicks=10; 
		int coasts=0; 
		BitSet bitSet = new BitSet(); 
		
		
		for (int i=0; i<nclicks; i++) {
			bitSet.set(i, true);
		}
		
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, nclicks);
		System.out.println("10 clicks: simclicks0: Chi2 value is: " + String.format("%.3f" , chi2));
		
		nclicks=20; 
		for (int i=0; i<nclicks; i++) {
			bitSet.set(i, true);
		}
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, nclicks);
		System.out.println("20 clicks: simclicks0: Chi2 value is: " + String.format("%.3f" , chi2));
		
		System.out.println("/*****BitSet tests******/");
		
		bitSet = new BitSet(); 
		bitSet.set(0, true);
		bitSet.set(1, true);
		bitSet.set(2, true);
		bitSet.set(3, true);
		bitSet.set(4, true);
		bitSet.set(5, true);
		
		//sim first ten clicks of overlapping tracks
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, 6);
		System.out.println("Bitset [" +MHTKernel.bitSetString(bitSet, 6)+"] Chi2 value is: " + String.format("%.3f" , chi2));
		
		bitSet = new BitSet(); 
		bitSet.set(0, true);
		bitSet.set(1, false);
		bitSet.set(2, false);
		bitSet.set(3, true);
		bitSet.set(4, false);
		bitSet.set(5, false);
		
		//sim first ten clicks of overlapping tracks
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, 6);
		System.out.println("Bitset [" +MHTKernel.bitSetString(bitSet, 6)+"] Chi2 value is: " + String.format("%.3f" , chi2));
		
		
		bitSet = new BitSet(); 
		bitSet.set(0, false);
		bitSet.set(1, true);
		bitSet.set(2, true);
		bitSet.set(3, false);
		bitSet.set(4, true);
		bitSet.set(5, true);
		
		//sim first ten clicks of overlapping tracks
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, 6);
		System.out.println("Bitset [" +MHTKernel.bitSetString(bitSet, 6)+"] Chi2 value is: " + String.format("%.3f" , chi2));
	
		bitSet = new BitSet(); 
		bitSet.set(0, false);
		bitSet.set(1, true);
		bitSet.set(2, true);
		bitSet.set(3, false);
		bitSet.set(4, true);
		bitSet.set(5, true);
		bitSet.set(6, false);
		bitSet.set(7, false);
		bitSet.set(8, false);
		bitSet.set(9, true);


		//sim first ten clicks of overlapping tracks
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, 10);
		System.out.println("Bitset [" +MHTKernel.bitSetString(bitSet, 10)+"] Chi2 value is: " + String.format("%.3f" , chi2));
		
		bitSet = new BitSet(); 
		bitSet.set(0, false);
		bitSet.set(1, true);
		bitSet.set(2, false);
		bitSet.set(3, false);
		bitSet.set(4, true);
		bitSet.set(5, false);
		bitSet.set(6, false);
		bitSet.set(7, false);
		bitSet.set(8, false);
		bitSet.set(9, true);


		//sim first ten clicks of overlapping tracks
		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, 10);
		System.out.println("Bitset [" +MHTKernel.bitSetString(bitSet, 10)+"] Chi2 value is: " + String.format("%.3f" , chi2));
		
//		/****Load new set of sim clicks****/
//		System.out.println("/*****Loading sim clicks 3******/");
//
//		simClicks.simClicks3(); // new sim clicks 
////		
//		nclicks=9;
//		bitSet = new BitSet(); 
//		for (int i=0; i<nclicks; i++) {
//			bitSet.set(i, true);
//		}
//		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, nclicks);
//		coasts = mhtChi2.getNCoasts(); 
//		System.out.println("simclicks1: 1 before coast: Chi2 value is: " + String.format("%.3f" , chi2) +  " ncoasts: " + coasts);
//		
//		nclicks=10; 
//		bitSet = new BitSet(); 
//		for (int i=0; i<nclicks; i++) {
//			bitSet.set(i, true);
//		}
//		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, nclicks);
//		coasts = mhtChi2.getNCoasts(); 
//		System.out.println("simclicks1: 2 click after coast: Chi2 value is: " + String.format("%.3f" , chi2) +  " ncoasts:  " + coasts);
//		
//		nclicks=8; 
//		bitSet = new BitSet(); 
//		for (int i=0; i<nclicks-2; i++) {
//			bitSet.set(i, true);
//		}
//		bitSet.set(nclicks-2, false);
//		bitSet.set(nclicks-1, false);
//
//		chi2 = calcChi2(simClicks.getSimClicks(), mhtChi2, bitSet, nclicks);
//		coasts = mhtChi2.getNCoasts();
//		System.out.println("simclicks1: 3 click with two coasts: " + String.format("%.3f" , chi2) +  " ncoasts:  " + coasts + " rawchi2: " + mhtChi2.getRawChi2() );
	}


	public static void main(String[] args) {
		testChiCalculation();
	}

}
