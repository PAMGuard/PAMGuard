package binaryFileStorage;

import java.util.Timer;
import java.util.TimerTask;

import PamUtils.PamCalendar;
import PamguardMVC.PamProcess;

public class BinaryStoreProcess extends PamProcess {

	private BinaryStore binaryStore;

	private long startTime;

	private long nextFileTime;

	private Timer timer;

	private Object timerSynch = new Object();

	public BinaryStoreProcess(BinaryStore binaryStore) {
		super(binaryStore, null);
		this.binaryStore = binaryStore;
	}


	@Override
	public String getProcessName() {
		return "Binary store file control";
	}

	public synchronized void checkFileTime(long masterClockTime) {
		//		if (binaryStore.binaryStoreSettings.autoNewFiles && 
		//				 PamCalendar.getTimeInMillis() >= nextFileTime) {
		//			startNewFiles();
		//		}
		if (binaryStore.binaryStoreSettings.autoNewFiles && 
				masterClockTime >= nextFileTime) {
			startNewFiles(masterClockTime);
		}

	}

	private synchronized void startNewFiles(long masterClockTime) {
		nextFileTime += binaryStore.binaryStoreSettings.fileSeconds * 1000;
		binaryStore.reOpenStores(BinaryFooter.END_FILETOOLONG, masterClockTime);
	}


	@Override
	public void pamStart() {
		startTime = PamCalendar.getTimeInMillis();
		long round = binaryStore.binaryStoreSettings.fileSeconds * 1000;
		nextFileTime = (startTime/round) * round + round;
		//		System.out.println("Next file start at " + PamCalendar.formatDateTime(nextFileTime));
	}

	public void checkFileTimer() {
		boolean needTimer = !PamCalendar.isSoundFile();
		if (needTimer) {
			startTimer();
		}
		else {
			stopTimer();
		}
	}
	
	private void startTimer() {
		synchronized (timerSynch) {
			if (timer == null) {
				timer = new Timer();
				timer.schedule(new FileTimerTask(), 1000, 1000);
			}
		}
	}
	
	private void stopTimer() {
		synchronized (timerSynch) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}



	//	@Override
	//	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
	//		super.masterClockUpdate(timeMilliseconds, sampleNumber);
	//		checkFileTime(timeMilliseconds);
	//	}

	class FileTimerTask extends TimerTask {
		@Override
		public void run() {
			checkFileTime(PamCalendar.getTimeInMillis());
		}
	}

	@Override
	public void pamStop() {
		stopTimer();
	}

}
