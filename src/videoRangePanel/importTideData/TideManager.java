package videoRangePanel.importTideData;

import java.util.ArrayList;
import java.util.List;

import PamController.OfflineDataStore;
import PamUtils.LatLong;
import PamUtils.TxtFileUtils;
import PamView.importData.DataImport;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMap;
import generalDatabase.DBControlUnit;
import videoRangePanel.VRControl;

/**
 * 
 * The tide manager open tide data and holds it in memory.
 * @author Jamie Macaulay
 *
 */
public class TideManager extends  DataImport<String>   {
	
	/**
	 * Reference to CR control  
	 */
	private VRControl vrControl;
	
	/**
	 * The tide data block
	 */
	private TideDataBlock tideDataBlock; 
	
	/**
	 * List of parsers for importing tide files 
	 */
	private ArrayList<TideParser> tideParsers;


//	private int maxStrikeCount=5; 
	
	/**
	 * Extensions for the tide file. 
	 */
	private String[] extensions = new String[]{"csv", "txt"};


	/**
	 * The latitude and longitude
	 */
	private LatLong latLong;

	/**
	 * The current parser
	 */
	private TideParser currentParser;

	public TideManager(VRControl vrControl) {
		this.vrControl=vrControl; 
		
		//create the datablock 
		this.tideDataBlock=new TideDataBlock("Tide Height Data: " , vrControl.getVRProcess());
		this.tideDataBlock.setDatagramProvider(new TideDatagramProvider());
		DBControlUnit dbControl = DBControlUnit.findDatabaseControl(); 
		if (dbControl != null) {
			TideDataMap dataMap = new TideDataMap(dbControl, tideDataBlock); 
			//tideDataBlock.addOfflineDataMap(dataMap);
		}
		
		//System.out.println("Tide OFFLINE DATA BLOCK: " + tideDataBlock.getNumOfflineDataMaps() + " Database: " +dbControl);

		tideParsers = new ArrayList<TideParser>(); 
		//add parsers here
		tideParsers.add(new POLPREDParser1()); 
		tideParsers.add(new POLPREDParser2());
		tideParsers.add(new POLPREDParser3());
		tideParsers.add(new CSVTideParser());


	}
	
	//TODO. Does not work yet. 
	public class TideDataMap extends OfflineDataMap {

		public TideDataMap(OfflineDataStore offlineDataStore, PamDataBlock parentDataBlock) {
			super(offlineDataStore, parentDataBlock);
		}
		
	}
	

//	public File getFile() {
//		File file=vrControl.getVRParams().currTideFile;
//		return file; 
//	}

	
//	/**
//	 * Open a browser to find a file. The fuile must be a polpred text file. 
//	 * @return 
//	 */
//	public String findFile(File lastFile) {
//		String dir; 
//		if (lastFile!=null){
//				dir=lastFile.getParent();
//		}
//		else dir=null;
//			
//		String newFile=PamFileBrowser.fileBrowser(vrControl.getPamView().getGuiFrame(),dir,PamFileBrowser.OPEN_FILE,"txt");
//		
//		return newFile;
//	}
	
//	/**
//	 * Import a text file of tidal heights
//	 * @param filePath - the path of the the file to import
//	 */
//	public void importTideTxtFile(String filePath){
//		File file = new File(filePath); 
//		importTideTxtFile( file);
//		PamController.getInstance().updateDataMap(); // update the data map 
//	}

//	/**
//	 * Import a file of tidal heights. 
//	 * @param file - the file to import
//	 */
//	public void importTideTxtFile(File file){
//		if (file==null) return; 
//		//surround with try since this is accessing external data:
//		boolean success; 
//		for (int i=0; i<this.tideParsers.size(); i++){
//
//			if (file.exists()){
//				success = loadTideFile( file.getPath(), tideParsers.get(i));
//				if (success){
//					System.out.println("Successfully loaded data using tide parser: " +  tideParsers.get(i).getName());
//					return; 
//				}
//				else {
//					System.out.println("Could not load data using tide parser: " +  tideParsers.get(i).getName());
//				}
//			}
//			else {
//				System.err.println("The file does nto exist!");
//			}
//
//		}
//
//
//	}
//	
	
//	/**
//	 * Go through the file and save data. 
//	 */
//	@SuppressWarnings("unchecked")
//	public boolean loadTideFile(String filePath, TideParser parser){
//	
//		int strikeCount=0; 
//		//load the file and create an array of strings
//		
//		List<String> txtData = TxtFileUtils.importTxtToCollection(filePath); 
//		
//		//TxtFileUtils.printData(txtData);
//		
//		//next get the location
//		LatLong latLong=parser.getLocation(txtData);
//
//		//now convert data from string to numbers and add to datablock. 
//		TideDataUnit tideDataUnit = null; 
//		for (int i=0; i<txtData.size(); i++){
//			if (strikeCount>maxStrikeCount){
//				return false; 
//			}
//			try{
//				tideDataUnit=parser.parseTideLine(txtData.get(i),latLong);
//			}
//			catch (Exception e){
//				e.printStackTrace();
//				System.err.println("Could convert the test string to a file: parser: " + parser.getName());
//				strikeCount++;
//			}
//			if (tideDataUnit!=null){
//				tideDataBlock.addPamData(tideDataUnit); //THE DATA UNIT HAS BEEN ADDED TO A DATA BLOCK. IT IS SAVED HERE. 
////				System.out.println("Time: "+PamCalendar.formatDate(tideDataUnit.getTimeMilliseconds())+" Level: "+tideDataUnit.getLevel()+ " speed: "+tideDataUnit.getSpeed()+" direction: "+Math.toDegrees(tideDataUnit.getAngle()));
//			}
//			else{
//				strikeCount++;
//			}
//		}
//		
//		return true;
//		
//	}
	
	/**
	 * Get the height of the tide above mean sea level at a given time. 
	 * @param timeMillis
	 * @return
	 */
	public double getHeightOffset(long timeMillis){
		TideDataUnit dataUnit= getInterpData( timeMillis);
//		System.out.println("Tide data has been found: " +  dataUnit);
		if (dataUnit==null) return 0; 
		else return dataUnit.getLevel();
	}
	
	/***
	 * Get a tide data unit which is interpolated from tide data.
	 * @param timeMillis - date and time in millis
	 * @return a tide dtaa unit with tidal height at the specified time 
	 */
	public TideDataUnit getInterpData(long timeMillis){
		return tideDataBlock.findInterpTideData(timeMillis);
	}
	
	
	public double getHeightOffset() {
		if (vrControl.getImageTime()==0) return 0; 
		return getHeightOffset(vrControl.getImageTime());
	}

	/**
	 * Get teh datablock which adds tide data. 
	 * @return the tide data block. 
	 */
	public PamDataBlock getTideDataBlock() {
		return this.tideDataBlock;
	}
	
	
	/**
	 * Try and find the latitude and longitude in the text file
	 * @param txtData - the txt data.
	 */
	private LatLong getLocation(List<String> txtData) {
		LatLong location = null; 
		for (int i=0; i<this.tideParsers.size(); i++) {
			location=tideParsers.get(i).getLocation(txtData);
			if (location!=null) break; //success parsing of location 
		}
		return location; 
	}


	@Override
	public ArrayList<String> loadDataIntermediate(String filePath) {
		this.latLong=null; 
		this.currentParser=null; 
		//load the file and create an array of strings
		
		List<String> txtData = TxtFileUtils.importTxtToCollection(filePath); 
		
		//TxtFileUtils.printData(txtData);
		
		//next get the location
		latLong=getLocation(txtData);
		
		//find the current parser
		this.currentParser=findParser(txtData); 
		
		//the current parser 
		if (currentParser==null) {
			return null;
		}
		else {
			return (ArrayList<String>) txtData;			
		}
	}

	/**
	 * Find the parser that works. Tries 10 sample points from each text files and finds the parser with the
	 * least number of errors  
	 * @return the parser that works. Null if no parser has worked. 
	 */
	private TideParser findParser(List<String> txtData) {
		int maxTries=20; 
		int[] strikeCount=new int[tideParsers.size()];
		int strike=0; 
		TideDataUnit testUnit; 
		
		for (int i=0; i<this.tideParsers.size(); i++) {
			strike=0; 
			for (int j=0; j<Math.min(txtData.size(), maxTries); j++) {
				try {
					testUnit=tideParsers.get(i).parseTideLine(txtData.get(j),null);
					if (testUnit==null) {
						strike++; 
					}
				}
				catch (Exception e){
					e.printStackTrace();
					strike++;
				}
			} 
			strikeCount[i]=strike; 
//			System.out.println("NO. STRIKES: " + strikeCount[i] + tideParsers.get(i).getName());
		}
		
		//find the minimum number of strikes. 
		int minIndex=-1;
		int minValue=Integer.MAX_VALUE; 
		for (int i=0; i<strikeCount.length; i++) {
			if (strikeCount[i]<minValue) {
				minValue=strikeCount[i]; 
				minIndex=i;
			}
		}
		
		if (strikeCount[minIndex]<3) {
			return tideParsers.get(minIndex); 
		} 
		else return null;
	}


	@Override
	public boolean isDataFormatOK(String dataLine) {
		return true;
	}


	@Override
	public PamDataUnit createDataUnit(String dataLine) {
		try {
			//System.out.println("Parse Tide Line: " + dataLine +  " Parser: " + currentParser.getName());
			return this.currentParser.parseTideLine(dataLine,latLong);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null; 
		}
	}


	@Override
	public PamDataBlock getDataBlock() {
		return tideDataBlock;
	}


	@Override
	public String[] getExtensionsStrings() {
		return this.extensions;
	}




	

}
