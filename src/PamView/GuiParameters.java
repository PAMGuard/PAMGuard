package PamView;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.Serializable;

public class GuiParameters implements Serializable, Cloneable {

	static final long serialVersionUID = 2L;
	
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
	
	/**
	 * Flag to hide all tool tips - which are 
	 * very annoying when they cover controls you want to use!
	 */
	private boolean hideAllToolTips = false;
	

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


	/**
	 * @return the hideAllToolTips
	 */
	public boolean isHideAllToolTips() {
		return hideAllToolTips;
	}


	/**
	 * @param hideAllToolTips the hideAllToolTips to set
	 */
	public void setHideAllToolTips(boolean hideAllToolTips) {
		this.hideAllToolTips = hideAllToolTips;
	}
		
}
