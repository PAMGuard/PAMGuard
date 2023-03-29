package tethys.swing.export;

import java.awt.Dimension;
import java.awt.Label;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import nilus.DescriptionType;

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
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		addScrollablePanel(tObjectives, "Objectives");
		addScrollablePanel(tAbstract, "Abstract");
		addScrollablePanel(tMethod, "Method");
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
	}
	
	public boolean getParams(DescriptionType description) {
		if (checkField(requireObjective, tObjectives) == false) {
			return PamDialog.showWarning(null, "Objectives", "The objectives field must be competed");
		}
		if (checkField(requireAbstract, tAbstract) == false) {
			return PamDialog.showWarning(null, "Abstract", "The abstract field must be competed");
		}
		if (checkField(requireMethod, tMethod) == false) {
			return PamDialog.showWarning(null, "Method", "The method field must be competed");
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
