package PamUtils.time;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.TimeZone;

import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import weka.core.SingleIndex;

public class CalendarControl implements PamSettings{
	
	private TimeDisplayParameters timeDisplayParameters = new TimeDisplayParameters();
	
	private static CalendarControl singleInstance;
			
	private CalendarControl() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public static CalendarControl getInstance() {
		if (singleInstance == null) {
			synchronized (CalendarControl.class) {
				if (singleInstance == null) {
					singleInstance = new CalendarControl();
				}
			}
		}
		return singleInstance;
	}
	
	public JMenuItem getMenu(Window window) {
		JMenuItem menuITem = new JMenuItem("Time Zone");
		menuITem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showTimeZoneDialog(window);
			}
		});
		return menuITem;
	}
	
	public TimeZone getChosenTimeZone() {
		if (timeDisplayParameters.timeZone == null) {
			timeDisplayParameters.timeZone = PamCalendar.defaultTimeZone;
		}
		return timeDisplayParameters.timeZone;
	}

	protected void showTimeZoneDialog(Window window) {
		TimeDisplayParameters tzParams = TimeZoneDisplayDialog.showDialog(window, timeDisplayParameters);
		if (tzParams != null) {
			timeDisplayParameters = tzParams;
		}
	}

	@Override
	public String getUnitName() {
		return "Calendar Control";
	}

	@Override
	public String getUnitType() {
		return "Calendar Control";
	}

	@Override
	public Serializable getSettingsReference() {
		return timeDisplayParameters;
	}

	@Override
	public long getSettingsVersion() {
		return TimeDisplayParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		timeDisplayParameters = ((TimeDisplayParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
	public String getTZCode(boolean daylight) {
		TimeZone tz = getChosenTimeZone();
		return tz.getDisplayName(true, TimeZone.SHORT);
	}

	public boolean isUTC() {
		TimeZone tz = getChosenTimeZone();
		return (tz.getID().equals(PamCalendar.defaultTimeZone.getID()));
	}
	

}
