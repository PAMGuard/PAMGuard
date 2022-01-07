package backupmanager.database;

import backupmanager.stream.BackupStream;
import generalDatabase.EmptyTableDefinition;

public class DecisionTable extends EmptyTableDefinition {

	public DecisionTable(BackupStream backupStream) {
		super("BackupDecision " + backupStream.getName());
	}

}
