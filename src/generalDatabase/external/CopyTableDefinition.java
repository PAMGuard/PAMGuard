package generalDatabase.external;

import generalDatabase.EmptyTableDefinition;

public class CopyTableDefinition extends EmptyTableDefinition {

	public Long idMin, idMax;
	
	public Long utcMin, utcMax;
	
	public CopyTableDefinition(String tableName) {
		super(tableName);
	}

}
