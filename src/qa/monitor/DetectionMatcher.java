package qa.monitor;

import PamguardMVC.PamDataUnit;
import qa.QASoundDataUnit;

/**
 * Interface to allow a bit of flexibility as to how different 'detector' outputs are 
 * matched with generated QA sounds.
 * @author dg50
 *
 * @param <T>
 */
public interface DetectionMatcher<T extends PamDataUnit> {

	/**
	 * 
	 * @return The name of the match system
	 */
	public String getName();
	
	/**
	 * 
	 * @return a longer description, perhaps including any parameters 
	 * describing how this is configured 
	 */
	public String getDescription();
	
	/**
	 * Get the overlap between the qaSound and the detection. This is reported
	 * as the percentage of the qaSound which is covered by the detection
	 * @param detection
	 * @param qaSound
	 * @return
	 */
	public double getOverlap(T detection, QASoundDataUnit qaSound);
	
	/**
	 * @return True if there is a settings dialog
	 */
	public boolean hasSettings();
	
	/**
	 * show the settings dialog
	 * @param parentWindow parent window
	 * @return true if settings were OK'd. 
	 */
	public boolean showSettings(Object parentWindow);
	
}
