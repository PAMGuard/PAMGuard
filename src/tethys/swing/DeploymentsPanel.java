package tethys.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import tethys.TethysControl;

public class DeploymentsPanel extends TethysGUIPanel {
	
	private JPanel mainPanel;
	
	private PAMGuardDeploymentsTable pamDeploymentsTable;
	
	DeploymentExportPanel exportPanel;
//	private TethysDeploymentsTable tethysDeploymentsTable;

	public DeploymentsPanel(TethysControl tethysControl) {
		super(tethysControl);
		pamDeploymentsTable = new PAMGuardDeploymentsTable(tethysControl);
		exportPanel = new DeploymentExportPanel(tethysControl, pamDeploymentsTable);
		pamDeploymentsTable.addObserver(exportPanel);
//		tethysDeploymentsTable = new TethysDeploymentsTable(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Deployment information"));
//		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		splitPane.add(pamDeploymentsTable.getComponent());
//		splitPane.add(tethysDeploymentsTable.getComponent());
//		mainPanel.add(splitPane,BorderLayout.CENTER);
//		SwingUtilities.invokeLater(new Runnable() {
//			
//			@Override
//			public void run() {
//				splitPane.setDividerLocation(0.6);
//			}
//		});
		mainPanel.add(BorderLayout.CENTER, pamDeploymentsTable.getComponent());
		mainPanel.add(BorderLayout.EAST, exportPanel.getComponent());
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}



}
