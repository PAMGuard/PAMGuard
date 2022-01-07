package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.layout.mht.BearingMHTVarPane;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;

/**
 * Measures the difference in change between three bearing measurements. So
 * measures the <i>change in change</i> of bearing rather than just the change
 * of bearing.
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class BearingChi2Delta extends SimpleChi2VarDelta {	

	/**
	 * A local reference to params to prevent casting all the time. 
	 */
	private BearingChi2VarParams bearingParams;


	/**
	 * The last diff value. 
	 */
	private double lastDiff;


	public BearingChi2Delta() {
		super(); 
		super.setSimpleChiVarParams(defaultSettings());
		//this.getSimpleChiVarParams().setResultConverter(new Rad2Deg());
	}

	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return default settings object. 
	 */
	private BearingChi2VarParams defaultSettings() {
		BearingChi2VarParams simpleChiVarParams = new BearingChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits= new double[]{0, Math.toRadians(90)}; 
		simpleChiVarParams.error=Math.toRadians(4); 
		simpleChiVarParams.minError=Math.toRadians(2); 
		simpleChiVarParams.errorScaleValue=SimpleChi2VarParams.SCALE_FACTOR_BEARING;
		return simpleChiVarParams; 
	}

	public String getName() {
		return "Bearing Delta"; // the bearing is input in degrees. 
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//this is in RADIANS
		//System.out.println("Angles: " + pamDataUnit0.getLocalisation().getAngles()[0]); 
		if (pamDataUnit0.getLocalisation()!=null) {
			lastDiff = getDifference(pamDataUnit0.getLocalisation().getAngles()[0], pamDataUnit1.getLocalisation().getAngles()[0]); 
//			lastDiff = pamDataUnit0.getLocalisation().getAngles()[0]-pamDataUnit1.getLocalisation().getAngles()[0]; 
		}
		else lastDiff = 0; 

		return lastDiff; 
	}


	/**
	 * Get the minimum angle between two angles. 
	 * @param a1 - angle one in RADIANS
	 * @param a2 - angle two in RADIANS
	 * @return the difference. 
	 */
	private double getDifference(double a1, double a2) {
		return Math.min((a1-a2)<0?a1-a2+2*Math.PI:a1-a2, (a2-a1)<0?a2-a1+2*Math.PI:a2-a1); 
	}



	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return this.getSimpleChiVarParams().error;
	}

	@Override
	public String getUnits() {
		return "\u00b0";
	}

	@Override
	public void setSettingsObject(Object object) {
		this.setSimpleChiVarParams((BearingChi2VarParams) object); 
	}

	@Override
	public void setSimpleChiVarParams(SimpleChi2VarParams params) {
		super.setSimpleChiVarParams(params);
		//save a reference to params so we don;t have to keep casting. 
		this.bearingParams = (BearingChi2VarParams)  params; 
	}


	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new BearingMHTVarPane(getSimpleChiVarParams(), new Rad2Deg()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}


	@Override
	public double calcDeltaChi2(double lastDelta, double newDelta, double timeDiff) {

		double chi2 = super.calcDeltaChi2(lastDelta, newDelta, timeDiff); 

		/**
		 * There was a problem here with using the delta instead of the absolute difference between bearings. 
		 * When using the delta there could be a slow change of bearing gradient which could lead to giant 
		 * changes in absolute bearings and so a sort of tail on click trains. By ensuring this is the absolute 
		 * value between bearings (lastdiff) then the bearingJump threshold works as it should.
		 */
		if (bearingParams.bearingJumpEnable) {
			double delta;
			switch (bearingParams.bearingJumpDrctn) {
			case BOTH:
				delta= Math.abs(lastDiff); 
				break;
			case NEGATIVE:
				delta = -(lastDiff); 
				break;
			case POSITIVE:
				delta = lastDiff; 
				break;
			default:
				delta = lastDiff; 
				break;
			}

			if (delta>bearingParams.maxBearingJump) {
				//System.out.println("Hello!!!! Reverse Bearing");
				chi2=chi2*StandardMHTChi2Params.JUNK_TRACK_PENALTY; 
			}	
		}

		return chi2; 
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
