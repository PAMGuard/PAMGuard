package jsonStorage;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import PamController.PamguardVersionInfo;
import PamUtils.PamCalendar;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryStore;

public abstract class JSONObjectDataSource<DataSource extends JSONObjectData> {

	/** 
	 * The data object to load parameters into - this is what will be used to generate
	 * the JSON string.  Note that this must be initialised in the constructor of the
	 * subclass.
	 * 
	 */
	protected DataSource objectData;
	
	
	/**
	 * <p>Don't do anything in the generic constructor, but the subclass that
	 * extends this class MUST INSTANTIATE A CONCRETE VERSION OF THE objectData
	 * FIELD USING THE CLASS-SPECIFIC JSONOBJECTDATA.</p>
	 * <p>Example: for the Whistle & Moan Detector...
	 * <ul>
	 * <li>the class WhistleJSONData extends JSONObjectData</li>
	 * <li>the class WhistleJSONDataSource extends JSONObjectDataSource<WhistleJSONData>...</li>
	 * <li>...and in it's constructor, it sets objectData = new WhistleJSONData();
	 * </ul>
	 */
	protected JSONObjectDataSource() {
	}


	/**
	 * Return a json-formatted string generated from the fields in this object
	 * @return
	 */
	public String getPackedObject(PamDataUnit dataUnit) {
		setFields(dataUnit);
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		
		String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(objectData) + '\n';
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			jsonString = String.format("{\"Error cannot convert: %s\"}", this.getClass());
		}
		return jsonString;
	}
	
	
	/**
	 * Loads the fields of the JSONObjectData object with the parameters from the dataUnit.  First
	 * all of the generic fields common to all PamDataUnits are loaded, and then the abstract
	 * function is called to load class-specific fields.
	 * 
	 * @param dataUnit
	 */
	private void setFields(PamDataUnit dataUnit) {
		DataUnitBaseData baseData = dataUnit.getBasicData();
		
		// transfer over the data common to all data unit types
		objectData.flagBitmap = baseData.getS1Contents();
		objectData.millis = baseData.getTimeMilliseconds();
		if ((objectData.flagBitmap & DataUnitBaseData.S1_TIMENANOSECONDS) != 0) {
			objectData.timeNanos = baseData.getTimeNanoseconds();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_CHANNELMAP) != 0) {
			objectData.channelMap = baseData.getChannelBitmap();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_UID) != 0) {
			objectData.UID = baseData.getUID();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_STARTSAMPLE) != 0) {
			objectData.startSample = baseData.getStartSample();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_SAMPLEDURATION) != 0) {
			objectData.sampleDuration = baseData.getSampleDuration();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_FREQUENCYLIMITS) != 0) {
			double[] freq = baseData.getFrequency();
			objectData.freqLimits = new Double[]{freq[0], freq[1]};
			
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_MILLISDURATION) != 0) {
			objectData.millisDuration = baseData.getMillisecondDuration();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_TIMEDELAYSSECONDS) != 0) {
			double[] delays = baseData.getTimeDelaysSeconds();
			objectData.numTimeDelays = delays.length;
			objectData.timeDelays = new Double[objectData.numTimeDelays];
			for (int i=0; i<delays.length; i++) {
				objectData.timeDelays[i] = delays[i];
			}
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_HASSEQUENCEMAP) != 0) {
			objectData.sequenceMap = baseData.getSequenceBitmap();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_HASNOISE) != 0) {
			objectData.noise = baseData.getNoiseBackground();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_HASSIGNAL) != 0) {
			objectData.signal = baseData.getSignalSPL();
		}
		if ((objectData.flagBitmap & DataUnitBaseData.S1_HASSIGNALEXCESS) != 0) {
			objectData.signalExcess = baseData.getSignalExcess();
		}	
		
		// force the subclass to set the object type
		setObjectType(dataUnit);
		
		// now add any fields specific to the subclass
		addClassSpecificFields(dataUnit);
		
		// finally, add in the new fields used in the convertBinToJSON Matlab script
		objectData.dateReadable = PamCalendar.formatDateTime2(objectData.millis, "yyyy MMMM dd HH:mm:ss.SSS", false);
		objectData.filePath = "Network Sender";
		BinaryDataSource theBinarySource = dataUnit.getParentDataBlock().getBinaryDataSource();
		objectData.moduleType = theBinarySource.getModuleType();
		objectData.moduleName = theBinarySource.getModuleName();
		objectData.streamName = theBinarySource.getStreamName();
		objectData.moduleVersion = theBinarySource.getModuleVersion();
		objectData.pamguardVersion = PamguardVersionInfo.version;
		objectData.fileFormat = BinaryStore.getCurrentFileFormat();
	}
	
	
	/**
	 * Add fields specific to this subclass
	 */
	abstract protected void addClassSpecificFields(PamDataUnit pamDataUnit);
	
	/**
	 * Set the object type specific to this subclass.  This is typically the objectType field from the subclass'
	 * implementation of the BInaryObjectData class.  BinaryObjectData is often created in the subclass' 
	 * BinaryDataSource.getPackedData() method.
	 * 
	 * @param pamDataUnit
	 */
	abstract protected void setObjectType(PamDataUnit pamDataUnit);
	

}
