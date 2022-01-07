package videoRangePanel.importTideData;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class TideDatagramProvider implements DatagramProvider{
	
	private DatagramScaleInformation scaleInfo;
	
	public TideDatagramProvider() {
		scaleInfo = new DatagramScaleInformation(-10, 10, "m", false, DatagramScaleInformation.PLOT_2D);
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		TideDataUnit dbhtDataunit = (TideDataUnit) dataUnit;
//		System.out.println("TIDE: ataGramLine: " + dbhtDataunit.getLevel()); 
		dataGramLine[0] = (float) dbhtDataunit.getLevel(); 
		return 1;
	}


	@Override
	public int getNumDataGramPoints() {
		return 1;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return scaleInfo;
	}

}
