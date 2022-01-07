package likelihoodDetectionModule.normalizer;

import likelihoodDetectionModule.spectralEti.SpectralEtiDataUnit;

/** A Split Window normalizer implementation.  Basically there is a wide and narrow
 *  blockAverager.  The signal estimate is the output of the narrow average, divided
 *  by the width of the narrow block.  The noise estimate is the output of the wide
 *  average, minus the signal (output of the narrow average).  The noise is then
 *  divided by the difference of the two widths (wide width - narrow width).
 *
 *  A list of blockTimeStamp objects is kept as data comes in since the blockAverage
 *  modules will cause lags in the processing and might not produce output immediately.
 *  When data is produces, the oldest blockTimeStamp object is used to create a block
 *  the same size as the input data, therefore data will come out in same sized and
 *  timestamped blocks as they came in
 * 
 * @author dave
 *
 */
public class SplitWindowNorm implements Normalizer {
	
	
	/** Convenience class which groups a narrow and wide blockAverager together for
	 *  easier storage
	 */
	private class BlockAveragerPair {
		
		public SlidingAverager narrowAverager;
		public SlidingAverager wideAverager;
		
		BlockAveragerPair( int narrowWidth, int wideWidth ) {
			narrowAverager = new SlidingAverager( narrowWidth, false );
			wideAverager = new SlidingAverager( wideWidth, false );
		}
		
	}
	
	
	
	
	private java.util.Queue< BlockTimeStamp > blockTimeStamps;
	// List of pairs of averagers, one per data point (ETI Band) in the channel
	private java.util.ArrayList< BlockAveragerPair > averagersList;
	
	SplitWindowNorm( java.util.List< SplitWindowNormWidths > widths ) {
		
		blockTimeStamps = new java.util.LinkedList< BlockTimeStamp >();

		averagersList = new java.util.ArrayList< BlockAveragerPair >();
		for( SplitWindowNormWidths w : widths ) {
			averagersList.add( new BlockAveragerPair( w.signalWidth, w.backgroundWidth ));
		}
		
	}
	
	private void Consume( SpectralEtiDataUnit a ) {
		blockTimeStamps.add(  new BlockTimeStamp( a.getTimeMilliseconds( ),
				                                  a.getChannelBitmap(),
				                                  a.getStartSample(),
				                                  a.getSampleDuration(),
				                                  a.getData().length ));
		
		for( int i = 0; i < averagersList.size(); ++i ) {
			double[] tmp = new double[1];
			tmp[0] = a.getData()[i];
			averagersList.get(i).narrowAverager.consume( tmp );
			averagersList.get(i).wideAverager.consume( tmp );
		}
		
	}
	
	private NormalizedDataUnit OutputData() {
		
		// At first we will just 'peek' (without removing from the Queue) until we know
		// we have enough data to fill the data block
		BlockTimeStamp bts = blockTimeStamps.peek();
		if( null == bts ) {
			return null;
		}
		
		int minOutputData;
		try {
			minOutputData = averagersList.get( 0 ).narrowAverager.processedData.size();
		}
		catch( java.lang.IndexOutOfBoundsException e ) {
			return null;
		}
		for( BlockAveragerPair averagers : averagersList ) {
			minOutputData = java.lang.Math.min( minOutputData, averagers.narrowAverager.processedData.size() );
			minOutputData = java.lang.Math.min( minOutputData, averagers.wideAverager.processedData.size() );
		}
		
		if( minOutputData * averagersList.size() < bts.dataLength ) {
			return null;
		}
		
		// At this point we know we can continue, so we must pop from the queue
		blockTimeStamps.poll();

		NormalizedData[] d = new NormalizedData[ bts.dataLength ];

		int i = 0;
		for( BlockAveragerPair averagers : averagersList ) {
				
			double sig = averagers.narrowAverager.processedData.poll();
			double noise = averagers.wideAverager.processedData.poll() - sig;
				
			sig   /= averagers.narrowAverager.getWidth();
			noise /= averagers.wideAverager.getWidth() - averagers.narrowAverager.getWidth();
			
			d[ i++ ] = new NormalizedData( sig, noise );
		}
		
		NormalizedDataUnit unit
		  = new NormalizedDataUnit( bts.timestamp,
									bts.channelBitmap,
									bts.startSample,
									bts.duration
									);
		
		
		unit.setData( d );
		return unit;
	}
	
	public NormalizedDataUnit Process( SpectralEtiDataUnit a ) {
		Consume( a );
		return OutputData();
	}
	
}