package clickDetector.offlineFuncs.rcImport;

import generalDatabase.SQLTypes;
import binaryFileStorage.DataUnitFileInformation;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.offlineFuncs.OfflineClickLogging;
import clickDetector.offlineFuncs.OfflineEventDataUnit;

public class ClickImportLogging extends OfflineClickLogging {

	public ClickImportLogging(ClickControl clickControl) {
		super(clickControl, null);
	}


	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		/**
		 * Need to do this a bit differently since not all references are in place. 
		 */		
		DummyClick click = (DummyClick) pamDataUnit;
//		binaryFile.setValue(click.pamFile);
		clickNumber.setValue(click.clickNo);
		eventId.setValue(click.eventId);
		amplitude.setValue(click.amplitude);
		
		
		
//		ClickDetection clickDetection = (ClickDetection) pamDataUnit;
//		DataUnitFileInformation fileInfo = clickDetection.getDataUnitFileInformation();
//		binaryFile.setValue(fileInfo.getShortFileName(BINARY_FILE_NAME_LENGTH));
//		clickNumber.setValue((int)fileInfo.getIndexInFile());
//		OfflineEventDataUnit offlineEvent = (OfflineEventDataUnit) 
//		clickDetection.getSuperDetection(OfflineEventDataUnit.class);
//		if (offlineEvent == null) {
//			eventId.setValue(null);
//		}
//		else {
//			eventId.setValue(offlineEvent.getDatabaseIndex());
//		}
//		amplitude.setValue(clickDetection.getAmplitudeDB());
	}

}
