package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import cpod.CPODClick;

/**
 * Peak frequency variable for click train detector. Correlation is very similar to this but 
 * peak frequency is more simple and perhaps a more stable measure. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class PeakFrequencyChi2 extends SimpleChi2Var {
	
	public PeakFrequencyChi2() {
		super();
		super.setSimpleChiVarParams(defaultSettings());
	}
	
	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return
	 */
	private SimpleChi2VarParams defaultSettings() {
		SimpleChi2VarParams simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits=new double[] {Double.MIN_VALUE, 100}; 
		simpleChiVarParams.error=30;
		simpleChiVarParams.minError=1;
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_PFREQ; 
		return simpleChiVarParams; 
	}
	
	
	public String getName() {
		return "Peak Frequency";
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//System.out.println("DB: " + pamDataUnit0.getAmplitudeDB());
		if (pamDataUnit0 instanceof RawDataHolder) {
			//TODO - this should be replaced by RawDataHolder once it has a transforms object which 
			//can be returned. 
			return getPeakFrequency(pamDataUnit0) - getPeakFrequency(pamDataUnit1); 
		}
		else if (pamDataUnit0 instanceof CPODClick) {
			return 1000.*(((CPODClick)  pamDataUnit0).getkHz() - ((CPODClick) pamDataUnit1).getkHz()); 

		}
		else {
			System.err.println("PeakFrequencyChi2: the peak frequency cannot be measured from the data unit: " + pamDataUnit0);
			return 0; 
		}
	}
	
	/**
	 * Get the peak frequency for a click detections. 
	 * @param dataUnit - the dataunit to calculate the peak frequency from. 
	 * @return the peak frequency in Hz. 
	 */
	private double getPeakFrequency(PamDataUnit dataUnit) {
		
		RawDataHolder rawDataHolder = (RawDataHolder) dataUnit; 
		
		double[]  powerSpectrum = rawDataHolder.getDataTransforms().getTotalPowerSpectrum(rawDataHolder.getDataTransforms().getShortestFFTLength());

		int maxIndex = PamUtils.getMaxIndex(powerSpectrum);
		
		return (maxIndex/(double) powerSpectrum.length)*dataUnit.getParentDataBlock().getSampleRate()/2;
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return super.getError();
	}


	@Override
	public String getUnits() {
		return "Hz";
	}
	
	
	@Override
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock) {
		if (parentDataBlock==null) return false; 
		
		if (RawDataHolder.class.isAssignableFrom(parentDataBlock.getUnitClass())) {
			return true; 
		}
		
		if (CPODClick.class.isAssignableFrom(parentDataBlock.getUnitClass())) {
			return true; 
		}
		
		return false;
	}

	@Override
	public void setSettingsObject(Object object) {
		super.setSettingsObject(object);
		this.getSimpleChiVarParams().errorScaleValue = defaultSettings().errorScaleValue; //meh
	}


}