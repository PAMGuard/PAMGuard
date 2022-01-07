package clickDetector;

import pamMaths.PamVector;
import Array.ArrayManager;
import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;

public class ClickLocalisation extends AbstractLocalisation {

	private ClickDetection clickDataUnit;
		
	private double firstDelay;
	
	private double[][] anglesAndErrors;
	
	private double delayCorrection;
	
	private boolean bearingAmbiguity = true;

	/**
	 * Index for channels to calculate time delays between: held here so does not have to 
	 * be constantly recalculated. 
	 */
	private int[][] index;
	
	public ClickLocalisation(ClickDetection parentDetection, int locContents, int referenceHydrophones, 
			int subArrayType, PamVector[] arrayOrientation) {
		super(parentDetection, locContents, referenceHydrophones, subArrayType, arrayOrientation);
		clickDataUnit = parentDetection;
		
	}

	@Override
	public boolean bearingAmbiguity() {
		return bearingAmbiguity;
	}

	@Override
	public double getBearing(int iSide) {
		//THODE flip sign of delay
		double ang = (firstDelay + delayCorrection) / clickDataUnit.getClickDetector().getSampleRate()
				/ ArrayManager.getArrayManager().getCurrentArray().getSeparationInSeconds(getReferenceHydrophones(),clickDataUnit.getTimeMilliseconds());
		ang = Math.min(1., Math.max(ang, -1.));
		double angle = Math.acos(ang);
		//System.out.println("");
		//System.out.println("cos angle: "+ ang + " Angle: "+ angle*180/Math.PI);
		return angle;
	}
	

	public double getFirstDelay() {
		return firstDelay;
	}

	public void setFirstDelay(double delays) {
		this.firstDelay = delays;
	}

//	public double getDelayCorrection() {
//		return delayCorrection;
//	}
//
//	public void setDelayCorrection(double delayCorrection) {
//		this.delayCorrection = delayCorrection;
//	}

	/**
	 * @param anglesAndErrors the anglesAndErrors to set
	 */
	public void setAnglesAndErrors(double[][] anglesAndErrors) {
		this.anglesAndErrors = anglesAndErrors;
		if (anglesAndErrors != null) {
			if (anglesAndErrors.length > 0 && anglesAndErrors[0].length > 1) {
				bearingAmbiguity = false;
			}
		}
	}

	@Override
	public double[] getAngles() {
		if (anglesAndErrors == null) {
			return null;
		}
		return anglesAndErrors[0]; // return first row - angles, not errors. 
	}
	
	@Override
	public double[] getAngleErrors() {
		if (anglesAndErrors == null || anglesAndErrors.length < 2) {
			return null;
		}
		return anglesAndErrors[1]; // return first row - angles, not errors. 
	}
	
	@Override
	public double[] getTimeDelays() {
		//System.out.println("Get time delays: ");
		double delay;
		double[] timeDelays=new double[clickDataUnit.getDelaysInSamples().length];
		
		clickDataUnit.setComplexSpectrum();

		for(int i=0; i<timeDelays.length; i++){
			delay=(clickDataUnit.getDelaysInSamples()[i])/clickDataUnit.getClickDetector().getSampleRate();
			timeDelays[i]=delay;
		}

		return timeDelays;	
	}
	
	/**
	 * Set the time delays
	 * @param timeDelays. List of time delays for this click detection obeying indexM1 and indexM2 rules. If the no. of delays doesn not satisfty group size then the delays are not set. 
	 */
	public void setTimeDelays(double[] timeDelays){
		if (timeDelays.length!=clickDataUnit.getDelaysInSamples().length) return;
		for (int i=0; i<timeDelays.length; i++){
			clickDataUnit.setDelayInSamples(i, timeDelays[i]*clickDataUnit.getClickDetector().getSampleRate());
		}
	}
	
	/**
	 * Set a time delay. 
	 * @param idelay - which delay to set 
	 * @param timeDelays - new time delays values
	 */
	public void setTimeDelay(int idelay, double timeDelay){
		if (idelay>=clickDataUnit.getDelaysInSamples().length) return; 
		clickDataUnit.setDelayInSamples(idelay, timeDelay*clickDataUnit.getClickDetector().getSampleRate());
	}
	
	@Override
	public double[] getTimeDelayErrors() {
		
		double[] timeDelayErrors=new double[clickDataUnit.getDelaysInSamples().length];
		
//		if (indexM1==null) indexM1=super.indexM1(clickDataUnit.getNChan());
//		if (indexM2==null) indexM2=super.indexM2(clickDataUnit.getNChan());
		
		index=getTimeDelayChIndex(clickDataUnit.getNChan());
		
		int h0;
		int h1;
		int map=clickDataUnit.getChannelBitmap();
		for (int n=0; n<timeDelayErrors.length;n++){
			//must be careful with groups here- cannot translate indexM1 and indexM2 directly to hydrophone positions! 
			h0=PamUtils.getChannelArray(map)[index[n][0]];
			h1=PamUtils.getChannelArray(map)[index[n][1]];
			timeDelayErrors[n]=ArrayManager.getArrayManager().getCurrentArray().getTimeDelayError(h0,h1,clickDataUnit.getTimeMilliseconds());
//			timeDelayErrors[n]=(AbstractDetectionMatch.getTimeDelayError(h0,h1,clickDataUnit.getTimeMilliseconds(),	ArrayManager.getArrayManager().getCurrentArray()));
		}
		return timeDelayErrors;
	}
	
	public void setAngles(double[] angles) {
		int nAngles = angles.length;
		if (anglesAndErrors == null) {
			anglesAndErrors = new double[2][nAngles];
		}
		anglesAndErrors[0] = angles;
	}

	public void setAngleErrors(double[] angleErrors) {
		int nAngles = angleErrors.length;
		if (anglesAndErrors == null) {
			anglesAndErrors = new double[2][nAngles];
		}
		anglesAndErrors[1] = angleErrors;
	}

	/**
	 * @return the anglesAndErrors
	 */
	public double[][] getAnglesAndErrors() {
		return anglesAndErrors;
	}

}
