package serialComms.jserialcomm;

public interface PJSerialLineListener {

	/**
	 * Called whenever a complete line has been read from the serial port. 
	 * Lines end with either a carriage return or line feed. 
	 * @param aLine
	 */
	public void newLine(String aLine);
	
	/**
	 * Called when port is closed / reading stops. 
	 */
	public void portClosed();

	/**
	 * Called if an exception is thrown in the thread reading the serial port
	 * @param e Exception. 
	 */
	public void readException(Exception e);
	
}
