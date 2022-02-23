package pamScrollSystem;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimeZone;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamUtils.time.CalendarControl;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class LoadOptionsDialog extends PamDialog {
	
	private PamScrollerData scrollerData;
	
	private JTextField startTime, duration, stepSize;
	
	private JLabel startTimeZone;
	
	private long minDuration, maxDuration; // scroller load period
	
	private long dataMinTime, dataMaxTime, dataMaxDuration; // total dataperiod. 
	
	private long currentDuration;
	
	private JButton durationButton;
	
	public static ImageIcon settings = new ImageIcon(ClassLoader
			.getSystemResource("Resources/MenuButton.png"));
	
	@SuppressWarnings("unused")
	private AbstractScrollManager scrollManager;
	
	private long[] standardLoadTimes = {1000, 2000, 5000, 10000, 30000, 60*1000, 2*60*1000,
			5*60*1000, 10*60*1000, 15*60*1000, 30*60*1000, 3600*1000, 3600*2*1000, 3600*6*1000,
			3600*12*1000, 3600*24*1000, 2*3600*24*1000, 7*3600*24*1000, 14L*3600L*24L*1000L, 30L*3600L*24L*1000L,
			60L*3600L*24L*1000L, 365L*3600L*24L*1000L};
	
	private LoadOptionsDialog(Window parentFrame, AbstractPamScroller scroller, JComponent parentComponent) {
		super(parentFrame, scroller.scrollerData.name + " Options", false);
		scrollManager = scroller.getScrollManager();
		dataMinTime = scrollManager.checkMinimumTime(Long.MIN_VALUE);
		dataMaxTime = scrollManager.checkMaximumTime(Long.MAX_VALUE);
		dataMaxDuration = dataMaxTime - dataMinTime;

		setSendGeneralSettingsNotification(false);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		panel.setBorder(new TitledBorder("Navigation options"));
		addComponent(panel, new JLabel("Start Time ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 1;
		addComponent(panel, startTime = new JTextField(14), c);
		c.gridx++;
		addComponent(panel, startTimeZone = new JLabel("", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(panel, new JLabel("Duration ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(panel, duration = new JTextField(16), c);
		duration.setEditable(false);
		c.gridx++;
		addComponent(panel, durationButton = new JButton(settings), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(panel, new JLabel("Step Size ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(panel, stepSize = new JTextField(7), c);
		c.gridx++;
		addComponent(panel, new JLabel(" %", SwingConstants.LEFT), c);
		
		startTime.setToolTipText("Start date and time for data loaded into memory");
		duration.setToolTipText("Duration of the data loaded into memory");
		durationButton.setToolTipText("Select data duration");
		stepSize.setToolTipText("Step size forward or backward when loading data into memory");
		
		durationButton.addActionListener(new DurationButton());
//		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//		ge.
//		parentComponent.getp
		setDialogComponent(panel);
//		setSize(new Dimension(300,200));
//		pack();
		setLocationRelativeTo(parentComponent);
		
		
	}
	
	private class DurationButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			durationButtonPressed(arg0);
		}	
	}

	private void durationButtonPressed(ActionEvent actionEvent) {
		JPopupMenu menu = new JPopupMenu();
		JCheckBoxMenuItem menuItem;
		long loadTime;
		int nItems = 0;
		String menuString;
		for (int i = 0; i < standardLoadTimes.length; i++) {
			loadTime = standardLoadTimes[i];
			if (loadTime < minDuration || loadTime > maxDuration) {
				continue;
			}
			nItems++;
			menuString = "";
//			for (int j = 0; j < nItems; j++) {
//				menuString += "\\";
//			}
			menuString += " " + PamCalendar.formatDuration(loadTime);
			menuItem = new JCheckBoxMenuItem(menuString);
//			if (loadTime =
			menuItem.addActionListener(new SelectDuration(loadTime));
			menu.add(menuItem);
		}
		if (dataMaxDuration <= maxDuration) {
			menuItem = new JCheckBoxMenuItem(String.format("%s (all data)", 
					PamCalendar.formatDuration(dataMaxDuration)));
			menuItem.addActionListener(new SelectDuration(dataMaxDuration));
			menu.add(menuItem);
		}
		menu.show(durationButton, durationButton.getWidth()/2, durationButton.getHeight()/2);
		
	}
	
	class SelectDuration implements ActionListener {
		long duration;

		
		public SelectDuration(long duration) {
			super();
			this.duration = duration;
		}


		@Override
		public void actionPerformed(ActionEvent arg0) {
			setDuration(duration);
		}
		
	}
	
	public static PamScrollerData showDialog(Window parentFrame, AbstractPamScroller scroller, JComponent parentComponent) {
		LoadOptionsDialog lod = new LoadOptionsDialog(parentFrame, scroller, parentComponent);
		lod.setParams(scroller);
		lod.setVisible(true);
		return lod.scrollerData;
	}

	private void setParams(AbstractPamScroller scroller) {
		this.scrollerData = scroller.scrollerData.clone();
		sayStartDate(scroller.scrollerData.minimumMillis);
		setDuration(scroller.scrollerData.maximumMillis-scroller.scrollerData.minimumMillis);
		stepSize.setText(String.format("%d", scroller.scrollerData.pageStep));
		minDuration = scrollerData.getStepSizeMillis()*100;
		maxDuration = minDuration * 1000000;
		pack();
	}
	
	private void setDuration(long duration) {
		currentDuration = duration;
		this.duration.setText(PamCalendar.formatDuration(duration));
		Long currentStart = readStartDate();
		if (currentStart < 0) {
			return;
		}
		long newDataMax = currentStart + currentDuration;
		if (newDataMax > dataMaxTime && dataMaxTime > 0) {
			newDataMax = dataMaxTime;
			currentStart = newDataMax - currentDuration;
			currentStart = scrollManager.checkMinimumTime(currentStart);
			sayStartDate(currentStart);
		}
	}
	
	/**
	 * Write the current start date into the dialog. 
	 */
	private void sayStartDate(long startDate) {
		String start = PamCalendar.formatDBStyleTime(startDate, false, true);
		startTime.setText(start);
		startTimeZone.setText(CalendarControl.getInstance().getTZCode(true));
		startTimeZone.setToolTipText(CalendarControl.getInstance().getChosenTimeZone().getDisplayName(true, TimeZone.LONG));
	}
	/**
	 * 
	 * @return the current date as read from the dialog. 
	 */
	private long readStartDate() {
		long t = PamCalendar.msFromDateString(startTime.getText(), true);
		return t;
	}

	@Override
	public void cancelButtonPressed() {
		scrollerData = null;
	}

	@Override
	public boolean getParams() {
		scrollerData.minimumMillis = readStartDate();
		if (scrollerData.minimumMillis < 0) {
			return this.showWarning("Invalid start date");
		}
		scrollerData.maximumMillis = scrollerData.minimumMillis + currentDuration;
		try {
			scrollerData.pageStep = Integer.valueOf(stepSize.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid Step Size (must be between 1 and 100%");
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
