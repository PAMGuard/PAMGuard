package PamguardMVC.uid;

public class UIDStatusReport {
	
	public static final int UID_NO_DATA = 0;
	public static final int UID_ALL_OK = 1;
	public static final int UID_ABSENT = 2;
	public static final int UID_PARTIAL = 3;
	public static final int UID_DAMAGED = 4;

	long okUIDs = 0;
	
	long missingUIDs = 0;
	
	int uidStatus = 0;
	
	long largestUID = 0;

	/**
	 * @param okUIDs
	 * @param missingUIDs
	 * @param largestUID
	 * @param uidStatus
	 */
	public UIDStatusReport(long okUIDs, long missingUIDs, long largestUID) {
		super();
		this.okUIDs = okUIDs;
		this.missingUIDs = missingUIDs;
		this.largestUID = largestUID;
		this.uidStatus = getStatus(okUIDs, missingUIDs, largestUID);
	}
	
	/**
	 * @param okUIDs
	 * @param missingUIDs
	 * @param largestUID
	 * @param uidStatus
	 */
	public UIDStatusReport(long okUIDs, long missingUIDs, long largestUID, int uidStatus) {
		super();
		this.okUIDs = okUIDs;
		this.missingUIDs = missingUIDs;
		this.largestUID = largestUID;
		this.uidStatus = uidStatus;
	}
	
	public static int getStatus(long okUIDs, long missingUIDs, long largestUID) {
		if (okUIDs == 0 && missingUIDs == 0) {
			return UID_NO_DATA;
		}
		if (okUIDs == 0 && missingUIDs > 0) {
			return UID_ABSENT;
		}
		if (okUIDs > 0 && missingUIDs == 0) {
			return UID_ALL_OK;
		}
		if (okUIDs > 0 && missingUIDs > 0) {
			return UID_PARTIAL;
		}
		return -1;
	}

	/**
	 * @return the okUIDs
	 */
	public long getOkUIDs() {
		return okUIDs;
	}

	/**
	 * @param okUIDs the okUIDs to set
	 */
	public void setOkUIDs(long okUIDs) {
		this.okUIDs = okUIDs;
	}

	/**
	 * @return the missingUIDs
	 */
	public long getMissingUIDs() {
		return missingUIDs;
	}

	/**
	 * @param missingUIDs the missingUIDs to set
	 */
	public void setMissingUIDs(long missingUIDs) {
		this.missingUIDs = missingUIDs;
	}

	/**
	 * @return the uidStatus
	 */
	public int getUidStatus() {
		return uidStatus;
	}

	/**
	 * @param uidStatus the uidStatus to set
	 */
	public void setUidStatus(int uidStatus) {
		this.uidStatus = uidStatus;
	}

	/**
	 * @return the largestUID
	 */
	public long getLargestUID() {
		return largestUID;
	}

	/**
	 * @param largestUID the largestUID to set
	 */
	public void setLargestUID(long largestUID) {
		this.largestUID = largestUID;
	}

	/**
	 * Add the counts of another result to this one. 
	 * @param result
	 */
	public void addResult(UIDStatusReport result) {
		if (result == null) {
			return;
		}
		this.okUIDs += result.okUIDs;
		this.missingUIDs += result.missingUIDs;
		this.largestUID = Math.max(this.largestUID, result.largestUID);
		this.uidStatus = getStatus(okUIDs, missingUIDs, largestUID);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("OK UID %d; Missing UID %d; Largest UID %d; status %d" , okUIDs, missingUIDs, largestUID, uidStatus);
	}

}
