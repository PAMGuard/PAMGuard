package likelihoodDetectionModule.thresholdDetector;

/**
 * This class is used to uniquely identify detections.  The channelMask and bandIndex is ample to do this.
 * The equals and hashCode() operators have been overloaded so that they may be compared, and placed in HashMaps.
 * 
 * @author Dave Flogeras
 *
 */
public class DetectionKey {

	private int channelMask;
	private int bandIndex;
	
	DetectionKey( int channelMask, int bandIndex ) {
		this.channelMask = channelMask;
		this.bandIndex = bandIndex;
	}
	
	/**
	 * equals operator.  Two DetectionKey's are equal if both their channelMask and bandIndex members are equal.
	 */
	@Override
	public boolean equals( Object o ) {
		if( o instanceof DetectionKey && this.channelMask == ((DetectionKey)o).channelMask &&
										this.bandIndex == ((DetectionKey)o).bandIndex ) {
			return true;
		}
		return false;
	}
	
	public int channelMask() {
		return this.channelMask;
	}
	
	/**
	 * Overload the hashCode operator.  The thinking here is that bandIndex will always be far < 64k.
	 * ChannelMask can be converted to a channel number between 1-32.  If we bit-pack there into the upper
	 * and lower words of an int, we have a unique, deterministic hashCode.
	 */
	@Override
	public int hashCode() {
		
		// Convert from channel mask to channel number
		int channel = PamUtils.PamUtils.getSingleChannel( this.channelMask );
		return ( channel << 16 ) + this.bandIndex;
		
	}
	
	public int bandIndex() {
		return this.bandIndex;
	}
	
}
