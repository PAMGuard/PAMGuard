package nmeaEmulator;

/**
 * Class to hold new emulated data
 * @author Doug Gillespie
 *
 */
public class EmulatedData {

	protected long dataTime;
	
	protected long simTime;
	
	protected String dataString;

	public EmulatedData(long dataTime, long simTime, String dataString) {
		super();
		this.dataTime = dataTime;
		this.simTime = simTime;
		this.dataString = dataString;
	}
	
}
