package Acquisition.filedate;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;

public class FileDateDialog extends PamDialog {
	
	private static FileDateDialog singleInstance;
	
	private StandardFileDateSettings standardFileDateSettings;
	
	private JComboBox<String> offlineTimeZone;
	
	private JCheckBox daylightSaving;
	
	private JTextField additionalOffset;

	private ArrayList<TimeZone> timeZones = new ArrayList<>();
	
	private JPanel soundTrapDate;

	private boolean allowCustomFormats;
	
	private JTextField customDateTimeFormat;

	private JCheckBox forcePCTime;
	
	private JRadioButton autoFormat, manualFormat;
	
	private FileDateDialog(Window parentFrame) {
		super(parentFrame, "File Date Settings", true);
		
//		JPanel mainPanel = new JPanel(new BorderLayout(5,5));
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		GridBagConstraints gbcmp = new PamGridBagContraints();
		gbcmp.fill = SwingConstants.HORIZONTAL;
		

		soundTrapDate = new JPanel(new GridBagLayout());
//		soundTrapDate.setPreferredSize(tzPanel.getPreferredSize());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		soundTrapDate.add(autoFormat = new JRadioButton("Use automatic date format selection"),c);
		c.gridy++;
		soundTrapDate.add(manualFormat = new JRadioButton("User defined date format"),c);
		customDateTimeFormat = new JTextField(30);
		c.gridy++;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		soundTrapDate.setBorder(new CompoundBorder(new TitledBorder("Date/Time Format "), new EmptyBorder(0, 10, 0, 0)));
		soundTrapDate.add(new JLabel("Enter the date/time format to use ", JLabel.LEFT),c);
		c.gridx++;
		soundTrapDate.add(customDateTimeFormat,c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		String text = "<html>Use # to replace all non-date numeric charcters. Count carefully!<br>" +
		"e.g. to get a date from a file 1677738025.180912065628.d24.d8.wav use <br>###########yyMMddHHmmss########</html>";
		soundTrapDate.add(new JLabel(text, JLabel.LEFT),c);
		c.gridy++;
		text = "<html><body style='width: 350px'>" + 
				"See <a href=\"https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html\">"
				+ "https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html</a> for " +
				"information on date and time codes, as well as examples of common formats.";
		JLabel linkTxt;
		soundTrapDate.add(linkTxt = new JLabel(text, JLabel.LEFT),c);
		c.gridy++;
		text = "For soundtraps, the default format is dd/MM/yyyy HH:mm:ss";
		soundTrapDate.add(new JLabel(text, JLabel.LEFT),c);
		JPanel mPanel = new JPanel(new BorderLayout());
		mPanel.add(BorderLayout.WEST, soundTrapDate);
//		mainPanel.add(BorderLayout.SOUTH, mPanel);
		gbcmp.gridy++;
		mainPanel.add(mPanel, gbcmp);
		
		JPanel tzPanel = new PamAlignmentPanel(new GridBagLayout(), BorderLayout.WEST);
		c = new PamGridBagContraints();
		
		tzPanel.setBorder(new CompoundBorder(new TitledBorder("Time Zone"), new EmptyBorder(0, 10, 0, 0)));
		
		offlineTimeZone = new JComboBox<>();
		fillTimeZones();
//		zones = TimeZone.getAvailableIDs();
		
		
//		TimeZone tz;
//		String tzStr;
//		for (int i = 0; i < zones.length; i++) {
//			tz = TimeZone.getTimeZone(zones[i]);
//			if (tz.getRawOffset() < 0) {
//				tzStr = String.format("UTC%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
//			}
//			else {
//				tzStr = String.format("UTC+%3.1f %s (%s)", (double)tz.getRawOffset()/3600000., tz.getID(), tz.getDisplayName());
//			}
//			offlineTimeZone.addItem(tzStr);
//		}
		daylightSaving = new JCheckBox("Use daylight saving");
		
		c.gridx = c.gridy = 0;
		tzPanel.add(offlineTimeZone, c);
		c.gridy++;
		tzPanel.add(daylightSaving, c);
//		mainPanel.add(BorderLayout.NORTH, tzPanel);
		gbcmp.gridx = gbcmp.gridy = 0;
		mainPanel.add(tzPanel, gbcmp);
		
		JPanel aPanel = new PamAlignmentPanel(new GridBagLayout(), BorderLayout.WEST);
		c = new PamGridBagContraints();
		additionalOffset = new JTextField(6);
		c.gridx = c.gridy = 0;
		aPanel.setBorder(new CompoundBorder(new TitledBorder("Additional Time Offset "), new EmptyBorder(0, 10, 0, 0)));
		aPanel.add(new JLabel("Additional offset to add to file time ", JLabel.RIGHT));
		c.gridx++;
		aPanel.add(additionalOffset);
		c.gridx++;
		aPanel.add(new JLabel(" Seconds ", JLabel.LEFT));
//		JPanel lPanel = new JPanel(new BorderLayout());
//		lPanel.add(BorderLayout.WEST, aPanel);
//		mainPanel.add(BorderLayout.CENTER, lPanel);
		gbcmp.gridy++;
		mainPanel.add(aPanel, gbcmp);

		
		JPanel ovPanel = new PamAlignmentPanel(new GridBagLayout(), BorderLayout.WEST);
		c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		ovPanel.setBorder(new CompoundBorder(new TitledBorder("Force PC Time "), new EmptyBorder(0, 10, 0, 0)));
		ovPanel.add(new JLabel("Override time from file and force current PC time ", JLabel.RIGHT));
		c.gridx++;
		ovPanel.add(forcePCTime = new JCheckBox());
//		JPanel forcePanel = new JPanel(new BorderLayout());
//		forcePanel.add(BorderLayout.WEST, ovPanel);
//		mainPanel.add(BorderLayout.CENTER, forcePanel);
		gbcmp.gridy++;
		mainPanel.add(ovPanel, gbcmp);

		
		
		linkTxt.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				PamGui.openURL("https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html");
			}
			
		});
		
		autoFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableContols();
			}
		});
		manualFormat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableContols();
			}
		});
		ButtonGroup bg = new ButtonGroup();
		bg.add(autoFormat);
		bg.add(manualFormat);

		setHelpPoint("sound_processing.AcquisitionHelp.docs.FileTimeZone");
		
		setDialogComponent(mainPanel);
	}
	
	private void fillTimeZones() {
		String[] zones = TimeZone.getAvailableIDs();	
//		zones = TimeZone.
		timeZones = new ArrayList<>();
		String tzStr;
		TimeZone tz;
		for (int i = 0; i < zones.length; i++) {
//			TimeZone tz = TimeZone.getTimeZone(zones[i]);
			try {
			tz = TimeZone.getTimeZone(ZoneId.of(zones[i]));
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
			offlineTimeZone.addItem(tzStr);
		}
	}
	
	public static StandardFileDateSettings showDialog(Window parentFrame, StandardFileDateSettings standardFileDateSettings, boolean allowCustomFormats) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new FileDateDialog(parentFrame);
		}
		singleInstance.standardFileDateSettings = standardFileDateSettings.clone();
		singleInstance.setAllowCustomFormats(allowCustomFormats);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.standardFileDateSettings;
	}

	private void setParams() {
		int idInd = getIdIndex(standardFileDateSettings.getTimeZoneName());
		if (idInd >= 0) {
			offlineTimeZone.setSelectedIndex(idInd);
		}
		autoFormat.setSelected(standardFileDateSettings.isUseBespokeFormat() == false);
		manualFormat.setSelected(standardFileDateSettings.isUseBespokeFormat());
		daylightSaving.setSelected(standardFileDateSettings.isAdjustDaylightSaving());
		forcePCTime.setSelected(standardFileDateSettings.isForcePCTime());
		additionalOffset.setText(String.format("%5.3f", (double) standardFileDateSettings.getAdditionalOffsetMillis() / 1000.));
		customDateTimeFormat.setText(standardFileDateSettings.getForcedDateFormat());
		soundTrapDate.setVisible(allowCustomFormats);
		enableContols();
		this.pack();
	}
	
	private int getIdIndex(String tzId) {
//		if (tzId == null) {
//			tzId = "UTC";
//		}
//		for (int i = 0; i < zones.length; i++) {
//			if (tzId.equals(zones[i])) {
//				return i;
//			}
//		}
		for (int i = 0; i < timeZones.size(); i++) {
			if (timeZones.get(i).getID().equals(tzId)) {
				offlineTimeZone.setSelectedIndex(i);
				break;
			}
		}
		return -1;
	}

	@Override
	public boolean getParams() {
		int idInd = offlineTimeZone.getSelectedIndex();
		if (idInd < 0) {
			return showWarning("You must select a time zone");
		}
		TimeZone tz = timeZones.get(idInd);
		if (tz == null) {
			return showWarning("The time zone you have selected does not exist");
		}
		standardFileDateSettings.setTimeZoneName(tz.getID());
		standardFileDateSettings.setAdjustDaylightSaving(daylightSaving.isSelected());
		standardFileDateSettings.setForcePCTime(forcePCTime.isSelected());
		
		try {
			long millis = (long) (Double.valueOf(additionalOffset.getText()) * 1000.);
			standardFileDateSettings.setAdditionalOffsetMillis(millis);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Additional Time Offset value");
		}
		String custDate = customDateTimeFormat.getText();
		if (custDate != null && custDate.isBlank()) {
			custDate = null;
		}
		standardFileDateSettings.setForcedDateFormat(custDate);
		standardFileDateSettings.setUseBespokeFormat(manualFormat.isSelected());
		if (manualFormat.isSelected()) {
			if (custDate == null) {
				return showWarning("If you intend to use a user defined date format, then you must specify the format to use");
			}
			// and do a check to see if hh is in the string. If it is, they should probably be using HH
			if (custDate.contains("hh")) {
				String msg = "<html>Your custom date format contains 'hh' for hours. 'hh' is a 12 hour format, meaning that file times may be misinterpreted."
						+ "<br><br>It's likely thta you meant to use 'HH' for 24 hour time format."
						+ "<br><br>Press Cancel to edit the date format, or Ok to continue with 'hh'.</html>";
				int ans = WarnOnce.showWarning(this, "Custom date format warning", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return false;
				}
			}
		}
		
		standardFileDateSettings.setDateTimeFormatToUse(customDateTimeFormat.getText());
		
		return true;
	}
	
	private void enableContols() {
		customDateTimeFormat.setEnabled(autoFormat.isSelected() == false);
	}

	@Override
	public void cancelButtonPressed() {
		standardFileDateSettings = null;
	}

	@Override
	public void restoreDefaultSettings() {
		standardFileDateSettings = new StandardFileDateSettings();
		setParams();
	}

	public void setAllowCustomFormats(boolean allowCustomFormats) {
		this.allowCustomFormats = allowCustomFormats;
	}

}
