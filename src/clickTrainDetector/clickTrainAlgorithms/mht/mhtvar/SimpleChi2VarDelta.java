package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.BitSet;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;

/**
 * Chi2 variable which compares the <i>difference</i> between the two three values. i.e 
 * difference between val2-val1 and val3-val2;  
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public abstract class SimpleChi2VarDelta extends SimpleChi2Var {

	/**
	 * The last data unit difference. 
	 */
	private double lastDelta = -1; 

	@Override
	public double updateChi2(PamDataUnit newdataUnit, BitSet bitSet, int bitcount, int kcount, IDIManager idiManager) {
		//have to do a slightly different calculation to update chi2 vales. 
		
		if (Double.isNaN(getChi2())) setChi2(0); 
		
		//do nothing if the data unit is not included in the track
		if (!bitSet.get(kcount-1)) {
			return getChi2()/bitcount; 
		}

		//first, do we have chi2 values 
		if (getLastDataUnit()==null || kcount<=1 || bitcount<2) {
			setLastDataUnit(newdataUnit); 
			setChi2(0.0);  
			return getChi2(); 
		}
		
		//have last data unit so can calculate chi2. 
		double newDelta = getDiffValue(getLastDataUnit(), newdataUnit); 
		
		//do we have a last chi2 value
		if (lastDelta==-1) {
			setLastDataUnit(newdataUnit);
			lastDelta=newDelta; 
			return getChi2()/bitcount; 
		}
		
		//check whether the the current detection is in the track
		setChi2(getChi2()+ calcDeltaChi2(lastDelta, newDelta, idiManager.calcTime(getLastDataUnit(), newdataUnit))); 
		
		
//		System.out.println("ICI chi2: " + String.format("%.3f ICI 1 %.4f ICI 2  %.4f val2 %.9f val1 %.9f", 
//				calcICIChi2(lastIDI, newIDI), lastIDI , newIDI, Math.pow((newIDI - lastIDI),2), 
//				Math.pow(Math.max(lastIDI*getSimpleChiVarParams().error, getMinCutValue()), 2))); 
		setLastDataUnit(newdataUnit); 
		lastDelta=newDelta; 
		
		//return the chi2 value divided by the number of data units. 
		return getChi2()/bitcount;
	}
	
	/**
	 * Calculate the chi2 for two different delta values. 
	 * @param lastDelta - the first delta value
	 * @param newDelta - the second delta value
	 * @param timeDiff - the time difference for error calculation
	 * @return the chi^2 value. 
	 */
	public double calcDeltaChi2(double lastDelta, double newDelta, double timeDiff) {
		//System.out.println("ICI: 1 " + ici1+ " ICI 2: " + ici2);#
		double chi2 = (Math.pow((lastDelta - newDelta),2)/
				Math.pow( Math.max(timeDiff*getSimpleChiVarParams().error, getMinCutValue()), 2));
		return chi2; 
	}
	
	
	

}
