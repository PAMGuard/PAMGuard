package PamController.settings.output.xml;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterData;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.XMLUtils;

public class PamguardXMLReader {

//	private static String tstFile = "C:\\PamguardTest\\NOAA\\XMLTest1.xml";
//		private static String tstFile = "C:\\PamguardTest\\NOAA\\Drift_10_Humpbacks._19700101_000000.xml";


//	public static void main(String[] args) {
//		// tester to try unpacking a single file and creating an object out of it. 
//		PamguardXMLReader reader = new PamguardXMLReader(tstFile);
//		//		reader.unpackFile(tstFile);
//	}

	private ArrayList<ModuleNode> moduleNodes;

	public PamguardXMLReader(String fileName) {
		unpackFile(fileName);
	}

	private void unpackFile(String fileName) {

		Document doc = XMLUtils.createDocument(fileName);
		String str = XMLUtils.getStringFromDocument(doc);
//		System.out.println(str);
		NodeList childNodes = doc.getChildNodes();
		moduleNodes = new ArrayList<>();
		findModuleNodes(childNodes, moduleNodes);
//		System.out.printf("Document contains %d module nodes\n", moduleNodes.size());
//		for (int i = 0; i < moduleNodes.size(); i++) {
//			System.out.println(moduleNodes.get(i));
//			unpackModuleNode(moduleNodes.get(i));
//		}

	}

	/**
	 * Unpack a module node, creating a class and settings
	 * it's data. 
	 */
	private Object unpackModuleNode(ModuleNode moduleNode) {
		if (moduleNode == null) {
			return null;
		}
		// first need to find the configuration node, then unpack that. 
		ArrayList<Node> settingsNodes = findNodesByType(moduleNode.getNode(), "SETTINGS", null);
		for (Node node : settingsNodes) {
			unpackSettingsNode(node);
		}
		return null;
	}
	
	public Object unpackSettingsNode(Node settingsNode) {
		String className = getAttrString(settingsNode.getAttributes(), "Class");
		Class<?> moduleClass = null;
		int errors = 0;
		Object newObject = null;
		try {
			newObject = createObject(className);
			errors = unpackSettingsNode(settingsNode, newObject);
		} catch (SecurityException e) {
			e.printStackTrace();
		} 

		return newObject;
	}
	
	/**
	 * Try to create an object from a class name assuming no parameters. 
	 * @param className
	 * @return
	 */
	Object createObject(String className) {
		Class moduleClass;
		try {
			moduleClass = Class.forName(className);
			Class<?>[] params = new Class<?>[0];
			Constructor<?> constructor = moduleClass.getConstructor(params);
			Object newObject = constructor.newInstance();
			return newObject;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Unpack settings in the XML node into the Java object. 
	 * @param settingsNode XML SETTINGS node
	 * @param dataObj Java parameter object. 
	 * @return true if success, false otherwise. 
	 */
	private int unpackSettingsNode(Node settingsNode, Object dataObj) {
		PamParameterSet parameterSet;
		if (dataObj instanceof ManagedParameters) {
			parameterSet = ((ManagedParameters) dataObj).getParameterSet();
		}
		else {
			int exclusions = Modifier.STATIC | Modifier.FINAL;
			parameterSet = PamParameterSet.autoGenerate(dataObj, exclusions);
		}
		int missing = 0;
		if (parameterSet == null) {
			return -1;
		}
		for (PamParameterData pamParam:parameterSet.getParameterCollection()) {
			String fieldName = pamParam.getFieldName();
			//			fieldNode = findNodesByType(settingsNode, fieldName, null);
			Node fieldNode = findNode(settingsNode, fieldName);
			if (fieldNode == null) {
				missing ++;
				System.out.printf("Unable to find node for field %s\n", fieldName);
				continue;
			}
			String fieldClassName = getAttrString(fieldNode.getAttributes(), "Class");
			Class fieldClass = null;
			try {
				fieldClass = ClassUtils.getClass(fieldClassName);
				//				fieldClass = Class.forName(fieldClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				missing++;
				continue;
			}

			Object obj = readNodeData(dataObj, pamParam, fieldNode, fieldClass);
//			System.out.printf("Node for %s is %s Class %s, Value %s\n", pamParam.getFieldName(), fieldNode, fieldClass, obj);
			try {
				pamParam.setData(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return missing;
	}
	private Object readNodeData(Object dataObj, PamParameterData pamParam, Node fieldNode, Class fieldClass) {

		String stringValue = getAttrString(fieldNode.getAttributes(), "Value");

		boolean ok = false;
		if (fieldClass.isPrimitive()) {
			return readPrimitive(fieldClass, stringValue);
		}
		if (fieldClass == String.class) {
			return stringValue;
		}
		else if (fieldClass.isEnum()) {
			return readEnum(fieldClass, stringValue);
		}
		else if (fieldClass.isArray()) {
			return readArray(dataObj, pamParam, fieldNode, fieldClass);
		}
		else if (List.class.isAssignableFrom(fieldClass)) {
			System.out.println("Read list not yet implemented in PamguardXMLReader");
		}
		else if (Map.class.isAssignableFrom(fieldClass)) {
			System.out.println("Read map not yet implemented in PamguardXMLReader");
		}
		else if (File.class.isAssignableFrom(fieldClass)) {
			File file = new File(stringValue);
			return file;
		}
		else if (stringValue == null) {
			return readObject(dataObj, pamParam, fieldNode, fieldClass);
		}


		return null;
	}

	private Object readArray(Object dataObj, PamParameterData pamParam, Node fieldNode, Class fieldClass) {
		// TODO Auto-generated method stub
		String className = getAttrString(fieldNode.getAttributes(), "Class");
		Class arrayClass = null;
		try {
			arrayClass = getArrayTypeClass(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
//		System.out.printf("Class for array %s is %s\n", className, arrayClass);
		String value = getAttrString(fieldNode.getAttributes(), "Value");
		if (value == null) {
			return null;
		}
		String[] bits = value.split(",");
		switch (arrayClass.getName()) {
		case "int":
		  return readIntArray(bits);
		case "double":
			return readDoubleArray(bits);
		case "boolean":
			return readBooleanArray(bits);
		case "java.lang.String":
		return bits;
		}

		return null;
	}
	
	private double[] readDoubleArray(String[] bits) {
		double data[] = new double[bits.length];
		try {
		for (int i = 0; i < bits.length; i++) {
			data[i] = Double.valueOf(bits[i]);
		}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}

	private boolean[] readBooleanArray(String[] bits) {
		boolean data[] = new boolean[bits.length];
		try {
		for (int i = 0; i < bits.length; i++) {
			data[i] = Boolean.valueOf(bits[i]);
		}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}

	private int[] readIntArray(String[] bits) {
		int data[] = new int[bits.length];
		try {
		for (int i = 0; i < bits.length; i++) {
			data[i] = Integer.valueOf(bits[i]);
		}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return data;
	}

	/**
	 * Get a class type for an array. 
	 * @param arrayClass something like [D, [ something, etc. 
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private Class getArrayTypeClass(String arrayClass) throws ClassNotFoundException {
		if (arrayClass == null) {
			return null;
		}
		if (arrayClass.charAt(0) != '[') {
			return null;
		}
		String type = arrayClass.substring(1,2);
		switch (type) {
		case "D":
			return ClassUtils.getClass("double");
		case "I":
			return ClassUtils.getClass("int");
		case "Z":
			return ClassUtils.getClass("boolean");
		case "L":
			String subString = type.substring(1, type.length()-2);
			return ClassUtils.getClass(subString);
		}
		return null;
	}

	private Object readObject(Object dataObj, PamParameterData pamParam, Node fieldNode, Class fieldClass) {
		/*
		 *  we're trying to read an object in dataObj; 
		 *  Hopefully, an instance already exists, OR we can construct one from nothing. 
		 */
		Object subObj = null;
		
		try {
			subObj = pamParam.getData();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (subObj != null) {
			// unpack into existing sub object. 
			unpackSettingsNode(fieldNode, subObj);
		}
		else {
			// create new sub object.
			subObj = unpackSettingsNode(fieldNode);
		}

		return subObj;
	}

	//	private boolean setEnum(Object dataObj, Class fieldClass, PamParameterData pamParam, String stringValue) {
	//		Enum val = readEnum(dataObj, fieldClass, pamParam, stringValue);
	//		try {
	//			pamParam.setData(val);
	//			return true;
	//		} catch (IllegalArgumentException | IllegalAccessException e) {
	//			e.printStackTrace();
	//		}
	//		return false;
	//	}
	//	
	private Enum readEnum(Class fieldClass, String stringValue) {
		//		Enum extEnum = null;
		try {
			// the enum will either be in it's raw form, or one of the toString functions, which 
			// makes life more complicated ! So need to scan all options to see what the value is. 
			Method method = fieldClass.getMethod("values", null);
			Object ans = method.invoke(null);
			Enum[] list = (Enum[]) ans;
			for (int i = 0; i < list.length; i++) {
				if (list[i].toString().equals(stringValue)) {
					return list[i];
				}
			}

			return null;
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	//	private boolean setPrimitive(Object dataObj, Class fieldClass, PamParameterData pamParam, String value) {
	//		Object val = readPrimitive(dataObj, fieldClass, pamParam, value);
	//		if (val == null) {
	//			return false;
	//		}
	//		try {
	//			pamParam.setData(val);
	//		} catch (IllegalArgumentException | IllegalAccessException e) {
	//			e.printStackTrace();
	//			return false;
	//		}
	//		return true;
	//	}

	private Object readPrimitive(Class fieldClass, String value) {
		if (fieldClass == String.class) {
			return value;
		}
		switch (fieldClass.getName()) {
		case "byte":
			return Byte.valueOf(value);
		case "short":
			return Short.valueOf(value);
		case "int":
			return Integer.valueOf(value);
		case "long":
			return Long.valueOf(value);
		case "float":
			return Float.valueOf(value);
		case "double":
			return Double.valueOf(value);
		case "boolean":
			return Boolean.valueOf(value);
		case "char":
			if (value.length() >= 1) {
				return value.charAt(0);
			}
			else {
				return null;
			}
		}

		System.out.println("Unknown primitive class " + fieldClass.getName());
		return null;
	}
	

//	public Object getModuleForClass(String className) {
//		ArrayList<Node> modules = findSettingsForClass(className);
//		if (modules == null || modules.size() == 0) {
//			WarnOnce.showWarning("XML Import Error", "file contains no modules for class " + className, WarnOnce.WARNING_MESSAGE);
//			return null;
//		}
//		if (modules.size() > 1) {
//			
//		}
//		// for now just take the first. In future, make a list and ask which one user wants. 
//		return unpackSettingsNode(modules.get(0));
//		
//	}

	/**
	 * Find all module nodes which are for a particular parameter class. 
	 * @param className
	 * @return module nodes
	 */
	public ArrayList<ModuleNode> modulesForClass(String className) {
		if (moduleNodes == null || className == null) {
			return null;
		}
		ArrayList<ModuleNode> classNodes = new ArrayList<ModuleNode>();
		for (ModuleNode aNode : moduleNodes) {
			if (className.equals(aNode.getjClass())) {
				classNodes.add(aNode);
			}
		}
		return classNodes;
	}

	public ArrayList<Node> findSettingsForClass(String className) {
		ArrayList<Node> allSettings = new ArrayList<>();
		if (moduleNodes == null) {
			return allSettings;
		}
		for (int i = 0; i < moduleNodes.size(); i++) {
//			System.out.println(moduleNodes.get(i));
			ArrayList<Node> settingsNodes = findNodesByType(moduleNodes.get(i).getNode(), "SETTINGS", null);
			if (settingsNodes == null) {
				continue;
			}
			for (int n = 0; n < settingsNodes.size(); n++) {
				String classStr = getAttrString(settingsNodes.get(n).getAttributes(), "Class");
				if (classStr == null) {
					continue;
				}
				if (className.equals(classStr)) {
					allSettings.add(settingsNodes.get(n));
				}
			}
		}
		return allSettings;
	}

	/**
	 * Find all module nodes which are for a particular module type. 
	 * @param unitType PAMGuard unit type
	 * @return module nodes
	 */
	public ArrayList<ModuleNode> modulesForType(String unitType) {
		if (moduleNodes == null || unitType == null) {
			return null;
		}
		ArrayList<ModuleNode> classNodes = new ArrayList<ModuleNode>();
		for (ModuleNode aNode : moduleNodes) {
			if (unitType.equals(aNode.getUnitType())) {
				classNodes.add(aNode);
			}
		}
		return classNodes;
	}

	/**
	 * iterate recursively through all nodes in an xml document node 
	 * @param childNodes
	 * @param moduleNodes
	 */
	private void findModuleNodes(NodeList childNodes, ArrayList<ModuleNode> moduleNodes) {
		if (childNodes == null) {
			return;
		}
		int nChild = childNodes.getLength();
		for (int i = 0; i < nChild; i++) {
			Node node = childNodes.item(i);
			String nodeName = node.getNodeName();
			if (nodeName.equals("MODULE")) {
				//				System.out.println(node + " : " + node.getNodeName());
				NamedNodeMap attributes = node.getAttributes();
				String jClass = getAttrString(attributes, "Java.class");
				String unitType = getAttrString(attributes, "UnitType");
				String unitName = getAttrString(attributes, "UnitName");
				//				System.out.printf("Module %s, %s, %s\n", jClass, unitName, unitType);
				moduleNodes.add(new ModuleNode(node, jClass, unitType, unitName));
			}
			findModuleNodes(node.getChildNodes(), moduleNodes);
		}
	}

	/**
	 * Get a list of all nodes in a tree with the given name. 
	 * @param parent
	 * @param nodeName
	 * @param nodeList
	 * @return
	 */
	private ArrayList<Node> findNodesByType(Node parent, String nodeName, ArrayList<Node> nodeList) {
		if (nodeList == null) {
			nodeList = new ArrayList<>();
		}
		String name = parent.getNodeName();
		if (name.equals(nodeName)) {
			nodeList.add(parent);
		}
		NodeList children = parent.getChildNodes();
		int nCh = children.getLength();
		for (int i = 0; i < nCh; i++) {
			nodeList = findNodesByType(children.item(i), nodeName, nodeList);
		}
		return nodeList;
	}

	/**
	 * find a note as a child of the current node
	 * @param parent
	 * @param nodeName
	 * @return
	 */
	private Node findNode(Node parent, String nodeName) {
		NodeList children = parent.getChildNodes();
		int nCh = children.getLength();
		for (int i = 0; i < nCh; i++) {
			Node aNode = children.item(i);
			if (aNode.getNodeName().equals(nodeName)) {
				return aNode;
			}
		}
		return null;
	}

	public String getAttrString(NamedNodeMap attrMap, String attName) {
		Node node = attrMap.getNamedItem(attName);
		if (node == null) {
			return null;
		}
		return node.getNodeValue();
	}


}
