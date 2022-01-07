package videoRangePanel.vrmethods;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import PamView.panel.PamPanel;

public interface VROverlayAWT {
	
	
	/******Swing GUI********/

	/**
	 * Some methods may require a panel above the picture for manual input or to view current information. getRibbonPanel() returns 
	 * the pane unique to this method which may go above the picture. May return null if no panel is needed. 
	 * @return
	 */
	public PamPanel getRibbonPanel();
	
	
	/**
	 * Different video range methods will have different controls in the side panel. This returns the Swing side panel unique to the method.
	 * May return null if no panel is needed. 
	 * @return
	 */
	public PamPanel getSidePanel();

	
	/**
	 * Different video range methods will have different settings. This Swing panel provide the vr specific settings in the vr settings dialog panel. 
	 * @return
	 */
	public PamPanel getSettingsPanel();
	
	/**
	 * Paint the marks on the image. This is used for both Swing and JavaFX GUI's
	 * @param g - the graphics handle 
	 * @return
	 */
	public void paintMarks(Graphics g);
	
	/**
	 * Called from other parts of the module whenever a method panel may needed updated e.g. when new calibration data is manually added in the settings dialog. 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType);
	
	/**
	 * What to do when there is a mouse event. 
	 * @param e - the mouse event. 
	 * @param motion - true if the mouse event is just a motion. ie. movement of mouse on screen but no clicking. 
	 */
	public void mouseAction(MouseEvent e, boolean motion);
	
	
	
	

}
