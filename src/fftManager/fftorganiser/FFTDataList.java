package fftManager.fftorganiser;

import java.util.ArrayList;

import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import fftManager.FFTDataUnit;

/**
 * List of FFT Data provided by the FFTDataOrganiser. This should be interleaved by channel
 * and makes a 'friendly' lump of FFT data to use with various localisation algorithms. 
 * @author Doug Gillespie
 *
 */
public class FFTDataList {

	private ArrayList<FFTDataUnit> fftDataUnits;
		
	private int channelMap;
	
	private int[] channelCount;
	
	private int minChannelCount, maxChannelCount;
	
	private int minFFTLength, maxFFTLength;

	private int maxChannel;
	
	private double sampleRate;

	/**
	 * Construct an FFT data list object. 
	 */
	public FFTDataList(double sampleRate) {
		super();
		this.sampleRate = sampleRate;
		fftDataUnits = new ArrayList<>();
		channelCount = new int[PamConstants.MAX_CHANNELS];
	}
	
	/**
	 * Add an FFTDataUnit to the list. 
	 * @param fftDataUnit
	 * @return true if all the FFT's are all still the same length. 
	 */
	public boolean addData(FFTDataUnit fftDataUnit) {
		int fftLen = fftDataUnit.getFftData().length()*2;
		if (fftDataUnits.isEmpty()) {
			minFFTLength = maxFFTLength = fftLen;
		}
		else {
			minFFTLength = Math.min(minFFTLength, fftLen);
			maxFFTLength = Math.max(maxFFTLength, fftLen);
		}
		channelMap |= fftDataUnit.getChannelBitmap();
		int iChan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		maxChannel = Math.max(maxChannel, iChan);
		channelCount[iChan] ++;
		maxChannelCount = Math.max(maxChannelCount, channelCount[iChan]);
		minChannelCount = maxChannelCount;
		for (int i = 0; i <= maxChannel; i++) {
			minChannelCount = Math.min(minChannelCount, channelCount[iChan]);
		}
		fftDataUnits.add(fftDataUnit);
		return minFFTLength == maxFFTLength;
	}
	
	/**
	 * Merge the other list into this one ....
	 * @param otherList Other list of FFT data (can be null in which 
	 * case nothing will happen);
	 * @return number of FFT data units added
	 */
	public int mergeIn(FFTDataList otherList) {
		if (otherList == null) {
			return 0;
		}
		ArrayList<FFTDataUnit> ffts = otherList.fftDataUnits;
		if (ffts == null) {
			return 0;
		}
		for (FFTDataUnit fftDataUnit:ffts) {
			addData(fftDataUnit);
		}
		return ffts.size();
	}
	
	/**
	 * Get the FFT data units for a single channel. 
	 * @param iChannel channel index
	 * @return list for a single channel. 
	 */
	public FFTDataUnit[] getChannelData(int iChannel) {
		int chMap = 1<<iChannel;
		int n = channelCount[iChannel];
		if (n == 0) {
			return null;
		}
		/**
		 * This all looks a little fragile, but shouldn't ever go wrong if this class if functioning correctly!!!!
		 */
		FFTDataUnit[] dataUnits = new FFTDataUnit[n];
		int iP = 0;
		for (FFTDataUnit f:this.fftDataUnits) {
			if (f.getChannelBitmap() == chMap) {
				dataUnits[iP++] = f;
			}
		}
		return dataUnits;
	}
	
	/**
	 * Get all the data separated by channel. The order will be sequential
	 * since all the data are being picked by channel map. 
	 * @return arrays of all data by channel
	 */
	public FFTDataUnit[][] getChannelSeparatedData() {
		int nChan = PamUtils.getNumChannels(channelMap);
		FFTDataUnit[][] dataUnits = new FFTDataUnit[nChan][];
		for (int i = 0; i < nChan; i++) {
			int iChan = PamUtils.getNthChannel(i, channelMap);
			dataUnits[i] = getChannelData(iChan);
		}
		return dataUnits;
	}

	/**
	 * Check channel ordering is all OK still. 
	 * @return true if channels correctly interleaved. 
	 */
	public boolean interleaveOK() {
		int nChan = PamUtils.getNumChannels(channelMap);
		int nFFT = fftDataUnits.size();
		if (nFFT%nChan != 0) {
			return false; // not a multiple !
		}
		int nPerChan = nFFT/nChan;
		int iF = 0;
		for (int f = 0; f < nPerChan; f++) {
			for (int i = 0; i < nChan; i++) {
				int expChan = PamUtils.getNthChannel(i, channelMap);
				int chan = PamUtils.getSingleChannel(fftDataUnits.get(iF++).getChannelBitmap());
				if (chan != expChan) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return the fftDataUnits
	 */
	public ArrayList<FFTDataUnit> getFftDataUnits() {
		return fftDataUnits;
	}

	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}

	/**
	 * @return the channelCount
	 */
	public int[] getChannelCount() {
		return channelCount;
	}

	/**
	 * @return the minChannelCount
	 */
	public int getMinChannelCount() {
		return minChannelCount;
	}

	/**
	 * @return the maxChannelCount
	 */
	public int getMaxChannelCount() {
		return maxChannelCount;
	}

	/**
	 * @return the minFFTLength
	 */
	public int getMinFFTLength() {
		return minFFTLength;
	}

	/**
	 * @return the maxFFTLength
	 */
	public int getMaxFFTLength() {
		return maxFFTLength;
	}

	/**
	 * FFTDataList carries it's own sample rate, this is so that it
	 * can be easily used with upsampled data during localisation, 
	 * i.e. the sample rate in the data may no longer be the same
	 * as the sample rate in the source of the data !
	 * @return the sampleRate in Hz
	 */
	public double getSampleRate() {
		return sampleRate;
	}

		
}
