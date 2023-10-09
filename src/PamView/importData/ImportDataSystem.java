package PamView.importData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.SwingWorker;

import PamController.PamController;
import PamView.ImportLoadBar;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataUnit;

/**
 * This class brings together the abstract classes and display componwenets which create a data import system for PAMGUARD. File dialogs, load bar dialogs etc. are all shared 
 * by most data import tasks. 
 * @param <T> - the type of data loaded form the file. This could be a singles String, an ArrayList<Double>, ArrayList<Long> etc. 
 * @author Jamie Macaulay
 */
public class ImportDataSystem<T>  {
	
	//must keep a reference to the dialog due to problems opening static functions
	ImportDataDialog importDataDialog;
	
	private FileImportParams fileimportParams;
	
	private String name="Data Import";

	private ImportLoadBar importLoadBar;
		
	/*
	 *The current import thread
	 */
	private ImportThread currentImportThread;

	private DataImport<T> dataImport;
	
	private int dataSaveIncrement=50; 
	
	//error codes
	/**
	 * Unable to load data file. Probably a bad file path. 
	 */
	public static final int NO_FILE_LOAD=0;
	/**
	* The file is in the wrong format. 
	*/
	public static final int WRONG_FORMAT=1;
	

		
	public ImportDataSystem(DataImport<T> dataImport ){
		this.dataImport=dataImport; 
	}
	
	public void showImportDialog(){
		if (importDataDialog==null) importDataDialog=new ImportDataDialog(PamController.getInstance().getMainFrame(), name,fileimportParams,dataImport.getExtensionsStrings() );
		FileImportParams newParams=importDataDialog.showDialog(PamController.getInstance().getMainFrame() , name,  fileimportParams, dataImport.getExtensionsStrings());
		if (newParams!=null){
			fileimportParams=newParams;
			//must check we have some files in the list. 
			if (fileimportParams.lastFiles.size()>0) {
				startImport(fileimportParams.lastFiles.get(0));
			}
		}
	}
	

	/**
	 * Starts the process of loading a file, converting to a data unit and then saving to database. 
	 * @param dataFile- file path to import
	 */
	@SuppressWarnings("static-access")
	protected void startImport(String dataFile){
		/**
		 * Open the dialog showing the progress bar
		 */
		importLoadBar=ImportLoadBar.showDialog(PamController.getInstance().getMainFrame(), name);
		importLoadBar.setProgress(0);
		importLoadBar.setIntermediate(true);
		//new load bar is created every time by show dialog so we are not adding multiple action listeners here. 
		importLoadBar.getCancelButton().addActionListener(new Stop());

		
		//Create a new thread. 
		currentImportThread=new ImportThread(dataFile);
		//Perform any pre-checks.
		if (dataImport.performPreChecks()){
		//start the thread. 
		currentImportThread.execute();
		}
		else endImport(null); 
		 
	}
	
	
 	class ShowProgress implements PropertyChangeListener{
		
		public void propertyChange(PropertyChangeEvent evt) {
			updateLoadBar(currentImportThread);
		}
	}
 	
 	private class Stop implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (currentImportThread!=null){
				currentImportThread.cancel(true);
				currentImportThread.done();
			}
		}
		
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
	}

	
	protected void updateLoadBar(ImportThread importThread){
	    if (importThread.isDone()==false) {

	        int progress = importThread.getProgress();
	        if (progress == 0) {
	        	importLoadBar.setIntermediate(true);
	            //taskOutput.append("No progress yet\n");
	        } 
	        else {
	        	 setLoadProgress(progress);
	        	 setTextProgress(getNUnitsLoaded(),getTotalNUnits(),dataImport.getDataUnitName());
	        }
	    }
	    else{
	    	importLoadBar.setProgress(100);
	    }
	}
	
	@SuppressWarnings("static-access")
	private void showErrorWarning(Integer errorCode){
		if (errorCode==null) return;
	
		switch (errorCode){
			case NO_FILE_LOAD:
				PamDialog.showWarning(PamController.getInstance().getMainFrame(), "Load File Error", "Unable to load file. Check the file path is correct and the file is present.");
			break;
			case WRONG_FORMAT:
				PamDialog.showWarning(PamController.getInstance().getMainFrame(), "Data Format Error", "Unable to create data unit. The .csv file does not contain the correct data structure");
			break;
		}
	}
	
	/**
	 * Set progress in the load bars' text output.
	 * @param N - number of units loaded
	 * @param ofN - total number of units to load
	 * @param name - name of the data unit that is being loaded.e.g. "NMEA units"
	 */
	public void setTextProgress(int N, int ofN, String name){
		importLoadBar.setTextUpdate(N + " of "+  ofN + " "+name+" loaded.");
	}
	
	/**
	 * Set the progress bar progress. Note that this will disable the intermediate state. 
	 * @param prog- progress0->0%, 100->100%
	 */
	protected void setLoadProgress(int prog){
		if (importLoadBar.isIntermediate()) importLoadBar.setIntermediate(false);
		importLoadBar.setProgress(prog);
	}
	
	/**
	 * Get the name of this loading system. e.g. Hydrophone Import
	 * @return name of the laoding system.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this loading system. e.g. Hydrophone Import
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Thread to import data. The import thread relies on the DaatImport class to load files and convert data into PamDataUnits to be saved to a datablock and then a database or binary file. 
	 * @author Jamie Macaulay
	 *
	 */
	@SuppressWarnings("rawtypes")
	class ImportThread extends SwingWorker{

		private String dateFile;
		private Integer errorCode;

		public ImportThread(String dataFile) {
			this.dateFile=dataFile;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground() throws Exception {
			try{
			//reset error code
			errorCode=null;
			
			//load the file			
			ArrayList<T> importData=dataImport.loadDataIntermediate(dateFile);
			if (importData==null){
				errorCode=NO_FILE_LOAD;
				this.cancel(true);
				done();
			}
			if (isDone() || isCancelled()) return false;
			
			//save the data in the file to the database 
			System.out.println(" ImportDataThread: No. data points: "+importData.size());
			PamDataUnit dataUnit; 
			
			setTotalUnits(importData.size());
			
			//must set a little progress to tell import manager we have finished loading .csv. 
			int progress=1;
			setSaveProgress(progress);
			for (int i=0; i<importData.size(); i++){
				
				//check we have the correct data format- if there is an error keep iterating through the data.
				if (!dataImport.isDataFormatOK(importData.get(i))){
					errorCode=WRONG_FORMAT;
					continue;
				}
				
				//stop the import if thread has been set as cancelled
				if (isDone() || isCancelled()) return false;
				
				dataUnit= dataImport.createDataUnit(importData.get(i));
				if (dataUnit!=null) dataImport.getDataBlock().addPamData(dataUnit);
				else System.out.println("ImportDataSystem: PamDataUnit is null.");
				
				//save the data- ensures if PAMGUARD crashes then the user does not lose imported data.
				if (i%dataSaveIncrement==0){
					setNLoadedUnits(i);
					progress=(Math.round(100*i/importData.size()));
					//need to trigger property change listener but don't want to set the laod bar to intermediate. 
					if (progress!=0) setSaveProgress(progress);
					else setSaveProgress(1);
					updateLoadBar(this);
					//save the data
					dataImport.getDataBlock().saveViewerData();
				}
				
			}
			dataImport.getDataBlock().saveViewerData();
			setProgress(100);
			return true; 
			}
			catch (Exception e){
				e.printStackTrace();
				return false; 
			}
		}
		
		public void setSaveProgress(int prog){
			//System.out.println("prog "+prog);
			setProgress(prog);
		}
		
		@Override
        public void done() {
			super.done();
			endImport(errorCode);
		}

	}
	
	private int totalUnits=0;
	private int nLoadedUnits=0;

	
	private void setTotalUnits(int totalUnits) {
		this.totalUnits=totalUnits;
	}
	
	private void setNLoadedUnits(int nLoadedUnits) {
		this.nLoadedUnits=nLoadedUnits;
	}
	
	/**
	 * The number of units which have been loaded into the database or binary files. 
	 * @return
	 */
	public int getNUnitsLoaded(){
		return nLoadedUnits;
	}
	
	/**
	 * The total number of units that need loaded into the database or binary files. 
	 * @return
	 */
	public int getTotalNUnits(){
		return totalUnits;
	}
	
	
		



}
