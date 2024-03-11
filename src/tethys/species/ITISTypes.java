package tethys.species;

/**
 * Some MR defined ITIS codes for non animal sounds. 
 * @author dg50
 *
 */
public class ITISTypes {

	public static final int OTHER = 0;
	public static final int ANTHROPOGENIC = 1;
	
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
