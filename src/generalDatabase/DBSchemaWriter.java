package generalDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Types;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamUtils.XMLUtils;
import PamguardMVC.PamDataBlock;

/**
 * Functions for writing database table schema in a format
 * compatible with the Tethys schemas. 
 * @author dg50
 *
 */
public class DBSchemaWriter {

	public DBSchemaWriter() {
		// TODO Auto-generated constructor stub
	}

	public boolean writeSchema(File outputFolder, PamDataBlock dataBlock) {
		if (dataBlock == null) {
			return false;
		}
		SQLLogging logging = dataBlock.getLogging();
		if (logging == null) {
			return false;
		}

		PamTableDefinition tableDef = logging.getTableDefinition();
		exportDatabaseSchema(outputFolder, dataBlock, tableDef);
		
		return true;
	}

	private void exportDatabaseSchema(File outputFolder, PamDataBlock dataBlock, PamTableDefinition tableDef) {
		String tableName = tableDef.getTableName();
		File outputFile = new File(outputFolder, tableName+".xsd");
		Document doc = PamUtils.XMLUtils.createBlankDoc();
		Element el = doc.createElement("xs:schema");
		el.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
		el.setAttribute("targetNamespace", "http://tethys.sdsu.edu/schema/1.0");
		doc.appendChild(el);
		for (PamTableItem tableItem : tableDef.pamTableItems) {
			Element itemEl = doc.createElement("xs:element");
			itemEl.setAttribute("name", tableItem.getName());
			itemEl.setAttribute("type", sqlTypeToString(tableItem.getSqlType(), tableItem.getLength()));
			String documentation = tableItem.getDescription();
			if (documentation != null) {
				Element annotation = doc.createElement("xs:annotation");
				itemEl.appendChild(annotation);
				Element docEl = doc.createElement("xs:documentation");
				docEl.setTextContent(documentation);
				annotation.appendChild(docEl);
			}
			el.appendChild(itemEl);
		}
		
		
		try {
			XMLUtils.writeToFile(doc, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String sqlTypeToString(int sqlType, int length) {
		switch (sqlType) {
		case Types.ARRAY:
			return "ARRAY";
		case Types.BIGINT:
			return "xs:long";
		case Types.BINARY:
			return "BINARY";
		case Types.BIT:
			return "BIT";
		case Types.BLOB:
			return "BLOB";
		case Types.BOOLEAN:
			return "xs:boolean";
		case Types.CHAR:
			return "xs:string";
		case Types.CLOB:
			return "CLOB";
		case Types.DATALINK:
			return "DATALINK";
		case Types.DATE:
			return "xs:dateTime";
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.DISTINCT:
			return "DISTINCT";
		case Types.DOUBLE:
			return "xs:double";
		case Types.FLOAT:
			return "xs:float";
		case Types.INTEGER:
			return "xs:int";
		case Types.JAVA_OBJECT:
			return "JAVA_OBJECT";
		case Types.LONGVARBINARY:
			return "LONGVARBINARY(" + length + ")";
		case Types.LONGVARCHAR:
			return "LONGVARCHAR(" + length + ")";
		case Types.NULL:
			return "NULL";
		case Types.NUMERIC:
			return "NUMERIC";
		case Types.OTHER:
			return "OTHER";
		case Types.REAL:
			return "xs:float";
		case Types.REF:
			return "REF";
		case Types.SMALLINT:
			return "xs:short";
		case Types.STRUCT:
			return "STRUCT";
		case Types.TIME:
			return "TIME";
		case Types.TIMESTAMP:
			return "xs:dateTime";
		case Types.TINYINT:
			return "TINYINT";
		case Types.VARBINARY:
			return "VARBINARY(" + length + ")";
		case Types.VARCHAR:
			return "VARCHAR(" + length + ")";
		}
		return null;
	}
}
