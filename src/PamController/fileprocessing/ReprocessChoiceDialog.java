package PamController.fileprocessing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamAlignmentPanel;

public class ReprocessChoiceDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private static ReprocessChoiceDialog singleInstance;
	
	private JRadioButton[] choiceButtons; 
	
	private ReprocessStoreChoice chosenChoice = null;

	private StoreChoiceSummary choiceSummary;
	
	private ReprocessChoiceDialog(Window parentFrame, StoreChoiceSummary choiceSummary) {
		super(parentFrame, "Existing Output Data", false);
		this.choiceSummary = choiceSummary;
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBorder(new TitledBorder("Data Summary"));
		mainPanel.add(infoPanel, BorderLayout.NORTH);
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		String inStr = String.format("Input data dates: %s to %s", PamCalendar.formatDBDateTime(choiceSummary.getInputStartTime()),
				PamCalendar.formatDBDateTime(choiceSummary.getInputEndTime()));
		infoPanel.add(new JLabel(inStr), c);
		c.gridy++;
		String outStr = String.format("Output data dates: %s to %s", PamCalendar.formatDBDateTime(choiceSummary.getOutputStartTime()),
				PamCalendar.formatDBDateTime(choiceSummary.getOutputEndTime()));
		infoPanel.add(new JLabel(outStr), c);
		String stateStr;
		if (choiceSummary.isProcessingComplete()) {
			stateStr = "Processing appears to be complete";
		}
		else {
			stateStr = "Processing appears to be partially complete";
		}
		c.gridy++;
		infoPanel.add(new JLabel(stateStr), c);
		
		
		JPanel choicePanel = new PamAlignmentPanel(new GridBagLayout(), BorderLayout.WEST);
		choicePanel.setBorder(new TitledBorder("Chose what to do"));
		c = new PamGridBagContraints();
		mainPanel.add(BorderLayout.SOUTH, choicePanel);
		List<ReprocessStoreChoice> userChoices = choiceSummary.getChoices();
		choiceButtons = new JRadioButton[userChoices.size()];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < userChoices.size(); i++) {
			ReprocessStoreChoice aChoice = userChoices.get(i);
			choiceButtons[i] = new JRadioButton(aChoice.toString());
			choiceButtons[i].setToolTipText(aChoice.getToolTip());
			choicePanel.add(choiceButtons[i], c);
			c.gridy++;
		}
		setDialogComponent(mainPanel);
		getCancelButton().setVisible(false);
	}
	
	public static ReprocessStoreChoice showDialog(Window parentFrame, StoreChoiceSummary choices) {
//		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ReprocessChoiceDialog(parentFrame, choices);
//		}
		singleInstance.setVisible(true);
		return singleInstance.chosenChoice;
	}

	@Override
	public boolean getParams() {
		List<ReprocessStoreChoice> userChoices = choiceSummary.getChoices();
		for (int i = 0; i < choiceButtons.length; i++) {
			if (choiceButtons[i].isSelected()) {
				chosenChoice = userChoices.get(i);
				break;
			}
		}
		if (chosenChoice == ReprocessStoreChoice.OVERWRITEALL) {
			String w = "Are you sure you want to delete / overwrite all existing output data ?";
			int ans = WarnOnce.showWarning("Overwrite existing data", w, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return false;
			}
		}
		return chosenChoice != null;
	}

	@Override
	public void cancelButtonPressed() {
		chosenChoice = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
