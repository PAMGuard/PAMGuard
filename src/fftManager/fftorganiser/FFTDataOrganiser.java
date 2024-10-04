package fftManager.fftorganiser;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import PamController.PamController;
import PamController.SettingsNameProvider;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.PamSimpleObserver;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataUnavailableException;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;

/**
 * Functions to create blocks of FFT data for multiple channels as needed by
 * various localisation algorithms in PAMGuard (e.g. BearingLocaliser and 
 * Group3DLocaliser). These localisers can take data from multiple sources:
 * <br>FFT Data from an FFTDataBlock
 * <br>RAW Data from a PAMRawDataBlock
 * <br>FFT Data from within a data unit (e.g. click spectrum)
 * <br>RAW Data from within a data unit (e.g. a click waveform)
 * In all cases, FFT data will either be taken or converted from the 
 * raw data into a list of FFT units for the requested channels and returned in 
 * a FFTDataList object with a bit of additional diagnostic information such 
 * as numbers of FFT's per channel, etc. 
 * Creates a block of FFT Data. These will be returned as an arraylist
 * @author Doug Gillespie
 *
 */
public class FFTDataOrganiser {

	public FFTInputTypes inputType = FFTInputTypes.FFTData;
	
	protected PamDataBlock rawOrFFTData;

	private Integer fftLength, fftHop;
	
	private FastFFT fastFFT;

	/**
	 * Force a datablock for detection data. This should only be used
	 * when a calculator really knows where it's data are coming from 
	 */
	private PamDataBlock<?> onlyAllowedDataBlock;

	private SettingsNameProvider settingsNameProvider;
	
	private FFTObservable fftObservable;
	
	/**
	 * @return the fftObservable
	 */
	public FFTObservable getFftObservable() {
		return fftObservable;
	}

	private long dataKeepMillis = 0;
	
	public FFTDataOrganiser(SettingsNameProvider settingsNameProvider) {
		this.settingsNameProvider = settingsNameProvider;
		fftObservable = new FFTObservable();
	}
	
	public boolean setInput(PamDataBlock rawOrFFTData, FFTInputTypes preferredInputType) {
		this.rawOrFFTData = rawOrFFTData;
		this.inputType = preferredInputType;
		return canProcess(rawOrFFTData, preferredInputType);
	}
	
	/**
	 * Determine whether or not a particular data block type can be processed 
	 * in the selected way 
	 * @param sourceInputBlock source data block
	 * @param inputType input type
	 * @return true if this is gonna work
	 */
	public boolean canProcess(PamDataBlock sourceInputBlock, FFTInputTypes inputType) {
		if (sourceInputBlock == null) {
			return false;
		}
		switch (inputType) {
		case FFTData:
			return (FFTDataUnit.class.isAssignableFrom(sourceInputBlock.getUnitClass()));
		case FFTDataHolder:
			return sourceInputBlock instanceof FFTDataHolder;
		case RAWDataHolder:
			return sourceInputBlock instanceof RawDataHolder;
		case RawData:
			return (RawDataUnit.class.isAssignableFrom(sourceInputBlock.getUnitClass()));
		default:
			return false;		
		}
	}
	
	/**
	 * Get the most suitable input type for a data block. 
	 * internal FFT data preferred, then internal raw, then external FFT, finally external raw. 
	 * @param sourceInputBlock source input data
	 * @return most suitable input type
	 */
	public FFTInputTypes suggestInputType(PamDataBlock sourceInputBlock) {
		if (sourceInputBlock == null) {
			return null;
		}
		if (FFTDataHolder.class.isAssignableFrom(sourceInputBlock.getUnitClass())) {
			return FFTInputTypes.FFTDataHolder;
		}
		if (RawDataHolder.class.isAssignableFrom(sourceInputBlock.getUnitClass())) {
			return FFTInputTypes.RAWDataHolder;
		}
		if (FFTDataUnit.class.isAssignableFrom(sourceInputBlock.getUnitClass())) {
			return FFTInputTypes.FFTData;
		}
		if ((RawDataUnit.class.isAssignableFrom(sourceInputBlock.getUnitClass()))) {
			return FFTInputTypes.RawData;
		}
		return null;
	}
	
	/**
	 * Create a list of FFT Data units using the preferred input type. <p>
	 * Note that some overridden versions of this function may chose to change the sample
	 * rate of the data (e.g. click detector upsampling) so the sample rate in the returned FFTDataList
	 * may not be the same as the sampleRate parameter fed to the function.  
	 * @param pamDataUnit data unit we need FFT data for (can be anything, just needs it's times)
	 * @param sampleRate sample rate of the data in PamDataUnit
	 * @param channelMap channel map we want data for
	 * @return Collated and interleaved list of FFT data units
	 * @throws FFTDataException
	 * @see FFTInputTypes
	 */
	public FFTDataList createFFTDataList(PamDataUnit pamDataUnit, double sampleRate, int channelMap) throws FFTDataException {
//		if (rawOrFFTData == null) {
//			return null;
//		}
		
//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//			checkOfflineDataLoad(rawOrFFTData, pamDataUnit.getTimeMilliseconds(), pamDataUnit.getEndTimeInMilliseconds());
//		}
		
		switch (inputType) {
		case FFTData:
			return createFromFFTData(pamDataUnit, sampleRate, channelMap);
		case FFTDataHolder:
			return createFromFFTHolder(pamDataUnit, sampleRate, channelMap);
		case RAWDataHolder:
			return createFromRawHolder(pamDataUnit, channelMap);
		case RawData:
			return createFromRawData(pamDataUnit, channelMap);
		default:
			return null;		
		}
	}

	/**
	 * Called when running offline to try to ensure required raw or fft data are in memory. 
	 * @param sourceData
	 * @param timeMilliseconds
	 * @param endTimeInMilliseconds
	 */
	private boolean checkOfflineDataLoad(PamDataBlock sourceData, long startMilliseconds, long endMilliseconds) {
		if (sourceData == null) {
			return false;
		}
		boolean needData = needOfflineDataLoad(sourceData, startMilliseconds, endMilliseconds);
		if (needData) {
			sourceData.loadViewerData(startMilliseconds, endMilliseconds, null);
		}
		return needOfflineDataLoad(sourceData, startMilliseconds, endMilliseconds);		
	}
	
	/**
	 * Test to see if we still need to load offline data. 
	 * @param sourceData
	 * @param startMilliseconds
	 * @param endMilliseconds
	 * @return
	 */
	private boolean needOfflineDataLoad(PamDataBlock sourceData, long startMilliseconds, long endMilliseconds) {
		if (sourceData == null) {
			return false;
		}
		synchronized (sourceData.getSynchLock()) {
			PamDataUnit first = sourceData.getFirstUnit();
			PamDataUnit last = sourceData.getLastUnit();
			if (first == null || last == null) {
				return true;
			}
			if (first.getTimeMilliseconds() > startMilliseconds) {
				return true;
			}
			if (last.getEndTimeInMilliseconds() < endMilliseconds) {
				return true;
			}
		}
		return false;
	}
	
	

	/**
	 * Get FFT data units matching in time from the source 
	 * @param pamDataUnit data unit we need FFT data for (can be anything, just needs it's times)
	 * @param channelMap channel map we want data for
	 * @return Collated and interleaved list of FFT data units
	 */
	private FFTDataList createFromFFTData(PamDataUnit pamDataUnit, double sampleRate, int channelMap) {
		FFTDataList fftDataList = new FFTDataList(sampleRate);

		long t1 = pamDataUnit.getTimeMilliseconds();
		long t2 = pamDataUnit.getEndTimeInMilliseconds();
		int firstChannelMap = 1<<PamUtils.getLowestChannel(channelMap);
		synchronized (rawOrFFTData.getSynchLock()) {
			ListIterator<PamDataUnit> fftIterator = rawOrFFTData.getListIterator((long) t1, firstChannelMap, PamDataBlock.MATCH_BEFORE, PamDataBlock.POSITION_BEFORE);
			if (fftIterator == null) {
				System.out.println("FFT iterator unavailable ");
				return null;
			}
			boolean goodtoUse = false; // need to check we really are starting on the first channel or all will go wrong. 
			while (fftIterator.hasNext()) {
				PamDataUnit fftData = fftIterator.next();
				if ((channelMap & fftData.getChannelBitmap()) == 0) {
					continue;
				}
				if (fftData.getTimeMilliseconds() < t1) {
					continue;
				}
				if (fftData.getTimeMilliseconds() >= t2) {
					break;
				}
				if (fftData.getChannelBitmap() == firstChannelMap) {
					goodtoUse = true;
				}
				if (goodtoUse == false) {
					continue;
				}
				fftDataList.addData((FFTDataUnit) fftData);
			}
		}
		
		return fftDataList;
	}

	/**
	 * Get the list of FFTDataunits from with the data unit itself
	 * @param dataUnit data unit we need FFT data for (must implement FFTDataholder)
	 * @param channelMap channel map we want data for
	 * @return Collated and interleaved list of FFT data units
	 */
	private FFTDataList createFromFFTHolder(PamDataUnit dataUnit, double sampleRate, int channelMap) {
		FFTDataHolder fftDataHolder = (FFTDataHolder) dataUnit;
		List<FFTDataUnit> fftUnits = getFFTDataUnits(fftDataHolder, fftLength);
		if (fftUnits == null) {
			return null;
		}
		FFTDataList fftDataList = new FFTDataList(sampleRate);
		for (FFTDataUnit fu:fftUnits) {
			if ((fu.getChannelBitmap() & channelMap) == 0) {
				continue;
			}
			fftDataList.addData(fu);
		}
		return fftDataList;
	}
	
	/**
	 * Separated out function to get the list of FFT data units from the FFT holder so 
	 * that individual modules can easily override this and add additional information
	 * (such as bin ranges) to the FFT data. 
	 * @param fftDataHolder FFT Data holder
	 * @param fftLength FFT length
	 * @return list of FFT data units for all channels in the fftDataholder
	 */
	public List<FFTDataUnit> getFFTDataUnits(FFTDataHolder fftDataHolder, Integer fftLength) {
		return fftDataHolder.getFFTDataUnits(fftLength);
	}

	/**
	 * @param pamDataUnit data unit we need FFT data for (must implement RawDataHolder)
	 * @param channelMap channel map we want data for
	 * @return Collated and interleaved list of FFT data units
	 */
	private FFTDataList createFromRawHolder(PamDataUnit pamDataUnit, int channelMap) {
		RawDataHolder rawHolder = (RawDataHolder) pamDataUnit;
		double[][] wavData = rawHolder.getWaveData();
		return rawToFFTData(wavData, pamDataUnit.getTimeMilliseconds(), pamDataUnit.getStartSample(), rawOrFFTData.getSampleRate(), pamDataUnit.getChannelBitmap(), channelMap);
	}

	/**
	 * 
	 * @param pamDataUnit data unit we need FFT data for (can be anything, just needs it's times)
	 * @param channelMap channel map we want data for
	 * @return Collated and interleaved list of FFT data units
	 * @throws FFTDataException
	 */
	private FFTDataList createFromRawData(PamDataUnit pamDataUnit, int channelMap) throws FFTDataException {
		PamRawDataBlock rawBlock = (PamRawDataBlock) rawOrFFTData;
		long t1 = pamDataUnit.getTimeMilliseconds();
		long t2 = pamDataUnit.getEndTimeInMilliseconds();
		double[][] wavData = null;
		try {
			wavData = rawBlock.getSamplesForMillis(t1, t2-t1, channelMap);
		} catch (RawDataUnavailableException e) {
			throw new FFTDataException("RAW Data unavailable");
//			System.err.println("Not raw data available in beam fomer: " + e.getMessage());
		}
		return rawToFFTData(wavData, pamDataUnit.getTimeMilliseconds(), pamDataUnit.getStartSample(), rawBlock.getSampleRate(), channelMap, channelMap);
	}
	
	/**
	 * 
	 * @param wavData double double array of Wav data
	 * @param startMillis start time of wavData in milliseconds. 
	 * @param sampleRate wav data sample rate
	 * @param dataChannels channel map to match wavData array
	 * @param wantedChannels wanted channels for output
	 * @return Collated and interleaved list of FFT data units
	 */
	protected FFTDataList rawToFFTData(double[][] wavData, long startMillis, Long unitStartSample, double sampleRate, int dataChannels, int wantedChannels) {
		if (wavData == null || wavData.length == 0) {
			return null;
		}
		FFTDataList fftDataList = new FFTDataList(sampleRate);
		int fftLen = fftLength;
		int fftHop;
		if (this.fftHop != null) {
			fftHop = this.fftHop;
		}
		else {
			fftHop = fftLen/2;
		}

		int nDataChan = PamUtils.getNumChannels(dataChannels);
		if (nDataChan != wavData.length) {
			System.out.println("Channel mismatch in FFTDataOrganiser.rawToFFTData");
			return null;
		}/*
		 * Check the length of the raw data, rounding it up to the next hop unless it's already >>
		 * several bins.  
		 */
		int nSamples = wavData[0].length;
		double dFFT = (nSamples - fftLength) / fftHop + 1;
		if (dFFT < 4) { // consider padding the data up to a round number of FFT's. 
			int nFFT = Math.max((int) Math.ceil(dFFT), 1);
			int wantedSamp = (nFFT-1) * fftHop + fftLength;
			if (wantedSamp > nSamples) {
				// pad the data, copying the entire array (avoids modifying internal data in the click detector). 
				wavData = Arrays.copyOf(wavData, wavData.length);
				for (int i = 0; i < wavData.length; i++) {
					wavData[i] = Arrays.copyOf(wavData[i], wantedSamp);
				}
			}
		}
		if (fastFFT == null) {
			fastFFT = new FastFFT();
		}

		nSamples = wavData[0].length;
		int nFFT = (nSamples - fftLength) / fftHop + 1;
		int startSample = 0;
		long sampleOffset = 0;
		if (unitStartSample != null) {
			sampleOffset = unitStartSample;
		}
		long fftMillis;
		int iSlice = 0;
		for (int i = 0; i < nFFT; i++) {
			fftMillis = startMillis + (long) (startSample * 1000 / sampleRate);
			for (int c = 0; c < nDataChan; c++) {
				int iChan = PamUtils.getNthChannel(c, dataChannels);
				if ((1<<iChan & wantedChannels) == 0) {
					continue; // unwanted channel
				}
				double[] wav = Arrays.copyOfRange(wavData[c], startSample, startSample+fftLen);
				ComplexArray fftData = fastFFT.rfft(wav, fftLen);
				FFTDataUnit fftDataUnit = new FFTDataUnit(fftMillis, 1<<iChan, startSample+sampleOffset, fftLen, fftData, iSlice);
				fftDataList.addData(fftDataUnit);
			}
			startSample += fftHop;
			iSlice++;
		}
		
		
		return fftDataList;
	}
	
	/**
	 * @return the fftLength
	 */
	public Integer getFftLength() {
		return fftLength;
	}

	/**
	 * @param fftLength the fftLength to set
	 */
	public void setFftLength(Integer fftLength) {
		this.fftLength = fftLength;
	}

	/**
	 * @return the fftHop
	 */
	public Integer getFftHop() {
		return fftHop;
	}

	/**
	 * @param fftHop the fftHop to set
	 */
	public void setFftHop(Integer fftHop) {
		this.fftHop = fftHop;
	}

	/**
	 * Use only if this FFT organiser is being used with a very specific
	 * data source, such as the click detector, where we 100% know that the 
	 * data will be coming from that detector. 
	 * @return the onlyAllowedDataBlock
	 */
	public PamDataBlock<?> getOnlyAllowedDataBlock() {
		return onlyAllowedDataBlock;
	}

	/**
	 * Use only if this FFT organiser is being used with a very specific
	 * data source, such as the click detector, where we 100% know that the 
	 * data will be coming from that detector. 
	 * @param onlyAllowedDataBlock the onlyAllowedDataBlock to set
	 */
	public void setOnlyAllowedDataBlock(PamDataBlock<?> onlyAllowedDataBlock) {
		this.onlyAllowedDataBlock = onlyAllowedDataBlock;
		rawOrFFTData = onlyAllowedDataBlock;
	}

	/**
	 * Get how long the input data to this need to be help in memory for (e.g. a 
	 * few seconds for whistles, perhaps a lot longer for things marked on the spectrogram ?
	 * @return the dataKeepMillis
	 */
	public long getDataKeepMillis() {
		return dataKeepMillis;
	}

	/**
	 * Set how long the input data to this need to be help in memory for (e.g. a 
	 * few seconds for whistles, perhaps a lot longer for things marked on the spectrogram ?
	 * @param dataKeepMillis the dataKeepMillis to set
	 */
	public void setDataKeepMillis(long dataKeepMillis) {
		this.dataKeepMillis = dataKeepMillis;
	}

	public class FFTObservable extends PamSimpleObserver {

		@Override
		public String getObserverName() {
			if (settingsNameProvider != null) {
				return settingsNameProvider.getUnitName();
			}
			else {
				return "FFT Data Organiser";
			}
		}

		/* (non-Javadoc)
		 * @see PamguardMVC.PamObserverAdapter#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object)
		 */
		@Override
		public long getRequiredDataHistory(PamObservable o, Object arg) {
			return dataKeepMillis;
		}
		
	}

}
