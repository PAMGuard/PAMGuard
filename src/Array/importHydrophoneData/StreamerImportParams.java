package Array.importHydrophoneData;

import java.io.Serializable;

import Array.HydrophoneLocatorSystem;
import Array.HydrophoneLocators;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;


public class StreamerImportParams implements Serializable, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * StreamerIndex
	 */
	int streamerIndex=0;

	/**
	 * How to work out where the streamer is
	 */
	HydrophoneOriginSystem hydropheonOrigin=HydrophoneOriginMethods.getInstance().getMethod(0);
	
	/**
	 * How to work out where hydrophones are in relation to the streamer, 
	 */
	HydrophoneLocatorSystem hydropheonLocator=HydrophoneLocators.getInstance().getSystem(0);
	
	@Override
	public StreamerImportParams clone() {
		try {
			return(StreamerImportParams) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		return null;
		}
	}

}
