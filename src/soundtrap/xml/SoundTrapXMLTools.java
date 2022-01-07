package soundtrap.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SoundTrapXMLTools {
	/**
	 * Find the source sample rate for a node. <p>
	 * This function iterates back through the source list until 
	 * if finds something with a sample rate or returns null. 
	 * @param node starting node. 
	 * @return sample rate or null
	 */
	public static Integer findSourceSampleRate(NodeList cfgNodes, Node node) {
		// if this node has a sample rate,m we're good, otherwise
		// try to get back to a parent node, if that fails, give up. 
		Integer fs = findIntegerChildValue(node, "FS");
		if (fs != null) {
			return fs;
		}
		Integer src = findIntegerChildAttribute(node, "SRC", "ID");
		if (src == null) {
			return null;
		}
		Node srcNode = findNodeById(cfgNodes, src);
		if (srcNode == null) {
			return null;
		}
		return findSourceSampleRate(cfgNodes, srcNode);
	}

	/**
	 * Get the string value of a child node
	 * @param node parent node 
	 * @param childName child name
	 * @return String value or null
	 */
	public static String findChildValue(Node node, String childName) {
		Node child = findChildNode(node, childName);
		if (child == null) {
			return null;
		}
		String txt = child.getTextContent();
		if (txt == null) {
			return null;
		}
		return txt.trim();
	}

	/**
	 * Get the Integer value of a child node
	 * @param node parent node 
	 * @param childName child name
	 * @return Integer value or null
	 */
	public static Integer findIntegerChildValue(Node node, String childName) {
		String strVal = findChildValue(node, childName);
		if (strVal == null) {
			return null;
		}
		try {
			return Integer.valueOf(strVal);
		}
		catch (NumberFormatException e) {
			System.out.printf("Can't extract Int value from node %s: %s\n", childName, e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * Get an Integer value for the given attribute of a named child node
	 * @param node parent node
	 * @param childName child node name
	 * @param attribute attribute name
	 * @return Integer value or null
	 */
	public static Integer findIntegerChildAttribute(Node node, String childName, String attribute) {
		String att = findChildAttribute(node, childName, attribute);
		if (att == null) {
			return null;
		}
		try {
			return Integer.valueOf(att);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
	/**
	 * Get an String value for the given attribute of a named child node
	 * @param node parent node
	 * @param childName child node name
	 * @param attribute attribute name
	 * @return String value or null
	 */
	public static String findChildAttribute(Node node, String childName, String attribute) {
		Node child = findChildNode(node, childName);
		if (child == null) {
			return null;
		}
		return findAttribute(child, attribute);
	}


	/**
	 * Get a named attribute from a node. 
	 * @param node parent node
	 * @param attribute attribute name
	 * @return Integer value or null
	 */
	public static Integer findIntegerAttribute(Node node, String attribute) {
		String txt = findAttribute(node, attribute);
		if (txt == null) {
			return null;
		}
		try {
			return Integer.valueOf(txt);
		}
		catch (Exception e) {
			return null;
		}
	}
	/**
	 * Get a named attribute from a node. 
	 * @param node parent node
	 * @param attribute attribute name
	 * @return String value or null
	 */
	public static String findAttribute(Node node, String attribute) {
		NamedNodeMap atts = node.getAttributes();
		if (atts == null) {
			return null;
		}
		Node att = atts.getNamedItem(attribute);
		if (att == null) {
			return null;
		}
		String txt = att.getTextContent();
		if (txt == null) {
			return null;
		}
		return txt.trim();
	}

	/**
	 * Find a child node by name
	 * @param node parent node
	 * @param childName child node name
	 * @return Child node or null
	 */
	public static Node findChildNode(Node node, String childName) {
		NodeList childNodes = node.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node childNode = childNodes.item(j);
			if (childNode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String str = childNode.getNodeName();
			if (str != null && str.equals(childName)) {
				return childNode;
			}
		}
		return null;
	}

	/**
	 * Find a node by it's ID
	 * @param nodes node list
	 * @param id id to search for
	 * @return found node or null. 
	 */
	public static Node findNodeById(NodeList nodes, int id) {
		return findNodeById(nodes, Integer.toString(id));
	}

	/**
	 * Find a node by it's ID
	 * @param nodes node list
	 * @param id id to search for
	 * @return found node or null. 
	 */
	public static Node findNodeById(NodeList nodes, String id) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NamedNodeMap atts = node.getAttributes();
			Node idAtt = atts.getNamedItem("ID");
			if (idAtt != null && idAtt.getTextContent().equals(id)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Find a node with a given PROC name. 
	 * @param nodes node list
	 * @param procName PROC name
	 * @return a node or null. 
	 */
	public static Node findNodeByProc(NodeList nodes, String procName) {
		return findNodeWithChildValue(nodes, "PROC", procName);
	}

	/**
	 * Find a node which has a child with a given name with that 
	 * child having a given value
	 * @param nodes node list
	 * @param childName child name
	 * @param childValue child value. 
	 * @return found node or null
	 */
	public static Node findNodeWithChildValue(NodeList nodes, String childName, String childValue) {		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			NodeList childNodes = node.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node childNode = childNodes.item(j);
				if (childNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				String str = childNode.getNodeName();
				if (str != null && str.equals(childName)) {
					String childText = childNode.getTextContent();
					if (childText != null) {
						childText = childText.trim();
					}
					if (childValue.equals(childText)) {
						return node;
					}
				}
			}
		}
		return null;
	}
}
