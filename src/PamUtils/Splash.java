/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import PamController.PamController;
import PamController.PamguardVersionInfo;
import PamUtils.PlatformInfo.OSType;
import pamguard.Pamguard;

/**
 * Splash screen
 * @author David McLaren / Douglas Gillespie
 * Displays a splash screen used at Program start up and also 
 * when Help / about menu selected.
 * <p>
 * Rewritten for V1.1.1 (August 2008) so that a simple images is used 
 * containing no text and the version information is written over the
 * image. Makes updating for future releases a lot easier !
 * <p>
 * Changed around a little to allow for the native JVM to be displayed
 * at startup and then this Class replaces that on screen with one
 * that shows the version number and the GPL. Also made sure that 
 * the main GUI changing bit is invoked from the Event Dispatch Thread
 * CJB 2009-06-15
 *
 */
 
//make this class implement Runnable so if needs be the 
//main stuff that can effect the GUI be called using
//SwingUtilities.invokeAndWait() CJB  
public class Splash extends JWindow implements Runnable {	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4269582461538539626L;

	static private final int border = 17;	
	static private volatile boolean startupErrors = false;
	private int displayDuration;
	private int runMode;
	
	public Splash(int inDisplayDuration, int inRunMode) {
		displayDuration=inDisplayDuration;
		runMode=inRunMode;
		//"About PAMGUARD" calls this from within the EDT
		//in which case just need to use run() 
		if (SwingUtilities.isEventDispatchThread()) {
			this.run();
		}
		else {
			//let's us invokeAndWait rather than invokeLater
			//as shouldn't take long and want to try to 
			//ensure this Splash Screen is visible whilst 
			//most of other set up stuff starts to happen
			try {
	           SwingUtilities.invokeAndWait(this);
	        }
	        catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	
	@Override
	public void run()  
	   { 
		//let's see if we have a JVM SplashScreen already displayed
		//and if so draw our own exactly where it is on screen 
		//which should cause the JVM one to close automatically
		SplashScreen jvmSplashScreen;
		Rectangle jvmSplashBounds;
		
		try {
			jvmSplashScreen=SplashScreen.getSplashScreen();
			jvmSplashBounds=jvmSplashScreen.getBounds();
		}
		catch (Exception e) {
			jvmSplashScreen=null;
			jvmSplashBounds=null;
	    }
		
		String filename = "Resources/pamguardSplash.png";

		/*
		 * Use classloader to get image out of Jar file. 
		 */
		URL url = ClassLoader.getSystemResource(filename);
		if (url == null) {
			System.out.println("Can't find splash image " + filename);
			return;
		}
		BufferedImage splashImage = null;
		
		// if we're on a Mac, set the cache folder so that the splash screen displays
		if (PlatformInfo.calculateOS()==OSType.MACOSX) {
			ImageIO.setCacheDirectory(new File(Pamguard.getSettingsFolder()));
		}
		
		try {
			splashImage = ImageIO.read(url);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		

		int cornercolour = splashImage.getRGB(splashImage.getWidth()/2, 0);
		
		// write version information to a String
		String version; // = "Version " + PamguardVersionInfo.version + " " + PamguardVersionInfo.getReleaseType().toString();
		String modeText = null;
		
		if (runMode == PamController.RUN_MIXEDMODE) {
			modeText = "Mixed Mode";
		}
		else if (runMode == PamController.RUN_PAMVIEW) {
			modeText = "Viewer";
		}
		if (modeText != null) {
			version = modeText + " " + PamguardVersionInfo.version + " " + PamguardVersionInfo.getReleaseType().toString();
		}
		else {
			version = "Version " + PamguardVersionInfo.version + " " + PamguardVersionInfo.getReleaseType().toString();
		}
		
		// get image graphics handle.
		Graphics g = splashImage.getGraphics();
		
		// get a decent sized font. 
		Font f = new Font("Arial",Font.ITALIC | Font.BOLD, 24);
		int gr = 235;
		g.setColor(new Color(gr,gr,gr));
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(version, g);
		Dimension d = new Dimension( splashImage.getWidth(), splashImage.getHeight());
		// draw the strings onto the image. 
		g.drawString(version, (int) (d.width - r.getWidth() - border), (border + fm.getHeight()*1));
		r = fm.getStringBounds(PamguardVersionInfo.webAddress, g);
		g.drawString(PamguardVersionInfo.webAddress, (int) (d.width - r.getWidth() - border), (border + fm.getHeight()*2));
		
		String java = System.getProperty("sun.arch.data.model") + " bit";
		g.drawString(java, border, (border + fm.getHeight()*1));
//		if (modeText != null) {
//			r = fm.getStringBounds(modeText, g);
//			g.drawString(modeText, (int) (d.width - r.getWidth())/2, (border + fm.getHeight()*3));
//		}
				
		// convert to Icon so it scales automatically in layout. 
		ImageIcon ii = new ImageIcon(splashImage);
		
		JLabel label = new JLabel(ii);
		//splashWindow.getContentPane().add(label, BorderLayout.CENTER);
		getContentPane().add(label, BorderLayout.NORTH);
		
		/*
		 * Add GNU license information in bottom panel. 
		 */
		JTextArea ta = new JTextArea(PamguardVersionInfo.license);
		int grey = 240;
		ta.setBackground(new Color(grey, grey,grey));
		Font taFont = ta.getFont().deriveFont((float) 12.0);
		ta.setFont(taFont);
		ta.setBorder(new EmptyBorder(10, 10, 10, 10));
		ta.setWrapStyleWord(true);
		ta.setLineWrap(true);
		ta.setEditable(false);
		JPanel taOuter = new JPanel(new BorderLayout());
		taOuter.setBorder(BorderFactory.createLineBorder(new Color(cornercolour), 3, false));
		taOuter.add(BorderLayout.CENTER, ta);
			
		getContentPane().add(taOuter, BorderLayout.SOUTH);
		
		pack();			//this sets the size for the window
		ta.validate();	//this sets the line returns etc knowing the width the window will be
		pack();			//this fixes the heights

		setAlwaysOnTop(true);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension labelSize = label.getPreferredSize();

		if (jvmSplashBounds==null) {
			setLocation(screenSize.width / 2 - (labelSize.width / 2),
					screenSize.height / 2 - (labelSize.height / 2));
		}
		else {
			setLocation(jvmSplashBounds.x,jvmSplashBounds.y);
		}
		
		final Runnable closeSplashRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					setVisible(false);
					dispose();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		final int pauseTimeout = displayDuration; // Cast to final

		Runnable displayDurationRunnable = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(pauseTimeout);
					//Commented out as believe this call
					//now to be redundant as call made earlier
					//in main Pamguard.java code
					//CJB 2009-06-15
					//ScreenSize.getScreenBounds(5000);
					
					//OK at next chance get the EDT to close the JWindow  
					//CJB 2009-06-15
					SwingUtilities.invokeLater(closeSplashRunnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		setVisible(true);
		
		Thread splashThread = new Thread(displayDurationRunnable,
				"PamGuard Splash");
		splashThread.start();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				closeSplashRunnable.run();
			}
		});
	}
	
	public static boolean isStartupErrors() {
		return startupErrors;
	}
	public static void setStartupErrors(boolean startupErrors) {
		Splash.startupErrors = startupErrors;
	}
}