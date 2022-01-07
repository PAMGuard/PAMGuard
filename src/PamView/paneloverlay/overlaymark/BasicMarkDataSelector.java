package PamView.paneloverlay.overlaymark;

import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.paneloverlay.OverlayDataManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

abstract public class BasicMarkDataSelector extends OverlayDataManager<OverlayMarkDataInfo> implements MarkDataSelector {

	public BasicMarkDataSelector(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		super(parameterTypes, parameterUnits);
	}

	private PamDataBlock lastDataBlock;
	private OverlayMarkDataInfo overlayDataInfo;
	private DataSelector dataSelector;
	
	@Override
	public boolean wantDataUnit(PamDataUnit dataUnit, int overlapLevel) {
		if (overlapLevel == OVERLAP_NONE) {
			return false;
		}
		PamDataBlock dataBlock = dataUnit.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		if (dataBlock != lastDataBlock) {
			lastDataBlock = dataBlock;
			overlayDataInfo = getOverlayInfo(lastDataBlock);
			dataSelector = dataBlock.getDataSelector(getDataSelectorName(), true);
		}
		if (overlayDataInfo.select == false) {
			// don't want it if it's not selected
			return false;
		}
		if (overlayDataInfo.acceptOverlapping == false && overlapLevel < OVERLAP_ALL) {
			// may not want it if it's only overlapping
			return false;
		}
		if (dataSelector != null) {
			// if there is a data selector, then test the data unit. 
			return (dataSelector.scoreData(dataUnit) > 0.);
		}
		else {
			// otherwise we want it, so return true
			return true;
		}
	}

}
