package dbht.offline;

import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import dbht.DbHtControl;
import dbht.DbHtDataUnit;

public class DbHtDatagramProvider implements DatagramProvider{

	private DbHtControl dbHtControl;
	
	private DatagramScaleInformation scaleInfo;
	
	public DbHtDatagramProvider(DbHtControl dbHtControl) {
		this.dbHtControl = dbHtControl;
		scaleInfo = new DatagramScaleInformation(Double.NaN, Double.NaN, "dB", false, DatagramScaleInformation.PLOT_2D);
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		DbHtDataUnit dbhtDataunit = (DbHtDataUnit) dataUnit;
		dataGramLine[0] = addDBValues(dataGramLine[0], dbhtDataunit.getRms());
		dataGramLine[1] = addDBValues(dataGramLine[1], dbhtDataunit.getZeroPeak());
		dataGramLine[2] = addDBValues(dataGramLine[2], dbhtDataunit.getPeakPeak());
		return 1;
	}
	
	private float addDBValues(float val1, double val2) {
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
