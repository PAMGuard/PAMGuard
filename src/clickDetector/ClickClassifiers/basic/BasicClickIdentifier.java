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

package clickDetector.ClickClassifiers.basic;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.PamSymbol;
import PamView.symbol.SymbolData;
import clickDetector.BasicClickIdParameters;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickTypeParams;
import clickDetector.ClickClassifiers.ClassifyDialogPanel;
import clickDetector.ClickClassifiers.ClickIdInformation;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.ClickClassifiers.ClickTypeCommonParams;
import clickDetector.ClickClassifiers.ClickTypesDialog;
import clickDetector.ClickClassifiers.basicSweep.ZeroCrossingStats;
import clickDetector.layoutFX.clickClassifiers.BasicIdentifierPaneFX;
import clickDetector.layoutFX.clickClassifiers.ClassifyPaneFX;

/**
 * 
 * @author Doug Gillespie
 *         <p>
 *         Identifies individual clicks to type (e.g. porpoise, beaked whale,
 *         etc) using methods developed by Marjolaine Caillat, SMRU, in 2005.
 *         <p>
 *         Classification is based on the following:
 *         <ul>
 *         <li>Relative energy in two different frequency bands
 *         <li>Peak spectral frequency in the click
 *         <li>The width of the main frequency peak
 *         <li>The duration of the click
 *         </ul>
 *         Multiple click types may be defined. If multile click types are
 *         defined, then the FIRST type to satisfy all criteria is selected.
 */
public class BasicClickIdentifier implements ClickIdentifier, PamSettings {

	protected BasicClickIdParameters idParameters = new BasicClickIdParameters();

	ClickControl clickControl;
	
	private BasicIdentifierPanel basicIdentifierPanel;

	/**
	 * The FX classifier pane. 
	 */
	private BasicIdentifierPaneFX basicIdentifierPaneFX;


	/**
	 * 
	 * @param clickDetector
	 */
	public BasicClickIdentifier(ClickControl clickControl) {
		this.clickControl = clickControl;
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * Returns a JMenuItem which will launch a dialog for defining different
	 * click types.
	 */
	public JMenuItem getMenuItem(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Click classification ...");
		menuItem.addActionListener(new MenuClickTypes(parentFrame));
		return menuItem;
	}

	/**
	 * Quick check to see if any click types have been defined
	 * 
	 * @return true if click types are defined
	 */
	public boolean haveClickTypes() {
		if (idParameters == null 
				|| idParameters.clickTypeParams == null
				|| idParameters.clickTypeParams.size() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Identifies a click.
	 * 
	 * @param click
	 *            A click from the detector
	 * @return the unique number code identifying the click type.
	 */
	public ClickIdInformation identify(ClickDetection click) {
		if (haveClickTypes()) {
			for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
				ClickTypeParams clickTypeInfo = idParameters.clickTypeParams.get(i);
				if (isThisType(click, clickTypeInfo)) {
					return new ClickIdInformation(clickTypeInfo.getSpeciesCode(),clickTypeInfo.getDiscard());
				}
			}
		}
		return new ClickIdInformation(0);
	}

	/**
	 * Checks a click against a specific type.
	 * 
	 * @param click
	 *            A Click from the detector
	 * @param params
	 *            Parameters describing this click type
	 * @return true if the click passes all selection criteria.
	 */
	private boolean isThisType(ClickDetection click, ClickTypeParams params) {
		/*
		 * Need to do a number of tests, if it fails any of them, return false;
		 */
		if ((params.whichSelections & ClickTypeParams.ENABLE_ENERGYBAND) != 0) {
			if (checkEnergyBands(click, params) == false) {
				return false;
			}
		}
		if ((params.whichSelections & ClickTypeParams.ENABLE_PEAKFREQPOS) != 0 ||
				(params.whichSelections & ClickTypeParams.ENABLE_PEAKFREQWIDTH) != 0) {
			if (checkPeakPosandWidth(click, params) == false) {
				return false;
			}
		}
		if ((params.whichSelections & ClickTypeParams.ENABLE_MEANFREQUENCY) != 0) {
			if (checkMeanFreq(click, params) == false) {
				return false;
			}
		}
		if ((params.whichSelections & ClickTypeParams.ENABLE_CLICKLENGTH) != 0) {
			if (checkLength(click, params) == false) {
				return false;
			}
		}
		

		return true;
	}

	/**
	 * Checks the relative energy in two different energy bands and also
	 * performs other tests to see if its the type of click described by params.
	 * 
	 * @param click
	 * @param params
	 * @return true if the click passes the test
	 */
	private boolean checkEnergyBands(ClickDetection click, ClickTypeParams params) {
		double eTest1 = click.inBandEnergy(params.band1Freq);
		if (eTest1 < params.band1Energy[0] || eTest1 > params.band1Energy[1]) {
			return false;
		}
		// check in band energies and energy ratio
		double eTest2 = click.inBandEnergy(params.band2Freq);
		if (eTest2 < params.band2Energy[0] || eTest2 > params.band2Energy[1]) {
			return false;
		}
		if (eTest1 - eTest2 < params.bandEnergyDifference) {
			return false;
		}
		return true;
	}
	
	private boolean checkPeakPosandWidth(ClickDetection click, ClickTypeParams params) {

		// check peak Frequency position and width - all in Hz.
		double peakFreq = click.peakFrequency(params.peakFrequencySearch);
		if ((params.whichSelections & ClickTypeParams.ENABLE_PEAKFREQPOS) != 0) {
			if (peakFreq < params.peakFrequencyRange[0]
			    || peakFreq > params.peakFrequencyRange[1]) {
				return false;
			}
		}
		// check click peak width
		if ((params.whichSelections & ClickTypeParams.ENABLE_PEAKFREQWIDTH) != 0) {
			double peakWidth = click.peakFrequencyWidth(peakFreq,
					params.widthEnergyFraction);
			if (peakWidth < params.peakWidth[0]
					|| peakWidth > params.peakWidth[1]) {
				return false;
			}
		}
		return true;
	}

	private boolean checkMeanFreq(ClickDetection click, ClickTypeParams params) {
		double meanFreq = click.getMeanFrequency(params.meanSumRange);
		if (meanFreq < params.meanSelRange[0] || meanFreq > params.meanSelRange[1]) {
			return false;
		}
		return true;
	}
	
	private boolean checkLength(ClickDetection click, ClickTypeParams params) {

		/*
		 * Check click length - it's returned in seconds, so convert to millis !
		 */
		if (params.clickLength[1] > 0) {
			double clickLength = click.clickLength(params.lengthEnergyFraction) * 1000;
			if (clickLength < params.clickLength[0]
					|| clickLength > params.clickLength[1]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 
	 * Action listener for the click types menu.
	 * <p>
	 * Opens a dialog for defining different click types.
	 * 
	 */
	class MenuClickTypes implements ActionListener {
		Frame parentFrame;
		
		public MenuClickTypes(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent ev) {
			if (idParameters == null) {
				idParameters = new BasicClickIdParameters();
			}
			BasicClickIdParameters newParams;
			if ((newParams = ClickTypesDialog.showDialog(parentFrame, idParameters, 
					clickControl.getClickDetector().getSampleRate())) != null) {
				idParameters = newParams.clone();
//				clickDetector.newParameters();
			}
		}
	}

	public Serializable getSettingsReference() {
		return idParameters;
	}

	public long getSettingsVersion() {
		return BasicClickIdParameters.serialVersionUID;
	}

	public String getUnitName() {
		return clickControl.getClickDetector().getProcessName();
	}

	public String getUnitType() {
		return "BasicClickIdParams";
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		idParameters = ((BasicClickIdParameters) pamControlledUnitSettings
				.getSettings()).clone();
		return (idParameters != null);
	}

	/**
	 * @return the PamSymbol associated with a particular click
	 */
	public PamSymbol getSymbol(ClickDetection click) {
		if (haveClickTypes() == false) {
			return null;
		}
		for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
			if (click.getClickType() == idParameters.clickTypeParams.get(i).getSpeciesCode()) {
				return idParameters.clickTypeParams.get(i).symbol;
			}
		}
		return null;
	}

	public String[] getSpeciesList() {
		if (idParameters == null 
				|| idParameters.clickTypeParams == null
				|| idParameters.clickTypeParams.size() == 0) {
			return null;
		}
		String[] strings = new String[idParameters.clickTypeParams.size()];
		for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
			strings[i] = idParameters.clickTypeParams.get(i).getName();
		}
		return strings;
	}
	
	public PamSymbol[] getSymbols() {
		if (idParameters == null 
				|| idParameters.clickTypeParams == null
				|| idParameters.clickTypeParams.size() == 0) {
			return null;
		}
		PamSymbol[] symbols = new PamSymbol[idParameters.clickTypeParams.size()];
		for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
			symbols[i] = idParameters.clickTypeParams.get(i).symbol;
		}
		return symbols;
	}

	private int[] codeToIndex;

	public int codeToListIndex(int code) {
		if (codeToIndex == null || code >= codeToIndex.length) {
			int maxCode = code;
			for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
				maxCode = Math.max(maxCode, idParameters.clickTypeParams.get(i).getSpeciesCode());
			}
			codeToIndex = new int[maxCode+1];
			for (int i = 0; i < idParameters.clickTypeParams.size(); i++) {
				codeToIndex[idParameters.clickTypeParams.get(i).getSpeciesCode()] = i;
			}
		}
		return codeToIndex[code];
	}

	@Override
	public String getSpeciesName(int code) {
		if (code == 0) {
			return null;
		}
		int li = codeToListIndex(code);
		String[] speciesList = getSpeciesList();
		if (speciesList == null) {
			return null;
		}
		if (li >= 0 && li < speciesList.length) {
			return speciesList[li];
		}
		return null;
	}

	@Override
	public ClassifyDialogPanel getDialogPanel(Frame windowFrame) {
		if (basicIdentifierPanel == null) {
			basicIdentifierPanel = new BasicIdentifierPanel(this, windowFrame, clickControl);
		}
		return basicIdentifierPanel;
	}

	@Override
	public String getParamsInfo(ClickDetection click) {
		return null;
	}
	
    /**
     * Returns a list of the currently-defined click types / species codes
     * @return int array with the codes
     */
    public int[] getCodeList() {
    	if (idParameters == null 
				|| idParameters.clickTypeParams == null
				|| idParameters.clickTypeParams.size() == 0) {
			return null;
    	}
        int[] clickList = new int[idParameters.clickTypeParams.size()];
        for (int i=0; i < idParameters.clickTypeParams.size(); i++) {
            clickList[i]=idParameters.clickTypeParams.get(i).getSpeciesCode();
        }
        return clickList;
    }

    /**
     * Return the superclass of the click type parameters class - currently used for
     * accessing the alarm functions.  Subclasses include ClickTypeParams and
     * SweepClassifierSet.
     *
     * @param code the click type to check
     * @return the ClickTypeCommonParams object related to the species code
     */
    public ClickTypeCommonParams getCommonParams(int code) {
        int codeIdx = codeToListIndex(code);
        if (codeIdx>=idParameters.clickTypeParams.size()) return null; 
        return idParameters.clickTypeParams.get(codeIdx);
    }

    /**
     * method used to get zero crossing data from sweep identifier.  Return null for
     * basic identifier
	 * 2014/07/25 MO
     */
	public ZeroCrossingStats[] getZeroCrossingStats(ClickDetection click) {
		return null;
	}

    /**
     * method used to get peak frequency search range from sweep identifier.  Return null for
     * basic identifier
	 * 2014/08/03 MO
     */
	public double[] getPeakSearchRange(ClickDetection click) {
		return null;
	}
	
	/**
	 * Returns the click length for the sweep classifier, using the times returned by the
	 * SweepClassifierWorker method getLengthData.  In the case of a different classifier,
	 * a 0 is returned
	 * 2014/10/13 MO
	 */
	public double getClickLength(ClickDetection click) {
		return 0.0;
	}

	/**
	 * Get ID params for the basic click identifier. 
	 * @return the params for basic clicks classifier. 
	 */
	public BasicClickIdParameters getIdParameters() {
		return idParameters;
	}

	/**
	 * Set the ID params for the basic click classifier. 
	 * @param idParameters
	 */
	public void setIdParameters(BasicClickIdParameters idParameters) {
		this.idParameters = idParameters;
	}
	
	@Override
	public ClassifyPaneFX getClassifierPane() {
		if (basicIdentifierPaneFX == null) {
			basicIdentifierPaneFX = new BasicIdentifierPaneFX(this, clickControl);
		}
		return basicIdentifierPaneFX;
	}
	

	/**
	 * Convert an aray of pam symbols to an array of symbol data. 
	 * @param pamSymbols - the pam symbol array to convert. 
	 * @return an array of symbol data correpsondiong to the pam symbols. 
	 */
	public static SymbolData[] pamSymbol2SymbolData(PamSymbol[] pamSymbols) {
		if (pamSymbols==null) return null; 
		SymbolData[] symbolDatas = new SymbolData[pamSymbols.length];
		for (int i=0; i<pamSymbols.length; i++) {
			symbolDatas[i]=pamSymbols[i].getSymbolData();
		}
		return symbolDatas;
	}
	

	@Override
	public SymbolData[] getSymbolsData() {
		return pamSymbol2SymbolData(getSymbols()) ;
	}
	
	
		

}
