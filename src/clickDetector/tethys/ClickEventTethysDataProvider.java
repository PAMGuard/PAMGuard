package clickDetector.tethys;

import java.math.BigInteger;

import PamguardMVC.PamDataUnit;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import nilus.Detection;
import nilus.GranularityEnumType;
import nilus.Detection.Parameters;
import nilus.Detection.Parameters.UserDefined;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;
import tethys.swing.export.ExportWizardCard;
import tethys.swing.export.GranularityCard;

public class ClickEventTethysDataProvider extends AutoTethysProvider {

	private OfflineEventDataBlock eventDataBlock;

	public ClickEventTethysDataProvider(TethysControl tethysControl, OfflineEventDataBlock eventDataBlock) {
		super(tethysControl, eventDataBlock);
		this.eventDataBlock = eventDataBlock;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		GranularityEnumType[] allowed = {GranularityEnumType.GROUPED};
		return allowed;
	}
	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection =  super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		if (detection == null) {
			return null;
		}
		OfflineEventDataUnit eventDataUnit = (OfflineEventDataUnit) dataUnit;
		detection.setCount(BigInteger.valueOf(eventDataUnit.getSubDetectionsCount()));
		String comment = eventDataUnit.getComment();
		if (comment != null && comment.length() > 0) {
			detection.setComment(comment);
		}
		Parameters params = detection.getParameters();
		addUserNumber(params, "MinNumber", eventDataUnit.getMinNumber());
		addUserNumber(params, "BestNumber", eventDataUnit.getBestNumber());
		addUserNumber(params, "MaxNumber", eventDataUnit.getMaxNumber());
		
	
		return detection;
	}
	
	private void addUserNumber(Parameters params, String numName, Short number) {
		if (number == null) {
			return;
		}
		addUserDefined(params, numName, number.toString());
	}

	@Override
	public boolean wantExportDialogCard(ExportWizardCard wizPanel) {
		if (wizPanel.getClass() == GranularityCard.class) {
			return false;
		}
		else {
			return true;
		}
	}

}
