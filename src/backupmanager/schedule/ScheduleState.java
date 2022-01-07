package backupmanager.schedule;

public enum ScheduleState {

	RUNNING, PAUSED;

	@Override
	public String toString() {
		switch (this) {
		case PAUSED:
			return "Paused";
		case RUNNING:
			return "Running";
		default:
			break;
		}
		return null;
	}
	
}
