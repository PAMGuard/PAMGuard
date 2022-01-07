package Array.importHydrophoneData;

import java.util.ArrayList;

import Array.ArrayManager;
import Array.Hydrophone;
import Array.HydrophoneDataBlock;
import Array.HydrophoneDataUnit;
import PamUtils.TxtFileUtils;
import PamView.importData.DataImport;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Class for importing hydrophone data from external file and saving to database. 
 * <p>
 * <b>.csv import formats</b>
 * <p>
 * <i>List Format:</i> 	
 * One hydrophone data unit per row (rows have ten columns). Format is: [0]=timeExcelSerial [1]=x  [2]=y [3]=z [4]=xErr [5]=yErr [6]=zErr [7]=streamerId [8]=hydrophoneId [9]=sensitivity [10]=gain . Note that sensitivity and gain info doesn't need to be included.  
 * <p>
 * <i>2D List Format (Legacy)</i>
 * Multiple hydrophones per row. Each row of the input array must have the following format. timeExcelSerial, x0, y0,z0, x0Error, y0Error, z0Error,x1, y1,z1, x1Error, y1Error, z1Error,,..... and so on depending on the number of hydrophones.
 * <p>
 * <b> .mat format</b>
 * Not yet implemented.
 * 
 * @author Jamie Macaulay
 */
public class HydrophoneImport extends DataImport<ArrayList<Double>>{
	
	String[] extensionStrings={".csv"};
	private ArrayList<ArrayList<Double>> hydrophonePositions;
	private  int errorCode;
	private HydrophoneDataBlock hydrophoneDataBlock;
	
	/**
	 * Streamer id to use if imported data has no streamer id info
	 */
	public static int defaultStreamerID=0; 
	
//	/**
//	 *Gain value to use if imported data has no gain info
//	 */
//	public static int defaultGain=0; // use ArrayManager default instead 
	
//	/**
//	 *Sensitivity value to use if imported data has no sensitivty info
//	 */
//	public static int defaultSens=-170;  // use ArrayManager default instead 

	
	/********NOT IMPLEMENTED YET*************
	 * Loads from a matlab structure with following format
	 * structure(i).time =time
	 * structure(i).hydrophones= Npositionsx3 array
	 * structure(i).hydrophoneErrors= Npositionsx3 array
	 * 
	 */
	public final static int MATLAB_STRUCT_FORMAT=2;
	
	/**
	 * Everything seems fine
	 */
	public final static int DATA_OK=3;
	
	/**
	 * The data is far in the past or in the future
	 */
	public final static int ERROR_YEARS=4;
	
	/**
	 * Something has gone wrong getting the csv file
	 */
	public final static int ERROR_LOADING_CSV=5;
	
	/**
	 * Something has gone wrong loading the matlab structure
	 */
	public final static int ERROR_LOADING_MATLAB_STRUCT=6;
	
	/**
	 * The number of hydrophones is not the same as the number of hydrophones in the curretn array manager.  
	 */
	public final static int ERROR_NUMBER_OF_HYDROPHONES_ARRAY=7;
	
	/**
	 * The number of hydrophones is different for different times. 
	 */
	public final static int ERROR_NUMBER_OF_HYDROPHONES_INCONSISTANT=8;

	public HydrophoneImport(HydrophoneDataBlock hydrophoneDataBlock) {
		this.hydrophoneDataBlock=hydrophoneDataBlock;
	}

	@Override
	public ArrayList loadDataIntermediate(String filePath) {
			
		if (filePath.endsWith(".csv")){
				
			hydrophonePositions=TxtFileUtils.importCSVData(filePath);
				
			if (hydrophonePositions==null || hydrophonePositions.size()==0 ) errorCode=ERROR_LOADING_CSV; 
			else{
				//we now have two possibilities. either loading in a legacy file or loading in a list of hydrophones. 
				//assume that all rows are the same length
				//check for current format
				if (hydrophonePositions.get(0).size()==11 || hydrophonePositions.get(0).size()==9){
					return hydrophonePositions;
				}
				//check for legacy 2D format. 
				if ((hydrophonePositions.get(0).size()-1)%6==0){
					return convertToHydrophoneList(hydrophonePositions);
				}
				
			}
		}
			
		if (filePath.endsWith(".mat")){
				
			hydrophonePositions=importPositionsFromMatlab(filePath);
				
			if (hydrophonePositions==null) errorCode=ERROR_LOADING_MATLAB_STRUCT; 
				
		}
		
		return null;
	
	}
	
	/**
	 * Converts a 2D List of hydrophones into a 1D list of hydrophones.
	 * @param importData. Each row of the input array must have the following format. time, x0, y0,z0, x0Error, y0Error, z0Error,x1, y1,z1, x1Error, y1Error, z1Error,,..... and so on depending on the number of hydrophones.   
	 * @return a list of hydrophones with the following format for each row. [0]=timeMilliss [1]=x  [2]=y [3]=z [4]=xErr [5]=yErr [6]=zErr [7]=streamerId [8]=hydrophoneId;
	 */
	public ArrayList<ArrayList<Double>> convertToHydrophoneList(ArrayList<ArrayList<Double>> importData){
		
		ArrayList<ArrayList<Double>> hydrophonesAll=new ArrayList<ArrayList<Double>>();
		ArrayList<Double> tempArray;
		Hydrophone hydrophone;
		double[] cOordinates;
		double [] cOordinateErrors;
		for (int i=0; i<importData.size(); i++){
			for (int j=0; j<((importData.get(i).size()-1)/6); j++){
				
				tempArray= new ArrayList<Double>();

				cOordinates=new double[3];
				cOordinateErrors=new double[3];
				
				cOordinates[0]=importData.get(i).get(j*6+1);
				cOordinates[1]=importData.get(i).get(j*6+2);
				cOordinates[2]=importData.get(i).get(j*6+3);
				
				cOordinateErrors[0]=importData.get(i).get(j*6+4);
				cOordinateErrors[1]=importData.get(i).get(j*6+5);
				cOordinateErrors[2]=importData.get(i).get(j*6+6);
					
				tempArray.add(importData.get(i).get(0));
				tempArray.add(cOordinates[0]);
				tempArray.add(cOordinates[1]);
				tempArray.add(cOordinates[2]);
				tempArray.add(cOordinateErrors[0]);
				tempArray.add(cOordinateErrors[1]);
				tempArray.add(cOordinateErrors[2]);
				//set Streamer ID. 
				tempArray.add((double) defaultStreamerID);
				//set hydrophoneID
//				System.out.println("Hydrophone iD"+j);
						
				tempArray.add((double) j);
				hydrophonesAll.add(tempArray);
//				System.out.println("TempArray: "+tempArray);

			}
		}
		return hydrophonesAll;
	}
	
	


	private ArrayList<ArrayList<Double>> importPositionsFromMatlab(
			String filePath) {
		// TODO- needs to be implemented. 
		return null;
	}

	@Override
	public String[] getExtensionsStrings() {
		return extensionStrings;
	}

	@Override
	public boolean isDataFormatOK(ArrayList<Double> dataLine) {
		// TODO might need to put some extra bits and bobs here eventually. 
		return true;
	}

	/**
	 * For hydrophone data imported [0]=timeMilliss [1]=x  [2]=y [3]=z [4]=xErr [5]=yErr [6]=zErr [7]=streamerId [8]=hydrophoneId [9]=sensitivity [10]=gain 
	 */
	@Override
	public PamDataUnit createDataUnit(ArrayList<Double> dataLine) {
		
		double sensitivity=ArrayManager.DEFAULT_HYDROPHONE_SENSITIVITY;
		double gain=ArrayManager.DEFAULT_PREAMP_GAIN; 
		if (dataLine.size()==10) {
			gain=dataLine.get(10);
			sensitivity=dataLine.get(9);
		}
		double[] bandwidth={0, 20000}; 

		Hydrophone hydrophone=new Hydrophone(dataLine.get(8).intValue(), dataLine.get(1), dataLine.get(2), dataLine.get(3), dataLine.get(4),dataLine.get(5), dataLine.get(6), "Unknown",  sensitivity,
		bandwidth, gain);
		//need to convert from excel serial to millis.
		long timeMillis= (long) PamUtils.PamCalendar.excelSerialtoMillis(dataLine.get(0));
		hydrophone.setTimeMillis(timeMillis);
		HydrophoneDataUnit hydrophoneDataUnit=new HydrophoneDataUnit(hydrophone);
		return hydrophoneDataUnit;
		
	}

	@Override
	public PamDataBlock getDataBlock() {
		return hydrophoneDataBlock;
	}
	
	@Override
	public String getDataUnitName(){
		return "Hydrophone Units";
	}

}
