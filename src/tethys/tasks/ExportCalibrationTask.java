package tethys.tasks;

import Array.Hydrophone;
import Array.HydrophoneDataBlock;
import Array.HydrophoneDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataMap.OfflineDataMapPoint;
import nilus.Calibration;
import nilus.Helper;
import nilus.MetadataInfo;
import tethys.TethysControl;
import tethys.calibration.CalibrationHandler;
import tethys.niluswraps.NilusSettingsWrapper;

public class ExportCalibrationTask extends TethysTask {

	private HydrophoneDataBlock hydrophoneDataBlock;
	private CalibrationHandler calibrationHandler;
	private NilusSettingsWrapper<Calibration> wrappedCalibration;

	public ExportCalibrationTask(TethysControl tethysControl, TethysTaskManager tethysTaskManager, HydrophoneDataBlock hydrophoneDataBlock) {
		super(tethysControl, tethysTaskManager, hydrophoneDataBlock);
		this.hydrophoneDataBlock = hydrophoneDataBlock;
		calibrationHandler = tethysControl.getCalibrationHandler();
		getCalibration(); // initialise the wrapper at least. 
	}
	
	private Calibration getCalibration() {
		TethysTaskSettings taskSettings = getTethysTaskManager().getTaskParameters().getTaskSettings(this);
		wrappedCalibration = null;
		if (taskSettings != null) {
			wrappedCalibration = (NilusSettingsWrapper<Calibration>) taskSettings.getWrappedSample();
		}
		if (wrappedCalibration == null) {
			Calibration cal = new Calibration();
			try {
				Helper.createRequiredElements(cal);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			wrappedCalibration = new NilusSettingsWrapper<Calibration>(cal);
			taskSettings = new TethysTaskSettings<>(getName());
			taskSettings.setWrappedSample(wrappedCalibration);
			getTethysTaskManager().getTaskParameters().setTaskSettings(this, taskSettings);
		}
		return wrappedCalibration.getNilusObject(Calibration.class);
	}
	
	private void setCalibration(Calibration calibration) {
		wrappedCalibration.setNilusObject(calibration);
	}

	@Override
	public String getName() {
		return "Export Calibration Data";
	}

	@Override
	public boolean processDataUnit(PamDataUnit dataUnit) {
//		System.out.println("Export calibration data unit: " + dataUnit.getSummaryString());
		HydrophoneDataUnit hdu = (HydrophoneDataUnit) dataUnit;
		Hydrophone h = hdu.getHydrophone();
		if (wrappedCalibration == null) {
			getCalibration();
		}
//		if (wrappedCalibration == null) {
//			return;
//		}
		calibrationHandler.exportOneCalibration(wrappedCalibration, h.getID(), false);
		return false;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedDataComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canRun() {
		boolean can = super.canRun();
		if (!can) {
			return false;
		}
//		boolean hydOk = calibrationHandler.isHydrophoneNamed();
//		if (hydOk == false) {
//			whyNot = "Hydrophone identification information not provided";
//			return false;
//		}
		// now check the basic calibration information is all there. 
		Calibration cal = getCalibration();
		MetadataInfo metaInfo = cal.getMetadataInfo();
		if (metaInfo == null) {
			whyNot = "No calibration metadata has been set";
			return false;
		}
		if (metaInfo.getContact() == null) {
			whyNot = "No calibration contact info has been set";
			return false;
		}
		if (metaInfo.getDate() == null) {
			whyNot = "Calibration date information has not been set";
			return false;
		}
		if (metaInfo.getUpdateFrequency() == null) {
			whyNot = "Calibration data update information has not been set";
		}
		
		return true;
	}

	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public boolean callSettings() {
		Calibration newCal = calibrationHandler.getExportSettings(getCalibration());
		if (newCal != null) {
			setCalibration(newCal);
			return true;
		}
		else {
			return false;
		}
	}

}
