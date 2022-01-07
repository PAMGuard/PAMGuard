package alarm.actions.sound;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import alarm.AlarmParameters;

public class PlaySoundParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public File[] soundFile = new File[AlarmParameters.COUNT_LEVELS];

	@Override
	protected PlaySoundParams clone() {
		try {
			PlaySoundParams newParams = (PlaySoundParams) super.clone();
			if (newParams.soundFile == null) {
				newParams.soundFile = new File[AlarmParameters.COUNT_LEVELS];
			}
			if (newParams.soundFile.length != AlarmParameters.COUNT_LEVELS) {
				newParams.soundFile = Arrays.copyOf(newParams.soundFile, AlarmParameters.COUNT_LEVELS);
			}
			return newParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
	

}
