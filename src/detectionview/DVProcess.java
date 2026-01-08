package detectionview;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamRawDataBlock;
import annotation.handler.AnnotationHandler;
import annotation.userforms.UserFormAnnotationType;
import detectionview.annotate.DVAnnotationWrapper;

public class DVProcess extends PamguardMVC.PamProcess {
	
	private DVControl dvControl;
	
	private DVDataBlock dvDataBlock;

	private PamRawDataBlock inputRawData;

	private PamDataBlock detectorDataBlock;
	
	private DVLoader dvLoader;

	private DVAnnotationWrapper annotationHandler;

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
		if (newInput != null) {
			if (inputRawData != null) {
				inputRawData.deleteObserver(this);
			}
			inputRawData = newInput;
			inputRawData.addInstantObserver(this);
			setSampleRate(inputRawData.getSampleRate(), true);
		}
		
		sortAnnotationHandler(detectorDataBlock);
		
		dvControl.updateConfigObs();
		return super.prepareProcessOK();
	}

	private void sortAnnotationHandler(PamDataBlock detectorDataBlock) {
		if (annotationHandler == null || annotationHandler.getPamDataBlock() != detectorDataBlock) {
			if (detectorDataBlock.getAnnotationHandler() != null) {
				annotationHandler = new DVAnnotationWrapper(detectorDataBlock.getAnnotationHandler(), dvControl, detectorDataBlock);
			}
			else {
				annotationHandler = null;
			}
		}
//		annotationHandler = detectorDataBlock.getAnnotationHandler();
//		if (annotationHandler == null) {
//			annotationHandler = new AnnotationHandler(detectorDataBlock);
//		}
//		// make sure the annotation handler has some basic types of annotator
//		// for manual annotation
//		if (annotationHandler.findAnnotationType(UserFormAnnotationType.class) == null) {
//			annotationHandler.addAnnotationType(new UserFormAnnotationType(detectorDataBlock));
//		}
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

	/**
	 * @return the annotationHandler
	 */
	public DVAnnotationWrapper getAnnotationHandler() {
		return annotationHandler;
	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		if (inputRawData != null) {
			super.setSampleRate(inputRawData.getSampleRate(), false);
		}
		else {
			super.setSampleRate(sampleRate, notify);
		}
	}

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		// will get called when a new unit is created. 
		// should make a new DV display unit and insert it into the correct place.
		if (dvControl.isViewer()) {
			dvLoader.createNew(arg);
		}
	}

	@Override
	public void updateData(PamObservable o, PamDataUnit pamDataUnit) {
		// there is a possibility that the unit is now marked as deleted, in which case
		// remove it from displays. Otherwise consider updating the time range of the specified
		// display. 
		if (dvControl.isViewer()) {
			// find the current dvDataUnit. 
			DVDataUnit dvDataUnit = dvDataBlock.findDataForTrigger(pamDataUnit);
			if (dvDataUnit == null) {
				return; // consider creating with a call to dvLoader.createNew ?????
			}
			if (pamDataUnit.isDeleted()) {
				dvDataBlock.remove(dvDataUnit);
				dvDataBlock.updateObservers(dvDataUnit);
//				dvDataBlock.updatePamData(dvDataUnit, System.currentTimeMillis());
			}
		}
		
	}
	
	

}
