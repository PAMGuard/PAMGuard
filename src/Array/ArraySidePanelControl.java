package Array;

import java.awt.Window;
import java.io.Serializable;

import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.swing.ArrayDisplayParamsDialog;
import Array.swing.sidepanel.ArraySidePanel;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamView.PamSidePanel;

public class ArraySidePanelControl extends PamControlledUnit implements ArrayDisplayParamsProvider, PamSettings {

	private static final String unitType = "Array Display";
	private ArraySidePanel arraySidePanel;
	
	private ArrayDisplayParameters arrayDisplayParameters = new ArrayDisplayParameters();
	
	public ArraySidePanelControl(String unitName) {
		super(unitType, unitName);
		PamSettingManager.getInstance().registerSettings(this);
	}	
	
	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#getSidePanel()
	 */
	@Override
	public PamSidePanel getSidePanel() {
		if (arraySidePanel == null) {
			arraySidePanel = new ArraySidePanel(this);
		}
		return arraySidePanel;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
		case PamController.HYDROPHONE_ARRAY_CHANGED:
			if (arraySidePanel != null) {
				arraySidePanel.update();
			}
			break;
		case PamController.NEW_SCROLL_TIME:
			if (isViewer()) {
				arraySidePanel.updateViewerTime(PamCalendar.getTimeInMillis());
			}
		}
	}

	@Override
	public ArrayDisplayParameters getDisplayParameters() {
		return arrayDisplayParameters;
	}

	@Override
	public boolean showDisplayParamsDialog(Window window) {
		return ArrayDisplayParamsDialog.showDialog(window, this);
	}

	@Override
	public void setDisplayParameters(ArrayDisplayParameters displayParameters) {
		this.arrayDisplayParameters = displayParameters;		
	}

	@Override
	public Serializable getSettingsReference() {
		return arrayDisplayParameters;
	}

	@Override
	public long getSettingsVersion() {
		return ArrayDisplayParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		arrayDisplayParameters = ((ArrayDisplayParameters) pamControlledUnitSettings.getSettings()).clone();
		return arrayDisplayParameters != null;
	}


}
