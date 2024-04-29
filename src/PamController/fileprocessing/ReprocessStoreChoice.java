package PamController.fileprocessing;

/**
 * Choices on what to do when re-processing data and finding that output data already exist. 
 * @author dg50
 *
 */
public enum ReprocessStoreChoice {
	
	
	STARTNORMAL, CONTINUECURRENTFILE, CONTINUENEXTFILE, OVERWRITEALL, DONTSSTART;

	public static final String paramName = "-reprocessoption";
	
	@Override
	public String toString() {
		switch (this) {
		case STARTNORMAL:
			return "Start normally. Note risk of overwriting!";
		case CONTINUECURRENTFILE:
			return "Continue from start of last input file processed";
		case CONTINUENEXTFILE:
			return "Continue from start of next input file to process";
		case DONTSSTART:
			return "Don't start processing";
		case OVERWRITEALL:
			return "Overwrite all existing output data";
		default:
			break;
		}
		return null;
	}
	
	public String getToolTip() {
		switch (this) {
		case STARTNORMAL:
			return "No risk of data overlap, so system will start normally";
		case CONTINUECURRENTFILE:
			return "System will work out how far data processing has got and continue from the start of the file it stopped in";
		case CONTINUENEXTFILE:
			return "System will work out how far data processing has got and continue from the start of the file AFTER the one it stopped in";
		case DONTSSTART:
			return "Processing will not start. Select alternative storage locations / databases and try again";
		case OVERWRITEALL:
			return "Overwrite existing output data. All existing data will be deleted";
		default:
			break;
		}
		return null;
	}
	

}
