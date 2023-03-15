package PamController.settings.output.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.javafx.runtime.VersionInfo;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamTempSettings;
import PamController.PamguardVersionInfo;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamCalendar;
import PamUtils.XMLUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import binaryFileStorage.BinaryStore;

/**
 * Class for writing XML configuration output to a file. 
 * @author dg50
 *
 */
public class PamguardXMLWriter implements PamSettings {

	private PamController pamController;
	private ArrayList<PamSettings> settingsSets;
	private boolean[] usedSettingsSets;
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
	
	private XMLWriterSettings writerSettings = new XMLWriterSettings();
	
	private static PamguardXMLWriter singleInstance;

	private PamguardXMLWriter() {
		this.pamController = PamController.getInstance();
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * 
	 * @return Singleton XML writer. There are shared settings, so this is needed. 
	 * May have to re-think this if we try to use this writer for other things apart from 
	 * settings, or writing settings in other ways, such as binary headers in order
	 * to make it thread safe.  
	 */
	public static PamguardXMLWriter getXMLWriter() {
		if (singleInstance == null) {
			singleInstance = new PamguardXMLWriter();
		}
		return singleInstance;
	}

	/**
	 * Make a document with the options specified in writerSettings.
	 * @param time timestamp for document
	 * @return
	 */
	public Document writeSelection(long time) {
			
		if (writerSettings.moduleSelection == XMLWriterSettings.MODULES_ALL) {
			return writeEverything(time);
		}
		// otherwise find the listed module. 
		PamControlledUnit pcu = pamController.findControlledUnit(writerSettings.selectedModuleType, writerSettings.selectedModuleName);
		if (pcu == null) {
			return null;
		}
		if (pcu instanceof PamSettings == false) {
			return null;
		}
		if (writerSettings.moduleSelection == XMLWriterSettings.MODULES_ONE) {
			return writeOneModule((PamSettings) pcu, time);
		}
		else if (writerSettings.moduleSelection == XMLWriterSettings.MODULES_CHAIN) {
			return writeModuleChain(pcu, time);
		}
		else {
			return null;
		}
	}
	/**
	 * Write a module and all modules that feed into that module. e.g. if
	 * you write a click detector, you'll also get an acquisition module. 
	 * @param pcu Pamguard module to write
	 * @param time timestamp for document
	 * @return XML document. 
	 */
	public Document writeModuleChain(PamControlledUnit pcu, long time) {
		/**
		 * Need to work through all processes in the module, finding their 
		 * parent and parents parent, etc. avoiding repeats, to make a list
		 * of modules. 
		 */
		ArrayList<PamControlledUnit> chainedUnits = new ArrayList();
		chainedUnits.add(pcu);
		int nP = pcu.getNumPamProcesses();
		for (int i = 0; i < nP; i++) {
			findChainedUnits(chainedUnits, pcu.getPamProcess(i));
		}
		Document doc = createDocument(time);
		Element modules = doc.createElement("MODULES");
		int nCU = pamController.getNumControlledUnits();
		Element root = doc.createElement("PAMGUARD");
		doc.appendChild(root);
		root.appendChild(getInfo(doc, time));
		root.appendChild(modules);
		// now get a list of all modules. 
		// remake a list of all PAMGuard settings ...
		makeSettingsList();
		Element moduleData;
		for (int i = chainedUnits.size()-1; i>= 0; i--) {
			PamControlledUnit pamCU = chainedUnits.get(i);
			if (pamCU instanceof PamSettings) {
				moduleData = writeUnitSettings(doc, modules, (PamSettings) pamCU);
				if (moduleData != null) {
					modules.appendChild(moduleData);
				}
			}
		}
		return doc;
	}

	/**
	 * Iterate through processes and parents to get a complete list of all units feeding into
	 * the top level unit. 
	 * @param chainedUnits
	 * @param pamProcess
	 */
	private void findChainedUnits(ArrayList<PamControlledUnit> chainedUnits, PamProcess pamProcess) {
		PamDataBlock<PamDataUnit> sourceData = pamProcess.getParentDataBlock();
		if (sourceData == null) {
			return;
		}
		PamProcess sourceProcess = sourceData.getParentProcess();
		if (sourceProcess == null) {
			return;
		}
		PamControlledUnit pcu = sourceProcess.getPamControlledUnit();
		if (chainedUnits.contains(pcu) == false) {
			chainedUnits.add(pcu);
		}
		findChainedUnits(chainedUnits, sourceProcess);
	}
	
	

	public Document writeOneModule(PamSettings pcu, long time) {	
		Document doc = createDocument(time);
		Element modules = doc.createElement("MODULES");
		Element root = doc.createElement("PAMGUARD");
		doc.appendChild(root);
		root.appendChild(getInfo(doc, time));
		root.appendChild(modules);
		// now get a list of all modules. 
		// remake a list of all PAMGuard settings ...
		makeSettingsList();
		Element moduleData;
		moduleData = writeUnitSettings(doc, modules, pcu);
		if (moduleData != null) {
			modules.appendChild(moduleData);
		}
		return doc;
	}
	
	
	/**
	 * Can be called from a dialog to write one object into a settings xml file. 
	 * It will write the objtoWrite in place of any settings from the module, since
	 * from the dialog, they may not be current. 
	 * @param file
	 * @param pamControlledUnit
	 * @param objToWrite
	 * @return
	 */
	public boolean writeOneModule(File file, PamSettings pamControlledUnit, Serializable objToWrite) {
		PamSettings[] aSet = new PamSettings[1];
		aSet[0] = new PamTempSettings(pamControlledUnit, objToWrite);
		Document doc = createDocument(System.currentTimeMillis());
		Element modules = doc.createElement("MODULES");
		Element root = doc.createElement("PAMGUARD");
		doc.appendChild(root);
		root.appendChild(getInfo(doc, System.currentTimeMillis()));
		root.appendChild(modules);
		Element moduleData;
		moduleData = writeUnitSettings(doc, modules, pamControlledUnit, aSet);
		if (moduleData != null) {
			modules.appendChild(moduleData);
		}
		try {
			writeToFile(doc, file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public Document writeModules(ArrayList<PamSettings> settings) {
		Document doc = createDocument(System.currentTimeMillis());
		Element modules = doc.createElement("MODULES");
		Element root = doc.createElement("PAMGUARD");
		doc.appendChild(root);
		root.appendChild(getInfo(doc, System.currentTimeMillis()));
		root.appendChild(modules);
		Element moduleData;
		for (PamSettings aSet:settings) {
			PamSettings[] asArray = new PamSettings[1];
			asArray[0] = aSet;
			moduleData = writeUnitSettings(doc, modules, aSet, asArray);
			if (moduleData != null) {
				modules.appendChild(moduleData);
			}
		}
		
		return doc;
	}

	private Element getInfo(Document doc, long time) {
		Element info = doc.createElement("INFO");
		info.setAttribute("TIMESTAMP", PamCalendar.formatDateTime(time));
		String name = pamController.getPSFName();
		info.setAttribute("CONFIGURATION", name);
		info.setAttribute("VERSION", PamguardVersionInfo.version);
//		info.setAttribute("REVISION", PamguardVersionInfo.revisionString);
		info.setAttribute("RELEASETYPE", PamguardVersionInfo.getReleaseType().toString());
		info.setAttribute("JAVA", VersionInfo.getVersion());
		return info;
	}
	
	public Document writeEverything(long time) {
		Document doc = createDocument(time);
		Element modules = doc.createElement("MODULES");
		int nCU = pamController.getNumControlledUnits();
		Element root = doc.createElement("PAMGUARD");
		doc.appendChild(root);
		root.appendChild(getInfo(doc, time));
		root.appendChild(modules);
		// now get a list of all modules. 
		// remake a list of all PAMGuard settings ...
		makeSettingsList();
		Element moduleData;
		for (int i = 0; i < nCU; i++) {
			PamControlledUnit pcu = pamController.getControlledUnit(i);
			if (pcu instanceof PamSettings == false) {
				continue;
			}
			moduleData = writeUnitSettings(doc, modules, (PamSettings) pamController.getControlledUnit(i));
			if (moduleData != null) {
				modules.appendChild(moduleData);
			}
		}
		/*
		 * Now go through and write everything that wasn't included in one of the modules
		 * (will be interesting to see what's there !)
		 */
		if (writerSettings.nonModuleData) {
			Element others = getNonModuleSettings(doc);
			if (others != null) {
				root.appendChild(others);
			}
		}
		return doc;
	}
	
	private Element getNonModuleSettings(Document doc) {
		Element others = doc.createElement("OTHERSETTINGS");
		int nOther = 0;
		for (int i = 0; i < settingsSets.size(); i++) {
			if (usedSettingsSets[i]) {
				continue;
			}
			Element setEl = writeSettings(doc, settingsSets.get(i), new ArrayList<Object>());
			if (setEl != null) {
				others.appendChild(setEl);
				nOther++;
			}
		}
		if (nOther > 0) {
			return others;
		}
		else {
			return null;
		}
	}

	/**
	 * Write the xml document to System.out.
	 * @param doc xml document
	 */
	public void writeToTerminal(Document doc) {
		String asString = getAsString(doc);
		if (asString!=null) {
			System.out.println(asString);
		}
	}
	
	/**
	 * Write the xml document to a file which will have the same path and 
	 * name as the current psf file, appended with the data constructed from the time argument
	 * @param doc xml document
	 * @param time time in milliseconds. 
	 */
	public boolean writeToFile(Document doc, long time) {
		String fileName = makeFileName(time);
		File outFile = new File(fileName);
		try {
			writeToFile(doc, outFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Write to the given file. 
	 * @param doc xml document
	 * @param outFile file
	 * @throws IOException 
	 */
	public void writeToFile(Document doc, File outFile) throws IOException {
		String asString = getAsString(doc);
		if (asString!=null) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(outFile, false));
				out.write(asString);
			    out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * Get the xml document as a String.
	 * @param doc xml document
	 * @return xml content as a a string. 
	 */
	public String getAsString(Document doc) {
		return getAsString(doc, true);
//		try {
//			DOMSource domSource = new DOMSource(doc);
//			StringWriter writer = new StringWriter();
//			StreamResult result = new StreamResult(writer);
//			TransformerFactory tf = TransformerFactory.newInstance();
//			Transformer transformer = tf.newTransformer();
//			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
////			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//			transformer.transform(domSource, result);
//			return writer.toString();
//		} catch (TransformerException e) {
//			e.printStackTrace();
//			return null;
//		}
	}
	/**
	 * Get the xml document as a String.
	 * @param doc xml document
	 * @param indent Indent / format the document. 
	 * @return xml content as a a string. 
	 */
	public String getAsString(Document doc, boolean indent) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String makeFileName(long time) {
		PamSettingManager settingsManager = PamSettingManager.getInstance();
		String settingFile = settingsManager.getSettingsFileName();
		File f = new File(settingFile);
		String psfname = f.getPath();
		String name = psfname.substring(0, psfname.length()-4);
		String newName = String.format("%s_%s.xml", name, PamCalendar.formatFileDateTime(time, false));
		return newName;
	}

	/**
	 * Writes unit settings from the main settings list of settings recovered 
	 * from modules. Note that this can't be used for what may be temporary
	 * settings currently not saved within a dialog. <br>
	 * There is a bit of messing about to avoid rewriting an object if 
	 * there are two objects that refer backwards and forth to each other ? 
	 * @param doc
	 * @param parent
	 * @param pamSettingsUnit 
	 * @return xml element
	 */
	public Element writeUnitSettings(Document doc, Element parent, PamSettings pamSettingsUnit) {

		int[] settingInds = findSettings(null, pamSettingsUnit.getUnitName());
		PamSettings[] settingsObjects = null;
		if (settingInds != null) {
			settingsObjects = new PamSettings[settingInds.length];
			for (int i = 0; i < settingInds.length; i++) {
				settingsObjects[i] = settingsSets.get(settingInds[i]);
				usedSettingsSets[settingInds[i]] = true;
			}
		}
		return writeUnitSettings(doc, parent, pamSettingsUnit, settingsObjects);
		
		
	}

	/**
	 * Write a units settings. 
	 * @param doc Document
	 * @param parent XML parent
	 * @param pamSettingsUnit Generally a PAMcontrolledUnit, so it can write processes too. 
	 * @param toWrite List of settings to write, generally all settings with same unitName as pamSettingsUnit but
	 * can be temporary settings objects when writing temporary settings from dialogs. 
	 * @return new XML element. 
	 */
	public Element writeUnitSettings(Document doc, Element parent, PamSettings pamSettingsUnit, PamSettings[] toWrite) {
		Element moduleData = doc.createElement("MODULE");
		moduleData.setAttribute("Java.class", pamSettingsUnit.getClass().getName());
		moduleData.setAttribute("UnitType", pamSettingsUnit.getUnitType());
		moduleData.setAttribute("UnitName", pamSettingsUnit.getUnitName());

		if (pamSettingsUnit instanceof PamControlledUnit) {
			PamControlledUnit pamControlledUnit = (PamControlledUnit) pamSettingsUnit;
			int nP = pamControlledUnit.getNumPamProcesses();
			Element processData;
			for (int i = 0; i < nP; i++) {
				processData = writeProcessSettings(doc, moduleData, pamControlledUnit.getPamProcess(i));
				moduleData.appendChild(processData);
			}
		}
		// so far, everything above is the same as for the Decimus output. Now it changes however
		// since we're going to add a whole load of settings 0 - many depending on the module ...
		if (toWrite != null) {
			Element settingEl = doc.createElement("CONFIGURATION");
			moduleData.appendChild(settingEl);
			for (int i = 0; i < toWrite.length; i++) {
				Element setEl = writeSettings(doc, toWrite[i], new ArrayList<Object>());
				if (setEl != null) {
					settingEl.appendChild(setEl);
				}
			}
		}

		return moduleData;
	}

	/**
	 * Write settings for a settings object, using the standard retreived object
	 * from the settings. 
	 * @param doc
	 * @param pamSettings
	 * @param objectHierarchy
	 * @return doc element
	 */
	private Element writeSettings(Document doc, PamSettings pamSettings, ArrayList<Object> objectHierarchy) {
		return writeSettings(doc, pamSettings, pamSettings.getSettingsReference(), objectHierarchy);
	}

	/**
	 * Write settings using an object of choice instead of the standard one from PamSettings. 
	 * <br> can be useful in saving specific parameters. 
	 * @param doc
	 * @param pamSettings
	 * @param data
	 * @param objectHierarchy
	 * @return
	 */
	private Element writeSettings(Document doc, PamSettings pamSettings, Object data, ArrayList<Object> objectHierarchy) {
		Element el = doc.createElement("SETTINGS");
		el.setAttribute("Type", pamSettings.getUnitType());
		el.setAttribute("Name", pamSettings.getUnitName());
		el.setAttribute("Class", data.getClass().getName());
		el.setAttribute("Version", String.format("%d",pamSettings.getSettingsVersion()));
		//		if (data.getClass().getName().equals("clickDetector.ClickParameters")) {
		writeObjectData(doc, el, data, objectHierarchy);
		//		}
		return el;
	}

	private Element writeObjectData(Document doc, Element el, Object data, ArrayList<Object> objectHierarchy) {
		if (data == null) {
			return null;
		}
		if (objectHierarchy.contains(data)) {
			// just write the reference, but nothing else or we'll end up in an infinite loop of objects. 
			Element e = doc.createElement("Object");
			e.setAttribute("Class", data.getClass().getName());
			e.setAttribute("Reference", String.format("%d", System.identityHashCode(data)));
			return e;
		}
		PamParameterSet parameterSet;
		if (data instanceof ManagedParameters) {
			parameterSet = ((ManagedParameters) data).getParameterSet();
		}
		else {
			int exclusions = 0;
			if (writerSettings.includeConstants == false) {
				exclusions = Modifier.STATIC | Modifier.FINAL;
			}
			parameterSet = PamParameterSet.autoGenerate(data, exclusions);
		}
		if (parameterSet == null) {
			return null;
		}

		objectHierarchy.add(data);
		for (PamParameterData pamParam:parameterSet.getParameterCollection()) {
			try {
				Object paramData = pamParam.getData();
				writeField(doc, el, paramData, pamParam, objectHierarchy);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		return el;
	}

	private Element writeField(Document doc, Element el, Object data, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {

		if (data == null) {
			return null;
		}
		Class javaClass = data.getClass();

		if (wantField(pamParam) == false) {
			return null;
		}
		if (isWritableType(javaClass)) {
			return writePrimitive(doc, el, data, pamParam);
		}
		if (javaClass.isArray()) {
			return writeArray(doc, el, data, pamParam, objectHierarchy);
		}
		if (List.class.isAssignableFrom(javaClass)){
			return writeList(doc, el, data, pamParam, objectHierarchy);
		}
		if (Map.class.isAssignableFrom(javaClass)){
			return writeMap(doc, el, data, pamParam, objectHierarchy);
		}
		if (File.class.isAssignableFrom(javaClass)) {
			return writeFile(doc, el, data, pamParam);
		}
		
		else {
			Element e = makeElement(doc, pamParam.getFieldName(), data.getClass().getName());
			el.appendChild(e);
			writeObjectData(doc, e, data, objectHierarchy);
			return e;
		}
		//		System.out.println("Unknown class: " + javaClass.toString());

		//		return false;
	}

	private boolean wantField(Field field) {
		if (field == null) {
			return true;
		}
		int modifiers = field.getModifiers();
		//		if ((modifiers & Modifier.PRIVATE) != 0) {
		//			return false;
		//		}
		if ((modifiers & Modifier.TRANSIENT) != 0) {
			return false;
		}
		//		if ((modifiers & Modifier.PROTECTED) != 0) {
		//			return false;
		//		}
		//		if ((modifiers & Modifier.FINAL) != 0) {
		//			return true;
		//		}
		return true;
	}

	private boolean wantField(Object data) {
		if (data == null) {
			return false;
		}
		Class cls = data.getClass();
		if (cls.isEnum()) {
			return false;
		}
		return true;
	}

	private Element writeMap(Document doc, Element el, Object fieldData, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {
		Map map = (Map) fieldData;
		Element e = makeElement(doc, pamParam);
		el.appendChild(e);
		Collection<Map.Entry> mapValues = map.entrySet();
		Iterator<Entry> mapit = mapValues.iterator();
		int iEnt = 0;
		while (mapit.hasNext()) {
			Entry mapVal = mapit.next();
			try {
				Element mapEl = doc.createElement(String.format("Element%d",iEnt));
				mapEl.setAttribute("Key", mapVal.getKey().toString());
				Object mapObj = mapVal.getValue();
				if (isWritableType(mapObj.getClass())) {
					mapEl.setAttribute("Value", mapObj.toString());
				}
				else {
					writeObjectData(doc, mapEl, mapObj, objectHierarchy);
				}
				e.appendChild(mapEl);
			}
			catch (Exception ex) {
				System.out.println("Error writing object " + fieldData.toString() + ": " + mapVal.getKey().toString());
				System.out.println(ex.getMessage());
			}
			iEnt++;
		}

		return e;
	}
	
	private Element writeList(Document doc, Element el, Object fieldData, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {
		List list = (List) fieldData;
		return writeArray(doc, el, list.toArray(), pamParam, objectHierarchy);
	}
	
	private Element writeArray(Document doc, Element el, Object fieldData, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {
		try {
			boolean isPrimative = true;
			int len = Array.getLength(fieldData);
			for (int i = 0; i < len; i++) {
				Object o = Array.get(fieldData, i);
				if (o != null && isWritableType(o.getClass()) == false) {
					isPrimative = false;
				}
			}
			if (isPrimative) {
				return writePrimitiveArray(doc, el, fieldData, pamParam);
			}
			else {
				return writeObjectArray(doc, el, fieldData, pamParam, objectHierarchy);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	//	private Element makeElement(Document doc, Class<?> clazz) {
	//		return makeElement(doc, clazz.)
	//	}
	private Element makeElement(Document doc, PamParameterData pamParam) {
		Element el = makeElement(doc, pamParam.getFieldName(), pamParam.getDataClass().getName());
		if (writerSettings.writeShortNames && pamParam.getShortName() != null) {
			el.setAttribute("Name", pamParam.getShortName());
		}
		if (writerSettings.writeToolTips && pamParam.getToolTip() != null) {
			el.setAttribute("Tip", pamParam.getToolTip());
		}
		return el;
	}

	private Element makeElement(Document doc, String name, String type) {
		Element e = doc.createElement(name);
		if (type != null) {
			e.setAttribute("Class", type);
		}
		return e;
	}

	private Element writeObjectArray(Document doc, Element el, Object fieldData, PamParameterData pamParam, ArrayList<Object> objectHierarchy) {		
		try {
			Element e = makeElement(doc, pamParam);
			int len = Array.getLength(fieldData);
			for (int i = 0; i < len; i++) {
				Object o = Array.get(fieldData, i);
				if (o != null) {
					Element fE = writeField(doc, e, o, pamParam, objectHierarchy);
					fE.setAttribute("Index", new Integer(i).toString());
				}
			}
			el.appendChild(e);
			return e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	private Element writePrimitiveArray(Document doc, Element el, Object fieldData, PamParameterData pamParam) {
		try {
			//			Element e = makeElement(doc, field);
			int len = Array.getLength(fieldData);
			String output = "";
			for (int i = 0; i < len; i++) {
				Object o = Array.get(fieldData, i);
				if (o == null) {
					output += "null";
				}
				else {
					output += o.toString(); 
				}
				if (i < len-1) {
					output += ",";
				}
			}
			return writePrimitive(doc, el, output, pamParam);
			//			el.appendChild(e);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	private Element writePrimitive(Document doc, Element el, Object fieldData, PamParameterData pamParam) {
		String name = pamParam.getFieldName();
		Element e = makeElement(doc, pamParam);;
		if (fieldData == null) {
			e.setAttribute("Value", null);
		}
		else {
			e.setAttribute("Value", fieldData.toString());
		}
		el.appendChild(e);
		return e;

	}
	
	private Element writeFile(Document doc, Element el, Object fieldData, PamParameterData pamParam) {
		Element e = makeElement(doc, pamParam);;
		if (fieldData == null) {
			e.setAttribute("Value", null);
		}
		else {
			e.setAttribute("Value", fieldData.toString());
		}
		el.appendChild(e);
		return e;
	}

	private Element writeProcessSettings(Document doc, Element parent, PamProcess process) {
		Element processData = doc.createElement("PROCESS");
		processData.setAttribute("Name", process.getProcessName());
		PamDataBlock source = process.getParentDataBlock();
		if (source != null) {
			Element inputEl = source.getDataBlockXML(doc);
//			Element inputEl = doc.createElement("Input");
//			inputEl.setAttribute("Name", source.getLongDataName());
//			inputEl.setAttribute("Channels", String.format("0x%X", source.getChannelMap()));
			processData.appendChild(inputEl);
		}
		int nOut = process.getNumOutputDataBlocks();
		for (int i = 0; i < nOut; i++) {
			Element outEl = writeDatablockSettings(doc, processData, process.getOutputDataBlock(i));
			processData.appendChild(outEl);
		}
		return processData;
	}

	private Element writeDatablockSettings(Document doc, Element processData, PamDataBlock outputDataBlock) {
		Element blockData = doc.createElement("Output");
		blockData.setAttribute("Name", outputDataBlock.getLongDataName());
		blockData.setAttribute("Type", outputDataBlock.getUnitClass().getName());
		blockData.setAttribute("Channels", String.format("0x%X", outputDataBlock.getChannelMap()));
		blockData.setAttribute("SampleRate", new Float(outputDataBlock.getSampleRate()).toString());
		return blockData;
	}

	/**
	 * Find all settings for a unit type and unit name. IF both 
	 * are non null, then this should only return one thing. However 
	 * if type is null, we may get several settings with the same name
	 * as is common for some modules with settings spread about the place. 
	 * @param type unit type
	 * @param name unit name
	 * @return indexes of settings. 
	 */
	private int[] findSettings(String type, String name) {
		if (settingsSets == null) {
			return null;
		}
		int[] found = new int[settingsSets.size()];
		int nFound = 0;
		for (int i = 0; i < settingsSets.size(); i++) {
			if (usedSettingsSets[i]) {
				continue;
			}
			PamSettings aSet = settingsSets.get(i);
			if (type != null && type.equals(aSet.getUnitType()) == false) {
				continue;
			}
			if (name != null && name.equals(aSet.getUnitName()) == false) {
				continue;
			}
			found[nFound++] = i;
		}
		return Arrays.copyOf(found, nFound);
	}

	private ArrayList<PamSettings> makeSettingsList() {
		PamSettingManager settingsManager = PamSettingManager.getInstance();
		settingsSets = settingsManager.getOwners();
		if (settingsSets == null) {
			usedSettingsSets = null;
			return null;
		}
		usedSettingsSets = new boolean[settingsSets.size()];
		return settingsSets;
	}
	/**
	 * Create a document and add some version information. 
	 * @param time
	 * @return an open document to add to. 
	 */
	private Document createDocument(long time) {

		Document doc = XMLUtils.createBlankDoc();

		Element root = doc.createElement("PAMGUARD");
		// start with the basic version information about PAMGAURD.
		Element vInfo = doc.createElement("VERSIONINFO");
		root.appendChild(vInfo);
		vInfo.setAttribute("Created", PamCalendar.formatDateTime(System.currentTimeMillis()));
		vInfo.setAttribute("Version", PamguardVersionInfo.version);
//		vInfo.setAttribute("Revision", PamguardVersionInfo.revisionString);
		vInfo.setAttribute("ReleaseType", PamguardVersionInfo.getReleaseType().toString());


		return doc;
	}

	public static boolean isWritableType(Class<?> clazz)
	{
		if (clazz.isEnum()) return true;
		return WRAPPER_TYPES.contains(clazz);
	}

	private static Set<Class<?>> getWrapperTypes()
	{
		Set<Class<?>> ret = new HashSet<Class<?>>();
		ret.add(Boolean.class);
		ret.add(Character.class);
		ret.add(Byte.class);
		ret.add(Short.class);
		ret.add(Integer.class);
		ret.add(Long.class);
		ret.add(Float.class);
		ret.add(Double.class);
		ret.add(Void.class);
		ret.add(String.class); // not really a wrapper, but will want to write out anyway. 
		return ret;
	}

	/**
	 * @return the writerSettings
	 */
	public XMLWriterSettings getWriterSettings() {
		return writerSettings;
	}

	/**
	 * @param writerSettings the writerSettings to set
	 */
	public void setWriterSettings(XMLWriterSettings writerSettings) {
		this.writerSettings = writerSettings;
	}

	/**
	 * Write everything to an XML file with same name as psf, the date appended. 
	 * If binary store exists, put it in there, if not put it with the configuration 
	 * file. 
	 * @param timeNow
	 */
	public void writeStartSettings(long timeNow) {
		if (writerSettings.writeAtStart == false){
			return;
		}
		String fileName = makeFileName(timeNow);
		File xFile = new File(fileName);
		String path = null;
		BinaryStore bs = (BinaryStore) pamController.findControlledUnit(BinaryStore.defUnitType);
		if (bs != null) {
			path = bs.getFolderName(timeNow, true);
			// now replace the start of fileName;
			String name = xFile.getName();
			fileName = path + name;
		}
		Document doc = writeEverything(timeNow);
		try {
			writeToFile(doc, new File(fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getUnitName() {
		return "XML Writer";
	}

	@Override
	public String getUnitType() {
		return "XML Writer";
	}

	@Override
	public Serializable getSettingsReference() {
		return writerSettings;
	}

	@Override
	public long getSettingsVersion() {
		return XMLWriterSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		writerSettings = ((XMLWriterSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	
}
