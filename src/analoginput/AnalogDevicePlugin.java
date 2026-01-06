package analoginput;

import PamController.SettingsNameProvider;
import PamModel.CommonPluginInterface;
import PamModel.PamPluginInterface;

/**
 * Plugins for analog sensors. Currently used by analoginput.AnalogDevicesManager.java
 * Demo plugin available in https://github.com/PAMGuard/DummyAnalogDevice/
 * @author dg50
 *
 */
public interface AnalogDevicePlugin extends CommonPluginInterface {

	public AnalogDeviceType createAnalogDevice(AnalogDevicesManager analogDevicesManager, 
			SettingsNameProvider settingsNameProvider, AnalogSensorUser analogSensorUser);
}
