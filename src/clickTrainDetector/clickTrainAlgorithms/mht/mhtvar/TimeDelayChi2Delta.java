package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.BitSet;

import PamDetection.LocContents;
import PamUtils.PamArrayUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;
import clickTrainDetector.layout.mht.SimpleMHTVarPane;

/**
 * Chi^2 value for time delays. This might be a little more robust than bearing, especially for 
 * hydrophone arrays with more than two hydrophones. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TimeDelayChi2Delta extends SimpleChi2Var {
	
	/**
	 * The difference between the previous set of time delays between two data units. 
	 */
	private double[] lastDelta = null;
	
	public TimeDelayChi2Delta() {
		super(); 
		super.setSimpleChiVarParams(defaultSettings());
//		this.getSimpleChiVarParams().setResultConverter(new Rad2Deg());
	}

	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return
	 */
	private SimpleChi2VarParams defaultSettings() {
		SimpleChi2VarParams simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits=new double[] {Double.MIN_VALUE, 100}; 
		simpleChiVarParams.error=1/1E6;
		simpleChiVarParams.minError=1/1E9;
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_TIMEDELAYS; 
		return simpleChiVarParams; 
	}

	
	@Override
	public String getName() {
		return "Time Delays";
	}

	@Override
	public String getUnits() {
		return "ms";
	}


	@Override
	public double updateChi2(PamDataUnit newdataUnit, BitSet bitSet, int bitcount, int kcount, IDIManager idiManager) {
		//have to do a slightly different calculation to update chi2 vales. 
		
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
		double[] newDelta = getDiffValue2(getLastDataUnit(), newdataUnit); 
		
		//do we have a last chi2 value
		if (lastDelta ==null) {
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
	
	
	double[] newDelta; 
	/**
	 * 
	 * @param lastDataUnit
	 * @param newdataUnit
	 * @return
	 */
	protected double[] getDiffValue2(PamDataUnit lastDataUnit, PamDataUnit newdataUnit) {
		//this will crash if the time delays are not the same size but this should be something caught earlier
		//want it to crash if this happens. 
		newDelta = new double[newdataUnit.getLocalisation().getTimeDelays().length]; 
		for (int i=0; i<newDelta.length; i++) {
			newDelta[i] = lastDataUnit.getLocalisation().getTimeDelays()[i]-newdataUnit.getLocalisation().getTimeDelays()[i]; 
		}	
		return newDelta;
	}

	/**
	 * Calculate the chi2 for two different delta values. 
	 * @param lastDelta - the first delta value
	 * @param newDelta - the second delta value
	 * @param timeDiff - the time difference for error calculation
	 * @return the chi^2 value. 
	 */
	public double calcDeltaChi2(double[] lastDelta, double[] newDelta, double timeDiff) {
		//System.out.println("ICI: 1 " + ici1+ " ICI 2: " + ici2);##
		double chi2 = 0.; 
		double[] chi2d = new double[lastDelta.length]; 
		for (int i=0; i<lastDelta.length; i++) {
			chi2d[i] = Math.pow((lastDelta[i] - newDelta[i]),2); 
			chi2 += chi2d[i]; 
			//Debug.out.println("Last TD: " + lastDelta[i] + " New Delta: " + newDelta[i]  + " Err: "+ timeDiff*getSimpleChiVarParams().error + " min Err: " + getMinCutValue()); 
		}
		

		//remove the worst time deltay?
		chi2=chi2-PamArrayUtils.max(chi2d);
		
		//use the median time delay chi^2. This means that if there are multiple time delays and one correlation is out then it does not mess 
		//up the chi^2 value...
//		chi2= PamArrayUtils.median(chi2d);
				
		chi2 = chi2/Math.pow( Math.max(timeDiff*getSimpleChiVarParams().error, getMinCutValue()), 2);
			
		return chi2; 
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//Not used here. 
		return 0;
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return this.getSimpleChiVarParams().error;
	}

	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new SimpleMHTVarPane(getSimpleChiVarParams(), new Seconds2Millis()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}
	
	@Override
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock) {
		//check whether bearing info is available from the datablock. 
		if (parentDataBlock!=null && parentDataBlock.getLocalisationContents()!=null) {
			return parentDataBlock.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING); 
		}
		return false; 
	}


}
