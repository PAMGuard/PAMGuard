package Spectrogram;

import java.awt.event.MouseEvent;

import PamguardMVC.PamDataBlock;
import dataPlotsFX.layout.TDGraphFX;

/**
 * Class to receive notifications when the mouse is dragged across the spectrogram display
 * Get's called on mouse down and mouse up. 
 * @author dg50
 *
 */
public interface SpectrogramMarkObserver {

	public static final int MOUSE_DOWN = 0;
	public static final int MOUSE_UP = 1;
	public static final int MOUSE_DRAG = 2;
	
	/**
	 * Receive notifications from the spectrogram display when the mouse is pressed and dragged on the display. 
	 * <p>
	 * Modified, 3 Jan 2014 to be sent for all buttons. Button number is coded into 
	 * high word of downUp. Normal Java button numbers are 1,2,3. The actual code used is
	 * downUp = oldDownUp + (button-1) &lt;&lt; 16 
	 * so existing button1 commands will not be affected in any way but it's now 
	 * possible to program up more advanced responses based on other buttons.
	 * <p>
	 * Modified 5/8/2020 - added TDGraphFX object.  When a mark is made on a TD Display, the SpectrogramDisplay object is null.
	 * Adding the TDGraphFX object allows the Mark Observers to still have access to the underlying data blocks
	 * 
	 * @param display spectrogram display
	 * @param mouseEvent Full mouse event - allows more flexibility in user functions
	 * @param downUp 0 = mouse down, 1 = mouse up, 2 = drag + button number in upper 16 bits. 
	 * @param channel channel or sequence number, depending on the source
	 * @param startMilliseconds start time in milliseconds. 
	 * @param duration duration in milliseconds.
	 * @param f1 min frequency in Hz
	 * @param f2 max frequency in Hz
	 * @param sourceDataBlock the data block 
	 * @return true if the user has popped up a menu or done something that should stop the 
	 * sending display from carrying out any further actions on this mouse action (put in place to prevent 
	 * the spectrogram menu popping up when editing marks in viewer mode). 
	 */
	public boolean spectrogramNotification(SpectrogramDisplay display, MouseEvent mouseEventFX, int downUp, int channel, 
			long startMilliseconds, long duration, double f1, double f2, TDGraphFX tdDisplay);
	
	/**
	 * 
	 * @return Name for the mark observer to show in the spectrogram dialog. 
	 */
	String getMarkObserverName();
	
	/**
	 * 
	 * @return whether or not it's possible to use this marker. 
	 * Generally false when in viewer mode, but maynot always be. 
	 */
	boolean canMark();

	/**
	 * Name of the mark. Observers may have several mark types and might want to 
	 * change the name to indicate what action will be taken when a mark is made
	 * 
	 * @return
	 */
	public String getMarkName();
	
}
