package generalDatabase.version;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class VersionLogging extends SQLLogging {
	
	PamTableItem name, className, version;

	public VersionLogging(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		PamTableDefinition tableDef = new PamTableDefinition("Version Information");
		tableDef.addTableItem(name = new PamTableItem("Name", Types.VARCHAR));
		tableDef.addTableItem(className = new PamTableItem("Class", Types.VARCHAR));
		tableDef.addTableItem(version = new PamTableItem("Version", Types.VARCHAR));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		VersionDataUnit vdu = (VersionDataUnit) pamDataUnit;
		name.setValue(vdu.getName());
		className.setValue(vdu.getClassName());
		version.setValue(vdu.getVersion());
		
	}

}
