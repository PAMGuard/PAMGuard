package annotation.string;

import java.sql.Types;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class StringSQLLogging implements SQLLoggingAddon {

	private StringAnnotationType stringAnnotationType;
	
//	private PamTableItem durationSecs, f1, f2, 
	private PamTableItem notes;
	
	
	public StringSQLLogging(StringAnnotationType stringAnnotationType) {
		super();
		this.stringAnnotationType = stringAnnotationType;
		notes = new PamTableItem(stringAnnotationType.getAnnotationName(),
				Types.CHAR, stringAnnotationType.getMaxLength(), "Notes");
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		pamTableDefinition.addTableItem(notes); 
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		StringAnnotation stringAnnotation = (StringAnnotation) pamDataUnit.findDataAnnotation(StringAnnotation.class, 
				stringAnnotationType.getAnnotationName());
		if (stringAnnotation == null) {
			notes.setValue("");
			return false;
		}

		notes.setValue(stringAnnotation.getString());
		return true;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		try {
			String note = notes.getDeblankedStringValue();
			if (note != null && note.length() > 0) {
				note = note.trim();
				StringAnnotation stringAnnotation = new StringAnnotation(stringAnnotationType);
				stringAnnotation.setString(note);
				pamDataUnit.addDataAnnotation(stringAnnotation);
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
		return stringAnnotationType.getAnnotationName();
	}

}
