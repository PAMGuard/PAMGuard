package generalDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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

	private PamTableDefinition baseTableDefinition = new PamTableDefinition("PamTableDefinition");

	private PamTableDefinition parentTable;

	public DBSchemaWriter() {
		// TODO Auto-generated constructor stub
	}


	public boolean writeStandardTableDef(File outputFolder, PamDataBlock dataBlock) {
		PamTableDefinition aTable = new PamTableDefinition("PamStandardTable");
		parentTable = null;
		exportDatabaseSchema(outputFolder, dataBlock, null, aTable);
		parentTable = aTable;

		return true;
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
		tableDef = logging.getBaseTableDefinition();

		if (tableDef instanceof PamTableDefinition) {
			writeStandardTableDef(outputFolder, dataBlock);
		}
		else {
			parentTable = null;
		}

		exportDatabaseSchema(outputFolder, dataBlock, logging, tableDef);

		return true;
	}

	private void exportDatabaseSchema(File outputFolder, PamDataBlock dataBlock, SQLLogging logging, PamTableDefinition tableDef) {

		/**
		 * write a parent item, e.g. if tableDef is a sub class of PamTableDefinition
		 */
		//		String parentName = writeParentTableSchema(outputFolder, dataBlock, tableDef);

		String tableName = tableDef.getTableName();
		Document doc = PamUtils.XMLUtils.createBlankDoc();
		Element schemaEl = doc.createElement("xs:schema");
		schemaEl.setAttribute("xmlns:xs","http://www.w3.org/2001/XMLSchema");
		schemaEl.setAttribute("targetNamespace", "http://tethys.sdsu.edu/schema/1.0");		
		doc.appendChild(schemaEl);
		if (parentTable != null) {
			Element parentEl = doc.createElement("xs:include");
			parentEl.setAttribute("schemaLocation", parentTable.getTableName()+".xsd");
			schemaEl.appendChild(parentEl);
		}
		
		fillItemElement(doc, schemaEl, tableDef.pamTableItems);
		
		if (logging != null) {
			ArrayList<SQLLoggingAddon> annots = logging.getLoggingAddOns();
			if (annots != null) {
				for (SQLLoggingAddon addon : annots) {
					Element compEl = addAddonElement(doc, schemaEl, addon);
					if (compEl != null) {
						schemaEl.appendChild(compEl);
					}
				}
			}
		}

		try {
			File outputFile = new File(outputFolder, tableName+".xsd");
			XMLUtils.writeToFile(doc, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a complex element for a SQLLogging addon. 
	 * @param doc
	 * @param schemaEl
	 * @param addon
	 */
	private Element addAddonElement(Document doc, Element schemaEl, SQLLoggingAddon addon) {
		Element compEl = doc.createElement("xs:complexType");
		
		Element oEl = doc.createElement("xs:element");
		oEl.setAttribute("name", addon.getName());
		
		Element seqEl = doc.createElement("xs:sequence");
		
		
		PamTableDefinition mtTable = new PamTableDefinition(addon.getName());
		addon.addTableItems(mtTable);
		// this is a mess ! must avoid the standard items. 
		fillItemElement(doc, seqEl, mtTable.pamTableItems);
		
		
		compEl.appendChild(seqEl);
		
		oEl.appendChild(compEl);
		return oEl;
	}


	/**
	 * Fills information on items in a list into the given element
	 * This may be the schema element or may be a complex element. 
	 * @param doc
	 * @param schemaEl
	 * @param tableItems
	 */
	private void fillItemElement(Document doc, Element schemaEl, List<PamTableItem> tableItems) {
		for (PamTableItem tableItem : tableItems) {
			if (shouldSkip(tableItem, parentTable)) {
				// element is included in parent table, so skip it. 
				continue;
			}

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
			schemaEl.appendChild(itemEl);
		}
	}

	private boolean shouldSkip(PamTableItem tableItem, PamTableDefinition parentTable) {
		if (parentTable == null) {
			return false;
		}
		if (parentTable.findTableItem(tableItem.getName()) != null) {
			return true;
		}
		return false;
	}


	//	private String writeParentTableSchema(File outputFolder, PamDataBlock dataBlock, PamTableDefinition tableDef) {
	//		/**
	//		 * Write a schema of everything that is in the parent table, then include a reference to that
	//		 * schema. To do this, we need to a) establish if there is a table def parent, then go through 
	//		 * all the fields in THIS tableDef, see if the field exists in the parent. IF it exists in the 
	//		 * parent, write it in the parent doc, if it's only in this, write it in this doc. So far so good, but
	//		 * we need to also make this recursive so it can build layer on layer ? 
	//		 * Reaslistically, this is all too complicated ! Just write out the schema for the PamTableDefinition 
	//		 * at the start of everything, and skip those fields in everything else. This will be a bit ad-hoc, but 
	//		 * it will otherwise be a nightmare since we don't know which table items are in which class at this point. 
	//		 */
	//		Class tableClass = tableDef.getClass();
	//		Class parentClass = tableClass.getSuperclass();
	//		if (parentClass.isAssignableFrom(tableClass) == false) {
	//			return null;
	//		}
	//		
	//		
	//		return null;
	//	}

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
