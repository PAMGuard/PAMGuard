package clickDetector.echoDetection;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import clickDetector.ClickControl;
import clickDetector.ClickDetector.ChannelGroupDetector;

/**
 * Very simple echo detection system which uses a single test of time
 * between the previous click and this click to determine if it's an echo or not. 
 * @author Doug Gillespie
 *
 */

public class SimpleEchoDetectionSystem extends EchoDetectionSystem implements PamSettings {

	public SimpleEchoDetectionSystem(ClickControl clickcontrol) {
		super(clickcontrol);
		PamSettingManager.getInstance().registerSettings(this);
	}

	protected SimpleEchoParams simpleEchoParams = new SimpleEchoParams();
	
	
	@Override
	public EchoDetector createEchoDetector(ChannelGroupDetector channelGroupDetector, 
			int channelBitmap) {
		return new SimpleEchoDetector(this, getClickcontrol(), channelGroupDetector, channelBitmap);
	}

	@Override
	public EchoDialogPanel getEchoDialogPanel() {
		
		return new SimpleEchoDialogPanel(this);
	}

	@Override
	public Serializable getSettingsReference() {
		return simpleEchoParams;
	}

	@Override
	public long getSettingsVersion() {
		return SimpleEchoParams.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return getClickcontrol().getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Simple Click Echo system";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		simpleEchoParams = ((SimpleEchoParams) pamControlledUnitSettings.getSettings()).clone();
		return (simpleEchoParams != null);
	}

}
