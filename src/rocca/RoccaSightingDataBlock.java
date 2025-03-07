/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
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


package rocca;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import rocca.tethys.RoccaSpeciesManager;
import rocca.tethys.RoccaTethysProvider;

/**
 * Container to hold all the sighting data
 *
 * @author Michael Oswald
 */
public class RoccaSightingDataBlock extends PamDataBlock<RoccaSightingDataUnit>  {

    private RoccaControl roccaControl;

	/**
     * Main Constructor
     */
	public RoccaSightingDataBlock(RoccaControl roccaControl, PamProcess parentProcess, int channelMap) {
		super(RoccaSightingDataUnit.class, "Rocca Encounter Stats", parentProcess, channelMap);
		this.roccaControl = roccaControl;
    }

    /**
     * Finds a RoccaSightingDataUnit by the sighting number field.  If a
     * matching sighting number cannot be found, a 'null' is returned.
     * 
     * @param snum sighting number to search for
     * @return the RoccaSightingDataUnit with the matching sighting number
     */
    public RoccaSightingDataUnit findDataUnitBySNum(String snum) {
        RoccaSightingDataUnit rsdu = null;
        for (int i=0;i<this.getUnitsCount(); i++) {
            rsdu = this.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
            if (rsdu.getSightNum().equals(snum)) {
                break;
            }
        }
        return rsdu;
    }

    /**
     * Override the PamDataBlock clearAll() method, otherwise the detections
     * will be erased everytime the spectrogram is run
     */
    @Override
    public synchronized void clearAll() {
    }

}
