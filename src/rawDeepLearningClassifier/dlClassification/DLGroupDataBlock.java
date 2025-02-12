package rawDeepLearningClassifier.dlClassification;

import PamView.GeneralProjector;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetDataBlock;
import clickTrainDetector.CTDataUnit;

/**
 * 
 * Data block which holds deep learning detections derived from groups of data units.
 */
public class DLGroupDataBlock extends SuperDetDataBlock<DLGroupDetection, PamDataUnit> {

	private DLClassifyProcess dlClassifyProcess;

	public DLGroupDataBlock(DLClassifyProcess parentProcess, String name, int channelMap) {
		super(CTDataUnit.class, name, parentProcess, channelMap, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.dlClassifyProcess = parentProcess;
		
		
	}
	
	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return super.getHoverText(generalProjector, dataUnit, iSide);
	}

	
	@Override
 	public boolean shouldNotify() {
		/**
		 * Segments are not saved by PAMGuard as it's a waste of storage space but this means that reprocessing data requires the
		 * segments are remad with detections in viewer mode. There is 
		 */
 		return true; 
 	}


}
