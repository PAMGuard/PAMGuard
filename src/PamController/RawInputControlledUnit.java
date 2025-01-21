package PamController;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

public abstract class RawInputControlledUnit extends PamControlledUnit {


	public static final int RAW_INPUT_UNKNOWN = 0;
	public static final int RAW_INPUT_FILEARCHIVE = 1;
	public static final int RAW_INPUT_REALTIME = 2;
	
	public RawInputControlledUnit(String unitType, String unitName) {
		super(unitType, unitName);
	}
	
	/**
	 * Type of data input, which can be one of RAW_INPUT_UNKNOWN (0),
	 *  RAW_INPUT_FILEARCHIVE (1), or RAW_INPUT_REALTIME (2)
	 * @return
	 */
	public abstract int getRawInputType();
	
	/**
	 * Start button extra actions. e.g. to process from first file, continue from current position, etc. 
	 * @param component The start button
	 * @param e input event, might be a MouseEvent or a ActionEvent from a button
	 */
	public void startButtonXtraActions(Component component, AWTEvent e) {
	}
	
	/**
	 * Get a tooltip for the default action of the start button. Return 
	 * null for default "start processing" tip. 
	 * @return
	 */
	public String getStartButtonToolTip() {
		return null;
	}

}
