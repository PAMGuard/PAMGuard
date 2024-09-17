package Array;

import java.sql.Types;

import Array.sensors.ArrayParameterType;
import Array.sensors.ArraySensorFieldType;
import Array.streamerOrigin.HydrophoneOriginMethod;
import Array.streamerOrigin.HydrophoneOriginMethods;
import Array.streamerOrigin.HydrophoneOriginSystem;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class StreamerLogging extends SQLLogging {

	private HydrophoneProcess hydrophoneProcess;

	private PamTableDefinition streamerTable;

	private PamTableItem coord[] = new PamTableItem[3];
	private PamTableItem coordErr[] = new PamTableItem[3];
	private PamTableItem name, streamerIndex, locator, origin;
	private PamTableItem heading, pitch, roll;

	private int basicItemCount;

	protected StreamerLogging(HydrophoneProcess hydrophoneProcess, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.hydrophoneProcess = hydrophoneProcess;
		streamerTable = new PamTableDefinition("HydrophoneStreamers", UPDATE_POLICY_WRITENEW);
		streamerTable.addTableItem(streamerIndex = new PamTableItem("StreamerIndex", Types.INTEGER));
		streamerTable.addTableItem(name = new PamTableItem("Name", Types.CHAR, 50));
		streamerTable.addTableItem(locator = new PamTableItem("Locator", Types.CHAR, 50));
		streamerTable.addTableItem(origin = new PamTableItem("Origin", Types.CHAR, 50));
		streamerTable.addTableItem(coord[0] = new PamTableItem("xPos", Types.DOUBLE));
		streamerTable.addTableItem(coord[1] = new PamTableItem("yPos", Types.DOUBLE));
		streamerTable.addTableItem(coord[2] = new PamTableItem("zPos", Types.DOUBLE));
		streamerTable.addTableItem(coordErr[0] = new PamTableItem("xErr", Types.DOUBLE));
		streamerTable.addTableItem(coordErr[1] = new PamTableItem("yErr", Types.DOUBLE));
		streamerTable.addTableItem(coordErr[2] = new PamTableItem("zErr", Types.DOUBLE));
		streamerTable.addTableItem(heading = new PamTableItem("Heading", Types.DOUBLE));
		streamerTable.addTableItem(pitch = new PamTableItem("Pitch", Types.DOUBLE));
		streamerTable.addTableItem(roll = new PamTableItem("Roll", Types.DOUBLE));

		basicItemCount = streamerTable.getTableItemCount();

		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			HydrophoneOriginSystem aMethod = HydrophoneOriginMethods.getInstance().getMethod(i);
			aMethod.createSQLLoggingFields(streamerTable);
		}

		setTableDefinition(streamerTable);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		StreamerDataUnit sdu = (StreamerDataUnit) pamDataUnit;
		Streamer s = sdu.getStreamerData();
		streamerIndex.setValue(s.getStreamerIndex());
		name.setValue(s.getStreamerName());
		locator.setValue(s.getHydrophoneLocator().getName());
		origin.setValue(s.getHydrophoneOrigin().getName());
		double[] c = s.getCoordinates();
		double[] cErr = s.getCoordinateError();
		for (int i = 0; i < 3; i++) {
			coord[i].setValue(c[i]);
			coordErr[i].setValue(cErr[i]);
		}
//		if (s.isEnableOrientation()) {
			heading.setValue(s.getHeading());
			pitch.setValue(s.getPitch());
			roll.setValue(s.getRoll());
//		}
//		else {
//			heading.setValue(null);
//			pitch.setValue(null);
//			roll.setValue(null);
//		}

		// set all the extra ones to null before filling. 
		int itemCount = streamerTable.getTableItemCount();
		for (int i = basicItemCount; i < itemCount; i++) {
			streamerTable.getTableItem(i).setValue(null);
		}

		// then fill with data from the origin data. 
		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			HydrophoneOriginSystem aMethod = HydrophoneOriginMethods.getInstance().getMethod(i);
			if (aMethod.getMethodClass() == s.getHydrophoneOrigin().getClass()) {
				aMethod.fillSQLLoggingFields(sdu);
			}
		}

		// should probably do something similar for locator information if we ever start using options in the locators. 

	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		int streamerIndex = this.streamerIndex.getIntegerValue();
		String streamerName = this.name.getStringValue();
		String locatorName = locator.getStringValue();
		String originName = origin.getStringValue();
		double[] coordinates = new double[3];
		double[] coordinateErrors = new double[3];
		for (int i = 0; i < 3; i++) {
			coordinates[i] = this.coord[i].getDoubleValue();
			coordinateErrors[i] = this.coordErr[i].getDoubleValue();
		}
		Streamer streamer = new Streamer(streamerIndex);

		streamer.setStreamerName(streamerName);
		streamer.setCoordinates(coordinates);
		streamer.setCoordinateErrors(coordinateErrors);
		HydrophoneOriginMethod originMethod = HydrophoneOriginMethods.getInstance().getMethod(originName, currentArray, streamer);
		streamer.setHydrophoneOrigin(originMethod);
		streamer.setOriginSettings(originMethod.getOriginSettings());
		HydrophoneLocator originLocator = HydrophoneLocators.getInstance().getLocatorSystem(locatorName).getLocator(currentArray, streamer);
		streamer.setHydrophoneLocator(originLocator);
		streamer.setLocatorSettings(originLocator.getLocatorSettings());
		Double head, pitch, roll;
		head = (Double) this.heading.getValue();
		pitch = (Double) this.pitch.getValue();
		roll = (Double) this.roll.getValue();
		streamer.setHeading(head);
		streamer.setPitch(pitch);
		streamer.setRoll(roll);
		
		
		/**
		 * Enable orientation is not saved in the database. It is handy to switch between enabling and disabling orientation for all streamers. Therefore we use the default streamer
		 * in the array manager to determine if our saved streamers will use their orientation data or not. 
		 */
		Streamer defaultStreamer=ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamer.getStreamerIndex());
//		if (defualtStreamer!=null) streamer.setEnableOrientation(ArrayManager.getArrayManager().getCurrentArray().getStreamer(streamer.getStreamerIndex()).isEnableOrientation());
//		else streamer.setEnableOrientation(true);
		if (defaultStreamer != null) {
			ArraySensorFieldType[] fieldTypes = ArraySensorFieldType.values();
			for (int i = 0; i < fieldTypes.length; i++) {
				ArrayParameterType aType = defaultStreamer.getOrientationTypes(fieldTypes[i]);
				streamer.setOrientationTypes(fieldTypes[i], aType);
			}
		}
		
		StreamerDataUnit sdu = new StreamerDataUnit(timeMilliseconds, streamer);
		sdu.setDatabaseIndex(databaseIndex);
		
		int n = HydrophoneOriginMethods.getInstance().getCount();
		for (int i = 0; i < n; i++) {
			HydrophoneOriginSystem aMethod = HydrophoneOriginMethods.getInstance().getMethod(i);
			if (aMethod.getMethodClass() == streamer.getHydrophoneOrigin().getClass()) {
 				aMethod.extractSQLLoggingFields(sdu);
			}
		}
	
//		if (streamerIndex==1) System.out.println("CreateStreamerDataUnit   " + streamer.getStreamerIndex()); 
//		System.out.println("Enabled: "+sdu.getStreamerData().isEnableOrientation());
	
		getPamDataBlock().addPamData(sdu);
		
		/**
		 * In addition to adding a streamer data unit, Also need to update the 
		 * default streamer as well as the current array.
		 */
		long deploymentTime = sdu.getTimeMilliseconds();
		String newArrayName = "Array: " + PamCalendar.formatDateTime(deploymentTime);
		streamer = sdu.getStreamerData();
		currentArray.updateStreamer(streamer.getStreamerIndex(), streamer);
		currentArray.setArrayName(newArrayName);
		ArrayManager.getArrayManager().setCurrentArray(currentArray);
		
		return sdu;
	}

	/**
	 * Clear the existing streamers since they will have timestamps from 
	 * the start of mixed-mode rather than the start of the data.
	 * Then try to load the streamers from the database. But check and make sure that 
	 * there is at least one streamerDataUnit for each of the streamers. If there is
	 * no prior streamerDataUnit in the database, then create a new default one with 
	 * the timestamp from the start of the mixed-mode data. 
	 */
	@Override
	public boolean prepareForMixedMode(PamConnection con) {
		// Remove the default streamerDataUnits that were created when PAMGuard started
		hydrophoneProcess.getStreamerDataBlock().clearAll();

		// Attempt to load the streamerDataUnits from the database (typical mixed-mode behaviour)
		boolean prepared = super.prepareForMixedMode(con);
		
		// Make sure that each streamer has a dataunit with the correct timestamp.
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		if (currentArray == null) {
			return false;
		}
		int n = currentArray.getNumStreamers();
		StreamerDataBlock sdb = ArrayManager.getArrayManager().getStreamerDatabBlock();
		StreamerDataUnit sdu;
		long timeNow = PamCalendar.getTimeInMillis();
		for (int i = 0; i < n; i++) {
			
			sdu = sdb.getPreceedingUnit(timeNow, 1<<i);
			if (sdu == null){
				Streamer s = currentArray.getStreamer(i);
				sdu = new StreamerDataUnit(timeNow, s);	
				sdb.addPamData(sdu);
				sdu.setDatabaseIndex(-1);
			}
		}
		return prepared;
	}

}
