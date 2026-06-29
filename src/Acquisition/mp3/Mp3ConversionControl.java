package Acquisition.mp3;

import PamController.PamControlledUnit;

public class Mp3ConversionControl extends PamControlledUnit{
	
	private Mp3ConversionProcess conversionProcess;
	public Mp3ConversionParams converterParams;

	public Mp3ConversionControl(String unitName) {
		super("Mp3Converter", unitName);
		addPamProcess(conversionProcess = new Mp3ConversionProcess(this));
	}

}
