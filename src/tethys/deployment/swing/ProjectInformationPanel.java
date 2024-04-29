package tethys.deployment.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamGridBagContraints;
import metadata.PamguardMetaData;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.swing.NewProjectDialog;
import tethys.swing.SelectProjectDialog;
import tethys.tooltips.TethysTips;

/**
 * Panel for entering project information
 * @author dg50
 *
 */
public class ProjectInformationPanel {

	private JPanel projectPanel;

	private JTextField project, site, cruise, region;
	
	private JButton newProject, selectProject;
	
	private TethysControl tethysControl;

	private Deployment deployment;

	private Window owner;
	
	public ProjectInformationPanel(Window owner, String title) {
		super();
		this.owner = owner;
		
		tethysControl = (TethysControl) PamController.getInstance().findControlledUnit(TethysControl.unitType);

		int txtWidth = 1;
		if (tethysControl != null) {
			txtWidth = 3;
		}
		projectPanel = new JPanel(new GridBagLayout());
		if (title != null) {
			projectPanel.setBorder(new TitledBorder(title));
		}
		GridBagConstraints c = new PamGridBagContraints();
		projectPanel.add(new JLabel("Project Name ", JLabel.RIGHT), c);
		c.gridx++;
		projectPanel.add(project = new JTextField(30), c);
		if (tethysControl != null) {
			c.gridx++;
			projectPanel.add(selectProject = new JButton("Select"));
			c.gridx++;
			projectPanel.add(newProject = new JButton("New"));
		}
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		projectPanel.add(new JLabel("Region ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = txtWidth;
		projectPanel.add(region = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		projectPanel.add(new JLabel("Cruise name ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = txtWidth;
		projectPanel.add(cruise = new JTextField(40), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		projectPanel.add(new JLabel("Site ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = txtWidth;
		projectPanel.add(site = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		

		if (newProject != null) {
			newProject.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selNewProject(e);
				}
			});
		}
		if (selectProject != null) {
			selectProject.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selProjectPressed(e);
				}
			});
		}
		
		project.setToolTipText(TethysTips.findTip(Deployment.class, "Project"));
		cruise.setToolTipText(TethysTips.findTip(Deployment.class, "Cruise"));
		region.setToolTipText(TethysTips.findTip(Deployment.class, "Region"));
		site.setToolTipText(TethysTips.findTip(Deployment.class, "Site"));
		
		
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return projectPanel;
	}
	
	public void setParams(Deployment deployment) {
		this.deployment = deployment;
		cruise.setText(deployment.getCruise());
		region.setText(deployment.getRegion());
		site.setText(deployment.getSite());
		project.setText(deployment.getProject());
	}
	
	public boolean getParams(Deployment deployment) {
		if (deployment == null) {
			return false;
		}
		deployment.setCruise(cruise.getText());
		deployment.setRegion(region.getText());
		deployment.setSite(site.getText());
		deployment.setProject(project.getText());
		return true;
	}
	/**
	 * Select a new project, uses a dialog from Tethys. Only enabled
	 * when the tethys database is present to allow this. 
	 * @param e
	 */
	protected void selNewProject(ActionEvent e) {
		if (tethysControl == null) {
			return;
		}
		getParams(deployment);
		Deployment newDeployment = NewProjectDialog.showDialog(owner, tethysControl, deployment);
		if (newDeployment != null) {
			deployment.setProject(newDeployment.getProject());
			deployment.setRegion(newDeployment.getRegion());
		}
		setParams(deployment);
	}
	
	protected void selProjectPressed(ActionEvent e) {
		if (tethysControl == null) {
			return;
		}
		getParams(deployment);
		// will this be fast enough, or do we need to get Tethys to hold this list in memory ? 
		ArrayList<String> projectNames = tethysControl.getDbxmlQueries().getProjectNames();
		if (projectNames.size() < 12) {
			showAsMenu(projectNames);
		}
		else {
			showAsDialog(projectNames);
		}
		
	}
	
	private void showAsDialog(ArrayList<String> projectNames) {
		Point p = selectProject.getLocationOnScreen();
		String selName = SelectProjectDialog.showDialog(owner, projectNames, project.getText(), p);
		if (selName != null) {
			project.setText(selName);
		}
	}

	private void showAsMenu(ArrayList<String> projectNames) {
		String currentName = project.getText();
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem;
		if (currentName != null && currentName.length()>0) {
			addProjMenuItem(popMenu, currentName);
		}
		for (String projName : projectNames) {
			if (projName.equals(currentName)) {
				continue;
			}
			addProjMenuItem(popMenu, projName);
		}
		
		popMenu.show(selectProject, selectProject.getWidth()/2, selectProject.getHeight()/2);
	}

	private void addProjMenuItem(JPopupMenu popMenu, String projectName) {
		JMenuItem menuItem = new JMenuItem(projectName);
		menuItem.addActionListener(new SelectProject(projectName));
		popMenu.add(menuItem);
	}
	
	private class SelectProject implements ActionListener {

		private String projectName;
		
		/**
		 * @param projectName
		 */
		public SelectProject(String projectName) {
			this.projectName = projectName;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			project.setText(projectName);
		}
		
	}

}
