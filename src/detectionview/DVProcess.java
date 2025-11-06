package detectionview;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;

public class DVProcess extends PamguardMVC.PamProcess {
	
	private DVControl dvControl;
	
	private DVDataBlock dvDataBlock;

	private PamRawDataBlock inputRawData;

	private PamDataBlock detectorDataBlock;
	
	private DVLoader dvLoader;

	public DVProcess(DVControl dvControl) {
		super(dvControl, null);
		this.dvControl = dvControl;
		dvDataBlock = new DVDataBlock(dvControl, this, 1);
		addOutputDataBlock(dvDataBlock);
		dvLoader = new DVLoader(dvControl, this);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	

	/**
	 * @return the dvControl
	 */
	public DVControl getDvControl() {
		return dvControl;
	}

	/**
	 * @return the dvDataBlock
	 */
	public DVDataBlock getDvDataBlock() {
		return dvDataBlock;
	}

	@Override
	public void setupProcess() {
		prepareProcessOK();
	}

	@Override
	public boolean prepareProcessOK() {
		DVParameters params = dvControl.getDvParameters();
		detectorDataBlock = dvControl.getPamConfiguration().getDataBlockByLongName(params.detectorName);
		if (detectorDataBlock == null) {
			return false;
		}
		setParentDataBlock(detectorDataBlock);
		
		PamRawDataBlock newInput = (PamRawDataBlock) dvControl.getPamConfiguration().getDataBlockByLongName(params.rawDataName);
		if (newInput != inputRawData && inputRawData != null) {
			inputRawData.deleteObserver(this);
			inputRawData = newInput;
			inputRawData.addInstantObserver(this);
		}
		
		
		dvControl.updateConfigObs();
		return super.prepareProcessOK();
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.OFFLINE_DATA_LOADED) {
			reloadEverying();
		}
	}

	/**
	 * Clear all current clips and generate a new clip for every data unit
	 * in the input data. This will need to be done in a swing worker since 
	 * it might take a very long time. New controls in the base of the display
	 * panels will hopefully provide an interrupt. 
	 * Functionality is in a different class which will have a LOT of synchronization 
	 * in it. 
	 */
	private void reloadEverying() {
		dvLoader.reloadEverything(true);
	}

	/**
	 * @return the inputRawData
	 */
	public PamRawDataBlock getInputRawData() {
		return inputRawData;
	}

	/**
	 * @return the detectorDataBlock
	 */
	public PamDataBlock getDetectorDataBlock() {
		return detectorDataBlock;
	}

}
