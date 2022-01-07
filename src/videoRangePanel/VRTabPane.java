package videoRangePanel;

import java.awt.Point;
import java.io.File;

import PamView.PamTabPanel;
import videoRangePanel.layoutFX.VRTabPanelControlFX;

public abstract class VRTabPane implements PamTabPanel {
	
	/**
	 * Get the main VRPane whihc makes the display. 
	 * @return the vro pane. 
	 */
	public abstract VRPane getVRPane();

	/**
	 * Called whenever an update occurs. 
	 * @param updateType - the update type flag. 
	 */
	public void update(int updateType) {
		
	}

	/**
	 * Paste an image file 
	 * @return paste an image file. 
	 */
	public boolean pasteImage() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Open a single image file (could also be a vide)
	 * @param file - the fiel to open
	 * @return true if the file is opended successfully
	 */
	public boolean openImageFile(File file) {
		// TODO Auto-generated method stub
		return false;
	}


	public void newMousePoint(Point mousePoint) {
		// TODO Auto-generated method stub
	}
	
}
