package RightWhaleEdgeDetector;

/**
 * Standard classifier which does exactly the same as the old edge detector
 * classifier. <p>
 * decide how much we like this sound and return one of the following:
 *   0 - totally uninteresting short sound <p>
 *   1 - passes basic loose cut - between .5 and 2 s long, Maximum comes later
 *       than minimum in time, Minimum frequency between 30 and 200 Hz, total sweep >= 23Hz <p>
 *   2 - passes slightly tighter cuts - as above, but sweep >= 54 Hz, Occupancy > 80%
 *       MaxWidth <= 20 fft bins (156Hz), MinFreq between 50 and 160Hz,
 *       MinPos <= 50% of sound duration, MaxPos >= 70% of sound duration<p>
 *   3-6 passes one of four different cuts on Canonical variable.
 *	<p>
 *   Code was written for 2000 Hz data with a 256 pt FFt length
 *   If the frequency is not 2000 Hz, the fft length should be scaled
 *   accordingly
 *   The following code should mitigate against changes though
 *
 * @author Doug Gillespie
 *
 */
public class RWStandardClassifier implements RWClassifier {

	private float sampleRate;
	private int fftLength;
	private int fftHop;

	int MaxStartFreq = 25;
	int MinStartFreq = 3;
	int MaxMaxWidth = 20;
	int MinSweep1 = 3;
	int MinSweep2 = 7;  
	int MinLen = 8;
	int MaxLen = 32;
	int First = 1;
	int MaxMinPos = 50;
	int MinMaxPos = 70;
	int MinMinFreq = 7;
	static public final int MNSCALE = 100;
	static public final int MAXSOUNDTYPE = 12;

	int[] mn100 = {1401, 1137, 1481, 1046};  // mean * 100
	int[] ev1000 = {-621, 1766, 396, -3156}; // ev * 10000
	// the whole eignesum is now scaled up by 1000000, so move the cuts up accordingly
	int[] canonCut = {0, 700000, 1250000};

	@Override
	public void setSoundData(float sampleRate, int fftLength, int fftHop) {
		this.sampleRate = sampleRate;
		this.fftLength = fftLength;
		this.fftHop = fftHop;

		MaxStartFreq = (int) (157L * fftLength / (int) sampleRate);
		MinStartFreq = (int) (55L * fftLength / (int) sampleRate);

		// New code added 9/12/05
		MinMinFreq = (int) (70L * fftLength / (int) sampleRate);

		MaxMaxWidth = 20;
		MinSweep1 = (int) (24L * fftLength / (int) sampleRate);
		 MinSweep2 = (int) (55L * fftLength / (int) sampleRate);
		// 23 November 2003, change follwoing correspondence with CC to try to remove false detections.
		//MinSweep2 = (int) (94L * (int) fftLength / (int) sampleRate);
		MinLen = (int) ((sampleRate / fftHop) / 2);
		MaxLen = (int) ((sampleRate / fftHop) * 2);
	}

	@Override
	public int getSoundClass(RWESound aSound) {
		int soundType = 0;
		
		if (!isTonal(aSound)) {
			return 0;
		}
		if (aSound.duration < MinLen) return soundType;

		soundType = 1;
		// if it's too long, return 1
		if (aSound.duration > MaxLen) return soundType;

		soundType = 2;
		// if it sweeps down, return 2
		if (aSound.maxPos <= aSound.minPos) return soundType;

		soundType = 3;
		// if it sweeps up, but < 3 bins (21 Hz) return 3
		if ((aSound.maxFreq - aSound.minFreq) < MinSweep1) return soundType;

		soundType = 4;
		// if it sweeps up, but < 7 bins (55 Hz) return 4
		if ((aSound.maxFreq - aSound.minFreq) < MinSweep2) return soundType;

		soundType = 5;
		// now only left with upsweeps. If they don't start in about
		// the right place, (24 to 200 Hz) return 5
		if (aSound.peakFreq[0] > MaxStartFreq) return soundType;
		if (aSound.peakFreq[0] < MinStartFreq) return soundType;

		// should now be left with upsweeps starting between 24 and 200 Hz
		// which sweep by at least 50 Hz and are between .5 and 2 s long
		soundType = 6;
		// return 6 if the min and max positions are in a no-good place
		int minPos = aSound.minPos * 100 / (aSound.sliceCount-1);
		if (minPos > MaxMinPos) return soundType;
		int maxPos = aSound.maxPos * 100 / (aSound.sliceCount-1);
		if (maxPos < MinMaxPos) return soundType;

		// New code added 9/12/05
		soundType = 7;
		// return 7 if the minimum point in the contour is below some level (70Hz)
		if (aSound.minFreq < MinMinFreq) return soundType;

		soundType = 8;
		// return 8 if there is too big a drop from the start of the call to the lowest frequency in the call.
		if (aSound.peakFreq[0] - aSound.minFreq > 3) return soundType;
		// end of new code added 9/12/05

		soundType = 9;
		// now construct the eigensum for the mv statistic
		// note that f and t values are scale up by *MNSCALE;
		int eigenSum = 0;
		eigenSum += (MNSCALE * aSound.peakFreq[0] - mn100[0]) * ev1000[0];
		eigenSum += (MNSCALE * (aSound.maxFreq-aSound.minFreq) - mn100[1]) * ev1000[1];
		eigenSum += (MNSCALE * aSound.duration - mn100[2]) * ev1000[2];
		eigenSum += (MNSCALE * aSound.maxWidth - mn100[3]) * ev1000[3];
		if (eigenSum < 0) {     // added 24/2/04
			aSound.canon1 = (int) -Math.sqrt(-eigenSum);
		}
		else {
			aSound.canon1 = (int) Math.sqrt(eigenSum);
		}
		for (int i = 0; i < 3; i++) {
			if (eigenSum > canonCut[i]) {
				soundType++;
			}
		}

		return soundType;
	}

	private boolean isTonal(RWESound aSound) {
		return (aSound.maxWidth <= 20);
	}
}
