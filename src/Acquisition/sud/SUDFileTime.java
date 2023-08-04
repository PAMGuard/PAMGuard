package Acquisition.sud;

import java.io.File;

import org.pamguard.x3.sud.SudAudioInputStream;

public class SUDFileTime {

	private static long sudTime;
	
	private static String lastFilePath = "";
	/**
	 * Temp measure to get the time from the first available sud record. 
	 * @param file
	 * @return
	 */
	public static long getSUDFileTime(File file) {
		
		//System.out.println("Get sud file time: " + file.getName()); 
		
		if (file == null || file.exists() == false) {
			return Long.MIN_VALUE;
		}
		if (file.getName().toLowerCase().endsWith(".sud") == false) {
			return Long.MIN_VALUE;
		}
		String filePath = file.getAbsolutePath();
		if (filePath.equals(lastFilePath)) {
			return sudTime;
		}
		/**
		 * Open the sud file and read it until the first chunk arrive, get the time 
		 * from there and close it again. I don't really see another way. 
		 */
//		long t1 = System.currentTimeMillis();
		sudTime = Long.MIN_VALUE;
//		SudParams sudParams = new SudParams();
//		sudParams.saveMeta = false;
//		sudParams.saveWav = false;
		try {
//			
//			SudAudioInputStream sudAudioInputStream = SudAudioInputStream.openInputStream(file, sudParams, false);
//			if (sudAudioInputStream == null) {
//				return Long.MIN_VALUE;
//			}
//			SudFileMap sudMap = sudAudioInputStream.getSudMap();
//			if (sudMap == null) {
//				return Long.MIN_VALUE;
//			}
//			long t = sudMap.getFirstChunkTimeMillis();
		
			long t = SudAudioInputStream.quickFileTime(file);
			t=t/1000; //turn to milliseconds. 
			if (t != 0) {
				sudTime = t;
			}
		
		
//			sudAudioInputStream.addSudFileListener((chunkID, sudChunk)->{
//				ChunkHeader chunkHead = sudChunk.chunkHeader;
//				if (chunkHead == null || sudTime != Long.MIN_VALUE) {
//					return;
//				}
//				long millis = (long) chunkHead.TimeS*1000 + (long) chunkHead.TimeOffsetUs/1000;
//				if (millis > 0) {
//					sudTime = millis;
//					lastFilePath = filePath;
//				}
//			});
//			
//			while (sudAudioInputStream.available() > 0 && sudTime == Long.MIN_VALUE) {
//
//				//note this is reading bytes of uncompressed continuous recordings only. 
//				sudAudioInputStream.read();
//			}
//			
//			sudAudioInputStream.close();
//			long t2 = System.currentTimeMillis();
//			System.out.printf("SUD file time %s extracted in %d milliseconds\n", PamCalendar.formatDBDateTime(sudTime), t2-t1);
			
		} catch (Exception e) {
			System.err.println("Error getting time from SUD file: " + file + "  " + e.getMessage());
			e.printStackTrace();
		}
		
		return sudTime;
	}

	

}
