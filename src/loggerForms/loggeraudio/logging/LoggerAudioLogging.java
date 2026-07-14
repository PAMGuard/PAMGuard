package loggerForms.loggeraudio.logging;

import java.io.File;
import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class LoggerAudioLogging extends SQLLogging {
	
	private PamTableItem platform, file, duration;

	public LoggerAudioLogging(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		PamTableDefinition tableDef = new PamTableDefinition("Logger Voice");
		tableDef.addTableItem(platform = new PamTableItem("Platform", Types.VARCHAR));
		tableDef.addTableItem(file = new PamTableItem("File", Types.VARCHAR));
		tableDef.addTableItem(duration = new PamTableItem("Duration", Types.INTEGER));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		LoggerAudioDataUnit ladu = (LoggerAudioDataUnit) pamDataUnit;
		File f = new File(ladu.getFileName());
		String name = f.getName();
		platform.setValue(ladu.getPlatform());
		file.setValue(name);
		duration.setValue(ladu.getDuration());

	}

}
