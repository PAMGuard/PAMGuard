package whistlesAndMoans;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.SQLTypes;

public class WhistleToneLogging extends PamDetectionLogging {

	public WhistleToneLogging(WhistleMoanControl whistleMoanControl, PamDataBlock pamDataBlock, int updatePolicy) {
		super(pamDataBlock, updatePolicy);
		getTableDefinition().setTableName(whistleMoanControl.getUnitName());
		getTableDefinition().setUseCheatIndexing(true);
		// TODO Auto-generated constructor stub
	}

}
