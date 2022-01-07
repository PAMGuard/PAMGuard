package PamguardMVC;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamModel.PamModel;

/**
 * Similar to PAMProcess, but always subscribes itself to upstream 
 * datablocks using the instant options to this process always get's called
 * in the same thread as data were collected in and before data units are sent for
 * storage. Used for classifiers and localisers which annotate existing data. 
 * @author Doug Gillespie
 *
 */
abstract public class PamInstantProcess extends PamProcess {

	public PamInstantProcess(PamControlledUnit pamControlledUnit) {
		super(pamControlledUnit, null);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setParentDataBlock(PamguardMVC.PamDataBlock)
	 */
	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock) {
		setParentDataBlock(newParentDataBlock, false);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setParentDataBlock(PamguardMVC.PamDataBlock, boolean)
	 */
	@Override
	public void setParentDataBlock(PamDataBlock newParentDataBlock, boolean reThread) {
		if (getParentDataBlock() == newParentDataBlock) {
			return;
		}
		
		if (getParentDataBlock() != null) {
			parentDataBlock.deleteObserver(this);
		}
		parentDataBlock = newParentDataBlock;
		if (parentDataBlock != null) {
			parentDataBlock.addInstantObserver(this);
			PamProcess pp = parentDataBlock.getParentProcess();
			setSampleRate(pp.getSampleRate(), true);
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.CHANGED_PROCESS_SETTINGS);
		createAnnotations(true);
	}


}
