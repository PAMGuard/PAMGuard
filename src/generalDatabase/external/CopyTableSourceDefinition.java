package generalDatabase.external;

import generalDatabase.PamTableItem;

public class CopyTableSourceDefinition extends CopyTableDefinition {

	public CopyTableSourceDefinition(String tableName) {
		super(tableName);
		this.tableName = tableName;
		removeTableItem(0); // get rid of the id item. 
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.EmptyTableDefinition#addTableItem(generalDatabase.PamTableItem)
	 */
	@Override
	public int addTableItem(PamTableItem pamTableItem) {
		/*
		 *  may need to reset the index item to be the id item. 
		 *  to support non - pamguard tables, the id item got removed, but for 
		 *  most tables will be put straight back in again, but we'll have lost 
		 *  the reference to the index item - so put it back ! 
		 */
		if (pamTableItem.getName().equalsIgnoreCase(getIndexItem().getName())) {
			setIndexItem(pamTableItem);
		}
		
		return super.addTableItem(pamTableItem);
	}

}
