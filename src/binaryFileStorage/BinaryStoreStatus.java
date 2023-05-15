package binaryFileStorage;

import PamController.fileprocessing.StoreStatus;

public class BinaryStoreStatus extends StoreStatus {

	private BinaryStore binaryStore;
	
	private BinaryHeader firstHeader;
	
	private BinaryFooter lastFooter;
	
	private BinaryFooter lastData;

	public BinaryStoreStatus(BinaryStore binaryStore) {
		super(binaryStore);
		this.binaryStore = binaryStore;
	}

	public BinaryStoreStatus(BinaryStore binaryStore, BinaryHeader firstHead, BinaryFooter lastFoot,
			BinaryFooter lastData) {
		super(binaryStore);
		this.binaryStore = binaryStore;
		this.firstHeader = firstHead;
		this.lastFooter = lastFoot;
		this.lastData = lastData;
	}


	@Override
	public Long getFirstDataTime() {
		if (firstHeader != null) {
			return firstHeader.getDataDate();
		}
		return null;
	}

	@Override
	public Long getLastDataTime() {
		if (lastData != null) {
			return lastData.getDataDate();
		}
		if (lastFooter != null) {
			return lastFooter.getDataDate();
		}
		return null;
	}

	/**
	 * @return the firstHeader
	 */
	public BinaryHeader getFirstHeader() {
		return firstHeader;
	}

	/**
	 * @param firstHeader the firstHeader to set
	 */
	public void setFirstHeader(BinaryHeader firstHeader) {
		this.firstHeader = firstHeader;
		if (firstHeader != null) {
			setFirstDataTime(firstHeader.getDataDate());
		}
		else {
			setFirstDataTime(null);
		}
	}

	/**
	 * @return the lastFooter
	 */
	public BinaryFooter getLastFooter() {
		return lastFooter;
	}

	/**
	 * @param lastFooter the lastFooter to set
	 */
	public void setLastFooter(BinaryFooter lastFooter) {
		this.lastFooter = lastFooter;
	}

	/**
	 * @return the lastData
	 */
	public BinaryFooter getLastData() {
		return lastData;
	}

	/**
	 * @param lastData the lastData to set
	 */
	public void setLastData(BinaryFooter lastData) {
		this.lastData = lastData;
		if (lastData != null) {
			setLastDataTime(lastData.getDataDate());
		}
		else {
			setLastDataTime(null);
		}
	}

	@Override
	public long getFreeSpace() {
		return getFreeSpace(binaryStore.getBinaryStoreSettings().getStoreLocation());
	}

	/**
	 * Looking overall for first header, last footers, etc. 
	 * @param blockStatus
	 */
	public void considerBlockStatus(BinaryStoreStatus blockStatus) {
		if (blockStatus == null) {
			return;
		}
		considerFirstHeader(blockStatus.firstHeader);
		considerLastFooter(blockStatus.lastFooter);
		considerLastData(blockStatus.lastData);
	}

	/**
	 * Take a footer for last data with the later date
	 * @param footer
	 */
	private void considerLastData(BinaryFooter footer) {
		if (footer == null || footer.getDataDate() == 0) {
			return;
		}
		if (lastData == null || lastData.getDataDate() == 0) {
			lastData = footer;
		}
		if (footer.getDataDate() > lastData.getDataDate()) {
			lastData = footer;
		}		
	}

	/**
	 * Take a footer for last footer with the later date
	 * @param footer
	 */
	private void considerLastFooter(BinaryFooter footer) {
		if (footer == null || footer.getDataDate() == 0) {
			return;
		}
		if (lastFooter == null || lastFooter.getDataDate() == 0) {
			lastFooter = footer;
		}
		if (footer.getDataDate() > lastFooter.getDataDate()) {
			lastFooter = footer;
		}		
	}

	/**
	 * Take a header for the first header with the earliest date. 
	 * @param header
	 */
	private void considerFirstHeader(BinaryHeader header) {
		if (header == null || header.getDataDate() == 0) {
			return;
		}
		if (this.firstHeader == null || firstHeader.getDataDate() == 0) {
			this.firstHeader = header;
		}
		if (header.getDataDate() < firstHeader.getDataDate()) {
			firstHeader = header;
		}
	}

}
