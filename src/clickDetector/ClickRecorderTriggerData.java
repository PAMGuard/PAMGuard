package clickDetector;

import java.io.Serializable;

import SoundRecorder.trigger.RecorderTriggerData;

public class ClickRecorderTriggerData extends RecorderTriggerData implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ClickRecorderTriggerData(ClickControl clickControl) {
		super(clickControl.getUnitName() + " Clicks", 5, 20);
		setCountSeconds(5);
		setMinDetectionCount(10);
		setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see SoundRecorder.trigger.RecorderTriggerData#clone()
	 */
	@Override
	public ClickRecorderTriggerData clone() {
		return (ClickRecorderTriggerData) super.clone();
	}

}
