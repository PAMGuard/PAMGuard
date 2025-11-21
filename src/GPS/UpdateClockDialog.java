package GPS;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.AdminTools;
import PamUtils.PamCalendar;
import PamUtils.PlatformInfo;
import PamUtils.SystemTiming;
import PamUtils.PlatformInfo.OSType;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import warnings.PamWarning;
import warnings.WarningSystem;

/**
 * Dialog to update the PC clock from GPRMC data.
 * <p> can be used in manual mode or automatic mode.
 * In automatic mode, it automatically updates the clock and
 * then closes itself a few seconds later.
 * In manual mode, the user must press the Set button and then press
 * the close button. 
 * @author Doug Gillespie
 *
 */
public class UpdateClockDialog extends PamDialog implements ActionListener, ClockUpdateObserver {

	private static final long serialVersionUID = 1L;

	private static UpdateClockDialog singleInstance;
	
	private JTextField pcTime, gpsTime, updatedAt;
	
	private JButton setButton, cancelButton;
	
	private JCheckBox setAlways;
	
	private GPSParameters gpsParameters;
	
	private GPSControl gpsControl;
	
	private ProcessNmeaData gpsProcess;
	
	private long lastUpdate;
	
//	private boolean setOnNextString;
//	
//	private Calendar nmeaCalendar;
	
	private static int clockSets;
	
	private boolean autoUpdate;
	
//	private int newDataCount = 0;
	
	private UpdateClockDialog(Frame parentFrame, GPSControl gpsControl) {
		super(parentFrame, "Set PC Clock", false);
		this.gpsControl = gpsControl;
		this.gpsProcess = gpsControl.getGpsProcess();
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
//		p.setBorder(new EmptyBorder(10,10,5,10));
		
		int textLen = 15;
		
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
		
		JPanel pcTimePanel = new JPanel();
		pcTimePanel.setLayout(new BorderLayout());
//		pcTimePanel.setBorder(new EmptyBorder(4,4,4,4));
		pcTimePanel.setBorder(new TitledBorder("Current PC System Time"));
		pcTimePanel.add(pcTime = new JTextField(textLen));
		
		JPanel gpsTimePanel = new JPanel();
		gpsTimePanel.setLayout(new BorderLayout());
		gpsTimePanel.setBorder(new TitledBorder("Current GPS Time"));
		gpsTimePanel.add(gpsTime = new JTextField(textLen), BorderLayout.NORTH);
		gpsTimePanel.add(updatedAt = new JTextField(textLen), BorderLayout.SOUTH);
//		gpsTime.setBorder(new EmptyBorder(4,4,4,4));
		
		pcTime.setEditable(false);
		gpsTime.setEditable(false);
		updatedAt.setEditable(false);
		
		timePanel.add(pcTimePanel);
		timePanel.add(gpsTimePanel);
		
		timePanel.add(setAlways = new JCheckBox("Auto set on Pamguard start-up"));
		
//		JPanel okPanel = new JPanel();
//		okPanel.add(setButton = new JButton(" Set Now "));
//		okPanel.setEnabled(false);
//		getRootPane().setDefaultButton(setButton);
//		okPanel.add(cancelButton = new JButton("Close"));
//		//getContentPane().add(BorderLayout.SOUTH, okPanel);
//		cancelButton.addActionListener(this);
		setButton = getOkButton();
		setButton.setText("Set Now");
		cancelButton = getCancelButton();
		setButton.addActionListener(this);
		cancelButton.setText("Close");
		p.add(BorderLayout.CENTER, timePanel);
//		p.add(BorderLayout.SOUTH, okPanel);
		
//		setContentPane(p);
		setDialogComponent(p);
		
		setHelpPoint("mapping.NMEA.docs.ClockOptions");

		pack();
//		setLocation(300, 200);
		this.setModal(true);
		this.setResizable(false);
		//this.setAlwaysOnTop(true);

	}
	/**
	 * Show the UpdateClock dialog
	 * @param parentFrame parent Frame
	 * @param gpsControl  GPS Controller
	 * @param gpsParameters nmea control parameters
	 * @param autoUpdate auto or manual update mode
	 * @return updated NMEA Parameters, or null if the cancel button was pressed
	 */
	static public GPSParameters showDialog(Frame parentFrame, GPSControl gpsControl, GPSParameters gpsParameters, boolean autoUpdate) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.gpsControl != gpsControl) {
			singleInstance = new UpdateClockDialog(parentFrame, gpsControl);
		}
		
		if (!AdminTools.isAdmin()) {
			JOptionPane.showMessageDialog(singleInstance, "The PC Clock can only be set automatically if you run PAMGuard as Administrator", 
					"Warning", JOptionPane.ERROR_MESSAGE);
		}

		singleInstance.gpsParameters = gpsParameters;//.clone();
		singleInstance.autoUpdate = autoUpdate;
//		singleInstance.setModal(!autoUpdate); // bodge it so it doens't halt program execution during auto update
//		singleInstance.setAlwaysOnTop(autoUpdate);
//		singleInstance.newDataCount = 0;
		singleInstance.setVisible(true);
		return singleInstance.gpsParameters;
	}
	private void setParams() {
		
	}
	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setVisible(boolean b) {
		NMEADataBlock nmeaDataBlock = gpsControl.getNMEADataBlock();
		if (b) {
			tellPCTime();
			timer.start();
			gpsProcess.getGpsClockUpdater().addObserver(this);
			setButton.setEnabled(false);
			lastUpdate = 0;
			lastSentGPSTime = 0;
			closeDown = -1;
//			setOnNextString = false;
			setAlways.setSelected(gpsParameters.setClockOnStartup);
			if (autoUpdate) {
				gpsProcess.getGpsClockUpdater().updateOnNext();
			}
			updatedAt.setText("Waiting Update ... ");
			
//			if (nmeaDataBlock != null) {
//				nmeaDataBlock.addObserver(this);
//			}
		}
		else { 
			timer.stop();
			gpsParameters.setClockOnStartup = setAlways.isSelected();
			gpsProcess.getGpsClockUpdater().removeObserver(this);
//			if (nmeaDataBlock != null) {
//				nmeaDataBlock.deleteObserver(this);
//			}
		}
		super.setVisible(b);
	}

	Timer timer = new Timer(1000, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent evt) {
			if (System.currentTimeMillis() - lastSentGPSTime > 1000) {
				tellPCTime();
			}
		}
	});

	private int closeDown;

	private long lastSentGPSTime;

	private void tellPCTime() {
		tellTime(pcTime, System.currentTimeMillis());
	}
	
	private void tellTime(JTextField field, long millis) {
		if (millis < 0) {
			field.setText("Invalid time");
		}
		else {
			field.setText(PamCalendar.formatDBDateTime(millis, true));
		}
	}
	private void tellTime(JTextField field, long millis, long offset) {
		if (millis < 0) {
			field.setText("Invalid time");
		}
		else {
			String str = String.format("%s (offset %s)", PamCalendar.formatDBDateTime(millis, true), PamCalendar.formatDuration(offset));
			field.setText(str);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			this.setVisible(false);

		} else if (e.getSource() == setButton) {
//			setOnNextString = true;
			gpsProcess.getGpsClockUpdater().updateOnNext();
//			setButton.setEnabled(false);
		}
		
	}

//	@Override
//	public PamObserver getObserverObject() {
//		return this;
//	}
//
//	@Override
//	public String getObserverName() {
//		return "Update Clock dialog";
//	}
//
//	@Override
//	public long getRequiredDataHistory(PamObservable o, Object arg) {
//		return 0;
//	}
//
//	@Override
//	public void noteNewSettings() {
//	}
//
//	@Override
//	public void setSampleRate(float sampleRate, boolean notify) {		
//	}
//
//	@Override
//	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void receiveSourceNotification(int type, Object object) {
//		// don't do anything by default
//	}
//
//	@Override
//	public void addData(PamObservable o, PamDataUnit arg) {
//		// look to see if it's an RMC String and if so get the date and time out.
////		NMEADataBlock nmeaDataBlock = (NMEADataBlock) o;
//		NMEADataUnit nmeaDataUnit = (NMEADataUnit) arg;
//		StringBuffer nmeaData = nmeaDataUnit.getCharData();
//		String stringId = NMEADataBlock.getSubString(nmeaData, 0);
//		if (!gpsControl.wantString(stringId)) {
//			return;
//		}
//		if (gpsControl.gpsParameters.mainString == GPSParameters.READ_GGA) {
//			newGGAData(nmeaData);
//		}else {
//			newRMCData(nmeaData);
//		}
//	}
//	
//	
//	@Override
//	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
//		// TODO Auto-generated method stub
//		
//	}
//	private void newGGAData(StringBuffer nmeaData) {
//		String date, time;
//		try {
//			time = NMEADataBlock.getSubString(nmeaData, 1);
//		}
//		catch (Exception Ex) {
//			return;
//		}
//		
//		newDataCount++;
//		if (time == null || time.length() < 6){
//			gpsTime.setText("Invalid time "  + time);
//			return;
//		}
//		int hour, minute;
//		double second;
//		try {
//		  hour = Integer.valueOf(time.substring(0,2));
//		  minute = Integer.valueOf(time.substring(2,4));
//		  second = Double.valueOf(time.substring(4)); // should pick up any decimal seconds. 
//		}
//		catch (Exception Ex) {
//			return;
//		}
//		/*
//		 * Some strings have a decimal number of seconds. Try getting this and adding 
//		 * some millis. 
//		 */
//		int millis = 0;
//		double allTime = 0;
//		try {
//			allTime = Double.valueOf(time);
//		}
//		catch (NumberFormatException e) {
//			allTime = 0;
//		}
//		millis = (int) (allTime*1000);
//		millis = millis%1000;
//		
//		nmeaCalendar = Calendar.getInstance();
//		nmeaCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
//		Date currTime = nmeaCalendar.getTime();
//		int year = currTime.getYear();
//		// fudge to last the next 85 years !
//		while (year < 2000) {
//			year += 100;
//		}
//		nmeaCalendar.set(year, currTime.getMonth(), currTime.getDate(), hour, minute, (int) second);
//		gpsTime.setText(PamCalendar.formatDateTime(nmeaCalendar.getTimeInMillis()));
//		tellTime(); // so they move together.
//		long timeMillis = nmeaCalendar.getTimeInMillis();
//		
//
//		if (setOnNextString) {
//			// go ahead and update the clock.
////			System.setProperties()
////			Calendar.getInstance().
//			setTimeNow(nmeaCalendar.getTimeInMillis());
//			setButton.setEnabled(false);
//			setOnNextString = false;
//		}
//		else {
//			setButton.setEnabled(AdminTools.isAdmin());
//		}
//		
//		if (autoUpdate) {
//			if (newDataCount == 2) {
//				setOnNextString = AdminTools.isAdmin();
//			}
//			else if (newDataCount == 5) {
//				setVisible(false);
//			}
//		}
//		
//	}
//	private void  newRMCData(StringBuffer nmeaData) {
//		String date, time;
//		try {
//			date = NMEADataBlock.getSubString(nmeaData, 9);
//			time = NMEADataBlock.getSubString(nmeaData, 1);
//		}
//		catch (Exception Ex) {
//			return;
//		}
//		
//		newDataCount++;
//		
//		if (date == null || time == null || date.length() < 6 || time.length() < 6){
//			gpsTime.setText("Invalid date time " + date + " " + time);
//			return;
//		}
//		int day, month, year, hour, minute;
//		double second;
//		try {
//		  day = Integer.valueOf(date.substring(0,2));
//		  month = Integer.valueOf(date.substring(2,4));
//		  year = Integer.valueOf(date.substring(4)); // should cope with four digit year.
//		  if (year < 2000) year += 2000; 
//		  hour = Integer.valueOf(time.substring(0,2));
//		  minute = Integer.valueOf(time.substring(2,4));
//		  second = Double.valueOf(time.substring(4)); // shoud pick up any decimal seconds. 
//		}
//		catch (Exception Ex) {
//			return;
//		}
//		nmeaCalendar = Calendar.getInstance();
//		nmeaCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
//		nmeaCalendar.set(year, month-1, day, hour, minute, (int) second);
//		gpsTime.setText(PamCalendar.formatDateTime(nmeaCalendar.getTimeInMillis()));
//		tellTime(); // so they move together.
//		
//		
//		if (setOnNextString) {
//			// go ahead and update the clock.
////			System.setProperties()
////			Calendar.getInstance().
//			setTimeNow(nmeaCalendar.getTimeInMillis());
//			setButton.setEnabled(false);
//			setOnNextString = false;
//		}
//		else {
//			setButton.setEnabled(AdminTools.isAdmin());
//		}
//		
//		if (autoUpdate) {
//			if (newDataCount == 2) {
//				setOnNextString = true;
//			}
//			else if (newDataCount == 5) {
//				setVisible(false);
//			}
//		}
//	}
	

//	private void setTimeNow(long nmeaCalendar) {
//		if (!SystemTiming.setSystemTime(nmeaCalendar)) {
//			String msg =  "The PC Clock can only be set automatically if you run PAMGuard as Administrator";
//			JOptionPane.showMessageDialog(singleInstance, msg, 
//					"Warning", JOptionPane.ERROR_MESSAGE);
//			System.out.println("Update system clock failed");
//			PamWarning newWarning = new PamWarning("GPS Clock Setting", msg, 2);
//			WarningSystem.getWarningSystem().addWarning(newWarning);
//		}
//		else {
//			System.out.println(String.format("PC Clock updated to " + PamCalendar.formatDateTime(nmeaCalendar)));
//		}
//	}
//	@Override
//	public void removeObservable(PamObservable o) {
//		// TODO Auto-generated method stub
//		
//	}

	public static int getClockSets() {
		return clockSets;
	}
	
	@Override
	public void clockUpdated(boolean success, long timeMillis, String message) {
		if (success) {
			updatedAt.setText(String.format("Updated %s", PamCalendar.formatDBDateTime(timeMillis, false)));
			if (autoUpdate) {
				closeDown = 4;
			}
			lastUpdate = timeMillis;
			autoUpdate = false;
		}
		else {
			updatedAt.setText("Update failed");
			showFailWarning();
		}
	}
	
	private void showFailWarning() {
		String tit = "Clock update failed";
		String msg;
		if (PlatformInfo.calculateOS() == OSType.WINDOWS) {
			msg = "<html>To update the PC clock, you must make adjustments to the Windows security settings<br>" +
			"See the PAMGuard help for details</html>";
		}
		else if (PlatformInfo.calculateOS() == OSType.LINUX) {
			msg = "<html>Unknown failure on Linux. Contact PAMGuard support</html>";
		}
		else if (PlatformInfo.calculateOS() == OSType.MACOSX) {
			msg = "Automatic clock updates are not currently supported on Mac OSX";
		}
		else {
		    String osName = System.getProperty("os.name").toLowerCase();
			msg = "Automatic clock updates are not currently supported on " + osName;
		}
		WarnOnce.showWarning(this, tit, msg, WarnOnce.WARNING_MESSAGE);
	}
	
	@Override
	public void newTime(long timeMillis) {
		tellTime(gpsTime, timeMillis, timeMillis-System.currentTimeMillis());
		lastSentGPSTime = System.currentTimeMillis();
		tellPCTime();
		enableControls(timeMillis > 0);
		if (lastUpdate > 0) {
			if (--closeDown == 0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						setVisible(false);
					}
					
				});
			}
		}
	}
	private void enableControls(boolean b) {
		setButton.setEnabled(b);
	}
	

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
}
