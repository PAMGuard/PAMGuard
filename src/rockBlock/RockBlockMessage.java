package rockBlock;
import PamguardMVC.PamDataUnit;

/**
 * Data unit for incoming/outgoing RockBlock messages.
 * 
 * @author mo55
 *
 */
abstract public class RockBlockMessage extends PamDataUnit<PamDataUnit, PamDataUnit> {

	/** The message as a String object */
	private String message;

	/**
	 * 
	 * @param timeMilliseconds the time in milliseconds
	 * @param message the message
	 */
	public RockBlockMessage(long timeMilliseconds, String message) {
		super(timeMilliseconds);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
