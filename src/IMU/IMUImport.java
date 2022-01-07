package IMU;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import PamUtils.PamCalendar;
import angleMeasurement.AngleDataUnit;

/**
 * Sets up a thread to import .csv files and save data units to a database. 
 * @author Jamie Macaulay. 
 *
 */
public class IMUImport {
	
	private IMUImportMananger importCSV;
	private ImportIMUThread importThread;
	
	public IMUImport(IMUImportMananger importCSVMethod){
		this.importCSV=importCSVMethod; 
	}
	
	/**
	 * Saves the imported hydrophone data to a database if one exists. 
	 * @param hydrophoneData
	 * @return true if the data load was successful. 
	 */
	class ImportIMUThread extends SwingWorker<Boolean, Void> {
		
		String file; 
		Integer errorCode=null;
		int nUnits=0;
		int totalNUnits=0; 

		
	
		public ImportIMUThread(String file){
			this.file=file;
			addPropertyChangeListener(importCSV.getProportyListener(this));
			setSaveProgress(0);
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			
			//reset error code
			errorCode=null;
			
			//load the file			
			ArrayList<ArrayList<Double>> imuData=importcsvData(file);
			if (imuData==null){
				errorCode=IMUImportMananger.NO_CSV_LOAD;
				this.cancel(true);
				done();
			}
			if (isDone() || isCancelled()) return false;
			
			//save the data in the file to the database 
			System.out.println(" ImportIMUThread: No. data points: "+imuData.size());
			AngleDataUnit dataUnit; 
			
			setTotalNUnits(imuData.size());
			
			//must set a little progress to tell import manager we have finished loading .csv. 
			int progress=1;
			setSaveProgress(progress);
			for (int i=0; i<imuData.size(); i++){
				
				//check we have the correct data format- if there is an error keep iterating through the data.
				if (!isDataFormatOK(imuData.get(i))){
					errorCode=IMUImportMananger.WRONG_FORMAT;
					continue;
				}
				
				//stop the import if thread has been set as cancelled
				if (isDone() || isCancelled()) return false;
				
				dataUnit= createDataUnit(imuData.get(i));
				importCSV.addDataToDataBlock(dataUnit);
				
				//save the data- ensures if PAMGUARD crashes then the user does not lose imported data.
				if (i%50==0){
					setNUnitsLoaded(i);
					progress=(Math.round(100*i/imuData.size()));
					//need to trigger property change listerner but don't want to set the laod bar to intermediate. 
					if (progress!=0) setSaveProgress(progress);
					else setSaveProgress(1);
					importCSV.updateLoadBar(this);
					importCSV.getIMUDataBlock().saveViewerData();
				}
				
			}
			
			importCSV.getIMUDataBlock().saveViewerData();
			System.out.println("IMUDataBlock unit count: "+importCSV.getIMUDataBlock().getUnitsCount());
			setProgress(100);
			return true; 
		}
		
		public void setSaveProgress(int prog){
//			System.out.println("prog "+prog);
			setProgress(prog);
		}
		
		/**
		 * Number of loaded units so far
		 * @param units-number of units loaded so far
		 */
		public void setNUnitsLoaded(int units){
			this.nUnits=units; 
		}
		
		/**
		 * Total number of units to load
		 * @param units- the total number of units which need loaded
		 */
		public void setTotalNUnits(int units){
			this.totalNUnits=units; 
		}
		
		/**
		 * @return total number of units to load
		 */
		public int getTotalNUnits(){
			return totalNUnits;
		}
		
		/**
		 * @return number of units loaded so far
		 */
		public int getNUnitsLoaded(){
			return nUnits;
		}
		
		@Override
        public void done() {
			super.done();
			importCSV.endImport(errorCode);
		}
		
		
	}
	
	public boolean isDataFormatOK(ArrayList<Double> imuData){
		if (imuData.size()!=4) return false; 
		else return true; 
	}
	
	
	
	/**
	 * Import the data form the .csv file into a 2D ArrayList. 
	 * @param file:
	 * @return
	 */
	public ArrayList<ArrayList<Double>> importcsvData(String file){
		return PamUtils.TxtFileUtils.importCSVData(file);
	}
	
	public int saveToDB(){
		return 0; 
	}

	public ImportIMUThread newThread(String csvFile) {
		return new ImportIMUThread(csvFile);
	}
	
	/**
	 * Get the error code for the import IMU thread. 
	 * @return
	 */
	public Integer getErrorCode(){
		return importThread.errorCode;
	}
	
	/**
	 * Create a data unit from input Array:
	 * @param imuData [0]=time (matlab datenum); [1] heading (degrees) [2] pitch (degrees) [3] roll degrees. 
	 * @return angle data unit
	 */
	public AngleDataUnit createDataUnit(ArrayList<Double> imuData){
		
		long timeMillis=PamCalendar.dateNumtoMillis(imuData.get(0));
//		System.out.println("raw Datenum: "+imuData.get(0)+" "+ timeMillis+" "+PamCalendar.getCalendarDate(timeMillis).getTime().toString());
		Double[] imuDat=new Double[3];
		imuDat[0]=Math.toRadians(imuData.get(1));
		imuDat[1]=Math.toRadians(imuData.get(2));
		imuDat[2]=Math.toRadians(imuData.get(3));
		AngleDataUnit newDataUnit=new AngleDataUnit(timeMillis, imuDat);
		
		return newDataUnit;
	}
	
	
}
