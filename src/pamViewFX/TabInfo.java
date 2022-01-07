package pamViewFX;

import java.io.Serializable;

/**
 * Settings information for a PamGuiTabFX. 
 *  
 * @author Jamie Macaulay
 *
 */
public class TabInfo implements Serializable {
	
	public TabInfo(String string) {
		this.tabName=string; 
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The name shown on the tab. 
	 */
	public String tabName = null;

}
