package cpod;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class CPODDataGramProvider implements DatagramProvider {

	private PamControlledUnit cpodControl;
	
	private DatagramScaleInformation cpodScaleInfo;

	public CPODDataGramProvider(PamControlledUnit cpodControl) {
		this.cpodControl = cpodControl;
		cpodScaleInfo = new DatagramScaleInformation(0, 255, "kHz");
	}

	@Override
	public int getNumDataGramPoints() {
		return 256;
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		CPODClick cpodClick = (CPODClick) dataUnit;
		dataGramLine[cpodClick.getkHz()] ++;
		return 1;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return cpodScaleInfo;
	}

}
