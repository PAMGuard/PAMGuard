package RightWhaleEdgeDetector;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class RWEDatagramProvider implements DatagramProvider{

	private DatagramScaleInformation scaleInfo;

	public RWEDatagramProvider() {
		super();
		scaleInfo = new DatagramScaleInformation(0, 11, "type");
	}

	@Override
	public int getNumDataGramPoints() {
		return RWStandardClassifier.MAXSOUNDTYPE+1;
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		int type = ((RWEDataUnit) dataUnit).rweSound.soundType;
//		if (type >= 0 && type < RWStandardClassifier.MAXSOUNDTYPE) {
			dataGramLine[type] += 1;
			return 1;
//		}
//		return 0;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return scaleInfo;
	}

}
