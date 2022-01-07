package likelihoodDetectionModule.normalizer;


import likelihoodDetectionModule.spectralEti.SpectralEtiDataUnit;

/**
 *
 * This class implements the decaying average normalizer/estimator for the likelihoodDetectionModule.
 * The signal is estimated using a simple ExponentialAverager, and the background is estimated using
 * a TwoSpeedExponentialAverager.
 * 
 * @author Dave Flogeras
 */
public class DecayingAverageNorm implements Normalizer {
	
	
	private class ExponentialAveragerPair {
		ExponentialAverager signalAverager;
		TwoSpeedExponentialAverager backgroundAverager;
		
		public ExponentialAveragerPair( double signalAlpha, double backgroundAlpha ) { 
			
			this.signalAverager = new ExponentialAverager( signalAlpha );
			this.backgroundAverager = new TwoSpeedExponentialAverager( backgroundAlpha, signalAlpha );
		}
		
	}
	
	
	
	private java.util.ArrayList< ExponentialAveragerPair > averagersList;
	private double referenceGain;
	
	
	/**
	 * Constructor
	 * 
	 * @param alphas A list of DecayingAverageNormAlphas describing each signal/background pair for each
	 * band (be it a signal or guard band).
	 * @param referenceGain The referenceGain is used to scale the signal estimate before passing it to
	 * the TwoSpeedExponentialAverager.  See it's JavaDoc for a description of how this is used in it's
	 * calculation.
	 */
	public DecayingAverageNorm( java.util.List< DecayingAverageNormAlphas > alphas, double referenceGain ) {

		this.referenceGain = referenceGain;
		
		averagersList = new java.util.ArrayList< ExponentialAveragerPair >();
		for( DecayingAverageNormAlphas a : alphas ) {
			averagersList.add( new ExponentialAveragerPair( a.signalAlpha, a.backgroundAlpha ));
		}
				
	}
	
	public NormalizedDataUnit Process( SpectralEtiDataUnit a ) {

		
		NormalizedData[] outputData = new NormalizedData[ a.getData().length ];
		
		for( int i = 0; i < averagersList.size(); ++i ) {
			double[] tmp = new double[1];
			tmp[0] = a.getData()[i];
			
			double[] sigResult = averagersList.get(i).signalAverager.Process( tmp );
			
			double[] ref = new double[1];
			ref[0] = sigResult[ 0 ] * referenceGain;
			
			double[] backgroundResult = averagersList.get(i).backgroundAverager.Process( tmp, ref );
			
			outputData[ i ] = new NormalizedData( sigResult[0], backgroundResult[ 0 ] );
		}

		NormalizedDataUnit unit
		  = new NormalizedDataUnit( a.getTimeMilliseconds(),
									a.getChannelBitmap(),
									a.getStartSample(),
									a.getSampleDuration() );
		
		
		unit.setData( outputData );
		
		return unit;
		
	}
}
