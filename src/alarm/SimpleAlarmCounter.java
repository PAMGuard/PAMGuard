package alarm;

import java.awt.Window;

import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;

public class SimpleAlarmCounter extends AlarmCounter {
	
	private PamDialogPanel dataSelDialogPanel;
	private DataSelector dataSelector;
	private PamDataBlock dataSource;

	public SimpleAlarmCounter(AlarmControl alarmControl, PamDataBlock dataSource) {
		super(alarmControl);
		this.dataSource = dataSource;
		if (dataSource != null) {
			dataSelector = dataSource.getDataSelector(alarmControl.getUnitName()+alarmControl.getUnitType(), true);
			if (dataSelector != null) {
				dataSelDialogPanel = dataSelector.getDialogPanel();
			}
		}
	}

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		if (dataSelector == null) {
			return 1;
		}
		double val = dataSelector.scoreData(dataUnit);
		return val;
	}

	@Override
	public void resetCounter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasOptions() {
		return (dataSelDialogPanel != null);
	}

	@Override
	public boolean showOptions(Window parent) {
		if (dataSelDialogPanel == null) return false;
		return GenericSwingDialog.showDialog(parent, dataSource.getDataName() + " selection", dataSelDialogPanel);
	}

}
