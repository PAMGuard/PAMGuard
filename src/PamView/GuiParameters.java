package PamView;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.Serializable;

public class GuiParameters implements Serializable, Cloneable {

	static final long serialVersionUID = 1;
	
	/**
	 * The currently select tab
	 */
	int selectedTab = 0;
	
	/**
	 * The  dimension size for tabs. If null uses defualt swing dimensions
	 */
	public Dimension tabSize =  null;
	
	boolean isZoomed = true;
	
	int state, extendedState;
	
	Dimension size;
	
	Rectangle bounds;
	
	boolean hideSidePanel;
	
	private String currentSelectedTab;
	

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected GuiParameters clone() {
		try {
			return (GuiParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * @return the currentSelectedTab
	 */
	public String getCurrentSelectedTab() {
		return currentSelectedTab;
	}


	/**
	 * @param currentSelectedTab the currentSelectedTab to set
	 */
	public void setCurrentSelectedTab(String currentSelectedTab) {
		this.currentSelectedTab = currentSelectedTab;
	}
		
}
