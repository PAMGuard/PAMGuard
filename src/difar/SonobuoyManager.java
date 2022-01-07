package difar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
import Array.StreamerDialog;
import Array.StreamerLogging;
import Array.streamerOrigin.OriginSettings;
import Array.streamerOrigin.StaticOriginSettings;
import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamProcess;
import PamguardMVC.RequestCancellationObject;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationHandler;
import annotation.string.StringAnnotation;
import annotation.string.StringAnnotationType;
import annotation.timestamp.TimestampAnnotation;
import annotation.timestamp.TimestampAnnotationType;
import difar.calibration.CalibrationDataUnit;
import difar.calibration.CalibrationDialog;
import difar.calibration.CalibrationHistogram;
import difar.calibration.CalibrationProcess;
import difar.dialogs.SonobuoyDialog;
import generalDatabase.DBControl;
import generalDatabase.DBControlUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;
import geoMag.MagneticVariation;

/**
 * SonobuoyManager should contain all of the Sonobuoy related functions in the
 * DIFAR module. Previous to 2018-07-24, These functions were split between
 * SonobuoyManager and DifarControl.
 * 
 * To the Difar Module (& SonobuoyManager) a sonobuoy is essentially an
 * Array.Streamer (or perhaps StreamerDataUnit) with some additional methods and
 * annotations added by the DIFAR module. These additional methods include
 * routines for deploying, calibrating, editing, and ending sonobuoys.
 * SonobuoyManager also manages the data structure for the SonobuoyLog
 * (SonobuoyManagerPanel)
 * 
 * Eventually it might make sense to extend Array.Streamer instead of using
 * custom module-specific annotations.
 * 
 * @author brian_mil
 *
 */
public class SonobuoyManager extends PamProcess {

	public static final int COLUMN_DATABASEID  = 0;
	public static final int COLUMN_NAME    	= 1;
//	public static final int COLUMN_ACTION = 2;
	public static final int COLUMN_TIMESTAMP   = 2;
	public static final int COLUMN_ENDTIME		= 3;
	public static final int COLUMN_CHANNEL     = 4;
	public static final int COLUMN_LATITUDE    = 5;
	public static final int COLUMN_LONGITUDE   = 6;
	public static final int COLUMN_DEPTH    	= 7;
	public static final int COLUMN_HEADING    	= 8;	
	public static final int COLUMN_COMPASSCORRECTION = 9;
	public static final int COLUMN_CALSTDDEV = 10;

	DifarControl difarControl;

	public TimestampAnnotationType sonobuoyEndTimeAnnotation = new TimestampAnnotationType("SonobuoyEndTime");
	
	public StringAnnotationType calMeanAnnotation = new StringAnnotationType("CompassCorrection",6);
	
	public StringAnnotationType calStdDevAnnotation = new StringAnnotationType("CompassStdDev",6);
	
	public StreamerDataBlock buoyDataBlock; 
	
	public String[] columnNames = {"UID",
			"Name",
//			"Action",
			"Deploy Time",
			"End Time",
			"Channel",
			"Latitude",
			"Longitude",
			"Depth",
			"Calibration (Heading)",
//			"Compass Correction",
//			"Std. Dev. (calibration)"
	};
	public Object tableData [][] = null;
	public DefaultTableModel tableDataModel = new SonobuoyTableModel(tableData, columnNames);
	private AnnotationChoiceHandler annotationHandler;
	
	public SonobuoyManager(DifarControl difarControl) {
		super(difarControl, null);
		this.difarControl = difarControl;
		buoyDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		buoyDataBlock.getLogging().setUpdatePolicy(SQLLogging.UPDATE_POLICY_OVERWRITE);
		annotationHandler = new SonobuoyAnnotationHandler(this, (PamDataBlock) buoyDataBlock);
		buoyDataBlock.setAnnotationHandler(annotationHandler);
		annotationHandler.addAnnotationType(sonobuoyEndTimeAnnotation);
//		annotationHandler.addAnnotationType(calMeanAnnotation);
//		annotationHandler.addAnnotationType(calStdDevAnnotation);
		annotationHandler.loadAnnotationChoices();
		sortSQLLogging();
	}

	/**
	 * Check all the SQL Logging additions are set up correctly. 
	 */
	protected void sortSQLLogging() {
		if (annotationHandler.addAnnotationSqlAddons(buoyDataBlock.getLogging()) > 0) {
			// will have to recheck the table in the database. 
			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
			if (dbc != null) {
				dbc.getDbProcess().checkTable(buoyDataBlock.getLogging().getTableDefinition());
			}
		}
	}

//	public void addSonobuoyAnnotation(StreamerDataUnit sdu, StringAnnotationType annotation, String annotationText){
//		StringAnnotation an = new StringAnnotation(annotation);
//		an.setString(annotationText);
//		sdu.addDataAnnotation(an);
//		overwriteSonobuoyData(sdu);
//	}
	
	public void addSonobuoyAnnotation(StreamerDataUnit sdu, TimestampAnnotationType annotation, long timestamp){
		TimestampAnnotation an = new TimestampAnnotation(sonobuoyEndTimeAnnotation);
		an.setTimestamp(timestamp);
		sdu.addDataAnnotation(an);
		overwriteSonobuoyData(sdu);
	}

	public void overwriteSonobuoyData(StreamerDataUnit sdu){
		buoyDataBlock.updatePamData(sdu, System.currentTimeMillis());
	}

	@Override
	public void pamStart() {
		updateSonobuoyTableData();
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		// TODO Auto-generated method stub
		if (o == buoyDataBlock){
			// New streamer/sonobuoy deployment

			// This timer is a hack to delay for a bit in the hopes that the addons will be loaded before the table updates
			Timer timer = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateSonobuoyTableData();
				}
				});
				timer.setRepeats(false); // Only execute once
				timer.start(); // Go go go!
		} else {
			
			System.out.println("Unknown data in DIFAR process " + o.toString());
		}
	}
	
	/**
	 * DIFAR module uses PAMGuard's ArrayManager very differently than modules that
	 * require a static or towed array. 
	 * Here we load sonobuoy deployments from the PAMGuard database in normal and 
	 * mixed-mode, so that we don't duplicate sonobuoy deployments every time PAMGuard
	 * restarts.
	*
	 * Add an annotation for sonobuoy end time
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
		buoyDataBlock = ArrayManager.getArrayManager().getStreamerDatabBlock();
		
		// Subscribe to StreamerDataBlock in order to update the SonobuoyManager
		buoyDataBlock.addObserver(this);
		
		PamConnection connection = null;
		DBControl dbControl = DBControlUnit.findDatabaseControl();
		if (dbControl == null) {
			return;
		}
		connection = dbControl.getConnection();
		if (connection == null){
			return;
		}
		
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW){
			buoyDataBlock.forceClearAll();
			int n = buoyDataBlock.getUnitsCount();
			StreamerLogging streamerLogging = (StreamerLogging) buoyDataBlock.getLogging();
			if (streamerLogging != null)
				streamerLogging.prepareForMixedMode(connection);
		}
		updateSonobuoyTableData();
	}

	void checkAndUpdateStreamer(PamArray array, int channel, Streamer newStreamerInfo, Long timeMillis) {
		// check a few things about the streamer data
		String Options[] =  {"Clear", "Keep", "Magnetic", "Cancel"};
		double magVariation = 0.0;
		try {
			GpsData streamerGps = GPSControl.getGpsControl().getShipPosition(timeMillis).getGpsData();
			magVariation = MagneticVariation.getInstance().getVariation(timeMillis,
					streamerGps.getLatitude(), streamerGps.getLongitude());
		}
		catch(NullPointerException e) {
			System.out.println("Could not obtain default magnetic variation for this sonobuoy.");
			//Problem with magnetic variation or lat/long -- shouldn't happen
		}
		Double head = newStreamerInfo.getHeading();
//		if (head != null) {
			String msg = String.format("Buoy heading is currently set to %3.1f%s. Do you want to clear it prior to calibrating the buoy?",
					head, LatLong.deg);
			msg += "\nClear (set to null) ";
			msg += String.format("\nKeep (Leave heading as %3.1f%s)",head, LatLong.deg);
			msg += String.format("\nMagnetic (Set to %3.1f%s)",magVariation, LatLong.deg);
			msg += "\nCancel to cancel the buoy deployment altogether";
//			int ans = JOptionPane.showConfirmDialog(difarControl.getGuiFrame(), msg, "Buoy Heading", JOptionPane.YES_NO_CANCEL_OPTION);
			int ans = JOptionPane.showOptionDialog(difarControl.getGuiFrame(), msg,
					"Buoy Heading", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, Options, 0 );
			if (ans == 3) {
				return;
			}
			else if (ans == 0) { // Clear
				newStreamerInfo.setHeading(null);
			} else if (ans == 2) { // Use Magnetic
				newStreamerInfo.setHeading(magVariation);
			} // No need to do anything if ans==1 (Keep)
			
//		}
		buoyDataBlock.setMixedDirection(PamDataBlock.MIX_INTODATABASE);		
		array.newStreamer(timeMillis, newStreamerInfo);
		buoyDataBlock.autoSetDataBlockMixMode();
	}

	/**
	 * Add a data annotation for the finishing time of a sonobuoy. 
	 * @param difarControl TODO
	 * @param channel
	 * @param endTime TODO
	 * @param overwrite TODO
	 */
	public void endBuoy(int channel, long endTime, boolean overwrite) {
		int channelMap = 1<<channel;
		StreamerDataUnit sdu = buoyDataBlock.getPreceedingUnit(endTime, channelMap);
		if (sdu != null){
			TimestampAnnotation existingAnnotation = (TimestampAnnotation) sdu.findDataAnnotation(TimestampAnnotation.class, sonobuoyEndTimeAnnotation.getAnnotationName());
			if (existingAnnotation==null) {
				TimestampAnnotation an = new TimestampAnnotation(sonobuoyEndTimeAnnotation);
				an.setTimestamp(endTime);
				sdu.addDataAnnotation(an);
			} else if (overwrite){
					String msg = String.format("End time for this buoy already exists. Do you want to overwrite?\n");
					msg += PamCalendar.formatDateTime2(existingAnnotation.getTimestamp()) + " - existing end time.\n";
					msg += PamCalendar.formatDateTime2(endTime) + " - proposed new end time.\n";
					int ans = JOptionPane.showConfirmDialog(difarControl.getGuiFrame(), msg, "DIFAR Buoy Warning", JOptionPane.YES_NO_OPTION);
					if (ans == JOptionPane.YES_OPTION) {
						existingAnnotation.setTimestamp(endTime);
					}// no need to do anything for NO option.
			}
			overwriteSonobuoyData(sdu);
			updateSonobuoyTableData();	
		}
	}

	/**
	 * Deploy a DIFAR buoy. In reality this means show only the streamer dialog
	 * for a single channel. 
	 * @param difarControl TODO
	 * @param channel channel number. If < 0 or if the streamer can't be found, then show the full array dialog
	 */
	public void deployBuoy(int channel) {
	
		PamArray newArray = ArrayManager.getArrayManager().getCurrentArray().clone();
		// If the array is not suitable for deploying a sonobuoy user must modify it
		if (!isArraySuitable(channel) ){
			ArrayManager.getArrayManager().setCurrentArray(newArray);
			ArrayManager.getArrayManager().showArrayDialog(difarControl.getGuiFrame());
			updateSonobuoyTableData();
			return;
		}
		
		// Auto-filling some of the deployment parameters with the current time, position, and deployment number.
		long deploymentTime = PamCalendar.getTimeInMillis();
		
		StreamerDataUnit sdu = new StreamerDataUnit(deploymentTime, 
				ArrayManager.getArrayManager().getCurrentArray().getStreamer(channel));
		sdu = setDeploymentGps(sdu);
		sdu = setDeploymentName(sdu);

		newArray.setArrayName("Sonobuoy Array: " + PamCalendar.formatDateTime(deploymentTime));
		
		// Display the streamer dialog, prefilled with the new streamer name, correct origin settings, and position
		StreamerDataUnit newStreamerInfo = SonobuoyDialog.showDialog(difarControl.getGuiFrame(), newArray, sdu, difarControl);
		
		if (newStreamerInfo != null) { //User accepted deployment 
			//End the previous deployment and create a new streamer and dataUnit.
			endBuoy(channel,deploymentTime - 1, false);
			checkAndUpdateStreamer(newArray, channel, newStreamerInfo.getStreamerData(), deploymentTime);
			updateSonobuoyTableData();		
		} 
		
	}
	
	private StreamerDataUnit setDeploymentName(StreamerDataUnit sdu) {
		String deploymentName = getNextDeploymentName(sdu.getTimeMilliseconds());
		sdu.getStreamerData().setStreamerName(deploymentName);
		return sdu;
	}

	private StreamerDataUnit setDeploymentGps(StreamerDataUnit sdu) {
		GpsData gpsData = getNewDeploymentGpsData(sdu.getTimeMilliseconds());
		StaticOriginSettings originSettings = new StaticOriginSettings();
		originSettings.setStaticPosition(sdu.getStreamerData(), gpsData);
		sdu.getStreamerData().setOriginSettings(originSettings);
		return sdu;
	}

	/**
	 *  Lookup the most recent GPS position as the default location for a deployment
	 * @param deploymentTime
	 * @return
	 */
	private GpsData getNewDeploymentGpsData(long deploymentTime) {
		GpsData gpsData = null;
		GPSDataBlock gpsBlock = ArrayManager.getGPSDataBlock();
		if (gpsBlock != null) {
			GpsDataUnit lastUnit = gpsBlock.getClosestUnitMillis(deploymentTime);
			if (lastUnit != null && lastUnit.getGpsData() != null){ 
				gpsData = new GpsData(lastUnit.getGpsData());
			} else {
				String msg = "GPS data not found."; 
				msg += "Please ensure GPS is connected and retry, or enter buoy location manually.";
				JOptionPane.showConfirmDialog(difarControl.getGuiFrame(), msg, "DIFAR Deployment", JOptionPane.OK_OPTION);
				gpsData = new GpsData(new LatLong(0, 0));
			}
		}
		return gpsData;
	}

	/**
	 * The next deployment number is incremented from all known deployments.
	 */
	private String getNextDeploymentName(long deploymentTime) {
		int highestDeploymentNumber = 0;
		for (int i = 0; i < ArrayManager.getArrayManager().getCurrentArray().getNumStreamers(); i++){
			StreamerDataUnit sdunit = buoyDataBlock.getPreceedingUnit(deploymentTime, 1<<i);
			if (sdunit == null || sdunit.getStreamerData() == null)
				continue;
	
			String streamerName = sdunit.getStreamerData().getStreamerName();
			if ( isNumeric(streamerName) ){
				int n = (int) Double.parseDouble(streamerName);
				highestDeploymentNumber = Math.max(highestDeploymentNumber, n); 
			}
		}
		return String.valueOf(highestDeploymentNumber+1);
	}

	public CalibrationHistogram getCalCorrectionHistogram(DifarControl difarControl, int channel)  {
		return difarControl.difarProcess.getCalCorrectionHistogram(channel);
	}

	public CalibrationHistogram getCalibrationHistogram(DifarControl difarControl, int channel) {
		return difarControl.difarProcess.getCalTrueBearingHistogram(channel);
	}
	
	/**
	 * Shows the calibration dialog for a particular channel. 
	 * @param difarControl TODO
	 * @param channel
	 */
	public void showCalibrationDialog(int channel) {
		CalibrationDialog.showDialog(difarControl.getGuiFrame(), difarControl, channel);
	}

	/**
	 * Check if the array is suitable for sonobuoys. At the moment, 
	 * A suitable array has one streamer per channel, one hydrophone 
	 * per streamer.
	 * @param channel
	 * @return
	 */
	boolean isArraySuitable(int channel) {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		if (array == null | channel < 0)
			return false;
		
		Streamer streamer = array.getStreamer(channel);
		if (streamer == null)
			return false;
	
		int hydrophoneBitmap = array.getPhonesForStreamer(streamer);
		if (hydrophoneBitmap != 1<<channel)
			return false;
		
		return true;
	}

	boolean isNumeric(String str){  
		  try  
		  {  
		    double d = Double.parseDouble(str);  
		  }  
		  catch(NumberFormatException nfe)  
		  {  
		    return false;  
		  }  
		  return true;  
		}
	
	/**
	 * Called after changes have been made to the sonobuoy data: 
	 * buoy deployed, buoy ended, buoy calibrated, or buoy manually edited 
	 */
	public synchronized void updateSonobuoyTableData() {

		StreamerDataBlock sdb = buoyDataBlock;
		StreamerDataUnit sdu = null;
			ListIterator<StreamerDataUnit> listIterator = sdb.getListIterator(0);
			int row = 0;
			while (listIterator.hasNext()) {
				sdu = listIterator.next();
				if (sdu.getDatabaseIndex() <= 0){
					continue;
				}
				row++;
			}
			int nRows = row;
			int nCols = columnNames.length;
			tableData = new Object[nRows][nCols];
			StreamerDataUnit prevUnit = null;
			listIterator = sdb.getListIterator(0);
			row = 0;
			while (listIterator.hasNext()) {
				sdu = listIterator.next();
				if (sdu == prevUnit){
					break;
				}
				if (sdu.getDatabaseIndex() <= 0){
					continue;
				}
				prevUnit = sdu;
				setTableData(row,sdu);

				row++;
			}
			List<RowSorter.SortKey> sortKeys = null;
			sortKeys = (List<SortKey>) difarControl.getSonobuoyManagerContainer().getSonobuoyTable().getRowSorter().getSortKeys();
			tableDataModel.setDataVector(tableData, columnNames); 
			tableDataModel.fireTableDataChanged();
			difarControl.getSonobuoyManagerContainer().getSonobuoyTable().getRowSorter().setSortKeys(sortKeys);
	}
	
	private void setTableData(int row, StreamerDataUnit sdu) {
		int col = 0;
		String endTime = null;
		
		// Extract DIFAR annotations
		TimestampAnnotation an = ((TimestampAnnotation) sdu.findDataAnnotation(TimestampAnnotation.class, sonobuoyEndTimeAnnotation.getAnnotationName()));
		if (an!=null) {
			endTime = PamCalendar.formatDBDateTime(an.getTimestamp());
		}
		
		// DIFAR action stored as an Annotation
//		String actionString = null;
//		StringAnnotation actionAnnotation = (StringAnnotation) sdu.findDataAnnotation(StringAnnotation.class, sonobuoyActionAnnotation.getAnnotationName());
//		if (actionAnnotation != null){
//			actionString = actionAnnotation.getString();
//		}
//		
//		String compassCorrection = null;
//		StringAnnotation correctionAnnotation = (StringAnnotation) sdu.findDataAnnotation(StringAnnotation.class, calMeanAnnotation.getAnnotationName());
//		if (correctionAnnotation != null){
//			compassCorrection = correctionAnnotation.getString();
//		}
//
//		String calStdDev = null;
//		StringAnnotation stdDevAnnotation = (StringAnnotation) sdu.findDataAnnotation(StringAnnotation.class, calStdDevAnnotation.getAnnotationName());
//		if (stdDevAnnotation != null){
//			calStdDev = stdDevAnnotation.getString();
//		}
		
		tableData[row][COLUMN_DATABASEID] = sdu.getUID();
		tableData[row][COLUMN_NAME] = sdu.getStreamerData().getStreamerName();
		tableData[row][COLUMN_TIMESTAMP] = PamCalendar.formatDBDateTime(sdu.getTimeMilliseconds());
		tableData[row][COLUMN_ENDTIME] =  endTime;
		tableData[row][COLUMN_CHANNEL] = PamUtils.getSingleChannel(sdu.getChannelBitmap());
		if (sdu.getGpsData()!=null){
			tableData[row][COLUMN_LATITUDE] = GpsData.formatLatitude(sdu.getGpsData().getLatitude()); 
			tableData[row][COLUMN_LONGITUDE] = GpsData.formatLongitude(sdu.getGpsData().getLongitude());
		}
		tableData[row][COLUMN_DEPTH] = sdu.getStreamerData().getZ();
		tableData[row][COLUMN_HEADING] = sdu.getStreamerData().getHeading();
//		tableData[row][COLUMN_ACTION] = null;//actionString;
//		tableData[row][COLUMN_COMPASSCORRECTION] = correctionAnnotation;
//		tableData[row][COLUMN_CALSTDDEV] = calStdDev; 
		
	}

	class SonobuoyTableModel extends DefaultTableModel {
		public SonobuoyTableModel(Object rowData[][], Object columnNames[]) {
			super(rowData, columnNames);
		}
		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		@Override
		public Class getColumnClass(int column) {
			switch (column) {
			case COLUMN_DATABASEID:
				return Integer.class;
			case COLUMN_NAME:
				return String.class;
			case COLUMN_TIMESTAMP:
				return String.class;
			default:
				return Integer.class;
			}
		}
	}

	public double getCompassCorrection(int channel, long timeMillis) {
		SnapshotGeometry geom = ArrayManager.getArrayManager().getSnapshotGeometry(1<<channel, timeMillis);
		return geom.getReferenceGPS().getHeading();
//		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
//		return currentArray.getHydrophoneLocator().getArrayHeading(timeMillis, channel);
	}

	public boolean updateCorrection(Streamer streamer, long calibrationStartTime, Double newHead, Double std, int numClips) {
		int channel = streamer.getStreamerIndex();
		streamer.setHeading(newHead);
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray(); 
		OriginSettings os = streamer.getOriginSettings();
		if (StaticOriginSettings.class.isAssignableFrom(os.getClass())) {
			StaticOriginSettings sos = (StaticOriginSettings) os;
			sos.getStaticPosition().getGpsData().setTrueHeading(newHead);
			streamer.setOriginSettings(sos);
	
			currentArray.updateStreamerAndDataUnit(calibrationStartTime, streamer);
			StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getPreceedingUnit(calibrationStartTime, 1<<channel);
			difarControl.getDifarProcess().getProcessedDifarData().clearOldOrigins(streamer.getStreamerIndex(), sdu.getTimeMilliseconds());
			difarControl.getDifarProcess().getQueuedDifarData().clearOldOrigins(streamer.getStreamerIndex(), sdu.getTimeMilliseconds());
			ArrayManager.getArrayManager().getStreamerDatabBlock().updatePamData(sdu, System.currentTimeMillis());
	
			
//			addSonobuoyAnnotation(sdu, calMeanAnnotation, String.format("%6f", newHead));
//			addSonobuoyAnnotation(sdu, calStdDevAnnotation, String.format("%6f", std));
			CalibrationDataUnit cdu = new CalibrationDataUnit(calibrationStartTime,	sdu.getUID(), newHead, std, numClips);
			difarControl.getDifarProcess().getCalibrationDataBlock().addPamData(cdu);
			difarControl.sonobuoyManager.updateSonobuoyTableData();
			return true;
		}
		else {
			return false;
		}
	}
	
	
	
}
