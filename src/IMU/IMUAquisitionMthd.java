package IMU;

import PamView.panel.PamPanel;

/**
 * The IMU module may have multiple ways of aquiring IMU data. Each method must satisfy this interface. This is generally used for realtime aquisition. 
 * @author Jamie Macaulay	
 *
 */
public interface IMUAquisitionMthd {
	
	public String getName();
	
	public PamPanel getSettingsPanel();

}
