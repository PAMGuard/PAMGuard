package generalDatabase;

import java.sql.Types;

import PamguardMVC.PamConstants;

public class XMLSettingsTableDefinition extends EmptyTableDefinition {
	
	private PamTableItem dataStart, dataEnd, processStart, processEnd, type, name, pamGuardVersion, settingsVersion, xmlSettings;
	
	public XMLSettingsTableDefinition(String tableName) {
		super(tableName, SQLLogging.UPDATE_POLICY_WRITENEW);
		pamTableItems.add(dataStart = new PamTableItem("Data Start", Types.TIMESTAMP, "Data start time"));
		pamTableItems.add(dataEnd = new PamTableItem("Data End", Types.TIMESTAMP, "Data end time"));
		pamTableItems.add(processStart = new PamTableItem("Process Start", Types.TIMESTAMP, "Process start time"));
		pamTableItems.add(processEnd = new PamTableItem("Process End", Types.TIMESTAMP, "Process end time"));
		addTableItem(type = new PamTableItem("unitType", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		addTableItem(name = new PamTableItem("unitName", Types.CHAR, PamConstants.MAX_ITEM_NAME_LENGTH));
		addTableItem(pamGuardVersion = new PamTableItem("PAMGuardVersion", Types.INTEGER));
		addTableItem(settingsVersion = new PamTableItem("SettingsVersion", Types.INTEGER));
		addTableItem(xmlSettings = new PamTableItem("XMLSettings", Types.VARCHAR));		
		setUseCheatIndexing(false);
	}


	/**
	 * @return the dataStart
	 */
	public PamTableItem getDataStart() {
		return dataStart;
	}


	/**
	 * @return the dataEnd
	 */
	public PamTableItem getDataEnd() {
		return dataEnd;
	}


	/**
	 * @return the processStart
	 */
	public PamTableItem getProcessStart() {
		return processStart;
	}


	/**
	 * @return the processEnd
	 */
	public PamTableItem getProcessEnd() {
		return processEnd;
	}


	/**
	 * @return the type
	 */
	public PamTableItem getType() {
		return type;
	}

	/**
	 * @return the name
	 */
	public PamTableItem getName() {
		return name;
	}

	/**
	 * @return the pamGuardVersion
	 */
	public PamTableItem getPamGuardVersion() {
		return pamGuardVersion;
	}

	/**
	 * @return the settingsVersion
	 */
	public PamTableItem getSettingsVersion() {
		return settingsVersion;
	}

	/**
	 * @return the xmlSettings
	 */
	public PamTableItem getXmlSettings() {
		return xmlSettings;
	}


}
