package tethys.swing.export;

import tethys.localization.CoordinateName;

/**
 * Interface for algorithms that can provide a coordinate choice. Works with 
 * Coordinate choice panel to automatically generate choise. 
 * @author dg50
 *
 */
public interface CoordinateChoice {

	public CoordinateName[] getPossibleCoordinates();
	
	public CoordinateName getCoordinateName();
	
	public void setCoordinateName(CoordinateName coordinateName);
	
}
