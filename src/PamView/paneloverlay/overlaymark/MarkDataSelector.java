package PamView.paneloverlay.overlaymark;

import PamguardMVC.PamDataUnit;

/**
 * Class that helps to chose whether a data unit within a given mark is 
 * actually wanted or not. These are generally held and managed by things
 * that receive marks, rather than mark providers.  
 * @author Doug Gillespie
 *
 */
public interface MarkDataSelector {

	public static final int OVERLAP_NONE = 0;
	public static final int OVERLAP_SOME = 1;
	public static final int OVERLAP_ALL = 2;
	
	/**
	 * Test a data unit that within a mark to see if it's wanted 
	 * or not.
	 * @param dataUnit data unit
	 * @param overlapLevel level of overlap with the mark. 
	 * @return true if we want to keep the data unit. 
	 */
	boolean wantDataUnit(PamDataUnit dataUnit, int overlapLevel);
	
}
