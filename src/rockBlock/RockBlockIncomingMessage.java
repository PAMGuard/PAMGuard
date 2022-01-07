package rockBlock;
/**
 * Data unit containing messages that have been sent to the local RockBlock.  These are typically
 * termed 'Mobile Terminated' or MT messages indicating they have been generated from the outside
 * and sent to the RockBlock unit
 * 
 * @author mo55
 *
 */
public class RockBlockIncomingMessage extends RockBlockMessage {

	/** Flag indicating whether message has been read */
	private boolean messageRead = false;
	
	public RockBlockIncomingMessage(long timeMilliseconds, String message) {
		super(timeMilliseconds, message);
	}

	public boolean isMessageRead() {
		return messageRead;
	}

	public void setMessageRead(boolean messageRead) {
		this.messageRead = messageRead;
	}
}
