package tethys.species;

/**
 * Some MR defined ITIS codes for non animal sounds. 
 * @author dg50
 *
 */
public class ITISTypes {

	/*
	 * A few that get used in defaults copied from itis.gov for convenience. 
	 */
	public static final int OTHER = -10;
	public static final int ANTHROPOGENIC = 1758;
	public static final int CETACEAN = 180403;
	public static final int ODONTOCETE = 180404;
	public static final int MYSTICETE = 552298;
	
	public static final String getName(int code) {
		switch (code) {
		case OTHER:
			return "Unknown";
		case ANTHROPOGENIC:
			return "Anthropogenic";
		}
		return null;
	}
}
