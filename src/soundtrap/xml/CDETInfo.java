package soundtrap.xml;

import org.w3c.dom.Node;

/**
 * Information on the Sound Trap CDET detector configuration
 * @author Doug
 *
 */
public class CDETInfo extends CFGInfo {

	public CDETInfo(Node cfgNode) {
		super(cfgNode);		// TODO Auto-generated constructor stub
	}
	public Integer cdetThresh;
	public Integer cdetBlanking;
	public Integer cdetPredet;
	public Integer cdetPostDet;
	public Integer cdetSRC;
	public Integer cdetLEN;

}
