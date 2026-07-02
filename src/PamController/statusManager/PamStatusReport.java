package PamController.statusManager;

import java.util.ArrayList;

import org.json.JSONObject;

import PamController.PamControlledUnit;
import PamController.PamController;

public class PamStatusReport {

	public static String buildFullPamStatusReport(boolean clear, String format) {
		
		if(!format.equals("json")) {
			return ModuleSummarizer.getModulesSummary(clear, format);
		}
		
		JSONObject fullPamStatus = new JSONObject(ModuleSummarizer.getModulesSummary(clear, format));
		
		fullPamStatus.put("programStatus", PamController.getInstance().getRealStatus());
		
		return "";
		
	}
	
	private static JSONObject getDAQInfo() {
		
		ArrayList<PamControlledUnit> pamControlledUnits = PamController.getInstance().findControlledUnits("Data Acquisition", null);
		for(PamControlledUnit unit: pamControlledUnits) {
			unit.tellModule("gettimeinfo");
		}
		
		
		return new JSONObject();
		
	}
	
}
