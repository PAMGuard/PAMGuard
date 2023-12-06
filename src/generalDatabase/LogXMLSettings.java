package generalDatabase;

import java.io.Serializable;
import java.util.HashMap;

import org.w3c.dom.Document;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamSettings;
import PamController.PamguardVersionInfo;
import PamController.settings.output.xml.PamguardXMLWriter;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
/**
 * 2022 Additional way of saving settings for each module into the database in more human readable 
 * XML format. <p>
 * In other ways, similar to LogSettings which saves serialised Java. This will write a line
 * per module. A main difference is that on pamStop it will update the end time for each line, so 
 * that we have a record of analysis effort for each module. Will therefore need to store the last
 * index of the entry for each module, so that we can update the appropriate row. 
 * @author dg50
 *
 */
public class LogXMLSettings extends DbSpecial {
	
	private XMLSettingsTableDefinition xmlTableDef;
	
	private HashMap<String, LogXMLDataUnit> moduleRows;
	
	private PamguardXMLWriter xmlWriter;

	public LogXMLSettings(DBControl dbControl) {
		super(dbControl);
		xmlTableDef = new XMLSettingsTableDefinition("Module Effort");
		setTableDefinition(xmlTableDef);
		moduleRows = new HashMap<>();
		xmlWriter = PamguardXMLWriter.getXMLWriter();
	}

	@Override
	public void pamStart(PamConnection con) {
		long time = PamCalendar.getTimeInMillis();
		saveModuleSettings(con, time);
	}

	@Override
	public void pamStop(PamConnection con) {
		long time = PamCalendar.getTimeInMillis();
		updateModuleSettings(con, time);
	}

	private void saveModuleSettings(PamConnection con, long dataTime) {
		int n = PamController.getInstance().getNumControlledUnits();
		long now = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			saveModuleSettings(con, dataTime, now, PamController.getInstance().getControlledUnit(i));
		}
		
	}

	private void saveModuleSettings(PamConnection con, long dataTime, long now, PamControlledUnit controlledUnit) {
		if (controlledUnit instanceof PamSettings == false) {
			return;
		}
		PamSettings pamSettings = (PamSettings) controlledUnit;
		Serializable settings = pamSettings.getSettingsReference();
		Document doc = xmlWriter.writeOneModule(pamSettings, dataTime);
		String xmlString = xmlWriter.getAsString(doc, true);
		LogXMLDataUnit logXMLDataUnit = new LogXMLDataUnit(dataTime, now, pamSettings, xmlString);
				
		logData(logXMLDataUnit);
		int dbIndex = logXMLDataUnit.getDatabaseIndex();
		moduleRows.put(getModuleHash(pamSettings), logXMLDataUnit);
	}

	private void updateModuleSettings(PamConnection con, long dataTime) {
		int n = PamController.getInstance().getNumControlledUnits();
		long now = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			updateModuleSettings(con, dataTime, now, PamController.getInstance().getControlledUnit(i));
		}
		
	}
	
	private void updateModuleSettings(PamConnection con, long dataTime, long now, PamControlledUnit controlledUnit) {
		if (controlledUnit instanceof PamSettings == false) {
			return;
		}
		PamSettings pamSettings = (PamSettings) controlledUnit;
		LogXMLDataUnit logXMLDataUnit = moduleRows.get(getModuleHash(pamSettings));
		if (logXMLDataUnit == null) {
			return;
		}
		logXMLDataUnit.setDataEnd(dataTime);
		logXMLDataUnit.setProcessEnd(now);
		reLogData(con, logXMLDataUnit);
	}

	private String getModuleHash(PamSettings pamSettings) {
		if (pamSettings == null) {
			return null;
		}
		return pamSettings.getUnitName()+pamSettings.getUnitType();
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		LogXMLDataUnit logXMLDataUnit = (LogXMLDataUnit) pamDataUnit;
		PamSettings pamSettings = logXMLDataUnit.getPamSettings();
		
		xmlTableDef.getDataStart().setValue(sqlTypes.getTimeStamp(pamDataUnit.getTimeMilliseconds()));
		xmlTableDef.getDataEnd().setValue(sqlTypes.getTimeStamp(logXMLDataUnit.getDataEnd()));
		xmlTableDef.getProcessStart().setValue(sqlTypes.getTimeStamp(logXMLDataUnit.getProcessTime()));
		xmlTableDef.getProcessEnd().setValue(sqlTypes.getTimeStamp(logXMLDataUnit.getProcessEnd()));
		xmlTableDef.getName().setValue(pamSettings.getUnitName());
		xmlTableDef.getType().setValue(pamSettings.getUnitType());
		xmlTableDef.getPamGuardVersion().setValue(PamguardVersionInfo.version);
		xmlTableDef.getSettingsVersion().setValue(pamSettings.getSettingsVersion());
		xmlTableDef.getXmlSettings().setValue(logXMLDataUnit.getXml());
	}

}
