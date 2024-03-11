package rawDeepLearningClassifier.logging;

import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableDefinition;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;

public class DLAnnotationSQL implements SQLLoggingAddon   {

	public DLAnnotationSQL(DLAnnotationType dlAnnotationType) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addTableItems(EmptyTableDefinition pamTableDefinition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean saveData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean loadData(SQLTypes sqlTypes, EmptyTableDefinition pamTableDefinition, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
