package export.RExport;

import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;
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
	
	private RAnnotationManager rAnnotationsManager;

	public RDataUnitExport() {
		this.rAnnotationsManager = new RAnnotationManager(); 
	}
	
	
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
		
		
		//super detections - a bit messy at the moment but meh. 
		int[] superDetUID = null;
		IntArrayVector superUID =null;
		if (dataUnit.getSuperDetectionsCount()>0) {
			superDetUID= new int[dataUnit.getSuperDetectionsCount()]; 
			for (int i=0; i<dataUnit.getSuperDetectionsCount(); i++) {
				//this is a little dodgy but super detections tend to have lower UID values and 
				//R exporting does not support long's well
				superDetUID[i]=(int) dataUnit.getSuperDetection(i).getUID();
			}
			superUID = new IntArrayVector(superDetUID);

		}
		else {
			superUID = new IntArrayVector();
		}
		
		rData.add("superUID", superUID);
		
		rAnnotationsManager.addDataAnnotations(rData, dataUnit, index);
				
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
	
	/**
	 * Convert a 2D double array to a vector which can be added to an RData frame.
	 * @param arr - the array
	 * @return Vector containing the array data. 
	 */
	public static Vector doubleArr2R(double[][] arr) {
		int nbins =arr.length*arr[0].length;
		int n=0;
		double[] concatWaveform  = new double[nbins];
		//System.out.println("Number of bins: " + nbins);
		for (int i=0; i<arr.length; i++) {
			for (int j=0; j<arr[i].length; j++) {
//				System.out.println("Current: " + i + " "+ j 
//						+ " nchan: " + dataUnit.getNChan() + "  wave size: " 
//						+ dataUnit.getWaveLength() +"len concat: " + concatWaveform.length);
				concatWaveform[n++] = arr[i][j];
			}
		}

		Vector newMatrix = DoubleArrayVector.newMatrix(concatWaveform, arr[0].length, arr.length); 
		
		return newMatrix;
	}


}
