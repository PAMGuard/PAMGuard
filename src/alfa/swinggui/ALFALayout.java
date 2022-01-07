package alfa.swinggui;

import javax.swing.JComponent;

/**
 * Layout options for ALFA data display.
 * Made interface so can play with a few different ideas. 
 * @author Doug
 *
 */
public interface ALFALayout {

	/**
	 * 
	 * @return component to slot into CENTER of main display area. 
	 */
	public JComponent getComponent();

	public void setOptionsComponent(JComponent optionsComponent);
	
	public void setWestStatusComponent(JComponent statusComponent);
	
	public void setMapComponent(JComponent mapComponent);
	
	public void setSpermSummaryComponents(JComponent spermSummary);
	
	public void setCommsComponent(JComponent commsComponent);
	
	public void setNorthStatusComponents(JComponent northComponent);

	public void setHelpcomponent(JComponent helpComponent);
}
