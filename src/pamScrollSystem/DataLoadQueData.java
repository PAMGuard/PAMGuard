package pamScrollSystem;

import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;

/**
 * Used in a simple store of datablocks queues for loading.
 * @author Doug
 *
 */
public class DataLoadQueData {

	private long dataStart;
	
	private long dataEnd;
	
	private PamDataBlock pamDataBlock;
	
	private boolean hasSubTable;

	public DataLoadQueData(PamDataBlock pamDataBlock, long dataStart,
			long dataEnd, boolean hasSubTable) {
		super();
		this.pamDataBlock = pamDataBlock;
		this.dataStart = dataStart;
		this.dataEnd = dataEnd;
		this.hasSubTable = hasSubTable;
	}


	protected long getDataStart() {
		return dataStart;
	}

	protected long getDataEnd() {
		return dataEnd;
	}

	protected PamDataBlock getPamDataBlock() {
		return pamDataBlock;
	}

	/**
	 * @return the hasSubTable
	 */
	public boolean isHasSubTable() {
		return hasSubTable;
	}

	/**
	 * @param hasSubTable the hasSubTable to set
	 */
	public void setHasSubTable(boolean hasSubTable) {
		this.hasSubTable = hasSubTable;
	}


	/**
	 * @param dataStart the dataStart to set
	 */
	public void setDataStart(long dataStart) {
		this.dataStart = dataStart;
	}


	/**
	 * @param dataEnd the dataEnd to set
	 */
	public void setDataEnd(long dataEnd) {
		this.dataEnd = dataEnd;
	}


	@Override
	public String toString() {
		String str = String.format("%s %s - %s", pamDataBlock.getLongDataName(),
				PamCalendar.formatDBDateTime(dataStart),PamCalendar.formatDBDateTime(dataEnd));
		return str;
	}
	
	
}
