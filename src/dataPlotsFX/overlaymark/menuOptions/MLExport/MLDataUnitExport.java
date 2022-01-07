package dataPlotsFX.overlaymark.menuOptions.MLExport;

import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLStructure;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

/**
 * Export a detection to MATLAB 
 * @author Jamie Macaulay
 *
 */
public abstract class MLDataUnitExport<T extends PamDataUnit<?, ?>> {
	
	/**
	 * Annotations manager for nadling adding annotations to data units. 
	 */
	private MLAnnotationsManager mlAnnotationsManager;

	public MLDataUnitExport() {
		this.mlAnnotationsManager = new MLAnnotationsManager(); 
	}

	/**
	 * Create a MATLAB structure which contains all information for a data unit. 
	 * @param dataUnit - the data unit to convert to a MATLAB structure
	 * @return detection data MATLAB structure ready to be exported to a .mat file or added to a ArrayList<MLArray>. 
	 */
	public MLStructure detectionToStruct(MLStructure mlStruct, T dataUnit, int index){
		
		//all the default stuff every data unit has. 
		MLInt64 millis = new MLInt64(null, new Long[]{dataUnit.getTimeMilliseconds()}, 1); 

		//the channel bit map.
		MLInt32 channelMap = new MLInt32(null, new Integer[]{dataUnit.getChannelBitmap()}, 1); 
		
		//the sequence map, or 0 if there is no sequence number
		MLInt32 sequenceMap = new MLInt32(null, new Integer[]{0}, 1); 
		if (dataUnit.getSequenceBitmapObject()!=null) {
			sequenceMap = new MLInt32(null, new Integer[]{dataUnit.getSequenceBitmapObject()},1);
		}
		
		//UID for the detection.
		MLInt64 UID = new MLInt64(null, new Long[]{dataUnit.getUID()}, 1); 

		//the start sample.
		MLInt64 startSample = new MLInt64(null, new Long[]{dataUnit.getStartSample()}, 1); 

		//the duration of the detection in samples.
		MLInt64 sampleDuration = new MLInt64(null, new Long[]{dataUnit.getSampleDuration()},1); 

		//the frequency limits.
		MLDouble freqLimits = new MLDouble(null, dataUnit.getBasicData().getFrequency(), 1); 

		//create the structure. 
		mlStruct.setField("millis", millis, index);
		mlStruct.setField("channelMap", channelMap, index);
		mlStruct.setField("sequenceMap", sequenceMap, index);
		mlStruct.setField("UID", UID, index);
		mlStruct.setField("startSample", startSample, index);
		mlStruct.setField("sampleDuration", sampleDuration, index);
		mlStruct.setField("freqLimits", freqLimits, index);

		//there may be no delay info 
		if (dataUnit.getBasicData().getTimeDelaysSeconds()!=null && dataUnit.getBasicData().getTimeDelaysSeconds().length>=1){
			MLInt32 numTimeDelays = new MLInt32(null, new Integer[]{dataUnit.getBasicData().getTimeDelaysSeconds().length}, 1); 
			MLDouble timeDelays = new MLDouble(null, dataUnit.getBasicData().getTimeDelaysSeconds(), 1); 
			mlStruct.setField("numTimeDelays", numTimeDelays, index);
			mlStruct.setField("timeDelays", timeDelays, index);
		}
		else {
			mlStruct.setField("numTimeDelays",  new MLInt32(null, new Integer[]{0},1), index);
			//mlStruct.setField("timeDelays", new MLEmptyArray(), index); //22-02-2018 - this caused an exception  when saving...
			mlStruct.setField("timeDelays",  new MLInt32(null, new Integer[]{0},1), index);
		}
		
		//MATLAB date number. 
		double datenumMT = PamCalendar.millistoDateNum(dataUnit.getTimeMilliseconds());
		MLDouble date = new MLDouble(null, new Double[]{datenumMT}, 1); 

		mlStruct.setField("date", date, index);
		
		//add detection specific data 
		mlStruct= addDetectionSpecificFields(mlStruct, dataUnit, index);
		
		this.mlAnnotationsManager.addAnnotations(mlStruct, dataUnit); 
		
		return mlStruct; 
	}

	/**
	 *Add detection specific fields  to a structure.
	 *@param structure containing all generic info from PamDataUnit
	 *@param the data unit. 
	 */
	public abstract MLStructure addDetectionSpecificFields(MLStructure mlStruct, T dataUnit, int index); 
	
	/**
	 * Get the unit class 
	 * @return
	 */
	public abstract Class<?> getUnitClass();
	
	/**
	 * Get the name of the structure 
	 * @return
	 */
	public abstract String getName();


	
}
