package alfa.utils;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;


/**
 * Functions to launch Microsoft Screen mirror service. 
 * We've been unable to work out a way of calling these functions 
 * directly so have instead implemented a system of key strokes. 
 * <p>May end up being very fragile !
 * @author MER Torp
 *
 */
public class MiraScreen {

//	public static void main(String[] args) {
//		
////		new Stage();
//		new JFXPanel();
//		
//		ObservableList<Screen> screens = Screen.getScreens();
//		for (Screen s:screens) {
//			System.out.println(s);
//		}	
//		
//		
//		
//		startMirror();
//
//	}
	
	private static volatile boolean running = false;
	
	
	
	public static boolean startMirror() {
		if (running) {
			return false;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				startMirrorFunc();
			}
		});
		t.start();
		return true;
	}

	private static synchronized void startMirrorFunc() {
		Robot robot;
		running = true;
		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_WINDOWS);
			robot.keyPress(KeyEvent.VK_K);
			robot.keyRelease(KeyEvent.VK_WINDOWS);
			robot.keyRelease(KeyEvent.VK_K);
			pause(5000);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
		} catch (AWTException e) {
			e.printStackTrace();
		}
		running = false;
	}
	
	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
