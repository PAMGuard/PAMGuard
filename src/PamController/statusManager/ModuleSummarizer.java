package PamController.statusManager;

import org.json.JSONArray;
import org.json.JSONObject;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamUtils.PamCalendar;

public class ModuleSummarizer {
	
	private static long lastCallTime = 0;
	
	public static String getModulesSummary(boolean clear, String format) {
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
		
		JSONObject returnJson = new JSONObject();
		JSONArray jsonSummaries = new JSONArray();
		for (int i = 0; i < nMod; i++) {
			aModule = pamController.getControlledUnit(i);
			aString = aModule.getModuleSummary(clear, format);
			if (aString == null) {
				continue;
			}
			usedModules ++;
			totalString += String.format("\n<%s>%s:%s<\\%s>", aModule.getShortUnitType(), 
					aModule.getUnitName(), aString, aModule.getShortUnitType());
			if(format.equals("json")) {
				JSONObject moduleSummary = new JSONObject();
				moduleSummary.put("moduleType", aModule.getUnitType());
				moduleSummary.put("moduleName", aModule.getUnitName());
				if(aString.startsWith("{")) {
					moduleSummary.put("moduleSummary", new JSONObject(aString));

				}else {
					moduleSummary.put("moduleSummary", new JSONObject("{\"Pamguard Module has no summary\":\"None\"}"));
				}
				jsonSummaries.put(moduleSummary);
			}
			
		}
		
		if(format.equals("json")) {
			returnJson.put("moduleSummaries", jsonSummaries);
			returnJson.put("summaryStartTime", lastCallTime);
			returnJson.put("summaryEndTime", nowTime);
		}
		
		if (clear) {
			lastCallTime = nowTime;
		}
		
		if(format.equals("json")) return returnJson.toString();
		
		return totalString;
	}
	
}
