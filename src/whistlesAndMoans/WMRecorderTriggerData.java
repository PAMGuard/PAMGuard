package whistlesAndMoans;

import java.io.Serializable;

import SoundRecorder.trigger.RecorderTriggerData;

/**
 * Notes on ManagedParameters
 * - the superclass RecorderTriggerData extends ManagedParameters and manually adds in a few fields
 * in the getParameterSet method that would otherwise be missed (because they are protected and
 * do not have getters).  So we definitely need to run that
 * - if we extend ManagedParameters in this class, we would run this getParameterSet method instead
 * of the RecorderTriggerData.getParameterSet, and those extra fields would not be included
 * - the fields minFreq and maxFreq in this class are protected, which would be a problem except that
 * they also have getters so are included when RecorderTriggerData.getParameterSet is run
 * 
 * 
 * @author mo55
 *
 */
public class WMRecorderTriggerData extends RecorderTriggerData implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	protected double minFreq, maxFreq;
		
	public WMRecorderTriggerData(WhistleMoanControl wmControl) {
		super(wmControl.getUnitName(), 30, 60);
		setMinDetectionCount(3);
		setCountSeconds(5);
	}

	public double getMinFreq() {
		return minFreq;
	}

	public double getMaxFreq() {
		return maxFreq;
	}

	@Override
	public WMRecorderTriggerData clone() {
		return (WMRecorderTriggerData) super.clone();
	}

}
