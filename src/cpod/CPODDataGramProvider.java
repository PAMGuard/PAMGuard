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
		
		int minKhz = cpodClick.getkHz()- cpodClick.getBw()/2;
		int maxkHz = cpodClick.getkHz()- cpodClick.getBw()/2;
		//each datagram line is a 1kHz bin
		for (int i=0; i<dataGramLine.length; i++) {
				if(i>=minKhz && i<=maxkHz) {
					dataGramLine[i] ++;
				}
		}
		
//		dataGramLine[cpodClick.getkHz()] ++;
		return 1;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return cpodScaleInfo;
	}

}
