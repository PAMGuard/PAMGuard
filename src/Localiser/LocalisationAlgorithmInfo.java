package Localiser;

import java.awt.Window;
import java.io.Serializable;

import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import tethys.localization.LocalizationBuilder;
import tethys.swing.export.LocalizationOptionsPanel;

public interface LocalisationAlgorithmInfo {
	
	/**
	 * Get the likely content flags for this localiser. 
	 * @see LocalisationInfo
	 * @see LocContents
	 * @return localisation flags. 
	 */
	public int getLocalisationContents();
	
	/**
	 * Get the algorithm name
	 * @return algorithm name
	 */
	public String getAlgorithmName();
	
	/**
	 * Get the algorithm parameters. Something else 
	 * can turn these into xml for Tethys. 
	 * @return algorithm parameters object. Might be null;
	 */
	public Serializable getParameters();
	
	/**
	 * Get options panel to either build into the export dialog or to show as a separate
	 * dialog (in which case export wizard will show a button). Can be null if no options. 
	 * @param locBuilder
	 * @return options panel or null if there are no options. 
	 */
	public LocalizationOptionsPanel getLocalizationOptionsPanel(Window parent, LocalizationBuilder locBuilder);
	
}
