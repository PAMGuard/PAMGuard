package backupmanager.stream;

public class BasicStreamItem extends StreamItem {

	
	public BasicStreamItem(long databaseIndex, String name, Long startTime, Long endTime, Long size) {
		super(databaseIndex, name, startTime, endTime, size);
	}

//	public BasicStreamItem(String name, Long startTime, Long endTime) {
//		super(name, startTime, endTime);
//	}

}
