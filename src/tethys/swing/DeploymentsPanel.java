package tethys.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.panel.PamPanel;
import tethys.TethysControl;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.RecordingPeriod;

public class DeploymentsPanel extends TethysGUIPanel implements DeploymentTableObserver {
	
	private JPanel mainPanel;
	
	private PAMGuardDeploymentsTable pamDeploymentsTable;
	
	private DeploymentExportPanel exportPanel;
	
	private JButton exportButton, optionsButton;
//	private TethysDeploymentsTable tethysDeploymentsTable;
	private JLabel exportWarning;

	public DeploymentsPanel(TethysControl tethysControl) {
		super(tethysControl);
		DeploymentHandler deploymentHandler = tethysControl.getDeploymentHandler();
		pamDeploymentsTable = new PAMGuardDeploymentsTable(tethysControl);
		exportPanel = new DeploymentExportPanel(tethysControl, pamDeploymentsTable);
		pamDeploymentsTable.addObserver(exportPanel);
//		tethysDeploymentsTable = new TethysDeploymentsTable(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Recording periods and deployment information"));
		pamDeploymentsTable.addObserver(this);
		pamDeploymentsTable.addObserver(deploymentHandler);
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
		JPanel ctrlPanel = new PamPanel(new BorderLayout());
		JPanel ctrlButtons = new JPanel();
		ctrlButtons.setLayout(new BoxLayout(ctrlButtons, BoxLayout.X_AXIS));
		optionsButton = new JButton("Options ...");
		exportButton = new JButton("Export ...");
		tethysControl.getEnabler().addComponent(exportButton);
		ctrlButtons.add(optionsButton);
		ctrlButtons.add(exportButton);
		ctrlPanel.add(BorderLayout.WEST, ctrlButtons);
		
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTethysControl().getDeploymentHandler().showOptions(null);
			}
		});
		
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportDeployments();
			}
		});
		exportWarning = new JLabel(" ");
		ctrlPanel.add(BorderLayout.CENTER, exportWarning);
		
		mainPanel.add(BorderLayout.CENTER, pamDeploymentsTable.getComponent());
		mainPanel.add(BorderLayout.NORTH, ctrlPanel);
//		mainPanel.add(BorderLayout.EAST, exportPanel.getComponent());
		exportButton.setEnabled(false);
	}

	protected void exportDeployments() {
		getTethysControl().getDeploymentHandler().exportDeployments();
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void selectionChanged() {
		enableExportButton();
	}

	private void enableExportButton() {
		ArrayList<RecordingPeriod> selected = pamDeploymentsTable.getSelectedPeriods();
		// and see if any warnings are needed: basically if anything selected has an output.
		boolean existing = false;
		for (RecordingPeriod aPeriod: selected) {
			if (aPeriod.getMatchedTethysDeployment() != null) {
				existing = true;
				break;
			}
		}
		String warning = null;
		if (existing) {
			warning = "  One or more deployment documents already exist. These must be deleted prior to exporting new documents";
			exportWarning.setText(warning);
		}

		exportButton.setEnabled(selected.size()>0 & existing == false);
	}



}
