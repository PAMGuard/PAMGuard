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

/*
 * subclass of PamDataBlock specifically for raw ADC data. 
 * Contains additional variables needed to describe the raw data. 
 * Each PamRawDataBlock will hold the input from a single file or device in 
 * an ArrayList of PamDataUnits
 */
package PamguardMVC;

import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import Acquisition.RawDataBinaryDataSource;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;

/**
 * Extension of RecyclingDataBlock that is used for Raw audio data. 
 * <p>
 * Has the extra function for getting raw data samples out of the blocks. Also
 * has some special constructors that set the parent and source data blocks to
 * null. However, Raw data my be poduced by intermediate processes as well, in
 * which case parent and source blocks will not be null
 * 
 * @author Doug Gillespie
 *
 */
public class PamRawDataBlock extends AcousticDataBlock<RawDataUnit> {
	
	private long desiredSample = -1;
	private long[] prevChannelSample = new long[PamConstants.MAX_CHANNELS];
	
	private double[] summaryTotals = new double[PamConstants.MAX_CHANNELS];
	private double[] summaryTotals2 = new double[PamConstants.MAX_CHANNELS];
	private double[] summaryMaxVal = new double[PamConstants.MAX_CHANNELS];
	private int[] summaryCount = new int[PamConstants.MAX_CHANNELS];

	/**
	 * Keep a record of the last sample added. 
	 */
//	long latestSample = 0;
	
	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		// TODO Auto-generated method stub
		if (pamDataUnits.isEmpty())
			return 0;
		int n = super.removeOldUnitsT(getLastUnitMillis());
//		checkIntegrity();
		return n;
	}

	/**
	 * Check the data block integrity - that is that all units are
	 * in order and that the sample numbers increase correctly.
	 * <p>This is used when loading data offline. 
	 * @return
	 */
	synchronized private boolean checkIntegrity() {
		int nChannels = PamUtils.getNumChannels(getChannelMap());
		int errors = 0;
		int[] channelList = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			channelList[i] = PamUtils.getNthChannel(i, getChannelMap());
		}
		int expectedChannel = channelList[0];
		ListIterator<RawDataUnit> iterator = getListIterator(0);
		RawDataUnit dataUnit;
		long[] expectedSample = new long[PamConstants.MAX_CHANNELS];
		int singleChannel;
		int channelIndex = 0;
		int unitIndex = 0;
		while (iterator.hasNext()) {
			expectedChannel = channelList[channelIndex];
			dataUnit = iterator.next();
			// check it's the expected channel. 
			singleChannel = PamUtils.getSingleChannel(dataUnit.getChannelBitmap());
			if (singleChannel != expectedChannel) {
				reportProblem(++errors, unitIndex, String.format("Got channel %d, expected %d", singleChannel, expectedChannel)
						, dataUnit);
			}
			
			// check the sample number
			if (expectedSample[channelIndex] > 0) {
				if (expectedSample[channelIndex] != dataUnit.getStartSample()) {
					reportProblem(++errors, unitIndex, String.format("Got sample %d, expected %d", 
							dataUnit.getStartSample(), expectedSample[channelIndex]), dataUnit);
				}
			}
			
			// check the length
			if (dataUnit.getSampleDuration() != dataUnit.getRawData().length) {
				reportProblem(++errors, unitIndex, String.format("Have %d samples, expected %d", 
						dataUnit.getSampleDuration(), dataUnit.getRawData().length), dataUnit);
			}
			
			// move expectations.
			expectedSample[channelIndex] = dataUnit.getStartSample() + dataUnit.getSampleDuration();
			if (++channelIndex >= nChannels) {
				channelIndex = 0;
			}
			unitIndex++;
		}
		
		return errors == 0;
	}
	private void reportProblem(int nErrors, int index, String str, RawDataUnit unit) {
		System.out.println(String.format("Error %d in RawDataBlock item %d of %d: %s", 
				nErrors, index, getUnitsCount(), str));
		System.out.println(unit.toString());
	}
	public PamRawDataBlock(String name, PamProcess parentProcess, 
			int channelMap, float sampleRate) {
		super(RawDataUnit.class, name, parentProcess, channelMap);
		new RawDataDisplay(this);
		setBinaryDataSource(new RawDataBinaryDataSource(this));
	}
	
	public PamRawDataBlock(String name, PamProcess parentProcess, 
			int channelMap, float sampleRate, boolean autoDisplay) {
		super(RawDataUnit.class, name, parentProcess, channelMap);
		if (autoDisplay) {
			new RawDataDisplay(this);
		}
	}
	
	/**
	 * Reset data integrity checking counters. 
	 */
	public void reset() {
		prevChannelSample = new long[PamConstants.MAX_CHANNELS];
		summaryTotals = new double[PamConstants.MAX_CHANNELS];
		summaryTotals2 = new double[PamConstants.MAX_CHANNELS];
		summaryMaxVal = new double[PamConstants.MAX_CHANNELS];
		summaryCount = new int[PamConstants.MAX_CHANNELS];
	}
	
	@Override
	public void addPamData(RawDataUnit pamDataUnit) {
		/*
		 *  need to do a few extra tests to check that data are arriving in the
		 *  correct channel order before data can be added to the list. 
		 *  The danger here occurs primarily when running in net receiver mode
		 *  where after a disconnect, some data may have been dumped to avoid buffer overflow. 
		 *  The problem then is that data may start at some random channel number, not
		 *  on channel 0. 
		 *  <p>
		 *  <p>
		 *  The fix in here could make things a lot worse though in some situations ???? 
		 */
		int firstChannel = PamUtils.getLowestChannel(getChannelMap());
		int thisChannel = PamUtils.getSingleChannel(pamDataUnit.getChannelBitmap());
		if (thisChannel == firstChannel) {
			desiredSample = pamDataUnit.getStartSample();
		}
		else if (desiredSample != pamDataUnit.getStartSample()) {
			// don't add this data unit since its probably out of synch
			System.out.println(String.format("Sample %d channel %d in %s out of synch - expected sample %d, previously got %d",
					pamDataUnit.getStartSample(), thisChannel, getDataName(), desiredSample, prevChannelSample[thisChannel]));
//			return; add the data anyway, may get back into synch !!!! 
		}
		prevChannelSample[thisChannel] = pamDataUnit.getStartSample();
//		System.out.println(String.format("Sample %d channel %d in %s is in  synch - expected sample %d",
//				pamDataUnit.getStartSample(), thisChannel, getDataName(), desiredSample));
		addSummaryData(thisChannel, pamDataUnit);
		
		super.addPamData(pamDataUnit);
	}
	

	/**
	 * Get available data from the raw data block. Similar to the functionality of 
	 * getSamplesforMillis, but this will not throw an exception if not all of the samples
	 * are available. Exception only thrown if the channels are unavailable or no data 
	 * whatsoever are available. <br>
	 * Data are returned in RawDataUnits so that start time and duration can be accurately
	 * given since they may not be the same as the requested values. 
	 * @param startMillis Start time in milliseconds
	 * @param durationMillis duration in milliseconds
	 * @param channelMap channel map to load. 
	 * @param offlineLoad flag to say to load data if required in viewer mode (not possible in normal mode)
	 * @return One RawDataUnit per channel.
	 * @throws RawDataUnavailableException
	 */
	synchronized public RawDataUnit[] getAvailableSamples(long startMillis, long durationMillis, int channelMap, boolean offlineLoad) throws RawDataUnavailableException {
		if (hasDataSamples(startMillis, durationMillis) == false) {
			if (offlineLoad && 
					PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
				// try to load some data !
				if (parentProcess != null) {
					this.clearAll();
					parentProcess.getOfflineData(this, null, startMillis, startMillis+durationMillis, 1);
				}
			}
		}
		// can callback into this for a second go so long as offlineLoad is false.
		return getAvailableSamples(startMillis, durationMillis, channelMap);
	}
	
	/**
	 * Check to see if (all) data samples for a given time period are already in memory. 
	 * @param startMillis
	 * @param durationMillis
	 * @return
	 */
	private boolean hasDataSamples(long startMillis, long durationMillis) {
		RawDataUnit firstUnit = getFirstUnit();
		if (firstUnit == null) {
			return false;
		}
		long firstMillis = firstUnit.getTimeMilliseconds();
		if (firstMillis > startMillis) {
			return false;
		}
		RawDataUnit lastUnit = getLastUnit();
		long lastMillis = lastUnit.getEndTimeInMilliseconds();
		if (lastMillis < startMillis + durationMillis) {
			return false;
		}
		return true;
	}

	/**
	 * Get available data from the raw data block. Similar to the functionality of 
	 * getSamplesforMillis, but this will not throw an exception if not all of the samples
	 * are available. Exception only thrown if the channels are unavailable or no data 
	 * whatsoever are available. <br>
	 * Data are returned in RawDataUnits so that start time and duration can be accurately
	 * given since they may not be the same as the requested values. 
	 * @param startMillis Start time in milliseconds
	 * @param durationMillis duration in milliseconds
	 * @param channelMap channel map to load. 
	 * @return One RawDataUnit per channel.
	 * @throws RawDataUnavailableException
	 */
	synchronized public RawDataUnit[] getAvailableSamples(long startMillis, long durationMillis, int channelMap) throws RawDataUnavailableException {
		RawDataUnit firstUnit = getFirstUnit();
		if (firstUnit == null) {
			throw new RawDataUnavailableException(this, RawDataUnavailableException.DATA_NOT_ARRIVED, 0,0, startMillis, (int) durationMillis);
		}
		long firstMillis = firstUnit.getTimeMilliseconds();
		long firstSamples = firstUnit.getStartSample();
		RawDataUnit lastUnit = getLastUnit();
		long lastMillis = lastUnit.getEndTimeInMilliseconds();
		long lastSample = lastUnit.getStartSample()+lastUnit.getSampleDuration();
		
		
		long firstAvailableMillis = Math.max(firstMillis, startMillis);
		long lastAvailableMillis = Math.min(lastMillis, startMillis+durationMillis);
		// we kinda know that these exist, so get them ...
		// this will throw an exception if we're wrong. 
		double[][] data = getSamplesForMillis(firstAvailableMillis, lastAvailableMillis-firstAvailableMillis, channelMap);
		if (data == null) {
			// this shouldn't happen. If an exception wasn't thrown from getSamples... then data should no tb enull
			throw new RawDataUnavailableException(this, RawDataUnavailableException.DATA_NOT_ARRIVED,
					firstSamples, lastSample,	startMillis, (int) durationMillis);
		}
		RawDataUnit[] dataUnits = new RawDataUnit[data.length];
		for (int i = 0; i < data.length; i++) {
			double[] chData = data[i];
			int iCh = PamUtils.getNthChannel(i, channelMap);
			long startSample = firstSamples+(long) ((firstAvailableMillis-firstMillis)*getSampleRate() / 1000.);
			dataUnits[i] = new RawDataUnit(firstAvailableMillis, 1<<iCh, startSample, chData.length);
			dataUnits[i].setRawData(chData);
			dataUnits[i].setParentDataBlock(this);
		}
			
		return dataUnits;
	}

	/**
	 * Get data based on millisecond times. 
	 * @param startMillis
	 * @param durationMillis
	 * @param channelMap
	 * @return
	 * @throws RawDataUnavailableException
	 */
	synchronized public double[][] getSamplesForMillis(long startMillis, long durationMillis, int channelMap) throws RawDataUnavailableException {
		RawDataUnit firstUnit = getFirstUnit();
		if (firstUnit == null) {
			throw new RawDataUnavailableException(this, RawDataUnavailableException.DATA_NOT_ARRIVED, 0, 0, startMillis, (int) durationMillis);
		}
		long firstMillis = firstUnit.getTimeMilliseconds();
		long firstSamples = firstUnit.getStartSample();
		long startSample = firstSamples + (long) ((startMillis-firstMillis) * getSampleRate() / 1000.);
		int nSamples = (int) (durationMillis * getSampleRate() / 1000.);
		return getSamples(startSample, nSamples, channelMap);
	}
	/**
	 * Creates an array and fills it with raw data samples. 
	 * @param startSample
	 * @param duration
	 * @param channelMap
	 * @return double array of raw data
	 */
	synchronized public double[][] getSamples(long startSample, int duration, int channelMap) throws RawDataUnavailableException {
		// run  a few tests ...
		int chanOverlap = channelMap & getChannelMap();
		if (chanOverlap != channelMap) {
			throw new RawDataUnavailableException(this, RawDataUnavailableException.INVALID_CHANNEL_LIST, 0,0,startSample, duration);
		}
		if (duration < 0) {
			throw new RawDataUnavailableException(this, RawDataUnavailableException.NEGATIVE_DURATION,0,0, startSample, duration);
		}
		
		RawDataUnit dataUnit = getFirstUnit();
		if (dataUnit == null) {
			return null;
		}
		RawDataUnit lastUnit = getLastUnit();
		long firstSample = dataUnit.getStartSample();
		long lastSample = lastUnit.getStartSample()+lastUnit.getSampleDuration();
		if (firstSample > startSample) {
//			System.out.println("Earliest start sample : " + dataUnit.getStartSample());
			throw new RawDataUnavailableException(this, RawDataUnavailableException.DATA_ALREADY_DISCARDED, 
					firstSample, lastSample, startSample, duration);
		}
		dataUnit = getLastUnit();
		if (hasLastSample(dataUnit, startSample+duration, channelMap) == false)  {
			throw new RawDataUnavailableException(this, RawDataUnavailableException.DATA_NOT_ARRIVED,
					firstSample, lastSample, startSample, duration);
		}
		
		int nChan = PamUtils.getNumChannels(channelMap);
		double[][] wavData = new double[nChan][duration];
		if (getTheSamples(startSample, duration, channelMap, wavData)) {
			return wavData;
		}
//		getTheSamples(startSample, duration, channelMap, wavData);
		return null;
	}
	
	/**
	 * Have we got the last needed sample ? 
	 * @param lastRawUnit last raw data unit
	 * @param lastSample last required sample 
	 * @param channelMap channel map for data required
	 * @return
	 */
	private boolean hasLastSample(RawDataUnit lastRawUnit, long lastSample, int channelMap) {
		long dataEndSample = lastRawUnit.getStartSample() + lastRawUnit.getSampleDuration();
		if (dataEndSample < lastSample) {
			// no way are we there yet. 
			return false;
		}
		if (lastRawUnit.getStartSample() > lastSample) {
			// yes, even the start of the last raw data is past where 
			//we need to be, so all channels should have enough data. 
			return true;
		}
		
		/*
		 * Final check - only need dataEndSample > lastSample IF the
		 * data unit channel is >=the highest channel in channelMAp
		 */
		int highestChannel = Integer.highestOneBit(channelMap);
		return lastRawUnit.getChannelBitmap() >= highestChannel;
		
	}

	/**
	 * Gets samples of raw data into a pre existing array. If the array is the wrong
	 * size or does not exist, then a new one is created. 
	 * @param startSample
	 * @param duration
	 * @param channelMap
	 * @param wavData
	 * @return double array of raw data
	 */
	synchronized public double[][] getSamples(long startSample, int duration, int channelMap, double[][] wavData) {
		int nChan = PamUtils.getNumChannels(channelMap);
		if (duration < 1) return null;
		if (wavData == null || nChan != wavData.length || duration != wavData[0].length) {
			wavData = new double[nChan][duration];
		}
		if (getTheSamples(startSample, duration, channelMap, wavData)) {
			return wavData;
		}
		getTheSamples(startSample, duration, channelMap, wavData);
		return null;
	}

	/**
	 * Does the work for the above two functions.
	 * @param startSample
	 * @param duration
	 * @param channelMap
	 * @param waveData
	 * @return copies data into a double array, taking if from multiple raw datablocks
	 * if necessary
	 */
	synchronized private boolean getTheSamples(long startSample, int duration, int channelMap,
			double[][] waveData) {
		// find the first data block
//		int blockNo = -1;
		ListIterator<RawDataUnit> rawIterator = pamDataUnits.listIterator();
		RawDataUnit unit = null;
		if (pamDataUnits.size() == 0) {
			return false;
		}
		boolean foundStart = false;
		while (rawIterator.hasNext()) {
			unit = rawIterator.next();
			if (unit.getLastSample() >= startSample) {
				foundStart = true;
				break;
			}
		}
//		for (int i = 0; i < pamDataUnits.size(); i++) {
//			if (pamDataUnits.get(i).getLastSample() >= startSample) {
//				blockNo = i;
//				break;
//			}
////		}
		if (foundStart == false) {
			return false;
		}
//		if (blockNo < 0) {
////			System.out.println("start sample always after last data block");
//			return false;
//		}
		if (startSample < unit.getStartSample()) {
//			System.out.println("start sample always less than data block start");
			return false;
		}

		int nChan = PamUtils.getNumChannels(channelMap);
		int iChan;
		int outChan;
		double[] unitData;
		int offset;
		int completeChannels = 0;
		int[] channelSamples = new int[nChan]; // will need to keep an eye on
												// how many samples we have for
												// each channel
//		RawDataUnit unit = pamDataUnits.get(blockNo);
//		RawDataUnit prevUnit = null;
//		if (startSample == 7309272) {
//			System.out.println("About to crash");
//		}
		while (true) {
			if ((unit.getChannelBitmap() & channelMap) != 0) {
				iChan = PamUtils.getSingleChannel(unit.getChannelBitmap());
				outChan = PamUtils.getChannelPos(iChan, channelMap);
				unitData = unit.getRawData();
				/*
				 * This will have to be improved upon when the channel numbers
				 * are no longer just 0 and 1
				 */
				offset = (int) (startSample - unit.getStartSample())
						+ channelSamples[outChan];
				while (channelSamples[outChan] < duration
						&& offset < unitData.length) {
					// put some checks in here to see why it crashes ...
					if (offset < 0){
						System.out.println("Negative value for offset " + offset  + " samples " + channelSamples[outChan]);
						checkIntegrity();
						return false;
					}
					if (offset >= unitData.length) {
						System.out.println("Taking data from beyond end of array - will crash !");
					}
					if (waveData.length <= outChan){
						System.out.println("Not enough channels in waveData - will crash !");
					} 
					if (channelSamples.length <= outChan){
						System.out.println("Not enough entries in channel LUT - will crash !");
					} 
					if (waveData[0].length <= channelSamples[outChan]){
						System.out.println("Not enough samples in waveData - will crash !");
					} 
					waveData[outChan][channelSamples[outChan]] = unitData[offset];
					channelSamples[outChan]++;
					offset++;
				}
				if (channelSamples[outChan] == duration) {
					completeChannels |= unit.getChannelBitmap();
					if (completeChannels == channelMap) {
						return true;
					}
				}
			}

			/** 
			 * if we get here, then we still need more data, but there
			 * may not be any, so may have to bail out. 
			 */
			if (rawIterator.hasNext() == false) {
				return false;
			}
			unit = rawIterator.next();
		}
	}

	
	@Override
	public ChannelListManager getChannelListManager() {
		if (getParentSourceData() != null) {
//			System.out.println("Good ... " + getDataName() + " uses a higher level channel list manager !!!");
			return getParentSourceData().getChannelListManager();
		}
		// must be acquisition module, so return the master manager. 
		if (AcquisitionProcess.class.isAssignableFrom(parentProcess.getClass())) {
//			System.out.println("Good ... " + getDataName() + " has a channel list manager !!!");
			return ((AcquisitionProcess) parentProcess).getAcquisitionControl().getDaqChannelListManager();
		}
		System.out.println("Error ... " + getDataName() + " has no channel list manager !!!");
		return null;
	}

	@Override
	public void addObserver(PamObserver o) {
		// TODO Auto-generated method stub
		super.addObserver(o);
	}

	@Override
	public void addObserver(PamObserver o, boolean reThread) {
		// TODO Auto-generated method stub
		super.addObserver(o, reThread);
	}

	private void addSummaryData(int channel, RawDataUnit pamDataUnit) {
		double[] data = pamDataUnit.getRawData();
		synchronized(summaryTotals) {
			double tot = 0, tot2 = 0, mx = 0;
			for (int i = 0; i < data.length; i++) {
				double dat = data[i];
				tot += dat;
				tot2 += dat*dat;
				mx = Math.max(mx, dat);
			}
			summaryTotals[channel] += tot/data.length;
			summaryTotals2[channel] += tot2/data.length;
			summaryMaxVal[channel] = Math.max(summaryMaxVal[channel], mx);
			summaryCount[channel] ++;
		}
		
	}
	
	public String getSummaryString(boolean clear) {
		/*
		 * 
	sumString[0] = 0;
	float rms, max, mean;
	for (int i = 0; i < ringBufferChannels; i++) {
		//		rms = sqrt(summaryTotals[i]/summaryCount);
		rms = sqrt(summaryTotals2[i]/summaryCount) + 0.01;
		max = (double)summaryMaxVal[i] + 0.01;
		mean = summaryTotals[i]/summaryCount;
		//		sprintf(sumString+strlen(sumString), "ch%d,%3.1f,%3.1f,", i, 20.*log10((double)summaryMaxVal[i]/32768.),
		//				20*log10(rms/32768.));
		sprintf(sumString+strlen(sumString), "ch%d,%3.1f,%3.1f,%3.1f,", i, mean, 20.*log10(max/32768.),
				20.*log10(rms/32768.));
	}
		 */
		String str = "";
		int nChan = PamUtils.getNumChannels(getChannelMap());
		synchronized(summaryTotals) {
			for (int i = 0; i < nChan; i++) {
				double rms = Math.sqrt(summaryTotals2[i]/summaryCount[i])+1e-6;
				double max = summaryMaxVal[i]+1e-6;
				double mean = summaryTotals[i]/summaryCount[i];
				str += String.format("ch%d,%3.1f,%3.1f,%3.1f,", i, mean, 20.*Math.log10(max),
						20.*Math.log10(rms));
			}
		}
		if (clear) {
			clearSummaryData();
		}
		return str;
	}
	
	public void clearSummaryData() {
		synchronized(summaryTotals) {
			for (int i = 0; i < summaryTotals.length; i++) {
				summaryTotals[i] = summaryTotals2[i] = summaryMaxVal[i] = 0;
				summaryCount[i] = 0;
			}
		}
	}

//	@Override
//	protected void findParentSource() {
//		super.findParentSource();
//		if (getParentSourceData() == null) {
//			System.out.println(getDataName() + " has no source data");
//		}
//	}

}
