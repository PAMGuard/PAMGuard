package Array.importHydrophoneData;

import java.util.ArrayList;

import Array.ArrayManager;
import Array.HydrophoneLocator;
import Array.HydrophoneLocatorSystem;
import Array.HydrophoneLocators;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import Array.streamerOrigin.GPSOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GpsData;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.TxtFileUtils;
import PamView.importData.DataImport;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Class for importing streamer data from external file and saving to database. 
 * <p>
 * .csv file format: name, streamerIndex, origin, Locator, time (excel datenum),  xPos, yPos, zPos, xErr, yErr, zErr, heading, pitch, roll, lat, long. lat and long are optional. 
 * <b>.csv import formats</b>
 * <p>
 * <i>List Format:</i> 	
 * @author Jamie Macaulay
 */
@SuppressWarnings("rawtypes")
public class StreamerImport  extends DataImport<ArrayList<Double>>{

	/**
	 * Define the columns of the csv once here instead of 
	 * hard-coding magic numbers throughout
	 * @author brian_mil
	 *
	 */
	private static final int NAME 					=0;
	private static final int STREAMERINDEX 			=1;
	private static final int ORIGIN 				=2;	
	private static final int LOCATOR				=3;
	private static final int TIME					=4; 
	private static final int XPOS 					=5;
	private static final int YPOS 					=6;
	private static final int ZPOS 					=7;
	private static final int XERR 					=8;
	private static final int YERR 					=9;
	private static final int ZERR 					=10;
	private static final int HEADING 				=11;
	private static final int PITCH 					=12;
	private static final int ROLL 					=13;
	private static final int LAT					=14;
	private static final int LONG 					=15;
	private static final int NUMCOLUMNS 			= 14;
	private static final int NUMCOLUMNSWITHLATLONG 	= 16;
	
	private StreamerDataBlock streamerDataBlock;
	
	String[] extensionStrings={".csv"};
	
	private ArrayList<ArrayList<Double>> streamerPositions;

	StreamerImportParams params; 
	
	public StreamerImport(StreamerDataBlock streamerDataBlock) {
		this.streamerDataBlock=streamerDataBlock; 
		params=new StreamerImportParams();
	}

	@Override
	public ArrayList<ArrayList<Double>> loadDataIntermediate(String filePath) {
		
		if (filePath.endsWith(".csv")){	
			return streamerPositions=TxtFileUtils.importCSVData(filePath);
		}
		
		return null;
	}
	
	/**
	 * Determine whether the streamer is going to be a threading or straight hydrophone and origin methodsd
	 */
	@Override
	public boolean performPreChecks(){
//		System.out.println("Streamer Import: Perform pre checks: ");
//		StreamerImportParams newParams=StreamerOriginDialog.showDialog(PamController.getInstance().getMainFrame(), params);
//		if (newParams!=null) {
//			params=newParams.clone(); 
			return true;
//		}
//		else return false;
	}

	
	@Override
	public boolean isDataFormatOK(ArrayList<Double> dataLine) {
		if (dataLine.size()==NUMCOLUMNS || dataLine.size()==NUMCOLUMNSWITHLATLONG) return true;
		// TODO might need to put some extra bits and bobs here eventually. 
		return false;
	}

	@Override
	public PamDataUnit createDataUnit(ArrayList<Double> dataLine) {
		
		PamArray currentArray=ArrayManager.getArrayManager().getCurrentArray();
		/**
		 * TODO: Lookup the locator system by name, rather than an index since,
		 * these index values could change as more locatorSystems and OriginSystems
		 * are added to PamGuard
		 */
		int locatorIndex = dataLine.get(LOCATOR).intValue();  
		int originIndex = dataLine.get(ORIGIN).intValue();
		
		HydrophoneLocatorSystem hydropheonLocator=HydrophoneLocators.getInstance().getSystem(locatorIndex);
		HydrophoneOriginSystem hydropheonOrigin=HydrophoneOriginMethods.getInstance().getMethod(originIndex);
		
		long timeMillis= (long) PamUtils.PamCalendar.excelSerialtoMillis(dataLine.get(TIME));
		
		Streamer streamer = new Streamer(dataLine.get(STREAMERINDEX).intValue(),
				dataLine.get(XPOS),dataLine.get(YPOS),dataLine.get(ZPOS),
				dataLine.get(XERR),dataLine.get(YERR),dataLine.get(ZERR));
		
//		streamer.setEnableOrientation(true);
		streamer.setHeading(dataLine.get(HEADING));
		streamer.setPitch(dataLine.get(PITCH));
		streamer.setRoll(dataLine.get(ROLL));
		streamer.setStreamerName(dataLine.get(NAME).toString());
		
		//tell the streamer how to calculate hydrophone positions.
		HydrophoneLocator hydrophoneLocator=hydropheonLocator.getLocator(currentArray, streamer);
		streamer.setHydrophoneLocator(hydrophoneLocator);
		streamer.setLocatorSettings(hydrophoneLocator.getLocatorSettings());
		
		///tell the streamer how to find it's own location.
		HydrophoneOriginMethod originMethod=HydrophoneOriginMethods.getInstance().getMethod(hydropheonOrigin.getName(), currentArray, streamer);

		streamer.setHydrophoneOrigin(originMethod);
		
		streamer.setOriginSettings(originMethod.getOriginSettings());
	
		streamer.setupLocator(currentArray);
		
		if (dataLine.size()==NUMCOLUMNSWITHLATLONG && originIndex==1){// Has Latitude and Longitude with staticOrigin
//			LatLong latLong = new LatLong(dataLine.get(10), dataLine.get(11));
//			GpsData gpsData = new GpsData(latLong);
			GpsData gpsData = new GpsData(dataLine.get(LAT),dataLine.get(LONG),0,timeMillis);
			StaticOriginSettings originSettings = (StaticOriginSettings) originMethod.getOriginSettings();
			originSettings.setStaticPosition(streamer, gpsData);
			streamer.setOriginSettings(originSettings);
		}
		
		if (currentArray.getNumStreamers() <= dataLine.get(STREAMERINDEX).intValue()){
			currentArray.addStreamer(streamer);
		}
		StreamerDataUnit streamerData=new StreamerDataUnit(timeMillis, streamer);
		currentArray.setArrayName("Array on " + PamCalendar.formatDateTime(timeMillis));
		ArrayManager.getArrayManager().addArray(currentArray);
		return streamerData;
	}

	@Override
	public PamDataBlock getDataBlock() {
		return streamerDataBlock;
	}

	@Override
	public String[] getExtensionsStrings() {
		return extensionStrings;
	}

}
