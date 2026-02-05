package PamUtils.time;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;

/**
 * Swing panel for a time zone combo and additional controls if required. 
 */
public class TimeZonePanel {

	private JPanel mainPanel;
	
	private TimeZoneComboBox timeZoneComboBox;
	
	private JCheckBox useDaylight;
	
	public TimeZonePanel(String title, boolean showDaylightButton, boolean showDefault, boolean showSetUTC) {
		timeZoneComboBox = new TimeZoneComboBox();
		useDaylight = new JCheckBox("Use daylight saving");
		
				
		mainPanel = new JPanel(new GridBagLayout());
		if (title != null) {
			mainPanel.setBorder(new TitledBorder(title));
		}
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 4;
		mainPanel.add(timeZoneComboBox, c);
		c.gridy++;
		c.gridwidth = 1;
		if (showSetUTC) {
			JButton defBut = new JButton("UTC");
			defBut.addActionListener(new SetZone(PamCalendar.defaultTimeZone));
			defBut.setToolTipText("Set to UTC (Universal Coordinated Time / Zulu)");
			mainPanel.add(defBut, c);
			c.gridx++;
		}
		if (showDefault) {
			JButton defBut = new JButton("Local");
			defBut.addActionListener(new SetZone(TimeZone.getDefault()));
			defBut.setToolTipText("Set to regional defulat time zone");
			mainPanel.add(defBut, c);
			c.gridx++;
		}
		if (showDaylightButton) {
			mainPanel.add(useDaylight, c);
		}
	}
	
	/**
	 * Get the main swing component
	 * @return
	 */
	public JPanel getComponent() {
		return mainPanel;
	}
	
	/**
	 * Set selected time zone
	 * @param timezone
	 * @return true if valid and selected. 
	 */
	public boolean setTimeZone(TimeZone timezone) {
		return timeZoneComboBox.setTimeZone(timezone);
	}
	
	/**
	 * Set selected time zone by name
	 * @param timezone
	 * @return true if valid and selected
	 */
	public boolean setTimeZone(String timezone) {
		return timeZoneComboBox.setTimeZone(timezone);
	}
	
	/**
	 * Get selected time zone
	 * @return
	 */
	public TimeZone getTimeZone() {
		return timeZoneComboBox.getTimeZone();
	}
	
	/**
	 * Set use dayling saving
	 * @param useDaylight
	 */
	public void setUseDaylightSaving(boolean useDaylight) {
		this.useDaylight.setSelected(useDaylight);
	}
	
	/**
	 * 
	 * @return is use daylight saving selected
	 */
	public boolean isUseDaylightSaving() {
		return useDaylight.isSelected();
	}

	private class SetZone implements ActionListener {

		private TimeZone zone;
		
		/**
		 * @param zone
		 */
		public SetZone(TimeZone zone) {
			super();
			this.zone = zone;
		}



		@Override
		public void actionPerformed(ActionEvent e) {
			timeZoneComboBox.setTimeZone(zone);
		}
		
	}
}
