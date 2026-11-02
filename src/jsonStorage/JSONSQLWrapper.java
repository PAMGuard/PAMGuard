package jsonStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Wrapper for an SQLLogging class to write all the fields from a table into 
 * JSON structures for publication to Network Sender. 
 * @param <DataSource>
 */
public class JSONSQLWrapper<DataSource extends JSONObjectData> extends JSONObjectDataSource<DataSource> {

	private SQLLogging sqlLogging;

	private SQLTypes sqlTypes = new SQLTypes();

	private PamDataBlock dataBlock;

	private boolean skipNulls = true;

	/**
	 * Construct with an SQLLogging class, then it will simply
	 * convert all the SQL into a simple json structure. 
	 * @param sqlLogging
	 */
	public JSONSQLWrapper(SQLLogging sqlLogging) {
		this.sqlLogging = sqlLogging;
		dataBlock = sqlLogging.getPamDataBlock();
	}

	/**
	 * @return the skipNulls
	 */
	public boolean isSkipNulls() {
		return skipNulls;
	}

	/**
	 * @param skipNulls the skipNulls to set
	 */
	public void setSkipNulls(boolean skipNulls) {
		this.skipNulls = skipNulls;
	}

	@Override
	public String getPackedObject(PamDataUnit dataUnit) {
		PamTableDefinition tableDef = sqlLogging.getTableDefinition();
		// push the data into the val fields. 
		sqlLogging.fillTableData(sqlTypes, dataUnit, null);
		// generate a json structure from the values in the SQL fields
		JsonFactory jf = new JsonFactory();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonGenerator jg = jf.createGenerator(os);
			jg.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
			//			jg.useDefaultPrettyPrinter();
			jg.writeStartObject();
			jg.writeStringField("TableName", tableDef.getTableName());
			jg.writeStringField("LongDataName", dataBlock.getLongDataName());
			int nCol = tableDef.getTableItemCount();
			for (int i = 0; i < nCol; i++) {
				PamTableItem item = tableDef.getTableItem(i);
				String colName = item.getName();
				Object val = item.getValue();
				if (val == null && skipNulls) {
					continue;
				}
				else {
					writeField(jg, colName, val, item.getSqlType());
				}
			}

			jg.writeEndObject();
			jg.close();

		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String jsonString = os.toString();
//		System.out.println(jsonString);
		return jsonString;
	}

	/**
	 * need to work out which are numeric and other known types, otherwise use toString. 
	 * @param jg
	 * @param colName
	 * @param val
	 * @param sqlType
	 * @return
	 */
	private boolean writeField(JsonGenerator jg, String colName, Object val, int sqlType)  {
		try {
			if (val == null) {
				jg.writeNullField(colName);
				return true;
			}
			if (val instanceof Double) {
				jg.writeNumberField(colName, (Double) val);
				return true;
			}
			if (val instanceof Float) {
				jg.writeNumberField(colName, (Float) val);
				return true;
			}
			if (val instanceof Integer) {
				jg.writeNumberField(colName, (Integer) val);
				return true;
			}
			if (val instanceof Long) {
				jg.writeNumberField(colName, (Long) val);
				return true;
			}
			if (val instanceof Number) {
				try {
					Double d = Double.valueOf(val.toString());
					jg.writeNumberField(colName, d);
					return true;
				}
				catch (NumberFormatException e) {
					// don't return here, let it run on and write this as a string. 
				}
			}
			jg.writeStringField(colName, val.toString());
		}
		catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	protected DataSource initializeObjectData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub

	}

}
