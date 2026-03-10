package analoginput;

import java.io.Serializable;
import java.util.ArrayList;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamModel.CommonPluginInterface;
import PamModel.PamModel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import analoginput.brainboxes.BrainBoxDevices;
import analoginput.measurementcomputing.MCCAnalogDevices;
import analoginput.swing.AnalogDiagnosticsDisplayProvider;
import analoginput.swing.AnalogDialogPanel;
import userDisplay.UserDisplayControl;

public class AnalogDevicesManager implements PamSettings {
	
	private ArrayList<AnalogDeviceType> availableTypes = availableTypes = new ArrayList<>();
	private SettingsNameProvider settingsNameProvider;
	
	private AnalogInputParams analogInputParams = new AnalogInputParams();
	private AnalogSensorUser sensorUser;
	private AnalogDeviceType activeDevice;
	
	private ArrayList<AnalogInputObserver> inputObservers = new ArrayList<>();
	

	public AnalogDevicesManager(SettingsNameProvider settingsNameProvider, AnalogSensorUser sensorUser) {
		this.settingsNameProvider = settingsNameProvider;
		this.sensorUser = sensorUser;
		availableTypes.add(new MCCAnalogDevices(this, settingsNameProvider, sensorUser));
		availableTypes.add(new BrainBoxDevices(this, settingsNameProvider, sensorUser));
		/**
		 * Load plugins by accessing the list for this module
		 */
		ArrayList<CommonPluginInterface> analogPlugins = PamModel.getPamModel().getPluginType(AnalogDevicePlugin.class);
		for (CommonPluginInterface cpi : analogPlugins) {
			AnalogDevicePlugin dev = (AnalogDevicePlugin) cpi;
			availableTypes.add(dev.createAnalogDevice(this, settingsNameProvider, sensorUser));
		}
		PamSettingManager.getInstance().registerSettings(this);
		UserDisplayControl.addUserDisplayProvider(new AnalogDiagnosticsDisplayProvider(this));
	}

	@Override
	public String getUnitName() {
		return "Analog Device Manager";
	}
	
	public AnalogDeviceType findDeviceType() {
		return findDeviceType(analogInputParams.selectedType);
	}
	
	/**
	 * 
	 * @return A component that can go into a larger dialog
	 */
	public PamDialogPanel getDialogPanel(PamDialog parentFrame, AnalogSensorUser sensorUser) {
		return new AnalogDialogPanel(this, parentFrame);
	}

	public AnalogDeviceType findDeviceType(String selectedType) {
		for (AnalogDeviceType dt : availableTypes) {
			if (dt.getDeviceType().equals(selectedType)) {
				return dt;
			}
		}
		return null;
	}

	@Override
	public String getUnitType() {
		return settingsNameProvider.getUnitName();
	}

	@Override
	public Serializable getSettingsReference() {
		return analogInputParams;
	}

	@Override
	public long getSettingsVersion() {
		return AnalogDeviceParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		if (pamControlledUnitSettings.getSettings() instanceof AnalogInputParams) {
			analogInputParams = (AnalogInputParams) pamControlledUnitSettings.getSettings();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @return the availableTypes
	 */
	public ArrayList<AnalogDeviceType> getAvailableTypes() {
		return availableTypes;
	}

	/**
	 * @return the analogDeviceParams
	 */
	public AnalogDeviceParams getAnalogDeviceParams() {
		if (activeDevice != null) {
			return activeDevice.getDeviceParams();
		}
		else {
			return null;
		}
	}
	
	public AnalogInputParams getInputParams() {
		return analogInputParams;
	}

	/**
	 * @return the sensorUser
	 */
	public AnalogSensorUser getSensorUser() {
		return sensorUser;
	}

	/**
	 * Called when settings have changes so that device
	 * can be selected and prepared. 
	 */
	public void prepareDevice() {
		activeDevice = findDeviceType();
		if (activeDevice != null) {
			activeDevice.prepareDevice();
		}
		notifyChange();
	}

	/**
	 * @return the activeDevice
	 */
	public AnalogDeviceType getActiveDevice() {
		return activeDevice;
	}

	public void addInputObserver(AnalogInputObserver inputObserver) {
		inputObservers.add(inputObserver);
	}
	
	public void notifyChange() {
		for (AnalogInputObserver inputObserver : inputObservers) {
			inputObserver.changedConfiguration();
		}
	}
	
	public void notifyData(ItemAllData itemData) {
		for (AnalogInputObserver inputObserver : inputObservers) {
			inputObserver.changedData(itemData);
		}
	}

	/**
	 * @return the analogInputParams
	 */
	public AnalogInputParams getAnalogInputParams() {
		return analogInputParams;
	}

}
