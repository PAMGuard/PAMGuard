package tethys.calibration.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import PamView.panel.PamPanel;
import tethys.TethysControl;
import tethys.calibration.CalibrationHandler;
import tethys.swing.TethysGUIPanel;

public class CalibrationsMainPanel extends TethysGUIPanel {

	private CalibrationHandler calibrationHandler;
	
	private CalibrationsTable calibrationsTable;
	
	private JPanel mainPanel;
	
	private JPanel ctrlPanel;
	
	private JButton exportButton;
	
	private JLabel warning;

	public CalibrationsMainPanel(TethysControl tethysControl, CalibrationHandler calibrationHandler) {
		super(tethysControl);
		this.calibrationHandler = calibrationHandler;
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Instrument calibration information"));
		
		calibrationsTable = new CalibrationsTable(tethysControl, calibrationHandler);
		mainPanel.add(BorderLayout.CENTER, calibrationsTable.getComponent());
		
		ctrlPanel = new PamPanel(new BorderLayout());
		exportButton = new JButton("Export ...");
		ctrlPanel.add(BorderLayout.WEST, exportButton);
		warning = new JLabel();
		ctrlPanel.add(BorderLayout.CENTER, warning);
		mainPanel.add(BorderLayout.NORTH, ctrlPanel);
		exportButton.setToolTipText("Export calibration data to database");
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportCalibrations();
			}
		});
	}

	protected void exportCalibrations() {
		calibrationHandler.exportAllCalibrations();
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

}
