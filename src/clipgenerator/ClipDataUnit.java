package clipgenerator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.PamDetection;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;
import PamguardMVC.superdet.SuperDetection;
import Spectrogram.WindowFunction;
import fftManager.Complex;
import fftManager.FastFFT;
import wavFiles.WavFileReader;
import wavFiles.WavHeader;

public class ClipDataUnit extends PamDataUnit<PamDataUnit, SuperDetection> implements PamDetection, RawDataHolder {

	public String fileName;
	
	public String triggerName;
	
	public long triggerMilliseconds;

	private double[][] rawData;
	
	/**
	 * Reference to the data unit that triggered the clip
	 * (can't store this)
	 */
	private PamDataUnit triggerDataUnit;
	
	/**
	 * UID of trigger data unit (will get stored)
	 */
	private long triggerUID;
		
	/**
	 * Original sample rate of the waveform clip
	 */
	private float sourceSampleRate;
	
	/**
	 * Constructor to use if storing data into the binary system. 
	 * @param timeMilliseconds
	 * @param triggerMilliseconds
	 * @param startSample
	 * @param durationSamples
	 * @param channelMap
	 * @param fileName
	 * @param triggerName
	 * @param rawData
	 */
	public ClipDataUnit(long timeMilliseconds, long triggerMilliseconds,
			long startSample, int durationSamples, int channelMap, String fileName,
			String triggerName,	double[][] rawData, float sourceSampleRate) {
		super(timeMilliseconds, channelMap, startSample, durationSamples);
		this.triggerMilliseconds = triggerMilliseconds;
		this.fileName = fileName;
		this.triggerName = triggerName;
		this.rawData = rawData;
		if (this.fileName == null) {
			this.fileName = "";
		}
		this.sourceSampleRate = sourceSampleRate;
		this.rawDataTransforms = new RawDataTransforms(this); 
	}

	private BufferedImage[] clipImages = new BufferedImage[PamConstants.MAX_CHANNELS];
	
	/**
	 * Get an image of the clip
	 * @param channel
	 * @param fftLength
	 * @param fftHop
	 * @param scaleMin
	 * @param scaleMax
	 * @param colorTable
	 * @return clip image (Swing buffered image)
	 */
	public BufferedImage getClipImage(int channel, int fftLength, int fftHop, 
			double scaleMin, double scaleMax, Color[] colorTable) {
		double[][] specData = getSpectrogramData(channel, fftLength, fftHop);
		if (specData == null) {
			return null;
		}
		int nT = specData.length;
		int nF = specData[0].length;
		BufferedImage image = new BufferedImage(nT, nF, BufferedImage.TYPE_INT_RGB);
		AcquisitionProcess daqProcess = findDaqProcess();
		if (daqProcess == null) {
			return null;
		}
		daqProcess.prepareFastAmplitudeCalculation(channel);
		double ampDB;
		int lutInd;
		int nCols = colorTable.length;
		float sampleRate = getParentDataBlock().getSampleRate();
		for (int i = 0; i < nT; i++) {
			for (int j = 0; j < nF; j++) {
				ampDB = daqProcess.fftAmplitude2dB(specData[i][j], channel, sampleRate,  
						fftLength, true, true);
				lutInd = (int) Math.round((ampDB - scaleMin) / (scaleMax - scaleMin) * nCols);
				lutInd = Math.min(Math.max(0, lutInd), nCols-1);
				image.setRGB(i, nF-j-1, colorTable[lutInd].getRGB());
			}
		}
		
		return image;
	}
	
	/**
	 * Clear existing clip spectrogram data. <br>
	 * This will force it to regenerate the data when next requested 
	 * rather than recycling it
	 */
	public void clearClipSpecData() {
		if (clipSpecData != null) {
			for (int i = 0; i < clipSpecData.length; i++) {
				clipSpecData[i] = null;
			}
		}
	}
	
	private ClipSpecData[] clipSpecData = new ClipSpecData[PamConstants.MAX_CHANNELS];
	/**
	 * get spectrogram data for the clip. 
	 * @param fftLength FFT length
	 * @param fftHop FFT hop
	 * @return double array of mag squared data or null if the clip waveform cannot be found
	 */
	public double[][] getSpectrogramData(int channel, int fftLength, int fftHop) {
		if (clipSpecData[channel] == null 
				|| clipSpecData[channel].fftLength != fftLength 
				|| clipSpecData[channel].fftHop != fftHop) {
			double[][] specData = generateSpectrogram(channel, fftLength, fftHop);
			if (specData == null) {
				clipSpecData[channel] = null;
				return null;
			}
			else {
				clipSpecData[channel] = new ClipSpecData(channel, fftLength, fftHop,specData);
			}
		}
		return clipSpecData[channel].spectrogramData;
	}
	
	/**
	 * Generate spectrogram data for the clip. 
	 * @param fftLength FFT length
	 * @param fftHop FFT hop
	 * @return double array of mag squared data or null if the clip waveform cannot be found
	 */
	private double[][] generateSpectrogram(int channel, int fftLength, int fftHop) {
		// TODO Auto-generated method stub
		double[] wave = getSpectrogramWaveData(channel, getDisplaySampleRate());
		if (wave == null) {
			return null;
		}
		int nFFT = (wave.length - (fftLength-fftHop)) / fftHop;
		if (nFFT <= 0) {
			return null;
		}
		double[][] specData = new double[nFFT][fftLength/2];
		double[] waveBit = new double[fftLength];
		double[] winFunc = getWindowFunc(fftLength);
		Complex[] complexOutput = Complex.allocateComplexArray(fftLength/2);
		int wPos = 0;
		getFastFFT(fftLength);
		int m = FastFFT.log2(fftLength);
		for (int i = 0; i < nFFT; i++) {
			wPos = i*fftHop;
			for (int j = 0; j < fftLength; j++) {
				waveBit[j] = wave[j+wPos]*winFunc[j];
			}
			fastFFT.rfft(waveBit, complexOutput, m);
			for (int j = 0; j < fftLength/2; j++) {
				specData[i][j] = complexOutput[j].magsq();
			}
		}
		return specData;
	}
	
	/**
	 * Generate complex spectrogram data for the clip. 
	 * @param channel
	 * @param fftLength FFT length
	 * @param fftHop FFT hop
	 * @return double array of mag squared data or null if the clip waveform cannot be found
	 */
	public Complex[][] generateComplexSpectrogram(int channel, int fftLength, int fftHop) {
		// TODO Auto-generated method stub
		double[] wave = getSpectrogramWaveData(channel, getDisplaySampleRate());
		if (wave == null) {
			return null;
		}
		int nFFT = (wave.length - (fftLength-fftHop)) / fftHop;
		if (nFFT <= 0) {
			return null;
		}
		Complex[][] specData = new Complex[nFFT][fftLength/2];
		double[] waveBit = new double[fftLength];
		double[] winFunc = getWindowFunc(fftLength);
		Complex[] complexOutput = Complex.allocateComplexArray(fftLength/2);
		int wPos = 0;
		getFastFFT(fftLength);
		int m = FastFFT.log2(fftLength);
		for (int i = 0; i < nFFT; i++) {
			wPos = i*fftHop;
			for (int j = 0; j < fftLength; j++) {
//				waveBit[j] = wave[j+wPos]*winFunc[j];
				waveBit[j] = wave[j+wPos]; // no windowing for this since used in cross correlation. 
			}
			fastFFT.rfft(waveBit, complexOutput, m);
			for (int j = 0; j < fftLength/2; j++) {
				specData[i][j] = complexOutput[j].clone();
			}
		}
		return specData;
	}
	/**
	 * Generate complex spectrogram data for the clip. 
	 * @param channel
	 * @param fftLength FFT length
	 * @param fftHop FFT hop
	 * @return Array of ComplexArray FFT data (half fft length, due to real input)
	 */
	public ComplexArray[] generateSpectrogramArrays(int channel, int fftLength, int fftHop) {
		// TODO Auto-generated method stub
		double[] wave = getSpectrogramWaveData(channel, getDisplaySampleRate());
		if (wave == null) {
			return null;
		}
		int nFFT = (wave.length - (fftLength-fftHop)) / fftHop;
		if (nFFT <= 0) {
			return null;
		}
		ComplexArray[] specData = new ComplexArray[nFFT];
		double[] waveBit = new double[fftLength];
		double[] winFunc = getWindowFunc(fftLength);
		Complex[] complexOutput = Complex.allocateComplexArray(fftLength/2);
		int wPos = 0;
		getFastFFT(fftLength);
		int m = FastFFT.log2(fftLength);
		for (int i = 0; i < nFFT; i++) {
			wPos = i*fftHop;
			for (int j = 0; j < fftLength; j++) {
//				waveBit[j] = wave[j+wPos]*winFunc[j];
				waveBit[j] = wave[j+wPos]; // no windowing for this since used in cross correlation. 
			}
			specData[i] = fastFFT.rfft(waveBit, fftLength);
//			fastFFT.rfft(waveBit, complexOutput, m);
//			for (int j = 0; j < fftLength/2; j++) {
//				specData[i][j] = complexOutput[j].clone();
//			}
		}
		return specData;
	}
	
	protected static FastFFT fastFFT;
	protected static FastFFT getFastFFT(int fftLength) {
		if (fastFFT == null) {
			fastFFT = new FastFFT();
		}
		return fastFFT;
	}
	
	AcquisitionProcess findDaqProcess() {
			return (AcquisitionProcess) getParentDataBlock().getSourceProcess();
	}
	
	
	static double[] windowFunc;
	private static int windowType = WindowFunction.HANNING;
	protected static double[] getWindowFunc(int fftLength) {
		if (windowFunc == null || windowFunc.length != fftLength) {
			windowFunc = WindowFunction.getWindowFunc(windowType, fftLength);
		}
		return windowFunc;
	}
	
	/**
	 * Function that can be overridden in the difar clips to allow access to decimated data, etc. 
	 * @return wave data to use for spectrogram image generation. 
	 */
	protected double[] getSpectrogramWaveData(int channel, float displayFrequency) {	
		return getWaveData(channel);
	}
	
	/**
	 * Get all the wave data into an array. 
	 * @return the wave data or null if it can't be found. 
	 */
	@Override
	public double[][] getWaveData() {
		if (rawData != null) {
			return rawData;
		}
		File wavFileName = findWavFile();
		if (wavFileName == null || !wavFileName.exists() || wavFileName.isDirectory()) {
			return null;
		}
		WavFileReader wavFile = null;
		try {
			wavFile = new WavFileReader(wavFileName.getAbsolutePath());
		}
		catch (Exception e) {
			return null;
		}
		if (wavFile == null) {
			return null;
		}
		WavHeader wavHeader = wavFile.readWavHeader();
		int nSamples = (int) (wavHeader.getDataSize() / wavHeader.getBlockAlign());
		int nChannels = wavHeader.getNChannels();
		double[][] waveData = new double[nChannels][nSamples];
		wavFile.readData(waveData);
		return waveData;
	}
	
	/**
	 * @return the sample rate of original wave data or null if it can't be found. 
	 */
	protected Float getSampleRate() {
		if (rawData != null) {
			return getParentDataBlock().getSampleRate();
//			return PamController.getInstance().getRawDataBlock(triggerName).getSampleRate();
		}
		
		File wavFileName = findWavFile();
		if (wavFileName == null || !wavFileName.exists()) {
			return null;
		}
		WavFileReader wavFile = null;
		try {
			wavFile = new WavFileReader(wavFileName.getAbsolutePath());
		} catch (Exception e) {
			return null;
		}
		
		if (wavFile == null) {
			return null;
		}
		
		WavHeader wavHeader = wavFile.readWavHeader();
		return (float) wavHeader.getSampleRate();
	}
	
	/**
	 * Get the wave data for a single channel. Note that the wave clip may have only 
	 * recorded data for a subset of channels, so it's necessary to look at the channel
	 * bitmap to work out which channel from the wave clip we actually want. 
	 * @param channel channel number
	 * @return array of wave data, or null if it can't be found. 
	 */
	protected double[] getWaveData(int channel) {
		double[][] waveData = getWaveData();
//		if (waveData != null) {
//			System.out.println(String.format("Clip wave data has %d channels", waveData.length));
//		}
		if (channel == 0) {
			// find the first waveData with data in it. 
			if (waveData == null) {
				return null;
			}
			for (int i = 0; i < waveData.length; i++) {
				if (waveData[i] != null) {
					return waveData[i];
				}
				return null;
			}
		}
		int channelPos = PamUtils.getChannelPos(channel, getChannelBitmap());
		if (waveData == null) {
			return null;
		}
		int waveChans = waveData.length;
		if (channelPos >= waveChans || channelPos < 0) {
			return null;
		}
		return waveData[channelPos];
	}

	private File findWavFile() {
		try {
			if (offlineFile == null || !offlineFile.exists()) {
				if (getParentDataBlock() == null) return null;
				ClipProcess clipProcess = (ClipProcess) getParentDataBlock().getParentProcess();
				offlineFile = clipProcess.findClipFile(this);
			}
			return offlineFile;
		}
		catch (ClassCastException e) {
			return null;
		}
	}
	
	/**
	 * offlien file handle - keep persistently, since it may get needed > 1 time. 
	 */
	private File offlineFile;
	
	/**
	 * Class to hold a set of information about a generated spectrgram clip. 
	 * @author Doug Gillespie
	 *
	 */
	class ClipSpecData {
		public ClipSpecData(int channel, int fftLength, int fftHop,
				double[][] spectrogramData) {
			this.channel = channel;
			this.fftLength = fftLength;
			this.fftHop = fftHop;
			this.spectrogramData = spectrogramData;
		}
		int channel;
		double[][] spectrogramData = null;
		int fftLength, fftHop;
	}

	/**
	 * @return the triggerMilliseconds
	 */
	public long getTriggerMilliseconds() {
		return triggerMilliseconds;
	}

	/**
	 * @return the rawData
	 */
	public double[][] getRawData() {
		return rawData;
	}

	/**
	 * @param rawData the rawData to set
	 */
	public void setRawData(double[][] rawData) {
		this.rawData = rawData;
	}

	/**
	 * @return the sourceSampleRate
	 */
	public final float getSourceSampleRate() {
		return sourceSampleRate;
	}

	/**
	 * 
	 * @return Sample rate for the display
	 */
	public float getDisplaySampleRate() {
		return getSourceSampleRate();
	}

	public int getWindowType() {
		return windowType;
	}


	public static void setWindowType(int windowType) {
		ClipDataUnit.windowType = windowType;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataUnit#getLocalisation()
	 */
	@Override
	public AbstractLocalisation getLocalisation() {
		AbstractLocalisation lc = super.getLocalisation();
		if (lc != null) {
			return lc;
		}
		PamDataUnit triggerData = findTriggerDataUnit();
		if (triggerData != null) {
			return triggerData.getLocalisation();
		}
		return null;
	}

	public PamDataUnit findTriggerDataUnit() {
		if (triggerDataUnit != null) {
			return triggerDataUnit;
		}
		String trigName = this.triggerName;
		long trigMillis = this.triggerMilliseconds;
//		long startMillis = clipDataUnit.getTimeMilliseconds();
		PamDataBlock<PamDataUnit> dataBlock = findTriggerDataBlock(trigName);
		if (dataBlock == null) {
			return null;
		}
		return triggerDataUnit = findTriggerDataUnit2(dataBlock, this, 20);
	}
	
	/**
	 * Bespoke search for finding the trig data unit, since the times don't always 
	 * seem to be matching up correctly. This seems to be more a problem for old data from old file
	 * format which didn't store the trigger time than it is for newer data. 
	 * @param dataBlock datablock to search
	 * @param clipDataUnit clip to match to
	 * @param timeJitter allowable time jitter (+ or -)
	 * @return found data unit with overlapping channel map and time close to the clip trigger time. 
	 */
	private PamDataUnit findTriggerDataUnit2(PamDataBlock<PamDataUnit> dataBlock, ClipDataUnit clipDataUnit, int timeJitter) {
		long trigMillis = clipDataUnit.triggerMilliseconds;
		long t1 = trigMillis - timeJitter;
		long t2 = trigMillis + timeJitter;
		int channels = clipDataUnit.getChannelBitmap();
		synchronized (dataBlock.getSynchLock()) {
			ListIterator<PamDataUnit> iter = dataBlock.getListIterator(PamDataBlock.ITERATOR_END);
			while (iter.hasPrevious()) {
				PamDataUnit trigUnit = iter.previous();
				long trigTime = trigUnit.getTimeMilliseconds();
				if (trigTime >= t1 && trigTime <= t2 && (trigUnit.getChannelBitmap() & channels) != 0) {
					return trigUnit;
				}
			}
			
		}
		return null;
	}

	
	private String lastFoundName;
	private PamDataBlock<PamDataUnit> lastFoundBlock;

	private RawDataTransforms rawDataTransforms;
	private PamDataBlock<PamDataUnit> findTriggerDataBlock(String dataName) {
		if (dataName == null) {
			return null;
		}
		if (dataName.equals(lastFoundName)) {
			return lastFoundBlock;
		}
		PamDataBlock<PamDataUnit> dataBlock = PamController.getInstance().getDetectorDataBlock(dataName);
		if (dataBlock == null) {
			return null;
		}
		lastFoundName = new String(dataName);
		lastFoundBlock = dataBlock;
		return dataBlock;
	}

	@Override
	public void setTimeMilliseconds(long timeMilliseconds) {
		/**
		 * Really annoying, but when correcting times of network data the 
		 * link to trigger data units gets broken unless they get the same time correction
		 */
		long timeCorr = timeMilliseconds - getTimeMilliseconds();
		super.setTimeMilliseconds(timeMilliseconds);
		triggerMilliseconds += timeCorr;
	}

	/**
	 * Reference to the data unit that triggered the clip<br>
	 * This is going to be correct in normal mode, but the unit will have
	 * to be searched for in other modes, so is not 100% guaranteed. 
	 * @return the triggerDataUnit
	 */
	public PamDataUnit getTriggerDataUnit() {
		return triggerDataUnit;
	}

	/**
	 * Reference to the data unit that triggered the clip
	 * @param triggerDataUnit the triggerDataUnit to set
	 */
	public void setTriggerDataUnit(PamDataUnit triggerDataUnit) {
		this.triggerDataUnit = triggerDataUnit;
		if (triggerDataUnit != null) {
			this.triggerUID = triggerDataUnit.getUID();
		}
	}

	/**
	 * UID of trigger data unit 
	 * @return the triggerUID
	 */
	public long getTriggerUID() {
		return triggerUID;
	}

	/**
	 * UID of trigger data unit 
	 * @param triggerUID the triggerUID to set
	 */
	public void setTriggerUID(long triggerUID) {
		this.triggerUID = triggerUID;
	}

	@Override
	public RawDataTransforms getDataTransforms() {
		return rawDataTransforms;
	}

	@Override
	public String getSummaryString() {
		String summary = super.getSummaryString();
		if (triggerDataUnit != null) {
			summary += "<p><b>Trigger:</b><br>" + triggerDataUnit.getSummaryString();
		}
		return summary;
	}

}
