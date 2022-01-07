package PamguardMVC.background;

import Acquisition.AcquisitionProcess;
import PamUtils.PamUtils;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;

public abstract class BackgroundDataUnit extends PamDataUnit {

	public BackgroundDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
	}

	public BackgroundDataUnit(long timeMilliseconds, int channelBitmap, double durationMillis) {
		super(timeMilliseconds);
		setChannelBitmap(channelBitmap);
		setDurationInMilliseconds(durationMillis);
	}

	public BackgroundDataUnit(DataUnitBaseData basicData) {
		super(basicData);
	}

	abstract public double getCountSPL();
	
	public double getAbsSLP() {
		double cspl = getCountSPL();
		PamDataBlock pdb = getParentDataBlock();
		PamProcess pp = pdb.getParentProcess();
		AcquisitionProcess ap = (AcquisitionProcess) pp.getRawSourceDataBlock().getSourceProcess();
		return ap.rawAmplitude2dB(cspl, PamUtils.getLowestChannel(getChannelBitmap()), true);
	}

}
