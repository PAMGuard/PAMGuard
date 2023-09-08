package PamView;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.SwingUtilities;

import com.sun.glass.ui.Screen;

import PamController.PamGUIManager;
import javafx.scene.layout.Pane;

/**
 * Class to launch a thread which will get the screen size 
 * (including the bounds of multiple monitors)
 * <p>
 * The process of getting the dimensions is launched in a 
 * different thread and can be going on while other PAMGAURD
 * startup processes are initialising.
 *  
 * @author Doug
 *
 */
public class ScreenSize {

	private volatile static Rectangle screenDimension = null;
	
	private static int nScreens;
	
	private static Rectangle[] screenDimensions;
	
	/**
	 * Gets the screen bounds, sum  / union of all screens. 
	 * @return Virtual screen size, or null. 
	 */
	public static synchronized Rectangle getScreenBounds() {
		if (screenDimension == null) {
			screenDimension = findScreenBounds();
		}
		return screenDimension;
	}
	
	/**
	 * Get the number of screens
	 * @return the number of screens
	 */
	public static synchronized int getNumScreens() {
		if (screenDimension == null) {
			screenDimension = findScreenBounds();
		}
		return nScreens;
	}
	
	/**
	 * Get the dimensions for a specified screen. 
	 * @param iScreen Screen number
	 * @return Rectangle dimensions
	 */
	public Rectangle getScreenBounds(int iScreen) {
		if (screenDimension == null) {
			screenDimension = findScreenBounds();
		}
		if (iScreen < 0 || iScreen >= nScreens) {
			return null;
		}
		return screenDimensions[iScreen];
	}
	
	private static Rectangle findScreenBounds() {
		Rectangle virtualBounds = new Rectangle();
		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
			virtualBounds = new Rectangle(0,0,1024,768);
			nScreens = 1;
			screenDimensions = new Rectangle[nScreens];
			screenDimensions[0] = virtualBounds;
		}
		else {

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs =ge.getScreenDevices();
			nScreens = gs.length;
			screenDimensions = new Rectangle[nScreens];
			for (int j = 0; j < gs.length; j++) { 
				GraphicsDevice gd = gs[j];
				//Think to get screen sizes on multiple monitors
				//can just use getDefaultConfiguration for
				//each device rather than looping over 
				//all configurations. Hopefully should 
				//let PAMGUARD start up a little quicker
				//CJB 2009-06-15
				GraphicsConfiguration dgc = gd.getDefaultConfiguration();
				screenDimensions[j] = dgc.getBounds();
				virtualBounds = virtualBounds.union(dgc.getBounds());

			} 
		}
		
		return virtualBounds;
	}
	
	
	/**
	 * Pushes a rectangle so that it's visible on the screen. 
	 * @param bounds - will get modified in place. 
	 * @return true if the position was changed. 
	 */
	public static boolean forceBoundToScreen(Rectangle bounds) {
		try {
			if (isPointOnScreen(bounds.getLocation()) == false) {
				bounds.x = bounds.y = 0;
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * Test to see if the point is within the bounds of any available monitor
	 * @param point point
	 * @return true if it's within the bounds of any montor
	 * @throws Exception Thrown if there are no monitors. 
	 */
	public static boolean isPointOnScreen(Point point) throws Exception {
		if (screenDimensions == null || screenDimensions.length == 0) {
			throw new Exception("No attached screens. Can't test point");
		}
		for (int i = 0; i < screenDimensions.length; i++) {
			if (screenDimensions[i].contains(point)) {
				return true;
			}
		}
		return false;
	}


}
