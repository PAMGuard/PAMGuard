package Array.streamerOrigin;

import generalDatabase.PamTableDefinition;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataUnit;

abstract public class HydrophoneOriginSystem {

	abstract public String getName();
	
	abstract public HydrophoneOriginMethod createMethod(PamArray pamArray, Streamer streamer);

	abstract public Class getMethodClass();
	
	/**
	 * Create any additional fields in the streamer logging table which may be needed. 
	 * This is a bit inefficient since all origin methods are going to do this - but who cares 
	 * since there are very few of them. <p>
	 * Should check that columns of the same name do not already exist before adding them !
	 * @param tableDefinition table definition to add to. 
	 * @return number of columns added. 
	 */
	public int createSQLLoggingFields(PamTableDefinition tableDefinition) {
		return 0;
	}
	
	/**
	 * Fill table time data prior to writing to the database. 
	 * @param streamerDataUnit streamer data unit to take data from. <p>
	 * Should check that this system is actually in use before proceeding !  
	 * @return true if successful. 
	 */
	public boolean fillSQLLoggingFields(StreamerDataUnit streamerDataUnit) {
		return true;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Extract data from SQL logging fields and add them back into the streamer data
	 * @param sdu streamer data unit
	 * @return true if successful
	 */
	public boolean extractSQLLoggingFields(StreamerDataUnit sdu) {
		return true;
	}
}
