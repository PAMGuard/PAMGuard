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
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

/**
 *
 *
 * @author Michael Oswald
 */
public class RoccaLoggingDataBlock extends PamDataBlock<RoccaLoggingDataUnit> {

    public RoccaProcess roccaProcess = null;
    
	private RoccaTethysProvider roccaTethysProvider;
	
	private RoccaSpeciesManager roccaSpeciesManager;

	private RoccaControl roccaControl;
	

    /**
     * Constructor.  Set the natural lifetime to the maximum integer value
     * possible so that the data is never deleted
     * 
     * serialVersionUID = 18 
     * 2015/04/20 change this to save the data for 10 seconds instead of forever.  Especially when
     * using the Whistle and Moan Detector, there can be so many detections that the computer memory
     * can be overloaded and cause a crash if we don't periodically get rid of the old units. There
     * isn't really any point in keeping them anyway, because they're saved to the database almost
     * immediately after being created.
     */
	public RoccaLoggingDataBlock(RoccaControl roccaControl, PamProcess parentProcess, int channelMap) {
		super(RoccaLoggingDataUnit.class, "Rocca Whistle Stats", parentProcess, channelMap);
        this.roccaProcess = (RoccaProcess) parentProcess;
        this.roccaControl = roccaControl;
        //this.setNaturalLifetime(Integer.MAX_VALUE/1000);	
        this.setNaturalLifetime(10);
        setPamSymbolManager(new RoccaSymbolManager(this, RoccaGraphics.defaultSymbol));
    }


	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (roccaTethysProvider == null) {
			roccaTethysProvider = new RoccaTethysProvider(tethysControl, roccaControl, this);
		}
		return roccaTethysProvider;
	}


	@Override
	public DataBlockSpeciesManager<RoccaLoggingDataUnit> getDatablockSpeciesManager() {
		if (roccaSpeciesManager == null) {
			roccaSpeciesManager = new RoccaSpeciesManager(roccaControl, this);
		}
		return roccaSpeciesManager;
	}

    /**
     * Override the PamDataBlock clearAll() method, otherwise the detections
     * will be erased everytime the spectrogram is run
     * 
     * serialVersionUID = 18
     * 2015/04/20 remove this method - memory problems can occur if we don't get rid of old units
     * when restarting the run 
     */
    //@Override
    //public synchronized void clearAll() {
    //}
	
	 



}
