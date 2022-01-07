package noiseMonitor.alarm;

import java.awt.Window;
import java.io.Serializable;

import noiseMonitor.NoiseDataBlock;
import noiseMonitor.NoiseDataUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import alarm.AlarmControl;
import alarm.AlarmCounter;
import alarm.AlarmDecibelCounter;

public class NoiseAlarmCounter extends AlarmDecibelCounter implements PamSettings {

	private NoiseDataBlock noiseDataBlock;
	private NoiseAlarmParameters noiseAlarmParameters = new NoiseAlarmParameters();

	public NoiseAlarmCounter(AlarmControl alarmControl, NoiseDataBlock noiseDataBlock) {
		super(alarmControl);
		this.noiseDataBlock = noiseDataBlock;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public double getValue(int countType, PamDataUnit dataUnit) {
		NoiseDataUnit ndu = (NoiseDataUnit) dataUnit;
		double[][] noiseBandData = ndu.getNoiseBandData();
		// for now take the mean (rms) measurement. Future may allow changes to peak, median, etc.  
//		int iMeasure = noiseAlarmParameters.usedMeasure; 
		int iMeasure = PamUtils.getChannelPos(noiseAlarmParameters.usedMeasure, noiseDataBlock.getStatisticTypes());
		double eTot = 0;
		int nBand = noiseBandData.length;
		if (nBand == 0) {
			return 0;
		}
		int nMeas = noiseBandData[0].length;
		if (nMeas <= iMeasure) {
			return 0;
		}
		boolean[] selBands = noiseAlarmParameters.selectedBands;
		if (selBands == null) {
			return 0;
		}
		int n = Math.min(selBands.length, nBand);
		for (int i = 0; i < n; i++) {
			if (selBands[i] == false) {
				continue;
			}
			eTot += Math.pow(10.,noiseBandData[i][iMeasure]/10.);
		}
		if (eTot > 0) {
			eTot = 10.*Math.log10(eTot);
		}
		return eTot;
	}

	@Override
	public void resetCounter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasOptions() {
		return true;
	}

	@Override
	public boolean showOptions(Window parent) {
		NoiseAlarmParameters newParams = NoiseAlarmDialog.showDialog(parent, noiseAlarmParameters, noiseDataBlock);
		if (newParams != null) {
			noiseAlarmParameters = newParams.clone();
			return true;
		}
		return false;
	}

	@Override
	public String getUnitName() {
		return getAlarmControl().getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Noise Alarm Settings";
	}

	@Override
	public Serializable getSettingsReference() {
		return noiseAlarmParameters;
	}

	@Override
	public long getSettingsVersion() {
		return NoiseAlarmParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		noiseAlarmParameters = ((NoiseAlarmParameters) pamControlledUnitSettings.getSettings()).clone();
		return (noiseAlarmParameters != null);
	}

}
