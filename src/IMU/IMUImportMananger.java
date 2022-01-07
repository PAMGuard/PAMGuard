package IMU;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import angleMeasurement.AngleDataUnit;
import IMU.IMUImport.ImportIMUThread;
import PamController.PamController;
import PamView.ImportLoadBar;
import PamView.dialog.PamDialog;
/**
 * Import IMU data from .csv. File must be in the following format: 
 * <p>
 * rows of:
 * <p>
 * time (MATLAB datenum),heading, pitch, roll
 * --format -all in radians:
 * Note: the PAMGAURD format for 3D IMU angles is: 
 * <p>
 * Bearing- 0==north, 90==east 180=south, 270==west
 * <p>
 * Pitch- 90=-g, 0=0g, -90=g
 * <p>
 * Tilt 0->180 -camera turning towards left to upside down 0->-180 camera turning right to upside down
 * @author Jamie Macaulay
 *
 */
public class IMUImportMananger {
	
	private IMUControl imuControl;
	private IMUImport importIMUCSV;
	private ImportIMUThread importThread;
	private ImportLoadBar importLoadBar; 
	
	//error codes
	/**
	 * Unable to load csv data. Probably a bad file path. 
	 */
	public static final int NO_CSV_LOAD=0;
	/**
	 * The file is in the wrong format. 
	 */
	public static final int WRONG_FORMAT=1;

	public IMUImportMananger(IMUControl imuControl){
		this.imuControl=imuControl; 
		this.importIMUCSV=new  IMUImport(this); 
	}
	
	public String getName() {
		return "Import Manager: ";
	}


	/**
	 * Starts the process of loading a .csv file, converting to an IMU data unit and then saving to database. 
	 * @param csvFile
	 */
	protected void startImport(String csvFile){
		/**
		 * Open the dialog showing the progress bar
		 */
		importLoadBar=ImportLoadBar.showDialog(PamController.getInstance().getMainFrame(), "Load IMU Data");
		importLoadBar.setProgress(0);
		importLoadBar.setIntermediate(true);
		
		importThread=importIMUCSV.newThread(csvFile);
		importLoadBar.getCancelButton().addActionListener(new Stop());

		
		importThread.execute();
		 
	}
	
	/**
	 * End the current import. 
	 */
	protected void endImport(Integer errorCode){
		
		if (errorCode==null){
			importLoadBar.setVisible(false);
			updateDataMap();
		}
		else{
			importLoadBar.setVisible(false);
			//show a warning that 
			showErrorWarning(errorCode);
			//must redo datamap if error code is WRONG_FORMAT as some data units may have been added. 
			if (errorCode==WRONG_FORMAT) updateDataMap();

		}
	}
	
	/**
	 * Update the data map after external data has been loaded. 
	 */
	private void updateDataMap(){
		PamController.getInstance().updateDataMap();
//		DBControlUnit.findDatabaseControl().updateDataMap(imuControl.getDataBlock());
//		PamController.getInstance().notifyModelChanged(PamController.EXTERNAL_DATA_IMPORTED);
	}

	
		
	private void showErrorWarning(Integer errorCode){
		if (errorCode==null) return;
	
		switch (errorCode){
			case NO_CSV_LOAD:
				PamDialog.showWarning(PamController.getInstance().getMainFrame(), "Load File Error", "Unable to load .csv file. Check the file path is correct and the file is present.");
			break;
			case WRONG_FORMAT:
				PamDialog.showWarning(PamController.getInstance().getMainFrame(), "Data Format Error", "Unable to create data unit. The .csv file does not contain the correct data structure");
			break;
		}
	
	}
	
	public ImportLoadBar getIMULoadBar(){
		return importLoadBar;
	}
	
	/**
	 * Set whether the load progress is intermediate
	 * @param intermediate. true of load progress is intermediate. 
	 */
	public void setIMULoadIntermediate(boolean intermediate){
		importLoadBar.setIntermediate(intermediate);
	}

	
	
	class ShowProgress implements PropertyChangeListener{
		
		ImportIMUThread importCSVthread  ;
		
		public ShowProgress(ImportIMUThread saveDBThread){
			this.importCSVthread=saveDBThread;
			
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			updateLoadBar(importCSVthread);
		}
		
	}
	
	protected void updateLoadBar(ImportIMUThread importCSVthread){
	    if (importCSVthread.isDone()==false) {

	        int progress = importCSVthread.getProgress();
	        if (progress == 0) {
	        	importLoadBar.setIntermediate(true);
	            //taskOutput.append("No progress yet\n");
	        } 
	        else {
	        	 setLoadProgress(progress);
	        	 setTextProgress(importCSVthread.getNUnitsLoaded(),importCSVthread.getTotalNUnits(),"IMU Data Units");
	        }
	    }
	    else{
	    	importLoadBar.setProgress(100);
	    }
	}
	
	private class Stop implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (importThread!=null){
				importThread.cancel(true);
				importThread.done();
			}
		}
		
	}
	

	public void setLoadProgress(int prog){
		if (importLoadBar.isIntermediate()) importLoadBar.setIntermediate(false);
		importLoadBar.setProgress(prog);
	}
	
	public void setTextProgress(int N, int ofN, String name){
		importLoadBar.setTextUpdate(N + " of "+  ofN + " "+name+" loaded.");
	}
	
	
	public PropertyChangeListener getProportyListener(
			ImportIMUThread saveDBThread) {
		return new ShowProgress(saveDBThread);
	}
	


	public IMUDataBlock getIMUDataBlock() {
		return imuControl.getDataBlock();
	}

	public void addDataToDataBlock(AngleDataUnit dataUnit) {
		imuControl.addDataToDataBlock(dataUnit);
		
	}
	
	public IMUControl getImuControl() {
		return imuControl;
	}
	


}
