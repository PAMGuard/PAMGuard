package likelihoodDetectionModule;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import fftManager.FFTParameters;
import Spectrogram.WindowFunction;

/**
 * The LikelihoodFFTParameters class represents Likelihood Detector-specific FFT
 * parameters and calculations that support the auto-creation of processing streams
 * for the target configurations. It takes in a combination of user-suggested parameters
 * and source data information and generates actual FFT parameters from them.
 */
public class LikelihoodFFTParameters implements Serializable, ManagedParameters {
	
	/** The constant serialVersionUID that is required by Serializable. */
	static final long serialVersionUID = 563;
	
	/** The fft overlap. */
	private double[] overlap = new double[1];
	
	/** The fft size. */
	private int fftSize;
	
	/** The fft hop. */
	private int fftHop;
	
	/** The channel map. */
	private int channelMap;
	
	/** The source number of the audio data source to be used (i.e., the acquisition module). */
	private int sourceNumber;
	
	/** The number averages. */
	private int[] numberAverages = new int[1];
	
	/** The actual time resolution, calculated from the requested time resolution. */
	private double actualTimeResolution;
	
	/** The actual freq resolution, calculated from the requested frequency resolution. */
	private double actualFreqResolution;

	/**
	 * Instantiates a new likelihood fft parameters.
	 * 
	 * @param acquisitionSettings The acquisition settings obtained from the audio data source.
	 * @param channelMap The channel map requested.
	 * @param requestedFrequencyResolution The requested frequency resolution.
	 * @param requestedTimeResolution The requested time resolution.
	 */
	public LikelihoodFFTParameters( AcquisitionSettings acquisitionSettings,
									   int channelMap,
								       double requestedFrequencyResolution,
								       double requestedTimeResolution ) {

		this.sourceNumber = acquisitionSettings.sourceNumber;
		this.channelMap = channelMap;
		this.fftSize = calculateFftSize( acquisitionSettings, requestedFrequencyResolution );
		calculateNumberAveragesAndOverlap( requestedTimeResolution, this.fftSize, acquisitionSettings.samplingRateHz,
				this.numberAverages, this.overlap );
		
		this.fftHop = calculateHop( this.fftSize, this.overlap[ 0 ] );
		
		this.actualTimeResolution = (double)this.fftHop / acquisitionSettings.samplingRateHz * this.numberAverages[0];
		this.actualFreqResolution = acquisitionSettings.samplingRateHz / this.fftSize;
		
	}
	
	/**
	 * Calculate hop.
	 * 
	 * @param fftSize the fft size
	 * @param overlap the overlap
	 * 
	 * @return the int
	 */
	private static int calculateHop( int fftSize, double overlap ) {
		return (int)( 0.5 + fftSize * ( 1.0 - overlap ));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object o ) {
		if ( ! ( o instanceof LikelihoodFFTParameters ) ) return false;
		LikelihoodFFTParameters other = (LikelihoodFFTParameters)o;
		return ( this.overlap[0] == other.overlap[0] &&
				  this.fftHop == other.fftHop &&
				  this.channelMap == other.channelMap &&
				  this.sourceNumber == other.sourceNumber );
	}
	
	/**
	 * Gets the overlap.
	 * 
	 * @return the overlap
	 */
	public double getOverlap() {
		return this.overlap[0];	
	}
	
	/**
	 * Gets the fFT size.
	 * 
	 * @return the fFT size
	 */
	public int getFFTSize() {
		return this.fftSize;
	}
	
	/**
	 * Gets the fFT hop.
	 * 
	 * @return the fFT hop
	 */
	public int getFFTHop() {
		return this.fftHop;	
	}
	
	/**
	 * Gets the data source number.
	 * 
	 * @return the data source number
	 */
	public int getdataSourceNumber() {
		return 	this.sourceNumber;
	}
	
	/**
	 * Gets the channel map.
	 * 
	 * @return the channel map
	 */
	public int getChannelMap() {
		return this.channelMap;	
	}
	
	public int getSourceId() {
		return this.sourceNumber;	
	}
	
	/**
	 * Gets the actual frequency resolution.
	 * 
	 * @return the actual frequency resolution
	 */
	public double getActualFrequencyResolution() {
		return this.actualFreqResolution;	
	}
	
	/**
	 * Gets the actual time resolution.
	 * 
	 * @return the actual time resolution
	 */
	public double getActualTimeResolution() {
		return this.actualTimeResolution;
	}
	
	/**
	 * Gets the number averages.
	 * 
	 * @return the number averages
	 */
	public int getNumberAverages() {
		return this.numberAverages[0];
	}
	
	/**
	 * Gets the pam fft parameters.
	 * 
	 * @return the pam fft parameters
	 */
	public FFTParameters getPamFFTParameters() {
		FFTParameters pamFFTParameters = new FFTParameters();
		pamFFTParameters.channelMap = this.channelMap;
		pamFFTParameters.dataSource = this.sourceNumber;
		pamFFTParameters.fftHop = this.fftHop;
		pamFFTParameters.fftLength = this.fftSize;
		pamFFTParameters.windowFunction = WindowFunction.HANNING;
		
		return pamFFTParameters;
	}
	
	/**
	 * Calculate fft size.
	 * 
	 * @param aquisitionSettings the aquisition settings
	 * @param frequencyResolution the frequency resolution
	 * 
	 * @return the int
	 */
	private static int calculateFftSize( AcquisitionSettings aquisitionSettings, double frequencyResolution ) {
		int firstTry = 0;
		int secondTry = 0;
		int retval = 0;
		double actualRes = 0;
		double threshold = 0.25;
		
		firstTry = nextPowerOfTwo( (int)(aquisitionSettings.samplingRateHz / frequencyResolution ) );
		secondTry = (int) Math.pow( 2.0d, Math.log( firstTry ) / Math.log( 2.0d ) - 1 );
		actualRes = aquisitionSettings.samplingRateHz / secondTry;
		if ( (Math.abs( frequencyResolution - actualRes ) / frequencyResolution ) < threshold ) {
			retval = secondTry;
		}
		else {
			retval = firstTry;	
		}
		
		return retval;
	}
	
	// ------------------------------------------------------------------------
	
	/**
	 * Next power of two.
	 * 
	 * @param number the number
	 * 
	 * @return the int
	 */
	private static int nextPowerOfTwo( int number ) {
		int i = 2;
		while( i < number ) {
			i *= 2;	
		}
		
		return i;
	}
	
	// ------------------------------------------------------------------------ 
	
	/**
	 * Calculate number averages and overlap.
	 * 
	 * @param timeResolution the time resolution
	 * @param fftSize the fft size
	 * @param samplingRate the sampling rate
	 * @param numberAverages the number averages
	 * @param overlap the overlap
	 */
	private static void calculateNumberAveragesAndOverlap( double timeResolution,
															int fftSize,
															double samplingRate,
															int[] numberAverages,
															double[] overlap ) {
		double tempTimeResolution = 0;
		overlap[0] = 0.5;
		numberAverages[0] = 1;
		
		tempTimeResolution = calculateHop( fftSize, overlap[ 0 ] ) / samplingRate;
		
		if ( tempTimeResolution > timeResolution ) {
			overlap[0] = 1 - ( samplingRate * timeResolution / fftSize );
			
			// Limit the overlap to 90%, otherwise we will get very in-efficient
			if( overlap[0] > 0.9 ) {
				overlap[0] = 0.9;
			}
		}
		else {
			numberAverages[0] = (int) (timeResolution / tempTimeResolution + 0.5);
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("sourceNumber");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return sourceNumber;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

}
