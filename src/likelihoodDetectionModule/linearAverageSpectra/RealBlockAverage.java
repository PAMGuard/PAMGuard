package likelihoodDetectionModule.linearAverageSpectra;

import PamUtils.complex.ComplexArray;

/** This is a helper class to compute block averages.  The first time processData is called,
 *  it creates a block of memory the same length as the data passed to the processData() method.
 *  After each call to processData, the user can check the status by calling averageReady().
 *  After an average is computed (averageReady() returns true), no more data will be accepted
 *  until you call the average() method to collect the results.
 *  The averageTimestamp() and averageAbsBlockIndex() return the timestamp and AbsBlockIndex of the
 *  averaged result.  The timestamp is that of the first block used in computing the average,
 *  FIXME what to do with AbsBlockIndex
 * 
 * @author dave
 *
 */
public class RealBlockAverage {
	
	private int nAvg;
	private double[] data;
	private int blocksProcessed;
	private long timestamp;
	private long startSample;
	
	public RealBlockAverage( int nAvg ) {
		this.nAvg = nAvg;
		this.blocksProcessed = 0;
		this.data = null;
	}

	public int NAvg() {
		return nAvg;
	}
	
	/** Processes a block of data at given timestamp and absolute block index.
	 * 
	 * @param timestamp The timestamp of the input block
	 * @param complexArray The complex input data
	 * @return true if the data was processed and is safe to discard, false if there is a pending average
	 * to be read, and the input data was not used
	 */
	public boolean processData( long timestamp,
								long startSample,
			                    ComplexArray complexArray ) {
		
		if( data == null ) {
			data = new double[ complexArray.length() ];
			java.util.Arrays.fill( data, 0. );
		}
		
		if( averageReady() ) {
			return false;
		}
		
		if( blocksProcessed == 0 ) {
			this.timestamp = timestamp;
			this.startSample = startSample;
		}

		for( int i = 0; i < complexArray.length(); ++i ) {
			this.data[ i ] += complexArray.magsq(i);
		}

		blocksProcessed++;
		
		if( averageReady() ) {
			for( int i = 0; i < complexArray.length(); ++i ) {
				this.data[ i ] /= nAvg;
			}
		}
		
		return true;
	}
	
	public boolean averageReady() {
		return blocksProcessed == nAvg;
	}
	
	public long averageTimestamp() {
		return this.timestamp;
	}
	
	public long averageStartSample() {
		return this.startSample;
	}
	
	/** Returns the processed average data.   Call averageReady() to know when data is ready.
	 * 
	 * @return The pending average, null if no average is ready
	 * @see RealBlockAverage#averageReady()
	 */
	public double[] average() {
		
		blocksProcessed = 0;
		double[] tmp = this.data;
		// We are passing ownership of the completed data block to the caller, 
		// set it null to force the processData() method to make a new array for the 
		// next block
		this.data = null;
		return tmp;
		
	}
}