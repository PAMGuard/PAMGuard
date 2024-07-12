package tethys.niluswraps;

import nilus.GranularityEnumType;
import nilus.GranularityType;

/**
 * Decorate GranularityType
 * @author dg50
 *
 */
public class PGranularityType {

	/**
	 * Nicer formatted name for a dialog. 
	 * @param granularity
	 * @return
	 */
	public static String prettyString(GranularityEnumType granularity) {
		switch (granularity) {
		case BINNED:
			return "Binned";
		case CALL:
			return "Call / detection";
		case ENCOUNTER:
			return "Encounter";
		case GROUPED:
			return "Grouped";
		default:
			break;
		
		}
		return granularity.toString();
	}
	
	/**
	 * Tool tip for display in a dialog. 
	 * @param granularity
	 * @return
	 */
	public static String toolTip(GranularityEnumType granularity) {
		switch (granularity) {
		case BINNED:
			return "Output of counts in regular time periods";
		case CALL:
			return "Call level output";
		case ENCOUNTER:
			return "Encounter level output";
		case GROUPED:
			return "Grouped output (whatever that is?)";
		default:
			break;
		
		}
		return prettyString(granularity);
	}


}
