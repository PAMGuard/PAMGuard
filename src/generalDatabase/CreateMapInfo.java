package generalDatabase;

import PamguardMVC.PamDataBlock;
import pamViewFX.pamTask.PamTaskUpdate;

/**
 *  simple class for passing information about data map making
 *  from swing worker to dialog. 
 */
public class CreateMapInfo extends PamTaskUpdate {

	static public final int BLOCK_COUNT = 0;
	
	static public final int START_TABLE = 1;
		
	private int numBlocks;
	
	private String databaseName;
	
	private int tableNum;
	
	private String tableName;
	
	private PamDataBlock dataBlock;
	
	public CreateMapInfo(int numBlocks, String databaseName) {
		setStatus(BLOCK_COUNT);
		this.numBlocks = numBlocks;
		this.databaseName = databaseName;
	}
	
	public CreateMapInfo(int tableNum, PamDataBlock dataBlock, String tableName) {
		setStatus(START_TABLE);
		this.tableName = tableName;
		this.tableNum = tableNum;
		this.dataBlock = dataBlock;
	}
	
	public CreateMapInfo(int status) {
		setStatus(status); 
		this.tableNum = 0;
		this.numBlocks = 1;
	}
	
	/**
	 * @return the numBlocks
	 */
	public int getNumBlocks() {
		return numBlocks;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @return the tableNum
	 */
	public int getTableNum() {
		return tableNum;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the dataBlock
	 */
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}
	
	@Override
	public String getName() {
		return "Create Data Map";
	}

	@Override
	public double getProgress() {
		if (numBlocks==0) return 0; 
		return ((double) getTableNum()+1)/numBlocks;
	}
	
	
}
