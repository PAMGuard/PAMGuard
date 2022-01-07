package clickDetector.echoDetection;

import clickDetector.ClickControl;
import clickDetector.ClickDetector.ChannelGroupDetector;

/**
 * An echo detection system can provide everything there that is needed to decide whether clicks 
 * are echoes or not. This includes the code for processing click and also dialogs for controlling 
 * the echo decision process. 
 * 
 * @author Doug Gillespie
 *
 */
public abstract class EchoDetectionSystem {
	
	private ClickControl clickcontrol;
	
	/**
	 * @param clickcontrol
	 */
	public EchoDetectionSystem(ClickControl clickcontrol) {
		super();
		this.clickcontrol = clickcontrol;
	}

	/**
	 * Get a dialog panel to be included in a larger or more general dialog.  
	 * @return a dialog panel. 
	 */
	public abstract EchoDialogPanel getEchoDialogPanel();
	
	/**
	 * Get an echo detection object for a set of channels. 
	 * @param clickControl
	 * @param channelGroupDetector
	 * @param channelBitmap channels to be included in this particular
	 * echo detector. 
	 * @return an echo detector for a group of channels. 
	 */
	public abstract EchoDetector createEchoDetector(ChannelGroupDetector channelGroupDetector, 
			int channelBitmap);

	/**
	 * @return the clickcontrol
	 */
	public ClickControl getClickcontrol() {
		return clickcontrol;
	}
}
