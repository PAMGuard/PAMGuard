package PamController.settings.output.xml;

import org.w3c.dom.Node;

/**
 * information on a module node read from a settings xml file. 
 * @author dg50
 *
 */
public class ModuleNode {

	private Node node;
	private String jClass;
	private String unitType;
	private String unitName;

	public ModuleNode(Node node, String jClass, String unitType, String unitName) {
		this.node = node;
		this.jClass = jClass;
		this.unitType = unitType;
		this.unitName = unitName;
	}

	/**
	 * @return the node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * @return the jClass
	 */
	public String getjClass() {
		return jClass;
	}

	/**
	 * @return the unitType
	 */
	public String getUnitType() {
		return unitType;
	}

	/**
	 * @return the unitName
	 */
	public String getUnitName() {
		return unitName;
	}

	@Override
	public String toString() {
		return String.format("Java class: %s; Module Type: %s; Module Name; %s", jClass, unitType, unitName);
	}

	
}
