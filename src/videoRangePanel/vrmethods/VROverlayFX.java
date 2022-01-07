package videoRangePanel.vrmethods;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public interface VROverlayFX {
	
	/******JavaFX GUI********/
	
	/**
	 * Some methods may require a panel above the picture for manual input or to view current information. getRibbonPanel() 
	 * returns the JavaFX pane unique to this method which may go above the picture. May return null if no panel is needed. 
	 * @return
	 */
	public Pane getRibbonPaneFX();
	
	
	/**
	 * Different video range methods will have different controls in the side panel. This returns the JavaFX side pane unique to the method.
	 *  May return null if no panel is needed. 
	 * @return
	 */
	public Pane getSidePaneFX();
	
	/**
	 * Different video range methods will have different settings. This JavaFX pane provide the vr specific settings in the vr settings dialog panel. 
	 * @return
	 */
	public Pane getSettingsPaneFX();
	
	/**
	 * Called from other parts of the module whenever a method panel may needed updated e.g. when new calibration data is manually added in the settings dialog. 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType);
	
	
	/**
	 * Paint graphics onto the image. 
	 * @param g - the graphics context handle. 
	 */
	public void paint(GraphicsContext g);
	
	
	/**
	 * Called whenever there is a mouse action on the overlay draw cnvas
	 * @param e - the mouse event. 
	 * @param motion - true if the mouse event is just a motion. ie. movement of mouse on screen but no clicking. 
	 * @param. The draw 
	 */
	public void mouseAction(MouseEvent e, boolean motion, Canvas drawCanvas);
	


}
