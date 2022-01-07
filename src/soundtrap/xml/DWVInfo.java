package soundtrap.xml;

import org.w3c.dom.Node;

/**
 * Information about dwv files extracted from 
 * Sound TRap xml log files. 
 * @author Doug
 *
 */
public class DWVInfo extends CFGInfo {

	public Integer dwvBlockLen;

	public DWVInfo(Node cfgNode) {
		super(cfgNode);
	}
}
