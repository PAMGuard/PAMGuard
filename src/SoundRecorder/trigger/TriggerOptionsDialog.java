package SoundRecorder.trigger;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import SoundRecorder.RecorderControl;

public class TriggerOptionsDialog extends PamDialog {

	
	private static TriggerOptionsDialog singleInstance;
	
	private RecorderTriggerData rtData;
	
	JTextField preSeconds, postSeconds;
	JTextField minDetections, countTime;
	JTextField maxLength, minGap, dayBudget, remainingBudget;

	private JButton resetButton, triggerOptions;

	
	/**
	 * @param parentFrame
	 * @param title
	 * @param hasDefault
	 */
	private TriggerOptionsDialog(Window parentFrame) {
		super(parentFrame, "Trigger options", true);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		GridBagConstraints c;
		
		JPanel condPanel = new JPanel();
		condPanel.setBorder(new TitledBorder("Trigger"));
		condPanel.setLayout(new GridBagLayout());
		mainPanel.add(condPanel);
		c = new PamGridBagContraints();
		addComponent(condPanel, new JLabel("Minimum number of detections ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(condPanel, minDetections = new JTextField(5), c);
//		c.gridx++;
//		addComponent(condPanel, new JLabel(" in"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(condPanel, new JLabel("(in)   Count integration time ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(condPanel, countTime = new JTextField(5), c);
		c.gridx++;
		addComponent(condPanel, new JLabel(" s"), c);
		c.gridx = 1;
		c.gridwidth = 2;
		c.gridy++;
		addComponent(condPanel, triggerOptions = new JButton("More Options ..."), c);
		triggerOptions.addActionListener(new TriggerOptions());
		c.gridwidth = 1;
		
		
		
		JPanel timePanel = new JPanel();
		timePanel.setBorder(new TitledBorder("Timing"));
		mainPanel.add(timePanel);
		timePanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		addComponent(timePanel, new JLabel("Seconds to record before trigger ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(timePanel, preSeconds = new JTextField(5), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(timePanel, new JLabel("Seconds to record after trigger ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(timePanel, postSeconds = new JTextField(5), c);
		c.gridy++;
		c.gridx = 0;

		JPanel budgetPanel = new JPanel();
		budgetPanel.setBorder(new TitledBorder("Data Budget"));
		mainPanel.add(budgetPanel);
		budgetPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		addComponent(budgetPanel, new JLabel("Daily data budget ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, dayBudget = new JTextField(5), c);
		c.gridx++;
		addComponent(budgetPanel, new JLabel(" MBytes (0 = no limit)"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(budgetPanel, new JLabel("Remaining data budget ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, remainingBudget = new JTextField(5), c);
		remainingBudget.setEditable(false);
		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		addComponent(budgetPanel, resetButton = new JButton("Reset"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		resetButton.addActionListener(new ResetButton());
		c.gridx = 0;
		c.gridy++;
		addComponent(budgetPanel, new JLabel("Max single triggered recording length ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, maxLength = new JTextField(5), c);
		c.gridx++;
		addComponent(budgetPanel, new JLabel(" s (0 = no limit)"), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(budgetPanel, new JLabel("Min interval betwen recordings ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, minGap = new JTextField(5), c);
		c.gridx++;
		addComponent(budgetPanel, new JLabel(" s"), c);
		
		setDialogComponent(mainPanel);
	}
	
	public static RecorderTriggerData showDialog(Window parent, RecorderTriggerData rtData) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new TriggerOptionsDialog(parent);
		}
		singleInstance.rtData = rtData.clone();
		singleInstance.setTitle(rtData.getTriggerName() + " options");
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.rtData;
	}

	private void setParams() {
		minDetections.setText(String.format("%d", rtData.getMinDetectionCount()));
		countTime.setText(String.format("%d", rtData.getCountSeconds()));
		preSeconds.setText(String.format("%3.1f", rtData.getSecondsBeforeTrigger()));
		postSeconds.setText(String.format("%3.1f", rtData.getSecondsAfterTrigger()));
		dayBudget.setText(String.format("%d", rtData.getDayBudgetMB()));
		sayRemaining();
		maxLength.setText(String.format("%d", rtData.getMaxTotalTriggerLength()));
		minGap.setText(String.format("%d", rtData.getMinGapBetweenTriggers()));
		enableControls();
	}
	
	private void enableControls() {
		RecorderTrigger rt = RecorderControl.findRecorderTrigger(rtData.getTriggerName());
		triggerOptions.setEnabled(rt.hasOptions());
	}
	
	private void sayRemaining() {
		remainingBudget.setText(String.format("%d", rtData.getDayBudgetMB()-rtData.usedDayBudget/1024/1024));
	}

	@Override
	public boolean getParams() {
		try {
			rtData.setMinDetectionCount(Integer.valueOf(minDetections.getText()));
			rtData.setCountSeconds(Integer.valueOf(countTime.getText()));
			rtData.setSecondsBeforeTrigger(Double.valueOf(preSeconds.getText()));
			rtData.setSecondsAfterTrigger(Double.valueOf(postSeconds.getText()));
			rtData.setDayBudgetMB(Integer.valueOf(dayBudget.getText()));
			rtData.setMaxTotalTriggerLength(Integer.valueOf(maxLength.getText()));
			rtData.setMinGapBetweenTriggers(Integer.valueOf(minGap.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		rtData = null;
	}

	@Override
	public void restoreDefaultSettings() {
		RecorderTrigger rt = RecorderControl.findRecorderTrigger(rtData.getTriggerName());
		if (rt == null) {
			return;
		}
		rtData = rt.getDefaultTriggerData().clone();
		setParams();
	}

	private class ResetButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {	
			rtData.usedDayBudget = 0;
			sayRemaining();
		}
		
	}
	
	private class TriggerOptions implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {	
			RecorderTrigger rt = RecorderControl.findRecorderTrigger(rtData.getTriggerName());
			if (!rt.hasOptions()) {
				return;
			}
			rt.showOptionsDialog(getOwner(), rtData);
		}
		
	}
}
