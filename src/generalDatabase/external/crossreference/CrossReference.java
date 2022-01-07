package generalDatabase.external.crossreference;

import java.util.ArrayList;

public class CrossReference {

	private String tableName1, columnName1, tableName2, columnName2;
	private String oldColName1;
	
	/**
	 * @param tableName1 Name of table containing the unique referenced items
	 * @param columnName1 Name of column containing the unique referenced items (always Id)
	 * @param oldColName1 Name of the original cross reference data (always CopyId)
	 * @param tableName2 Name of table referencing data in tableName1
	 * @param columnName2 Name of column referencing data in columnName1
	 */
	public CrossReference(String tableName1, String columnName1, String oldColName1,
			String tableName2, String columnName2) {
		super();
		this.tableName1 = tableName1;
		this.columnName1 = columnName1;
		this.oldColName1 = oldColName1;
		this.tableName2 = tableName2;
		this.columnName2 = columnName2;
	}

	/**
	 * @return the tableName1
	 */
	public String getTableName1() {
		return tableName1;
	}

	/**
	 * @return the columnName1
	 */
	public String getColumnName1() {
		return columnName1;
	}

	/**
	 * @return the tableName2
	 */
	public String getTableName2() {
		return tableName2;
	}

	/**
	 * @return the columnName2
	 */
	public String getColumnName2() {
		return columnName2;
	}

	/**
	 * @return the oldColName1
	 */
	public String getOldColName1() {
		return oldColName1;
	}

}
