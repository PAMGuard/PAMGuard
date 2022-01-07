package crossedbearinglocaliser;

import Array.ArrayManager;
import Array.PamArray;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamController.PamController;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.dataSelector.DataSelector;
import annotation.localise.targetmotion.TMAnnotation;
import crossedbearinglocaliser.CBDetectionMatcher.CBMatchGroup;
import generalDatabase.SQLLogging;

public class CBLocaliserProcess extends PamInstantProcess {

	private CBLocaliserControl cbLocaliserControl;
	private DataSelector dataSelector;
	/*
	 * Max phone separation in metres in the entire array
	 */
	private double maxPhoneSeparation;
	/*
	 * Max phone separation in seconds in the entire array
	 */
	private double maxPhoneDelaySecs;
	
	private CBDetectionMatcher cbDetectionMatcher;
	private long maxPhoneDelayMillis;

	public CBLocaliserProcess(CBLocaliserControl cbLocaliserControl) {
		super(cbLocaliserControl);
		this.cbLocaliserControl = cbLocaliserControl;
		setProcessName(cbLocaliserControl.getUnitName());
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		CBLocaliserSettngs params = cbLocaliserControl.getCbLocaliserSettngs();
		PamDataBlock parentDataBlock = PamController.getInstance().getDataBlockByLongName(params.getParentDataBlock());
		setParentDataBlock(parentDataBlock);
		if (parentDataBlock != null) {
			dataSelector = parentDataBlock.getDataSelector(cbLocaliserControl.getDataSelectorName(), false);
			parentDataBlock.addLocalisationContents(LocContents.HAS_LATLONG | LocContents.HAS_RANGE);
			SQLLogging logging = parentDataBlock.getLogging();
			if (logging != null) {
				logging.addAddOn(cbLocaliserControl.getTmAnnotationType().getSQLLoggingAddon());
			}
		}
		/*
		 * Have a wee think about maximum array dimensions ...
		 */
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		double[][] dimLims = currentArray.getDimensionLimits();
		double d = 0.;
		for (int i = 0; i < 3; i++) {
			d += Math.pow(dimLims[i][1]-dimLims[i][0], 2.);
		}
		maxPhoneSeparation = Math.sqrt(d);
		maxPhoneDelaySecs = maxPhoneSeparation / currentArray.getSpeedOfSound();
		maxPhoneDelayMillis = (long) (maxPhoneDelaySecs * 1000.);
		prevClockTime = 0;
		cbDetectionMatcher = new CBDetectionMatcher(currentArray.getSpeedOfSound());
	}

	@Override
	public void pamStart() { }

	@Override
	public void pamStop() {	}

	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {
		if (dataSelector != null) {
			if (dataSelector.scoreData(pamDataUnit) <= 0.) {
				return;
			}
		}
		processDataUnit(pamDataUnit);
	}
	
	

	@Override
	public void updateData(PamObservable o, PamDataUnit pamDataUnit) {
		if (dataSelector != null) {
			if (dataSelector.scoreData(pamDataUnit) <= 0.) {
				return;
			}
		}
		processDataUnit(pamDataUnit);
	}

	/**
	 * We've a data unit we want to process, so ...
	 * @param pamDataUnit
	 */
	private void processDataUnit(PamDataUnit pamDataUnit) {
		AbstractLocalisation loc = pamDataUnit.getLocalisation();
		if (loc == null) {
			return;
		}
		LatLong detLatLong = pamDataUnit.getOriginLatLong(false);
		if (detLatLong == null) {
			return;
		}
		CBMatchGroup newGroup = cbDetectionMatcher.newData(pamDataUnit);
		if (newGroup != null && newGroup.getSubDetectionsCount() >= cbLocaliserControl.getCbLocaliserSettngs().getMinDetections()) {
//			System.out.println("Crossed bearings - localise group now");
			localiseGroup(newGroup, pamDataUnit);
		}
	}

	private void localiseGroup(CBMatchGroup newGroup, PamDataUnit pamDataUnit) {
//		System.out.printf("Localise group with %d data units at %s: ", 
//			newGroup.getSubDetectionsCount(), PamCalendar.formatTime(pamDataUnit.getTimeMilliseconds(), true));
		TMAnnotation tmAnnotation = cbLocaliserControl.getTmAnnotationType().autoAnnotate(newGroup);
		if (tmAnnotation == null) {
			return;
		}
		GroupLocalisation gl = tmAnnotation.getGroupLocalisation();
		if (gl == null) {
			return;
		}
		pamDataUnit.addDataAnnotation(tmAnnotation);
		pamDataUnit.setLocalisation(gl);
//		System.out.printf("%s\n", tmAnnotation.getGroupLocalisation().toString());
		
	}

	long prevClockTime = 0;
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#masterClockUpdate(long, long)
	 */
	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		// need to chuck out any groups which don't have enough data to process....
		cbDetectionMatcher.cleanOldData(prevClockTime-maxPhoneDelayMillis, 5);
		prevClockTime = timeMilliseconds;
	}



}
