package PamView;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Class to get called early on which will initialise FX 
 * so that it can be used within the Swing GUI. 
 * @author dg50
 *
 */
public class FXInitialiser {

	public static boolean haveRun = false;
	private static boolean fxOK = false;
	private static volatile boolean running = false;
	
	public synchronized static void initialise() {
		if (!haveRun) {
			running = true;
			new JFXPanel();
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					fxInit();
				}
			});
			while (running) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			haveRun = true;
		}
	}

	private static void fxInit() {
		try {
	        Group  root  =  new  Group();
	        Scene  scene  =  new  Scene(root, Color.BLACK);
			fxOK = true;
		}
		catch (Exception e){
			e.printStackTrace();
			fxOK = false;
		}
		running = false;
	}

	public static boolean isFxOK() {
		return fxOK;
	}
}
