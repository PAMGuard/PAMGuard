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
	
	private TethysDeploymentsTable tethysDeploymentsTable;

	public DeploymentsPanel(TethysControl tethysControl) {
		super(tethysControl);
		pamDeploymentsTable = new PAMGuardDeploymentsTable(tethysControl);
		tethysDeploymentsTable = new TethysDeploymentsTable(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Deployment information"));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.add(pamDeploymentsTable.getComponent());
		splitPane.add(tethysDeploymentsTable.getComponent());
		mainPanel.add(splitPane,BorderLayout.CENTER);
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				splitPane.setDividerLocation(0.6);
			}
		});
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}



}
