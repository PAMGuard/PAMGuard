package GPS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.swing.SwingWorker;

import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import PamUtils.TxtFileUtils;
import PamView.ImportLoadBar;

/**
 * Class to take external GPS data and record in a database table. Developers should be able to easily add functions to load data the simply add to the switch statement in the doInBackground() function of the 
 * ImportGPSThread. Inside the function, the ImportGPSThread should be updated with load progress. Everything else should then just work. 
 * @author Jamie Macaulay
 *
 */
public class ImportGPSData {
	
	GPSDataBlock gpsDataBlock;
	
	GPSControl gpsControl; 
	
	PamFileFilter fileFilter;
	
	ImportGPSThread importGPSThread;

	private GpsDataUnit gpsDataUnit;

	private ImportLoadBar importLoadBar;

//	public final static int  NMEA_TEXT=1;
//	public final static int KML=2;	
//	public final static int GPX=3; 	
//	public final static int CSV=4; 

	//keep the last five units in memory
	private  ArrayList<Long> lastUnits=new ArrayList<Long>();
	
	//maximum number of days between the average of the last five units a new GPS data unit. Prevents spurious dates from corrupt NMEA strings or binary files. 
	private int maxDaysOut=30;
	
	
	public ImportGPSData(GPSControl gpsControl){	
		this.gpsControl=gpsControl;
		this.gpsDataBlock=gpsControl.getGpsDataBlock();
		//types of file to load
		fileFilter=new PamFileFilter("GPS Data", ".txt"); 
		fileFilter.addFileType(".asc");
		fileFilter.addFileType(".nmea");
		fileFilter.addFileType(".csv");

		
	}
	
	/**
	 * Get file filter for GPS files 
	 * @return file filter. 
	 */
	protected PamFileFilter getGPSFileFilter(){
		return fileFilter;
	}
	
	/**
	 * Import data from a file 
	 * @param file - path to file; 
	 */
	public void loadFile(String fileString){
		
		importLoadBar=ImportLoadBar.showDialog(PamController.getInstance().getMainFrame(), "Load GPS Data");
		importLoadBar.setProgress(0);
		importLoadBar.setIntermediate(true);
		PamController.getInstance().enableGUIControl(false);
		
		
		importGPSThread=new ImportGPSThread(fileString);
		importLoadBar.getCancelButton().addActionListener(new StopGPSImportThread(importGPSThread));
		
		performPreChecks(new File(fileString));

		importGPSThread.execute();
		
	}
	
	/**
	 * Update the data map after external data has been loaded. Because we know that only gps data has been added we don't need to update all data blocks. 
	 */
	private void updateDataMap(){
		PamController.getInstance().updateDataMap();
	}
	
	//TODO
	/**
	 * Having problems with deadlock when opening dialog from inside thread so this is a quick fix. Not as pretty- sure a better fix is available
	 */
	public void performPreChecks(File file){
		String extension=PamFileFilter.getExtension(file);
		try{
			switch (extension){
			
			case "txt":
				openGGADialog( file);
				break;
			case "asc":
				openGGADialog( file);
				break;
			case "nmea":
				openGGADialog( file);
				break;
			case "csv":
				break; 
			case "kml":
				break;
			case "gpx":
				break;
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	//TODO
	/**Open GGA dialog if necessary. 
	 * Having problems with deadlock when opening dialog from inside thread so this is a quick fix. Not as pretty- sure a better fix is available
	 */
	synchronized void openGGADialog(File file){
		 Collection<String> nmeaStrings=importFileStrings( file.getAbsolutePath());
		 
		 if  (checkGGA(nmeaStrings)) {
			 ImportGPSParams newParams=ImportGGADialog.showDialog(PamController.getInstance().getMainFrame(), PamController.getInstance().getMainFrame().getMousePosition(), gpsControl.gpsImportParams, this) ;
			 if (newParams!=null) gpsControl.gpsImportParams=newParams.clone();
		 }
	}
	
	class StopGPSImportThread implements ActionListener{
		ImportGPSThread importGPSThread;
		
		public StopGPSImportThread(ImportGPSThread importGPSThread){
			this.importGPSThread=importGPSThread;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			importGPSThread.cancel(true);
			importGPSThread.done();
		}
		
		
	}
	
	/**
	 * Thread for importing GPS data. 
	 * @author Jamie Macaulay
	 *
	 */
	class ImportGPSThread extends SwingWorker<Boolean, Void> {
		
		File file;
		String extension;
	
		
		public ImportGPSThread(String fileString){
			
			 file= new File(fileString);
			 extension=PamFileFilter.getExtension(file);
			
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			
			lastUnits=new ArrayList<Long>();
			
			try{
				switch (extension){
				
				case "txt":
					loadGPSTextFile(file, this);
					break;
				case "asc":
					loadGPSTextFile(file, this);
					break;
				case "nmea":
					loadGPSTextFile(file, this);
					break;
				case "kml":
					loadKMLFile(file , this);
					break;
				case "gpx":
					loadGPXFile(file , this);
					break;
				case "csv":
					loadCSVFile(file , this);
					break;
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
	
			return true;
			
		}
		
		/**
		 * Set load progress of the load thread. 
		 * @param prog - progress % 0-1; 
		 */
		public void setLoadProgress(int prog){
			if (importLoadBar.isIntermediate()) importLoadBar.setIntermediate(false);
			importLoadBar.setProgress(prog);
			setProgress(prog);
		}
		
		/**
		 * Set progress for text output.
		 * @param N - number of loaded units
		 * @param ofN - total units to load
		 * @param name - name of the units loading 
		 */
		public void setTextProgress(int N, int ofN, String name){
			importLoadBar.setTextUpdate(N + " of "+  ofN + " "+name+" loaded.");
		}
		
		@Override
        public void done() {
			super.done();
			PamController.getInstance().enableGUIControl(true);
			if (importLoadBar!=null){
//				System.out.println("done loading data");
				importLoadBar.setIntermediate(false);
				importLoadBar.setVisible(false);
			}
			//finally, update te datamap
			updateDataMap();
		}
			
		

	}
	
	/**
	 * Import from .csv or txt- creates an array of strings. 
	 * @param filename
	 * @return
	 */
	public static Collection<String> importFileStrings(String filename){
		
		Collection<String> lines = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
				new File(filename)
			));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return lines;
		
	}
	
	/**
	 * Check if a file contains GGA strings. GGA strings are a problem due to the fact they do not time but no date. The user must manually set the date if GGA strings are the only NMEA strings which have lat long data. 
	 * @param nmeaStrings
	 * @return
	 */
	private boolean checkGGA( Collection<String> nmeaStrings){
		
		 String stringType; 
		 int nmeaPos;
		 int typePos;
		 
		 boolean GGA=false; 
		 
		 for (String line : nmeaStrings) {
			 
			 nmeaPos=line.indexOf("$");
			 if (nmeaPos<0) continue;
			 line=line.substring(nmeaPos,line.length());
			 
			 //find the type of nmea string
			 typePos =line.indexOf(",");
			 if (typePos<0) typePos=line.indexOf("*");
			 if (typePos<0) continue; 
			 stringType= line.substring(1, typePos);
			 
			 if (stringType.equals("GPGGA")){
				 GGA=true;
				 return GGA; 
			 }
		
 		 }
		 
		 return GGA; 
	}	
	
	/**
	 * Load GPS data from a text file containing NMEA strings. 
	 * @param file- file path.
	 * @param importGPSThread - the thread used to import the GPS data.
	 */
	 private void loadGPSTextFile(File file, ImportGPSThread importGPSThread){
	
		 Collection<String> nmeaStrings=importFileStrings( file.getAbsolutePath());
		 
//		 openGGADialog(nmeaStrings);
		 
		 int n=0; 
		 
		 StringBuffer nmeaString;
		 String stringType; 
		 GpsData gpsData=null; 
		 String headingString;
		 Double trueHeading;
		 Double magHeading; 
		 Double varHeading;
		 int nmeaPos;
		 int typePos;
		 
		 for (String line : nmeaStrings) {
			 
			 if (importGPSThread.isCancelled()) break; 
			 
			 nmeaPos=line.indexOf("$");
			 if (nmeaPos<0) continue;
			 line=line.substring(nmeaPos,line.length());
			 
			 //find the type of nmea string
			 typePos =line.indexOf(",");
			 if (typePos<0) typePos=line.indexOf("*");
			 if (typePos<0) continue; 
			 stringType= line.substring(1, typePos);
	
			 nmeaString=new StringBuffer(line);
			 
			// System.out.println("NMEA String: " +nmeaString);
			 
			 if (n%100==0){
				 gpsDataBlock.saveViewerData();
			 }
			 
			 if (n%5==0){
//				 System.out.println("GPS load progress: " + (double) 100* n/ (double) nmeaStrings.size()+ " %");
				 importGPSThread.setLoadProgress((int) Math.round((double) 100*n/ (double) nmeaStrings.size()));
				 importGPSThread.setTextProgress(n, nmeaStrings.size(), "NMEA strings");
			 }
			 n++;
			 
			 try{
				 
				 switch (stringType){
			 
					 case "GPGGA":{
						 
						 if (gpsControl.gpsImportParams.useGGA){
							 
							 //last data was Ok and in this string no extra heading data etc to add so save previous and start another
							 if (gpsData!=null ){
								 if (gpsData.isDataOk()) saveGPSData(gpsData);
							 }
							 
							 gpsData=new GpsData(nmeaString, gpsControl.gpsParameters.READ_GGA);
							 ///GGA strings contain no date so we need to set it. 
							 gpsData.getGpsCalendar().set(Calendar.YEAR, gpsControl.gpsImportParams.year);
							 gpsData.getGpsCalendar().set(Calendar.MONTH, gpsControl.gpsImportParams.month);
							 gpsData.getGpsCalendar().set(Calendar.DAY_OF_MONTH, gpsControl.gpsImportParams.day);
							 gpsData.setTimeInMillis(gpsData.getGpsCalendar().getTimeInMillis());
							 
							 //System.out.println("gpsData:" +gpsData.getGpsCalendar().getTime().toString());
							 // System.out.println("nmeaStrings:" +nmeaStrings.size());
						 }
						 break;
					 }
					 
					 
					 case "GPRMC":{
						 
						 //last data was Ok and in this string no extra heading data etc to add so save previous and start another
						 if (gpsData!=null ){
							 if (gpsData.isDataOk()) saveGPSData(gpsData);
						 }
						 
//						 System.out.println("nmea string: "+nmeaString);
						 gpsData=new GpsData(nmeaString, gpsControl.gpsParameters.READ_RMC);
						 
						 break; 
					 }
					 case "GPHDT":{
						 //true heading- likely from a vector GPS
						 if (gpsData!=null && gpsData.isDataOk()){
							 headingString=nmeaString.substring(nmeaString.indexOf(",")+1, nmeaString.length());
							 headingString=headingString.substring(0,  (headingString.indexOf(",")-1));
							 trueHeading=Double.valueOf(headingString);
							 gpsData.setTrueHeading(trueHeading);
						 }
						 
						 break;
					}
					 
					case "HCHDG":{
						 //Garmin eTrex summit, Vista and GPS76S receivers output value for the internal flux-gate compass. A magnetic heading. 
						 if (gpsData!=null && gpsData.isDataOk()){
							 
							 headingString=nmeaString.substring(nmeaString.indexOf(",")+1, nmeaString.length());
							 
							 //magnetic heading
							 magHeading=Double.valueOf(headingString.substring(0,  (headingString.indexOf(",")-1)));
							 
							 //heading variation
							 headingString=headingString.substring(headingString.indexOf(",")+1,  headingString.lastIndexOf("*"));
							 headingString=headingString.substring(headingString.indexOf(",")+1,  headingString.length());
							 headingString=headingString.substring(headingString.indexOf(",")+1,  headingString.length());
							 							 
							 varHeading=Double.valueOf(headingString.substring(0, headingString.indexOf(",")-1));
							 
							 //true heading
							 if (headingString.substring(headingString.indexOf(",")+1,headingString.length()).contentEquals(new StringBuffer("W"))){
								 trueHeading=magHeading+varHeading; 
							 }
							 else{
								 trueHeading=magHeading-varHeading; 
							 }
							 
							 gpsData.setTrueHeading(trueHeading);
						 }
						 
						 break;
					}
			 	}
			 }
			 catch(Exception e){
				 e.printStackTrace();
			 }

		 }
		 
		 gpsDataBlock.saveViewerData();

	 };
	 
	public void saveGPSData(GpsData gpsData){
		//System.out.println("GPSData to save:  " +gpsData + "gpsDataBlock: "+gpsDataBlock);
		//System.out.println("GPSData time:  " +gpsData.getTimeInMillis());
		if (gpsData.getHeading(true)==null){
			 //need to calculate course over ground. 
			 GpsDataUnit oldGPS = gpsDataBlock.getLastUnit();
			 if (oldGPS!=null){
				 LatLong oldLatLong=new LatLong( oldGPS.getGpsData().getLatitude(), oldGPS.getGpsData().getLongitude());
				 double bearing=oldLatLong.bearingTo(new LatLong(gpsData.getLatitude(),gpsData.getLongitude()));
				 gpsData.setCourseOverGround(bearing);
			 }
		 }
		
		gpsDataUnit= new GpsDataUnit(gpsData.getTimeInMillis(), gpsData) ;
		if (checkDate(gpsDataUnit)){
			
			gpsDataUnit.setChannelBitmap(-1);
			
			gpsDataBlock.addPamData(gpsDataUnit);	
		}
		else{
			System.out.println("Unlikely date: GPS Data not saved to database...");
		}
	}
	
	/**
	 * Occasionally a completely spurious date is recorded. The field lastUnits keep a record of the past five timeMillis. If the average of these five is more than maxDaysOut out from the GPS data unit then the GPS data unit is not saved. 
	 * This prevents dates decades in the future or past been recorded. 
	 * @param gpsDataUnit - gps data unit. 
	 * @return - true if the date seems OK. 
	 */
	public boolean checkDate(GpsDataUnit gpsDataUnit){
	
		long average = 0;
		if (lastUnits.size()>=5){
			for (int i=0; i<lastUnits.size(); i++){
				average+=lastUnits.get(i);
			}
			average=average/lastUnits.size();
		
			if (Math.abs(average-gpsDataUnit.getTimeMilliseconds())>PamCalendar.millisPerDay*maxDaysOut) return false;
			else{
				lastUnits.add(0,gpsDataUnit.getTimeMilliseconds());
				lastUnits.remove(4);
			}
		}
		else{
			lastUnits.add(gpsDataUnit.getTimeMilliseconds());
		}
		return true; 
	}
	 
	private void loadKMLFile(File file , ImportGPSThread importGPSThread){
		// TODO Auto-generated method stub
	};
	 
	private void loadGPXFile(File file2, ImportGPSThread importGPSThread) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Import data from a .csv file. The GPS file which can imported must have the following format. 
	 * <br>
	 * Column 0-  datenum time in - EXCEL or MATLAB datenum accepted. 
	 * <br>
	 * Column 1 - latitude in decimal format
	 * <br>
	 * Column 2 - longitude in decimla format
	 * <br>
	 * Column 3 - true heading in degrees 0-360 (not necessary); 
	 * @param file - file to import
	 * @param importGPSThread -reference to thread on which importing is running. 
	 */
	private void loadCSVFile(File file , ImportGPSThread importGPSThread){
		
		//first import .csv file. 
		ArrayList<ArrayList<Double>> gpsDataArray=TxtFileUtils.importCSVData(file.getAbsolutePath());
		
		
		GpsData gpsData; 
		long timeMillis = 0; 
		int n=0; 
		for (int i=0; i<gpsDataArray.size(); i++){
			
			if (gpsDataArray.get(i).get(0) > 700000) timeMillis=PamCalendar.dateNumtoMillis(gpsDataArray.get(i).get(0));
			else timeMillis=PamCalendar.excelSerialtoMillis(gpsDataArray.get(i).get(0));
			
			gpsData=new GpsData(gpsDataArray.get(i).get(1), gpsDataArray.get(i).get(2) , 0, timeMillis);  
			
			//add true heading if in data
			if (gpsDataArray.get(i).size()>3) gpsData.setTrueHeading(gpsDataArray.get(i).get(3)); 
			
			//save data 
			saveGPSData(gpsData);
			
			//set progress. 
			 if (n%5==0){
//				 System.out.println("GPS load progress: " + (double) 100* n/ (double) gpsDataArray.size()+ " %");
				 importGPSThread.setLoadProgress((int) Math.round((double) 100*n/ (double) gpsDataArray.size()));
				 importGPSThread.setTextProgress(n, gpsDataArray.size(), "csv data");
			 }
			 n++;
			 //save data
			 if (n%100==0){
				 gpsDataBlock.saveViewerData();
			 }
				
		}
		
		gpsDataBlock.saveViewerData();

	}
	 
}
