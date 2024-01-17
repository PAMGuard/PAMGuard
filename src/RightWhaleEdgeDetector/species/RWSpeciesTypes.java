package RightWhaleEdgeDetector.species;

import tethys.species.DataBlockSpeciesCodes;

public class RWSpeciesTypes extends DataBlockSpeciesCodes {
	
	public static final String onlyType = "Up call";
	
	public static final int eubalaena = 180536;
	
	public static final String defaultName = "Right Whale";

	public RWSpeciesTypes() {
		super(eubalaena, defaultName, onlyType);
	}

}
