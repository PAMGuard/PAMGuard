package clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.ListIterator;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTGarbageBot;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTParams;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Provider;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.SimpleChi2VarParams;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2VarParams.BearingJumpDrctn;
import clickTrainDetector.clickTrainAlgorithms.mht.test.SimpleClickDataBlock;
import clickTrainDetector.clickTrainAlgorithms.mht.test.ExampleClickTrains;

/**
 * 
 * An MHT algorithm with all the hooks required for running in MATLAB. 
 * <p> 
 * This MHT algorithm works only with time, amplitude and bearing. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class MHTAlgorithmMAT {

	/**
	 * The MHT Kernel.
	 */
	protected MHTKernel<PamDataUnit> mhtKernel;

	/**
	 * The chi^2 calculator. 
	 */
	protected StandardMHTChi2Provider mhtChi2;

	/**
	 * Standard MHT parameters.
	 */
	private MHTParams pamMHTChi2Params = new MHTParams(); 

	/**
	 * Datablock to hold the detections to be analysed. 
	 */
	private SimpleClickDataBlock simpleClickDataBlock;

	/**
	 * The MHT Garbage bot.
	 */
	private MHTGarbageBot mhtGarbageBot = new MHTGarbageBot(); 

	/**
	 * Enable or disable the garbage bot. 
	 */
	private boolean enableGarbageBot = true; //this should be true!
	
	/**
	 * Classifiers for the click train detector. 
	 */
	private MHTClassifierMAT mhtClassiferMAT;

	/**
	 * Garbage count
	 */
	private int garbagecount = 0;

	//	/**
	//	 * Sample rate. Not really needed for this particular example. 
	//	 */
	//	private double sR = 500000; 

	public MHTAlgorithmMAT() {
		this(null);
	}

	/**
	 * Constructor for the MATLAB based algorithm. 
	 * @param mhTVar - the list of algorithms to enable. 
	 */
	public MHTAlgorithmMAT(boolean[] mhTVar) {
		simpleClickDataBlock= new SimpleClickDataBlock() ;

		mhtChi2 =  new StandardMHTChi2Provider(pamMHTChi2Params); 

		mhtKernel = new MHTKernel<PamDataUnit>(mhtChi2);

		mhtKernel.setMHTParams(pamMHTChi2Params.mhtKernal);

		if (mhTVar!=null) {
			((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).enable=mhTVar; 
		}
		//set up the classifier 
		mhtClassiferMAT = new MHTClassifierMAT();
	}
	
	/**
	 * Add a classifier to the algorithm 
	 * @param type - the type of classifier e.g. "Template", "Simple", "Bearing"
	 * @param clssfrsettings - the classifier settings object
	 */
	public void addClassifier(String type, CTClassifierParams clssfrsettings) {
		mhtClassiferMAT.addClassifier(type, clssfrsettings);
	}

	/**
	 * Get the StandardMHTChi2Params
	 * @return the StandardMHTChi2Params; 
	 */
	public StandardMHTChi2Params getStandardMHTParams(){
		return ((StandardMHTChi2Params) pamMHTChi2Params.chi2Params); 
	}


	/**
	 * Set which of the MHT variables are enabled
	 * 
	 * @param enabled
	 *            - array showing which chi2 variables are enabled- should be the
	 *            same size as the number of availble chi2 variables
	 */
	public void setMHTVarEnable(boolean[] enabled) {
		((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).enable=enabled; 
	}


	/**
	 * Set the error value for a MHT chi^2 parameter. 
	 * @param var - the parameter index.
	 * @param error - the error value to set. 
	 */
	public void setMHTVarSettings(int var, double error, double minErr) {
		//changed this because chi^2 parameters might be a superclass of SimpleChi2VarParams
		SimpleChi2VarParams varParams = (SimpleChi2VarParams) ((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).chi2Settings[var]; 
		varParams.error=error;
		varParams.minError=minErr;
		((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).chi2Settings[var]= varParams; 
	}

	/**
	 * Set some parameters for the MHT Kernel. Allows MATLAB to change params easily. 
	 * @param npruneback - the prune back value. 
	 * @param nprunestart - the minimum number of detections before prune back occurs. 
	 * @param nhold - the max number of tracks. 
	 * @param maxcoasts - the maximum allowed number for coasts for a track. 
	 */
	public void setMHTKernelParams(int npruneback, int nprunestart, int nhold, int maxcoasts) {
		
		pamMHTChi2Params.mhtKernal.nPruneback=npruneback;
		pamMHTChi2Params.mhtKernal.nPruneBackStart=nprunestart;
		pamMHTChi2Params.mhtKernal.nHold=nhold;
		pamMHTChi2Params.mhtKernal.maxCoast=maxcoasts; 

		mhtKernel.setMHTParams(pamMHTChi2Params.mhtKernal); 
	}
	
	
	/**
	 * Set settings for the standard MHT parameters. Convenience function for MATLAB to easily
	 * set parameters. 
	 * @param maxICI - the maximum allowed ICI
	 * @param newtrackpenalty - the new track penalty factor. 
	 * @param coastpenalty - the coast penalty. 
	 * @param longiciexponenet - bonus factor for long ICI tracks.
	 * @param longtrackexponent - bonus factor for long ICI tracks.
	 */
	public void setStandardMHTParams(double maxICI, double newtrackpenalty, double coastpenalty, 
			double  longiciexponenet, double longtrackexponent) {
		
		getStandardMHTParams().maxICI = maxICI; 
		getStandardMHTParams().newTrackPenalty = newtrackpenalty; 
		getStandardMHTParams().coastPenalty = coastpenalty; 
		
		getStandardMHTParams().lowICIExponent = longiciexponenet; 
		getStandardMHTParams().longTrackExponent = longtrackexponent; 

//		getStandardMHTParams().lowTrackICIBonus = iciBonus; 
//		getStandardMHTParams().longTrackBonus = longTrackBonus; 
	}



	/**
	 * Add a series of clicks to the data block to be analysed. 
	 * The input for simple clicks is time (seconds), amplitude (dB re 1uPa) and optionally bearing. 
	 * @param simpleClicks a list of clicks.
	 * @param sR - the sample rate in samples per second. 
	 */
	public void setClicks(double[][] simpleClicks, float sR) {
		simpleClickDataBlock.clearAll();
		simpleClickDataBlock.setSampleRate((float) sR, true);
		ArrayList<SimpleClick> clicks=new ArrayList<SimpleClick>(); 
		for (int i=0; i<simpleClicks.length; i++) {
			if (simpleClicks[i].length==2) {
				clicks.add(new SimpleClick((int) i, simpleClicks[i][0], simpleClicks[i][1], sR)); 
			}
			else if (simpleClicks[i].length==3) {
				clicks.add(new SimpleClick((int) i, simpleClicks[i][0], simpleClicks[i][1], simpleClicks[i][2], sR)); 
			}
		}
		simpleClickDataBlock.addPamData(clicks); 
	}


	/**
	 * Add a series of clicks to the data block to be analysed. 
	 * The input for simple clicks is time (seconds), amplitude (dB re 1uPa) and optionally bearing. 
	 * @param simpleClicks a list of clicks.
	 * @param sR - the sample rate in samples per second. 
	 */
	public void setClicks(Object[][] simpleClicks, float sR) {
		simpleClickDataBlock.clearAll();
		simpleClickDataBlock.setSampleRate((float) sR, true);
		ArrayList<SimpleClick> clicks=new ArrayList<SimpleClick>(); 
		for (int i=0; i<simpleClicks.length; i++) {
			if (simpleClicks[i].length==3) {
				clicks.add(new SimpleClick((Integer) simpleClicks[i][0], (Double) simpleClicks[i][1], (Double) simpleClicks[i][2], sR)); 
			}
			else if (simpleClicks[i].length==4) {
				clicks.add(new SimpleClick((Integer)  simpleClicks[i][0], (Double)  simpleClicks[i][1], (Double)  simpleClicks[i][2], (Double)  simpleClicks[i][3], sR)); 
			}
			else if (simpleClicks[i].length==5) {
				clicks.add(new SimpleClick((Integer)  simpleClicks[i][0], (Double)  simpleClicks[i][1], (Double)  simpleClicks[i][2], (Double)  simpleClicks[i][3], (double[]) simpleClicks[i][4], sR)); 
			}
		}
		simpleClickDataBlock.addPamData(clicks); 
	}

	
	/**
	 * Print out a list of simple clicks 
	 */
	public void printSimpleClickData() {
		ListIterator<SimpleClick> iterator = simpleClickDataBlock.getListIterator(0);
		SimpleClick click; 
		while (iterator.hasNext()){
			click=iterator.next(); 
			System.out.println(String.format("Time:  %.5f, Amplitude:  %.1f", 
					(click.getStartSample()/simpleClickDataBlock.getSampleRate()), click.getAmplitudeDB()));
			if (click.getLocalisation()!=null) {
				System.out.print(String.format(" Bearing:  %.2f ", click.getLocalisation().getAngles()[0]));
			}
		}
	}



	/**
	 * Runs the whole algorithm based on data in the SimpleClickDataBlock. 
	 */
	public void run() {
		mhtKernel.clearKernel();
		ListIterator<SimpleClick> iterator = simpleClickDataBlock.getListIterator(0);
		SimpleClick click; 
		while (iterator.hasNext()){

			click=iterator.next(); 

			//optimise the MHTKernel for memory 
			if (this.enableGarbageBot) {
				this.mhtGarbageBot.checkCTGarbageCollect(click, mhtKernel); 
			}

			//add the click.
			this.addSimpleClick(click=iterator.next()); 
		}
	}

	/**
	 * Get list iterator from the data block starting at 0
	 * @return the list iterator from the data block. 
	 */
	public ListIterator<SimpleClick> getSimpleClickIterator(){
		return simpleClickDataBlock.getListIterator(0);
	}



	/**
	 * Add a simple click to the click train. This must be sequential. 
	 */
	public void addSimpleClick(SimpleClick simpleClick) {
		
		//optimise the MHTKernel for memory 
		if (this.enableGarbageBot) {
			this.mhtGarbageBot.checkCTGarbageCollect(simpleClick, mhtKernel); 
		}
		garbagecount++;
		
		mhtKernel.addDetection(simpleClick);
	}

	/*****Some easy functions for MATLAB to hook into results*****/

	/**
	 * Get the number of active branches. These are the unconfirmed branches which are
	 * still in the possibility mix but with prune back- Usually used for plotting.  
	 * @return the number of active branches. 
	 */
	public int getActiveBranchSize() {
		if (mhtKernel.getActiveTracks()==null) return 0; 
		return mhtKernel.getActiveTracks().size(); 
	}

	/**
	 * Get a active array from the MHTKernel.
	 * @param n - the possibility index
	 * @return a boolean array of possibilities starting from the reference detection. 
	 */
	public boolean[] getActiveBranch(int n) {
		return bitSet2Array(mhtKernel.getActiveTracks().get(n).trackBitSet, mhtKernel.getKCount());	
	}


	/**
	 * Get the number of confirmed branches. 
	 * @return the number of confirmed branches. 
	 */
	public int getConfirmedBranchSize() {
		return mhtKernel.getNConfrimedTracks(); 
	}

	/**
	 * Get a confirmed track from the MHTKernel.
	 * @param n - the confirmed track index
	 * @return a boolean array of confirmed detections starting from the reference detection. 
	 */
	public boolean[] getConfirmedBranch(int n) {
		return bitSet2Array(mhtKernel.getConfirmedTrack(n).trackBitSet, mhtKernel.getKCount());	
	}
	
	
	/**
	 * Get a CTDataUnit form the confirmed track. Also adds classification information to the CTDataUnit 
	 * if classifier are present.
	 * @param n - the confirmed track index. 
	 * @return the CTDF
	 */
	public CTDataUnit getCTDataUnit(int n) {
		
		if (mhtKernel.getConfirmedTrack(n)!=null) {
			boolean[] clks = bitSet2Array(mhtKernel.getConfirmedTrack(n).trackBitSet, mhtKernel.getKCount());	
			// get the simple clicks

			ListIterator<PamDataUnit> simpleClks = mhtKernel.getDataUnits().listIterator();

			CTDataUnit ctDataUnit = new CTDataUnit(n);

			//set the chi2 value
			ctDataUnit.setCTChi2(new Double(getConfirmedBranchChi2(n))); 

			ctDataUnit.checkAverageWaveformInfo();

			//add the detections to the CT data unit.
			int i=0; 
			SimpleClick simpleClk;
			while (simpleClks.hasNext()){
				simpleClk = (SimpleClick) simpleClks.next();
				if (clks[i]) {
					ctDataUnit.addSubDetection(simpleClk);
				}
				i++;
			}

			//set the algorithm info
			ctDataUnit.setCTAlgorithmInfo(mhtKernel.getConfirmedTrack(n).chi2Track.getMHTChi2Info());

			//classify the ct data unit. 
			this.classifyCTDataUnit(ctDataUnit); 

			return ctDataUnit;
		}
		else return null; 
		
	}

	
	/**
	 * Classify a CTDataUnit
	 * @param ctDataUnit - the CTDataUnit to classify 
	 */
	public void classifyCTDataUnit(CTDataUnit ctDataUnit) {
		this.mhtClassiferMAT.classify(ctDataUnit);
	}


	/**
	 * Get a confirmed track from the MHTKernel.
	 * @param n - the possibility index
	 * @return a boolean array of confirmed detections starting from the reference detection. 
	 */
	public double getConfirmedBranchChi2(int n) {
		return mhtKernel.getConfirmedTrack(n).chi2Track.getChi2();
	}


	/**
	 * Get the size of possibility matrix. 
	 */
	public int getPossibilitySize() {
		return mhtKernel.getPossibleTracks().size(); 
	}

	/**
	 * Get a possibility array from the MHTKernel.
	 * @param n - the possibility index
	 * @return a boolean array of possibilities starting from the reference detection. 
	 */
	public boolean[] getPossibility(int n) {
		return bitSet2Array(mhtKernel.getPossibleTracks().get(n).trackBitSet, mhtKernel.getKCount());	
	}

	/**
	 * Get the chi^2 values for a possibility from the MHTKernel.
	 * @param n - the possibility index.
	 * @return a chi^2 value for the possibility. 
	 */
	public double getPossibilityChi2(int n) {
		return mhtKernel.getPossibleTracks().get(n).chi2Track.getChi2(); 
	}

	/**
	 * Convert a BitSet to a boolean array.
	 * @param bitSet - the BitSet to convert.
	 */
	public boolean[] bitSet2Array(BitSet bitSet, int kcount){
		boolean[] bitarrayB= new boolean[kcount]; 
		for (int i=0; i<kcount; i++) {
			bitarrayB[i]=bitSet.get(i); 
		}
		return bitarrayB; 
	}


	/**
	 * Convert a boolean array to a BitSet. 
	 * @param array, the array to convert. 
	 */
	public BitSet array2BitSet(boolean[] array){
		BitSet bitset= new BitSet(); 
		for (int i=0; i<array.length; i++) {
			bitset.set(i, array[i]);
		}
		return bitset; 
	}

	/**
	 * Reset the array. 
	 */
	public void reset() {
		mhtKernel.clearKernel();	
	}


	/**
	 * Clear the MHT kernel. Also clears the current datablock. 
	 */
	public void clearKernel() {
		mhtKernel.clearKernel();	
		simpleClickDataBlock.clearAll();
	}

	/**
	 * Get the MHT Kernel. 
	 * @return the MHTT kernel
	 */
	public MHTKernel<PamDataUnit> getMHTKernel() {
		return this.mhtKernel;
	}

	
	/**
	 * Confirm the remaining tracks. This should be called at the end of the an analysis run.
	 */
	public void confirmRemainingTracks() {
		mhtKernel.confirmRemainingTracks();
	}

	/**
	 * Get the chi^2 calculator. 
	 * @return the chi2 calculator. 
	 */
	public StandardMHTChi2Provider getMHTChi2() {
		return this.mhtChi2;
	}

	/**
	 * Convenience function to set some parameters. 
	 * @param nPruneback - the prune back
	 * @param nHold - the number of holds. 
	 * @param nPruneStart - the prune start. 
	 * @param maxCoasts - max coasts.
	 * @param maxICI - the maximum inter-click-interval.
	 *
	 */
	public void setParams(int nPruneback, int nHold, int nPruneStart, int maxCoasts, double maxICI) {		
		//MHT Kernel
		mhtKernel.getMHTParams().nPruneback=nPruneback; 
		mhtKernel.getMHTParams().nHold=nHold; 
		mhtKernel.getMHTParams().nPruneBackStart=nPruneStart; 
		mhtKernel.getMHTParams().maxCoast=maxCoasts; 

		//max inter-click-interval
		pamMHTChi2Params.chi2Params.maxICI=maxICI; 
	}



	/**
	 * Print the settings for the MHT algorithms to the console. 
	 */
	public void printSettings() {

		//print thge chi2 params

		//MHT kernel params
		mhtKernel.getMHTParams().printSettings();
		((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).printSettings();

	}


	/**
	 * Test the algorithm. 
	 */
	public void testRunAlgorithm() {
		ExampleClickTrains simClicks = new ExampleClickTrains(); 

		simClicks.simClicks1();; //load up MATLAB data. 
		setMHTVarEnable(new boolean[] {true, true, true, false});

		System.out.println("Start Algorithm");
		ListIterator<SimpleClick> iterator = simClicks.getSimClicks().getListIterator(0);
		SimpleClick click; 
		while (iterator.hasNext()){
			this.addSimpleClick(click=iterator.next()); 
		}
		System.out.println("Number of possible tracks: " + this.getMHTKernel().getPossibleTracks().size());
		System.out.println("Stop Algorithm");
	} 


	/**
	 * @return the simpleClickDataBlock
	 */
	public SimpleClickDataBlock getSimpleClickDataBlock() {
		return simpleClickDataBlock;
	}
	


	/**
	 * Checks whether the garbage bot is enabled
	 * @return true if garbage bot is enabled
	 */
	public boolean isEnableGarbageBot() {
		return enableGarbageBot;
	}

	/**
	 * Set whether the garbage bot is enabled
	 * @param enableGarbageBot - true to enable garbage bot
	 */
	public void setEnableGarbageBot(boolean enableGarbageBot) {
		this.enableGarbageBot = enableGarbageBot;
	}
	
	/**
	 * Set the maximum bearing  jump. This is a hard cutoff in the maximum a click
	 * train can jump in bearing- prevents tracks going crazy for sperm whales and other slowly changing bearings 
	 * especially in quiet environments. 
	 * @param jump - the max bearing jump in DEGREES
	 */
	public void setMaxBearingJump(double jump) {
		BearingChi2VarParams varParams = (BearingChi2VarParams) ((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).chi2Settings[2]; 
		varParams.maxBearingJump = Math.toRadians(jump);
		varParams.bearingJumpEnable=true;
		varParams.bearingJumpDrctn=BearingJumpDrctn.BOTH;
		((StandardMHTChi2Params) pamMHTChi2Params.chi2Params).chi2Settings[2]= varParams; 
	}
	
	
	/**
	 * Main class to quickly check the algorithm works sort of. 
	 * @param args - input arguments
	 */
	public static void main(String[] args) {
		MHTAlgorithmMAT algorithm = new MHTAlgorithmMAT(); 
		//		double[][] test = new double[][] {{1,100}, {2,120}, {3,150}, {3.1, 160}};
		//		algorithm.setClicks(test, 500000); 

		algorithm.testRunAlgorithm(); 
	}


}
