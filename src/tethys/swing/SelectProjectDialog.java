package tethys.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import tethys.TethysControl;

public class SelectProjectDialog extends PamDialog {
	
	private String project;
	
	private JComboBox<String> comboBox;

	private SelectProjectDialog(Window parentFrame, List<String> projects, String topOne) {
		super(parentFrame, "Projects", topOne != null & topOne.length() > 0);
		this.project = topOne;
		
		comboBox = new JComboBox<String>();
		JPanel mainPanel = new JPanel(new BorderLayout());
//		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Project names"));
		mainPanel.add(comboBox, BorderLayout.CENTER);
		
		if (project != null) {
			comboBox.addItem(topOne);
		}
		for (String name : projects) {
			comboBox.addItem(name);
		}
		
		setDialogComponent(mainPanel);
	}

	public static String showDialog(Window parentFrame, TethysControl tethysControl, String topOne) {
		ArrayList<String> projects = tethysControl.getDbxmlQueries().getProjectNames();
		return showDialog(parentFrame, projects, topOne, null);
	}

	public static String showDialog(Window parentFrame, List<String> projects, String topOne, Point point) {
		if (topOne != null & topOne.length() == 0) {
			topOne = null;
		}
		SelectProjectDialog dialog = new SelectProjectDialog(parentFrame, projects, topOne);
		if (point != null) {
			dialog.setLocation(point);
		}
		dialog.setVisible(true);
				
		return dialog.project;
	}

	@Override
	public boolean getParams() {
		project = (String) comboBox.getSelectedItem();
		return (project != null & project.length()>0);
	}

	@Override
	public void cancelButtonPressed() {
		project = null;
	}

	@Override
	public void restoreDefaultSettings() {
		if (project != null) {
			comboBox.setSelectedItem(project);
		}
	}

}
