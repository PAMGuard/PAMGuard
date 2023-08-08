package RightWhaleEdgeDetector.species;

import tethys.species.DataBlockSpeciesTypes;

public class RWSpeciesTypes extends DataBlockSpeciesTypes {
	
	public static final String onlyType = "Up call";
	
	private static final int glacialis = 180536;

	public RWSpeciesTypes() {
		super(glacialis, onlyType);
	}

}
