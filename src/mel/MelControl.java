package mel;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;

public class MelControl extends PamControlledUnit {
	
	public static String unitType = "Mel Spectrogram";
	
	public MelControl(String unitName) {
		super(unitType, unitName);
		// TODO Auto-generated constructor stub
	}

	public MelControl(PamConfiguration pamConfiguration, String unitType, String unitName) {
		super(pamConfiguration, unitType, unitName);
		// TODO Auto-generated constructor stub
	}

}
