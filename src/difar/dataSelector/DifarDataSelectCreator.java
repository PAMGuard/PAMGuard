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

	/**
	 * PAMGuard adds a section to the dialog for each grouping a DIFAR clip
	 * could belong to. None is added here.
	 * <p>
	 * In practice DIFAR clips belong only to triangulations, and the DIFAR
	 * clip filter's Triangulations panel covers those in plain words. Other
	 * groupings are listed only because they accept any kind of data, such as
	 * the deep learning classifier's detection groups. They never hold DIFAR
	 * clips, and choosing AND in their section would hide every bearing.
	 * Should DIFAR clips ever be grouped another way, this is where to let
	 * that grouping's section back in.
	 */
	@Override
	protected DataSelector addSuperDetectionOptions(DataSelector ds, String selectorName,
			boolean allowScores, String selectorType) {
		return ds;
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
