package qa.database;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLogging;

public abstract class QASubTableLogging extends SQLLogging {

	public QASubTableLogging(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
	}
	
	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		if (superDetection == null) {
			return true;
		}
		else {
			return super.logData(con, dataUnit, superDetection);
		}
	}
}
