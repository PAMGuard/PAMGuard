package GPS;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import warnings.PamWarning;
import warnings.WarningSystem;
import NMEA.NMEADataBlock;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.positionreference.PositionReference;
import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;

public class GPSControl extends PamControlledUnit implements PamSettings, PositionReference {

	protected GPSParameters gpsParameters = new GPSParameters();
	
	protected NMEADataBlock nmeaDataBlock;
	boolean doAutoClockUpdate;
	
	/**
	 * There can only be one GPS control for the ship, so make 
	 * it easy to find.  
	 */
	private static GPSControl gpsControl;
	
	private ProcessNmeaData gpsProcess;
	
	protected ProcessHeadingData headingProcess;
	
	//viewer functionality;
	private ImportGPSData importGPSData;
	ImportGPSParams gpsImportParams= new ImportGPSParams(); 
	
	
	public static final String gpsUnitType = "GPS Acquisition";
	
	private PamWarning gpsTimeWarning;
	
	private boolean isGpsMaster = false;
	
	private GPSDataMatcher gpsDataMatcher;
	
	/**
	 * Do some mucking about with dataTableNames in order to allow > 1 GPS 
	 * module, for viewer mode when collecting ancillary data. 
	 */
	
	public GPSControl(String unitName) {
		super(gpsUnitType, unitName);

		gpsTimeWarning = new PamWarning(unitName, "GPSClock Warning", 2);
		gpsTimeWarning.setWarningTip("<html>You should set your PC clock to be the correct UTC time.<br>"
				+ "Note that if you set the correct local time and the correct time zone, UTC should be correct !</html>");
		
		PamControlledUnit existingUnit = PamController.getInstance().findControlledUnit(gpsUnitType);
		if (existingUnit == null || existingUnit == this) {
			isGpsMaster = true;
		}
		
		if (gpsControl == null) gpsControl = this;

		addPamProcess(gpsProcess = new ProcessNmeaData(this));
		addPamProcess(headingProcess = new ProcessHeadingData(this));
		PamSettingManager.getInstance().registerSettings(this);
		gpsProcess.noteNewSettings();
		headingProcess.noteNewSettings();
				
		if (super.isViewer){
			
			importGPSData=new ImportGPSData(this);
			gpsImportParams=new ImportGPSParams();
			
			gpsDataMatcher = new GPSDataMatcher(this);
		}
	
	}
	/**
	 * Work out if this is the only GPS module or not. If it isn't then 
	 * it will have to have a different GPS Data table name and also won't
	 * be connecting to the NMEA data source. 
	 * @return 
	 */
	public String getDataTableName() {
		if (isGpsMaster) {
			return GpsLogger.defaultDataTableName;
		}
		else {
			return getUnitName();
		}
	}
	NMEADataBlock getNMEADataBlock() {
		return nmeaDataBlock;
	}

	public GPSDataBlock getGpsDataBlock() {
		return gpsProcess.getGpsDataBlock();
	}

	public JMenuItem createGPSMenu(Frame parentFrame) {
		JMenuItem menuItem;
		
		JMenu subMenu = new JMenu(getUnitName());
		
		menuItem = new JMenuItem("GPS Options ...");
		menuItem.addActionListener(new GpsOptions(parentFrame));
		subMenu.add(menuItem);
		
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			menuItem = new JMenuItem("Import GPS Data ...");
			menuItem.addActionListener(new ImportGPSDataDialog());
			subMenu.add(menuItem);
		}
		
		menuItem = new JMenuItem("Update PC Clock ...");
		menuItem.addActionListener(new UpdateClock(parentFrame));
		subMenu.add(menuItem);
		

		return subMenu;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return createGPSMenu(parentFrame);
	}
	
	class ImportGPSDataDialog implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String currentPath;
			ImportGPSParams newParams=ImportGPSDialog.showDialog(PamController.getInstance().getMainFrame(), PamController.getInstance().getMainFrame().getMousePosition(),gpsImportParams, importGPSData);
			
			if (newParams!=null) gpsImportParams=newParams.clone();
			
			if (newParams.path.size()==0) {
				currentPath=null;
				return; 
			}
			else{
				currentPath=newParams.path.get(0);
				importGPSData.loadFile(currentPath);
			}

		}
		
	}

	class GpsOptions implements ActionListener {
		Frame parentFrame;
		
		public GpsOptions(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {
			GPSParameters newP = GPSParametersDialog.showDialog(parentFrame, gpsParameters);
			if (newP != null) {
				gpsParameters = newP.clone();
				gpsProcess.noteNewSettings();
				headingProcess.noteNewSettings();
			}
		}
	}
	class UpdateClock implements ActionListener {
		Frame parentFrame;
		
		public UpdateClock(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {
			String message = "<html>Clock management is now controlled from the \"File/Global time settings\" menu<p>" +
		"The Global time settings do not require administrator privilidges<p><p>" +
					"Press OK to proceed anyway to old GPS clock setting method</html>";
			int ans = WarnOnce.showWarning(parentFrame, "System clock updating", message, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.OK_OPTION) {
				GPSParameters newP = UpdateClockDialog.showDialog(parentFrame, gpsControl, gpsParameters, false);
				gpsParameters = newP.clone();
			}
		}
	}
	public Serializable getSettingsReference() {
		return gpsParameters;
	}
	public long getSettingsVersion() {
		return GPSParameters.serialVersionUID;
	}
	
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		if (pamControlledUnitSettings.getUnitType().equals(this.getUnitType())
			&& pamControlledUnitSettings.getVersionNo() == GPSParameters.serialVersionUID) {
			this.gpsParameters = ((GPSParameters) pamControlledUnitSettings.getSettings()).clone();
		}
		
		doAutoClockUpdate = gpsParameters.setClockOnStartup;
		
		
		return true;
	}
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
//		gpsProcess.noteNewSettings(); removed 11/01/2018 because getting called whenever new GPS data unit was added; overrode notifyModelChanged in ProcessNmeaData instead
//		headingProcess.noteNewSettings(); removed 11/01/2018 because getting called whenever new GPS data unit was added; overrode notifyModelChanged in ProcessHeadingData instead
	}
	public GPSParameters getGpsParameters() {
		return gpsParameters;
	}
	
	/**
	 * There is only one GPS controller in the model, 
	 * so might as well make it easy to find with a static function. 
	 * @return the gpsControl.  
	 */
	public static GPSControl getGpsControl() {
		return gpsControl;
	}
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#removeUnit()
	 */
	@Override
	public boolean removeUnit() {
		if (this == gpsControl) {
			gpsControl = null;
		}
		return super.removeUnit();
	}
	
	public GpsDataUnit getShipPosition(long timeMilliseconds) {
		return getGpsDataBlock().getClosestUnitMillis(timeMilliseconds);
	}
	/**
	 * Do we want this string ? It will be either RMC or GGA and may want wildcarding
	 * @param stringId
	 * @return
	 */
	public boolean wantString(String stringId) {
		if (stringId == null || stringId.length() < 6) {
			return false;
		}
		if (gpsControl.gpsParameters.allowWildcard) {
			String lastBit = stringId.substring(3, 6);
			switch (gpsControl.gpsParameters.mainString) {
			case GPSParameters.READ_RMC:
				return lastBit.equals("RMC");
			case GPSParameters.READ_GGA:
				return lastBit.equals("GGA");
			default:
				return false;
			}
		}
		else {
			String wantedString = gpsControl.getWantedString();
			return stringId.equals(wantedString);
		}
	}
	
	/**
	 * Get the name of the string we're wanting. 
	 * @return
	 */
	public String getWantedString() {
		if (gpsParameters.mainString == GPSParameters.READ_GGA) {
			return String.format("$%sGGA", gpsParameters.ggaInitials);
		}
		else {
			return String.format("$%sRMC", gpsParameters.rmcInitials);
		}
	}
	
	/**
	 * Check the time of a new GPS Data unit and see if there is a timing problem. 
	 * @param newUnit
	 */
	public void checkGPSTime(GpsDataUnit newUnit) {
//		String str = String.format("GPS millis %s GPS time %s, diff %d", PamCalendar.formatDateTime(newUnit.getTimeMilliseconds()),
//				PamCalendar.formatDateTime(newUnit.getGpsData().getTimeInMillis()),  newUnit.getGpsData().getTimeInMillis()-newUnit.getTimeMilliseconds());
//		System.out.println(str);
		long tDiff = (newUnit.getGpsData().getTimeInMillis()-newUnit.getTimeMilliseconds())/1000;
		long aDiff = Math.abs(tDiff);
		int warnLev = 0;
		if (aDiff > 60) warnLev = 2;
		else if (aDiff > 10) warnLev = 1;
		gpsTimeWarning.setWarnignLevel(warnLev);
		if (tDiff > 10) {
			gpsTimeWarning.setWarningMessage(String.format("The PC clock is %d seconds behind the GPS clock", aDiff));
			WarningSystem.getWarningSystem().addWarning(gpsTimeWarning);
		}
		else if (tDiff < -10) {
			gpsTimeWarning.setWarningMessage(String.format("The PC clock is %d seconds ahead of the GPS clock", aDiff));
			WarningSystem.getWarningSystem().addWarning(gpsTimeWarning);
		}
		else {
			WarningSystem.getWarningSystem().removeWarning(gpsTimeWarning);
		}
	}
	/**
	 * @return the isGpsMaster
	 */
	public boolean isGpsMaster() {
		return isGpsMaster;
	}
	/**
	 * @return the gpsDataMatcher
	 */
	public GPSDataMatcher getGpsDataMatcher() {
		return gpsDataMatcher;
	}
	@Override
	public GpsData getReferencePosition(long timeMillis) {
		GpsDataUnit gpDU = getShipPosition(timeMillis);
		if (gpDU == null) {
			return null;
		}
		else {
			return gpDU.getGpsData();
		}
	}
	@Override
	public String getReferenceName() {
		return getUnitName();
	}
}
