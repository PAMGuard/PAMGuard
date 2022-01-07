package PamDetection;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import annotationMark.MarkDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public abstract class AcousticSQLLogging extends SQLLogging {

	private PamTableItem channelMap;
	private PamTableItem durationSecs, f1, f2;
	
	protected AcousticSQLLogging(PamDataBlock pamDataBlock, String tableName) {
		super(pamDataBlock);

		PamTableDefinition tableDef = new PamTableDefinition(tableName, SQLLogging.UPDATE_POLICY_OVERWRITE);
		tableDef.addTableItem(channelMap = new PamTableItem("Sequence", Types.INTEGER));
		tableDef.addTableItem(durationSecs = new PamTableItem("Duration", Types.DOUBLE)); 
		tableDef.addTableItem(f1 = new PamTableItem("f1", Types.DOUBLE)); 
		tableDef.addTableItem(f2 = new PamTableItem("f2", Types.DOUBLE)); 
		setTableDefinition(tableDef);
		
		AnnotationHandler annotationHandler = pamDataBlock.getAnnotationHandler();
		if (annotationHandler != null) {
			List<DataAnnotationType<?>> annotationTypes = annotationHandler.getUsedAnnotationTypes();
			for (int i = 0; i < annotationTypes.size(); i++) {
				DataAnnotationType anType = annotationTypes.get(i);
				SQLLoggingAddon sqlLoggingAddon = anType.getSQLLoggingAddon();
				if (sqlLoggingAddon != null) {
					addAddOn(sqlLoggingAddon);
				}
			}
		}
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {	
		PamDataUnit acousticDataUnit = pamDataUnit;	// originally cast to AcousticDataUnit
		channelMap.setValue(pamDataUnit.getSequenceBitmap());
		double duration = acousticDataUnit.getDurationInMilliseconds() / 1000.;
		durationSecs.setValue(duration);
		double[] f = acousticDataUnit.getFrequency();
		if (f == null || f.length != 2) {
			f1.setValue(null);
			f2.setValue(null);
		}
		else {
			f1.setValue(f[0]);
			f2.setValue(f[1]);
		}
	}
	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds,
			int databaseIndex) {
		long duration = (long) (durationSecs.getDoubleValue() * 1000.);
		double[] f = new double[2];
		f[0] = f1.getDoubleValue();
		f[1] = f2.getDoubleValue();
		int chanMap = channelMap.getIntegerValue();
		return createDataUnit(sqlTypes, timeMilliseconds, chanMap, duration, f);
	}

	protected abstract PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap,
			long duration, double[] f);

}
