package PamController.command;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;

/**
 * Gets a summary string made up of strings from 
 * multiple modules. 
 * @author Doug Gillespie
 *
 */
public class SummaryCommand extends ExtCommand {
	
	private long lastCallTime = 0;

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
		return getModulesSummary(true);
	}
	
	public String getModulesSummary(boolean clear) {
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
		int usedModules = 0;
		for (int i = 0; i < nMod; i++) {
			aModule = pamController.getControlledUnit(i);
			aString = aModule.getModuleSummary(clear);
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
