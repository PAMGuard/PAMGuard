package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;

/**
 * Exports a data unit to a List in R. Specific data units should subclass this. 
 * 
 * @author Jamie Macaulay
 *
 * @param <T> - the data unit type. 
 */
public abstract class RDataUnitExport<T extends PamDataUnit<?, ?>> {
	
	
	/**
	 * Create a R data frame which contains all information for a data unit. 
	 * @param dataUnit - the data unit to convert to a R structure
	 * @return detection data R structure ready to be exported to a .mat file or added to a ArrayList<MLArray>. 
	 */
	public ListVector.NamedBuilder detectionToStruct(T dataUnit, int index){
		
		ListVector.NamedBuilder rData = new ListVector.NamedBuilder();

		rData.add("millis", dataUnit.getTimeMilliseconds());
		rData.add("channelMap", dataUnit.getChannelBitmap());
		if (dataUnit.getSequenceBitmapObject()!=null) {
			rData.add("sequenceMap", dataUnit.getSequenceBitmap());
		}
		rData.add("UID", dataUnit.getUID());
		rData.add("startSample", dataUnit.getStartSample());
		rData.add("sampleDuration", dataUnit.getSampleDuration());
//		rData.add("freqLimits", new DoubleArrayVector(dataUnit.getBasicData().getFrequency()));
		rData.add("minFreq", dataUnit.getBasicData().getFrequency()[0]);
		rData.add("maxFreq", dataUnit.getBasicData().getFrequency()[1]);
		rData.add("amplitude", dataUnit.getBasicData().getCalculatedAmlitudeDB());

		//there may be no delay info 
		if (dataUnit.getBasicData().getTimeDelaysSeconds()!=null && dataUnit.getBasicData().getTimeDelaysSeconds().length>=1){
			rData.add("numTimeDelays", dataUnit.getBasicData().getTimeDelaysSeconds().length);
			rData.add("numTimeDelays", new DoubleArrayVector(dataUnit.getBasicData().getTimeDelaysSeconds()));
		}
		else {
			rData.add("numTimeDelays",  0);
			rData.add("timeDelays",  0);
		}
		
		//MATLAB date number. 
		
		long millisR = PamCalendar.millisToUnixEpoch(dataUnit.getTimeMilliseconds());
		rData.add("date", millisR); 
		
		
		//add detection specific data 
		rData= addDetectionSpecificFields(rData, dataUnit, index);
		
		return rData; 
	}

	
	/**
	 *Add detection specific fields  to a structure.
	 *@param structure containing all generic info from PamDataUnit
	 *@param the data unit. 
	 */
	public abstract ListVector.NamedBuilder addDetectionSpecificFields(ListVector.NamedBuilder rData, T dataUnit, int index); 
	
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
