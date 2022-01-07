package likelihoodDetectionModule.normalizer;

/** A simple sliding block averager.  The width is given in samples, and must be odd.
 *  If it is not odd, 1 is added to choose the next largest odd number.  We wait until
 *  we have seen (n+1)/2 samples before we can output a data point, therefore this unit
 *  will cause some lag in the system as it buffers. As stated above, an initial (n+1)/2 data points
 *  are required before the first output point is produced.  We make up the rest of the data
 *  by mirroring the data about the center point.
 * 
 * @author Dave Flogeras
 *
 */
public class SlidingAverager {
	
	private int width;
	private boolean divideThrough;
	
	// This is public for efficiency, the user will remove data they want once it's available
	public java.util.Queue< Double > processedData;
	private boolean steadyState;
	private java.util.Queue< Double > history;
	private double currentSum;
	// Temporary staging area used until we hit steady state. This avoids
	// using a double ended container since we have to back track and make
	// up past data
	private java.util.List< Double > initialData;
	
	public SlidingAverager( int width, boolean divideThrough ) {
		
		this.width = width;
		// Ensure width is odd
		if( width % 2 == 0 ) {
		  ++this.width;
		}
		
		history = new java.util.LinkedList< Double >();
		this.currentSum = 0;
		this.steadyState = false;
		this.divideThrough = divideThrough;
		processedData = new java.util.LinkedList< Double >();
		initialData = new java.util.LinkedList< Double >();
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public void consume( double [] data ) {

		for( int i = 0; i < data.length; ++i ) {
			if( ! steadyState ) {
				initialData.add( data[ i ] );
				if( initialData.size() == ( this.width + 1 ) / 2) {
					
					// Reverse (mirror copy) the initialData 
					for( int reverse = initialData.size(); reverse > 1; --reverse ) {
						history.add( initialData.get( reverse - 1 ));
					}
					// Now do the forward order copy
					for( Double d : initialData ) {
						history.add( d );
					}
					
					// Sum up for our first output
					for( Double d : history ) {
						currentSum += d;
					}
					
					if( divideThrough ) {
						processedData.add( currentSum / width );
					}
					else {
						processedData.add( currentSum );
					}
					
					steadyState = true;
					
				}
			}
			else {
				currentSum -= history.poll();
				currentSum += data[ i ];
				history.add( data[ i ] );
				
				if( divideThrough ) {
					processedData.add( currentSum / width );
				}
				else {
					processedData.add( currentSum );
				}
			}
			
		}
	}
	
}