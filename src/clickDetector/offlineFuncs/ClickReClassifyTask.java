package clickDetector.offlineFuncs;

import PamUtils.PamUtils;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickTabPanelControl;
import clickDetector.ClickClassifiers.ClickIdInformation;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.ClickClassifiers.annotation.ClickClassifierAnnotation;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;


public class ClickReClassifyTask extends OfflineTask<ClickDetection> {

	private ClickControl clickControl;
	
	private ClicksOffline clicksOffline;

	private ClickIdentifier clickClassifier;
	
	/**
	 * @param clickControl
	 */
	public ClickReClassifyTask(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		clicksOffline = clickControl.getClicksOffline();
//		setParentDataBlock(clickControl.getClickDataBlock());
		addAffectedDataBlock(clickControl.getClickDataBlock());
	}

	@Override
	public String getName() {
		return "Reclassify Clicks";
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processDataUnit(ClickDetection click) {
		ClickIdInformation idInfo = clickClassifier.identify(click);
//		click.freeClickMemory();
		if (idInfo.clickType != click.getClickType()) {
//			if (click.getAmplitudeDB()>150) System.out.println("Not the same click!" + idInfo.clickType+" channel: "+PamUtils.getSingleChannel(click.getChannelBitmap()));
			click.setClickType((byte) idInfo.clickType);
//			if (click.getAmplitudeDB()>150) System.out.println("Type Now: "+click.getClickType());
//			click.getDataUnitFileInformation().setNeedsUpdate(true);
			return true;
		}
		
//		System.out.println("Classifiers passed!!! " + idInfo.classifiersPassed + " Click type: " + click.getClickType()); 
		//Also have to deal with annotations...
		if (idInfo.classifiersPassed!=null) {
			click.addDataAnnotation(new ClickClassifierAnnotation(clickControl.getClickDetector().getClickAnnotationType(), idInfo.classifiersPassed));
			return true;
		}
		
		return false;
	}

	@Override
	public void loadedDataComplete() {
		ClickTabPanelControl ctpc = (ClickTabPanelControl) clickControl.getTabPanel();
		if (ctpc != null) {
			ctpc.offlineDataChanged();
		}
	}


	@Override
	public boolean callSettings() {
		return clickControl.classificationDialog(clickControl.getPamView().getGuiFrame());
	}


	@Override
	public boolean hasSettings() {
		return true;
	}

	@Override
	public void prepareTask() {
		clickClassifier = clickControl.getClickIdentifier();
	}

}
