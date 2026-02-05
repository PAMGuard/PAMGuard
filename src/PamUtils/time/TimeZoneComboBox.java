package PamUtils.time;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.JComboBox;

/**
 * TimeZone combobox. A combobox showing all available time zones. 
 */
public class TimeZoneComboBox extends JComboBox<String> {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<TimeZone> timeZones;

	/**
	 * Create a time zone combo box which will be automatically filled with a sorted list
	 * of all available time zones. 
	 */
	public TimeZoneComboBox() {
		fillTimeZones();
	}
	
	/**
	 * Set current time zone
	 * @param aZone
	 * @return true if zone exists and can be selected
	 */
	public boolean setTimeZone(TimeZone aZone) {
		int ind = timeZones.indexOf(aZone);
		if (ind < 0) {
			return false;
		}
		this.setSelectedIndex(ind);
		return true;
	}
	
	/**
	 * Set time zone based on a zone name. 
	 * @param zoneName
	 * @return true if zone exists and can be selected
	 */
	public boolean setTimeZone(String zoneName) {
		try {
			ZoneId zoneId = ZoneId.of(zoneName);
			TimeZone tz = TimeZone.getTimeZone(zoneId);
			return setTimeZone(tz);
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Set to the default time zone. 
	 * @return true if zone exists and can be selected
	 */
	public boolean setDefault() {
		TimeZone def = TimeZone.getDefault();
		return setTimeZone(def);
	}
	
	/**
	 * Get current selected time zone
	 * @return current zone (should never be null)
	 */
	public TimeZone getTimeZone() {
		int ind = getSelectedIndex();
		if (ind >= 0) {
			return timeZones.get(ind);
		}
		else {
			return null;
		}
	}

	/**
	 * Fill the time zone list
	 */
	private void fillTimeZones() {
		timeZones = new ArrayList<TimeZone>();
		Set<String> zoneIds = ZoneId.getAvailableZoneIds();
		String tzStr;
		TimeZone tz;
		for (String zoneName : zoneIds) {
//		for (int i = 0; i < zones.length; i++) {
//			TimeZone tz = TimeZone.getTimeZone(zones[i]);
			try {
				tz = TimeZone.getTimeZone(ZoneId.of(zoneName));
			}
			catch (Exception e) {
				continue;
			}
			if (tz == null) {
				continue;
			}
			timeZones.add(tz);
		}
		Collections.sort(timeZones, new Comparator<TimeZone>() {
			@Override
			public int compare(TimeZone o1, TimeZone o2) {
				return o2.getRawOffset()-o1.getRawOffset();
			}
		});
		int offs;
		for (int i = 0; i < timeZones.size(); i++) {
			tz = timeZones.get(i);
			String id =  tz.getID();
			String displayName =  tz.getDisplayName();
			offs = tz.getRawOffset();
			if (tz.getRawOffset() < 0) {
				tzStr = String.format("UTC%3.1f %s (%s)", (double)offs/3600000., id,  displayName);
			}
			else {
				tzStr = String.format("UTC+%3.1f %s (%s)", (double)offs/3600000., id, displayName);
			}
			this.addItem(tzStr);
		}
		
	}

}
