package bearinglocaliser;

import PamDetection.LocContents;
import PamUtils.CPUMonitor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import generalDatabase.SQLLogging;

public class DetectionMonitor extends PamInstantProcess {

	private BearingLocaliserControl bearingLocaliserControl;

	private CPUMonitor cpuMonitor = new CPUMonitor();

	private BearingUpdateScheduler updateScheduler;

	public DetectionMonitor(BearingLocaliserControl bearingLocaliserControl) {
		super(bearingLocaliserControl);
		this.bearingLocaliserControl = bearingLocaliserControl;
		setProcessName("Detection Monitor");
		updateScheduler = new BearingUpdateScheduler(bearingLocaliserControl);
	}

	@Override
	public void pamStart() {
		cpuMonitor.reset();
	}

	@Override
	public void pamStop() {
		System.out.println(cpuMonitor.getSummary(bearingLocaliserControl.getUnitName() + ": "));
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		if (params.detectionSource == null) {
			return;
		}
		PamDataBlock sourceData = bearingLocaliserControl.getPamConfiguration().getDataBlockByLongName(params.detectionSource);
		setParentDataBlock(sourceData);
		if (sourceData != null) {
			/*
			 * Tell the source that it has bearing data. 
			 */
			sourceData.addLocalisationContents(LocContents.HAS_BEARING);
			SQLLogging logging = sourceData.getLogging();
			if (logging != null) {
				logging.addAddOn(bearingLocaliserControl.getBearingAnnotationType().getSQLLoggingAddon());
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit triggerData) {
		cpuMonitor.start();
		updateScheduler.newDataUnit(triggerData);
		cpuMonitor.stop();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void updateData(PamObservable o, PamDataUnit triggerData) {
		/*
		 * Adding the bearing annotation updates the unit, which brings it straight back
		 * here, so this used to be left out to avoid an infinite loop. The scheduler
		 * only recalculates if the unit's time / frequency geometry has changed since
		 * its bearing was calculated (e.g. a spectrogram annotation that's been moved
		 * or resized), which breaks the loop, and does the work off the GUI threads.
		 */
		updateScheduler.updatedDataUnit(triggerData);
	}

	/**
	 * @return the scheduler which recalculates bearings when units change.
	 */
	public BearingUpdateScheduler getUpdateScheduler() {
		return updateScheduler;
	}





}
