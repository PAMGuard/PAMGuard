package offlineProcessing.logging;

import PamUtils.PamCalendar;
import offlineProcessing.TaskActivity;
import offlineProcessing.TaskMonitorData;
import offlineProcessing.TaskStatus;

public class OldTaskData extends TaskMonitorData {

	public long firstDataDate;
	
	public long processingStart, processingEnd;

	private String taskNote;
	
	/**
	 * 
	 * @param taskStatus completion status of task
	 * @param firstDataDate first data date
	 * @param lastDataDate last data date
	 * @param procStart date run started
	 * @param procEnd date run ended.
	 * @param taskNote 
	 */
	public OldTaskData(TaskStatus taskStatus, long firstDataDate, 
			 long lastDataDate, long procStart, long procEnd, String taskNote) {
		super(taskStatus, TaskActivity.IDLE, 0, 0, null, lastDataDate);
		this.firstDataDate = firstDataDate;
		this.processingStart = procStart;
		this.processingEnd = procEnd;
		this.taskNote = taskNote;
	}

	@Override
	public String toString() {
		String endStatus;
		if (this.taskStatus == null) {
			endStatus = "Unknown completion code. Possible crash";
		}
		else {
			endStatus = this.taskStatus.toString();
		}
		String str = String.format("Processed from %s to %s on %s. %s", PamCalendar.formatDateTime(firstDataDate),
				PamCalendar.formatDateTime(lastDataDate), PamCalendar.formatDateTime(this.processingStart), endStatus);
		if (taskNote != null) {
			str = str + "\n" + taskNote;
//			str = "<html>" + str + "\br" + taskNote + "<\\html>";
		}
		return str;
	}

	
}
