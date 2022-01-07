package backupmanager.filter.alarm;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;
import alarm.AlarmControl;

public class AlarmFilterDialogPanel implements PamDialogPanel {
	
	private AlarmBackupFilter alarmBackupFilter;

	private JPanel mainPanel;
	
	private JCheckBox passAll;
	
	private AlarmPanel[] alarmPanels;
	
	public AlarmFilterDialogPanel(AlarmBackupFilter alarmBackupFilter) {
		super();
		this.alarmBackupFilter = alarmBackupFilter;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Alarm Filter Options"));
		JPanel topPanel = new PamAlignmentPanel(BorderLayout.WEST);
		topPanel.add(BorderLayout.WEST, passAll = new JCheckBox("Select Everything"));
		passAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		});
		
		ArrayList<PamControlledUnit> alarmControls = PamController.getInstance().findControlledUnits(AlarmControl.class);
		alarmPanels = new AlarmPanel[alarmControls.size()];
		JPanel centPanel = new JPanel();
		centPanel.setLayout(new BoxLayout(centPanel, BoxLayout.Y_AXIS));
		for (int i = 0; i < alarmControls.size(); i++) {
			alarmPanels[i] = new AlarmPanel((AlarmControl) alarmControls.get(i));
			centPanel.add(alarmPanels[i].getDialogComponent());
		}
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(centPanel, BorderLayout.CENTER);
		mainPanel.setToolTipText("Data to backup can be selected based on PAMGuard Alarm. Configure one or more alarms to control backup");
	}

	private void enableControls() {
		if (alarmPanels.length == 0) {
			passAll.setSelected(true);
			passAll.setEnabled(false);
		}
		else {
			passAll.setEnabled(true);
			for (int i = 0; i < alarmPanels.length; i++) {
				alarmPanels[i].enableAlarm();
			}
		}
	}
	
	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		AlarmFilterParams params = alarmBackupFilter.getAlarmFilterParams();
		passAll.setSelected(params.isPassEverything());
		for (int i = 0; i < alarmPanels.length; i++) {
			alarmPanels[i].setParams();
		}
		enableControls();
	}

	@Override
	public boolean getParams() {
		AlarmFilterParams params = alarmBackupFilter.getAlarmFilterParams();
		params.setPassEverything(passAll.isSelected());
		boolean ok = true;
		if (passAll.isSelected() == false) {
			for (int i = 0; i < alarmPanels.length; i++) {
				ok |= alarmPanels[i].getParams();
			}
		}
		return ok;
	}
	
	private class AlarmPanel implements PamDialogPanel {
		private JPanel mainPanel;
		
		private AlarmControl alarmControl;
		
		private JCheckBox enable;
		
		private JTextField prePeriod, postPeriod; 
		
		public AlarmPanel(AlarmControl alarmControl) {
			super();
			this.alarmControl = alarmControl;
			mainPanel = new PamAlignmentPanel(BorderLayout.WEST);
			mainPanel.setLayout(new GridBagLayout());
			mainPanel.setBorder(new TitledBorder(alarmControl.getUnitName()));
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 2;
			mainPanel.add(enable = new JCheckBox("Use this alarm"));
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy++;
			mainPanel.add(new JLabel("Time before alarm ", JLabel.RIGHT), c);
			c.gridx++;
			mainPanel.add(prePeriod = new JTextField(4), c);
			c.gridx++;
			mainPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
			c.gridx = 0;
			c.gridy++;
			mainPanel.add(new JLabel("Time after alarm ", JLabel.RIGHT), c);
			c.gridx++;
			mainPanel.add(postPeriod = new JTextField(4), c);
			c.gridx++;
			mainPanel.add(new JLabel(" seconds ", JLabel.LEFT), c);
			
			enable.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					enableAlarm();
				}
			});
		}

		protected void enableAlarm() {
			boolean e = passAll.isSelected() == false & enable.isSelected();
			enable.setEnabled(passAll.isSelected() == false);
			prePeriod.setEnabled(e);
			postPeriod.setEnabled(e);
		}

		@Override
		public JComponent getDialogComponent() {
			return mainPanel;
		}

		@Override
		public void setParams() {
			AlarmParamSet params = alarmBackupFilter.getAlarmFilterParams().getAlarmParamSet(alarmControl.getUnitName());
			enable.setSelected(params.useAlarm);
			prePeriod.setText(String.format("%3.1f", (double) params.prePeriod / 1000.));
			postPeriod.setText(String.format("%3.1f", (double) params.postPeriod / 1000.));
		}

		@Override
		public boolean getParams() {
			AlarmParamSet params = alarmBackupFilter.getAlarmFilterParams().getAlarmParamSet(alarmControl.getUnitName());
			params.useAlarm = enable.isSelected();
			if (params.useAlarm) {
				try {
					params.prePeriod = (long) (Double.valueOf(prePeriod.getText()) * 1000);
					params.postPeriod = (long) (Double.valueOf(postPeriod.getText()) * 1000);
				}
				catch (NumberFormatException e) {
					return PamDialog.showWarning(null, "Invalid number", "Invalid pre period or post period. Enter valid decimal numbers");
				}
			}
			return true;
		}
	}

}
