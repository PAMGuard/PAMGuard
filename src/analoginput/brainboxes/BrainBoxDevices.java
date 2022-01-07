package analoginput.brainboxes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
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
import analoginput.brainboxes.swing.BBDialogPanel;
import analoginput.calibration.AddThenMultiplyThenAdd;
import analoginput.calibration.CalibrationData;
import analoginput.calibration.CalibrationException;
import analoginput.calibration.SensorCalibration;
import modbustcp.ModbusTCPException;
import modbustcp.brainbox.BBED549;
import warnings.PamWarning;
import warnings.WarningSystem;

public class BrainBoxDevices  implements AnalogDeviceType, PamSettings{

	private SettingsNameProvider settingsNameProvider;
	
	private AnalogSensorUser sensorUser;
	
	private BrainBoxParams brainBoxParams = new BrainBoxParams();
	
	private BBED549 bbed549;

	private AnalogDevicesManager analogDevicesManager;
	
	private PamWarning deviceWarning;

	private int nChannels;

	private AnalogRangeData[] channelRanges;

	private int[] channels;

	private int[] bbRanges;
	
	private SensorCalibration calibration = new AddThenMultiplyThenAdd();

	private CalibrationData[] calibrationData;

	public BrainBoxDevices(AnalogDevicesManager analogDevicesManager, SettingsNameProvider settingsNameProvider, AnalogSensorUser sensorUser) {
		this.analogDevicesManager = analogDevicesManager;
		this.settingsNameProvider = settingsNameProvider;
		this.sensorUser = sensorUser;
		PamSettingManager.getInstance().registerSettings(this);
		deviceWarning = new PamWarning("BrainBox Sensor Readout", "", 0);
	}

	@Override
	public String getUnitName() {
		return settingsNameProvider.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Brain Box Input";
	}

	@Override
	public Serializable getSettingsReference() {
		return brainBoxParams;
	}

	@Override
	public long getSettingsVersion() {
		return BrainBoxParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		brainBoxParams = (BrainBoxParams) pamControlledUnitSettings.getSettings();
		return true;
	}

	@Override
	public String getDeviceType() {
		return "Brain Boxes ED-549";
	}

	@Override
	public int getNumChannels() {
		return 8;
	}

	@Override
	public List<AnalogRangeData> getAvailableRanges(int iChan) {
		int[] ranges = BBED549.ALLRANGES;
		ArrayList<AnalogRangeData> availRanges = new ArrayList<>();
		for (int i = 0; i < ranges.length; i++) {
			availRanges.add(BBED549.getRangeData(ranges[i]));
		}
		
		return availRanges;
	}

	@Override
	public boolean setChannelRange(AnalogRangeData analogRange) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AnalogSensorData readData(int item) throws AnalogReadException {
		if (bbed549 == null) {
			openDevice();
		}
		if (bbed549 == null) {
			throw new AnalogReadException("No connected input device");
		}
		if (channels == null || item < 0 || item >= channels.length) {
			throw new AnalogReadException("Invalid channel selection for item " + item);
		}
		int chan = channels[item];
		if (chan < 0) {
			throw new AnalogReadException("Channel disabled " + item);
		}
		try {
			int sensInts = bbed549.readRawAnalogChannel(chan);
			double sensData = BBED549.hexToEngineering(bbRanges[item], sensInts);
			double paramValue = calibration.rawToValue(sensData, calibrationData[item]);
			analogDevicesManager.notifyData(new ItemAllData(item, sensInts, sensData, paramValue));
//			System.out.printf("Read item %d, chan %d, int %d, real %3.5f, param %3.5f\n", iChan, chan, sensInts, sensData, paramValue);
			sayError(null);
			return new AnalogSensorData(sensData, paramValue);
			
			//			for (int i = 0; i < sensInts.length; i++) {
			//				System.out.printf("Read channel %d, intval %d, floatval %3.3fmA\n", i, sensInts[i], sensData[i]*1000);
			//			}
		} catch (ModbusTCPException e) {
			sayError(e.getMessage());
			throw new AnalogReadException("BrainBox Modbus Error " + e.getMessage());
		} catch (CalibrationException e) {
//			e.printStackTrace();
			throw new AnalogReadException("BrainBox Calibration Error " + e.getMessage());
		}

	}

	@Override
	public PamDialogPanel getDevicePanel() {
		// TODO Auto-generated method stub
		return new BBDialogPanel(this, sensorUser);
	}

	/**
	 * @return the brainBoxParams
	 */
	public BrainBoxParams getBrainBoxParams() {
		return brainBoxParams;
	}

	/**
	 * @param brainBoxParams the brainBoxParams to set
	 */
	public void setBrainBoxParams(BrainBoxParams brainBoxParams) {
		this.brainBoxParams = brainBoxParams;
	}

	@Override
	public AnalogDeviceParams getDeviceParams() {
		return brainBoxParams.getAnalogDeviceParams();
	}

	@Override
	public void setDeviceParams(AnalogDeviceParams deviceParams) {
		brainBoxParams.setAnalogDeviceParams(deviceParams);
	}

	@Override
	public void prepareDevice() {
		closeDevice();
	}
	
	private synchronized void closeDevice() {
		if (bbed549 != null) {
			bbed549.close();
			bbed549 = null;
		}
	}	

	private synchronized boolean openDevice() {
		try {
			bbed549 = new BBED549(brainBoxParams.ipAddress);
			AnalogDeviceParams channelData = brainBoxParams.getAnalogDeviceParams();
			// see what the user wants.
			SensorChannelInfo[] channelInfo = sensorUser.getChannelNames();
			nChannels = channelInfo.length;
			channelRanges = new AnalogRangeData[nChannels];
			channels = new int[nChannels];
			bbRanges = new int[nChannels];
			calibrationData = new CalibrationData[nChannels];
			for (int i = 0; i < nChannels; i++) {
				channelRanges[i] = channelData.getItemRange(i);
				Integer chan = channelData.getItemChannel(i);
				Integer bbRange = BBED549.getBBRangeCode(channelRanges[i]);
				if (bbRange == null) {
					continue;
				}
				else {
					bbRanges[i] = bbRange;
				}
				if (chan == null) {
					chan = -1;
				}
				channels[i] = chan;
				if (channels[i] >= 0 && bbRange != null) {
					bbed549.setInputRange(channels[i], bbRange);
				}
				calibrationData[i] = channelData.getCalibration(i);
			}
//			for (int i = 0; i < 4; i++) {
//				bbed549.setInputRange(i, BBED549.mAMP_4_20);
//			}
		} catch (ModbusTCPException e) {
			sayError(e.getMessage());
			bbed549 = null;
			return false;
		}
		return true;
}

//	private void readSensors() {
//		if (bbed549 == null) {
//			openDevice();
//			if (bbed549 == null) {
//				return;
//			}
//		}
//		double[] sensData = null;
//		try {
//			short[] sensInts = bbed549.readRawAnalogChannels(0,3);
//			sensData = bbed549.hexToEngineering(BBED549.mAMP_4_20, sensInts);
//			//			for (int i = 0; i < sensInts.length; i++) {
//			//				System.out.printf("Read channel %d, intval %d, floatval %3.3fmA\n", i, sensInts[i], sensData[i]*1000);
//			//			}
//		} catch (ModbusTCPException e) {
//			sayError(e.getMessage());
//			closeDevice();
//			return;
//		}
//		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
//		Streamer streamer = array.getStreamer(0);
//		//		double depth = (sensData[0]-.00);
//		//		streamer.setCoordinate(2, -depth);
//
//		//		double roll = (sensData[0]-.004)/.016*180.-90.;
//		double head = (sensData[2]-0.004)/.016*360.;
//		streamer.setHeading(head);
//		streamer.setPitch(sensData[0]*1000.);
//		streamer.setRoll(sensData[1]*1000.);
//
//		PamController.getInstance().notifyModelChanged(PamController.HYDROPHONE_ARRAY_CHANGED);
//	}

	private void sayError(String error) {
		if (error == null && deviceWarning.getWarnignLevel() > 0) {
			WarningSystem.getWarningSystem().removeWarning(deviceWarning);
			deviceWarning.setWarnignLevel(2);
		}
		if (error != null) {
			deviceWarning.setWarningMessage(error);
			deviceWarning.setWarnignLevel(2);
			WarningSystem.getWarningSystem().addWarning(deviceWarning);
		}
	}
}
