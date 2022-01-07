package clickDetector.echoDetection;

import java.io.Serializable;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import clickDetector.ClickControl;
import clickDetector.ClickDetector.ChannelGroupDetector;

public class JamieEchoDetectionSystem extends EchoDetectionSystem implements PamSettings {
	
	
	

	public JamieEchoDetectionSystem(ClickControl clickcontrol) {
		super(clickcontrol);
		PamSettingManager.getInstance().registerSettings(this);
		// TODO Auto-generated constructor stub
	}
	
	//get the paramters to be used by the echo detector
	protected JamieEchoParams jamieEchoParams = new JamieEchoParams();
	
	//create the JamieEcho Detector
	@Override
	public EchoDetector createEchoDetector(ChannelGroupDetector channelGroupDetector, 
			int channelBitmap) {
		return new JamieEchoDetector(this, getClickcontrol(), channelGroupDetector, channelBitmap);
	}

	@Override
	public EchoDialogPanel getEchoDialogPanel() {
		return new JamieEchoDialogPanal(this);
		
	}

	@Override
	public String getUnitName() {
		// TODO Auto-generated method stub
		return getClickcontrol().getUnitName();
	}

	
	@Override
	public String getUnitType() {
		// TODO Auto-generated method stub
		return "Jamie Click Echo system";
	}

	@Override
	public Serializable getSettingsReference() {
		// TODO Auto-generated method stub
		return jamieEchoParams;
	}

	@Override
	public long getSettingsVersion() {
		// TODO Auto-generated method stub
		return JamieEchoParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		jamieEchoParams = ((JamieEchoParams) pamControlledUnitSettings.getSettings()).clone();
		return (jamieEchoParams != null);
	}

	

	
}
