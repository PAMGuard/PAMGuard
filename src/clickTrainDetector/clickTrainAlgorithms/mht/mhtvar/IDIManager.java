package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;

/**
 * Calculates the inter-detection interval (IDI) (usually clicks) interval for a set of data
 * units based either on the time stamp or a more complex sample rate and cross
 * correlation calculation.
 * <p>
 * This is an important class in which all time calculations for tracks are
 * based. It holds a master time list of all detections considered by the
 * algorithm. The inter-click interval for an individual track is then
 * calculated form this master list. This ensures that the no access to the data
 * blocks and the list iterators which slow everything down are required by the
 * MHT algorithms
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class IDIManager {

	/**
	 * The size of the master IDI list. If it goes over this size then the array is 
	 * doubled in length, then trebled and so on
	 */
	private static final int ICI_MASTER_BUFFER=100; 

	/**
	 * Correlation manager, handles all cross correlation calculations. 
	 */
	private CorrelationManager correlationManager;

	/**
	 * Use the correlation to increase IDI accuracy. 
	 */
	private boolean useCorrelation = false;

	/**
	 * The inter-click interval in seconds of ALL selected incoming clicks
	 */
	private double[] masterIDISeries;

	/**
	 * The current time series in seconds starting at zero of ALL incoming clicks 
	 */
	private double[] masterTimeSeries;

	/**
	 * The last detection used in the master ICI series. 
	 */
	private PamDataUnit lastDetection;

	/**
	 * The first detection used in the master ICI series. 
	 */
	private PamDataUnit firstDetection;


	/***The last calculated track IDI info***/

	private BitSet lastBitSet = null; 

	/**
	 * The time series in seconds calculated from the last currentBitSet
	 */
	private double[] timeSeries;

	/**
	 * The ICI series in seconds 
	 */
	private double[] idiSeries;

	/**
	 * The last calculate median ICI 
	 */
	private Double medianICI;

	/**
	 * An independent count of the ICI. This should follow kcount in the MHTKernel. 
	 * Useful for error finding.m
	 */
	private int iciCount;


	private boolean forceCalc = false; 


	/**
	 * Constructor for the ICIManager. 
	 */
	public IDIManager() {
		correlationManager = new CorrelationManager(); 
	}

	/**
	 * Add a detection to the ICIManager and update the master time series. 
	 * @param detection - the current detection ion the kernel
	 * @param kcount - the current kernel kcount. This should match ICICount
	 */
	public void addDetection(PamDataUnit detection, int kcount) {
		if (this.lastDetection==null || kcount==0) {
			masterTimeSeries= new double[ICI_MASTER_BUFFER];
			masterIDISeries= new double[ICI_MASTER_BUFFER-1]; //ICI always has one less vale than ARRAY
			lastDetection=detection; 
			firstDetection=detection; 
			iciCount=1;
			correlationManager.addDetection(detection);
			return;
		}

		//add to correlation manager. 
		correlationManager.addDetection(detection);

		//increase the array size if necessary 
		if (kcount>masterTimeSeries.length) {
			masterTimeSeries = Arrays.copyOf(masterTimeSeries, masterTimeSeries.length+ICI_MASTER_BUFFER);
			masterIDISeries = Arrays.copyOf(masterIDISeries, masterIDISeries.length+ICI_MASTER_BUFFER);
		}

		//now calculate the ICI values
		double time = calcTime(lastDetection, detection);

		//add time to the master list.
		masterTimeSeries[iciCount]=time+masterTimeSeries[iciCount-1];
		masterIDISeries[iciCount-1]=time;

		//System.out.println("ICICount: " + iciCount + " time: " + time);
		iciCount++;
		lastDetection=detection; 
	
	}
	
	/**
	 * Get the last time in seconds from the time series start. This is the 
	 * last time of the last data unit to be added to the hypothesis mix. It is
	 * NOT the last time of the last hypothesis. 
	 * @return the last time in seconds from the time series start. 
	 */
	public double getLastTime() {
		return masterTimeSeries[iciCount];
	}


	/**
	 * Get the time series for a list of sequential detections. 
	 * @return a time series starting at time 0 for the first data unit in seconds. 
	 */
	@SuppressWarnings("unused")
	private double[] getTimeSeries() {
		return timeSeries; 
	}

	/**
	 * Get the inter-click interval series for a list of sequential detections. 
	 * @return a time series starting at time 0 for the first data unit in seconds. 
	 */
	@SuppressWarnings("unused")
	private double[] getIDISeries() {
		return this.idiSeries; 
	}



	/**
	 * Set the current data unit list. Sets the current list and calculates time, ICI and correlation series values. 
	 * Note: if the useCorrelation is set to false then all correlation series values will be set to zero. 
	 * @param dataUnits - the data units to set. 
	 * @param useCorrelation - use cross correlation to get more accurate ICI measurements. 
	 */
	public void setDataUnitList(ArrayList<PamDataUnit> dataUnits, boolean useCorrelation) {
		//don not keep data unit list as this could list to significant memory issues. 

		this.useCorrelation=useCorrelation; 
		//now calculate the time series. 
		calcTimeSeries(dataUnits, useCorrelation);
	}

	/**
	 * Calculate the time series, iciSeries and correlation series for the list of data units.
	 * 
	 * @param useCorrelation
	 *            - true to use a fine scale cross correlation.
	 */
	private void calcTimeSeries(ArrayList<PamDataUnit> dataUnits, boolean useCorrelation) {
		//		System.out.println("---------ICI Manager--------"); 

		this.timeSeries=null;

		//cannot calculate the size if ICI<2; 
		if (dataUnits.size()<2) return; 

		// the time series
		double[] timeSeries = new double[dataUnits.size()];


		double sampleDiff; 
		float sampleRate=dataUnits.get(0).getParentDataBlock().getSampleRate(); 

		timeSeries[0]=dataUnits.get(0).getStartSample()/sampleRate; 
		for (int i = 1; i < dataUnits.size(); i++) {

			sampleDiff = calcTime(dataUnits.get(i-1), dataUnits.get(i)); 
			timeSeries[i]=timeSeries[i-1]+(sampleDiff/sampleRate);
			//System.out.println("ICI sample diff: " +  sampleDiff + " samplerate: " + sampleRate + " timeSeries: " + timeSeries);


		}

		this.timeSeries=timeSeries;
		idiSeries=timeSeries2IDI(timeSeries); 
		//		System.out.println("----------------------------"); 
	}

	/**
	 * Calculate the time between two data units
	 * @param pamDataUnitPrev - the first data unit (in time)
	 * @param pamDataUnitNext - the second data unit (in time)
	 * @return the time in SECONDS
	 */
	public double calcTime(PamDataUnit pamDataUnitPrev, PamDataUnit pamDataUnitNext) {
		return calcTimeSR(pamDataUnitPrev,  pamDataUnitNext)/pamDataUnitNext.getParentDataBlock().getSampleRate();
	}


	/**
	 * Calculate the time between two data units
	 * @param pamDataUnitPrev - the first data unit (in time)
	 * @param pamDataUnitNext - the second data unit (in time)
	 * @return the time in SAMPLES
	 */
	public double calcTimeSR(PamDataUnit pamDataUnitPrev, PamDataUnit pamDataUnitNext) {


		//calculate the sample or millis difference between the clicks 
		double sampleDiff; 
		if (pamDataUnitNext.getTimeNanoseconds()<pamDataUnitPrev.getTimeNanoseconds()) {
			//use a millisecond time stamp - less accruate but won;t be messed up by sample number
			sampleDiff = ((pamDataUnitNext.getTimeMilliseconds()-pamDataUnitPrev.getTimeMilliseconds())/1000.)
					*pamDataUnitNext.getParentDataBlock().getSampleRate(); 
		}
		else {
			sampleDiff = ((pamDataUnitNext.getTimeNanoseconds()-pamDataUnitPrev.getTimeNanoseconds())/1E9)
					*pamDataUnitNext.getParentDataBlock().getSampleRate(); 
		}

		//System.out.println("Calculate the time between " + pamDataUnitPrev.getStartSample() + "  " +  pamDataUnitNext.getStartSample()  );

		//		//use correlation to improve the ICI estimate
		if (useCorrelation) {
			//get the correaltion value form the correlations manager. 
			CorrelationValue corrVal = correlationManager.getCorrelationValue(pamDataUnitPrev, pamDataUnitNext); 
			sampleDiff = sampleDiff-corrVal.correlationLag; 
		}

		return sampleDiff; 
	}

	/**
	 * Check whether calculation is using cross correlation to increase ICI accuracy 
	 * @return true if using correlation to increase ICI accuracy. 
	 */
	public boolean isUseCorrelation() {
		return useCorrelation;
	}

	/**
	 * Set whether calculation is using cross correlation to increase ICI accuracy 
	 * @param useCorrelation - true to use cross correlation to increase accuracy. 
	 */
	public void setUseCorrelation(boolean useCorrelation) {
		//		if (useCorrelation!=this.useCorrelation) {
		//			this.calcTimeSeries(this.currentDataUnitList, useCorrelation);
		//		}
		this.useCorrelation = useCorrelation;
	}


	/**
	 * Clear all the info from the ICIManager. This deletes the master time list. 
	 */
	public void clear() {
		//the master arrays
		masterTimeSeries=null;
		masterIDISeries=null;
		lastDetection=null;
		firstDetection=null; 
		iciCount=0; 

		//the temporary track arrays
		timeSeries=null;
		lastBitSet=null;
		idiSeries=null; 
		medianICI=null;

		//clear the correlation manager. 
		this.correlationManager.clear();
	}


	/**
	 * Get the time difference in seconds between the current last data unit and the last data unit a track
	 * @return
	 */
	public double getTimeDiff(BitSet bitSet) {
		if (forceCalc || !bitSet.equals(this.lastBitSet)) {
			calcTrackTimeIDISeries(bitSet); 
			bitSet=lastBitSet; 
			forceCalc=false; 
		}
		//now calculate the time between the last data unit int he trac
		//and the last data unit.
		return masterTimeSeries[iciCount-1]
				-timeSeries[timeSeries.length-1]; 
	}

	/**
	 * Get the IDI series for a track in seconds
	 * @param bitSet - the track bitset.
	 * @return the IDI series of the positive track detections. 
	 */
	public double[] getIDISeries(BitSet bitSet) {
		if (forceCalc || !bitSet.equals(this.lastBitSet)) {
			calcTrackTimeIDISeries(bitSet); 
			bitSet=lastBitSet; 
			forceCalc=false; 
		}
		return this.idiSeries; 
	}

	/**
	 * Get the time series for a track in seconds
	 * @param bitSet - the track bitset.
	 * @return the IDI series of the positive track detections. 
	 */
	public double[] getTimeSeries(BitSet bitSet) {
		if (forceCalc || !bitSet.equals(this.lastBitSet)) {
			calcTrackTimeIDISeries(bitSet); 
			bitSet=lastBitSet; 
			forceCalc=false;
		}
		return this.timeSeries; 
	}

	/**
	 * Get the IDI series for a track in seconds
	 * @param bitSet - the track bitset.
	 * @return the IDI series of the positive track detections. 
	 */
	public double getIDIMedian(BitSet bitSet) {
		if (forceCalc || !bitSet.equals(this.lastBitSet)) {
			calcTrackTimeIDISeries(bitSet); 
			bitSet=lastBitSet; 
			forceCalc=false; 
		}
		return this.medianICI;
	}

	/**
	 * Get the IDI series for a track in seconds
	 * @param bitSet - the track bitset.
	 * @return the IDI series of the positive track detections. 
	 */
	public IDIData getIDIStruct(BitSet bitSet) {
		//if (forceCalc || !bitSet.equals(this.lastBitSet)) {
		calcTrackTimeIDISeries(bitSet); 
		bitSet=lastBitSet; 
		forceCalc=false; 
		//}
		return new IDIData(medianICI, idiSeries, timeSeries, (masterTimeSeries[iciCount-1]
				-timeSeries[timeSeries.length-1])); 
	}

	/**
	 * Calculate the IDI and time (seconds of a track) form the current ICI data
	 * and save as fields until new bitset. 
	 */
	private void calcTrackTimeIDISeries(BitSet bitSet) {
		int ndets=bitSet.cardinality();
		int npos = Math.min(ndets, iciCount);
		double[] timeSeries=new double[npos]; 
		double[] idiSeries = new double[npos-1];
		int n=0; 
		for (int i=0; i<iciCount; i++) {
			if (bitSet.get(i)) {
				timeSeries[n]=masterTimeSeries[i];
				if (n>0) {
					//System.out.println("IDI Manager: " +  (timeSeries[n]-timeSeries[n-1]) + " time: " + timeSeries[n]);
					idiSeries[n-1]=timeSeries[n]-timeSeries[n-1];
				}
				n=n+1; 
			}
		}
		this.timeSeries=timeSeries; 
		this.idiSeries=idiSeries; 
		//this.idiSeries = timeSeries2IDI(timeSeries); 
		if (ndets>=2) this.medianICI= PamArrayUtils.median(idiSeries);
		else medianICI=-1.; 
	}


	/**
	 * Simple conversation of a time series to an inter detection interval series/
	 * 
	 * @param timeSeries
	 *            - the time series in seconds
	 * @return - the inter detection interval in seconds. The size of the array will
	 *         be one less than the time series array.
	 */
	public static double[] timeSeries2IDI(double[] timeSeries) {
		if (timeSeries.length<2) return null; 
		double[] iciSeries= new double[timeSeries.length-1]; 
		for (int i =1; i<timeSeries.length; i++) {
			iciSeries[i-1]= timeSeries[i]-timeSeries[i-1];
		}
		return iciSeries;
	}

	/**
	 * Print out double array 
	 * @param timeSeries
	 */
	public static void printTimeSeries(double[] timeSeries) {
		System.out.println("Time Series (s): ");
		for (int i=0; i<timeSeries.length; i++) {
			System.out.print(String.format("%.5f ", timeSeries[i]));
		}
		System.out.println("");
	}

	/**
	 * Print out double array 
	 * @param timeSeries
	 */
	public static void printICISeries(double[] timeSeries) {
		System.out.println("ICI Series (s): ");
		for (int i=0; i<timeSeries.length; i++) {
			System.out.print(String.format("%.5f ", timeSeries[i]));
		}
		System.out.println("");
	}

	/**
	 * Get the master time series. 
	 * @return the master time series. 
	 */
	public double[] getMasterTimeSeries() {
		return this.masterTimeSeries;
	}

	/**
	 * Check whether force calc flag is set. If true then the IDI manager will recalculate the 
	 * IDI series whether the bitset is the same or not. This is reset after the calculation to false. 
	 * @return the forceCalc flag. 
	 */
	public boolean isForceCalc() {
		return forceCalc;
	}

	/**
	 * Set the force calc flag. If true then the IDI manager will recalculate the 
	 * IDI series whether the bitset is the same or not. This is reset after the calculation to false. 
	 * @param the forceCalc flag. 
	 */
	public void setForceCalc(boolean forceCalc) {
		this.forceCalc = forceCalc;
	}

	/**
	 * Trims the front of all arrays to the specified index. This is 
	 * used to junk old data that is no longer relevant. 
	 * @param newRefIndex - the index to trim to. 
	 */
	public void trimData(int newRefIndex) {
		//the master arrays- remember the master array length is not the count size (because they are pre-allocated).
		//		System.out.println("Old IDI size: masterTimeSeries: " + masterTimeSeries.length +
		//				" masterIDISeries: " + masterIDISeries.length +  " iciCount: " + iciCount);

		//found a bug here 07/10/2019
		//For some reason I decided that the best way to copy arrays was to minus an index from everything. 
		//e.g. masterTimeSeries=Arrays.copyOfRange(masterTimeSeries, newRefIndex-1, iciCount-1); <- this is wrong and introduces a major bug....

		masterTimeSeries=Arrays.copyOfRange(masterTimeSeries, newRefIndex, iciCount);
		masterIDISeries=Arrays.copyOfRange(masterIDISeries, newRefIndex, iciCount-1);
		iciCount=iciCount-newRefIndex; 

		//		System.out.println("New IDI size: masterTimeSeries: " + masterTimeSeries.length +
		//				" masterIDISeries: " + masterIDISeries.length +  " iciCount: " + iciCount); 

		//the temporary track arrays
		timeSeries=null;
		lastBitSet=null;
		idiSeries=null; 
		medianICI=null;


		//clear the correlation manager. 
		this.correlationManager.clear();
	}

	/**
	 * Get the correlation manager. This handles correlations between data units.
	 * @return the correlations manager. 
	 */
	public CorrelationManager getCorrelationManager() {
		return this.correlationManager;
	}

	/**
	 * Get the last positive detection time for a given track BitSet. 
	 * @param bitSet - the BitSet. 
	 * @return the positive detection time in SECONDS. 
	 */
	public double getLastTime(BitSet bitSet) {
		double lastTime = 0; 
		for (int i=0; i<iciCount; i++) {
			if (bitSet.get(i)) {
				lastTime=masterTimeSeries[i];
			}
		}
		return lastTime;
	}

	/**
	 * The total time of the current set of detections. 
	 * @return the total time in seconds. 
	 */
	public double getTotalTime() {
		//08/06/2021 - this should be millisecond time stamps - definitely not nanoseconds as it was before or will
		//seriously mess up for small files
		return (this.lastDetection.getTimeMilliseconds()- this.firstDetection.getTimeMilliseconds())/1000.;
	}





}
