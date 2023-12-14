package tethys.pamdata;

import org.w3c.dom.Document;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBSchemaWriter;
import generalDatabase.SQLLogging;

/**
 * Automatically provides Tethys data based on the SQL database interface 
 * for a data block. 
 * @author dg50
 *
 */
public class AutoTethysProvider implements TethysDataProvider {

	private PamDataBlock pamDataBlock;

	public AutoTethysProvider(PamDataBlock pamDataBlock) {
		this.pamDataBlock = pamDataBlock;
	}

	@Override
	public TethysSchema getSchema() {
		SQLLogging logging = pamDataBlock.getLogging();
		if (logging == null) {
			return null;
		}
		DBSchemaWriter schemaWriter = new DBSchemaWriter();
		Document doc = schemaWriter.generateDatabaseSchema(pamDataBlock, logging, logging.getTableDefinition());
		TethysSchema schema = new TethysSchema(doc);
		return schema;
	}

	@Override
	public TethysDataPoint getDataPoint(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

}
