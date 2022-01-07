package dbht.alarm;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import dbht.DbHtControl;
import alarm.AlarmCounter;

public class DbHtAlarmParamsDialog extends PamDialog {

	private static DbHtAlarmParamsDialog singleInstance;
	private DbHtAlarmParameters dbHtAlarmParameters;
	private JRadioButton[] measures = new JRadioButton[DbHtControl.NMEASURES];
	private DbHtAlarmCounter dbHtAlarmCounter;
	
	private DbHtAlarmParamsDialog(Window parentFrame, DbHtAlarmCounter alarmCounter) {
		super(parentFrame, alarmCounter.getUnitName() + " settings", true);
		this.dbHtAlarmCounter = alarmCounter;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new TitledBorder("Select measure"));
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			measures[i] = new JRadioButton(DbHtControl.measureNames[i]);
			bg.add(measures[i]);
			mainPanel.add(measures[i]);
		}
		setDialogComponent(mainPanel);
	}
	
	public static DbHtAlarmParameters showDialog(Window frame, DbHtAlarmCounter dbHtAlarmCounter) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.dbHtAlarmCounter != dbHtAlarmCounter) {
			singleInstance = new DbHtAlarmParamsDialog(frame, dbHtAlarmCounter);
		}
		singleInstance.dbHtAlarmParameters = dbHtAlarmCounter.dbHtAlarmParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.dbHtAlarmParameters;
	}

	private void setParams() {
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			measures[i].setSelected(i==dbHtAlarmParameters.returnedMeasure);
		}
	}

	@Override
	public boolean getParams() {
		for (int i = 0; i < DbHtControl.NMEASURES; i++) {
			if (measures[i].isSelected()) {
				dbHtAlarmParameters.returnedMeasure = i;
				return true;
			}
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		dbHtAlarmParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		dbHtAlarmParameters.returnedMeasure = 0;
		setParams();
	}

}
