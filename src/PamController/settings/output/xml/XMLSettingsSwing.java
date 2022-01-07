package PamController.settings.output.xml;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import PamController.PamFolders;
import PamController.PamSettings;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.PamUtils;
import PamUtils.TxtFileUtils;
import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;

/**
 * Some Swing dialogs n stuff for managing module settings in and out of 
 * XML. 
 * Mostly calling through to functions in PamguardXMLReader and PamguardXMLWriter
 * @author dg50
 *
 */
public class XMLSettingsSwing {
	
	private PamFileChooser pamFileChooser;

	public XMLSettingsSwing() {
		PamFileFilter fileFilter = new PamFileFilter("xlm file", "xml");
		pamFileChooser = new PamFileChooser(PamFolders.getDefaultProjectFolder());
		pamFileChooser.setFileFilter(fileFilter);
	}
	
	/**
	 * Write settings for a single object to an xml file.  
	 * @param pamSettings
	 * @param data
	 * @param suggestedName
	 * @return
	 */
	public boolean writeXMLSettings(Window parent, PamSettings pamSettings, Serializable data, String suggestedName) {
		/*
		 * first work out a sensible file name. 
		 */
		String fileName = suggestFileName(pamSettings, suggestedName);

		int ans = pamFileChooser.showSaveDialog(parent);
		if (ans == JFileChooser.APPROVE_OPTION) {
			File file = pamFileChooser.getSelectedFile();
			if (file.exists()) {
				String msg = String.format("Do you want to overwrite %s ?", file.getName()); 
				ans = WarnOnce.showWarning(parent, "File already exists", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return true; // not an error, so return true
				}
			}
			return saveXMLSettings(parent, pamSettings, data, pamFileChooser.getSelectedFile());
		}
		else {
			return false;
		}
	}

	/**
	 *  called once the file is selected. Should be a viable file to write to 
	 * @param parent
	 * @param pamSettings
	 * @param data
	 * @param selectedFile
	 */
	private boolean saveXMLSettings(Window parent, PamSettings pamSettings, Serializable data, File selectedFile) {
		selectedFile = PamFileFilter.checkFileEnd(selectedFile, ".xml", true);
		PamguardXMLWriter xmlWriter = PamguardXMLWriter.getXMLWriter();
		return xmlWriter.writeOneModule(selectedFile, pamSettings, data);
	}

	/**
	 * Work out  asensible file name from suggested name. 
	 * @param pamSettings 
	 * @param suggestedName
	 * @return
	 */
	private String suggestFileName(PamSettings pamSettings, String suggestedName) {
		if (suggestedName == null || suggestedName.length() == 0) {
			suggestedName = pamSettings.getUnitName();
		}
		// see if it has a file end on it. 
		suggestedName = checkFileEnd(suggestedName, ".xml");
		File path = checkFolder(suggestedName);
		
		
		// see if there is already a valid folder. 
//		file
		
		return null;
	}

	private File checkFolder(String suggestedName) {
		File file = new File(suggestedName);
		if (file.exists()) {
			// file already exists, so must have something doing for it. 
			return file;
		}
		String fileName = file.getName();
		File path = PamFolders.getFileChooserPath(file);
		return new File(path, fileName);
	}

	/**
	 * Check there is a file end of some sort. Doesn't have to be xml
	 * @param suggestedName
	 * @param name
	 * @return
	 */
	private String checkFileEnd(String suggestedName, String name) {
		int dotPos = name.indexOf('.');
		if (dotPos < 0) {
			name += ".xml";
		}
		return name;
	}

	/**
	 * Find a file (using dialog) and import settings of given class within that file
	 * @param parent
	 * @param objectClass class to search for in file
	 * @return imported data - selected file AND data object. 
	 */
	public XMLImportData importXMLSettings(Window parent, Class objectClass) {
		int ans = pamFileChooser.showOpenDialog(parent);
		if (ans != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = pamFileChooser.getSelectedFile();
		if (file == null || file.exists() == false) {
			WarnOnce.showWarning("XML import error", "The selected file does not exist", WarnOnce.WARNING_MESSAGE);
			return null;
		}
		return importXMLSettings(parent, file, objectClass);
	}
	
	public XMLImportData importXMLSettings(Window parent, File file, Class objectClass) {
		PamguardXMLReader xmlReader = new PamguardXMLReader(file.getAbsolutePath());

		ArrayList<Node> modules = xmlReader.findSettingsForClass(objectClass.getName());
		if (modules == null || modules.size() == 0) {
			WarnOnce.showWarning(parent, "XML Import Error", "file contains no modules for class " + objectClass.getName(), 
					WarnOnce.WARNING_MESSAGE, null);
			return null;
		}
		int sel = 0;
		if (modules.size() > 1) {
			// need to make a dialog to select the set of settings we want ...
			String[] types = new String[modules.size()];
			String[] names = new String[modules.size()];
			for (int i = 0; i < types.length; i++) {
				NamedNodeMap atts = modules.get(i).getAttributes();
				types[i] = xmlReader.getAttrString(atts, "Type");
				names[i] = xmlReader.getAttrString(atts, "Name");
			}
			sel = NodeSelectDialog.showDialog(parent, objectClass, types, names);
			if (sel < 0) {
				return null;
			}
		}
		
//		Object o = xmlReader.getModuleForClass(objectClass.getName());
		Object o = xmlReader.unpackSettingsNode(modules.get(sel));
		if (o == null) {
			return null;
		}
		if (o.getClass() != objectClass) {
			WarnOnce.showWarning("XML import error", "Returned object is of the wrong class", WarnOnce.WARNING_MESSAGE);
			return null;
		}
		return new XMLImportData(file, o);
	}

}
