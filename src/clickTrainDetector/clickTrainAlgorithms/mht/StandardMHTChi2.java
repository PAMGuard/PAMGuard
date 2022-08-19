package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.ArrayList;
import java.util.BitSet;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter.ElectricalNoiseFilter;
import clickTrainDetector.clickTrainAlgorithms.mht.electricalNoiseFilter.SimpleElectricalNoiseFilter;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.AmplitudeChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.BearingChi2Delta;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.CorrelationChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIData;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIManager;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.LengthChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.MHTChi2Var;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.PeakFrequencyChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.TimeDelayChi2Delta;

/**
 * The chi^2 calculator for the MHTKernal. This calculates the chi^2 value for a particular 
 * track based on a series of MHTChi2Var classes that look for slowly changing values over time 
 * for a particular variable e.g. amplitude. 
 * <p>
 * There are also various penalty factors added intended to favour lower ICI values and 
 * penalise new tracks being created. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings({"rawtypes"})
public class StandardMHTChi2 implements MHTChi2<PamDataUnit>, Cloneable {

	/**
	 * Flag for verbosity i.e. how much stuff to print to the console. 
	 */
	public static int verbosity=0; 

	/**
	 * The chi^2 value of the last calculated tracks.
	 */
	private double chi2 = Double.MAX_VALUE;  

	/**
	 * The number of coasts of the last calculated tracks. 
	 */
	private int nCoasts;

	/**
	 * The raw chi2. This is simply the chi2 value calculated from the the
	 * combined chi2 variables without any of the penalty factors of coasts etc 
	 * added. 
	 */
	private double rawChi2;


	/////Chi^2 metrics/////

	/**
	 *The maximum chi2 value for a track
	 */
	private double maxChi=200000000000000000.;


	/**
	 * Refrence to the chi2 provider. 
	 */
	private StandardMHTChi2Provider standardMHTChi2Provider;

	/**
	 * MHTChi2Vars; 
	 */
	private ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars;

	/**
	 * A copy of the last IDI data - means we don't have to keep going back into IDI manager
	 */
	private IDIData lastIDIData; 


	/**
	 * Electrical noise filter. 
	 */
	private ElectricalNoiseFilter electricalNoiseFilter; 

	/**
	 * Multiplier for the penalty factor applied to track length. 
	 */
	//private double trackLengthMultiplier = 4; 

	//////////////////////


	public StandardMHTChi2(StandardMHTChi2Provider standardMHTChi2Provider){
		this.standardMHTChi2Provider=standardMHTChi2Provider; 
		//this.channelBitMap=channelBitmap; 
		this.mhtChi2Vars=createChi2Vars(); 
		//apply settings to the chi2 variables
		standardMHTChi2Provider.getParams().restoreSettings(this.mhtChi2Vars); 
		//create electrical noise filter
		electricalNoiseFilter = new SimpleElectricalNoiseFilter(this); 
	}


	public StandardMHTChi2(StandardMHTChi2Params pamMHTChi2Params, MHTKernelParams mHTkernalParams) {
		this.mhtChi2Vars=createChi2Vars(); 
		//CREATE THE SETTINGS
		this.standardMHTChi2Provider= new StandardMHTChi2Provider(pamMHTChi2Params,  mHTkernalParams);
		//create electrical noise filter
		electricalNoiseFilter = new SimpleElectricalNoiseFilter(this);
	}

	/**
	 * Create a set of chi^2 variables. 
	 * @return a list of chi2 varibales. 
	 */
	public static ArrayList<MHTChi2Var<PamDataUnit>> createChi2Vars() {
		ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars = new ArrayList<MHTChi2Var<PamDataUnit>>(); 
		/******Add new chi2 vars here*******/
		mhtChi2Vars.add(new IDIChi2()); 
		mhtChi2Vars.add(new AmplitudeChi2()); 
		mhtChi2Vars.add(new BearingChi2Delta()); 
		mhtChi2Vars.add(new CorrelationChi2()); 
		mhtChi2Vars.add(new TimeDelayChi2Delta()); 
		mhtChi2Vars.add(new LengthChi2()); 
		mhtChi2Vars.add(new PeakFrequencyChi2()); 
		/**********************************/
		return mhtChi2Vars; 
	}

	@Override
	public void update(PamDataUnit newDataUnit, TrackBitSet<PamDataUnit> bitSet, int kcount) {
		updateChi2(newDataUnit, bitSet, kcount, getMHTKernelParams().nPruneback);
	}


	/**
	 * Update the chi2 value based on a new data unit and new track bitset. 
	 * @param newDataUnit - the most recent data unit (not necessarily in the track)
	 * @param trackBitSet - the new track bit set.
	 * @param kcount - the total number of data units so far considered (because bitsets are always length of N*32)
	 * @param nPruneback - the prune back value from the MHT kernel
	 */
	private synchronized void updateChi2(PamDataUnit newDataUnit, TrackBitSet trackBitSet, int kcount, int nPruneback) {

		BitSet bitSet = trackBitSet.trackBitSet;

		int bitcount = MHTKernel.getTrueBitCount(bitSet); 
		
		/**
		 * Calculate the chi2 values from all the different chi2 variable 
		 * functions.
		 */
		double chi2=0; 
		for (int i=0; i<getMhtChi2Vars().size(); i++) {
			if (getChi2Params().enable[i]) {
				chi2+=getMhtChi2Vars().get(i).updateChi2(newDataUnit, bitSet, bitcount, kcount, getIDIManager()); 
				//System.out.println("StandardMHTChi2: Chi2 for " +getMhtChi2Vars().get(i).getName() + " is: " + chi2); 
//				if (Double.isNaN(chi2)) {
//					chi2=0;
//					System.err.println("Chi2 is NaN for MHT var!: " + getMhtChi2Vars().get(i).getName() + " bitcount: " + bitcount); 
//				}
			}
		}
		this.rawChi2=chi2; 
		

		//long time2 = System.nanoTime();
		int nCoasts = (int) calcNCoasts(newDataUnit, bitSet, bitcount, kcount);

		if (bitcount<2 || kcount<2 || Double.isNaN(rawChi2)) {
			//either no data units or the track is not more than three data units. If 
			//this is the case then there is not a track
			this.chi2=maxChi; 
			this.nCoasts=0; //important this remains zero..??.
			return; 
		}
		
		

		// add extra penalties and bonuses for longer tracks etc. 
		chi2 = addChi2TrackPenalties(rawChi2, nCoasts, bitSet, bitcount, kcount, nPruneback);

		//check to see whether the clicks train detector might be tracking electrical noise. 
		if (getChi2Params().useElectricNoiseFilter) {
			chi2 = electricalNoiseFilter.addChi2Penalties(chi2, trackBitSet, bitcount, kcount, nPruneback); 
		}

		//long time4 = System.nanoTime();

//		if (verbosity>0 || Double.isNaN(chi2)) {
//			System.out.println("rawchi2: "+rawChi2 + " ncoasts: " + nCoasts + " chi2: " + chi2 + " kcount: " + kcount + " bitcount: " + bitcount);
//		}
		//PamArrayUtils.printArray(getIDIManager().getTimeSeries(bitSet)); 

//				if (verbosity>0) System.out.println("TimeTaken: raw chi2 : " + (time2-time1)/1000000. 
//						+ " nCoasts : " + (time3-time2)/1000000. + " Penalties " +  (time4-time3)/1000000.); 

		this.chi2=chi2; 
		this.nCoasts=(int) nCoasts; 

	}

	/**
	 * Calculate the number of coasts in a track. This is the number of detections
	 * which have been missed at the end of the track assuming the ICI average has
	 * remained the same.
	 * 
	 * @param bitSet -= bitSet representing the track
	 * @param kcount - the total number of track units (used and unused)
	 * @return the number of track coasts
	 */
	private double calcNCoasts(PamDataUnit newdataUnit, BitSet bitSet, int bitcount, int kcount) {

		/**
		 * Work out the number of coasts. 
		 * The coasting is based on the number of missed detection. As the 
		 */

		//calculate the difference between the last value. Remember indexing in java; second last unit is size of array -2, not -1
		double nCoasts =0 ;
		if (bitcount>1) {
			getIDIManager().setForceCalc(true);
			this.lastIDIData = getIDIManager().getIDIStruct(bitSet);
			
			//System.out.println("Time diff: " + lastIDIData.timeDiff + "  " + lastIDIData.medianIDI); 
			
			nCoasts=(int) Math.floor(lastIDIData.timeDiff/Math.abs(lastIDIData.medianIDI)); 
		}
		else if (bitcount==1) {
			//this stops a single units being stuck in the back of the probability matrix. 
			nCoasts = (int) Math.floor(((newdataUnit.getTimeMilliseconds()-getIDIManager().getFirstDataUnit().getTimeMilliseconds())/1000.
					-getIDIManager().getLastTime(bitSet))/this.getChi2Params().maxICI); 
		}
		
		//System.out.println("nCoasts: " + nCoasts); 

		return nCoasts;
	}

	/**
	 * 
	 * Add penalties to the raw chi^2 value. These penalties are based on the length of track, minimum allowed ICI etc. 
	 * @param rawChi2 - the rawChi2 value calculated form the MHTCHi2Var variables. 
	 * @param nCoasts - the total number of coasts in the track
	 * @param bitSet - the track bitset.
	 * @param kcount - the track kcount,  i.e. total number of data units considered. 
	 * @param nPruneBack - the pruneback value from the MHTKernel
	 * @return updated chi2 value. 
	 */
	private double addChi2TrackPenalties(double rawChi2, double nCoasts, 
			BitSet bitSet, int bitcount, int kcount, int nPruneBack) {

		long time1 = System.nanoTime();

		double chi2=rawChi2; 
		int nDataUnits = bitcount; 

		long time2 = System.nanoTime();

		if (this.lastIDIData==null) {
			getIDIManager().setForceCalc(true); //skips needing to compare large bitsets. 
			this.lastIDIData = getIDIManager().getIDIStruct(bitSet);
		}

		long time3 = System.nanoTime();


		//now add penalty factor due to coasts
		chi2=chi2+getChi2Params().coastPenalty*nCoasts;
		
//		if (Double.isNaN(chi2)){
//			System.err.println("Chi2 is NaN after coasts penalty"); 
//		}
		
		
//		Debug.out.println("After Long track bonus penalty: " + chi2);
		/**
		 * Add a minor penalty factor for a new track. 
		 */
		if (kcount-1-nPruneBack>=0) {
			BitSet pruneBitSet = bitSet.get(0, kcount-1-nPruneBack); 
			//check if there any detections previous to pruneback.
			int nDet=MHTKernel.getTrueBitCount(pruneBitSet); 
			if (nDet<=getChi2Params().newTrackN) {
				chi2=chi2+getChi2Params().newTrackPenalty;
			}
//			Debug.out.println("After New track penalty: " + chi2);
		}

//		if (Double.isNaN(chi2)){
//			System.err.println("Chi2 is NaN after new track  penalty"); 
//		}

//		Debug.out.println("After coast: " + chi2 + " nCoasts: " + nCoasts);

		/**
		 * Add a minor penalty factor to select lower ICI's. This prevents aliased ICI's being 
		 * selected
		 */
		double maxICIVal=PamArrayUtils.max(lastIDIData.idiSeries); //maximum single ICI value. 

		long time4 = System.nanoTime();

		/**
		 * The click train gets a big chi2 hit if
		 * 1) The median ICI is greater than the maxICI
		 * 3) the maximum ICI is greater than number coasts +1 times the median ICI. 
		 */
		if (lastIDIData.medianIDI>getChi2Params().maxICI
				|| maxICIVal>(standardMHTChi2Provider.getMHTKernelParams().maxCoast+1)*lastIDIData.medianIDI) { 
			//the median ICI is greater than max ICI value ..or
			//the maximum ICI value in the list is more than 5 times the median ICI
			//the maximum ICI value in the list is more than the maximum allowed ICI value. 
			//add a very significant penalty factor. 
			chi2=chi2+StandardMHTChi2Params.JUNK_TRACK_PENALTY;
//			Debug.out.println("After junk track bonus: " + chi2);
		}
		else if (nDataUnits>getChi2Params().newTrackN) {
			//need to ensure this is not used with less than three data units or messes 
			//everything up. 
			
			/**
			 * A bit of a nudge towards lower ICI values to prevent aliasing of the click train. Note that
			 * maxICI divisor is meaningless because all tracks are divided by it and x^2 is only used
			 * for comparison in detection stage. The reason we use is to keep the X^2 close to it's original value
			 * i.e. Multiplication factors are only small nudges between 0 and 1...ish. 
			 */
			//19/10/2019 <- this really seems to mess up results. I think this is because 
			chi2 = chi2*(Math.pow((lastIDIData.medianIDI/getChi2Params().maxICI), getChi2Params().lowICIExponent));
			
			double totalTracktime = PamArrayUtils.sum(lastIDIData.idiSeries); 
			
			//System.out.println("Total track time: " + totalTracktime);
			
			/**
			* Add a nudge towards longer tracks (remember to cast to double when dividing). Note that 
			* kcount coefficient is meaningless because all tracks are multiplied by it and x^2 is only used
			* for comparison in detection stage. The reason we use is to keep the X^2 close to it's original value
			* i.e. Multiplication factors are only small nudges between 0 and 1...ish.
			 */
			//19/03/2020 - fixed a bug; Was multiplying instead of dividing - as such long tracks were being 
			//discriminated against causing fragmentation...ooops
			chi2=chi2/Math.pow(totalTracktime/getIDIManager().getTotalTime(),getChi2Params().longTrackExponent);
			//chi2=chi2/Math.pow(bitSet.cardinality(),getChi2Params().longTrackExponent);
			
			
		}
		
		
		long time5 = System.nanoTime();
		

		if (verbosity>1) {
			System.out.println("TimeTaken: time21 : " + (time2-time1)/1000000. 
					+ " time32 : " + (time3-time2)/1000000. + " time43 " +  (time4-time3)/1000000.
					+ " time54 " +  (time5-time4)/1000000. ); 
		}


		//		if (verbosity>0) {
		//			Debug.out.println("After long track bonus: " + chi2);
		//		}


		//			02/01/2018- tested this and didn't seem to make algorithm much better. 
		//			/**
		//			 * Give  a track a bit of bonus for being slightly longer. i.e. take 
		//			 */
		//			int trackLength=dataUnits.size(); 
		//			int startsAt=bitSet.nextSetBit(0); //work out the start of the track.
		//			chi2=chi2+(trackLengthMultiplier*(trackLength-startsAt)/(double) trackLength);

		
		//All done. Set the values. 
		//set the chi2 values. 
		//long time3=System.nanoTime();
		//Debug.out.println("Track chi2: " + chi2 + " " + bitcount ); 

		return chi2; 
	}



	//	/**
	//	 * Check if new track metrics need calculated and if so calculate them. 
	//	 * @param detection0 - the first data unit in the track. 
	//	 * @param bitSet - the bit set representing the track - this is still useful for knowing how many data units are not included in track. 
	//	 * @param kcount - the total kcount - total number of data units processed.
	//	 * @param nPruneback - the pruneback value for the track. 
	//	 */
	//	@Deprecated
	//	protected void calcTrackMetrics(PamDataUnit detection0, BitSet bitSet, int kcount, int nPruneback) {
	//
	//		//System.out.println("1 The chi2 value is : " + chi2 + "  " + dataUnits.size());
	//
	//		if (MHTKernel.getTrueBitCount(bitSet, kcount)<3) {
	//			//either no data units or the track is not more than three data units. If 
	//			//this is the case then there is not a track
	//			this.chi2=maxChi; 
	//			this.nCoasts=0; 
	//			return; 
	//		}
	//
	//		//this function is very processor intensive so it's critical that it is called as few times as possible. 
	//		if (detection0!=lastDetection0 || !bitSet.equals(lastBitSet)) {
	//			//data units in the track. 
	//			TrackDataUnits trackDataUnits =  MHTChi2.getTrackDataUnits(detection0, bitSet, channelBitMap, kcount);
	//			calcTrackMetrics(trackDataUnits, bitSet, kcount,  nPruneback);
	//			this.lastDetection0=detection0;
	//			this.lastBitSet=bitSet;
	//		}
	//
	//	}



	/**
	 * Check if new track metrics need calculated and if so calculate them. 
	 * @param trackDataUnits - a list of  data unit included in the track;
	 * @param bitSet - the bit set representing the track - this is still useful for knowing how many data units are not included in track. 
	 * @param kcount - the total kcount - toal number of data units processed.
	 * @param nPruneback - the pruneback value for the track. 
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void calcTrackMetrics(TrackDataUnits trackDataUnits, BitSet bitSet, int kcount, int nPruneback) {
		//		long time1=System.nanoTime(); 
		//		long time2 = 0;

		int bitcount = MHTKernel.getTrueBitCount(bitSet, kcount); 

		ArrayList<PamDataUnit> dataUnits = trackDataUnits.dataUnits;

		//time2=System.nanoTime(); 
		if (verbosity>1){
			System.out.println("----:Data units and bit set:----");
			System.out.print("BitSet: ");
			System.out.println(MHTKernel.bitSetString(bitSet, kcount));
			for (int i=0; i<dataUnits.size(); i++) {
				System.out.println(" i: " + i + " time (millis): "+ 
						(dataUnits.get(i).getTimeMilliseconds()-dataUnits.get(0).getTimeMilliseconds()) + 
						" time (samples): " + (dataUnits.get(i).getStartSample()-dataUnits.get(0).getStartSample()) +
						" amplitude: "+ dataUnits.get(i).getAmplitudeDB());
			}
		}


		//do the ICI calculations
		getIDIManager().setDataUnitList(dataUnits, getChi2Params().useCorrelation);


		//System.out.println("2 The chi2 value is : " + chi2);

		/**
		 * Calculate the chi2 values from all the different chi2 variable 
		 * functions.
		 */
		for (int i=0; i<getMhtChi2Vars().size(); i++) {
			if (getChi2Params().enable[i]) {
				chi2+=getMhtChi2Vars().get(i).calcChi2(dataUnits, getIDIManager()); 
			}
		}
		double rawChi2=chi2;

		double nCoasts= calcNCoasts(trackDataUnits.dataUnits.get(trackDataUnits.dataUnits.size()-1), bitSet, bitcount,  kcount);

		double chi2 = addChi2TrackPenalties( rawChi2,  nCoasts,  bitSet, bitcount,  kcount,  nPruneback);

		//set the chi2 values. 
		this.rawChi2=rawChi2; 
		this.chi2=chi2; 
		this.nCoasts=(int) nCoasts; 
		//long time3=System.nanoTime(); 
		//System.out.println("Chi2: time total: " + ((time3-time1)/1000000.) + " find data units: " + (time2-time1)/1000000.);
	}

	@Override
	public double getChi2(int pruneback) {
		// TODO Auto-generated method stub
		//Will eventually need to implement this. Will require a list of previous chi2 values. 
		return 0;
	}


	@Override
	public double getChi2() {
		//		calcTrackMetrics(currentDetection, bitSet, kcount, mHTkernal.nPruneback);
		return chi2; 
	}


	@Override
	public int getNCoasts() {
		//		calcTrackMetrics(currentDetection, bitSet, kcount, mHTkernal.nPruneback);
		return nCoasts;
	}

	/**
	 * Gte the raw chi^2 value for the last calculated track. the raw chi^2 value is the chi^2 values 
	 * calculated from the enabled MHTChiVars. It has no additional penalty or bonus factors added. 
	 * @return the raw chi^2 value form the last calculated track. 
	 */
	public double getRawChi2() {
		return rawChi2;
	}

	//	/**
	//	 * Set the channel map. 
	//	 * @param channelBitMap - the channel map of detection to use. 
	//	 */
	//	public void setChannelMap(int channelBitMap2) {
	//		this.channelBitMap=channelBitMap2; 
	//	}

	/**
	 * Get the verbosity level. This shows the amount of print out statemnts. 
	 * @return the verbosity
	 */
	public int getVerbosity() {
		return verbosity;
	}

	/**
	 * Set the verbosity
	 * @param verbosity
	 */
	public void setVerbosity(int verbosity) {
		this.verbosity = verbosity;
	}

	/**
	 * Get the ICI Manager 
	 * @return ICI manager. 
	 */
	private IDIManager getIDIManager() {
		return this.standardMHTChi2Provider.getIDIManager(); 
	}

	/**
	 * Get the chi^2 parameters. 
	 * @retur the chi2 parameters. 
	 */
	private StandardMHTChi2Params getChi2Params() {
		return this.standardMHTChi2Provider.getParams(); 
	}

	/**
	 * Get the MHT Kernel parameters. 
	 * @return the MHT Kernel parameters. 
	 */
	private MHTKernelParams getMHTKernelParams() {
		return this.standardMHTChi2Provider.getMHTKernelParams(); 
	}


	/**
	 * Print chi^2 calculation settings. 
	 */
	public void printSettings() {
		this.standardMHTChi2Provider.getPamMHTChi2Params().printSettings();
	}

	public void restoreSettingsPane() {

	}

	/**
	 * List of current chi2 variables. 
	 * @return list of current chi2 variables. 
	 */
	public ArrayList<MHTChi2Var<PamDataUnit>> getMhtChi2Vars() {
		if (mhtChi2Vars==null) mhtChi2Vars = createChi2Vars();
		return mhtChi2Vars;
	}

	/**
	 * Set the MHT chi^2 variables. 
	 * @param mhtChi2Var - list of mht chi2 variables. 
	 */
	public void setMHTChi2Var(ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars) {
		this.mhtChi2Vars=mhtChi2Vars; 
	}

	/**
	 * Clone the individual MHT chi^2 variables. 
	 */
	private void cloneMHTVars() {
		ArrayList<MHTChi2Var<PamDataUnit>> mhtChi2Vars 
		= new ArrayList<MHTChi2Var<PamDataUnit>>(); 
		for (int i =0; i<this.mhtChi2Vars.size(); i++) {
			mhtChi2Vars.add(this.mhtChi2Vars.get(i).clone()); 
		}
		this.mhtChi2Vars=mhtChi2Vars; 
	}


	@Override
	public MHTChi2<PamDataUnit> cloneMHTChi2() {
		try {
			return this.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*****************************/

	//Modified clone() method in Employee class
	@Override
	protected StandardMHTChi2 clone() throws CloneNotSupportedException {
		StandardMHTChi2 cloned = (StandardMHTChi2) super.clone();
		cloned.cloneMHTVars(); 
		cloned.electricalNoiseFilter = new SimpleElectricalNoiseFilter(this); 
		//System.out.println("StandardMHTChi2: Cloned: CLONED: " +cloned);
		return cloned;
	}


	/**
	 * Reset the mhtchi2. This will also reset the associated provider. 
	 */
	public void clear() {
		this.standardMHTChi2Provider.clear();
		for (int i=0; i<mhtChi2Vars.size(); i++) {
			mhtChi2Vars.get(i).clear();
		}
	}


	/**
	 * Get the chi2 provider. This handles the addition of data units and 
	 * 
	 * @return standardMHTChi2Provider
	 */
	public StandardMHTChi2Provider getChi2Provider() {
		return this.standardMHTChi2Provider;
	}


	@Override
	public void clearKernelGarbage(int newRefIndex) {
		// Nothing to do here. IDIManager is cleared in the MHTChi2Provider. 
	}


	@Override
	public CTAlgorithmInfo getMHTChi2Info() {
		return new StandardMHTChi2Info(getMhtChi2Vars(), this.getChi2Params());
	}


}
