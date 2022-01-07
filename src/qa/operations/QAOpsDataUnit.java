package qa.operations;

import PamguardMVC.PamDataUnit;
import generalDatabase.lookupTables.LookupItem;;

public class QAOpsDataUnit extends PamDataUnit {

	private String opsStatusCode;
	private String opsStatusName;

	/**
	 * @param timeMilliseconds
	 */
	public QAOpsDataUnit(long timeMilliseconds, String opsStatusCode, String opsStatusName) {
		super(timeMilliseconds);
		this.opsStatusCode = opsStatusCode;
		this.opsStatusName = opsStatusName;
	}
	
	public QAOpsDataUnit(long timeMillseconds, LookupItem lutItem) {
		this(timeMillseconds, lutItem.getCode(), lutItem.getText());
	}

	/**
	 * @return the opsStatusCode
	 */
	public String getOpsStatusCode() {
		return opsStatusCode;
	}

	/**
	 * @param opsStatusCode the opsStatusCode to set
	 */
	public void setOpsStatusCode(String opsStatusCode) {
		this.opsStatusCode = opsStatusCode;
	}

	/**
	 * @return the opsStatusName
	 */
	public String getOpsStatusName() {
		return opsStatusName;
	}

	/**
	 * @param opsStatusName the opsStatusName to set
	 */
	public void setOpsStatusName(String opsStatusName) {
		this.opsStatusName = opsStatusName;
	}


}
