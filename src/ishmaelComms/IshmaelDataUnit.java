package ishmaelComms;

import PamguardMVC.PamDataUnit;

public class IshmaelDataUnit extends PamDataUnit {

	private IshmaelData ishmaelData;

	public IshmaelDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		// TODO Auto-generated constructor stub
	}

	public IshmaelData getIshmaelData() {
		return ishmaelData;
	}

	public void setIshmaelData(IshmaelData ishmaelData) {
		this.ishmaelData = ishmaelData;
	}

	@Override
	public double[] getFrequency() {
		if (ishmaelData.lofreq == 0 && ishmaelData.hifreq == 0) {
			return null;
		}
		double[] freq = new double[2];
		freq[0] = ishmaelData.lofreq;
		freq[1] = ishmaelData.hifreq;
		return freq;
	}
	

}
