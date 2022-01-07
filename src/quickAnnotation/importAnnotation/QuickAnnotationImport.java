package quickAnnotation.importAnnotation;

import java.util.ArrayList;
import java.util.List;

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
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoices;
import annotation.string.StringAnnotation;
import annotationMark.MarkDataUnit;
import quickAnnotation.QuickAnnotationModule;

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
public class QuickAnnotationImport  extends DataImport<ArrayList<String>>{

	/**
	 * Define the columns of the csv once here instead of 
	 * hard-coding magic numbers throughout
	 * @author brian_mil
	 *
	 */
	private enum col {
		UTC, Channel, Duration, f1, f2, Label, Note
	}
	private static final int UTC					=0;
	private static final int CHANNEL				=1;
	private static final int DURATION 			    =2;
	private static final int F1     				=3;	
	private static final int F2						=4;
	private static final int LABEL					=5; 
	private static final int NOTE 					=6;
	private static final int NUMCOLUMNS 			=7;

	private QuickAnnotationModule quickAnnotationModule;
	private PamDataBlock quickAnnotationDataBlock;
	
	String[] extensionStrings={".csv"};
	
	private ArrayList<ArrayList<String>> streamerPositions;

	QuickAnnotationImportParams params; 
	
	public QuickAnnotationImport(QuickAnnotationModule quickAnnotationModule) {
		this.quickAnnotationModule = quickAnnotationModule;
		this.quickAnnotationDataBlock=quickAnnotationModule.getAnnotationDataBlock(); 
		params=new QuickAnnotationImportParams();
	}

	@Override
	public ArrayList<ArrayList<String>> loadDataIntermediate(String filePath) {
		
		if (filePath.endsWith(".csv")){	
			return streamerPositions=TxtFileUtils.importTxtDataToString(filePath);
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
	public boolean isDataFormatOK(ArrayList<String> dataLine) {
		if (dataLine.size()==NUMCOLUMNS | dataLine.size()==NUMCOLUMNS-1) return true;
		// TODO might need to put some extra bits and bobs here eventually. 
		return false;
	}

	public PamDataUnit createDataUnit(ArrayList<String> dataLine) {
		
	
		/**
		 * TODO: Lookup the locator system by name, rather than an index since,
		 * these index values could change as more locatorSystems and OriginSystems
		 * are added to PamGuard
		 */
		long utc = Long.parseLong(dataLine.get(UTC));
		int channels = (int) Long.parseLong(dataLine.get(CHANNEL));
		double duration = Double.parseDouble(dataLine.get(DURATION));
		long durationMillis = (long) Math.ceil(duration*1000);
		double f1 = Double.parseDouble(dataLine.get(F1));
		double f2 = Double.parseDouble(dataLine.get(F2));
		String label = dataLine.get(LABEL);
		String note = null;
		if (dataLine.size()==NUMCOLUMNS) {
			note = dataLine.get(NOTE);
		};
		long startSample = -1;
		
		MarkDataUnit adu = new MarkDataUnit(utc, channels, durationMillis);
		// set the datablock now since it's needed in some calculations. 
		adu.setParentDataBlock(quickAnnotationDataBlock);
		adu.setFrequency(new double[] {f1,  f2});
		adu.setChannelBitmap(channels);
		List<DataAnnotationType<?>> ans = quickAnnotationModule.getAnnotationHandler().getUsedAnnotationTypes();
//		for (DataAnnotationType<?> anType:ans) {
//			if(anType.canAutoAnnotate());
//				anType.autoAnnotate(adu);
//		}
		StringAnnotation labelAnnotation = new StringAnnotation(quickAnnotationModule.getLabelAnnotationType());
		StringAnnotation noteAnnotation = new StringAnnotation(quickAnnotationModule.getStringAnnotationType());
		labelAnnotation.setString(label);
		noteAnnotation.setString(note);
		adu.addDataAnnotation(labelAnnotation);
		adu.addDataAnnotation(noteAnnotation);
		
		return adu;
		}

	@Override
	public PamDataBlock getDataBlock() {
		return quickAnnotationDataBlock;
	}

	@Override
	public String[] getExtensionsStrings() {
		return extensionStrings;
	}

}
