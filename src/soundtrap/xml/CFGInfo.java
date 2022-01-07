package soundtrap.xml;

import org.w3c.dom.Node;

public class CFGInfo {

	private Node cfgNode;
	
	private TimeInformation timeInfo;
	
	public CFGInfo(Node cfgNode) {
		super();
		this.cfgNode = cfgNode;
		id = SoundTrapXMLTools.findIntegerAttribute(cfgNode, "ID");
		src = SoundTrapXMLTools.findIntegerChildAttribute(cfgNode, "SRC", "ID");
		fType = SoundTrapXMLTools.findAttribute(cfgNode, "FTYPE");
	}
	
	public Integer id;
	public Integer src;
	public String fType;
	public Integer fs;

	/**
	 * @return the timeInfo
	 */
	public TimeInformation getTimeInfo() {
		return timeInfo;
	}
	/**
	 * @param timeInfo the timeInfo to set
	 */
	public void setTimeInfo(TimeInformation timeInfo) {
		this.timeInfo = timeInfo;
	}
	/**
	 * @return the cfgNode
	 */
	public Node getCfgNode() {
		return cfgNode;
	}
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @return the src
	 */
	public Integer getSrc() {
		return src;
	}
	/**
	 * @return the fType
	 */
	public String getfType() {
		return fType;
	}
	/**
	 * @return the fs
	 */
	public Integer getFs() {
		return fs;
	}

}
