package group3dlocaliser;

import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.superdet.SuperDetDataBlock;
import group3dlocaliser.dataselector.Group3DDataSelectCreator;

public class Group3DDataBlock extends SuperDetDataBlock {

	private Group3DProcess group3DProcess;
	private Group3DDataSelectCreator dataSelCreator;
	private Group3DLocaliserControl groupLocControl;

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

}
