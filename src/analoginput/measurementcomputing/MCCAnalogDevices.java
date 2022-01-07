package analoginput.measurementcomputing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamView.dialog.PamDialogPanel;
import analoginput.AnalogDeviceParams;
import analoginput.AnalogDeviceType;
import analoginput.AnalogDevicesManager;
import analoginput.AnalogRangeData;
import analoginput.AnalogReadException;
import analoginput.AnalogSensorData;
import analoginput.AnalogSensorUser;
import analoginput.ItemAllData;
import analoginput.SensorChannelInfo;
import analoginput.calibration.AddThenMultiplyThenAdd;
import analoginput.calibration.CalibrationData;
import analoginput.calibration.CalibrationException;
import analoginput.calibration.SensorCalibration;
import analoginput.measurementcomputing.swing.MCCDialogPanel;
import mcc.MccJniInterface;
import mcc.mccjna.MCCBoardInfo;
import mcc.mccjna.MCCConstants;
import mcc.mccjna.MCCException;
import mcc.mccjna.MCCJNA;
import mcc.mccjna.MCCUtils;
import modbustcp.brainbox.BBED549;

public class MCCAnalogDevices implements AnalogDeviceType, PamSettings {

	private SettingsNameProvider settingsNameProvider;
	
	private MCCParameters mccParameters = new MCCParameters();
	
	private AnalogSensorUser sensorUser;

	private AnalogDevicesManager analogDevicesManager;

	private int nChannels;

	private AnalogRangeData[] channelRanges;

	private int[] channels;

	private int[] mccRanges;

	private CalibrationData[] calibrationData;
	
	private SensorCalibration calibration = new AddThenMultiplyThenAdd();

	public MCCAnalogDevices(AnalogDevicesManager analogDevicesManager, SettingsNameProvider settingsNameProvider, AnalogSensorUser sensorUser) {
		this.analogDevicesManager = analogDevicesManager;
		this.settingsNameProvider = settingsNameProvider;
		this.sensorUser = sensorUser;
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public String getDeviceType() {
		return "Measurement Computing Devices";
	}

	@Override
	public int getNumChannels() {
		return 0;
	}
	
	public String[] getDeviceNames() {
//		int nB = mccJniInterface.getNumBoards();
//		String[] names = new String[nB];
//		for (int i = 0; i < nB; i++) {
//			names[i] = mccJniInterface.getBoardName(i);
//		}
		ArrayList<MCCBoardInfo> boardInfo = MCCJNA.getBoardInformation();
		if (boardInfo == null) {
			return new String[0];
		}
		String[] names = new String[boardInfo.size()];
		for (int i = 0; i < boardInfo.size(); i++) {
			names[i] = boardInfo.get(i).getBoardName();
		}
		return names;
	}
	

	@Override
	public List<AnalogRangeData> getAvailableRanges(int iChan) {
		List<AnalogRangeData> rangeList = new ArrayList<>();
		// this seems fixed - don't really believe it but it will do. 
		int[] ranges = MCCConstants.bipolarRanges;
		for (int i = 0; i < ranges.length; i++) {
			rangeList.add(MCCUtils.getRangeData(ranges[i]));
		}
		return rangeList;
	}

	@Override
	public boolean setChannelRange(AnalogRangeData analogRange) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AnalogSensorData readData(int item) throws AnalogReadException {
		if (channels.length <= item) {
			return null;
		}
		int chan = channels[item];
		if (chan < 0) {
			throw new AnalogReadException("Channel disabled " + item);
		}
		double volts;
		try {
			int devNum = MCCUtils.boardIndexToNumber(mccParameters.deviceNumber);
			volts = MCCUtils.readVoltage(devNum, chan, true, mccRanges[item]);
		} catch (MCCException e1) {
			throw new AnalogReadException(e1.getMessage());
		}
		try {
			double param = calibration.rawToValue(volts, calibrationData[item]);
			analogDevicesManager.notifyData(new ItemAllData(item, null, volts, param));
			return new AnalogSensorData(volts, param);
		} catch (CalibrationException e) {
			throw new AnalogReadException("Bad calibration for item " + item);
		}
		
	}

	@Override
	public PamDialogPanel getDevicePanel() {
		return new MCCDialogPanel(this);
	}

	@Override
	public String getUnitName() {
		return settingsNameProvider.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getDeviceType();
	}

	@Override
	public Serializable getSettingsReference() {
		return mccParameters;
	}

	@Override
	public long getSettingsVersion() {
		return MCCParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		mccParameters = (MCCParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the mccParameters
	 */
	public MCCParameters getMccParameters() {
		return mccParameters;
	}

	/**
	 * @param mccParameters the mccParameters to set
	 */
	public void setMccParameters(MCCParameters mccParameters) {
		this.mccParameters = mccParameters;
	}

	/**
	 * @return the settingsNameProvider
	 */
	public SettingsNameProvider getSettingsNameProvider() {
		return settingsNameProvider;
	}

	/**
	 * @return the sensorUser
	 */
	public AnalogSensorUser getSensorUser() {
		return sensorUser;
	}

	@Override
	public AnalogDeviceParams getDeviceParams() {
		return mccParameters.getAnalogDeviceParams();
	}

	@Override
	public void setDeviceParams(AnalogDeviceParams deviceParams) {
		mccParameters.setAnalogDeviceParams(deviceParams);
	}

	@Override
	public synchronized void prepareDevice() {
		AnalogDeviceParams channelData = mccParameters.getAnalogDeviceParams();
		SensorChannelInfo[] channelInfo = sensorUser.getChannelNames();
		nChannels = channelInfo.length;
		channelRanges = new AnalogRangeData[nChannels];
		channels = new int[nChannels];
		mccRanges = new int[nChannels];
		calibrationData = new CalibrationData[nChannels];
		for (int i = 0; i < nChannels; i++) {
			channelRanges[i] = channelData.getItemRange(i);
			Integer chan = channelData.getItemChannel(i);
			Integer mccRange = MCCUtils.findRangeCode(channelRanges[i]);
			if (mccRange == null) {
				continue;
			}
			else {
				mccRanges[i] = mccRange;
			}
			if (chan == null) {
				chan = -1;
			}
			channels[i] = chan;
//			if (channels[i] >= 0 && bbRange != null) {
//				bbed549.setInputRange(channels[i], bbRange);
//			}
			calibrationData[i] = channelData.getCalibration(i);
		}
		
	}

}
