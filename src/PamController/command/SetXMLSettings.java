package PamController.command;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.settings.output.xml.ModuleNode;
import PamController.settings.output.xml.PamguardXMLReader;

public class SetXMLSettings extends ExtCommand {

	public SetXMLSettings() {
		super("setxmlsettings", true);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute(String command) {
		/*
		 * Hopefully, we just need to remove the name of the command and trim and we've got perfect xml to play with.
		 */
		String xmlString = command.substring(getName().length());
		xmlString = xmlString.trim();
		PamSettingManager settingManager = PamSettingManager.getInstance();    
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(xmlString)));
		}    
		catch (Exception e) 
		{
			e.printStackTrace();
			return "invalid xml settings can't be pushed to modules";
		}
		PamguardXMLReader xmlReader = new PamguardXMLReader(doc);
		ArrayList<ModuleNode> nodes = xmlReader.getModuleNodes();
		for (int i = 0; i < nodes.size(); i++) {
			ModuleNode aNode = nodes.get(i);
			// now work out the name and type of the module and tell PamSettings to load 
			// these. 
			ArrayList<PamSettings> owner = settingManager.findPamSettings(aNode.getUnitType(), aNode.getUnitName());
			if (owner == null || owner.size() == 0) {
				return "no Java module for xml settings";
			}
			PamSettings own = owner.get(0);
//			System.out.println("XML Node " + aNode.toString());
			Object[] objData = xmlReader.unpackModuleNode(aNode);
//			System.out.println(objData);
			if (objData == null || objData.length == 0) {
				return "no Java object(s) for xml settings";
			}
			for (int iOb = 0; iOb < objData.length; iOb++) {
				/*
				 * Do a test to see if the object is the same type as the current settings. 
				 */
				Object currSettings = own.getSettingsReference();
				Object newSettings = objData[iOb];
				if (newSettings == null) {
					continue;
				}
				if (currSettings != null && currSettings.getClass() != newSettings.getClass()) {
					return "Created java settings object is not the same as currently used: " + currSettings.getClass().getName();
				}
				
				PamControlledUnitSettings pcus = new PamControlledUnitSettings(aNode.getUnitType(), aNode.getUnitName(), 
						aNode.getjClass(), own.getSettingsVersion(), newSettings);
				own.restoreSettings(pcus);
			}
		}

		return "xml settings pushed to modules";
		
	}

	@Override
	public String getHint() {
		return "Set XML settings for a named module, (see getxmlsettings to retreive module settings to modify)";
	}
}
