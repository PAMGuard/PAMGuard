package RightWhaleEdgeDetector;

import java.util.Arrays;

public class RWESound {

	long timeMilliseconds;
	protected int deadCount = 0;
	public boolean peakStolen = false;
	public int sliceCount = 0;
	public int[] sliceList;
	public int[] lowFreq;
	public int[] highFreq;
	public int[] peakFreq;
	public double[] peakAmp;
	public double signal, noise;
	public int maxOT; // max number of bins over threshold. 
	// stuff calculated at the end. 
	int minFreq, maxFreq;
	int meanWidth, maxWidth;
	int minPos, maxPos;
	int duration;
	int canon1;
	public int soundType;

	public RWESound(long timeMilliseconds, RWEDetectionPeak aPeak, int numOT) {
		this.timeMilliseconds = timeMilliseconds;
		sliceList = new int[1];
		lowFreq = new int[1];
		highFreq = new int[1];
		peakFreq = new int[1];
		peakAmp = new double[1];
		sliceCount = 1;
		sliceList[0] = 0;
		lowFreq[0] = aPeak.bin1;
		highFreq[0] = aPeak.bin2;
		peakFreq[0] = aPeak.peakBin;
		peakAmp[0] = aPeak.maxAmp;
		signal = aPeak.signal;
		noise = aPeak.noise;
		this.maxOT = numOT;
	}

	protected void completeSound() {
		/*
		 *  do some basic processing when the sound has 
		 *  competed to get some min and max and mean values, etc. 
		 */
		minFreq = maxFreq = peakFreq[0];
		meanWidth = maxWidth = highFreq[0]-lowFreq[0];
		minPos = maxPos = 0;
		int w;
		for (int i = 1; i < sliceCount; i++) {
			if(peakFreq[i] <  minFreq) {
				minFreq = peakFreq[i];
				minPos = i;
			}
			if(peakFreq[i] >  maxFreq) {
				maxFreq = peakFreq[i];
				maxPos = i;
			}
			w = highFreq[i]-lowFreq[i];
			maxWidth = Math.max(maxWidth, w);
			meanWidth += w;
		}
		meanWidth /= sliceCount;
		duration = sliceList[sliceCount-1] + 1;
	}

	/**
	 * Add one more bin to everything in the sound
	 */
	public void extendAllocation() {
//		if (sliceCount == 0) {
//			sliceList = new int[1];
//			lowFreq = new int[1];
//			highFreq = new int[1];
//			peakFreq = new int[1];
//			peakAmp = new double[1];
//			sliceCount = 1;
//		}
//		else {
			sliceCount++;
			sliceList = Arrays.copyOf(sliceList, sliceCount);
			lowFreq = Arrays.copyOf(lowFreq, sliceCount);
			highFreq = Arrays.copyOf(highFreq, sliceCount);
			peakFreq = Arrays.copyOf(peakFreq, sliceCount);
			peakAmp = Arrays.copyOf(peakAmp, sliceCount);
//		}
	}

	/**
	 * Add a new peak to extend the slice. 
	 * @param sliceNum slice num relative to start of sound
	 * @param newPeak new peak with associated data. 
	 * @param numOT number of bins over threshold. 
	 */
	public void addPeak(int sliceNum, RWEDetectionPeak newPeak, int numOT) {
		extendAllocation();	
		int newSlice = sliceCount-1;
		sliceList[newSlice] = sliceNum;
		lowFreq[newSlice] = newPeak.bin1;
		highFreq[newSlice] = newPeak.bin2;
		peakFreq[newSlice] = newPeak.peakBin;     
		peakAmp[newSlice] = newPeak.maxAmp;
		signal += newPeak.signal;
		noise += newPeak.noise;
		maxOT = Math.max(maxOT, numOT);
		deadCount = 0;
	}

	public String getTypeString()  {
		return getTypeString(soundType);
	}
	
	public static String getTypeString(int soundType) {
		switch(soundType) {
		case 0:
			return "Short bit of noise";
		case 1:
			return "Sound is too long";
		case 2:
			return "Not an upsweep";
		case 3:
			return "Sweep up < 21 Hz";
		case 4:
			return "Sweep up < 75 Hz";
		case 5:
			return "Bad start or end frequency";
		case 6:
			return "Bad min or max positions";
		case 7:
			return "Min frequency is too low (<70Hz)";
		case 8:
			return "Big downseep at start of sound";
		case 9:
			return "Right Whale, quality 0";
		case 10:
			return "Right Whale, quality 1";
		case 11:
			return "Right Whale, quality 2";
		case 12:
			return "Right Whale, quality 3";
		}
		return "Unknown sound type";
	}
	
	public static int getNumSoundTypes() {
		return 12;
	}
}
