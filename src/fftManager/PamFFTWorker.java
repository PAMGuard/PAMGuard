package fftManager;

import java.util.ArrayList;

import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import Spectrogram.WindowFunction;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Extracted functions from PamFFTProcess so that other modules can make their
 * own spectrograms easily enough. 
 * A key task is synching all the channels so that FFT's in the output stream are 
 * correctly interleaved. 
 * @author dg50
 *
 */
public class PamFFTWorker {

	int channelMap = 1;
	int fftLength = 512;
	int fftHop = 256;
	int windowFunction = WindowFunction.HANNING;
	private FFTDataBlock fftDataBlock;
	private int fftOverlap = fftLength - fftHop;
	private double[] window = null;
	private double[] windowedWav = null;
	private TempFFTStore[] tempStores = new TempFFTStore[PamConstants.MAX_CHANNELS];
	private Object storeSynch = new Object();
	private int highestChannel;
	private DoubleFFT_1D doubleFFT_1D;
	private double sampleRate;

	public PamFFTWorker(FFTDataBlock fftDataBlock) {
		this.fftDataBlock = fftDataBlock;
	}

	public void prepare(double sampleRate, int channelMap, int fftLength, int fftHop) {
		this.sampleRate = sampleRate;
		this.channelMap = channelMap;
		this.fftLength = fftLength;
		this.fftHop = fftHop;
		this.fftOverlap  = fftLength-fftHop;
		window = WindowFunction.getWindowFunc(windowFunction, fftLength);
		windowedWav = new double[fftLength];
		doubleFFT_1D = new DoubleFFT_1D(fftLength);
		highestChannel = PamUtils.getHighestChannel(channelMap);
		synchronized (storeSynch) {
			deleteStores();
			for (int i = 0; i <= highestChannel; i++) {
				if ((1<<i & channelMap) != 0) {
					tempStores[i] = new TempFFTStore(i, fftLength);
				}
			}
		}

	}

	private void deleteStores() {
		synchronized (storeSynch) {
			tempStores = new TempFFTStore[PamConstants.MAX_CHANNELS];
		}
	}

	public void newRawData(RawDataUnit rawDataUnit) {
		int chanMap = rawDataUnit.getChannelBitmap();
		if ((chanMap & channelMap) == 0) {
			return;
		}
		int singleChan = PamUtils.getSingleChannel(chanMap);
		synchronized (storeSynch) {
			TempFFTStore store = tempStores[singleChan];
			double[] wav = rawDataUnit.getRawData();
			while (store.sourceStartIndex < wav.length) {
				int toCopy = fftLength - store.wavIndex;
				toCopy = Math.min(toCopy,  wav.length-store.sourceStartIndex);
				// be careful with the last bit, since wav will run out of data. 
				System.arraycopy(wav, store.sourceStartIndex, store.wavData, store.wavIndex, toCopy);
				store.wavIndex += toCopy;
				if (store.wavIndex == fftLength) {
					// do the FFT. will nearly always go into this except at the end of a wav segment
					/*
					 *  make a new place for the windowed data since we need a new one
					 *  every time with the in place FFT function. 
					 */
					windowedWav = new double[fftLength];
					for (int j = 0; j < fftLength; j++) {
						windowedWav[j] = store.wavData[j] * window[j];
					}
					doubleFFT_1D.realForward(windowedWav);
					int offSamps = store.sourceStartIndex-fftLength; // start sample relative to raw data unit
					int offMillis = (int) ((double) offSamps * 1000./sampleRate);
					ComplexArray complexArray = new ComplexArray(windowedWav);
					FFTDataUnit fftDataUnit = new FFTDataUnit(rawDataUnit.getTimeMilliseconds() + offMillis, chanMap, 
							rawDataUnit.getStartSample() + offSamps, fftLength, complexArray, store.fftSliceNo++);
					store.tempData.add(fftDataUnit);
					store.shuffle();
					store.sourceStartIndex += fftHop;
				}
			}
			store.sourceStartIndex -= wav.length;
			if (singleChan == highestChannel) {
				// time to dump the stores data. 
				/*
				 *  they should all have the same amount of data in them. though I think this is a 
				 *  bug that came up in the old code. Lets see if it recreates now this is correctly synched 
				 */
				int n = store.tempData.size();
				for (int i = 0; i < n; i++) {
					for (int j = 0; j <= highestChannel; j++) {
						store = tempStores[j];
						if (store == null) {
							continue;
						}
						FFTDataUnit fftDataUnit = store.tempData.get(i);
						fftDataBlock.addPamData(fftDataUnit);
					}
				}
				// now clear the stores
				for (int j = 0; j <= highestChannel; j++) {
					store = tempStores[j];
					if (store == null) {
						continue;
					}
					store.clearStore();
				}

			}
		}
	}

	private class TempFFTStore {

		private int channelIndex;

		private ArrayList<FFTDataUnit> tempData = new ArrayList();

		private double[] wavData;

		private int wavIndex;

		private int sourceStartIndex;

		private int fftSliceNo;

		public TempFFTStore(int channelIndex, int fftLength) {
			super();
			this.channelIndex = channelIndex;
			wavData = new double[fftLength];
		}

		/**
		 * Shuffle data at the end of the store back to the start to handle
		 * overlapped data. 
		 */
		public void shuffle() {
			if (fftOverlap <= 0) {
				wavIndex = 0;
			}
			else {
				System.arraycopy(wavData, fftLength-fftOverlap, wavData, 0, fftOverlap);
			}
			wavIndex -= fftOverlap; 
		}

		private void clearStore() {
			tempData.clear();
		}


	}

}
