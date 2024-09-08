package clickDetector;

import SoundRecorder.trigger.RecorderTrigger;
import SoundRecorder.trigger.RecorderTriggerData;

public class ClickRecorderTrigger extends RecorderTrigger {
	
	private ClickRecorderTriggerData crtData;
	private ClickControl clickControl;

	/**
	 * @param clickControl
	 */
	public ClickRecorderTrigger(ClickControl clickControl, ClickDataBlock clickDataBlock) {
		super(clickDataBlock);
		this.clickControl =clickControl;
	}

	@Override
	public RecorderTriggerData getDefaultTriggerData() {
		if (crtData == null) {
			crtData = new ClickRecorderTriggerData(clickControl);
		}
		return crtData;
	}

//	/* (non-Javadoc)
//	 * @see SoundRecorder.trigger.RecorderTrigger#hasOptions()
//	 */
//	@Override
//	public boolean hasOptions() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see SoundRecorder.trigger.RecorderTrigger#showOptionsDialog(java.awt.Window, SoundRecorder.trigger.RecorderTriggerData)
//	 */
//	@Override
//	public boolean showOptionsDialog(Window frame,
//			RecorderTriggerData triggerData) {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see SoundRecorder.trigger.RecorderTrigger#triggerDataUnit(PamguardMVC.PamDataUnit, SoundRecorder.trigger.RecorderTriggerData)
//	 */
//	@Override
//	public boolean triggerDataUnit(PamDataUnit dataUnit,
//			RecorderTriggerData rtData) {
//		return true;
//	}

}
