package PamController.command;

import java.util.ArrayList;

import org.w3c.dom.Document;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.settings.output.xml.PamguardXMLWriter;

/**
 * Get XML settings from one or more modules. Note that the first two commands are
 * the names that are used in PamSettings which are usually the same as module names
 * but this gives a little more flexibility.  
 * @author dg50
 *
 */
public class GetXMLSettings extends ExtCommand {

	public GetXMLSettings() {
		super("getxmlsettings", true);
	}

	@Override
	public String execute(String command) {
		String[] commandWords = CommandManager.splitCommandLine(command);
		if (commandWords.length < 3) {
			return "Unspecified modules for settings";
		}
		PamSettingManager settingsManager = PamSettingManager.getInstance();
		String unitType = commandWords[1];
		String unitName = commandWords[2];
		ArrayList<PamSettings> foundSettings = settingsManager.findPamSettings(unitType, unitName);
		if (foundSettings == null || foundSettings.size() == 0) {
			return "No settings";
		}
		PamguardXMLWriter xmlWriter = PamguardXMLWriter.getXMLWriter();
		Document doc = xmlWriter.writeModules(foundSettings);
		String txt = xmlWriter.getAsString(doc);
		return txt;
	}

	@Override
	public String getHint() {
		return "Get XML settings for a named module";
	}

	
	
}
