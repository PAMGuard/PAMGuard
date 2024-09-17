package whistleDetector;

import java.util.ListIterator;

import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import fftManager.FastFFT;

/**
 * 
 * @author Doug Gillespie
 * Utility functions to measure time delays for whistles and 
 * eventually to get localisations. 
 *
 */
public class WhistleLocaliser {

	WhistleControl whistleControl;

	static final int WHISTLE_NODELAY = Integer.MIN_VALUE;

	public WhistleLocaliser(WhistleControl whistleControl) {
		this.whistleControl = whistleControl;
	}

	ComplexArray complexData;

	private FastFFT fastFFt = new FastFFT();
	/**
	 * Calculates the delay between a whistle on one channel and another given channel
	 * number. Used the whistle contour width and the sweep to calculate only in a very
	 * narrow band so that overlapping whistles may simultaneously get bearings 
	 * calculated.
	 * @param whistle
	 * @return a delay in bins or WHISTLE_NODELAY. 
	 */
	public int getDelay(FFTDataBlock fftDataBlock, WhistleShape whistle, int whistleChannel, int bearingChannel) {
		/*
		 * General aproach for measuring a delay that is reasonably short (< half the length
		 * of a FFT time partition) is to multiply one FFT by the complex conjugae of the other
		 * half, then take the inverse transform. 
		 * This is extended here, but summing the fft's for all time bins within a whitle in the
		 * frequency domain so that only a single inverse fft is performed. Only accumalate
		 * complex data within the frequency bins indicated in thepeak data. 
		 */
		// first need to find the original FFT data source to get some info about the FFT data.
		int fftLength = fftDataBlock.getFftLength();

		// check memory allocation for the complex data. 
		complexData = new ComplexArray(fftLength);
		/*
		 * find the fft data from the start of the whistle
		 */ 
		FFTDataUnit u1 = null, u2 = null;
		//		int iPeak = 0;
		u1 = u2 = fftDataBlock.getFirstUnitAfter(whistle.GetPeak(0).timeMillis);
		if (u1 == null) return WHISTLE_NODELAY;
		int channels1 = 1<<whistleChannel;
		int channels2 = 1<<bearingChannel;
		int correlationCount = 0;
		double[] c1;
		double[] c2;
		int f1, f2;
		if (whistle == null) {
			System.out.println("Null Whistle");
		}
		WhistlePeak peak = whistle.GetPeak(0);
		if (peak == null) return WHISTLE_NODELAY;
		u1 = fftDataBlock.findDataUnit(peak.timeMillis, channels1, PamDataBlock.ITERATOR_END);
		u2 = fftDataBlock.findDataUnit(peak.timeMillis, channels2, PamDataBlock.ITERATOR_END);
		if (u1 == null) {
			u1 = fftDataBlock.getFirstUnit();
			if (u1 == null) return WHISTLE_NODELAY;
		}
		if (u2 == null) {
			u2 = fftDataBlock.getFirstUnit();
			if (u2 == null) return WHISTLE_NODELAY;
		}
		synchronized (fftDataBlock.getSynchLock()) {
			ListIterator<FFTDataUnit> fftIterator1 = 
				fftDataBlock.getListIterator(peak.timeMillis, channels1, PamDataBlock.MATCH_EXACT, PamDataBlock.POSITION_BEFORE);
			ListIterator<FFTDataUnit> fftIterator2 = 
				fftDataBlock.getListIterator(peak.timeMillis, channels2, PamDataBlock.MATCH_EXACT, PamDataBlock.POSITION_BEFORE);
			//		fftDataBlock.getPreceedingUnit(fftIterator, peak.timeMillis);
			// and go back a bit further if necessary to make sure we're earlier than the time given
			//		while (fftIterator.hasPrevious()) {
			//			if (fftIterator.previous().getTimeMilliseconds() < )
			//		}
			if (fftIterator1 == null || fftIterator2 == null) {
				return WHISTLE_NODELAY;
			}
			for (int iSlice = 0; iSlice < whistle.getSliceCount(); iSlice++) {
				//			WhistlePeak peak = whistle.GetPeak(iSlice);
				peak = whistle.GetPeak(iSlice);
				//			u1 = fftDataBlock.findDataUnit(peak.timeMillis, channels1, u1.getAbsBlockIndex());
				//			u2 = fftDataBlock.findDataUnit(peak.timeMillis, channels2, u2.getAbsBlockIndex());
				while (fftIterator1.hasNext()) {
					u1 = fftIterator1.next();
					if (u1.getChannelBitmap() == channels1) {
						break;
					}
					else {
						u1 = null;
					}
				}
				while (fftIterator2.hasNext()) {
					u2 = fftIterator2.next();
					if (u2.getChannelBitmap() == channels2) {
						break;
					}
					else {
						u2 = null;
					}
				}

				if (u1 != null && u2 != null && u1.getTimeMilliseconds() == u2.getTimeMilliseconds()) {
					// have found both channels, so do some of the bearing calculation.
					c1 = u1.getFftData().getData();
					c2 = u2.getFftData().getData();
					f1 = Math.max(0, peak.MinFreq);
					f2 = Math.min(fftLength / 2 - 1, peak.MaxFreq);
					double[] cd = complexData.getData();
					
					for (int i = f1, re = f1*2, im = f1*2+1; i <= f2; i++, im+=2, re+=2) {
						cd[re] += c1[re]*c2[re] + c1[im]*c2[im];
						cd[im] = c1[im]*c2[re] - c1[re]*c2[im];
//						complexData[i].real += c1[i].real * c2[i].real + c1[i].imag * c2[i].imag;
//						complexData[i].imag += c1[i].imag * c2[i].real - c1[i].real * c2[i].imag;
					}
					correlationCount++;
					//				iPeak++;
					//				peak = whistle.GetPeak(iPeak);
				}		
				else {
					//				getDelay(fftDataBlock, whistle, whistleChannel, bearingChannel);
					break;
				}

			}
		}
		if (correlationCount < 3) {
			//			System.out.print("Not enough data - ");
			return WHISTLE_NODELAY;
		}

		// now take the inverse fft ...
		// FastFFT.ifft(corrData, log2FFTLen);
		fastFFt.ifft(complexData, fftDataBlock.getFftLength());

		// now find the maximum, if it's in the second half, then
		// delay is that - fftLength
		double corrMax = 0;
		int delay = WHISTLE_NODELAY;
		double sumSq = 0;
		for (int i = 0; i < fftLength; i++) {
			if (complexData.getReal(i) > corrMax) {
				corrMax = complexData.getReal(i);
				delay = i;
			}
			sumSq += complexData.magsq(i);
		}

		if (delay > fftLength / 2) {
			delay -= fftLength;
		}
		return -delay;
	}

}
