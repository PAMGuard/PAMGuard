package bearinglocaliser;

import PamguardMVC.PamDataUnit;

/**
 * Temp data units for bearing localiser and crossed bearing localisation ...
 * @author Doug Gillespie
 *
 */
public class TempDataUnit extends PamDataUnit {

	public TempDataUnit(PamDataUnit masterDataUnit, int newChannels) {
		super(masterDataUnit.getBasicData().clone());
		setChannelBitmap(newChannels);
		clearOandAngles();
		setDurationInMilliseconds(masterDataUnit.getDurationInMilliseconds());
	}
	
}

