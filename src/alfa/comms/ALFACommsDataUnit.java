package alfa.comms;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

/**
 * Abstract data unit class that will be used by anything that's going to produce
 * data that will ultimately get sent through the messaging interface. 
 * @author dg50
 *
 */
abstract public class ALFACommsDataUnit extends PamDataUnit {

	private boolean readyToSend;
	
	/**
	 * @param basicData
	 */
	public ALFACommsDataUnit(DataUnitBaseData basicData) {
		super(basicData);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param timeMilliseconds
	 * @param channelBitmap
	 * @param startSample
	 * @param durationSamples
	 */
	public ALFACommsDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param timeMilliseconds
	 */
	public ALFACommsDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return A summary String to send via satellite.  
	 */
	abstract public String getCommsString();

	/**
	 * @return the readyToSend
	 */
	public boolean isReadyToSend() {
		return readyToSend;
	}

	/**
	 * @param readyToSend the readyToSend to set
	 */
	public void setReadyToSend(boolean readyToSend) {
		this.readyToSend = readyToSend;
	}
	
	
}
