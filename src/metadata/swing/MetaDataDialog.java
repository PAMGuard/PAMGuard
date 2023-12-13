package metadata.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import metadata.PamguardMetaData;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.deployment.swing.ProjectInformationPanel;
import tethys.swing.NewProjectDialog;
import tethys.swing.SelectProjectDialog;
import tethys.swing.export.DescriptionTypePanel;
import tethys.swing.export.ResponsiblePartyPanel;

public class MetaDataDialog extends PamDialog {
	
	private static MetaDataDialog singleInstance;
	
	private PamguardMetaData pamguardMetaData;
	
	private DescriptionTypePanel descriptionPanel;
	
	private ProjectInformationPanel projectInformationPanel;

	private ResponsiblePartyPanel responsiblePanel;
	
	private TethysControl tethysControl;

	private MetaDataDialog(Window parentFrame) {
		super(parentFrame, "Project information", false);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		
		tethysControl = (TethysControl) PamController.getInstance().findControlledUnit(TethysControl.unitType);
		
		projectInformationPanel = new ProjectInformationPanel(parentFrame, null);
		descriptionPanel = new DescriptionTypePanel(null, false, false, false);
		descriptionPanel.getMainPanel().setPreferredSize(new Dimension(400,300));
		
		
//		JPanel projectPanel = new JPanel(new GridBagLayout());
		
		responsiblePanel = new ResponsiblePartyPanel();
		JPanel northPanel = new JPanel();
		WestAlignedPanel wp;
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
		northPanel.add(wp = new WestAlignedPanel(projectInformationPanel.getMainPanel()));
		wp.setBorder(new TitledBorder("General project information"));
		northPanel.add(wp = new WestAlignedPanel(responsiblePanel.getMainPanel()));
		wp.setBorder(new TitledBorder("Contact information"));

//		mainPanel.add(BorderLayout.CENTER, descriptionPanel.getMainPanel());
//		mainPanel.add(BorderLayout.NORTH, northPanel);
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.add(northPanel, "General");
		tabbedPane.add(descriptionPanel.getMainPanel(), "Description");
		
		setResizable(true);
		
		setDialogComponent(mainPanel);
	}
	

	

	public static PamguardMetaData showDialog(Window frame, PamguardMetaData pamguardMetaData) {
		singleInstance = new MetaDataDialog(frame);
		singleInstance.setParams(pamguardMetaData);
		singleInstance.setVisible(true);
		return singleInstance.pamguardMetaData;
	}

	private void setParams(PamguardMetaData pamguardMetaData) {
		this.pamguardMetaData = pamguardMetaData;
		Deployment deployment = pamguardMetaData.getDeployment();
		descriptionPanel.setParams(deployment.getDescription());
		responsiblePanel.setParams(deployment.getMetadataInfo().getContact());
	}

	@Override
	public boolean getParams() {
		Deployment deployment = pamguardMetaData.getDeployment();
		boolean ok = descriptionPanel.getParams(deployment.getDescription());
		ok &= responsiblePanel.getParams(deployment.getMetadataInfo().getContact());
		
		if (tethysControl != null) {
			tethysControl.sendStateUpdate(new TethysState(StateType.NEWPROJECTSELECTION));
		}
		return ok;
	}

	@Override
	public void cancelButtonPressed() {
		pamguardMetaData = null;
	}

	@Override
	public void restoreDefaultSettings() {
		
	}

}
