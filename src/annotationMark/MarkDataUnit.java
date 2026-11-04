package annotationMark;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.superdet.SuperDetection;


public class MarkDataUnit extends PamDataUnit<PamDataUnit,SuperDetection> implements AcousticDataUnit, PamDetection {

	/**
	 * The raw data feeding the display the mark was made (or last touched) on. This
	 * may be a decimator rather than the sound acquisition, and it's the data the
	 * user was actually looking at, so it's what pop up spectrograms, playback and
	 * measurements should use. Not stored in the database, so null for marks loaded
	 * from the database until they're next touched on a display.
	 */
	private PamRawDataBlock rawSourceDataBlock;

	public MarkDataUnit(long timeMilliseconds, int channelBitmap, long millisDuration) {
		super(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setDurationInMilliseconds(millisDuration);
	}

	/**
	 * @return the raw data block feeding the display the mark was made on, or null if unknown.
	 */
	public PamRawDataBlock getRawSourceDataBlock() {
		return rawSourceDataBlock;
	}

	/**
	 * @param rawSourceDataBlock the raw data block feeding the display the mark was made on.
	 */
	public void setRawSourceDataBlock(PamRawDataBlock rawSourceDataBlock) {
		this.rawSourceDataBlock = rawSourceDataBlock;
	}

}
