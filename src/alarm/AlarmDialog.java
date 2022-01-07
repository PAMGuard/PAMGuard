package alarm;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import alarm.actions.AlarmAction;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class AlarmDialog extends PamDialog {

	private static AlarmDialog singleInstance;
	private AlarmParameters alarmParameters;
	
	private SourcePanel sourcePanel;
	
	private JComboBox<String> countType;
	
	private JTextField alarmTime, alarmInterval;
	
	private JTextField[] alarmCount = new JTextField[AlarmParameters.COUNT_LEVELS];
	
	private JButton settingsButton;
	private AlarmControl alarmControl;
	
	private JCheckBox[] alarmActionBoxes;
	private JButton[] alarmActionButtons;
	private JTextField holdTime;
	
	private AlarmDialog(Window parentFrame, AlarmControl alarmControl) {
		super(parentFrame, alarmControl.getUnitName() + " Settings", false);
		this.alarmControl = alarmControl;
		JTabbedPane tabPane = new JTabbedPane();
		
		JPanel configPanel = new JPanel(new BorderLayout());
		sourcePanel = new SourcePanel(this, PamDataUnit.class, false, true);
		sourcePanel.addSelectionListener(new SourceSelection());
		JPanel northPanel = new JPanel(new GridBagLayout());
		northPanel.setBorder(new TitledBorder("Trigger source"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		addComponent(northPanel, sourcePanel.getPanel(), c);
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.gridy++;
		c.gridx = 1;
		addComponent(northPanel, settingsButton = new JButton("Settings..."), c);
		settingsButton.addActionListener(new SettingsButton());
		
		configPanel.add(BorderLayout.NORTH, northPanel);
		
		JPanel southPanel = new JPanel(new GridBagLayout());
		southPanel.setBorder(new TitledBorder("Alarm Count"));
		c = new PamGridBagContraints();
		addComponent(southPanel, new JLabel("Count Type ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 3;
		addComponent(southPanel, countType = new JComboBox<String>(), c);
		countType.addItem("Simple Counts");
		countType.addItem("Scored Values");
		countType.addItem("Single Measurements");
		countType.addActionListener(new CountTypeAction());
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		addComponent(southPanel, new JLabel("Count Time ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, alarmTime = new JTextField(5), c);
		c.gridx++;
		addComponent(southPanel, new JLabel(" (s) ", JLabel.RIGHT), c);
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			c.gridx = 0;
			c.gridy++;
			addComponent(southPanel, new JLabel(AlarmParameters.levelNames[i] + " Count ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(southPanel, alarmCount[i] = new JTextField(5), c);
		}
		c.gridy++;
		c.gridx = 0;
		addComponent(southPanel, new JLabel("Minimum gap ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, alarmInterval = new JTextField(5), c);
		c.gridx++;
		addComponent(southPanel, new JLabel(" (s) ", JLabel.RIGHT), c);
		configPanel.add(BorderLayout.SOUTH, southPanel);
		
		JPanel actionsPanel = new JPanel(new GridBagLayout());
		JPanel actionsOuterPanel = new JPanel();
		actionsOuterPanel.setLayout(new BoxLayout(actionsOuterPanel, BoxLayout.Y_AXIS));
		actionsOuterPanel.add(actionsPanel);
		actionsPanel.setBorder(new TitledBorder("Actions"));
		c = new PamGridBagContraints();
		ArrayList<AlarmAction> alarmActions = alarmControl.alarmActions;
		alarmActionBoxes = new JCheckBox[alarmActions.size()];
		alarmActionButtons = new JButton[alarmActions.size()];
		for (int i = 0; i < alarmActions.size(); i++) {
			c.gridx = 0;
			alarmActionBoxes[i] = new JCheckBox(alarmActions.get(i).getActionName());
			alarmActionBoxes[i].addActionListener(new AlarmActionSelect());
			addComponent(actionsPanel, alarmActionBoxes[i], c);
			if (alarmActions.get(i).hasSettings()) {
				c.gridx++;
				alarmActionButtons[i] = new JButton("Settings...");
				alarmActionButtons[i].addActionListener(new AlarmActionSettings(alarmActions.get(i)));
				addComponent(actionsPanel, alarmActionButtons[i], c);
			}
			c.gridy++;
		}
		alarmTime.setToolTipText("Alarm integration time in seconds");
		for (int i = 0; i < alarmCount.length; i++) {
			alarmCount[i].setToolTipText(String.format("Count for %s level alarm", AlarmParameters.levelNames[i]));
		}
		alarmInterval.setToolTipText("Minimum gap between repeat alarm actions");
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new TitledBorder("Data"));
		dataPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		dataPanel.add(new JLabel("Data hold time ", JLabel.RIGHT), c);
		c.gridx++;
		dataPanel.add(holdTime = new JTextField(5), c);
		c.gridx++;
		dataPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
		actionsOuterPanel.add(dataPanel);
		holdTime.setToolTipText("Time to hold alarm data in memory before deleting");
		
		JPanel veryOuterActionsPanel = new JPanel(new BorderLayout());
		veryOuterActionsPanel.add(BorderLayout.NORTH, actionsOuterPanel);
		
		tabPane.add("Configuration", configPanel);
		tabPane.add("Actions", veryOuterActionsPanel);
		
		setHelpPoint("utilities.Alarms.docs.Alarms_Overview");
		setDialogComponent(tabPane);
		
	}

	public static final AlarmParameters showDialog(Window parentFrame, AlarmControl alarmControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.alarmControl != alarmControl) {
			singleInstance = new AlarmDialog(parentFrame, alarmControl);
		}
		singleInstance.alarmParameters = alarmControl.alarmParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.alarmParameters;
	}
	
	private void setParams() {
		sourcePanel.setSourceList();
		sourcePanel.excludeDataBlock(alarmControl.getAlarmProcess().getAlarmDataBlock(), true);
		sourcePanel.setSource(alarmParameters.dataSourceName);
		countType.setSelectedIndex(alarmParameters.countType);
		alarmTime.setText(String.format("%3.1f", (double) alarmParameters.countIntervalMillis / 1000.));
		alarmInterval.setText(String.format("%3.1f", (double) alarmParameters.minAlarmIntervalMillis / 1000.));
		for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
			alarmCount[i].setText(String.format("%3.1f", alarmParameters.getTriggerCount(i)));
		}
		boolean[] enabledActions = alarmParameters.getEnabledActions();
		if (enabledActions == null) {
			enabledActions = new boolean[alarmActionBoxes.length];
		}
		int nAct = Math.min(enabledActions.length, alarmActionBoxes.length);
		for (int i = 0; i < nAct; i++) {
			alarmActionBoxes[i].setSelected(enabledActions[i]);
		}
		holdTime.setText(String.format("%d", alarmParameters.getHoldSeconds()));
		enableControls();
	}

	@Override
	public void cancelButtonPressed() {
		alarmParameters = null;
	}

	@Override
	public boolean getParams() {
		alarmParameters.dataSourceName = sourcePanel.getSourceName();
		if (alarmParameters.dataSourceName == null) {
			return showWarning("No data source selected");
		}
		try {
			alarmParameters.countType = countType.getSelectedIndex();
			alarmParameters.countIntervalMillis = (long) (Double.valueOf(alarmTime.getText()) * 1000.);
			alarmParameters.minAlarmIntervalMillis = (long) (Double.valueOf(alarmInterval.getText()) * 1000.);
			for (int i = 0; i < AlarmParameters.COUNT_LEVELS; i++) {
				alarmParameters.setTriggerCount(i, Double.valueOf(alarmCount[i].getText()));
			}
			int hs = Integer.valueOf(holdTime.getText());
			alarmParameters.setHoldSeconds(hs);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid number");
		}
		int nAct = alarmActionBoxes.length;
		boolean[] selActions = new boolean[nAct];
		for (int i = 0; i < nAct; i++) {
			selActions[i] = alarmActionBoxes[i].isSelected();
		}
		alarmParameters.setEnabledActions(selActions);
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	private void enableControls() {
		AlarmCounter ac = findAlarmCounter();
		boolean isSingles = countType.getSelectedIndex() == AlarmParameters.COUNT_SINGLES;
		if (ac == null || ac.hasOptions() == false) {
			settingsButton.setEnabled(false);
		}
		else {
			settingsButton.setEnabled(true);
		}
		alarmTime.setEnabled(!isSingles);
//		alarmInterval.setEnabled(!isSingles);
//		for (int i = 0; i < alarmCount.length; i++) {
//			if (alarmCount[i] != null) alarmCount[i].setEnabled(!isSingles);
//		}
		for (int i = 0; i < alarmActionBoxes.length; i++) {
			if (alarmActionButtons[i] != null) {
				alarmActionButtons[i].setEnabled(alarmActionBoxes[i].isSelected());
			}
		}
	}

	private class SettingsButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			AlarmCounter ac = findAlarmCounter();
			if (ac != null) {
				ac.showOptions(getOwner());
			}
		}
		
	}
	
	class SourceSelection implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	
	class CountTypeAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	
	class AlarmActionSelect implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
	}
	
	private AlarmCounter findAlarmCounter() {
		PamDataBlock alarmSource = sourcePanel.getSource();
		if (alarmSource == null) {
			return null;
		}
		if (AlarmDataSource.class.isAssignableFrom(alarmSource.getClass())) {
			AlarmCounterProvider alarmProvider = ((AlarmDataSource) alarmSource).getAlarmCounterProvider();
			if (alarmProvider != null) {
				return alarmProvider.getAlarmCounter(alarmControl);
			}
		}
		return new SimpleAlarmCounter(alarmControl, alarmSource);
		
	}
	
	private class AlarmActionSettings implements ActionListener {
		private AlarmAction alarmAction;

		public AlarmActionSettings(AlarmAction alarmAction) {
			super();
			this.alarmAction = alarmAction;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			alarmAction.setSettings(getOwner());
		}
		
	}
}
