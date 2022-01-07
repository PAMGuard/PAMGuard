package likelihoodDetectionModule.spectralEti;

/** A class which sums energy across a spectrum from a start frequency to an end frequency
 * 
 * @author Dave Flogeras
 *
 */
public class SpectralBand {
	
	private int startBin;
	private int endBin;
	private int lastBlockSize;
	private double fStart;
	private double fEnd;
	private double sampleRate;
	
	public SpectralBand( double fStart, double fEnd, double sampleRate ) {
		this.sampleRate = sampleRate;
		this.fStart = fStart;
		this.fEnd = fEnd;
		lastBlockSize = -1;
	}
	
	/** Calculates the energy in the given band.  Since the spectrum's size is unknown at
	 *  construction time, the calculate method calculates in terms of bins the start and end
	 *  bin 
	 *  
	 * @param input Input spectra
	 * @return Value of average energy in the band
	 *
	*/
	public double calculate( double[] input ) {
		
	  if( lastBlockSize != input.length ) {
		  
		  lastBlockSize = input.length;
		  startBin = (int) ( fStart * 2 / sampleRate * input.length );
		  endBin = (int) ( fEnd * 2 / sampleRate * input.length );

		  if( startBin > endBin ) {
			  int tmp = startBin;
			  startBin = endBin;
			  endBin = tmp;
		  }
	
		  if( startBin < 0 ) {
			  startBin = 0;
		  }
		  if( endBin > input.length - 1 ) {
			  endBin = input.length - 1;
		  }
	  }
	
	  double ret = 0;
	  for( int bin = startBin; bin <= endBin; ++bin ) {
		  ret += input[ bin ];
	  }
	  ret /= ( endBin - startBin + 1 );
		
	  return ret;
	  
	}
	
	
}