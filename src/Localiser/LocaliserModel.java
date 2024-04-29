package Localiser;

import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamguardMVC.PamDataUnit;

/**
 * All localisers in PAMGuard should satisfy this interface. 

 * @author Jamie Macaulay
 *
 * @param <T> - the type of PamDataUnit
 * @param <U> - the settings class for the localiser. 
 */
public interface LocaliserModel<T extends PamDataUnit> {

	/**
	 * Get the name which describes the localiser. 
	 * @return the name of the localiser. 
	 */
	public String getName();
	
	/*
	 *Some text describing the localiser  
	 */
	public String getToolTipText();
	
	/**
	 * The type of localisation information the localiser can accept. e.g. bearings, time delays etc. The types are
	 * defined in the AbstractLocalisation class.  
	 * @return integer bitmap of the type of localisation information the localiser can use. 
	 */
	public LocContents getLocContents();
	
	/**
	 * Get the settings pane for the localiser. Allows users to change localiser settings.
	 * @return the settings pane for the localiser. 
	 */
	public LocaliserPane<?> getAlgorithmSettingsPane(); 


	/**
	 * True if the model has parameters to set. If has the localiser has a settings pane it will have
	 * parameters. This generally puts an extra button onto a display panel. 
	 */
	public boolean hasParams(); 

	/**
	 * Run the localisation model. Once completed the results are added to the AbstractLoclaisation class of the input PamDataUnit.
	 *Note that algorithms may run on a separate thread. Once processing has finished the notifyModelFinished function is called with 
	 * a progress of 1;. 
	 * @param pamDataUnit the pamDataUnit. This can be a super unit if multiple detections are required.
	 * @param addLoc  automatically add the localisation result to the data unit, replacing it's current localisation info. 
	 */
	public AbstractLocalisation runModel(T pamDataUnit, DetectionGroupOptions detectionGroupOptions, boolean addLoc);

	/**
	 * This should be called whenever the localiser has finished processing and, if the localisation process is long, then updates progress. 
	 */
	public void notifyModelProgress(double progress); 

}