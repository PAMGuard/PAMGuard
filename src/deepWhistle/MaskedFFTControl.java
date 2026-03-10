package deepWhistle;

import PamController.PamControlledUnit;
import PamController.PamSettings;

/**
 * Control class for DeepWhistle module (initial masking-only implementation).
 */
public abstract class MaskedFFTControl extends PamControlledUnit implements PamSettings {


    public MaskedFFTControl(String unitType, String unitName) {
		super(unitType, unitName);
		// TODO Auto-generated constructor stub
	}


	public abstract MaskedFFTParamters getParameters() ;

	public abstract void setParameters(MaskedFFTParamters parameters);
	
  
}