package PamController;

/**
 * Class for passing around data on how the load of 
 * data from the databsae is going. 
 * @author Doug
 *
 */
public class ViewerLoadProgress {

	public ViewerLoadProgress(String tableName, int totalLines, int linesRead) {
		super();
		this.tableName = tableName;
		this.totalLines = totalLines;
		this.linesRead = linesRead;
	}

	private String tableName;

	private int totalLines;

	private int linesRead;

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @return the totalLines
	 */
	public int getTotalLines() {
		return totalLines;
	}

	/**
	 * @return the linesRead
	 */
	public int getLinesRead() {
		return linesRead;
	}
}
