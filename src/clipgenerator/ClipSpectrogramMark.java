package clipgenerator;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;

public class ClipSpectrogramMark extends PamDataUnit<PamDataUnit,SuperDetection> implements PamDetection {

	public ClipSpectrogramMark(long timeMilliseconds, int channelBitmap,
			long startSample, long duration, double[] frequency) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.setFrequency(frequency);
	}

}
