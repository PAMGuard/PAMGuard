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
	

}
