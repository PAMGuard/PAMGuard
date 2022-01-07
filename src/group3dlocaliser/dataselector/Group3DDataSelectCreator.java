package group3dlocaliser.dataselector;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import group3dlocaliser.Group3DDataBlock;
import group3dlocaliser.Group3DLocaliserControl;

public class Group3DDataSelectCreator extends DataSelectorCreator {

	private Group3DDataBlock group3dDataBlock;
	private Group3DLocaliserControl groupLocControl;

	public Group3DDataSelectCreator(Group3DLocaliserControl groupLocControl, Group3DDataBlock group3dDataBlock) {
		super(group3dDataBlock);
		this.group3dDataBlock = group3dDataBlock;
		this.groupLocControl = groupLocControl;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new Group3DDataSelector(groupLocControl, group3dDataBlock, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new Group3DDataSelectParams();
	}

}
