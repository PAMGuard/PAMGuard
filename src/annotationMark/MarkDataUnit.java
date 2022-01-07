package annotationMark;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;


public class MarkDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit, PamDetection {

	public MarkDataUnit(long timeMilliseconds, int channelBitmap, long millisDuration) {
		super(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setDurationInMilliseconds(millisDuration);
	}

}
