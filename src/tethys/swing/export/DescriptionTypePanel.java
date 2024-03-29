package tethys.swing.export;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import PamView.PamGui;
import PamView.dialog.PamDialog;
import nilus.DescriptionType;
import tethys.niluswraps.WrappedDescriptionType;
import tethys.tooltips.TethysTips;

/**
 * Panel containing the three test entry fields for nilus.DescriptionType
 * @author dg50
 *
 */
public class DescriptionTypePanel {

	private JTextArea tObjectives, tAbstract, tMethod;
	
	private JPanel mainPanel;

	private boolean requireObjective;

	private boolean requireAbstract;

	private boolean requireMethod;
	
	private static final int ctrlWidth = 40;
	
	public static final String objectivesTip = "What are the objectives of this effort?  Examples:\r\n"
			+ "Beamform to increase SNR for detection.\r\n"
			+ "Detect every click of a rare species.\r\n"
			+ "Verify data quality.";
	public static final String abstractTip = "Overview of effort.";
	public static final String methodTip = "High-level description of the method used.";
	
	public DescriptionTypePanel(String bordertitle, boolean requireObjective, boolean requireAbstract, boolean requireMethod) {
		this.requireObjective = requireObjective;
		this.requireAbstract = requireAbstract;
		this.requireMethod = requireMethod;
		
		mainPanel = new JPanel();
		if (bordertitle != null) {
			mainPanel.setBorder(new TitledBorder(bordertitle));
		}
		tObjectives = new JTextArea(12, ctrlWidth);
		tAbstract = new JTextArea(8, ctrlWidth);
		tMethod = new JTextArea(9, ctrlWidth);
		tObjectives.setLineWrap(true);
		tObjectives.setWrapStyleWord(true);
		tAbstract.setLineWrap(true);
		tAbstract.setWrapStyleWord(true);
		tMethod.setLineWrap(true);
		tMethod.setWrapStyleWord(true);
		
		tObjectives.setToolTipText(objectivesTip);
		tAbstract.setToolTipText(abstractTip);
		tMethod.setToolTipText(methodTip);
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		addScrollablePanel(tObjectives, "Objectives");
		addScrollablePanel(tAbstract, "Abstract");
		addScrollablePanel(tMethod, "Method");
		
		tObjectives.setToolTipText(TethysTips.Detections_Description_Objectives);
		tAbstract.setToolTipText(TethysTips.Detections_Description_Abstract);
		tMethod.setToolTipText(TethysTips.Detections_Description_Method);
	}

	private void addScrollablePanel(JTextArea textArea, String title) {
		// TODO Auto-generated method stub
//		mainPanel.add(new Label(title, JLabel.LEFT));
//		textArea.setMinimumSize(new Dimension(200, 200));
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new TitledBorder(title));
		scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().height/2, 0));
		mainPanel.add(scrollPane);
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void setParams(DescriptionType description) {
		if (description == null) {
			tObjectives.setText(null);
			tAbstract.setText(null);
			tMethod.setText(null);
		}
		else {
			tObjectives.setText(description.getObjectives());
			tAbstract.setText(description.getAbstract());
			tMethod.setText(description.getMethod());
		}
	}
	
	public boolean getParams(DescriptionType description) {
		Window f = PamGui.findComponentWindow(mainPanel);
		if (checkField(requireObjective, tObjectives) == false) {
			return PamDialog.showWarning(f, "Objectives", "The objectives field must be completed");
		}
		if (checkField(requireAbstract, tAbstract) == false) {
			return PamDialog.showWarning(f, "Abstract", "The abstract field must be completed");
		}
		if (checkField(requireMethod, tMethod) == false) {
			return PamDialog.showWarning(f, "Method", "The method field must be completed");
		}

		description.setObjectives(tObjectives.getText());
		description.setAbstract(tAbstract.getText());
		description.setMethod(tMethod.getText());
		
		return true;
	}

	private boolean checkField(boolean required, JTextArea field) {
		if (required == false) {
			return true;
		}
		String txt = field.getText();
		if (txt == null || txt.length() == 0) {
			return false;
		}
		return true;
	}
}
