package likelihoodDetectionModule.normalizer;

/** This is a convenience class which stores the critical non-static information about the
 *  data blocks being processed.  Because the normalizer will have a time lag, we have to cache
 *  these so we have accurate time information when we output blocks later.
 * 
 * @author Dave Flogeras
 *
 */
public class BlockTimeStamp {
	
	public BlockTimeStamp( long timestamp, int channelBitmap, long startSample, long duration, int dataLength ) {
		this.timestamp = timestamp;
		this.dataLength = dataLength;
		this.channelBitmap = channelBitmap;
		this.duration = duration;
		this.startSample = startSample;
		
	}
	public long timestamp;
	public int dataLength;
	public int channelBitmap;
	public long duration;
	public long startSample;
}