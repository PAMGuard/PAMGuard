package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.ArrayList;
import java.util.BitSet;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;
import clickTrainDetector.layout.mht.SimpleMHTVarPane;

/**
 * Calculate the ICI value of amplitude. 
 * @author Jamie Macaulay 
 */
@SuppressWarnings("rawtypes")
public abstract class SimpleChi2Var implements MHTChi2Var<PamDataUnit> {
	
	/**
	 * Parameters for the chi2 variable. 
	 */
	private SimpleChi2VarParams simpleChiVarParams;
	
	/**
	 * Pane with controls to change chi^2 settings
	 */
	protected SimpleMHTVarPane settingsPane;
	
	/**
	 * The current chi2 value. This is the chi2 value but NOT divided by the number of 
	 * units to make calcualtions easier. 
	 */
	private double chi2 = 0; 
	
	/**
	 * The last data unit that was in the track (not necessarily the last data unit to be called in updateChi2 function)
	 */
	private PamDataUnit lastDataUnit;

//	/**
//	 * Converts between units and human readable units. 
//	 */
//	private ResultConverter resultConverter = new ResultConverter();  

	
	public SimpleChi2Var(){
		simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
	}
	
	@Override
	public double calcChi2(ArrayList<PamDataUnit> mhtDataUnits, IDIManager iciManager) {
		double chi2Val=0; 
		//printTimeSeries(timeSeries); //DELETE
		
		for (int i=1; i<mhtDataUnits.size(); i++) {
			//System.out.println(mhtDataUnits.get(i-1));
			chi2Val+=calcChi2(mhtDataUnits.get(i-1), mhtDataUnits.get(i), iciManager); 
			//System.out.println("Amp: " + String.format("%.3f", chi2Val));
		}
		chi2Val = chi2Val/(mhtDataUnits.size()-1); 
		
		//if (StandardMHTChi2.verbosity>0) System.out.println(getName() + "  "+  String.format("%.3f", ampChi2)); 

		return chi2Val;
	}
	

	@Override
	public double updateChi2(PamDataUnit newDataUnit, BitSet bitSet, int bitcount, int kcount, IDIManager iciManager) {
						
		if (Double.isNaN(chi2)) setChi2(0); 

		//do nothing if the data unit is not included in the track <-This is very important
		if (!bitSet.get(kcount-1)) {
			return chi2/bitcount; 
		}

		//first, do we have chi2 values 
		if (getLastDataUnit()==null || kcount<=1 || bitcount<2) {
			setLastDataUnit(newDataUnit); 
			chi2=0; 
			return chi2; 
		}
		
		//check whether the the current detection is in the track
		chi2+=calcChi2(this.getLastDataUnit(), newDataUnit, iciManager); 
//		System.out.println("Amp chi2: " + String.format("%.3f", chi2) + 
//				" ici: " +  iciManager.calcTime(this.getLastDataUnit(), newDataUnit) +
//				" diff value: " + getDiffValue(this.getLastDataUnit(), newDataUnit)
//				+" last data unit: " + lastDataUnit.hashCode() + " new data unit: " 
//				+ newDataUnit.hashCode() + " this " + this.hashCode());
		setLastDataUnit(newDataUnit); 

		//return the chi2 value divided by the number of data units. 
		return chi2/bitcount;
	}
	
	/**
	 * Calculate the chi2 value between two subsequent data units in a track. 
	 * @param pamDataUnit0 - the first data unit (in time). 
	 * @param pamDataUnit1 - the second data unit (in time).
	 * @return the chi2 value between the two data units. 
	 */
	protected double calcChi2(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1, IDIManager iciManager) {
		return (Math.pow(getDiffValue(pamDataUnit0, pamDataUnit1),2)/
				Math.pow(Math.max(getMinCutValue(), 
						iciManager.calcTime(pamDataUnit0, pamDataUnit1)*getErrValue(pamDataUnit0, pamDataUnit1)), 2)); 
	}
	
	
	/**
	 * Get the difference in value between two sequential data units. This is top of the chi2 equation. 
	 * @param pamDataUnit0 - the first data unit.
	 * @param pamDataUnit1 - the second data unit.
	 * @return - the difference in selected variable between the two data units. 
	 */
	public abstract double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1); 
	
	/**
	 * Get the error value between two sequential data units. This is denominator of the chi2 equation. 
	 * @param pamDataUnit0 - the first data unit.
	 * @param pamDataUnit1 - the second data unit.
	 * @return - the error between the difference 
	 */
	public abstract double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1); 
	
	
	/**
	 * The minimum value of chi^2. If chi^2 is below this vlaue then it is return as this value. 
	 * @return the minimum value chi^" can be. 
	 */
	public double getMinCutValue() {
		return simpleChiVarParams.minError; 
	}
	
	
	/**
	 * Get the error value. This a percentage of whatever variable is 
	 * @return the error
	 */
	public double getError() {
		return simpleChiVarParams.error;
	}

	/**
	 * Set the error value
	 * @param error the error to set
	 */
	public void setError(double error) {
		this.simpleChiVarParams.error = error;
	}
	
	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new SimpleMHTVarPane(simpleChiVarParams); 
		settingsPane.setParams(this.simpleChiVarParams);
		return settingsPane;
	}
	
	
	/**
	 * Set the chi2 params for the chi2variable. 
	 * @param simpleChiVarParams
	 */
	public void setSimpleChiVarParams(SimpleChi2VarParams simpleChiVarParams) {
		this.simpleChiVarParams = simpleChiVarParams;
	}
	
	
	/**
	 * Get the chi2 variable params. 
	 * @return the chi2variable params. 
	 */
	public SimpleChi2VarParams getSimpleChiVarParams() {
		//if (settingsPane!=null) return settingsPane.getParams(simpleChiVarParams);
		return simpleChiVarParams;
	}
	
	@Override
	public Object getSettingsObject() {
		//if (settingsPane!=null) simpleChiVarParams = settingsPane.getParams(simpleChiVarParams); //update the settings. 
		 return getSimpleChiVarParams();
	}
	
	public void setSettingsObject(Object object) {
		this.simpleChiVarParams=(SimpleChi2VarParams) object; 
	}

	

//	/**
//	 * Convert result from 
//	 * @return
//	 */
//	/**
//	 * @return the resultConverter
//	 */
//	public ResultConverter getResultConverter() {
//		return resultConverter;
//	}
//
//
//	/**
//	 * @param resultConverter the resultConverter to set
//	 */
//	public void setResultConverter(ResultConverter resultConverter) {
//		this.resultConverter = resultConverter;
//	}
	
	/**
	 * Get the current chi2 value. Note: this is the raw stored chi2 value without 
	 * being divided by total number of data units. Only use for sub classes. 
	 * @return the current chi2 value
	 */
	@Override
	public double getChi2() {
		return this.chi2; 
	}
	
	/**
	 * Set the chi2 value.  Note, only used in sub classes. 
	 * @param chi2
	 */
	protected void setChi2(double chi2) {
		this.chi2=chi2; 
	}

	/**
	 * Get the last data unit which was in the track
	 * @return the last data unit in the track 
	 */
	protected PamDataUnit getLastDataUnit() {
		return lastDataUnit;
	}

	/**
	 * Set the last data unit in the track. Note, only used in sub classes. 
	 * @param lastDataUnit
	 */
	protected void setLastDataUnit(PamDataUnit lastDataUnit) {
		this.lastDataUnit = lastDataUnit;
	}
	
	/**
	 * Reset everything. 
	 */
	public void clear() {
		this.chi2=0; 
		lastDataUnit=null; 
	}

//	@Override
//	public String toString() {
//		return getName() + String.format(" Error: %.3f Min Error: %.5f", 
//				this.getResultConverter().convert2Control(simpleChiVarParams.error), 
//				this.getResultConverter().convert2Control(simpleChiVarParams.minError)); 
//	}
	
	
	@Override
	public SimpleChi2Var clone() {
		SimpleChi2Var theClone = null;
	    try {
	        theClone = (SimpleChi2Var) super.clone();
	    } catch (CloneNotSupportedException e) {
	        e.printStackTrace();
	    }
	    return theClone;
	}
	
	@Override
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock) {
		return true; 
	}
	

}
