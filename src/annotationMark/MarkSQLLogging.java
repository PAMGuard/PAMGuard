package annotationMark;

import PamDetection.AcousticSQLLogging;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

/**
 * Database logging for stand alone Annotation data. 
 * <p>This is just a skeleton SQL logging which saves nothing 
 * more than time. Everything else is set using an SQLLoggingAddon created
 * by the DataAnnotationType. 
 * @author Doug Gillespie
 *
 */
public class MarkSQLLogging extends AcousticSQLLogging {
	
	public MarkSQLLogging(MarkDataBlock pamDataBlock, String tableName) {
		super(pamDataBlock, tableName);
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap,
			long duration, double[] f) {
		MarkDataUnit adu = new MarkDataUnit(timeMilliseconds, chanMap, duration);
		adu.setFrequency(f);
		return adu;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#logData(generalDatabase.PamConnection, PamguardMVC.PamDataUnit)
	 */
	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return super.logData(con, dataUnit);
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#addAddOn(generalDatabase.SQLLoggingAddon)
	 */
	@Override
	public void addAddOn(SQLLoggingAddon sqlLoggingAddon) {
		// TODO Auto-generated method stub
		super.addAddOn(sqlLoggingAddon);
	}


}
