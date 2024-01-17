package tethys.deployment.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import PamUtils.PamCalendar;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import dataMap.OfflineDataMap;
import tethys.TethysControl;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.TrackInformation;

public class DeploymentTrackCard extends PamWizardCard<DeploymentExportOpts> {

	private static final long serialVersionUID = 1L;
	
	private TethysControl tethysControl;

	private TrackInformation trackInfo;
	
	private JTextField totalPoints, startDate, endDate, highestRate;
	
	private JTextField exportInterval, exportCount;

	public DeploymentTrackCard(PamWizard pamWizard, TethysControl tethysControl, TrackInformation trackInfo) {
		super(pamWizard, "Track Data");
		this.tethysControl = tethysControl;
		this.trackInfo = trackInfo;
		JPanel trackPanel = new JPanel();
		WestAlignedPanel wp = new WestAlignedPanel(trackPanel);
		wp.setBorder(new TitledBorder("Track data summary"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(wp);
		trackPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 1;
		trackPanel.add(new JLabel("PAMGuard data content .... ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Track Start ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(startDate = new TrackField(20), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Track End ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(endDate = new TrackField(20), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Total Points ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(totalPoints = new TrackField(20), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Interval ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(highestRate = new TrackField(20), c);
		
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 1;
		trackPanel.add(new JLabel("Export .... ", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		trackPanel.add(new JLabel("Export interval ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(exportInterval = new JTextField(12), c);
		c.gridx = 0;
		c.gridy++;
		trackPanel.add(new JLabel("Estimated elements ", JLabel.RIGHT), c);
		c.gridx++;
		trackPanel.add(exportCount = new TrackField(12), c);
		c.gridx = 0;
		c.gridy++;
		
		IntervalListener il = new IntervalListener();
//		exportInterval.addActionListener(il);
//		exportInterval.addKeyListener(il);
//		exportInterval.addFocusListener(il);
		exportInterval.getDocument().addDocumentListener(il);
		
		
//		c.gridx++;
//		trackPanel.add(new JLabel(" per minute ", JLabel.LEFT), c);
//		c.gridy++;
		
	}
	
	private class IntervalListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateExportCount();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateExportCount();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateExportCount();
		}
		
	}
	
	private class TrackField extends JTextField {

		/**
		 * @param columns
		 */
		public TrackField(int columns) {
			super(columns);
			setEditable(false);
		}
		
	}

	@Override
	public boolean getParams(DeploymentExportOpts cardParams) {
		try {
			cardParams.trackPointInterval = Double.valueOf(exportInterval.getText());
		}
		catch (Exception e) {
			return getPamWizard().showWarning("Invalid track point interval");
		}
		return true;
	}

	public void updateExportCount() {
		OfflineDataMap dataMap = trackInfo.getGpsDataMap();
		if (dataMap == null) {
			return;
		}
		try {
//			System.out.println(exportInterval.getText());
			double intval = Double.valueOf(exportInterval.getText());
			double highRate = trackInfo.getGPSDataRate();
			int nCount = dataMap.getDataCount();
			int newEst = (int) Math.round(Math.min(nCount/(intval*highRate), nCount));
			exportCount.setText(String.format("%d", newEst));
		}
		catch (Exception e) {
			exportCount.setText(null);
		}
		
	}

	@Override
	public void setParams(DeploymentExportOpts cardParams) {
		OfflineDataMap dataMap = trackInfo.getGpsDataMap();
		if (dataMap == null) {
			return;
		}
		startDate.setText(PamCalendar.formatDBDateTime(dataMap.getFirstDataTime()));
		endDate.setText(PamCalendar.formatDBDateTime(dataMap.getLastDataTime()));
		totalPoints.setText(String.format("%d", dataMap.getDataCount()));
		double rate = trackInfo.getGPSDataRate();
		highestRate.setText(PamCalendar.formatDuration((long) (1000/rate)));

	}

}
