package bearinglocaliser;

import PamController.PamController;
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

	public DetectionMonitor(BearingLocaliserControl bearingLocaliserControl) {
		super(bearingLocaliserControl);
		this.bearingLocaliserControl = bearingLocaliserControl;
		setProcessName("Detection Monitor");
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
		PamDataBlock sourceData = PamController.getInstance().getDataBlockByLongName(params.detectionSource);
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
		bearingLocaliserControl.estimateBearings(triggerData);
		cpuMonitor.stop();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void updateData(PamObservable o, PamDataUnit triggerData) {
		/*
		 * 		Mustn't have this or when a unit has come in newData it comes in here again after and goes into 
		 *      an infinite loop.  
		 */
		//		cpuMonitor.start();
		//		bearingLocaliserControl.estimateBearings(triggerData);
		//		cpuMonitor.stop();
	}





}
