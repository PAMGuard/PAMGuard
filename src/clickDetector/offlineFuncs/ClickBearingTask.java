package clickDetector.offlineFuncs;

import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickLocalisation;
import clickDetector.ClickTabPanelControl;
import clickDetector.ClickDetector.ChannelGroupDetector;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import PamUtils.PamUtils;
import PamguardMVC.debug.Debug;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

public class ClickBearingTask extends OfflineTask<ClickDetection> {

	private ClickControl clickControl;
	
	private ClicksOffline clicksOffline;
	
	private BearingLocaliser bearingLocaliser;

	private double sampleRate;
	
	/**
	 * @param clickControl
	 */
	public ClickBearingTask(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		clicksOffline = clickControl.getClicksOffline();
//		setParentDataBlock(clickControl.getClickDataBlock());
		addAffectedDataBlock(clickControl.getClickDataBlock());
		prepareLocalisers();
	}
	
	private void prepareLocalisers() {
		int n = clickControl.getClickDetector().getnChannelGroups();
		ChannelGroupDetector gd;
		BearingLocaliser bl;
		int[] groupPhones;
		for (int i = 0; i < n; i++) {
			gd = clickControl.getClickDetector().getChannelGroupDetector(i);

			int[] phones = gd.getGroupHydrophones();
			bl = gd.getBearingLocaliser();
			if (bl != null) {
				bl.prepare(phones,0, Correlations.defaultTimingError(clickControl.getClickDetector().getSampleRate()));
			}
		}
	}

	@Override
	public String getName() {
		return "Click bearings";
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean processDataUnit(ClickDetection click) {

		double[][] angles = null;
		double[] delays = click.getDelaysInSamples();
		long[] interestingclicks = {53009719, 53009443};
		if (click.getUID() == interestingclicks[1]) {
			Debug.out.printf("Process click bearings for UID " + click.getUID());
		}
		/*
		 * Need to copy array otherwise we write over the old delays in the click 
		 * (which is disastrous !). 
		 */
		double[] delaySecs = new double[delays.length];
		for (int i = 0; i < delaySecs.length; i++) {
			delaySecs[i] = delays[i] / sampleRate;
		}
		bearingLocaliser = click.getChannelGroupDetector().getBearingLocaliser();
		if (bearingLocaliser != null) {
//			if (click.getUID() == 892017319) {
//				System.out.println("This click");
//			}
			angles = bearingLocaliser.localise(delaySecs, click.getTimeMilliseconds());
//			if (click.getUID() == 4158004564L) {
//				System.out.println("Click " + click.getUID());
//			}
		}
		
		ClickLocalisation clickLoc = click.getClickLocalisation();		
		if (clickLoc != null) {
			clickLoc.setAnglesAndErrors(angles);
			clickLoc.setSubArrayType(bearingLocaliser.getArrayType());
			clickLoc.setArrayAxis(bearingLocaliser.getArrayAxis());
		}
		return true;
	}
	
	@Override
	public void prepareTask() {
		sampleRate = clickControl.getClickDataBlock().getSampleRate();
	}

	@Override
	public void loadedDataComplete() {
		ClickTabPanelControl ctpc = (ClickTabPanelControl) clickControl.getTabPanel();
		if (ctpc != null) {
			ctpc.offlineDataChanged();
		}
	}

}
