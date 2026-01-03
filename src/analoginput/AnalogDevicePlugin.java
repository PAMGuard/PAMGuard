package analoginput;

import PamController.SettingsNameProvider;
import PamModel.CommonPluginInterface;
import PamModel.PamPluginInterface;

public interface AnalogDevicePlugin extends CommonPluginInterface {

	public AnalogDeviceType createAnalogDevice(AnalogDevicesManager analogDevicesManager, 
			SettingsNameProvider settingsNameProvider, AnalogSensorUser analogSensorUser);
}
