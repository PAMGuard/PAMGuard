package analogarraysensor;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.sensors.ArrayDisplayParameters;
import Array.sensors.ArrayDisplayParamsProvider;
import Array.sensors.swing.ArrayDisplayParamsDialog;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.StorageOptions;
import PamController.StorageParameters.StoreChoice;
import PamView.dialog.warn.WarnOnce;
import analogarraysensor.swing.AnalogSensorDialog;
import analoginput.AnalogDevicesManager;
import analoginput.AnalogSensorUser;
import analoginput.SensorChannelInfo;

public class ArraySensorControl extends PamControlledUnit implements AnalogSensorUser, PamSettings, ArrayDisplayParamsProvider {

	public static final String unitType = "Analog Array Sensors";
	
	private ArraySensorProcess analogSensorProcess;
	
	private ArraySensorParams analogSensorParams = new ArraySensorParams();
	
	private AnalogDevicesManager analogDevicesManager;
	
//	private ArraySidePanel arraySidePanel;
	
	private String[] sensChannelNames = {"Depth", "Pitch", "Roll", "Heading"};
	
	private String helpPoint = "sensors.analogarray.docs.analogarray";
	
	private StorageOptions storageOptions;

//	private PitchRollSidePanel pitchRollSidePanel;
//	/SMRU_UID_2018/src/help/sensors/analogarray/docs/analogarray.html
	
	public ArraySensorControl(String unitName) {
		super(unitType, unitName);
		analogDevicesManager = new AnalogDevicesManager(this, this);
		analogSensorProcess = new ArraySensorProcess(this);
		addPamProcess(analogSensorProcess);
		PamSettingManager.getInstance().registerSettings(this);
		/*
		 * Check default storage option - if nothing is set, then set it to false, otherwise go 
		 * with what's selected. 
		 */
		storageOptions = StorageOptions.getInstance();
		StoreChoice selStorage = storageOptions.getStorageParameters().findStoreChoice(analogSensorProcess.getSensorDataBlock());
		if (selStorage == null) {
			storageOptions.getStorageParameters().setStorageOptions(analogSensorProcess.getSensorDataBlock(), false, false);
			analogSensorProcess.getSensorDataBlock().setShouldLog(false);
		}
		else {
			analogSensorProcess.getSensorDataBlock().setShouldLog(selStorage.isDatabase());
		}
		
//		StorageOptions.getInstance().getStorageParameters().setStorageOptions(analogSensorProcess.getSensorDataBlock(), 
//				analogSensorParams.storeRawValues, false);
//		analogSensorProcess.getSensorDataBlock().setShouldLog(analogSensorParams.storeRawValues);
	}

	/**
	 * @return the analogSensorParams
	 */
	public ArraySensorParams getAnalogSensorParams() {
		return analogSensorParams;
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName()+ " Settings...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSettingsDialog(parentFrame);
			}
		});
		return menuItem;
	}

	protected void showSettingsDialog(Frame parentFrame) {
		ArraySensorParams newParams = AnalogSensorDialog.showDialog(parentFrame, this);
		if (newParams != null) {
			newSettings();
//			checkStreamerOrientationEnable();
			analogSensorProcess.getSensorDataBlock().setShouldLog(storageOptions.getStorageParameters().isStoreDatabase(analogSensorProcess.getSensorDataBlock(), false));
		}
	}
	
//	private void checkStreamerOrientationEnable() {
//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		if (array == null) return;
//		int nS = array.getNumStreamers();
//		int nOff = 0;
//		for (int i = 0; i < nS; i++) {
//			Streamer streamer = array.getStreamer(i);
//			if (streamer == null) continue;
//			if (streamer.isEnableOrientation() == false) {
//				nOff++;
//			}
//		}
//		if (nOff > 0) {
//			String msg = "In order to use sensor orientation data you will need to go to the Array settings and select the sensor data source"
//					+ " for each parameter for each streamer";
//			WarnOnce.showWarning(getGuiFrame(), "Array Sensors", msg, WarnOnce.WARNING_MESSAGE);
//		}
//	}

	private void newSettings() {
		analogSensorProcess.prepareProcess();
	}

	/**
	 * @return the analogDevicesManager
	 */
	public AnalogDevicesManager getAnalogDevicesManager() {
		return analogDevicesManager;
	}

	@Override
	public SensorChannelInfo[] getChannelNames() {
		int nS = ArrayManager.getArrayManager().getCurrentArray().getNumStreamers();
		if (nS <= 1) {
			return SensorChannelInfo.makeQuick(sensChannelNames);
		}
		else {
			SensorChannelInfo[] infos = new SensorChannelInfo[nS*sensChannelNames.length];
			for (int i = 0, j = 0; i < nS; i++) {
				for (int k = 0; k < sensChannelNames.length; k++, j++) {
					String name = String.format("%s %d", sensChannelNames[k], i);
					String tip = String.format("%s data for array streamer %d", sensChannelNames[k], i);
					infos[j] = new SensorChannelInfo(name, tip);
				}
			}
			return infos;
		}
	}

	@Override
	public Serializable getSettingsReference() {
		return analogSensorParams;
	}

	@Override
	public long getSettingsVersion() {
		return ArraySensorParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		analogSensorParams = (ArraySensorParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public String getUserName() {
		return getUnitName();
	}

	/**
	 * @return the helpPoint
	 */
	public String getHelpPoint() {
		return helpPoint;
	}

	/**
	 * @return the sensChannelNames
	 */
	public String[] getSensChannelNames() {
		return sensChannelNames;
	}

	/**
	 * @return the analogSensorProcess
	 */
	public ArraySensorProcess getAnalogSensorProcess() {
		return analogSensorProcess;
	}

	public int getNumSensorGroups() {
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		int nS = array.getNumStreamers();
		return nS;
	}

	@Override
	public ArrayDisplayParameters getDisplayParameters() {
		return analogSensorParams.getArrayDisplayParameters();
	}

	@Override
	public void setDisplayParameters(ArrayDisplayParameters displayParameters) {
		analogSensorParams.setArrayDisplayParameters(displayParameters);
	}

	@Override
	public boolean showDisplayParamsDialog(Window window) {
		return ArrayDisplayParamsDialog.showDialog(window, this);
	}

}
