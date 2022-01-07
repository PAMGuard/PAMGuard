package clickDetector;

import Layout.PamFramePlots;

/**
 * Common functionality for all display windows in the click 
 * detector panel
 * @author Doug Gillespie
 *
 */
public abstract class ClickDisplay extends PamFramePlots {
	

	ClickControl clickControl;
	
	ClickDisplayManager.ClickDisplayInfo clickDisplayInfo;
	ClickDisplayManager clickDisplayManager;
	

//	private Frame plotFrame;

	public ClickDisplay(ClickControl clickControl, ClickDisplayManager clickDisplayManager, ClickDisplayManager.ClickDisplayInfo clickDisplayInfo) {
		this.clickControl = clickControl;
		this.clickDisplayInfo = clickDisplayInfo;
		this.clickDisplayManager = clickDisplayManager;
		
	
	}

	public ClickDisplayManager.ClickDisplayInfo getClickDisplayInfo() {
		return clickDisplayInfo;
	}
	
	abstract public void noteNewSettings(); 
	
	public void pamStart() {
		
	}

    /**
     * PamStop method - called by ClickControl
     */
    public void pamStop() {}

	/*
	 * called after the display has been created, the frame is available, etc. 
	 */
	public void created() {
		
	}
	public void clickedOnClick(ClickDetection click) {
		
	}

	/** 
	 * Called from clicksOffline when data have changed (eg from re doing click id). 
	 * Needs to notify the display and maybe some other classes. 
	 */
	public void offlineDataChanged() {
		// TODO Auto-generated method stub
	}
}
