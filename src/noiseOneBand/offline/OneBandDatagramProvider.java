package noiseOneBand.offline;

import noiseOneBand.OneBandControl;
import noiseOneBand.OneBandDataUnit;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;

public class OneBandDatagramProvider implements DatagramProvider{

	private OneBandControl oneBandControl;
	
	private DatagramScaleInformation scaleInfo;
	
	public OneBandDatagramProvider(OneBandControl oneBandControl) {
		this.oneBandControl = oneBandControl;
		scaleInfo = new DatagramScaleInformation(Double.NaN, Double.NaN, "dB", false, DatagramScaleInformation.PLOT_2D);
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		OneBandDataUnit dbhtDataunit = (OneBandDataUnit) dataUnit;
		dataGramLine[0] = addDBValues(dataGramLine[0], dbhtDataunit.getRms());
		dataGramLine[1] = addDBValues(dataGramLine[1], dbhtDataunit.getZeroPeak());
		dataGramLine[2] = addDBValues(dataGramLine[2], dbhtDataunit.getPeakPeak());
		return 1;
	}
	
	private float addDBValues(double val1, double val2) {
		return (float) (10.*Math.log10(Math.pow(10., val1/10.) + Math.pow(10., val2/10.)));
	}

	@Override
	public int getNumDataGramPoints() {
		return 3;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		return scaleInfo;
	}

}
