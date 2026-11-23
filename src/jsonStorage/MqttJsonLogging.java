package jsonStorage;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Log MQTT / Json data to the database. This will create a standard table with two columns
 * one for the topic and one for the json payload. 
 */
public class MqttJsonLogging extends SQLLogging {
		
	private PamTableItem jsonTopic, jsonData;

	/**
	 * Construct json logging into a table that uses the datablocks .getDataName() for the table name. 
	 * @param pamDataBlock
	 */
	public MqttJsonLogging(PamDataBlock pamDataBlock) {
		this(pamDataBlock, null);
	}
	
	/**
	 * Construct json logging with a bespoke table name
	 * @param pamDataBlock
	 * @param tableName
	 */
	public MqttJsonLogging(PamDataBlock pamDataBlock, String tableName) {
		super(pamDataBlock);
		
		this.setUpdatePolicy(UPDATE_POLICY_WRITENEW);
				
		if (tableName == null) {
			tableName = pamDataBlock.getDataName();
		}
		PamTableDefinition tableDef = new PamTableDefinition(tableName);
		
		tableDef.addTableItem(jsonTopic = new PamTableItem("MQTTTopic", Types.VARCHAR));
		tableDef.addTableItem(jsonData = new PamTableItem("JsonData", Types.VARCHAR));
		
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		JSONObjectDataSource jsonDataSource = getPamDataBlock().getJSONDataSource();
		String str = jsonDataSource.getPackedObject(pamDataUnit);
		jsonTopic.setValue(jsonDataSource.getMQTTTopic(pamDataUnit));
		jsonData.setValue(str);
	}
	
}
