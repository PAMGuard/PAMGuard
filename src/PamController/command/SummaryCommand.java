package PamController.command;

import Map.GetMapFile;
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
	public boolean execute() {
		return true;
	}

	@Override
	public String getReturnString() {
		PamController pamController = PamController.getInstance();
		int nMod = pamController.getNumControlledUnits();
		PamControlledUnit aModule;
		String totalString;
		String aString;
		if (lastCallTime == 0) {
			lastCallTime = PamCalendar.getSessionStartTime();
		}
		long nowTime = PamCalendar.getTimeInMillis();
		totalString = PamCalendar.formatDateTime(lastCallTime) + "-" + PamCalendar.formatDateTime(nowTime);
		int usedModules = 0;
		for (int i = 0; i < nMod; i++) {
			aModule = pamController.getControlledUnit(i);
			aString = aModule.getModuleSummary();
			if (aString == null) {
				continue;
			}
			usedModules ++;
			/*
		strcat(moduleSummaryText, "\n");
		strcat(moduleSummaryText, module->getModuleName());
		strcat(moduleSummaryText, ":");
		strcat(moduleSummaryText, moduleText);
			 */
			totalString += String.format("\n<%s>%s:%s<\\%s>", aModule.getShortUnitType(), 
					aModule.getUnitName(), aString, aModule.getShortUnitType());
		}
		return totalString;
	}

	@Override
	public String getHint() {
		return "Get summary information about each running process";
	}
}
