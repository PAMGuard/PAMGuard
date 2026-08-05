package noiseOneBand;

import jsonStorage.JSONObjectData;

public class OneBandJsonData extends JSONObjectData{
	
	double rms;
	double peakpeak;
	double zeropeak;
	double sel;
	long millis;
	String buoyId;

	public OneBandJsonData() {
		super();
	}

}
