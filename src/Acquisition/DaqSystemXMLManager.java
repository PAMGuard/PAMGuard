package Acquisition;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;

/**
 * Odd little class that tries to stop all of the settings for every possible
 * daq system being output to xml by only accepting them if they are the selected
 * system type of at least one daq module. Annoyingly, individual parameter sets don't and
 * can't have a reference back to their module, so this has to be done at the global level<br><br>
 * See the SimParameters class for an example on how to use.<br><br>
 * Note that this can't be implemented everywhere.  It will not work when the parameters class
 * is shared by multiple PamSettings classes.  For example: the SoundCardParameters class is used
 * by the SoundCardSystem, ASIOSoundSystem and NewAsioSoundSystem classes.  Each of these has it's own
 * type (see the individual getUnitType calls) but the SoundCardParameters class doesn't know who called
 * it and so can't properly check whether the type matches.  Further, SoundCardParameters is extended by 
 * NIDAQParams, SimParameters and SmruDaqParameters.  So trying to make SoundCardParameters check for a
 * type would potentially screw up those extended classes.  That being said, the extended classes are
 * able to check for their types themselves.
 * 
 * @author dg50
 *
 */
public class DaqSystemXMLManager {

	public static boolean isSelected(String daqSystemType) {
		ArrayList<PamControlledUnit> daqs = PamController.getInstance().findControlledUnits(AcquisitionControl.class);
		if (daqs == null) {
			return false;
		}
		for (int i = 0; i < daqs.size(); i++) {
			AcquisitionControl daq = (AcquisitionControl) daqs.get(i);
			AcquisitionParameters daqParams = daq.getAcquisitionParameters();
			if (daqParams.daqSystemType.equals(daqSystemType)) {
				return true;
			}
		}
		return false;
	}
}
