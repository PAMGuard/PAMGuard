package difar.dataSelector;

import difar.DifarControl;
import generalDatabase.lookupTables.LookupList;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;

public class DifarDataSelectCreator extends DataSelectorCreator {

	private DifarControl difarControl;

	public DifarDataSelectCreator(DifarControl difarControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.difarControl = difarControl;
	}

	@Override
	public DataSelector createDataSelector(String selectorName,
			boolean allowScores, String selectorType) {
		return new DifarDataSelector(difarControl, getPamDataBlock(), selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		LookupList speciesList = difarControl.getDifarParameters().getSpeciesList(difarControl);
		
		int channelBitmap = 0;
		PamDataBlock<PamDataUnit> sourceBlock = difarControl.getDifarProcess().getSourceDataBlock();
		if (sourceBlock != null) {
			channelBitmap = sourceBlock.getChannelMap();
		}
		return new DifarSelectParameters(speciesList, channelBitmap);
	}

}
