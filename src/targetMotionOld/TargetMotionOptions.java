package targetMotionOld;

import java.io.Serializable;

import tethys.localization.CoordinateName;

public class TargetMotionOptions implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	/**
	 * Coordinate choice for Tethys export. 
	 */
	public CoordinateName exportCoordinate = CoordinateName.WGS84;

	public TargetMotionOptions() {
		// TODO Auto-generated constructor stub
	}

}
