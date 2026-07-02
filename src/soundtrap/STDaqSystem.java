package soundtrap;

import javax.swing.JComponent;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.DaqSystem;
import Acquisition.layoutFX.AcquisitionPaneFX;
import Acquisition.layoutFX.DAQSettingsPane;
import soundtrap.layoutFX.STDAQPane;

public class STDaqSystem extends DaqSystem {

	private STDAQPane stDaqPane;

	public STDaqSystem() {
	}

	@Override
	public String getSystemType() {
		return "SoundTrap clicks";
	}

	@Override
	public String getSystemName() {
		return "SoundTrap clicks";
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dialogSetParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean dialogGetParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMaxSampleRate() {
		return PARAMETER_UNKNOWN;
	}

	@Override
	public int getMaxChannels() {
		return 1;
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRealTime() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getDataUnitSamples() {
		// TODO Auto-generated method stub
		return 100;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDeviceName() {
		return "Sound Trap detector";
	}
	
	@Override
	public DAQSettingsPane getDAQSpecificPane(AcquisitionPaneFX acquisitionPaneFX) {
		if (stDaqPane == null) {
			stDaqPane = new STDAQPane();
		}
		return stDaqPane;
	}

}
