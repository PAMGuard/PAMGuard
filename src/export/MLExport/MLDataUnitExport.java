package export.MLExport;

import org.jamdev.jdl4pam.utils.DLMatFile;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.types.Matrix;
import us.hebi.matlab.mat.types.Struct;

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
	public Struct detectionToStruct(Struct mlStruct, T dataUnit, int index){
		
		Mat5.newScalar(dataUnit.getTimeMilliseconds());
		
		//all the default stuff every data unit has. 
		Matrix millis = Mat5.newScalar(dataUnit.getTimeMilliseconds());

		//the channel bit map.
		Matrix channelMap = Mat5.newScalar(dataUnit.getChannelBitmap()); 
		
		//the sequence map, or 0 if there is no sequence number
		Matrix sequenceMap = Mat5.newScalar(0); 
		if (dataUnit.getSequenceBitmapObject()!=null) {
			sequenceMap =  Mat5.newScalar(dataUnit.getSequenceBitmapObject());
		}
		
		//UID for the detection.
		Matrix UID =  Mat5.newScalar(dataUnit.getUID()); 

		Matrix startSample = Mat5.newScalar(0); 
		if (dataUnit.getStartSample()!=null) {
			//the start sample.
			 startSample = Mat5.newScalar(dataUnit.getStartSample()); 
		}
		

		//the duration of the detection in samples.
		Matrix sampleDuration = Mat5.newScalar(dataUnit.getSampleDuration()); 
		
		Matrix freqLimits = DLMatFile.array2Matrix(dataUnit.getBasicData().getFrequency());

		//the frequency limits.

		//create the structure. 
		mlStruct.set("millis", index, millis);
		mlStruct.set("channelMap", index, channelMap);
		mlStruct.set("sequenceMap",index, sequenceMap);
		mlStruct.set("UID",index, UID);
		mlStruct.set("startSample", index,startSample);
		mlStruct.set("sampleDuration",index, sampleDuration);
		mlStruct.set("freqLimits", index,freqLimits);

		//there may be no delay info 
		if (dataUnit.getBasicData().getTimeDelaysSeconds()!=null && dataUnit.getBasicData().getTimeDelaysSeconds().length>=1){
			Matrix numTimeDelays = Mat5.newScalar(dataUnit.getBasicData().getTimeDelaysSeconds().length); 
			Matrix timeDelays = DLMatFile.array2Matrix(dataUnit.getBasicData().getTimeDelaysSeconds()); 
			mlStruct.set("numTimeDelays", index, numTimeDelays);
			mlStruct.set("timeDelays", index,timeDelays);
		}
		else {
			mlStruct.set("numTimeDelays", index, Mat5.newScalar(0));
			//mlStruct.setField("timeDelays", new MLEmptyArray(), index); //22-02-2018 - this caused an exception  when saving...
			mlStruct.set("timeDelays",  index, Mat5.newScalar(0));
		}
		
		//MATLAB date number. 
		double datenumMT = PamCalendar.millistoDateNum(dataUnit.getTimeMilliseconds());
		Matrix date = Mat5.newScalar(datenumMT); 

		mlStruct.set("date", index, date);
		
		//add detection specific data 
		mlStruct= addDetectionSpecificFields(mlStruct, index, dataUnit);
		
		this.mlAnnotationsManager.addAnnotations(mlStruct, index, dataUnit); 
		
		return mlStruct; 
	}

	/**
	 *Add detection specific fields  to a structure.
	 *@param structure containing all generic info from PamDataUnit
	 *@param the data unit. 
	 */
	public abstract Struct addDetectionSpecificFields(Struct mlStruct, int index, T dataUnit); 
	
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
