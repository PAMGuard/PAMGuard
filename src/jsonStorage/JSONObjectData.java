package jsonStorage;

/**
 * Class to hold data that will be converted into a json-formatted string.  Individual classes should extend this class, and call
 * the packDataUnitBaseData method in order to load up the generic fields with everything from the data unit base data
 * 
 * Note that the majority of the fields use the wrapper (e.g. Integer) instead of the primitive (e.g. int) class so that they can be
 * null if empty.  The json formatter will ignore null fields.
 * 
 * @author michaeloswald
 *
 */
public class JSONObjectData {

	
	short flagBitmap;
	
	public Integer identifier;
	
	long millis;
	
	Long timeNanos;
	
	Integer channelMap;
	
	Long UID;
	
	Long startSample;
	
	Long sampleDuration;

	Double[] freqLimits;
	
	Double millisDuration;
	
	Integer numTimeDelays;
	
	Double[] timeDelays;
	
	Integer sequenceMap;
	
	Float noise;
	
	Float signal;
	
	Float signalExcess;
	
	String dateReadable;
	
	String filePath;
	
	String moduleType;
	
	String moduleName;
	
	String streamName;
	
	int moduleVersion;
	
	String pamguardVersion;
	
	int fileFormat;

	
	/**
	 * Main constructor
	 */
	public JSONObjectData() {
	}


	/**
	 * Added in so that the Click Detector can copy over the sampleDuration variable into it's own
	 * duration field (to match the Matlab code)
	 * @return
	 */
	public Long getSampleDuration() {
		return sampleDuration;
	}


	/**
	 * Added in so that the Click Detector can use the channelMap field to save the number of channels in
	 * it's nChan field (to match the Matlab code)
	 * @return
	 */
	public Integer getChannelMap() {
		return channelMap;
	}
	

}
