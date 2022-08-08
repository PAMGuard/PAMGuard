package clickTrainDetector;

import java.util.Arrays;
import java.util.List;

import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;

/**
 * Holds some basic IDI info on the click train. 
 * @author Jamie Macualay 
 *
 */
public class IDIInfo {

	/**
	 * The median IDI in seconds
	 */
	public  double medianIDI;

	/**
	 * The mean IDI in seconds 
	 */
	public double meanIDI;

	/**
	 * The standard deviation in IDI in seconds 
	 */
	public double stdIDI;

	/**
	 * The last number of data units recorded.
	 */
	public int lastNumber = -1;
	
	public IDIInfo() {};
	
	public IDIInfo(double medianIDI, double meanIDI, double stdIDI, int nUnits) {
		this.medianIDI=medianIDI;
		this.meanIDI=meanIDI;
		this.stdIDI = stdIDI;
		this.lastNumber=nUnits;
	};

	public void calcTimeSeriesData(List<PamDataUnit<?,?>> dataUnits) {
		
		lastNumber = dataUnits.size();

		if (dataUnits.size()<3) {
			Debug.out.println("CTDataUnit: Cannot calculate IDIInfo for less than three data units"); 
			return; 
		}

		double[] idiSeriesS = new double[dataUnits.size()-1]; 
		
		//must ensure we sort the data units before doing anything else. 
		
		long[] nanosecondTime = new long[dataUnits.size()];
		for (int i=0; i<dataUnits.size(); i++) {
			nanosecondTime[i] = dataUnits.get(i).getTimeNanoseconds(); 
		}
		
		Arrays.sort(nanosecondTime);

		for (int i=1; i<dataUnits.size(); i++) {
			idiSeriesS[i-1]=(nanosecondTime[i] - nanosecondTime[i-1])/1.e9; 
		}

		//calculate the info - this will all be in seconds now. 
		this.meanIDI = PamArrayUtils.mean(idiSeriesS);
		this.medianIDI = PamArrayUtils.median(idiSeriesS);
		this.stdIDI =  PamArrayUtils.std(idiSeriesS);

	}

}