package generalDatabase.external;

import java.sql.Types;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;

public class DestinationTableDefinition extends CopyTableDefinition {

	private PamTableItem copyIdItem;

	public DestinationTableDefinition(String tableName) {
		super(tableName);
		addTableItem(copyIdItem = new PamTableItem("CopyId", Types.INTEGER));
	}

	/**
	 * @return the copiIdItem
	 */
	public PamTableItem getCopyIdItem() {
		return copyIdItem;
	}

}
