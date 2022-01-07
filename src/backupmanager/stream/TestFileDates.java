package backupmanager.stream;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import PamUtils.PamAudioFileFilter;
import PamUtils.PamCalendar;

public class TestFileDates {

	/**
	 * Quick prog to test file data information and see how often it's updating in the system
	 * @param args
	 */
	public static void main(String[] args) {
		new TestFileDates().run();
	}

	private void run() {
		long maxGap = 0;
		String folder = "C:\\PamguardTest\\Gemini\\PAMRecordings\\20201201";
		File folderFile = new File(folder);
		FileFilter fileFilter = new PamAudioFileFilter();
		while(true) {
			String[] files = folderFile.list();
			int n = files.length;
			File lastFile = new File(folder, files[n-1]);
			long created = 0, modified = 0;
			try {
				 BasicFileAttributes attr = Files.readAttributes(lastFile.toPath(), BasicFileAttributes.class);
				 created = attr.creationTime().toMillis();
				 modified = attr.lastModifiedTime().toMillis();
				 
			} catch (IOException e) {
				
			}
			long now = System.currentTimeMillis();
			maxGap = Math.max(maxGap, now-modified);
			System.out.printf("file %s created %s, modified %s: %10d/%5d millis ago (max %d)\n", files[n-1], 
					PamCalendar.formatTime(created),  PamCalendar.formatTime(modified), now-created, now-modified, maxGap);
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
