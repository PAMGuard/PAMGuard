/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package fftManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.stream.Collectors;

import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.ProcessAnnotation;
import Spectrogram.WindowFunction;

/**
 * @author Doug Gillespie
 * 
 * PamFFTProcess is a extends the PamProcess superclass
 * <br>
 * Main processing class to calculate FFT data.
 * <br>
 * Modified October 13 2009 to correctly interleave data from > 1 channel
 * rather then sending data out in chunks corresponding to individual input
 * raw data which is packed as single channels 
 * <br>
 * PamFFTProcess is then notified when new chunks of raw data have been
 * generated (e.g. by reading from a file or taking input from a sound card). It
 * then processes the data to produce blocks of overlapping FFT data which are
 * used to create a new PamDataUnit that is sent to be included in a new
 * PamDataBlock of type PamFFTDataBlock
 * 
 */
public class PamFFTProcess extends PamProcess {
	
	private int logFftLength;

	// private int fftHop;
	// private int updateCount = 0;
	// private int channelMap;
	private int fftOverlap;

	int[] channelPointer = new int[PamConstants.MAX_CHANNELS];

	private double[] windowFunction;

	private double[][] windowedData = new double[PamConstants.MAX_CHANNELS][];
	
	private double[] dataToFFT;

//	private ComplexArray fftData;

	private double[] fftRealBlock;

	private PamFFTControl fftControl;
	
	private ClickRemoval clickRemoval = new ClickRemoval();
	
	private int[] rawBlocks = new int[PamConstants.MAX_CHANNELS];
	
	private int[] fftBlocks = new int[PamConstants.MAX_CHANNELS];
	
	private int[] channelCounts;

//	RecyclingDataBlock<FFTDataUnit> recyclingOutputData;
	private FFTDataBlock outputData;
	
	private TempOutputStore[] tempStores;
	
	private long[] lastChannelMillis = new long[PamConstants.MAX_CHANNELS];
	
	private FastFFT fastFFT = new FastFFT();
	
	private Vector<ProcessAnnotation> fftAnnotations;

	private PamRawDataBlock rawDataBlock;

	public PamFFTProcess(PamFFTControl pamControlledUnit,
			PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);

		fftControl = pamControlledUnit;
		
		//sourceDataBlock.addObserver(this);
		
		setParentDataBlock(parentDataBlock);

//		AddOutputDataBlock(outputData = new RecyclingDataBlock(PamguardMVC.DataType.FFT, "Raw FFT Data", 
//				this, sourceDataBlock.getSourceProcess(), fftControl.fftParameters.channelMap));
//		addOutputDataBlock(outputData = new RecyclingDataBlock<FFTDataUnit>(FFTDataUnit.class, "Raw FFT Data", 
//				this, fftControl.fftParameters.channelMap));
		outputData = new FFTDataBlock(fftControl.getUnitName(), this, 
				fftControl.fftParameters.channelMap, fftControl.fftParameters.fftHop,
				fftControl.fftParameters.fftLength);
		outputData.setRecycle(true);
		addOutputDataBlock(outputData);
	
		setupFFT();
	}

	public synchronized void setupFFT() {
		
//		System.out.println("In call to setupFFT in " + getProcessName());
		// need to find the existing source data block and remove from observing it.
		// then find the new one and subscribe to that instead. 
		channelCounts = new int[PamConstants.MAX_CHANNELS];
		// since it's used so much, make a local reference
		FFTParameters fftParameters = fftControl.fftParameters;
		
		int[] chanList = PamUtils.getChannelArray(fftParameters.channelMap);
		
		tempStores = new TempOutputStore[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < chanList.length; i++) {
			tempStores[chanList[i]] = new TempOutputStore(chanList[i]);
		}
		
		if (fftControl == null) return;
		
		/*
		 * Data block used to be by number, now it's by name, but need to handle situations where
		 * name has not been set, so if there isn't a name, use the number !
		 */
		if (fftParameters.dataSourceName != null) {
			rawDataBlock = (PamRawDataBlock) fftControl.getPamConfiguration().getDataBlock(RawDataUnit.class, fftParameters.dataSourceName);
		}
		else {
			rawDataBlock = fftControl.getPamConfiguration().getRawDataBlock(fftParameters.dataSource);
			if (rawDataBlock != null) {
				fftParameters.dataSourceName = rawDataBlock.getDataName();
			}
		}
		
		//added as null pointer causing exception in FFT module on viewer start up. 
		//26/02/2018
		if (rawDataBlock==null) return;
		
		setParentDataBlock(rawDataBlock);
		
		if (rawDataBlock == null) return;
//		outputData.setChannelMap(fftControl.fftParameters.channelMap);
		outputData.sortOutputMaps(
				rawDataBlock.getChannelMap(),
				rawDataBlock.getSequenceMapObject(), 
				fftControl.fftParameters.channelMap);
		outputData.setFftHop(fftControl.fftParameters.fftHop);
		outputData.setFftLength(fftControl.fftParameters.fftLength);

		

		setProcessName("FFT - " + fftControl.fftParameters.fftLength + " point, "
				+ getSampleRate() + " Hz");

		//outputData.
		setSampleRate(getSampleRate(), true);
		
		// outputData.setChannelMap(fftParameters.channelMap);
		// outputData.set
		fftOverlap = fftParameters.fftLength - fftParameters.fftHop;
		logFftLength = 1;
		while (1 << logFftLength < fftParameters.fftLength) {
			logFftLength++;
		}

		windowFunction = WindowFunction.getWindowFunc(fftParameters.windowFunction, fftParameters.fftLength);
		double windowGain = WindowFunction.getWindowGain(windowFunction);
		outputData.setWindowGain(windowGain);

		fftRealBlock = new double[fftParameters.fftLength];
		//		
		// and for each channel, make a double array
		// and set the pointer to zero
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if (((1 << i) & fftControl.fftParameters.channelMap) != 0) {
				windowedData[i] = new double[fftParameters.fftLength];
				channelPointer[i] = 0;
			}
		}
//		fftData = new ComplexArray(fftParameters.fftLength);
		/*
		 * Tell the output data block - should then get passed on to Spectrogram
		 * display which can come back and work it out for itself that life has
		 * changed.
		 */
		noteNewSettings();
		
		makeAnnotations();

	}

	public int getFftLength() {
		return fftControl.fftParameters.fftLength;
	}

	public int getFftHop() {
		return fftControl.fftParameters.fftHop;
	}

	public int getChannelMap() {
		return fftControl.fftParameters.channelMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *      Gets blocks of raw audio data (single channel), blocks it up into
	 *      fftSize blocks dealing with overlaps as appropriate. fft's each
	 *      complete block and sends it off to the output PamDataBlock, which
	 *      will in turn notify any subscribing processes and views
	 */
	@Override
	public void newData(PamObservable obs, PamDataUnit pamRawData) {
		FFTDataUnit pu;
/*
 * 
 * 		int i=0;
 * 		System.out.println(pamRawData.getParentDataBlock().);
 * 		
 * 			TODO -implement some dialog or status loading bar around this.-putting this in has shown that FFTs are recalculated for whole window rather than new section too!
 * 		
 * 		System.out.println("newData: "+ pamRawData.getAbsBlockIndex());
 * 
 */		
		RawDataUnit rawDataUnit = (RawDataUnit) pamRawData;
		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());
		rawBlocks[iChan] ++;
		// see if the channel is one we want before doing anything.
		if ((rawDataUnit.getChannelBitmap() & fftControl.fftParameters.channelMap) == 0){
			return;
		}
		int copyFrom;
		/*
		 * Now make blocks of overlapping FFT data and parse them to the output
		 * data block Raw data blocks should be for a single channel
		 */
		if (iChan < 0) {
			 System.out.println("Invalid channel combination - channel map = "
			 + rawDataUnit.getChannelBitmap());
		}

		double rawData[] = rawDataUnit.getRawData();
		int dataPointer = channelPointer[iChan];
		
		//local copy
		FFTParameters fftParameters = fftControl.fftParameters;
		
		/**
		 * Work out how many milliseconds per typical
		 * FFT hop there are.  
		 */
		double hopMillis = fftParameters.fftHop / getSampleRate() * 1000.; 
		double fftMillis = fftParameters.fftLength / getSampleRate() * 1000.; 
		long millisForward = pamRawData.getTimeMilliseconds() - lastChannelMillis[iChan];
		long maxForward = (long) ((hopMillis + fftMillis) * 2)+1;
		
		if (millisForward < 0 || millisForward > maxForward) { // rest fill counter to start new bin. 
			dataPointer = 0;
			lastChannelMillis[iChan] = pamRawData.getTimeMilliseconds();
		}

		/*
		 * Data are first copied into a new block of double data wndowedData.
		 * dataPointer is the index within windowedData. When windoewedData is
		 * full, the real data are copied over into a complex array and the
		 * window function applied so that the data in windowedData is still
		 * untouched. To allow for fft overlap, the back bit of windowedData is
		 * then copied to the front of windowedData, and the dataPointer left in
		 * the right place (at fftOverlap) so that filling can continue from the
		 * outer loop over raw data.
		 * 
		 */
		for (int i = 0; i < rawData.length; i++) {
			if (dataPointer >= 0) {
				windowedData[iChan][dataPointer] = rawData[i];
			}
			dataPointer++;
			if (dataPointer == fftParameters.fftLength) {
				/*
				 * Before doing anything else, run the click removal
				 * 
				 */
				if (fftParameters.clickRemoval) {
//					clickRemoval.removeClickInPlace(windowedData[iChan], 
//							fftParameters.clickThreshold, fftParameters.clickPower);
					dataToFFT = clickRemoval.removeClicks(windowedData[iChan], dataToFFT,
							fftParameters.clickThreshold, fftParameters.clickPower);
				}
				else {
					dataToFFT = windowedData[iChan];
				}
				
				/*
				 * we have a complete block, so make the FFT and send it off to
				 * the output data block. The fftOutData was created by the FFT
				 * routine, so we can pass it off to the data manager without
				 * risk of it being overwritten
				 */
				for (int w = 0; w < fftParameters.fftLength; w++) {
					fftRealBlock[w] = dataToFFT[w] * windowFunction[w];
				}

				/*
				 * Create a new data unit BEFORE the fft is called - or better
				 * still, get one from the recycler. The recycled unit should
				 * already have the correct output data which can be parsed to
				 * the rfft routine to avoid constantly recreating complex data
				 * units.
				 */

				long startSample = rawDataUnit.getStartSample() + i - fftParameters.fftLength;

				/**
				 * Use a millisecond time from the raw datablock, not from the 
				 * sample number so that it works as well offline.  
				 */
//				pu = new FFTDataUnit(absSamplesToMilliseconds(startSample), 
//						rawDataUnit.getChannelBitmap(), startSample, fftParameters.fftLength, 
//						null, channelCounts[iChan]);
				lastChannelMillis[iChan] = pamRawData.getTimeMilliseconds() + 
					relSamplesToMilliseconds(i-fftParameters.fftLength);
				pu = new FFTDataUnit(lastChannelMillis[iChan], 
						rawDataUnit.getChannelBitmap(), startSample, fftParameters.fftLength, 
						null, channelCounts[iChan]);
					
				channelCounts[iChan]++;

				/*
				 * rfft will check that the pu.data is not null and that it is
				 * the right length. If not, it will recreate the array - so
				 * normally the return value fftOutData will be the same as
				 * pu.data !
				 * 
				 * The first data block can never be a recycled one, so will
				 * always contain a null data reference.
				 */
//				Complex[] fftOutData = outputData.getComplexArray(fftParameters.fftLength/2);
				ComplexArray fftOutData =  fastFFT.rfft(fftRealBlock, fftParameters.fftLength);
				
				// set the correct reference in the data block
				pu.setFftData(fftOutData);
//				
//				
//				double[] powerSpec = new double[fftOutData.length];
//				for (int ii = 0; ii < powerSpec.length; ii++) {
//					powerSpec[ii] = fftOutData[ii].magsq();
//				}
				

				fftBlocks[PamUtils.getSingleChannel(pu.getChannelBitmap())] ++;
				
//				outputData.addPamData(pu);
				tempStores[iChan].addData(pu);

				/*
				 * then check out the hop - if it's < fftLength it's necessary to
				 * copy data from the end of the block back to the beginning if
				 * the hop is > fftLength, then set dataPointer to - the gap so
				 * that no data is added to fftData for a while.
				 */
				dataPointer = fftOverlap;
				if (dataPointer > 0) {
					copyFrom = fftParameters.fftHop;
					for (int j = 0; j < fftOverlap; j++) {
						windowedData[iChan][j] = windowedData[iChan][copyFrom++];
//						windowedData[iChan] = Arrays.copyOfRange(windowedData[iChan], fftParameters.fftHop, fftParameters.fftHop+fftParameters.fftLength);
					}
				}
			}
		}
		TempOutputStore[] oldStores = tempStores;
		if (iChan == PamUtils.getHighestChannel(fftParameters.channelMap)) {
			// time to empty the stores - assume they all have the same
			// amount of data 
			int[] chanList = PamUtils.getChannelArray(fftParameters.channelMap);
			try {
				int n = tempStores[iChan].getN();
				for (int iF = 0; iF < n; iF++) {
					for (int iC = 0; iC < chanList.length; iC++) {
						//					pu = tempStores[chanList[iC]].get(iF);
						try {
							outputData.addPamData(tempStores[chanList[iC]].get(iF));
						}
						catch (ArrayIndexOutOfBoundsException e) {
							//						e.printStackTrace();
							System.err.printf("%s.newData: %s Store %s (was %s) iC: %d of %d iF: %d of %d\n", 
									this.getPamControlledUnit().getUnitName(), e.getMessage(), 
									tempStores[chanList[iC]], oldStores[chanList[iC]],
									iC, chanList.length, iF, n);
						}
						//					outputData.addPamData(null);
					}
				}
				for (int iC = 0; iC < chanList.length; iC++) {
					tempStores[chanList[iC]].clearStore();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		/*
		 * finally store the pointer position in the ArrayList for that channel
		 */
		channelPointer[iChan] = dataPointer;

	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	@Override
	public void pamStart() {
//		setupFFT();
	}

	@Override
	public boolean prepareProcessOK() {
		setupFFT();

		int fftChannelMap = fftControl.fftParameters.channelMap;
		int sourceChannelMap = this.parentDataBlock.getChannelMap();
		int unavailableSelectedChannels = fftChannelMap & ~sourceChannelMap;

		if (unavailableSelectedChannels != 0) {
			String commaSeparatedChannels = Arrays.stream(PamUtils.getChannelArray(unavailableSelectedChannels))
					.mapToObj(String::valueOf)
					.collect(Collectors.joining(", "));

			System.err.printf(
					"Error in the configuration of %s.\nFFT configuration uses the following channels that are not available in the source data: %s\n\n",
					getProcessName(),
					commaSeparatedChannels);
			return false;
		}

		return true;
	}
	

	@Override
	public void pamStop() {
		// nothing to do here for this class - it all happens in update
	}

	public FFTDataBlock getOutputData() {
		return outputData;
	}

	@Override
	public ProcessAnnotation getAnnotation(PamDataBlock pamDataBlock, int annotation) {
		// TODO Auto-generated method stub
		return fftAnnotations.get(annotation);
	}

	@Override
	public int getNumAnnotations(PamDataBlock pamDataBlock) {
		if (fftAnnotations == null) {
			return 0;
		}
		return fftAnnotations.size();
	}
	
	public void makeAnnotations() {
		if (fftAnnotations == null) {
			fftAnnotations = new Vector<ProcessAnnotation>();
		}
		else {
			fftAnnotations.clear();
		}
		if (fftControl.fftParameters.clickRemoval) {
			fftAnnotations.add(new ProcessAnnotation(this, clickRemoval, fftControl.getUnitType(), "Click Removal"));
		}
		fftAnnotations.add(super.getAnnotation(outputData, 0));

		outputData.createProcessAnnotations(getSourceDataBlock(), this, true);
		
	}
	
	/**
	 * Clear all temporary output stores. 
	 */
	public void clearTempStores() {
		if (tempStores == null) {
			return;
		}
		for (int i = 0; i < tempStores.length; i++) {
			if (tempStores[i] != null) {
				tempStores[i].clearStore();
			}
		}
	}
	
	class TempOutputStore {
		
		private int channel;
		
		private int capacity;

		private Vector<FFTDataUnit> tempUnits;
		
		private Object temSynch = new Object();
		
		public TempOutputStore(int channel) {
			super();
			this.channel = channel;
			tempUnits = new Vector<FFTDataUnit>();
			tempUnits.ensureCapacity(capacity = 20);
		}
		
		private synchronized void addData(FFTDataUnit fftData) {
			tempUnits.add(fftData);
		}
		
		private synchronized int getN() {
			return tempUnits.size();
		}
		
		private synchronized FFTDataUnit get(int i) {
			return tempUnits.get(i);
		}
		
		private synchronized void clearStore() {
			capacity = Math.max(capacity, tempUnits.size());
			tempUnits.ensureCapacity(capacity);
			tempUnits.clear();
		}
		
	}

	public PamFFTControl getFftControl() {
		return fftControl;
	}
	
	@Override
	public ArrayList getCompatibleDataUnits(){
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class));
	}

	@Override
	public synchronized void dumpBufferStatus(String message, boolean sayEmpties) {
		
		super.dumpBufferStatus(message, sayEmpties);
		int nTemp = 0;
		if (tempStores != null) {
			nTemp = tempStores.length;
		}
		for (int i = 0; i < nTemp; i++) {
			if (tempStores[i] == null) {
				continue;
			}
			int n = tempStores[i].tempUnits.size();
			if (n > 0 || sayEmpties) {
				System.out.printf("FFT %s temp store %d has %d datas\n", getProcessName(), i, n);
			}
		}
	}

//	@Override
//	public boolean requestOfflineData(PamDataBlock dataBlock, long startMillis,
//			long endMillis) {
//		if (rawDataBlock == null) {
//			return false;
//		}
//		return rawDataBlock.requestOfflineData(this, startMillis, endMillis);
//	}

}
