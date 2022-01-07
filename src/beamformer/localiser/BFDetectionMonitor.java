package beamformer.localiser;

import PamController.PamController;
import PamDetection.LocContents;
import PamUtils.CPUMonitor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import generalDatabase.SQLLogging;

public class BFDetectionMonitor extends PamInstantProcess {

	private BeamFormLocaliserControl beamFormLocaliserControl;
	
	private CPUMonitor cpuMonitor = new CPUMonitor();

	public BFDetectionMonitor(BeamFormLocaliserControl beamFormLocaliserControl) {
		super(beamFormLocaliserControl);
		this.beamFormLocaliserControl = beamFormLocaliserControl;
		setProcessName("Detection Monitor");
	}

	@Override
	public void pamStart() {
		cpuMonitor.reset();
	}

	@Override
	public void pamStop() {
		System.out.println(cpuMonitor.getSummary("Beamformer localiser:"));
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
		BFLocaliserParams params = beamFormLocaliserControl.getBfLocaliserParams();
		if (params.detectionSource == null) {
			return;
		}
		PamDataBlock sourceData = PamController.getInstance().getDataBlockByLongName(params.detectionSource);
		setParentDataBlock(sourceData);
		if (sourceData != null) {
			/*
			 * Tell the source that it has bearing data. 
			 */
			sourceData.addLocalisationContents(LocContents.HAS_BEARING);
			SQLLogging logging = sourceData.getLogging();
			if (logging != null) {
				logging.addAddOn(beamFormLocaliserControl.getBfAnnotationType().getSQLLoggingAddon());
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit triggerData) {
		// double[] f = triggerData.getFrequency();
		// /**
		// * At the current time, the double precision on a millisecond time
		// * is about 2.1e-4ms, so can use doubles for time in millis which will give
		// * < 1 sample accuracy if needed.
		// */
		// PamDataBlock sourceData = (PamDataBlock) o;
		// float sourceSampleRate = sourceData.getSampleRate();
		// long tStartMillis = triggerData.getTimeMilliseconds();
		// Double millisDuration = triggerData.getDurationInMilliseconds();
		// if (millisDuration == null) {
		// millisDuration = 1000.; // set a default of one second ?
		// }
		// double fD = millisDuration;
		// long tEndMillis = tStartMillis + (long) fD;
		// double[] timeRange = {tStartMillis, tEndMillis};
		cpuMonitor.start();
		beamFormLocaliserControl.beamFormDataUnit(triggerData);
		cpuMonitor.stop();
//		beamFormLocaliserControl.newTriggerData(triggerData);
	}



}
