package annotation.timestamp;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class TimestampSQLLogging implements SQLLoggingAddon {

	private TimestampAnnotationType timestampAnnotationType;
	private PamTableItem timestamp;
	
	
	public TimestampSQLLogging(TimestampAnnotationType timestampAnnotationType) {
		super();
		this.timestampAnnotationType = timestampAnnotationType;
		timestamp  = new PamTableItem(timestampAnnotationType.getAnnotationName(),
				Types.TIMESTAMP);
	}

	@Override
	public void addTableItems(PamTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(timestamp); 
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		TimestampAnnotation timestampAnnotation = (TimestampAnnotation) pamDataUnit.findDataAnnotation(TimestampAnnotation.class, 
				timestampAnnotationType.getAnnotationName());
		if (timestampAnnotation == null) {
			timestamp.setValue(null);
		}
		else {
			timestamp.setValue(sqlTypes.getTimeStamp(timestampAnnotation.getTimestamp()));
		}
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, PamTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		try {
			TimestampAnnotation timestampAnnotation = new TimestampAnnotation(timestampAnnotationType);
			Long note = sqlTypes.millisFromTimeStamp(timestamp.getValue());
			if (note != null){
				timestampAnnotation.setTimestamp(note);
				pamDataUnit.addDataAnnotation(timestampAnnotation);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		return timestampAnnotationType.getAnnotationName();
	}

}
