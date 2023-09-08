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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import warnings.PamWarning;
import warnings.WarningSystem;
import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.AdminTools;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.SystemTiming;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

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
public class UpdateClockDialog extends JDialog implements ActionListener, PamObserver {

	static UpdateClockDialog singleInstance;
	
	JTextField pcTime, gpsTime;
	
	JButton setButton, cancelButton;
	
	JCheckBox setAlways;
	
	static GPSParameters gpsParameters;
	
	GPSControl gpsControl;
	
	boolean setOnNextString;
	
	Calendar nmeaCalendar;
	
	private static int clockSets;
	
	boolean autoUpdate;
	
	int newDataCount = 0;
	
	private UpdateClockDialog(Frame parentFrame) {
		super(parentFrame, "Set PC Clock");
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(new EmptyBorder(10,10,5,10));
		
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
		
		JPanel pcTimePanel = new JPanel();
		pcTimePanel.setLayout(new BorderLayout());
//		pcTimePanel.setBorder(new EmptyBorder(4,4,4,4));
		pcTimePanel.setBorder(new TitledBorder("Current PC System Time"));
		pcTimePanel.add(pcTime = new JTextField());
		
		JPanel gpsTimePanel = new JPanel();
		gpsTimePanel.setLayout(new BorderLayout());
		gpsTimePanel.setBorder(new TitledBorder("Current GPS Time"));
		gpsTimePanel.add(gpsTime = new JTextField());
//		gpsTime.setBorder(new EmptyBorder(4,4,4,4));
		
		timePanel.add(pcTimePanel);
		timePanel.add(gpsTimePanel);
		
		timePanel.add(setAlways = new JCheckBox("Auto set on Pamguard start-up"));
		
		JPanel okPanel = new JPanel();
		okPanel.add(setButton = new JButton(" Set Now "));
		okPanel.setEnabled(false);
		getRootPane().setDefaultButton(setButton);
		okPanel.add(cancelButton = new JButton("Close"));
		//getContentPane().add(BorderLayout.SOUTH, okPanel);
		setButton.addActionListener(this);
		cancelButton.addActionListener(this);

		p.add(BorderLayout.CENTER, timePanel);
		p.add(BorderLayout.SOUTH, okPanel);
		
		setContentPane(p);
		

		pack();
		setLocation(300, 200);
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
		UpdateClockDialog.gpsParameters = gpsParameters;//.clone();
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new UpdateClockDialog(parentFrame);
		}
		
		if (AdminTools.isAdmin() == false) {
			JOptionPane.showMessageDialog(singleInstance, "The PC Clock can only be set automatically if you run PAMGuard as Administrator", 
					"Warning", JOptionPane.ERROR_MESSAGE);
		}
		
		singleInstance.autoUpdate = autoUpdate;
		singleInstance.setModal(autoUpdate == false); // bodge it so it doens't halt program execution during auto update
		singleInstance.setAlwaysOnTop(autoUpdate);
		singleInstance.gpsControl = gpsControl;
		singleInstance.newDataCount = 0;
		singleInstance.setVisible(true);
		return UpdateClockDialog.gpsParameters;
	}

	@Override
	public void setVisible(boolean b) {
		NMEADataBlock nmeaDataBlock = gpsControl.getNMEADataBlock();
		if (b) {
			tellTime();
			timer.start();
			setButton.setEnabled(false);
			setOnNextString = false;
			setAlways.setSelected(gpsParameters.setClockOnStartup);
			if (nmeaDataBlock != null) {
				nmeaDataBlock.addObserver(this);
			}
		}
		else { 
			timer.stop();
			gpsParameters.setClockOnStartup = setAlways.isSelected();
			if (nmeaDataBlock != null) {
				nmeaDataBlock.deleteObserver(this);
			}
		}
		super.setVisible(b);
	}

	Timer timer = new Timer(500, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			tellTime();
		}
	});

	private void tellTime() {
		pcTime.setText(PamCalendar.formatDateTime(PamCalendar.getTimeInMillis()));
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			this.setVisible(false);

		} else if (e.getSource() == setButton) {
			setOnNextString = true;
			setButton.setEnabled(false);
		}
		
	}

	@Override
	public PamObserver getObserverObject() {
		return this;
	}

	public String getObserverName() {
		return "Update Clock dialog";
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 0;
	}

	public void noteNewSettings() {
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {		
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveSourceNotification(int type, Object object) {
		// don't do anything by default
	}

	public void addData(PamObservable o, PamDataUnit arg) {
		// look to see if it's an RMC String and if so get the date and time out.
//		NMEADataBlock nmeaDataBlock = (NMEADataBlock) o;
		NMEADataUnit nmeaDataUnit = (NMEADataUnit) arg;
		StringBuffer nmeaData = nmeaDataUnit.getCharData();
		String stringId = NMEADataBlock.getSubString(nmeaData, 0);
		if (gpsControl.wantString(stringId) == false) {
			return;
		}
		if (gpsControl.gpsParameters.mainString == GPSParameters.READ_GGA) {
			newGGAData(nmeaData);
		}else {
			newRMCData(nmeaData);
		}
	}
	
	
	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		
	}
	private void newGGAData(StringBuffer nmeaData) {
		String date, time;
		try {
			time = NMEADataBlock.getSubString(nmeaData, 1);
		}
		catch (Exception Ex) {
			return;
		}
		
		newDataCount++;
		if (time == null || time.length() < 6){
			gpsTime.setText("Invalid time "  + time);
			return;
		}
		int hour, minute;
		double second;
		try {
		  hour = Integer.valueOf(time.substring(0,2));
		  minute = Integer.valueOf(time.substring(2,4));
		  second = Double.valueOf(time.substring(4)); // should pick up any decimal seconds. 
		}
		catch (Exception Ex) {
			return;
		}
		/*
		 * Some strings have a decimal number of seconds. Try getting this and adding 
		 * some millis. 
		 */
		int millis = 0;
		double allTime = 0;
		try {
			allTime = Double.valueOf(time);
		}
		catch (NumberFormatException e) {
			allTime = 0;
		}
		millis = (int) (allTime*1000);
		millis = millis%1000;
		
		nmeaCalendar = Calendar.getInstance();
		nmeaCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date currTime = nmeaCalendar.getTime();
		int year = currTime.getYear();
		// fudge to last the next 85 years !
		while (year < 2000) {
			year += 100;
		}
		nmeaCalendar.set(year, currTime.getMonth(), currTime.getDate(), hour, minute, (int) second);
		gpsTime.setText(PamCalendar.formatDateTime(nmeaCalendar.getTimeInMillis()));
		tellTime(); // so they move together.
		

		if (setOnNextString) {
			// go ahead and update the clock.
//			System.setProperties()
//			Calendar.getInstance().
			setTimeNow(nmeaCalendar);
			setButton.setEnabled(false);
			setOnNextString = false;
		}
		else {
			setButton.setEnabled(AdminTools.isAdmin());
		}
		
		if (autoUpdate) {
			if (newDataCount == 2) {
				setOnNextString = AdminTools.isAdmin();
			}
			else if (newDataCount == 5) {
				setVisible(false);
			}
		}
		
	}
	private void  newRMCData(StringBuffer nmeaData) {
		String date, time;
		try {
			date = NMEADataBlock.getSubString(nmeaData, 9);
			time = NMEADataBlock.getSubString(nmeaData, 1);
		}
		catch (Exception Ex) {
			return;
		}
		
		newDataCount++;
		
		if (date == null || time == null || date.length() < 6 || time.length() < 6){
			gpsTime.setText("Invalid date time " + date + " " + time);
			return;
		}
		int day, month, year, hour, minute;
		double second;
		try {
		  day = Integer.valueOf(date.substring(0,2));
		  month = Integer.valueOf(date.substring(2,4));
		  year = Integer.valueOf(date.substring(4)); // should cope with four digit year.
		  if (year < 2000) year += 2000; 
		  hour = Integer.valueOf(time.substring(0,2));
		  minute = Integer.valueOf(time.substring(2,4));
		  second = Double.valueOf(time.substring(4)); // shoud pick up any decimal seconds. 
		}
		catch (Exception Ex) {
			return;
		}
		nmeaCalendar = Calendar.getInstance();
		nmeaCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		nmeaCalendar.set(year, month-1, day, hour, minute, (int) second);
		gpsTime.setText(PamCalendar.formatDateTime(nmeaCalendar.getTimeInMillis()));
		tellTime(); // so they move together.
		
		
		if (setOnNextString) {
			// go ahead and update the clock.
//			System.setProperties()
//			Calendar.getInstance().
			setTimeNow(nmeaCalendar);
			setButton.setEnabled(false);
			setOnNextString = false;
		}
		else {
			setButton.setEnabled(AdminTools.isAdmin());
		}
		
		if (autoUpdate) {
			if (newDataCount == 2) {
				setOnNextString = true;
			}
			else if (newDataCount == 5) {
				setVisible(false);
			}
		}
	}
	

	private void setTimeNow(Calendar nmeaCalendar) {
		if (SystemTiming.setSystemTime(nmeaCalendar) == false) {
			String msg =  "The PC Clock can only be set automatically if you run PAMGuard as Administrator";
			JOptionPane.showMessageDialog(singleInstance, msg, 
					"Warning", JOptionPane.ERROR_MESSAGE);
			System.out.println("Update system clock failed");
			PamWarning newWarning = new PamWarning("GPS Clock Setting", msg, 2);
			WarningSystem.getWarningSystem().addWarning(newWarning);
		}
		else {
			System.out.println(String.format("PC Clock updated to " + PamCalendar.formatDateTime(nmeaCalendar.getTimeInMillis())));
		}
	}
	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub
		
	}

	public static int getClockSets() {
		return clockSets;
	}
}
