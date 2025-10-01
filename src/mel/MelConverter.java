package mel;


/**
 * does the work of creating a Mel Spec. Keeping out of MelProcess so that
 * it could be used independently if needed. 
 * @author dg50
 *
 */
public class MelConverter {

	private double sampleRate, minFrequency, maxFrequency;
	private int nFFT, nMel;
	private double[][] melMatrix;
	private double power = 2.0;
	private int[] lowBins; // lowest bin for each mel
	private int[] highBins; //highest bin for each mel
	private int lowestBin, highestBin; // abs outer limits. Saves poweering all data. 
	
	public MelConverter(double sampleRate, double minFrequency, double maxFrequency, int nFFT, int nMel, double power) {
		super();
		this.sampleRate = sampleRate;
		this.minFrequency = minFrequency;
		this.maxFrequency = maxFrequency;
		this.nFFT = nFFT;
		this.nMel = nMel;
		this.power = power;
		calculateCoefficients();
	}
	
	/**
	 * Calculate the mel coefficients
	 */
	private void calculateCoefficients() {
		AudioFeatureExtraction afe = new AudioFeatureExtraction();
		afe.setSampleRate(sampleRate);
		afe.setfMin(minFrequency);
		afe.setfMax(maxFrequency);
		afe.setN_fft(nFFT);
		afe.setN_mels(nMel);
		melMatrix = afe.melFilter();
		int n = melMatrix.length;
		int m = melMatrix[0].length;
		lowestBin = m;
		highestBin = 0;
		lowBins = new int[n];
		highBins = new int[n];
		for (int iMel = 0; iMel < n; iMel++) {
			lowBins[iMel] = m;
			for (int iF = 0; iF < m; iF++) {
				if (melMatrix[iMel][iF] > 0.) {
					lowestBin = Math.min(lowestBin, iF);
					highestBin = Math.max(highestBin, iF);
					lowBins[iMel] = Math.min(lowBins[iMel], iF);
					highBins[iMel] = Math.max(highBins[iMel], iF);
				}
			}
		}
	}
	
	/**
	 * Convert real FFT data into Mels. fftData assumed to be on amplitude scale
	 * @param fftData
	 * @return
	 */
	public double[] melFromMag(double[] fftData) {
		double[] mels = melWithPower(fftData, power);
		scalePower(mels, 1./power);
		return mels;
	}
	
	public double[] melFromMagSq(double[] fftDataSq) {
		double data[] = melWithPower(fftDataSq, power/2.);
		scalePower(data, 1./power);
		return data;
	}

	/**
	 * Called from the mag or magsq functions to try to more efficiently 
	 * handle powering up any data. 
	 * @param fftData
	 * @param nPower
	 * @return
	 */
	private double[] melWithPower(double[] fftData, double nPower) {
		if (nPower == 1.0) {
			return singlePower(fftData);
		}
		else {
			return anyPower(fftData, nPower);
		}
	}

	/**
	 * Do the matrix multiplication using a power law
	 * @param fftData
	 * @param nPower 
	 * @return
	 */
	private double[] anyPower(double[] fftData, double nPower) {
		double[] magData = new double[fftData.length];
		highestBin = Math.min(fftData.length-1, highestBin);
		for (int i = lowestBin; i <= highestBin; i++) {
			magData[i] = Math.pow(fftData[i], nPower);
		}
		return singlePower(magData);
	}

	/**
	 * Do the matrix multiplication using no power. Either because that's what
	 * we want, or because the data have already been raised to the correct power. 
	 * @param poweredData
	 * @return
	 */
	private double[] singlePower(double[] poweredData) {
		double[] mels = new double[nMel];
		for (int iMel = 0; iMel < nMel; iMel++) {
			int h = Math.min(poweredData.length-1, highBins[iMel]);
			for (int iF = lowBins[iMel]; iF <= h; iF++) {
				mels[iMel] += poweredData[iF];
			}
		}
		return mels;
	}

	/**
	 * Raise all elements in data to given power
	 * @param data
	 * @param power
	 */
	private void scalePower(double[] data, double power) {
		if (power == 1.) {
			return;
		}
		for (int i = 0; i < data.length; i++) {
			data[i] = Math.pow(data[i], power);
		}
	}
	
	public double[] melFromComplex(PamUtils.complex.ComplexArray complexArray) {
		return melFromMagSq(complexArray.magsq());
	}

	/**
	 * @return the sampleRate
	 */
	public double getSampleRate() {
		return sampleRate;
	}

	/**
	 * @return the minFrequency
	 */
	public double getMinFrequency() {
		return minFrequency;
	}

	/**
	 * @return the maxFrequency
	 */
	public double getMaxFrequency() {
		return maxFrequency;
	}

	/**
	 * @return the nMel
	 */
	public int getnMel() {
		return nMel;
	}

	/**
	 * @return the melMatrix
	 */
	public double[][] getMelMatrix() {
		return melMatrix;
	}
	

}
