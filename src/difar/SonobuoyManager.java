package difar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Point;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import Array.ArrayManager;
import Array.PamArray;
import Array.SnapshotGeometry;
import Array.Streamer;
import Array.StreamerDataBlock;
import Array.StreamerDataUnit;
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
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import annotation.handler.AnnotationChoiceHandler;
import annotation.string.StringAnnotationType;
import annotation.timestamp.TimestampAnnotation;
import annotation.timestamp.TimestampAnnotationType;
import difar.calibration.CalibrationDataBlock;
import difar.calibration.CalibrationDataUnit;
import difar.calibration.CalibrationDialog;
import difar.calibration.CalibrationHistogram;
import difar.dialogs.SonobuoyDialog;
import difar.display.SonobuoyOverlayGraphics;
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

	/**
	 * Buoy positions for the map. Display only: these units are built from the
	 * sonobuoy history and are never saved.
	 */
	private PamDataBlock<SonobuoyDataUnit> buoyPositions;
	
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

	/**
	 * Whether each row's buoy is the one in force on its channel at the time
	 * being viewed.
	 */
	public boolean rowInForce [] = null;

	/** The records shown in the table, one per row. */
	private List<SonobuoyRecord> tableRecords = new ArrayList<>();
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

		buoyPositions = new PamDataBlock<SonobuoyDataUnit>(SonobuoyDataUnit.class,
				"Sonobuoy Positions", this, 0);
		buoyPositions.setOverlayDraw(new SonobuoyOverlayGraphics(difarControl));
		buoyPositions.setPamSymbolManager(new StandardSymbolManager(buoyPositions,
				SonobuoyOverlayGraphics.defaultSymbol, true));
		/*
		 * A day, so a buoy stays on the map for as long as the detections made
		 * on it. These units are rebuilt from the history, never saved.
		 */
		buoyPositions.setNaturalLifetime(24 * 3600);
		addOutputDataBlock(buoyPositions);
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
	 * buoy deployed, buoy ended, buoy calibrated, or buoy manually edited.
	 * <p>
	 * Rows come from the sonobuoy history, one per saved record, so duplicate
	 * records loaded by the viewer appear once. Records made up at startup to
	 * stand for the configured array are not saved, and are not shown.
	 */
	public synchronized void updateSonobuoyTableData() {
		SonobuoyHistory history = difarControl.getSonobuoyHistory();
		List<SonobuoyRecord> records = new ArrayList<>();
		for (SonobuoyRecord record : history.getAllRecords()) {
			if (record.isSaved()) {
				records.add(record);
			}
		}
		JTable table = difarControl.getSonobuoyManagerContainer().getSonobuoyTable();
		Long selectedUid = getSelectedUid(table);
		Point scrollPosition = null;
		if (table.getParent() instanceof JViewport) {
			scrollPosition = ((JViewport) table.getParent()).getViewPosition();
		}

		updateBuoyPositions(records);
		tableData = new Object[records.size()][columnNames.length];
		tableRecords = records;
		for (int row = 0; row < records.size(); row++) {
			setTableData(row, records.get(row));
		}
		updateRowInForce();
		List<RowSorter.SortKey> sortKeys = null;
		sortKeys = (List<SortKey>) table.getRowSorter().getSortKeys();
		tableDataModel.setDataVector(tableData, columnNames);
		tableDataModel.fireTableDataChanged();
		table.getRowSorter().setSortKeys(sortKeys);

		restoreSelection(table, selectedUid);
		if (scrollPosition != null) {
			Point position = scrollPosition;
			SwingUtilities.invokeLater(() -> {
				if (table.getParent() instanceof JViewport) {
					((JViewport) table.getParent()).setViewPosition(position);
				}
			});
		}
	}

	/**
	 * Rebuild the buoy positions shown on the map, one unit per record.
	 * @param records the records now held.
	 */
	private void updateBuoyPositions(List<SonobuoyRecord> records) {
		buoyPositions.clearAll();
		for (SonobuoyRecord record : records) {
			if (record.hasPosition()) {
				buoyPositions.addPamData(new SonobuoyDataUnit(record));
			}
		}
	}

	/**
	 * Work out which rows hold the buoy in force on their channel, at the time
	 * being viewed. In the viewer that is where the scroll bar sits, and in
	 * normal mode it is now.
	 * @return true if this differs from what the table already showed.
	 */
	private boolean updateRowInForce() {
		boolean[] inForce = new boolean[tableRecords.size()];
		SonobuoyHistory history = difarControl.getSonobuoyHistory();
		long viewTime = PamCalendar.getTimeInMillis();
		for (int row = 0; row < tableRecords.size(); row++) {
			SonobuoyRecord record = tableRecords.get(row);
			inForce[row] = record == history.getRecordAt(record.getChannel(), viewTime);
		}
		boolean changed = !Arrays.equals(inForce, rowInForce);
		rowInForce = inForce;
		return changed;
	}

	/**
	 * The time being viewed has changed, so a different buoy may be in force.
	 * Only the marking of rows changes, so the table is repainted rather than
	 * rebuilt.
	 */
	public void scrollTimeChanged() {
		if (updateRowInForce() && difarControl.getSonobuoyManagerContainer() != null) {
			difarControl.getSonobuoyManagerContainer().getSonobuoyTable().repaint();
		}
	}

	/**
	 * @param row a row of the table model.
	 * @return the channel of the buoy in that row, or -1 if not known.
	 */
	public int getRowChannel(int row) {
		if (tableData == null || row < 0 || row >= tableData.length) {
			return -1;
		}
		Object channel = tableData[row][COLUMN_CHANNEL];
		return channel instanceof Integer ? (Integer) channel : -1;
	}

	/**
	 * @param row a row of the table model.
	 * @return true if that row's buoy is the one in force on its channel.
	 */
	public boolean isRowInForce(int row) {
		return rowInForce != null && row >= 0 && row < rowInForce.length && rowInForce[row];
	}

	/**
	 * @return the UID of the record selected in the table, or null if none.
	 */
	private Long getSelectedUid(JTable table) {
		int viewRow = table.getSelectedRow();
		if (viewRow < 0 || tableData == null) {
			return null;
		}
		int row = table.convertRowIndexToModel(viewRow);
		if (row < 0 || row >= tableData.length) {
			return null;
		}
		Object uid = tableData[row][COLUMN_DATABASEID];
		return uid instanceof Long ? (Long) uid : null;
	}

	/**
	 * Select the row showing a record, so a refresh does not lose the user's place.
	 */
	private void restoreSelection(JTable table, Long uid) {
		if (uid == null) {
			return;
		}
		for (int row = 0; row < tableData.length; row++) {
			if (uid.equals(tableData[row][COLUMN_DATABASEID])) {
				int viewRow = table.convertRowIndexToView(row);
				if (viewRow >= 0) {
					table.setRowSelectionInterval(viewRow, viewRow);
				}
				return;
			}
		}
	}

	/**
	 * Fill one row of the table.
	 * @param row the row.
	 * @param record the buoy record.
	 */
	private void setTableData(int row, SonobuoyRecord record) {
		Long endTime = record.getEndTimeMillis();
		tableData[row][COLUMN_DATABASEID] = record.getUid();
		tableData[row][COLUMN_NAME] = record.getName();
		tableData[row][COLUMN_TIMESTAMP] = PamCalendar.formatDBDateTime(record.getTimeMillis());
		tableData[row][COLUMN_ENDTIME] = endTime == null ? null : PamCalendar.formatDBDateTime(endTime);
		tableData[row][COLUMN_CHANNEL] = record.getChannel();
		if (record.hasPosition()) {
			tableData[row][COLUMN_LATITUDE] = LatLong.formatLatitude(record.getLatitude());
			tableData[row][COLUMN_LONGITUDE] = LatLong.formatLongitude(record.getLongitude());
		}
		tableData[row][COLUMN_DEPTH] = record.getDepth();
		/*
		 * Shown to one decimal place. The record keeps its full precision, and
		 * the column still sorts as a number.
		 */
		Double heading = record.getHeading();
		tableData[row][COLUMN_HEADING] = heading == null ? null : Math.round(heading * 10.) / 10.;
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

	/**
	 * The compass correction of the buoy on a channel at a time.
	 * <p>
	 * Read from the buoy record in force, so a calibration starts from the
	 * heading actually in use. Falls back to the core array only when there are
	 * no buoy records at all.
	 * @param channel the channel.
	 * @param timeMillis the time.
	 * @return correction in degrees, or zero if the buoy has not been calibrated.
	 */
	public double getCompassCorrection(int channel, long timeMillis) {
		SonobuoyHistory history = difarControl.getSonobuoyHistory();
		if (history.getRecordCount() > 0) {
			SonobuoyRecord record = history.getRecordAt(channel, timeMillis);
			if (record == null || record.getHeading() == null) {
				return 0;
			}
			return record.getHeading();
		}
		SnapshotGeometry geom = ArrayManager.getArrayManager().getSnapshotGeometry(1<<channel, timeMillis);
		return geom.getReferenceGPS().getHeading();
	}

	/**
	 * Calibrate the buoy in force on a channel from a set of ship noise clips.
	 * The streamer is used only for its channel. The record in force at the
	 * start of the calibration is the one calibrated.
	 * @param streamer streamer for the channel being calibrated.
	 * @param calibrationStartTime time of the first calibration clip.
	 * @param newHead new compass correction in degrees.
	 * @param std standard deviation of the correction, in degrees.
	 * @param numClips number of clips the correction came from.
	 * @return true if a buoy record was found and calibrated.
	 */
	public boolean updateCorrection(Streamer streamer, long calibrationStartTime, Double newHead, Double std, int numClips) {
		if (newHead == null) {
			return false;
		}
		StreamerDataUnit record = findRecordUnitAt(streamer.getStreamerIndex(), calibrationStartTime);
		if (record == null) {
			return false;
		}
		if (!confirmChange(record, null)) {
			return false;
		}
		boolean done = calibrate(record, newHead, std == null ? 0 : std, numClips, calibrationStartTime);
		if (done) {
			carryChangeDownstream(record, null);
		}
		return done;
	}

	/**
	 * Set the compass correction of one buoy record, and log it as a calibration.
	 * <p>
	 * This is the one path for any change of heading, whether from ship noise
	 * clips or typed in. The heading applies to the whole deployment, so
	 * detections made before the calibration use it too. A heading typed in is
	 * logged with no clips, which marks it as not measured.
	 * @param record the buoy record to calibrate.
	 * @param heading new compass correction in degrees.
	 * @param stdDev standard deviation of the correction in degrees, zero if
	 * not measured.
	 * @param numClips number of clips the correction came from, zero if typed in.
	 * @param calibrationTime time the calibration is logged at.
	 * @return false if the record is not a static buoy, and nothing was changed.
	 */
	public boolean calibrate(StreamerDataUnit record, double heading, double stdDev, int numClips, long calibrationTime) {
		Streamer streamer = record.getStreamerData();
		if (streamer == null || !(streamer.getOriginSettings() instanceof StaticOriginSettings)) {
			return false;
		}
		streamer.setHeading(heading);
		GpsDataUnit staticPosition = ((StaticOriginSettings) streamer.getOriginSettings()).getStaticPosition();
		if (staticPosition != null) {
			staticPosition.getGpsData().setTrueHeading(heading);
		}
		saveRecord(record);
		int channel = streamer.getStreamerIndex();
		difarControl.getDifarProcess().getProcessedDifarData().clearOldOrigins(channel, record.getTimeMilliseconds());
		difarControl.getDifarProcess().getQueuedDifarData().clearOldOrigins(channel, record.getTimeMilliseconds());
		CalibrationDataUnit cdu = new CalibrationDataUnit(calibrationTime, record.getUID(), heading, stdDev, numClips);
		CalibrationDataBlock calibrations = difarControl.getDifarProcess().getCalibrationDataBlock();
		calibrations.addPamData(cdu);
		if (!calibrations.shouldNotify()) {
			/*
			 * In viewer mode a data block does not announce new units, so the
			 * calibration table would not show this one until a reload. It does
			 * listen for updates.
			 */
			calibrations.updatePamData(cdu, System.currentTimeMillis());
		}
		updateSonobuoyTableData();
		return true;
	}

	/**
	 * Apply an edit made in the sonobuoy dialog to the record it was made from.
	 * <p>
	 * Name, position, depth and times are changed on the record itself. A
	 * change of heading goes through calibrate(), so it is logged. The dialog
	 * shows the heading to one decimal place, so a heading within 0.05 degrees
	 * of the old one counts as unchanged, and keeps its full precision.
	 * @param record the buoy record that was edited.
	 * @param edited the edited copy returned by the dialog.
	 */
	public boolean applyEdit(StreamerDataUnit record, StreamerDataUnit edited) {
		Streamer oldStreamer = record.getStreamerData();
		Double oldHeading = oldStreamer == null ? null : oldStreamer.getHeading();
		Streamer newStreamer = edited.getStreamerData();
		Double newHeading = newStreamer.getHeading();
		boolean headingChanged = newHeading != null
				&& (oldHeading == null || Math.abs(newHeading - oldHeading) >= 0.05);
		if (!headingChanged) {
			newStreamer.setHeading(oldHeading);
		}
		record.setStreamerData(newStreamer);
		if (edited.getTimeMilliseconds() != record.getTimeMilliseconds()) {
			record.setTimeMilliseconds(edited.getTimeMilliseconds());
			buoyDataBlock.sortData();
		}
		TimestampAnnotation newEnd = findEndTime(edited);
		if (newEnd != null) {
			TimestampAnnotation oldEnd = findEndTime(record);
			if (oldEnd != null) {
				oldEnd.setTimestamp(newEnd.getTimestamp());
			} else {
				record.addDataAnnotation(newEnd);
			}
		}
		if (!confirmChange(record, edited)) {
			return false;
		}
		if (headingChanged) {
			calibrate(record, newHeading, 0, 0, record.getTimeMilliseconds());
		} else {
			saveRecord(record);
			updateSonobuoyTableData();
		}
		carryChangeDownstream(record, edited);
		return true;
	}

	/**
	 * Ask before a change that leaves saved triangulations describing a buoy
	 * that no longer exists as it was. Nothing is asked when nothing downstream
	 * is affected.
	 * @param record the buoy record about to change.
	 * @param edited the edited copy, or null where only the heading changes.
	 * @return true to go ahead.
	 */
	private boolean confirmChange(StreamerDataUnit record, StreamerDataUnit edited) {
		SonobuoyEditEffects effects = getEditEffects(record, edited);
		if (effects == null || !effects.isAnythingAffected()) {
			return true;
		}
		return JOptionPane.showConfirmDialog(difarControl.getGuiFrame(),
				effects.getMessage(isViewer()), "Change sonobuoy",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
	}

	/**
	 * Work out the triangulations again, or clear them, over the whole time the
	 * changed record is in force. Only in the viewer: while PAMGuard is
	 * running, data already written cannot be changed.
	 * @param record the buoy record that changed.
	 */
	private void carryChangeDownstream(StreamerDataUnit record, StreamerDataUnit edited) {
		SonobuoyEditEffects effects = getEditEffects(record, edited);
		if (effects == null || effects.getTriangulations() == 0) {
			return;
		}
		if (isViewer()) {
			difarControl.runCrossingTasks(effects.getReprocessStartTime(), effects.getEndTimeOrLatest());
		}
		else {
			updateLoadedTriangulations(effects);
		}
	}

	/**
	 * Bring the triangulations held in memory into line with a changed buoy.
	 * <p>
	 * Used while PAMGuard is running, where the offline tasks cannot be used.
	 * Detections written to file before the change keep their old
	 * triangulation until the data are reprocessed in the viewer.
	 * @param effects what the change affects.
	 */
	private void updateLoadedTriangulations(SonobuoyEditEffects effects) {
		PamDataBlock<DifarDataUnit> detections = difarControl.getDifarProcess().getProcessedDifarData();
		for (DifarDataUnit unit : detections.getDataCopy()) {
			if (unit.getDifarCrossing() == null || !effects.covers(new DifarDetection(unit))) {
				continue;
			}
			unit.setDifarCrossing(difarControl.getDifarProcess().getDifarRangeInfo(unit));
			detections.updatePamData(unit, System.currentTimeMillis());
		}
	}

	/**
	 * @param record a buoy record.
	 * @return what changing it affects, or null if it is not in the history.
	 */
	private SonobuoyEditEffects getEditEffects(StreamerDataUnit record, StreamerDataUnit edited) {
		SonobuoyHistory history = difarControl.getSonobuoyHistory();
		Streamer streamer = record.getStreamerData();
		if (streamer == null) {
			return null;
		}
		SonobuoyRecord buoy = history.getRecordAt(streamer.getStreamerIndex(),
				record.getTimeMilliseconds());
		if (buoy == null) {
			return null;
		}
		PamDataBlock<DifarDataUnit> detections = difarControl.getDifarProcess().getProcessedDifarData();
		List<SonobuoyEditEffects.Detection> loaded = new ArrayList<>();
		for (DifarDataUnit unit : detections.getDataCopy()) {
			loaded.add(new DifarDetection(unit));
		}
		long newStart = edited == null ? record.getTimeMilliseconds() : edited.getTimeMilliseconds();
		TimestampAnnotation newEnd = edited == null ? findEndTime(record) : findEndTime(edited);
		return new SonobuoyEditEffects(buoy, history, loaded,
				detections.getCurrentViewDataStart(), detections.getCurrentViewDataEnd(),
				newStart, newEnd == null ? null : newEnd.getTimestamp());
	}

	/**
	 * A DIFAR detection as the edit effects see it: its time, its channel, and
	 * whether a triangulation is saved for it.
	 */
	private static class DifarDetection implements SonobuoyEditEffects.Detection {

		private final DifarDataUnit unit;

		DifarDetection(DifarDataUnit unit) {
			this.unit = unit;
		}

		@Override
		public long getTimeMillis() {
			return unit.getTimeMilliseconds();
		}

		@Override
		public int getChannel() {
			return PamUtils.getSingleChannel(unit.getChannelBitmap());
		}

		@Override
		public boolean hasTriangulation() {
			return unit.getDifarCrossing() != null;
		}
	}

	/**
	 * @return true in viewer mode.
	 */
	private boolean isViewer() {
		return PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
	}

	/**
	 * Save a changed buoy record. The record's cached position is cleared,
	 * since its streamer may have moved.
	 * <p>
	 * In normal mode, a change to the latest record on a channel is passed on to
	 * the array, since that is the buoy in the water now. In the viewer there is
	 * no single present time, so the array is left alone: saving a record must
	 * never change which buoy the array shows as current.
	 */
	private void saveRecord(StreamerDataUnit record) {
		record.setGpsData(null);
		Streamer streamer = record.getStreamerData();
		int channel = streamer.getStreamerIndex();
		if (!isViewer() && buoyDataBlock.getLastUnit(1 << channel) == record) {
			ArrayManager.getArrayManager().getCurrentArray().updateStreamer(channel, streamer);
		}
		buoyDataBlock.updatePamData(record, System.currentTimeMillis());
	}

	/**
	 * @return the end time annotation of a buoy record, or null if it has none.
	 */
	private TimestampAnnotation findEndTime(StreamerDataUnit record) {
		return (TimestampAnnotation) record.findDataAnnotation(TimestampAnnotation.class,
				sonobuoyEndTimeAnnotation.getAnnotationName());
	}

	/**
	 * @param uid unique identifier of a buoy record.
	 * @return the stored buoy record with this UID, or null if it is not loaded.
	 */
	public StreamerDataUnit findRecordUnit(long uid) {
		for (StreamerDataUnit unit : buoyDataBlock.getDataCopy()) {
			if (unit.getUID() == uid) {
				return unit;
			}
		}
		return null;
	}

	/**
	 * @param channel the channel.
	 * @param timeMillis the time.
	 * @return the stored buoy record in force on a channel at a time, or null.
	 */
	public StreamerDataUnit findRecordUnitAt(int channel, long timeMillis) {
		SonobuoyRecord record = difarControl.getSonobuoyHistory().getRecordAt(channel, timeMillis);
		if (record == null || record.getUid() == null) {
			return null;
		}
		return findRecordUnit(record.getUid());
	}

}
