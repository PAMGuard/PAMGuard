package generalDatabase;

import PamController.fileprocessing.StoreStatus;

public class DatabaseStoreStatus extends StoreStatus {

	private DBControlUnit dbControl;

	public DatabaseStoreStatus(DBControlUnit dbControl) {
		super(dbControl);
		this.dbControl = dbControl;
	}

	@Override
	public long getFreeSpace() {
		String name = dbControl.getDatabaseName(); // may not have the path, which is what we need. 
		return getFreeSpace(name); // this may not work, particularly for server based systems. 
	}

	public void testFirstDataTime(Long t) {
		if (t == null) {
			return;
		}
		if (getFirstDataTime() == null) {
			setFirstDataTime(t);
		}
		if (t < getFirstDataTime()) {
			setFirstDataTime(t);
		}
	}
	public void testLastDataTime(Long t) {
		if (t == null) {
			return;
		}
		if (getLastDataTime() == null) {
			setLastDataTime(t);
		}
		if (t > getLastDataTime()) {
			setLastDataTime(t);
		}
	}

}
