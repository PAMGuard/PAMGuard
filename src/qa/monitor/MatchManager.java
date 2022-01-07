package qa.monitor;

import java.util.Hashtable;

import PamguardMVC.PamDataBlock;
import listening.ThingHeard;
import printscreen.PrintScreenDataUnit;

/**
 * Manage detection matchers and their settings. 
 * @author dg50
 *
 */
public class MatchManager {

	private Hashtable<PamDataBlock, DetectionMatcher> detectionMatchers;
	private QAMonitorProcess qaMonitorProcess;
	
	private PamDataBlock lastDataBlock = null;
	private DetectionMatcher lastMatcher = null;
	
	public MatchManager(QAMonitorProcess qaMonitorProcess) {
		this.qaMonitorProcess = qaMonitorProcess;
		detectionMatchers = new Hashtable<>();
	}

	public DetectionMatcher getDetectionMatcher(PamDataBlock detectorDataBlock) {
		if (detectorDataBlock == lastDataBlock) {
			return lastMatcher;
		}
		DetectionMatcher matcher = detectionMatchers.get(detectorDataBlock);
		if (matcher == null) {
			matcher = createMatcher(detectorDataBlock);
			detectionMatchers.put(detectorDataBlock, matcher);
		}
		lastDataBlock = detectorDataBlock;
		lastMatcher = matcher;
		return matcher;
	}

	/**
	 * Make a detection matcher based on the type of detector data. Annoying that this is here and is
	 * not somehow part of the datablocks of each detector, but I can't see a way of doing that. 
	 * @param detectorDataBlock
	 * @return Detection matcher. 
	 */
	private DetectionMatcher createMatcher(PamDataBlock detectorDataBlock) {
		Class detClass = detectorDataBlock.getUnitClass();
		if (detClass == ThingHeard.class) {
			return new TimeWindowMatcher(30000, 0);
		}
		else if (detClass == PrintScreenDataUnit.class) {
			return new TimeWindowMatcher(30000, 0);
		}
		
		return new TimeFreqDetectionMatcher();
	}

}
