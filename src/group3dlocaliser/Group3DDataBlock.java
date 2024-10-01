package group3dlocaliser;

import IshmaelDetector.IshDetection;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.superdet.SuperDetDataBlock;
import group3dlocaliser.dataselector.Group3DDataSelectCreator;
import group3dlocaliser.tethys.Group3DSpeciesManager;
import group3dlocaliser.tethys.Group3DTethysProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

public class Group3DDataBlock extends SuperDetDataBlock {

	private Group3DProcess group3DProcess;
	private Group3DDataSelectCreator dataSelCreator;
	private Group3DLocaliserControl groupLocControl;
	private Group3DTethysProvider group3dTethysProvider;
	private Group3DSpeciesManager group3dSpeciesManager;

	public Group3DDataBlock(String dataName, Group3DProcess group3DProcess, int channelMap, Group3DLocaliserControl groupLocControl) {
		super(Group3DDataUnit.class, dataName, group3DProcess, channelMap, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.group3DProcess = group3DProcess;
		this.groupLocControl = groupLocControl;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getDataSelectCreator()
	 */
	@Override
	public DataSelectorCreator getDataSelectCreator() {
		if (dataSelCreator == null) {
			dataSelCreator = new Group3DDataSelectCreator(groupLocControl, this);
		}
		return dataSelCreator;
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (group3dTethysProvider == null) {
			group3dTethysProvider = new Group3DTethysProvider(tethysControl, groupLocControl, this);
		}
		return group3dTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<IshDetection> getDatablockSpeciesManager() {
		if (group3dSpeciesManager == null) {
			group3dSpeciesManager = new Group3DSpeciesManager(this);
		}
		return group3dSpeciesManager;
	}

}
