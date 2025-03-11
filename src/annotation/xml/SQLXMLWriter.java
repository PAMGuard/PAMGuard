package annotation.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.sqlite.SqliteSQLTypes;

public class SQLXMLWriter<TDataAnnotation extends DataAnnotation<?>> implements AnnotationXMLWriter<TDataAnnotation> {

	private SQLLoggingAddon sqlAddon;
	
	private DataAnnotationType<TDataAnnotation> dataAnnotationType;

	private SqliteSQLTypes sqlTypes;
	
	private EmptyTableDefinition emptyTableDefinition;

	public SQLXMLWriter(DataAnnotationType<TDataAnnotation> dataAnnotationType) {
		this.dataAnnotationType = dataAnnotationType;
		sqlAddon = dataAnnotationType.getSQLLoggingAddon();
		sqlTypes = new SqliteSQLTypes();
		emptyTableDefinition = new EmptyTableDefinition("Dummy table");
		sqlAddon.addTableItems(emptyTableDefinition);
	}

	@Override
	public Element writeAnnotation(Document document, PamDataUnit pamDataUnit,
			TDataAnnotation annotation) {
		boolean wrote = sqlAddon.saveData(sqlTypes, emptyTableDefinition, pamDataUnit);
		if (wrote == false) {
			return null;
		}
		// now all the data will be in the named elements of the sqlAddon
//		Element el = document.createElement(dataAnnotationType.getAnnotationName());
//		Element el = document.createElement("annot");
		Element docEl = document.getDocumentElement();
		Element el;
		int nItems = emptyTableDefinition.getTableItemCount();
		// first one is id, which we'll ignore, do all the other ones
//		Element dataEl = document.createElement("data");
//		docEl.appendChild(dataEl);
		int added = 0;
		for (int i = 1; i < nItems; i++) {
//			if (1<2) {
//				continue;
//			}
			PamTableItem tableItem = emptyTableDefinition.getTableItem(i);
			String itemName = tableItem.getName();
			Object data = tableItem.getValue();
			if (data == null) {
				continue;
			}
			String dataStr = data.toString();
			el = document.createElement(itemName);
//			el.setNodeValue(dataStr);
			el.setTextContent(dataStr);
//			el.setAttribute(itemName, dataStr);
			docEl.appendChild(el);
			added++;
		}
		if (added == 0) {
			return null;
		}
				
		return docEl;
	}

}
