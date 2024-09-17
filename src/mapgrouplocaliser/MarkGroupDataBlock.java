package mapgrouplocaliser;

import PamguardMVC.PamProcess;
import PamguardMVC.superdet.SuperDetDataBlock;
import annotationMark.MarkDataUnit;

public class MarkGroupDataBlock extends SuperDetDataBlock {

	public MarkGroupDataBlock(String dataName, PamProcess parentProcess) {
		super(MarkDataUnit.class, dataName, parentProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		// TODO Auto-generated constructor stub
	}

}
