package dataPlots.data;

import PamguardMVC.PamDataUnit;

/**
 * Utility class to return details of a data unit found on a graph. 
 * <p>
 * This is essentially a wrapper around a PamDataUnit which provides some more info on graph data type and selection method. 
 * @author Doug Gillespie
 *
 */
public class FoundDataUnit{
	
	/**
	 * Data unit was found by a single mouse click or finger tap. 
	 */
	public final static int SINGLE_SELECTION=0;
	
	/**
	 * Data unit was found byt being part of a selction area- i.e. was marked up with a series of other units.
	 */
	public final static int MARKED_AREA_DETECTION=1; 
	
	public int selectionType=0;
	
	public TDDataInfo dataInfo;
	
	public PamDataUnit dataUnit;

	public double distance;

	/**
	 * Create a FoundDataUnit
	 * @param dataInfo- dataInfo i.e. what typoe of unit this is. 
	 * @param dataUnit- the data unit
	 * @param type- the selection type. 
	 */
	public FoundDataUnit(TDDataInfo dataInfo, PamDataUnit dataUnit, int type) {
		super();
		this.selectionType=type;
		this.dataInfo = dataInfo;
		this.dataUnit = dataUnit;
	}
	
}
