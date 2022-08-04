package PamView;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.SwingUtilities;

import com.sun.glass.ui.Screen;

import PamController.PamGUIManager;
import javafx.scene.layout.Pane;

/**
 * Class to launch a thread which will get the screen size 
 * (inlcuding the bounds of multiple monitors)
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
	
//	private volatile static boolean hasRun = false;
//
//	private ScreenSize() {
//		//Problems with startup on Mac that 
//		//seem to have been caused by the 
//		//GetBounds not being invoked from within
//		//the Event Display Thread
//		//CJB 2009-06-15
////		SwingUtilities.invokeLater(new GetBounds());
//		//old version 
//		//Thread thread = new Thread(new GetBounds());
//		//thread.start();
//		long m1 = System.currentTimeMillis();
//		findScreenBounds();
//		long m2 = System.currentTimeMillis();
//		System.out.println("screen bounds took ms: " + (m2-m1));
//	}
//	
//	/**
//	 * Only needs to be called once to 
//	 * start the background process which gets
//	 * the virtual screen size. 
//	 * <p>
//	 * Is called from PAMGUARD Main to get it
//	 * going asap. 
//	 */
//	public static void startScreenSizeProcess() {
//		if (hasRun == false) {
//			hasRun = true;
//			new ScreenSize();
//		}
//		
//	}
//	
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
	
//	/**
//	 * Gets the virtual screen size. Will wait for them to be
//	 * extracted from the background thread up to some set timeout
//	 * @param maxWaitMillis Max time to wait.
//	 * @return virtual screen dimension, or null if unavailable 
//	 * after the set wait period. 
//	 */
//	public static Rectangle getScreenBounds(int maxWaitMillis) {
//		
//		startScreenSizeProcess();
//		
//		long endTime = System.currentTimeMillis() + maxWaitMillis;
//		while (System.currentTimeMillis() <= endTime) {
//			if (screenDimension != null) {
//				return screenDimension;				
//			}
//			try {
//				Thread.sleep(100);
//			}
//			catch (InterruptedException e) {
//				return screenDimension;
//			}
//		}
//		return screenDimension;
//	}
//	
//	/**
//	 * 
//	 * @return true if screen bounds are available. 
//	 */
//	public boolean haveScreenBounds() {
//		return (screenDimension != null);
//	}

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
				//			System.out.printf("", dgc.getBufferCapabilities().)
				//			System.out.println(dgc);
				screenDimensions[j] = dgc.getBounds();
				virtualBounds = virtualBounds.union(dgc.getBounds());

			} 
		}
		//		new Pane();
		//		List<Screen> screens = Screen.getScreens();
		//		if (screens != null) {
		//			for (Screen aScreen : screens) {
		//				System.out.printf("Screen resX %d, resY %d\n", 	
		//						aScreen.getResolutionX(),
		//						aScreen.getResolutionY());
		//			}
		//		}
		//System.out.println("virtualBounds="+virtualBounds);
		return virtualBounds;
	}

	//	/**
	//	 * Thread to obtain the screen bounds. 
//	 * @author Doug
//	 *
//	 */
//	public class GetBounds implements Runnable {
//
//		@Override
//		public void run() {
//
//			screenDimension = findScreenBounds();
//
//		}
//
//	}

}
