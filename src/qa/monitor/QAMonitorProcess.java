package qa.monitor;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetection;
import qa.QAControl;
import qa.QASoundDataBlock;
import qa.QASoundDataUnit;
import qa.database.QASoundLogging;

/**
 * Process to monitor output of all other detector data blocks. 
 * @author dg50
 *
 */
public class QAMonitorProcess extends PamProcess {

	private ArrayList<PamDataBlock> allDetectors;
	
	private QAControl qaControl;
	private QASoundDataBlock soundsDataBlock;
	
	private MatchManager matchManager;

	public QAMonitorProcess(QAControl qaControl) {
		super(qaControl, null);
		this.qaControl = qaControl;
		soundsDataBlock = qaControl.getQaGeneratorProcess().getSoundsDataBlock();
		matchManager = new MatchManager(this);
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
		/**
		 * Make this process observe all data blocks, but don't 
		 */
		allDetectors = makeDetectorMasterList();
		
		for (PamDataBlock detBlock:allDetectors) {
			detBlock.addObserver(this);
		}
		qaControl.checkDatabase(allDetectors);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit detection) {
		/*
		 * Called whenever a data unit form any of the detectors or other 
		 * things that might match a detection arrives. 
		 */
		checkMatches(o, detection, false);
	}
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void updateData(PamObservable o, PamDataUnit detection) {
		checkMatches(o, detection, true);
	}

	public boolean checkMatches(PamObservable o, PamDataUnit detection, boolean isUpdate) {
		return checkMatches(o, detection, detection.getUID(), isUpdate);
	}

	/**
	 * Check this detection (or other type of data) to see if it matches with any of
	 * the generated sounds currently held in memory. 
	 * @param o
	 * @param detection
	 * @param detectionUID
	 * @param isUpdate
	 * @return
	 */
	public boolean checkMatches(PamObservable o, PamDataUnit detection, long detectionUID, boolean isUpdate) {
		/**
		 * Assume that it's likely to be very recent sounds that match, so 
		 * work backwards from the end. 
		 * 
		 * Should really speed this up by only looking for the first sound to match, but 
		 * this is good for debugging. 
		 */
		/*
		 * think about click trains with sub detections. Really we only want to check the sub detections, 
		 * not the entire event - which woule be too long. We also want to avoid re-doing every  
		 * sub detection every time the train is updated, so perhaps just do the sub detection ?  
		 */
		if (detection instanceof SuperDetection) {
			SuperDetection superDetection = (SuperDetection) detection;
			synchronized (superDetection.getSubDetectionSyncronisation()) {
				int nSubs = superDetection.getSubDetectionsCount();
				/*
				 * in these callbacks, make sure to pass on the datablock of THIS data unit
				 * so that the match gets attributed to the super detections datablock
				 */
				if (nSubs > 0) {
					if (isUpdate) {
						// only do the last sub-detection
						PamDataUnit lastSub = superDetection.getSubDetection(nSubs-1);
						return checkMatches(o, lastSub, detectionUID, false);
					}
					else {
						// do all sub detections
						boolean matched = false;
						for (int i = 0; i < nSubs; i++) {
							matched |= checkMatches(o, superDetection.getSubDetection(i), detectionUID, false);
						}
						return matched;
					}
				}
			}
		}
		int nMatches = 0;
		PamDataBlock detectorDataBlock = (PamDataBlock) o;
		DetectionMatcher detectionMatcher = matchManager.getDetectionMatcher(detectorDataBlock);
		synchronized (soundsDataBlock.getSynchLock()) {
			ListIterator<QASoundDataUnit> soundsIt = soundsDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
//			int bestOverlap = Integer.MIN_VALUE;
//			QASoundDataUnit bestSound = null;
			while (soundsIt.hasPrevious()) {
				QASoundDataUnit genSound = soundsIt.previous();
				if (genSound.getState() == QASoundDataUnit.SOUND_NOT_STARTED) {
					continue;
				}
				double overlap = detectionMatcher.getOverlap(detection, genSound);
				if (overlap > 0) {
					genSound.setDetectorHit(detectorDataBlock, Math.max(detection.getChannelBitmap(), 1));
					soundsDataBlock.updatePamData(genSound, detection.getTimeMilliseconds());
//					Debug.out.printf("%s matched to %s\n", PamCalendar.formatDateTime(genSound.getTimeMilliseconds()),
//							detection.getSummaryString());
					nMatches++;
				}
//				int overlap = getOverlap(genSound, detection);
//				if (overlap > bestOverlap){
//					bestOverlap = overlap;
//					bestSound = genSound;
//				}
//				if (overlap >= 0) {
////					break; // stop looking - can comment out for debugging. 
//				}
			}
//			if (bestOverlap >= 0) {
//				if (bestSound.setDetectorHit((PamDataBlock) o, detection)) {
//					soundsDataBlock.updatePamData(bestSound, detection.getTimeMilliseconds());
//				}
////				System.out.printf("Sound Match olap %d det at %s, gen at %s\n", bestOverlap,
////						PamCalendar.formatTime(detection.getTimeMilliseconds(), true),
////						PamCalendar.formatTime(bestSound.getArrivalStartMillis(), true));
//				return true;
//			}
//			else if (bestSound != null) {
//				PamDataBlock db = (PamDataBlock) o;
//				System.out.printf("No Sound Match for %s olap %d det at %s, gen at %s\n", 
//						db.getDataName(), bestOverlap,
//						PamCalendar.formatTime(detection.getTimeMilliseconds(), true),
//						PamCalendar.formatTime(bestSound.getArrivalStartMillis(), true));
//			}
//			else {
//				System.out.println("No Gen sounds to Match");
//			}
			return false;
		}
	}

	/**
	 * Is there adequate overlap between the generated sound and the detection ...
	 * @param genSound
	 * @param detection
	 * @return
	 */
	private int getOverlap(QASoundDataUnit genSound, PamDataUnit detection) {
		long tOverlap = Math.min(genSound.getArrivalEndMillis(), detection.getEndTimeInMilliseconds()) - 
				Math.max(genSound.getArrivalStartMillis(), detection.getTimeMilliseconds());

		return (int) tOverlap; // (genSound.getArrivalEndMillis() - ;
	}

	/**
	 * Get a list of detectors to be monitored by the QA module. 
	 * @return list of detectors to monitor. 
	 */
	private ArrayList<PamDataBlock> makeDetectorMasterList() {
		ArrayList<PamDataBlock> allDetectors = PamController.getInstance().getDetectorDataBlocks();
		ListIterator<PamDataBlock> it = allDetectors.listIterator();
		while (it.hasNext()) {
			PamDataBlock dataBlock = it.next();
			if (dataBlock.getDataName().endsWith("Noise Data Samples")) {
				it.remove();
			}
		}
		return allDetectors;
	}

	/**
	 * @return the allDetectors
	 */
	public ArrayList<PamDataBlock> getAllDetectors() {
		return allDetectors;
	}


}
