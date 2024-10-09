package PamController;

/**
 * Parameters for choosing pAMGuard run mode. 
 */
public class PamRunModeParams  {

	/**
	 * The mode to run. 
	 */
	public int runMode = PamController.RUN_PAMVIEW;

	public String getRunString() {
		return getRunString(runMode);
	}
	
	public static String getRunString(int runbModeParams) {
		String runMode  =null;
		switch (runbModeParams) {
		case PamController.RUN_NORMAL:
			runMode = "";
			break;
		case PamController.RUN_PAMVIEW:
			runMode ="-v";
			break;
		}
		return runMode;
	}
	
	public static String getRunString(PamRunModeParams runbModeParams) {
		return getRunString(runbModeParams); 
	}

}
