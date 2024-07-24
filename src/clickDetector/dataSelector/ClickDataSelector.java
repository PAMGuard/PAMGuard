package clickDetector.dataSelector;

import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.alarm.ClickAlarmParameters;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;

public class ClickDataSelector extends DataSelector {

	private ClickControl clickControl;
	private ClickDataBlock clickDataBlock;
	private ClickAlarmParameters clickAlarmParameters = new ClickAlarmParameters();
	private ClickSelectPanel clickSelectPanel;
	private boolean useEventTypes;
	
	/**
	 * The JavaFX pane 
	 */
	private ClickSelectPaneFX clickSelectPaneFX;
	private boolean allowScores;

	public ClickDataSelector(ClickControl clickControl, ClickDataBlock clickDataBlock, 
			String selectorName, boolean allowScores, boolean useEventTypes) {
		super(clickDataBlock, selectorName, allowScores);
		this.clickControl = clickControl;
		this.clickDataBlock = clickDataBlock;
		this.useEventTypes = useEventTypes;
		this.allowScores = allowScores;

	}

	@Override
	public PamDialogPanel getDialogPanel() {
//		if (clickSelectPanel == null) {
			clickSelectPanel = new ClickSelectPanel(this, allowScores, useEventTypes);
//		}
		return clickSelectPanel;
	}
	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (clickSelectPaneFX == null) {
			clickSelectPaneFX = new ClickSelectPaneFX(this, allowScores, useEventTypes);
		}
		return clickSelectPaneFX;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#scoreData(PamguardMVC.PamDataUnit)
	 */
	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		ClickDetection click = (ClickDetection) pamDataUnit;
		if (clickAlarmParameters.useEchoes == false && click.isEcho()) {
			return 0;
		}
		
		double score = scoreClick(click);
		if (useEventTypes) {
			double eventScore = scoreEventType(click);
			if (clickAlarmParameters.isClicksANDEvents()) {
				score = Math.min(score, eventScore);
			}
			else {
				score = Math.max(score, eventScore);
			}
		}
		return score;
		
	}
	
	private double scoreClick(ClickDetection click) {
		if (click.getAmplitudeDB() < clickAlarmParameters.minimumAmplitude) {
			return 0;
		}
		ClickIdentifier clickIdentifier = clickControl.getClickIdentifier();
		int code = click.getClickType();
		if (code > 0 && clickIdentifier != null) {
			code = clickIdentifier.codeToListIndex(code) + 1;
		}
		boolean enabled = clickAlarmParameters.getUseSpecies(code);
		if (enabled == false) {
			return 0;
		}if (isAllowScores()) {
			return clickAlarmParameters.getSpeciesWeight(code);
		}
		else {
			return 1;
		}

	}

	/**
	 * Work out if the event type is wanted. 
	 * @param click
	 * @return
	 */
	private double scoreEventType(ClickDetection click) {
		OfflineEventDataUnit oev = null;
	
		try {
			oev = (OfflineEventDataUnit) click.getSuperDetection(OfflineEventDataUnit.class, true);
		}
		catch (Exception e) {
			
		}
		
		int eventId = click.getOfflineEventID();
		if (oev == null) {
			return clickAlarmParameters.unassignedEvents ? 1 : 0;
		}
		
		// see if there is a super detection and see if it's got a comment. 
		String comment = oev.getComment();
		
		boolean isAutomatic = false;
		if (comment != null) {
			isAutomatic = comment.startsWith("Automatic");
		}
		if (isAutomatic && clickAlarmParameters.onlineAutoEvents) {
			return 1;
		}
		else if (clickAlarmParameters.onlineManualEvents) {
			return 1;
		}
//			if (clickAlarmParameters.onlineAutoEvents && comment.startsWith("Automatic")) {
//				return true;
//			}
//			if (clickAlarmParameters.onlineManualEvents && comment.startsWith("Manual")) {
//				return true;
//			}
		/*
		 * Otherwise need to work out where the hell the event type is in the 
		 * list of event types and see if it's wanted. 
		 */	
		String evType = oev.getEventType();
		return clickAlarmParameters.isUseEventType(evType) ? 1 : 0;
	}


//	/* (non-Javadoc)
//	 * @see PamguardMVC.dataSelector.DataSelector#selectDialogToOpen()
//	 */
//	@Override
//	public void selectDialogToOpen() {
//		super.selectDialogToOpen();
//	}

	/**
	 * @return the clickControl
	 */
	public ClickControl getClickControl() {
		return clickControl;
	}

	/**
	 * @return the clickDataBlock
	 */
	public ClickDataBlock getClickDataBlock() {
		return clickDataBlock;
	}

	/**
	 * @return the clickAlarmParameters
	 */
	public ClickAlarmParameters getClickAlarmParameters() {
		return clickAlarmParameters;
	}

	/**
	 * @param clickAlarmParameters the clickAlarmParameters to set
	 */
	public void setClickAlarmParameters(ClickAlarmParameters clickAlarmParameters) {
		this.clickAlarmParameters = clickAlarmParameters;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#setParams(PamguardMVC.dataSelector.DataSelectParams)
	 */
	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof ClickAlarmParameters) {
			clickAlarmParameters = ((ClickAlarmParameters) dataSelectParams).clone();
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.dataSelector.DataSelector#getParams()
	 */
	@Override
	public ClickAlarmParameters getParams() {
		return clickAlarmParameters;
	}

}
