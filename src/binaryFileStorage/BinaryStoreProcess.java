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

	public BinaryStoreProcess(BinaryStore binaryStore) {
		super(binaryStore, null);
		this.binaryStore = binaryStore;
	}

	
	@Override
	public String getProcessName() {
		return "Binary store file control";
	}
	
	public synchronized void checkFileTime() {
		if (binaryStore.binaryStoreSettings.autoNewFiles && 
				 PamCalendar.getTimeInMillis() >= nextFileTime) {
			startNewFiles();
		}
		
	}

	private synchronized void startNewFiles() {
		nextFileTime += binaryStore.binaryStoreSettings.fileSeconds * 1000;
		binaryStore.reOpenStores(BinaryFooter.END_FILETOOLONG);
	}


	@Override
	public void pamStart() {
		startTime = PamCalendar.getTimeInMillis();
		long round = binaryStore.binaryStoreSettings.fileSeconds * 1000;
		nextFileTime = (startTime/round) * round + round;
		// this was a print of the time of the NEXT binary file. Not really of interest. 
//		System.out.println("Next file start at " + PamCalendar.formatDateTime(nextFileTime));
		timer = new Timer();
		timer.schedule(new FileTimerTask(), 1000, 1000);
		
	}
	
	class FileTimerTask extends TimerTask {
		@Override
		public void run() {
			checkFileTime();
		}
	}

	@Override
	public void pamStop() {
		if (timer != null) {
			timer.cancel();
		}
	}

}
