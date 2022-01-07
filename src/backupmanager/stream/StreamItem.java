package backupmanager.stream;

public abstract class StreamItem implements Comparable<StreamItem> {

	private String name;
	private Long startTime;
	private Long endTime;
	private long databaseIndex;
	
	private String actionMessage;
	private Long size;
	
	private String filterMessage;
	private boolean processIt;
	
	public String getFilterMessage() {
		return filterMessage;
	}

	public void setFilterMessage(String filterMessage) {
		this.filterMessage = filterMessage;
	}

	public boolean isProcessIt() {
		return processIt;
	}

	public void setProcessIt(boolean processIt) {
		this.processIt = processIt;
	}

	public StreamItem(String name, Long startTime, Long endTime, Long size) {
		this(-1, name, startTime, endTime, size);
	}
	
	public StreamItem(long databaseIndex, String name, Long startTime, Long endTime, Long size) {
		this.setDatabaseIndex(databaseIndex);
		this.name = name;
		this.startTime = startTime;
		this.endTime = endTime;
		this.setSize(size);
	}

	public String getName() {
		return name;
	}

	public Long getStartUTC() {
		return startTime;
	}

	public Long getEndUTC() {
		return endTime;
	}

	public long getDatabaseIndex() {
		return databaseIndex;
	}

	public void setDatabaseIndex(long databaseIndex) {
		this.databaseIndex = databaseIndex;
	}

	public String getActionMessage() {
		return actionMessage;
	}

	public void setActionMessage(String actionMessage) {
		this.actionMessage = actionMessage;
	}

	/**
	 * @return the size
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Long size) {
		this.size = size;
	}

	@Override
	public int compareTo(StreamItem other) {
		// try to compare their start times
		if (startTime != null && other.startTime != null) {
			int c = startTime.compareTo(other.startTime);
			if (c != 0) {
				return c;
			}
		}
		// otherwise compare their end times. 
		if (endTime != null && other.endTime != null) {
			int c = endTime.compareTo(other.endTime);
			if (c != 0) {
				return c;
			}
		}
		// otherwise give up
		return 0;
	}

	/**
	 * Adds / concatenates messages from file filters. 
	 * @param unitName
	 */
	public String addFilterMessage(String newMessage) {
		if (filterMessage == null) {
			filterMessage = newMessage;
		}
		else {
			if (!filterMessage.contains(newMessage)) {
				filterMessage += "; " + newMessage;
			}
		}
		return filterMessage;
	}
	
}
