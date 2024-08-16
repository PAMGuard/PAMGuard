package tethys.deployment;

/**
 * Enum of heading types (mostly for track output in Deployment documents)
 * @author dg50
 *
 */
public enum HeadingTypes {
	
	TRUE, MAGNETIC;

	@Override
	public String toString() {
		switch (this) {
		case MAGNETIC:
			return "magnetic";
		case TRUE:
			return "true";
		default:
			break;
		}
		return null;
	}
}
