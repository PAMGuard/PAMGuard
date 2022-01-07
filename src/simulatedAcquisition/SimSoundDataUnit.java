package simulatedAcquisition;

import simulatedAcquisition.sounds.SimSound;
import PamguardMVC.PamDataUnit;

/**
 * One of these will be created for every sound produced
 * by a SimObjectDataUnit. A single datablock holds units
 * for all different SimObjectDataUnit's. It's primary purpose 
 * is to interface to the database. 
 * @author doug
 *
 */
public class SimSoundDataUnit extends PamDataUnit {

	private SimSound simSound;

	public SimSoundDataUnit(long timeMilliseconds, SimSound simSound) {
		super(timeMilliseconds);
		this.simSound = simSound;
	}

	/**
	 * @return the simSound
	 */
	public SimSound getSimSound() {
		return simSound;
	}

}
