package d3.calibration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import d3.D3XMLFile;

public class CalFileReader {

	private Document doc;
	private File xmlFile;

	private CalFileReader(Document doc, File xmlFile) {
		this.doc = doc;
		this.xmlFile = xmlFile;
		
	}/**
	 * Open an XML file for a corresponding wav file 
	 * by changing the file end to xml and then trying to
	 * find an xml file in the same directory. 
	 * @param file wav file (or valid xml file) 
	 * @return an XML file, from which additional information can then be extracted. 
	 */
	public static CalFileReader openCalFile(File xmlFile) {
		if (xmlFile.exists() == false) {
			return null;
		}
		/*
		 * Try reading the document.
		 */
		Document doc;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(xmlFile);
		} catch (ParserConfigurationException e) {
			System.out.println(String.format("Parser Error in XML file %s: %s", xmlFile, e.getMessage()));
			return null;
		} catch (SAXException e) {
			System.out.println(String.format("SAX Error in XML file %s: %s", xmlFile, e.getMessage()));
			return null;
		} catch (IOException e) {
			System.out.println(String.format("IO Error in XML file %s: %s", xmlFile, e.getMessage()));
			return null;
		}
		doc.getDocumentElement().normalize();
	
		return new CalFileReader(doc, xmlFile);
	}
	
	/**
	 * Read in all the calibration information and pack it into a nested list
	 * of CalibrationInfo objects. 
	 */
	public CalibrationSet readEverything() {
		/**
		 * Start with the basic information
		 */
		String id = readRootString("ID");
		String name = readRootString("NAME");
		String builder = readRootString("BUILDER");
		CalibrationSet calSet = new CalibrationSet(id, name, builder);
		
		/*
		 * Find the CAL node and start there. 
		 */
		NodeList calList = doc.getElementsByTagName("CAL");
		if (calList.getLength() == 0) {
			return calSet;
		}		
		readStructures(calList.item(0), calSet.calibrationInfo);
		
		return calSet;
	}
	
	/**
	 * read structures from a node in the xml file. 
	 * This will get called recursively as it works through
	 * the nested list. 
	 * @param item
	 * @param calibrationInfo
	 */
	private void readStructures(Node calNode, CalibrationInfo rootInfo) {
//		Node calNode = item.getFirstChild();
//		while (calNode != null) {
//			String nodeName = calNode.getNodeName();
//			if (nodeName.equals("#text")) {
//				calNode = calNode.getNextSibling();
//				continue;
//			}
////			String childClass = childNode.get
//			System.out.println(nodeName);
//			NamedNodeMap namedNodes = calNode.getAttributes();
//			Node aNode = namedNodes.getNamedItem("class");
//			if (aNode.getNodeValue().equals("struct") == false) {
//				calNode = calNode.getNextSibling();
//				continue;
//			}
//			/*
//			 * At this point, make a new info point
//			 */
//			CalibrationInfo calibrationInfo = new CalibrationInfo(nodeName);
//			rootInfo.subInfos.add(calibrationInfo);
			/*
			 *  we should be in a structure here which will have various
			 *  things we want to read (such as poly, use, etc. )
			 *  read away at them, but if we find anything that is a 
			 *  struct, then call recursively into that node.  
			 */
			NodeList children = calNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node aChild = children.item(i);
				String childName = aChild.getNodeName();
				if (childName.equals("#text")) continue;
//				System.out.println(childName);
				String childClass = findAttribute(aChild, "class");
				if (childClass == null) continue;
				if (childClass.equals("struct")) {
					readStructures(aChild, rootInfo.getNewChild(childName));
				}
				else switch (childName.toUpperCase()) {
				case "USE":
					rootInfo.use = readElementString(aChild);
					break;
				case "UNIT":
					rootInfo.unit = readElementString(aChild);
					break;
				case "METHOD":
					rootInfo.method = readElementString(aChild);
					break;
				case "POLY":
					rootInfo.poly = readPolynomial(aChild);
					break;
				case "TREF":
					rootInfo.tref = readDouble(aChild);
					break;
				case "TYPE":
					rootInfo.type = readElementString(aChild);
					break;
				case "SRC":
					rootInfo.src = readElementString(aChild);
					break;
				default:
					System.out.println("Unknown Element in " + calNode.getNodeName() + " - " + childName);
				}
				
				
			}
			
//			calNode = calNode.getNextSibling();
//		}
		
	}
	
	private Double readDouble(Node aChild) {
		String data = readElementString(aChild);
		if (data == null) {
			return null;
		}
		try {
			return new Double(data.trim());
		}
		catch (NumberFormatException e) {
			System.err.println("Error reading double value from calibration file node " + aChild.getNodeName());
			return null;
		}
	}
	
	private double[][] readPolynomial(Node aChild) {

		int[] size = readElementSize(aChild);
		if (size == null) {
			return null;
		}		
		
		String data = readElementString(aChild);
		if (data == null) {
			return null;
		}
		
		String[] bits = data.split(" ");
		if (size[0]*size[1] != bits.length) {
			return null;
		}
		double[][] poly = new double[size[0]][size[1]];
		int ind = 0;
		try {
			for (int j = 0; j < size[1]; j++) {
				for (int i = 0; i < size[0]; i++) {
				  poly[i][j] = Double.valueOf(bits[ind++].trim());
				}
			}
		}
		catch (NumberFormatException e) {
			System.err.println("Error reading polynomial from calibration file node " + aChild.getNodeName());
		}
		return poly;
	}
	/**
	 * Read character data from an element
	 * @param aChild
	 * @return
	 */
	private String readElementString(Node aChild) {
		if (aChild.getNodeType() == Node.ELEMENT_NODE) {
			String str = ((Element) aChild).getTextContent();
			str = str.replace("#32;", " ");
			str = str.replace("#95;", "_");
			str = str.replace("#45;", "-");
			return str;
		}
		else {
			return null;
		}
	}
	
	private int[] readElementSize(Node aChild) {
		String szStr = findAttribute(aChild, "size");
		if (szStr == null) {
			return null;
		}
		String[] bits = szStr.split(" ");
		if (bits.length != 2) {
			return null;
		}
		int[] size = new int[2];
		try {
			for (int i = 0; i < 2; i++) {
				size[i] = Integer.valueOf(bits[i].trim());
			}
		}
		catch (NumberFormatException e) {
			return null;
		}
		return size;
	}
	
	private String findAttribute(Node aChild, String string) {
		NamedNodeMap namedNodes = aChild.getAttributes();
		if (namedNodes == null) {
			return null;
		}
		Node aNode = namedNodes.getNamedItem(string);
		if (aNode == null) {
			return null;
		}
		return aNode.getNodeValue();
//		if (aNode.getNodeValue().equals("struct") == false) {
//			calNode = calNode.getNextSibling();
//			continue;
//		}
	}
	private String readRootString(String id) {
		NodeList cfgList = doc.getElementsByTagName(id);
		if (cfgList.getLength() == 0) {
			return null;
		}
		Element el = (Element) cfgList.item(0);
		String fullId = el.getTextContent();
		if (fullId == null) {
			return null;
		}
		return fullId.trim();
	}
}
