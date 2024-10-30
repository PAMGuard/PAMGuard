package noiseOneBand.alarm;

import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamController.soundMedium.GlobalMediumManager;
import PamView.dialog.PamDialog;
import noiseOneBand.OneBandAlarmParameters;
import noiseOneBand.OneBandControl;
import alarm.AlarmCounter;

public class OneBandAlarmParamsDialog extends PamDialog {

	private static OneBandAlarmParamsDialog singleInstance;
	private OneBandAlarmParameters oneBandAlarmParameters;
	private JRadioButton[] measures = new JRadioButton[OneBandControl.NMEASURES];
	private OneBandAlarmCounter oneBandAlarmCounter;
	
	private OneBandAlarmParamsDialog(Window parentFrame, OneBandAlarmCounter alarmCounter) {
		super(parentFrame, alarmCounter.getUnitName() + " settings", true);
		this.oneBandAlarmCounter = alarmCounter;
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new TitledBorder("Select measure"));
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			measures[i] = new JRadioButton(OneBandControl.getMeasurementName(i));
			bg.add(measures[i]);
			mainPanel.add(measures[i]);
		}
		setDialogComponent(mainPanel);
	}
	
	public static OneBandAlarmParameters showDialog(Window frame, OneBandAlarmCounter oneBandAlarmCounter) {
		if (singleInstance == null || singleInstance.getOwner() != frame || singleInstance.oneBandAlarmCounter != oneBandAlarmCounter) {
			singleInstance = new OneBandAlarmParamsDialog(frame, oneBandAlarmCounter);
		}
		singleInstance.oneBandAlarmParameters = oneBandAlarmCounter.oneBandAlarmParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.oneBandAlarmParameters;
	}

	private void setParams() {
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			measures[i].setSelected(i==oneBandAlarmParameters.getReturnedMeasure());
		}
	}

	@Override
	public boolean getParams() {
		for (int i = 0; i < OneBandControl.NMEASURES; i++) {
			if (measures[i].isSelected()) {
				oneBandAlarmParameters.setReturnedMeasure(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		oneBandAlarmParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		oneBandAlarmParameters.setReturnedMeasure(0);
		setParams();
	}

}
