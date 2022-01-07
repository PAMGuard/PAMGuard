package dataModelFX.connectionNodes;

import binaryFileStorage.BinaryStore;
import dataModelFX.DataModelConnectPane;
import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;

/**
 * Connection node for the binary storage module.
 * <p>
 * The binary storage node has no connection plugs or sockets but does have
 * lines when clicked indicating which modules save to the binary store.
 * 
 * @author Jamie Macaulay
 *
 */
public class BinaryConnectionNode extends DBConnectionNode {

	public BinaryConnectionNode(DataModelConnectPane connectionPane) {
		super(connectionPane);
	}

	@Override
	public void setPamControlledUnit(PamControlledUnit pamControlledUnit) {
		super.setPamControlledUnit(pamControlledUnit);
	}

	/**
	 * Check whether a data block subscribes to the binary store to store data.
	 * 
	 * @param pamDataBlock- data block to check
	 * @return true if the data block does subscribe to the binary store. .
	 */
	@Override
	protected boolean canConnect(PamDataBlock pamDataBlock) {
		return BinaryStore.isDataBlockBinaryOut(pamDataBlock);
	}

}
