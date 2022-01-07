package dataMap;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.OfflineDataStore;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamUtils.time.CalendarControl;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.panel.PamBorderPanel;

/**
 * Display for summary data inserted at top of DataMap
 * @author Doug Gillespie
 *
 */
public class SummaryPanel extends HidingDialogComponent {

	private JPanel panel;

	private DataMapControl dataMapControl;

	private JLabel[] dataNames, dataStarts, dataEnds;
	private JLabel cursorPos;
	private final int maxDataSources = 12;

	private JLabel[] toLabels;

	public SummaryPanel(DataMapControl dataMapControl, DataMapPanel dataMapPanel) {
		this.dataMapControl = dataMapControl;
		panel = new PamBorderPanel(new BorderLayout());
		JPanel innerPanel = new PamBorderPanel();
		panel.setBorder(new TitledBorder("Data Summary"));
		innerPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		dataNames = new JLabel[maxDataSources];
		toLabels = new JLabel[maxDataSources];
		dataStarts = new JLabel[maxDataSources];
		dataEnds = new JLabel[maxDataSources];
		c.gridx = 1;
		c.gridy = 0;
		PamDialog.addComponent(innerPanel, new PamLabel("Data Start "), c);
		c.gridx+=2;
		PamDialog.addComponent(innerPanel, new PamLabel("Data End"), c);
		c.gridx++;
		PamDialog.addComponent(innerPanel, new PamLabel("   "), c);
		c.gridx++;
		PamDialog.addComponent(innerPanel, new PamLabel("Cursor"), c);
		c.gridy++;
		PamDialog.addComponent(innerPanel, cursorPos = new PamLabel(), c);
		c.gridy--;

		for (int i = 0; i < maxDataSources; i++) {
			c.gridx = 0;
			c.gridy++;
			PamDialog.addComponent(innerPanel, dataNames[i] = new PamLabel(), c);
			c.gridx++;
			PamDialog.addComponent(innerPanel, dataStarts[i] = new PamLabel(), c);
			c.gridx++;
			PamDialog.addComponent(innerPanel, toLabels[i] = new PamLabel(" to "), c);
			c.gridx++;
			PamDialog.addComponent(innerPanel, dataEnds[i] = new PamLabel(), c);

			dataNames[i].setVisible(false);
			toLabels[i].setVisible(false);
			dataStarts[i].setVisible(false);
			dataEnds[i].setVisible(false);
		}

		panel.add(BorderLayout.WEST, innerPanel);
	}

	public void newDataSources() {
		ArrayList<OfflineDataStore> offlineDataStores = PamController.getInstance().findOfflineDataStores();
		OfflineDataStore aSource;
		int n = Math.min(offlineDataStores.size(), maxDataSources);
		long[] dataExtent;
		for (int i = 0; i < n; i++) {
			aSource = offlineDataStores.get(i);
			dataNames[i].setVisible(true);
			toLabels[i].setVisible(true);
			dataStarts[i].setVisible(true);
			dataEnds[i].setVisible(true);
			dataNames[i].setText(aSource.getDataSourceName());
			dataExtent = dataMapControl.getDataExtent(aSource);
			if (dataExtent[0] == Long.MAX_VALUE) {
				dataStarts[i].setText(" ---No data---");
				dataEnds[i].setText(" ---No data---");
			}
			else {
			dataStarts[i].setText(PamCalendar.formatDateTime2(dataExtent[0], true));
			dataEnds[i].setText(PamCalendar.formatDateTime(dataExtent[1], true));
			}
		}
		for (int i = offlineDataStores.size(); i < maxDataSources; i++) {
			dataNames[i].setVisible(false);
			toLabels[i].setVisible(false);
			dataStarts[i].setVisible(false);
			dataEnds[i].setVisible(false);
		}
		panel.invalidate();
		//		if (dataMapControl.getFirstBinaryTime() == Long.MAX_VALUE) {
		//			binStart.setText("No Data");
		//			binEnd.setText("");
		//		}
		//		else {
		//			binStart.setText(PamCalendar.formatDateTime2(dataMapControl.getFirstBinaryTime()));
		//			binEnd.setText(PamCalendar.formatDateTime(dataMapControl.getLastBinaryTime()));
		//		}
		//		if (dataMapControl.getFirstDatabaseTime() == Long.MAX_VALUE) {
		//			dbStart.setText("No Data");
		//			dbEnd.setText("");
		//		}
		//		else {
		//			dbStart.setText(PamCalendar.formatDateTime2(dataMapControl.getFirstDatabaseTime()));
		//			dbEnd.setText(PamCalendar.formatDateTime(dataMapControl.getLastDatabaseTime()));
		//		}
	}

	protected void setCursorTime(Long timeMillis) {
		if (timeMillis == null) {
			cursorPos.setText("");
		}
		else {
			String str = PamCalendar.formatDateTime2(timeMillis, true);
			if (CalendarControl.getInstance().isUTC() == false) {
				str += String.format("%s  (%s UTC)", CalendarControl.getInstance().getTZCode(true), PamCalendar.formatDateTime2(timeMillis, false));
			}
			cursorPos.setText(str);
		}
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#getComponent()
	 */
	@Override
	public JComponent getComponent() {
		return panel;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#canHide()
	 */
	@Override
	public boolean canHide() {
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#showComponent(boolean)
	 */
	@Override
	public void showComponent(boolean visible) {
		
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#getName()
	 */
	@Override
	public String getName() {
		return "Data Summary";
	}


}
