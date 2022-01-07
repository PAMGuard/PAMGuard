package difar.offline;

import offlineProcessing.DataCopyTask;
import Array.ArrayManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Some small changes to the generic class for copying data from binary data files to database files. 
 * Make sure that the sonobuoy deployment information is included by loading it from the streamerDataBlock
 * Include Processed DIFAR data as an "AffectedDataBlock" so that they will be deleted as per the user's 
 * request. 
 * @author Brian Miller
 *
 * @param <T>
 */
public class DifarDataCopyTask<T extends PamDataUnit> extends DataCopyTask<T> {

	/**
	 * @param pamDataBlock
	 */
	public DifarDataCopyTask(PamDataBlock<T> pamDataBlock) {
		super(pamDataBlock);
		addRequiredDataBlock(ArrayManager.getArrayManager().getStreamerDatabBlock());
		addAffectedDataBlock(pamDataBlock);
	}

}
