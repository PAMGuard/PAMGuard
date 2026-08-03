package NMEA;

import PamModel.CommonPluginInterface;

public interface NMEAPlugin extends CommonPluginInterface {

	/**
	 * Get an NMEA provider that can be added to the list. 
	 * @param nmeaControl
	 * @return
	 */
	public NMEAProvider getNMEAProvider(NMEAControl nmeaControl);
	
}
