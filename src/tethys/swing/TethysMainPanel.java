package tethys.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import tethys.TethysControl;
import tethys.calibration.swing.CalibrationsPanel;

public class TethysMainPanel extends TethysGUIPanel {

	private TethysControl tethysControl;

	private JPanel mainPanel;
	
	private TethysConnectionPanel connectionPanel;
	
	private DatablockSynchPanel datablockSynchPanel;
	
	private DeploymentsPanel deploymentsPanel;
	
	private DatablockDetectionsPanel datablockDetectionsPanel;
	
	private DetectionsExportPanel detectionsExportPanel;

	private CalibrationsPanel calibrationPanel;
	
	public TethysMainPanel(TethysControl tethysControl) {
		super(tethysControl);
		this.tethysControl = tethysControl;
		mainPanel = new JPanel(new BorderLayout());
		connectionPanel = new TethysConnectionPanel(tethysControl);
		datablockDetectionsPanel = new DatablockDetectionsPanel(tethysControl);
		datablockSynchPanel = new DatablockSynchPanel(tethysControl);
		deploymentsPanel = new DeploymentsPanel(tethysControl);
		detectionsExportPanel = new DetectionsExportPanel(tethysControl);
		datablockSynchPanel.addTableObserver(detectionsExportPanel);
		datablockSynchPanel.addTableObserver(datablockDetectionsPanel);
		calibrationPanel = new CalibrationsPanel(tethysControl, tethysControl.getCalibrationHandler());
		
		JSplitPane southwestSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JPanel southEastPanel = new JPanel(new BorderLayout());
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(BorderLayout.CENTER, connectionPanel.getComponent());
		northPanel.add(BorderLayout.WEST, new TethysImagePanel(100));
		mainPanel.add(BorderLayout.NORTH, northPanel);
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//		splitPane.set
		mainPanel.add(BorderLayout.CENTER, splitPane);
//		mainPanel.add(BorderLayout.CENTER, datablockSynchPanel.getComponent());
		JPanel splitNorth = new JPanel(new BorderLayout());
		splitNorth.add(BorderLayout.WEST, calibrationPanel.getComponent());
		splitNorth.add(deploymentsPanel.getComponent());
		splitPane.add(splitNorth);
		southwestSplit.add(datablockSynchPanel.getComponent());
		southwestSplit.add(southEastPanel);
		southEastPanel.add(datablockDetectionsPanel.getComponent(), BorderLayout.CENTER);
		southEastPanel.add(detectionsExportPanel.getComponent(), BorderLayout.WEST);
		splitPane.add(southwestSplit);
		SwingUtilities.invokeLater(new Runnable() {
			// these only work if called after display is visible
			@Override
			public void run() {
				splitPane.setDividerLocation(0.5);
				southwestSplit.setDividerLocation(0.5);
			}
		});
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}

	@Override
	public JComponent getComponent() {
		return getMainPanel();
	}
	
	
}
