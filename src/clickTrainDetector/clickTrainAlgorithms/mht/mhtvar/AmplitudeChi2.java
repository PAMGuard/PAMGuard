package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;


import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.layout.mht.AmplitudeMHTVarPane;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;

/**
 * Chi^2 value for dB amplitude of tracks. Measures the chnage in track delta between 
 * subsequent detections. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class AmplitudeChi2 extends SimpleChi2VarDelta {
	
	private double lastDiff;
	
	private AmplitudeChi2Params amplitudeParams;


	public AmplitudeChi2() {
		super();
		super.setSimpleChiVarParams(amplitudeParams = (AmplitudeChi2Params) defaultSettings());
	}
	
	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return
	 */
	private SimpleChi2VarParams defaultSettings() {
		AmplitudeChi2Params simpleChiVarParams = new AmplitudeChi2Params(getName(), getUnits()); 
		//simpleChiVarParams.errLimits=new double[] {Double.MIN_VALUE, 100}; 
		simpleChiVarParams.error=30;
		simpleChiVarParams.minError=1;
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_AMPLITUDE; 
		return simpleChiVarParams; 
	}
	
	
	public String getName() {
		return "Amplitude";
	}


	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//System.out.println("DB: " + pamDataUnit0.getAmplitudeDB());
		//made this abs so it can deal with increasing then decreasing click trains. i.e.
		//the click trian is not penalised if it gradually increasing then starts to gradually decrease
		//in amplitude. 
		this.lastDiff = Math.abs(pamDataUnit0.getAmplitudeDB()-pamDataUnit1.getAmplitudeDB());
		return lastDiff;
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return super.getError();
	}
	
	@Override
	public double calcDeltaChi2(double lastDelta, double newDelta, double timeDiff) {

		double chi2 = super.calcDeltaChi2(lastDelta, newDelta, timeDiff); 

		/**
		 * There was a problem here with using the delta instead of the absolute difference between amplitudes. 
		 * When using the delta there could be a slow change of amplitude gradient which could lead to giant 
		 * changes in absolute amplitude. By ensuring this is the absolute 
		 * value between bearings (lastdiff) then the bearingJump threshold works as it should.
		 */
		
		//System.out.println("Amplitude: " + amplitudeParams.ampJumpEnable + "  " + lastDiff  + "  " + amplitudeParams.maxAmpJump);

		if (lastDiff>amplitudeParams.maxAmpJump && amplitudeParams.ampJumpEnable ) {
			chi2=chi2+StandardMHTChi2Params.JUNK_TRACK_PENALTY; 
		}	
	

		return chi2; 
	}


	@Override
	public String getUnits() {
		return "dB";
	}
	
	@Override
	public void setSimpleChiVarParams(SimpleChi2VarParams params) {
		if (params==null) amplitudeParams = new AmplitudeChi2Params(getName(), getUnits()); //backwards compatibility
		else this.amplitudeParams = (AmplitudeChi2Params)  params; ;

		super.setSimpleChiVarParams(params);
		//save a reference to params so we don;t have to keep casting. 
	}
	
	@Override
	public void setSettingsObject(Object object) {
		this.setSimpleChiVarParams((AmplitudeChi2Params) object); 
	}

	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new AmplitudeMHTVarPane(getSimpleChiVarParams(), new ResultConverter()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}
	
}


