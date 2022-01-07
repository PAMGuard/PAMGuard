package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;
import clickTrainDetector.layout.mht.SimpleMHTVarPane;

/**
 * Chi2 value for changing bearing. 
 * 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class BearingChi2 extends SimpleChi2Var {
	
	public BearingChi2() {
		super(); 
		super.setSimpleChiVarParams(defaultSettings());
//		this.getSimpleChiVarParams().setResultConverter(new Rad2Deg());
	}
	
	/**
	 * Create default settings. On first instance of module these are called an saved. 
	 * @return default settings object
	 */
	private SimpleChi2VarParams defaultSettings() {
		SimpleChi2VarParams simpleChiVarParams = new SimpleChi2VarParams(getName(), getUnits()); 
		//simpleChiVarParams.errLimits= new double[]{0, Math.toRadians(90)}; 
		simpleChiVarParams.error=Math.toRadians(100); 
		simpleChiVarParams.minError=Math.toRadians(2); 
		simpleChiVarParams.errorScaleValue = SimpleChi2VarParams.SCALE_FACTOR_BEARING;
		return simpleChiVarParams; 
	}
	
	public String getName() {
		return "Bearing_old \u00B0"; // the bearing is input in degrees. 
	}

	@Override
	public double getDiffValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//this is in RADIANS
		//System.out.println("Angles: " + pamDataUnit0.getLocalisation().getAngles()[0]); 
		if (pamDataUnit0.getLocalisation()!=null) {
			return pamDataUnit0.getLocalisation().getAngles()[0]-pamDataUnit1.getLocalisation().getAngles()[0];
		}
		else return 0; 
	}

	@Override
	public double getErrValue(PamDataUnit pamDataUnit0, PamDataUnit pamDataUnit1) {
		//just a simple static error coefficient. 
		return this.getSimpleChiVarParams().error;
	}
	
	@Override
	public MHTVarSettingsPane<SimpleChi2VarParams> getSettingsPane() {
		if (this.settingsPane==null) this.settingsPane= new SimpleMHTVarPane(getSimpleChiVarParams(), new Rad2Deg()); 
		settingsPane.setParams(getSimpleChiVarParams());
		return settingsPane;
	}
	
	@Override
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock) {
		//check whether ebaring info is available from the datablock. 
		if (parentDataBlock.getLocalisationContents()!=null) {
			return parentDataBlock.getLocalisationContents().hasLocContent(LocContents.HAS_BEARING); 
		}
		return false; 
	}

	@Override
	public String getUnits() {
		return "\u00b0";
	}
}
