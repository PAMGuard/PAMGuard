package d3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import PamUtils.FileParts;

public class D2TextTime {

	long tagReferenceTime = 0;

	public D2TextTime() {
		/*
		 * Convert the tag reference time string to a long ...
		 * 13:38:35 on 14 June 2003 
		 */
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");      
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
//			tagReferenceTime = dateFormat.parse("14/06/2003 13:38:35").getTime();
			tagReferenceTime = dateFormat.parse("01/01/1900 00:00:00").getTime();
		}
		catch (Exception e) {
			System.out.println("d2 reference date string cannot be parsed ! " + e.getLocalizedMessage());
		}
//		tagReferenceTime = 0;
	}

//	@Override
	public long getTimeFromFile(File file) {
		String wavFileName = file.getAbsolutePath();
		FileParts fp = new FileParts(file);
		String end = fp.getFileEnd();
		if (end == null) {
			return Long.MIN_VALUE;
		}
		if (!end.equalsIgnoreCase("wav")) {
			return Long.MIN_VALUE;
		}
		String txtFileName = wavFileName.replace(".wav", ".txt");
		txtFileName = txtFileName.replace(".WAV", ".txt");
		File txtFile = new File(txtFileName);
		if (!txtFile.exists()) {
			return Long.MIN_VALUE;
		}
		long fileTime = Long.MIN_VALUE;
		try {
			FileReader fileReader = new FileReader(txtFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			//			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = bufferedReader.readLine()) != null && fileTime == Long.MIN_VALUE) {
				//				stringBuffer.append(line);
				//				stringBuffer.append("\n");
				fileTime = interpretLine(line);
			}
			fileReader.close();
			//			System.out.println("Contents of file:");
			//			System.out.println(stringBuffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileTime;
	}

	private static String searchLine = "File recording started";

	/**
	 * Attempt to get a valid data from within a single line from the file. 
	 * @param line
	 * @return a millisecond data, or Long.Min_VALUE if it can't be interpreted.
	 */
	private long interpretLine(String line) {
		/*
		 * Sample file from Marjo is#FFSRD DTAG flexible file system extractor
% Chunk report for file gm143a03.dtg, 
% Starting at chip 3, block 1
% Recording number 162
% File recording started: cbe00ffd
% Error numbers:
%	CHUNK TOO LARGE      	1
%	UNKNOWN SOURCE ID    	2
%	BAD DATA CRC         	3
%	OUT OF SEQUENCE CHUNK	4
%	INTERPRETATION ERROR 	5
		 */
		if (line == null) {
			return Long.MIN_VALUE;
		}
		int p = line.indexOf(searchLine, 0);
		if (p < 0) {
			return Long.MIN_VALUE;
		}
		String hexStr = "0x"+ line.substring(p + searchLine.length() + 2).trim();
		long tVal = Long.MIN_VALUE;
		try {
			tVal = Long.decode(hexStr);
		}
		catch (NumberFormatException e) {
			return Long.MIN_VALUE;
		}
		if (tVal == Long.MIN_VALUE) {
			return Long.MIN_VALUE;
		}

		return tagReferenceTime + tVal * 1000;
	}

//	@Override
//	public boolean hasSettings() {
//		return false;
//	}
//
//	@Override
//	public boolean doSettings() {
//		return false;
//	}
//
//	@Override
//	public String getName() {
//		return "d2 file time";
//	}
//
//	@Override
//	public String getDescription() {
//		return "Extract time information from txt files accompanying d2 wav files";
//	}

}
