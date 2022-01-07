package PamguardMVC;

/**
 * Default data unit matcher for use with 
 * DataUnitFinder
 * Matches clicks on time and possibly also channel/sequence number
 * depending on the number of arguments supplied.
 * @see DataUnitFinder 
 * @author Doug Gillespie
 *
 */
public class DefaultUnitMatcher implements DataUnitMatcher {

	@Override
	public int match(PamDataUnit dataUnit, Object... criteria) {
		long dT = (Long) criteria[0] - dataUnit.getTimeMilliseconds();
		if (dT != 0) {
			return Long.signum(dT);
		}
//		if (criteria.length >= 2 &&
//				dataUnit.getChannelBitmap() != (Integer) criteria[1]) {
		if (criteria.length >= 2) {
			int dC = (Integer) criteria[1] - dataUnit.getChannelBitmap();
			if (dC != 0) {
				return Integer.signum(dC);
			}
		}
//		if (criteria.length >= 2 &&
//				dataUnit.getSequenceBitmap() != (Integer) criteria[1]) {
//			return false;
//		}
		return 0; // match
	}

}
