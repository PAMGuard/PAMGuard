package PamController.command;

import org.json.JSONArray;
import org.json.JSONObject;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.statusManager.ModuleSummarizer;
import PamUtils.PamCalendar;

/**
 * Gets a summary string made up of strings from 
 * multiple modules. 
 * @author Doug Gillespie
 *
 */
public class SummaryCommand extends ExtCommand {
	
	private long lastCallTime = 0;
	
	/**
	 * Some of the summary commands now return data in three different formats. 
	 * There are historical reasons for this, which we'll not go into, but 
	 * we'll just say that it's a form of divergent evolution and we need
	 * to support all three. 
	 */
	public static final String CSV = "csv";
	public static final String JSON = "json";
	public static final String XML = "xml";

	public SummaryCommand() {
		super("summary", true);
	}

	@Override
	public String execute(String command) {
//		String[] cmdBits = CommandManager.splitCommandLine(command);
//		boolean clear = true;
//		// first word is the command, we want one after that. 
//		if (cmdBits.length >= 2 && cmdBits[1] != null) {
//			String bit = cmdBits[1].trim().toLowerCase();
//			if (bit.equals("0") || bit.equals("false")) {
//				clear = false;
//			}
//			
//		}
		String [] splitCommand = command.split(" ");
		String format = CSV;
		if(splitCommand.length>1) format = splitCommand[1];
		return ModuleSummarizer.getModulesSummary(true,format);
	}
	

	public String getModulesSummary(boolean clear, String format) {
		PamController pamController = PamController.getInstance();
		int nMod = pamController.getNumControlledUnits();
		PamControlledUnit aModule;
		String totalString;
		String aString;
		if (lastCallTime == 0) {
			lastCallTime = PamCalendar.getSessionStartTime();
		}
		long nowTime = PamCalendar.getTimeInMillis();
		totalString = PamCalendar.formatDBDateTime(lastCallTime) + "-" + PamCalendar.formatDBDateTime(nowTime);
		String mainSummary = pamController.getMainSummary(clear);
		String mainName = "PAMGUARD";
		totalString += String.format("\n<%s>%s<\\%s>", mainName, 
				 mainSummary, mainName);
		int usedModules = 0;
		for (int i = 0; i < nMod; i++) {
			aModule = pamController.getControlledUnit(i);
			aString = aModule.getModuleSummary(clear,format);
			if (aString == null) {
				continue;
			}
			usedModules ++;
			totalString += String.format("\n<%s>%s:%s<\\%s>", aModule.getShortUnitType(), 
					aModule.getUnitName(), aString, aModule.getShortUnitType());
		}
		if (clear) {
			lastCallTime = nowTime;
		}
		return totalString;
	}

	@Override
	public String getHint() {
		return "Get summary information about each running process";
	}
}
