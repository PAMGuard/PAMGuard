package metadata.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import metadata.PamguardMetaData;
import nilus.Deployment;
import tethys.swing.export.DescriptionTypePanel;
import tethys.swing.export.ResponsiblePartyPanel;

public class MetaDataDialog extends PamDialog {
	
	private static MetaDataDialog singleInstance;
	
	private PamguardMetaData pamguardMetaData;
	
	private DescriptionTypePanel descriptionPanel;
	
	private JTextField project, site, cruise, region;

	private ResponsiblePartyPanel responsiblePanel;

	private MetaDataDialog(Window parentFrame) {
		super(parentFrame, "Project information", false);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		
		descriptionPanel = new DescriptionTypePanel(null, false, false, false);
		descriptionPanel.getMainPanel().setPreferredSize(new Dimension(400,300));
		
		JPanel projectPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		projectPanel.add(new JLabel("Project Name ", JLabel.RIGHT), c);
		c.gridx++;
		projectPanel.add(project = new JTextField(40), c);
		c.gridx = 0;
		c.gridy++;
		projectPanel.add(new JLabel("Region ", JLabel.RIGHT), c);
		c.gridx++;
		projectPanel.add(region = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		projectPanel.add(new JLabel("Cruise name ", JLabel.RIGHT), c);
		c.gridx++;
		projectPanel.add(cruise = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		projectPanel.add(new JLabel("Site ", JLabel.RIGHT), c);
		c.gridx++;
		projectPanel.add(site = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		
		responsiblePanel = new ResponsiblePartyPanel();
		JPanel northPanel = new JPanel();
		WestAlignedPanel wp;
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
		northPanel.add(wp = new WestAlignedPanel(projectPanel));
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
		cruise.setText(deployment.getCruise());
		region.setText(deployment.getRegion());
		site.setText(deployment.getSite());
		project.setText(deployment.getProject());
	}

	@Override
	public boolean getParams() {
		Deployment deployment = pamguardMetaData.getDeployment();
		boolean ok = descriptionPanel.getParams(deployment.getDescription());
		ok &= responsiblePanel.getParams(deployment.getMetadataInfo().getContact());
		deployment.setCruise(cruise.getText());
		deployment.setRegion(region.getText());
		deployment.setSite(site.getText());
		deployment.setProject(project.getText());
		
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
