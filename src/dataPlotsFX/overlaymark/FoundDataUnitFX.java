package dataPlotsFX.overlaymark;

import PamguardMVC.PamDataUnit;
import dataPlotsFX.data.TDDataInfoFX;

/**
 * Wrapper class to hold a data unit and some click information. 
 * @author Jamie Macaulay 
 *
 */
public class FoundDataUnitFX  {
	
	
	/**
	 * Data unit was found by a single mouse click or finger tap. 
	 */
	public final static int SINGLE_SELECTION=0;
	
	/**
	 * Data unit was found by being part of a selection area- i.e. was marked up with a series of other units.
	 */
	public final static int MARKED_AREA_DETECTION=1; 
	
	public int selectionType=0;
	
	public TDDataInfoFX dataInfo;
	
	public PamDataUnit dataUnit;

	public double distance;

	/**
	 * Create a FoundDataUnit
	 * @param dataInfo- dataInfo i.e. what typoe of unit this is. 
	 * @param dataUnit- the data unit
	 * @param type- the selection type. 
	 */
	public FoundDataUnitFX(TDDataInfoFX dataInfo, PamDataUnit dataUnit, int type) {
		this.selectionType=type;
		this.dataInfo = dataInfo;
		this.dataUnit = dataUnit;
	}
	
	public FoundDataUnitFX(PamDataUnit dataUnit, int type) {
		this.selectionType=type;
		this.dataUnit = dataUnit;
	}


}
