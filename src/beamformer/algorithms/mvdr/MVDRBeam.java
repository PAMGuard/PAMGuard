package beamformer.algorithms.mvdr;


import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import beamformer.algorithms.BeamInformation;
import pamMaths.PamVector;

public class MVDRBeam extends BeamInformation {
	
	/**
	 * A reference to the beam former creating this beam
	 */
	MVDRalgorithm beamformer;
	
	/**
	 * Channel map describing the channels (hydrophones) used in this beam
	 */
	private int channelMap;

	/**
	 * Sequence map containing the sequence number for this beam
	 */
	private int sequenceMap=0;

	/**
	 * A PamVector object describing the beam (calculated from of heading and slant angle).
	 */
	private PamVector beamVec;
	
	/**
	 * An array of PamVector objects, of size numElementsInBeam.  Each PamVector describes the location of the hydrophone element.
	 */
	private PamVector[] elementLocs;
	
	/**
	 * A 2-index array with the frequency range to analyse.  Index 0 = min freq, index 1 = max freq
	 */
	private double[] freqBins;
	
	/**
	 * The index number in the FFT data unit that corresponds to the minimum frequency to analyse.  This refers to
	 * the entire frequency range of analysis as specified by the user in the parameters GUI
	 */
	private int startIdx;
	
	/**
	 * A matrix containing Complex steering vectors.  Rows = fft bins, and Columns = elements
	 */
	private FieldMatrix<Complex> steering;
	
	/**
	 * A matrix containing the complex conjugate steering vectors.  Rows = fft bins, and Columns = elements
	 */
	private FieldMatrix<Complex> steeringConj;

	/**
	 * The speed of sound in the water
	 */
	private double speedOfSound;
	
	/**
	 * the number of FFT bins this beam needs to process.  This is the frequency range OF ANALYSIS, from the parameters GUI,
	 * and NOT the entire frequency range of the fftDataUnit
	 */
	private int numFFTBins;
	
	/**
	 * Main constructor
	 * 
	 * @param mvdRalgorithm
	 * @param channelMap
	 * @param sequenceNum
	 * @param beamVec
	 * @param elementLocs
	 * @param channelList
	 * @param weights
	 * @param freqBins
	 * @param speedOfSound
	 */
	public MVDRBeam(MVDRalgorithm mvdRalgorithm, int channelMap, int sequenceNum, PamVector beamVec,
			PamVector[] elementLocs, double[] freqBins, double speedOfSound) {
		this.beamformer = mvdRalgorithm;
		this.channelMap = channelMap;
		this.sequenceMap = PamUtils.SetBit(0, sequenceNum, true);
		this.beamVec = beamVec;
		this.elementLocs = elementLocs;
		this.freqBins = freqBins;
		this.speedOfSound = speedOfSound;
		
		// create the steering vectors
		calcSteeringVec();
	}
	
	
	
	/**
	 * Calculates a steering vector over all hydrophones and all frequency bins in the fft<p>
	 * A phase delay of the form e<sup>j&#969t</sup> is calculated for each frequency bin, where &#969=the angular
	 * frequency (=2*&#960*center-of-freq-bin) and t is the time delay based on the location of the hydrophone.<p>
	 * t is calculated as the location vector of the hydrophone / speed of sound (=locVec/c).<p>
	 * The wave number k is defined as &#969/c.  Substituting for t gives the phase delay in the form
	 * e<sup>j*k*locVec</sup>, which can also be expressed through Euler's formula as cos(k*locVec)+j*sin(k*locVec).
	 * The cos/sin values are saved to the steering FieldMatrix, and a conjugate form is saved to the steeringConj FieldMatrix.
	 * 
	 * @param elementNumber the hydrophone number
	 * @return a ComplexArray[] vector of length numFreqBins.  Each ComplexArray in the vector contains a double[] vector, which
	 * holds the complex steering value for each hydrophone at that frequency bin
	 */
	protected void calcSteeringVec() {
		
		// Calculate the number of frequency bins in the required freq range, based on the fft source params.  The center value
		// of any frequency bin is (i+0.5)*hzPerBin, where i=index number and hzPerBin = Fs / nfft
		double hzPerBin = beamformer.getBeamProcess().getFftDataSource().getSampleRate() / beamformer.getBeamProcess().getFftDataSource().getFftLength();
		startIdx = beamformer.getBeamProcess().frequencyToBin(freqBins[0]);
		int endIdx = beamformer.getBeamProcess().frequencyToBin(freqBins[1]);
		numFFTBins = endIdx-startIdx;	// note that frequencyToBin may return the total number of bins, not necessarily the last bin index.  So don''t do end-start+1
		int nFreq = beamformer.getBeamProcess().getFftDataSource().getFftLength()/2;
		
		// Initialise the steering vector objects
		steering = MatrixUtils.createFieldMatrix(ComplexField.getInstance(), nFreq, elementLocs.length);
		steeringConj = MatrixUtils.createFieldMatrix(ComplexField.getInstance(), nFreq, elementLocs.length);
		
		// loop through the data, first over the element locations and then over the frequency bins, calculating the phase delay for each
		// and storing in a double vector in real,imaginary pairs.
		for (int i=0; i<nFreq; i++) {
			
			// loop through all frequency bins
			for (int j=0; j<elementLocs.length; j++) {

				// calculate the frequency for this iteration and the corresponding wavenumber vector
				double k = 2*Math.PI*((i+0.5)*hzPerBin)/speedOfSound;
				PamVector kVec = beamVec.times(k);

				// calculate the phase delay by getting the dot product of the location and wavenumber vectors
				double phaseDelay = kVec.dotProd(elementLocs[j]);
				
				// put the real and imaginary components into the steering vectors
				steering.setEntry(i, j, new Complex(Math.cos(phaseDelay), Math.sin(phaseDelay)));
				steeringConj.setEntry(i, j, new Complex(Math.cos(phaseDelay), -1*Math.sin(phaseDelay)));
			}		
		}		
	}
	
	/**
	 * Process a set of FFT Data units over the entire frequency range
	 * @return complex array of summed beamformed data. Note that the size of the array is the number of frequency bins in the full
	 * frequency range, as specified by the user in the parameters GUI
	 */
	public ComplexArray process(FieldMatrix<Complex>[] Rinv) {
		return process(Rinv, startIdx, numFFTBins, Rinv.length);
	}

	/**
	 * Process a set of FFT Data units.  Note that we may only be processing a portion of the frequency range
	 * 
	 * @param Rinv array containing one FFTDataUnit for each channel to analyse
	 * @param startBin the index in the FFTDataUnit to start processing at.
	 * @param numBins the number of FFT bins to process
	 * @param fullNumFFTBins the number of fft bins in an FFTDataUnit object
	 * 
	 * @return complex array of summed beamformed data. Note that the size of the array is the number of frequency bins in the full
	 * frequency range, as specified by the user in the parameters GUI, even if only a portion of that range was actually processed
	 */
	public ComplexArray process(FieldMatrix<Complex>[] Rinv, int startBin, int numBins, int fullNumFFTBins) {
		
		// initialise the return array to be large enough to hold the entire frequency range.  If we are only processing
		// a portion of that range, the other bins will simply be 0
		ComplexArray summedData = new ComplexArray(fullNumFFTBins);
		
		// loop over the FFT bins
		for (int fftBin=0; fftBin<numBins; fftBin++) {
			Complex finalVal = new Complex(0., 0.);

			// calculate the power at this FFT bin, using the formula P=1/(e' * Rinv * e)
			ArrayFieldVector<Complex> intermediateVec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), Rinv[fftBin].getColumnDimension());
			for (int col=0; col<Rinv[fftBin].getColumnDimension(); col++) {
				intermediateVec.setEntry(col, steeringConj.getRowVector(startBin+fftBin).dotProduct(Rinv[fftBin].getColumnVector(col)));
			}
			/*
			 * Need to break this up a bit to trap a nan at the point where the reciprocol is taken
			 * which will happen when intermediateVec is all zeros. 
			 */
//			finalVal = (intermediateVec.dotProduct(steering.getRowVector(startBin+fftBin)).reciprocal()).sqrt();
			Complex dotProd = intermediateVec.dotProduct(steering.getRowVector(startBin+fftBin));
			if (dotProd.getReal() == 0 && dotProd.getImaginary() == 0) {
				finalVal = new Complex(0,0);
			}
			else {
				finalVal = dotProd.reciprocal().sqrt();
			}
			if (Double.isNaN(finalVal.abs())) {
//				FieldVector<Complex> row = steering.getRowVector(startBin+fftBin);
//				Complex dot = intermediateVec.dotProduct(row);
//				Complex rec = dot.reciprocal();
//				Complex sq = rec.sqrt();
//				finalVal = (intermediateVec.dotProduct(steering.getRowVector(startBin+fftBin)).reciprocal()).sqrt();
				finalVal = new Complex(0,0);
			}

			summedData.set(startBin+fftBin, finalVal.getReal(), finalVal.getImaginary() );
			
		}
		
		// return the summedData
		return summedData;
	}

	/**
	 * @return the sequenceMap
	 */
	public int getSequenceMap() {
		return sequenceMap;
	}

	/**
	 * Return the index number in the FFT data unit that corresponds to the minimum frequency to analyse.  This refers to
	 * the entire frequency range as specified by the user in the parameters GUI
	 * @return the starting index number
	 */
	public int getStartIdx() {
		return startIdx;
	}

/**
 * Return the number of FFT bins this beam needs to process.  This is the entire frequency range, as specified by the user in the parameters GUI
 * @return the number of frequency bins in the entire frequency range
 */
	public int getNumFFTBins() {
		return numFFTBins;
	}
	
	

}
