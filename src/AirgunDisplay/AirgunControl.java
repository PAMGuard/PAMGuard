package AirgunDisplay;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import GPS.GpsData;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.positionreference.PositionReference;


public class AirgunControl extends PamControlledUnit implements PamSettings, PositionReference {

	AirgunParameters airgunParameters;
	
	AirgunProcess airgunProcess;

	protected boolean initialisationComplete;
	
	public AirgunControl(String unitName) {
		
		super("Airgun Display", unitName);

		airgunParameters = new AirgunParameters();
		
		addPamProcess(airgunProcess = new AirgunProcess(this));
		
		PamSettingManager.getInstance().registerSettings(this);
		
		airgunProcess.findSourceData();
	}

	public Serializable getSettingsReference() {
		return airgunParameters;
	}

	public long getSettingsVersion() {
		return AirgunParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		if (pamControlledUnitSettings.getVersionNo() != AirgunParameters.serialVersionUID) {
			return false;
		}
		airgunParameters = ((AirgunParameters) pamControlledUnitSettings.getSettings()).clone();
		airgunProcess.noteNewSettings();
		return true;
	}

	@Override
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Display Options ...");
		menuItem.addActionListener(new AirgunDisplayOptions(parentFrame));
		return menuItem;
	}
	
	private class AirgunDisplayOptions implements ActionListener {
		
		Frame frame;

		public AirgunDisplayOptions(Frame frame) {
			this.frame = frame;
		}

		public void actionPerformed(ActionEvent e) {

			AirgunParameters newParams = AirgunParametersDialog.showDialog(frame, airgunParameters);
			if (newParams != null) {
				airgunParameters = newParams.clone();
				airgunProcess.findSourceData();
			}
		}	
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			airgunProcess.findSourceData();			
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			if (initialisationComplete) {
				airgunProcess.findSourceData();			
			}
			break;
		case PamControllerInterface.NEW_SCROLL_TIME:
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			airgunProcess.newViewTime();
			break;
		}
	}

	@Override
	public GpsData getReferencePosition(long timeMillis) {
		return airgunProcess.getReferencePosition(timeMillis);
	}

	@Override
	public String getReferenceName() {
		return this.getUnitName();
	}

}
