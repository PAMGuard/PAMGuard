package group3dlocaliser;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class GroupLocDataUnit extends PamDataUnit {

	public GroupLocDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		// TODO Auto-generated constructor stub
	}

	public GroupLocDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		// TODO Auto-generated constructor stub
	}

	public GroupLocDataUnit(DataUnitBaseData basicData) {
		super(basicData);
		// TODO Auto-generated constructor stub
	}

}
