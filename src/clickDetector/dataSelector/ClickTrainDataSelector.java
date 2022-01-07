package clickDetector.dataSelector;

import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamUtils.LatLong;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class ClickTrainDataSelector extends DataSelector {

	private OfflineEventDataBlock offlineEventDataBlock;
	private ClickControl clickControl;
	private ClickTrainSelectParameters ctSelectParams = new ClickTrainSelectParameters();
	
	private ClickTrainSelectPanel clickTrainSelectPanel;
	private OfflineEventDataUnit lastPlottedEvent;
	
	public ClickTrainDataSelector(ClickControl clickControl, OfflineEventDataBlock offlineEventDataBlock, String selectorName, boolean allowScores) {
		super(offlineEventDataBlock, selectorName, allowScores);
		this.clickControl = clickControl;
		this.offlineEventDataBlock = offlineEventDataBlock;
		
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (clickTrainSelectPanel == null) {
			clickTrainSelectPanel = new ClickTrainSelectPanel(this);
		}
		return clickTrainSelectPanel;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		OfflineEventDataUnit event = (OfflineEventDataUnit) pamDataUnit;
		boolean isAuto = isAutoEvent(event);  
		if (isAuto && ctSelectParams.showAutoTrains == false) {
			return 0;
		}
		if (!isAuto && ctSelectParams.showManualTrains == false) {
			return 0;
		}
		boolean hasLatLong = hasLatLong(event);
		if (hasLatLong) {
			return 1;
		}
		return plotBearingOnly(event) ? 1 : 0;	
	}

	private boolean plotBearingOnly(OfflineEventDataUnit event) {
		switch (ctSelectParams.showShortTrains) {
		case  ClickParameters.LINES_SHOW_NONE:
			return false;
		case ClickParameters.LINES_SHOW_ALL:
			return true;
		default:
			return plotSomeBearings(event);
		}
	}

	private boolean plotSomeBearings(OfflineEventDataUnit event) {
		boolean plotIt = testEvent(event);
		if (plotIt) {
			lastPlottedEvent = event;
		}
		return plotIt;
	}

	/**
	 * Test to see if we should plot this event. 
	 * @param event
	 * @return always true in viewer. Otherwise depends on plot options. 
	 */
	private boolean testEvent(OfflineEventDataUnit event) {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return true;
		}
		int evIndex = event.getAbsBlockIndex();
		OfflineEventDataUnit lastunit = offlineEventDataBlock.getLastUnit();
		if (event == lastunit) {
			return true; // always plot the last event. 
		}
		OfflineEventDataUnit firstunit = offlineEventDataBlock.getFirstUnit();
		boolean isFirst = firstunit == event;
		if (isFirst || lastPlottedEvent == null) {
			return true;
		}
		long dt = event.getTimeMilliseconds() - lastPlottedEvent.getTimeMilliseconds();
		if (dt >= (long) (ctSelectParams.minTimeSeparation * 1000.)) {
			return true;
		}
		AbstractLocalisation thisLoc = event.getLocalisation();
		AbstractLocalisation lastLoc = lastPlottedEvent.getLocalisation();
		if (thisLoc == null) {
			System.out.printf("Event %d has null localisation\n", event.getEventId());
			return false;
		}
		if (lastLoc == null) {
			return true;
		}
		double[] thisAngles = thisLoc.getAngles();
		double[] lastAngles = lastLoc.getAngles();
		if (thisAngles == null || thisAngles.length < 1) {
			return false;
		}
		if (lastAngles == null || lastAngles.length < 1) {
			return true;
		}
		double angDiff = Math.abs(PamUtils.PamUtils.constrainedAngle(Math.toDegrees(lastAngles[0]-thisAngles[0]), 180.));
		return angDiff > ctSelectParams.minBearingSeparation;
		
	}

	/**
	 * 
	 * @param event
	 * @return true if the event has a position. 
	 */
	private boolean hasLatLong(OfflineEventDataUnit event) {
		AbstractLocalisation loc = event.getLocalisation();
		if (loc == null) {
			return false;
		}
		LatLong ll = loc.getLatLong(0);
		return ll != null;
	}

	/**
	 * . 
	 * @param event
	 * @return true if the event was created automatically
	 */
	private boolean isAutoEvent(OfflineEventDataUnit event) {
		if (event == null) return false;
		String com = event.getComment();
		if (com == null) {
			return false;
		}
		return com.startsWith("Automatic");
	}


	/**
	 * @return the ctSelectParams
	 */
	public ClickTrainSelectParameters getCtSelectParams() {
		return ctSelectParams;
	}

	/**
	 * @param ctSelectParams the ctSelectParams to set
	 */
	public void setCtSelectParams(ClickTrainSelectParameters ctSelectParams) {
		this.ctSelectParams = ctSelectParams;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#setParams(PamguardMVC.dataSelector.DataSelectParams)
	 */
	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		ctSelectParams = (ClickTrainSelectParameters) dataSelectParams;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#getParams()
	 */
	@Override
	public DataSelectParams getParams() {
		return ctSelectParams;
	}

}
