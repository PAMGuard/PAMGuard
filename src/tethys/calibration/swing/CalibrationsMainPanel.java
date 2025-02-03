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
import tethys.TethysState;
import tethys.calibration.CalibrationHandler;
import tethys.deployment.PInstrument;
import tethys.swing.TethysExportPanel;
import tethys.swing.TethysGUIPanel;
import tethys.swing.TippedButton;

public class CalibrationsMainPanel extends TethysExportPanel {

	private CalibrationHandler calibrationHandler;
	
	private CalibrationsTable calibrationsTable;
	
//	private JPanel mainPanel;
//	
//	private JPanel ctrlPanel;
//	
//	private TippedButton exportButton;
//	
//	private JLabel warning;

	public CalibrationsMainPanel(TethysControl tethysControl, CalibrationHandler calibrationHandler) {
		super(tethysControl, calibrationHandler, false);
		this.calibrationHandler = calibrationHandler;
		JPanel mainPanel = getMainPanel();
//		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Instrument calibration information"));
		
		calibrationsTable = new CalibrationsTable(tethysControl, calibrationHandler);
		mainPanel.add(BorderLayout.CENTER, calibrationsTable.getComponent());
		
//		ctrlPanel = new PamPanel(new BorderLayout());
//		exportButton = new TippedButton("Export ...", "Export calibration data to database");
//		ctrlPanel.add(BorderLayout.WEST, exportButton);
//		warning = new JLabel();
//		ctrlPanel.add(BorderLayout.CENTER, warning);
//		mainPanel.add(BorderLayout.NORTH, ctrlPanel);
//		exportButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				exportCalibrations();
//			}
//		});
	}

	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		enableControls();
	}

	private void enableControls() {
		if (getTethysControl().isServerOk() == false) {
			disableExport("Tethys Server not running");
			return;
		}
		if (calibrationHandler.isHydrophoneNamed() == false) {
			disableExport("Can't export calibrations until the Hydrophone array has been correctly named");
			return;
		};
		enableExport(true);
	}



	@Override
	protected void exportButtonPressed(ActionEvent e) {
		calibrationHandler.exportAllCalibrations();
	}

	@Override
	protected void optionsButtonPressed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
