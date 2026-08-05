package PamUtils.time;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class TimeZoneDisplayDialog extends PamDialog {
	
	private JRadioButton useUTC, usePC, useOther;
	private TimeZoneComboBox timeZones;
	private JLabel pcTimeZone;
	private TimeDisplayParameters timeDisplayParameters;
//	private TimeZone thisTimeZone;
	private int utcTZIndex, pcTZIndex;
	private static TimeZoneDisplayDialog singleInstance;

	private TimeZoneDisplayDialog(Window parentFrame) {
		super(parentFrame, "Time Zone", true);
		
		JPanel tzPanel = new JPanel(new GridBagLayout());
		tzPanel.setBorder(new TitledBorder("Select Time Zone"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		tzPanel.add(useUTC = new JRadioButton("Display in UTC"), c);
		c.gridy++;
		c.gridwidth = 2;
		tzPanel.add(usePC = new JRadioButton("Use PC Local Time"), c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 1;
		tzPanel.add(pcTimeZone = new JLabel(" PC Zone", JLabel.LEFT),c);
		c.gridy++;
		c.gridx = 0;
		tzPanel.add(useOther = new JRadioButton("Other"), c);
		c.gridx++;
		c.gridwidth = 2;
		tzPanel.add(timeZones = new TimeZoneComboBox(), c);
		c.gridwidth = 3;
		c.gridy++;
		c.gridx = 0;
		JLabel ta = new JLabel();
		ta.setText("<html>Note that all processing and data storage will continue to use UTC. <p>The selection " +
		"you make here will only affect what is displayed on the screen.");
		tzPanel.add(ta, c);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(useUTC);
		bg.add(usePC);
		bg.add(useOther);

//		TimeZone pcTimeZone = TimeZone.getDefault();
//		Set<String> timeZoneIds = ZoneId.getAvailableZoneIds();
//		Arrays.sort(timeZoneIds, new TimeZoneComparator());
//		TimeZone tz;
//		String tzStr;
//		for (int i = 0; i < timeZoneIds.length; i++) {
//			tz = TimeZone.getTimeZone(timeZoneIds[i]);
//			if (timeZoneIds[i].equals(PamCalendar.defaultTimeZone.getID())) {
//				utcTZIndex = i;
//			}
//			if (timeZoneIds[i].equals(pcTimeZone.getID())) {
//				pcTZIndex = i;
//			}
//			if (tz.getRawOffset() < 0) {
//				tzStr = String.format("UTC%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
//			}
//			else {
//				tzStr = String.format("UTC+%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
//			}
//			timeZones.addItem(tzStr);
//		}
		
		ButtonChanged bc = new ButtonChanged();
		useUTC.addActionListener(bc);
		usePC.addActionListener(bc);
		useOther.addActionListener(bc);
		
		setDialogComponent(tzPanel);
	}
	
	public static TimeDisplayParameters showDialog(Window parentFrame, TimeDisplayParameters timeDisplayParameters) {
		singleInstance = new TimeZoneDisplayDialog(parentFrame);
		singleInstance.setParams(timeDisplayParameters);
		singleInstance.setVisible(true);
		return singleInstance.timeDisplayParameters;
	}
	
	private class ButtonChanged implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
			showSelection();
		}
		
	}

	private void setParams(TimeDisplayParameters timeDisplayParameters) {
		this.timeDisplayParameters = timeDisplayParameters;
		useUTC.setSelected(timeDisplayParameters.zoneType == TimeDisplayParameters.TIME_ZONE_UTC);
		usePC.setSelected(timeDisplayParameters.zoneType == TimeDisplayParameters.TIME_ZONE_PC);
		useOther.setSelected(timeDisplayParameters.zoneType == TimeDisplayParameters.TIME_ZONE_OTHER);
		TimeZone defaultTimeZone = TimeZone.getDefault();
		pcTimeZone.setText(String.format("(%s / %s)", defaultTimeZone.getID(), defaultTimeZone.getDisplayName()));
		enableControls();
		showSelection();
	}

	public void showSelection() {
		if (useUTC.isSelected()) {
			timeZones.setSelectedIndex(utcTZIndex);
		}
		else if (usePC.isSelected()) {
			timeZones.setSelectedIndex(pcTZIndex);
		}
		else {
			setSelTimeZone(timeDisplayParameters.timeZone);
		}
		
	}

	private void setSelTimeZone(TimeZone timeZone) {
		if (timeZone == null) {
			return;
		}
		timeZones.setTimeZone(timeZone);
	}


	public void enableControls() {
		timeZones.setEnabled(useOther.isSelected());
	}

	@Override
	public boolean getParams() {
		if (useUTC.isSelected()) {
			timeDisplayParameters.zoneType = TimeDisplayParameters.TIME_ZONE_UTC;
			timeDisplayParameters.timeZone = PamCalendar.defaultTimeZone;
		}
		else if (usePC.isSelected()) {
			timeDisplayParameters.zoneType = TimeDisplayParameters.TIME_ZONE_PC;
			timeDisplayParameters.timeZone = TimeZone.getDefault();
		}
		else if (useOther.isSelected()) {
			timeDisplayParameters.zoneType = TimeDisplayParameters.TIME_ZONE_OTHER;
			int tzInd = timeZones.getSelectedIndex();
			if (tzInd < 0) {
				return showWarning("You must select a time zone from the drop down list");
			}
			TimeZone selZone = timeZones.getTimeZone();
			if (selZone == null) {
				timeDisplayParameters.timeZone = null;
			}
			else {
				timeDisplayParameters.timeZone = selZone;
			}
		}
		return timeDisplayParameters.timeZone != null;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
