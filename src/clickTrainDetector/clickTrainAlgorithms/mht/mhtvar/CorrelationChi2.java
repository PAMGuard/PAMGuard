package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clickTrainDetector.layout.mht.CorrelationMHTPane;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;

/**
 * Calculates the correlation value between subsequent clicks. Unlike amplitude
 * and bearing used in the click train detector correlation is an absolute
 * measure i.e. the absolute normalized difference between the current and
 * second last detection in a train, rather than the change in difference
 * between the last three clicks,.
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class CorrelationChi2 extends SimpleChi2Var {
	
	/**
	 * Reference to the correlation params
	 */
	private CorrelationChi2Params correlationParams;
	
	/**
	 * 
	 */
	public CorrelationChi2() {
		super(); 
		super.setSimpleChiVarParams(correlationParams = defaultSettings());
	}
	
	private CorrelationChi2Params defaultSettings() {
		CorrelationChi2Params simpleChiVarParams = new CorrelationChi2Params(getName()); 
		//simpleChiVarParams.errLimits= new double[]{0, 2}; 
		simpleChiVarParams.error=1; 
		simpleChiVarParams.minError=0.01; 
		simpleChiVarParams.errorScaleValue =  SimpleChi2VarParams.SCALE_FACTOR_CORRELATION; 
		return simpleChiVarParams; 
	}
	
	@Override
	public void setSettingsObject(Object object) {
		this.setSimpleChiVarParams((CorrelationChi2Params) object); 
	}
	
	@Override
	public void setSimpleChiVarParams(SimpleChi2VarParams params) {
		super.setSimpleChiVarParams(params);
		//save a reference to parameters so we don;t have to keep casting. 
		this.correlationParams = (CorrelationChi2Params)  params; 
	}


	public String getName() {
		return "Correlation";
	}

	/**
	 * Calculate the chi2 value between two subsequent data units in a track. 
	 * @param pamDataUnit0 - the first data unit (in time). 
	 * @param pamDataUnit1 - the second data unit (in time).
	 * @return the chi2 value between the two data units. 
	 */
	@Override
	protected double calcChi2(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1, IDIManager iciManager) {
		double corrVal = iciManager.getCorrelationManager().getCorrelationValue(pamDataUnit0, pamDataUnit1,
				correlationParams.useFilter ? correlationParams.fftFilterParams : null).correlationValue;
		//corr value is high if similar and low if not similar. Thus use 1/corrValue. Use a log scale because linear
		//never makes much sense and it seems to work 
		
		return (Math.pow(Math.log(1/corrVal),2)/
				Math.pow(Math.max(getMinCutValue(), 
						iciManager.calcTime(pamDataUnit0, pamDataUnit1)*getErrValue(pamDataUnit0, pamDataUnit1)), 2)); 
	}
	
	
	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new CorrelationMHTPane(getSimpleChiVarParams()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}


	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		// Not used
		return 0;
	}


	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		return super.getSimpleChiVarParams().error;
	}
	
	@Override
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock) {
		//check correlation info is available from the datablock 
		if (parentDataBlock!=null && RawDataHolder.class.isAssignableFrom(parentDataBlock.getUnitClass())) {
			return true; 
		}
		return false; 
	}


	@Override
	public String getUnits() {
		return "";
	}




}
