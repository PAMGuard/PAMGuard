package whistlesAndMoans;

import java.util.ArrayList;
import java.util.List;

import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.FFTDataHolder;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import fftManager.FFTDataUnit;

public class ConnectedRegionDataUnit extends AbstractWhistleDataUnit implements TFContourProvider, FFTDataHolder {

	private ConnectedRegion connectedRegion;
	private WhistleToneConnectProcess whistleToneConnectProcess;

	//	As of version 2 this has been removed, since time delays are now stored
	//	in the DataUnitBaseData object
	//	private double[] timeDelaysSeconds; 

	public ConnectedRegionDataUnit(ConnectedRegion connectedRegion, WhistleToneConnectProcess whistleToneConnectProcess) {
		super(connectedRegion.getStartMillis(), 1<<connectedRegion.getChannel(), 
				connectedRegion.getStartSample(), connectedRegion.getDuration());
		this.connectedRegion = connectedRegion;
		setChannelBitmap(1<<connectedRegion.getChannel());	// note that ConnectedRegion.getChannel may return a channel or a sequence, depending on source
		// extract frequency information
		setFrequency(getFrequency(whistleToneConnectProcess.getSampleRate(), 
				whistleToneConnectProcess.getFFTLen()));
		this.whistleToneConnectProcess = whistleToneConnectProcess;
	}

	/**
	 * Constructor to use from Binary files
	 * @param dataUnitBaseData
	 * @param connectedRegion
	 * @param whistleToneConnectProcess
	 */
	public ConnectedRegionDataUnit(DataUnitBaseData dataUnitBaseData, ConnectedRegion connectedRegion, WhistleToneConnectProcess whistleToneConnectProcess) {
		super(dataUnitBaseData);
		this.connectedRegion = connectedRegion;
		this.whistleToneConnectProcess = whistleToneConnectProcess;
	}

	public ConnectedRegion getConnectedRegion() {
		return connectedRegion;
	}

	@Override
	public double[] getFrequency() {
		if (getParentDataBlock() != null) {
			return getFrequency(getParentDataBlock().getSampleRate(), 
					((ConnectedRegionDataBlock) getParentDataBlock()).getFftLength());
		}
		return super.getFrequency();
	}

	private double[] getFrequency(double sampleRate, int fftLength) {

		double[] f = new double[2];
		int[] range = connectedRegion.getFreqRange();
		for (int i = 0; i < 2; i++) {
			f[i] = range[i] * sampleRate / fftLength;
		}
		return f;
	}

	@Override
	public int getSliceCount() {
		return connectedRegion.getNumSlices();
	}

	private double[] freqData;
	@Override
	public double[] getFreqsHz() {
		int[] fb = connectedRegion.getPeakFreqsBins();
		ConnectedRegionDataBlock db = (ConnectedRegionDataBlock) getParentDataBlock();
		int L = getSliceCount();
		if (freqData == null || freqData.length != L) {
			freqData = new double[L];
			for (int i = 0; i < L; i++) {
				freqData[i] = db.binsToHz(fb[i]);
			}
		}

		return freqData;
	}

	private double[] timeData;
	@Override
	public double[] getTimesInSeconds() {
		int[] tb = connectedRegion.getTimesBins();
		ConnectedRegionDataBlock db = (ConnectedRegionDataBlock) getParentDataBlock();
		int L = getSliceCount();
		if (timeData == null || timeData.length != L) {
			timeData = new double[L];
			for (int i = 0; i < L; i++) {
				timeData[i] = db.binsToSeconds(tb[i]);
			}
		}
		return timeData;
	}

	/**
	 * As of version 2 time delays are now stored in the DataUnitBaseData object.  An
	 * important difference, however, is that if there are no time delays yet the
	 * DataUnitBaseData would set the array to null, whereas this object would
	 * have set it to an empty array (in the WhistleBinaryDataSource object).
	 * Therefore, override PamDataUnit's setTimeDelaysSeconds method to intercept
	 * any empty arrays and set them as null
	 * 
	 * @param timeDelaysSeconds the time delays (in seconds) to set
	 */
	@Override
	public void setTimeDelaysSeconds(double[] timeDelaysSeconds) {
		if (timeDelaysSeconds.length==0) {
			timeDelaysSeconds=null;
		}
		getBasicData().setTimeDelaysSeconds(timeDelaysSeconds);
	}

	/**
	 * As of version 2 time delays are now stored in the DataUnitBaseData object.  An
	 * important difference, however, is that if there are no time delays yet the
	 * DataUnitBaseData would set the array to null, whereas this object would
	 * have set it to an empty array (in the WhistleBinaryDataSource object).
	 * Therefore, override PamDataUnit's getTimeDelaysSeconds method to intercept
	 * any null arrays and return them as empty
	 * 
	 * @return the time delays in seconds
	 */
	@Override
	public double[] getTimeDelaysSeconds() {
		double[] delays = getBasicData().getTimeDelaysSeconds();
		if (delays==null) {
			delays=new double[0];
		}
		return delays;
	}

	@Override
	public TFContourData getTFContourData() {
		if (connectedRegion == null) {
			return null;
		}
		List<SliceData> sliceData = connectedRegion.getSliceData();
		int nBins = sliceData.size();
		long[] times = new long[nBins];
		double[] fLow = new double[nBins];
		double[] fHigh = new double[nBins];
		double[] fRidge = new double[nBins];
		int i = 0;
		/**
		 * bugger - the slices only have a sample number and I want 
		 * absolute milliseconds of data, so will have to recalculate. 
		 */
		ConnectedRegionDataBlock db = (ConnectedRegionDataBlock) getParentDataBlock();
		double tScale = 1000. / db.getSampleRate();
		double fScale = db.getSampleRate() / db.getFftLength();
		SliceData firstSlice = sliceData.get(0);
		for (SliceData slice:sliceData) {
			times[i] = getTimeMilliseconds() + (long) ((slice.startSample-firstSlice.startSample)*tScale);
			fLow[i] = (slice.peakInfo[0][0]+.5)*fScale;
			fHigh[i] = (slice.peakInfo[slice.nPeaks-1][2]+.5)*fScale;
			fRidge[i] = slice.peakInfo[0][1];
			for (int p = 1; p < slice.nPeaks; p++) {
				fRidge[i] = Math.max(fRidge[i], slice.peakInfo[p][1]);
			}
			fRidge[i] = (fRidge[i] + 0.5) * fScale;
			i++;
		}

		return new TFContourData(times, fRidge, fLow, fHigh);
	}

	@Override
	public List<FFTDataUnit> getFFTDataUnits(Integer fftLength) {
		List<FFTDataUnit> fftList = getInternalFFTList();
		if (fftList != null) {
			return fftList;
		}
		if (whistleToneConnectProcess != null) {
			return whistleToneConnectProcess.getFFTInputList(connectedRegion, this.getChannelBitmap());
		}
		return null;
	}

	/**
	 * Try to get the FFT list from existing slice data. 
	 * This will only work for a single channel. 
	 * @return list of fft data units. 
	 */
	private List<FFTDataUnit> getInternalFFTList() {
		int chMap = getBasicData().getChannelBitmap();
		if (PamUtils.getNumChannels(chMap) != 1) {
			return null;
		}
		int nS = connectedRegion.getNumSlices();
		int nOK = 0;
		ArrayList<FFTDataUnit> fftList = new ArrayList<FFTDataUnit>(nS);
		List<SliceData> sliceDatas = connectedRegion.getSliceData();
		for (SliceData sl:sliceDatas) {
			FFTDataUnit fftUnit = sl.getFftDataUnit();
			if (fftUnit != null) {
				fftUnit.setUsefulBinRange(sl.getUsefulBinRange());
				fftList.add(fftUnit);
				nOK ++;
			}
		}
		return nOK == nS ? fftList : null;
	}
}
