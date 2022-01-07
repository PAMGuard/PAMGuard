/**
 * 
 */
package serialComms.jserialcomm;

/**
 * @author dg50
 *
 */
public class PJPortBusyException extends PJSerialException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public PJPortBusyException(String message) {
		super(message);
	}

}
