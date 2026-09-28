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
	 * The name of the data model tab. This tab is always recreated on startup and
	 * so is treated specially when tab information is saved and restored.
	 */
	public static final String DATA_MODEL_TAB_NAME = "Data Model";

	/**
	 * The name shown on the tab.
	 */
	public String tabName = null;

}
