package generalDatabase;

public class ColumnMetaData {

	/*
	 * Columns in the metadata are 
	 * [TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME, COLUMN_SIZE, BUFFER_LENGTH, 
	 * DECIMAL_DIGITS, NUM_PREC_RADIX, NULLABLE, REMARKS, COLUMN_DEF, SQL_DATA_TYPE, SQL_DATETIME_SUB, 
	 * CHAR_OCTET_LENGTH, ORDINAL_POSITION, IS_NULLABLE, SCOPE_CATLOG, SCOPE_SCHEMA, SCOPE_TABLE, 
	 * SOURCE_DATA_TYPE, IS_AUTOINCREMENT, IS_GENERATEDCOLUMN]
	 */
	enum METACOLNAMES {TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME, COLUMN_SIZE, 
		BUFFER_LENGTH, DECIMAL_DIGITS, NUM_PREC_RADIX, NULLABLE, REMARKS, COLUMN_DEF, SQL_DATA_TYPE, SQL_DATETIME_SUB, 
		CHAR_OCTET_LENGTH, ORDINAL_POSITION, IS_NULLABLE, SCOPE_CATLOG, SCOPE_SCHEMA, SCOPE_TABLE, SOURCE_DATA_TYPE, 
		IS_AUTOINCREMENT, IS_GENERATEDCOLUMN}
	
	private String tableName;
	private String columnName;
	private int type;
	private String typeName;
	private int size;
	private int position;
	private String catalog;
	private boolean autoIncrement;

	public ColumnMetaData(String tableName, String columnName, int type) {
		this.tableName = tableName;
		this.columnName = columnName;
		this.type = type;
	}

}
