/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
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

package clickDetector.ClickClassifiers;

import java.awt.Frame;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.SettingsPane;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.basicSweep.ZeroCrossingStats;
import clickDetector.layoutFX.clickClassifiers.ClassifyPaneFX;
import pamViewFX.fxNodes.PamSymbolFX;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Interface for click identification from the click detector
 *         <p>
 */
public interface ClickIdentifier extends ClickTypeProvider {

	/**
	 * Identify a click detection as a click type. 
	 * @param click - the click to identfy. 
	 * @return the click to 
	 */
	public ClickIdInformation identify(ClickDetection click);

	public JMenuItem getMenuItem(Frame parentFrame);

	public PamSymbol getSymbol(ClickDetection click);
	
	public ClassifyDialogPanel getDialogPanel(Frame windowFrame); 
	
	public String getSpeciesName(int code);

	public String getParamsInfo(ClickDetection click);
	
	/**
	 * Get symbols for each click type. 
	 * @return the symbols for type.
	 */
	public PamSymbol[] getSymbols();

    /**
     * Return the superclass of the click type parameters class - currently used for
     * accessing the alarm functions.  Subclasses include ClickTypeParams and
     * SweepClassifierSet.
     *
     * @param code the click type to check
     * @return the ClickTypeCommonParams object related to the species code
     */
    public ClickTypeCommonParams getCommonParams(int code);


//	public String getParamsInfo(ClickDetection click);

	/**
	 * return the zeroCrossingStats object in the case of the sweep classifier, or a null.
	 * 2014/07/25 MO
	 * 
	 * @param click the clickDetection to examine
	 * @return
	 */
	public ZeroCrossingStats[] getZeroCrossingStats(ClickDetection click);
	
	
	/**
	 * return the peak/mean frequency search range for the sweep classifier, or a null.
	 * 2014/08/03 MO
	 * 
	 * @param click the clickDetection to examine
	 * @return
	 */
	public double[] getPeakSearchRange(ClickDetection click);

	/**
	 * Returns the click length for the sweep classifier, using the times returned by the
	 * SweepClassifierWorker method getLengthData.  In the case of a different classifier,
	 * or if there is an error in the sweep classifier, a 0 is returned.
	 * 2014/10/13 MO
	 * 
	 * @param click the current click detection
	 * 
	 * @return the click duration, in seconds
	 */
	public double getClickLength(ClickDetection click);
	

	/*********************FX GUI***********************/

	/**	
	 * A pane which holds specific settings for the click classifier type selected. 
	 * @return the classifier pane
	 */
	public ClassifyPaneFX getClassifierPane(); 

		

}
