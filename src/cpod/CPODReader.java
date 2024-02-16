package cpod;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import PamUtils.PamCalendar;
import cpod.CPODUtils.CPODFileType;
import cpod.FPODReader.FPODdata;

/**
 * Read CPOD data.
 * <p>
 * Note this should, as much as possible not have any PAMGuard based code. 
 * 
 * @author Jamie Macaulay
 * @author Douglas Gillespie
 *
 */
public class CPODReader  {


	/**
	 * A new minute. Don;t think we need to do anything here.?
	 * @param byteData
	 */
	private static void processMinute(byte[] byteData) {
		// TODO Auto-generated method stub

	}

	public static CPODHeader readHeader(File cpFile) {

		BufferedInputStream bis = null;
		FileInputStream fileInputStream = null;

		try {
			bis = new BufferedInputStream(fileInputStream = new FileInputStream(cpFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return readHeader( bis,  CPODUtils.getFileType(cpFile));

	}



	public static CPODHeader readHeader(BufferedInputStream bis, CPODFileType cpFileType) {
		int bytesRead;
		byte[] headData = new byte[getHeadSize(cpFileType)];
		try {
			bytesRead = bis.read(headData);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (bytesRead != headData.length) {
			return null;
		}
		// read as a load of 4 byte integers and see what we get !
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(headData));
		int nShort = headData.length / 2;
		short[] shortData = new short[nShort];
		for (int i = 0; i < shortData.length; i++) {
			try {
				shortData[i] = dis.readShort();
				if (shortData[i] == 414) {
					//					System.out.println("Found id at %d" + i);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		dis = new DataInputStream(new ByteArrayInputStream(headData));
		int nFloat = headData.length / 4;
		float[] floatData = new float[nFloat];
		for (int i = 0; i < floatData.length; i++) {
			try {
				floatData[i] = dis.readFloat();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		dis = new DataInputStream(new ByteArrayInputStream(headData));
		int nInt = headData.length / 4;
		int[] intData = new int[nInt];
		for (int i = 0; i < nInt; i++) {
			try {
				intData[i] = dis.readInt();
				int bOff = i*4;
				int sOff = i*2;
				//				if (intData[i] > 0)
				//					System.out.println(String.format("%d, Int = %d, Float = %3.5f, Short = %d,%d, bytes = %d,%d,%d,%d", i, intData[i],
				//							floatData[i],
				//							shortData[sOff], shortData[sOff+1],
				//							headData[bOff], headData[bOff+1], headData[bOff+2], headData[bOff+3]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CPODHeader header = new CPODHeader(); 

		header.fileStart = CPODUtils.podTimeToMillis(intData[64]);
		header.fileEnd = CPODUtils.podTimeToMillis(intData[65]);
		// other times seem to be packed in ints 66 - 69. 
		header.podId = shortData[50];
		header.waterDepth = headData[8];

		return header;
	}


	/**
	 * Import a CPOD file. 
	 * @param cpFile - the CP1 or CP3 file. 
	 * @param from - the click index to save from. e.g. 100 means that only click 100 + in the file is saved
	 * @param maxNum - the maximum number to import
	 * @return the total number of clicks in  the file. 
	 */
	protected static ArrayList<CPODClick> importCPODFile(File cpFile, int from, int maxNum) {	
		
		if (cpFile==null) return null;

		//holds a map of the click train detections. 
		HashMap<Integer, CPODClassification> clickTrains = new 	HashMap<Integer, CPODClassification>();
		//the current classification
		CPODClassification cpodClassification;

		ArrayList<CPODClick> clicks = new ArrayList<CPODClick>();
		BufferedInputStream bis = null;
		int bytesRead;
		FileInputStream fileInputStream = null;
		long totalBytes = 0;

		CPODFileType cpFileType = CPODUtils.getFileType(cpFile); 

		try {
			bis = new BufferedInputStream(fileInputStream = new FileInputStream(cpFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		CPODHeader header = readHeader(bis, cpFileType); 
		if (header == null) {
			return null;
		};

		totalBytes = getHeadSize(cpFileType);
		int dataSize = getDataSize(cpFileType);
		byte[] byteData = new byte[dataSize];
		short[] shortData = new short[dataSize];
		int fileEnds = 0;
		boolean isClick;
		// first record is always a minute mark, so start
		// at -1 to avoid being skipped forward one minute. 
		int nClicks = 0, nMinutes = -1;
		try {
			while (true) {
				bytesRead = bis.read(byteData);
				for (int i = 0; i < bytesRead; i++) {
					shortData[i] = CPODUtils.toUnsigned(byteData[i]);
				}
				if (isFileEnd(byteData)) {
					fileEnds++;
				}
				else {
					fileEnds = 0;
				}
				if (fileEnds == 2) {
					break;
				}

				isClick = byteData[dataSize-1] != -2;
				if (isClick) {

					if (from<0 || (nClicks>from && nClicks<(from+maxNum))) {

						//System.out.println("Create a new CPOD click: ");
						CPODClick cpodClick = processCPODClick(nMinutes, shortData, header);


						if (cpFileType.equals(CPODFileType.CP3)) {

							short trainID = shortData[20];

//							short species =  (short) (shortData[36]  & (112 >> 4));

							short quality =  (short) (shortData[36]  & 3);
							
							short species =  (short) (shortData[36]  >> 3);
							
//														short species =  (short) ((shortData[19]  >> 2)  & 7);
//														short quality =  (short) (shortData[19]  & 3);

							//generate a unique train ID within the file			
							int trainUID = Integer.valueOf(String.format("%06d", nMinutes) + String.format("%d", trainID));

							//find the click train from the hash map - if it is not there, create a new one. 
							cpodClassification = clickTrains.get(trainUID);

							if (cpodClassification==null) {
								cpodClassification = new CPODClassification();
								cpodClassification.isEcho = false;
								cpodClassification.clicktrainID = trainUID;
								cpodClassification.species = CPODUtils.getCPODSpecies(species); 
								cpodClassification.qualitylevel = quality;

								clickTrains.put(trainUID, cpodClassification); 
//								long timeMillis = PamCalendar.msFromDate(2023, 1, 6, 22, 32, 41, 200);
//								if (cpodClick.getTimeMilliseconds()>timeMillis && cpodClick.getTimeMilliseconds()<(timeMillis+20000)) {
//									System.out.println("Click train ID: " + trainUID + " minutes: " + nMinutes + " species: " + species + " quality level: " + quality);
//									System.out.println(String.format("<p>QClass %d, SpClass %d", CPODUtils.getBits(shortData[19], (short) 0x3), 
//											CPODUtils.getBits(shortData[19], (short) 0b11100)));
//								}
							}
							cpodClick.setClassification(cpodClassification);
						}

						clicks.add(cpodClick);

						if (nClicks%100000==0) {
							System.out.println("CPOD data: " + nClicks + "  " + PamCalendar.formatDateTime(cpodClick.getTimeMilliseconds()) + "  " +cpodClick.getBw() + "  " +shortData[4] );
//							if (nClicks>0)return clicks; //FIXME
						}
					}

					nClicks++;
					//					// now remove the data unit from the data block in order to clear up memory.  Note that the remove method
					//					// saves the data unit to the Deleted-Items list, so clear that as well (otherwise we'll just be using
					//					// up all the memory with that one)
					//					dataBlock.remove(cpodClick);
					//					dataBlock.clearDeletedList();
				}
				else {
					nMinutes ++;
					processMinute(byteData);
				}
				totalBytes += dataSize;
			}
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(String.format("File read: Clicks %d, minutes %d", nClicks, nMinutes));

		return clicks;
	}

	public static int getHeadSize(CPODFileType fileType) {
		switch (fileType) {
		case CP1:
			return 360;
		case CP3:
			return 720;
		case FP1:
			return 1024;
		case FP3:
			return 1024;
		}
		return 0;
	}

	public static int getDataSize(CPODFileType fileType) {
		switch (fileType) {
		case CP1:
			return 10;
		case CP3:
			return 40;
		case FP1:
			return 16;
		case FP3:
			return 32;
		}
		return 0;
	}

	/**
	 * Holds an CPOD header information
	 * <p>
	 * Note that is pretty opaque what all this means. The important parameters have been commented. 
	 */
	public static class CPODHeader {

		public byte waterDepth;
		public short podId;
		public long fileEnd;
		public long fileStart;

	}

	/**
	 * Is it the end of the file ? 
	 * @param byteData
	 * @return true if all bytes == 255
	 */
	public static boolean isFileEnd(byte[] byteData) {
		for (int i = 0; i < byteData.length; i++) {
			//			if ((byteData[i] ^ 0xFF) != 0)  {
			//				return false;
			//			}
			if (byteData[i] != -1)  {
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a CPOD click object from CPOD data. 
	 * @param nMinutes
	 * @param shortData
	 * @param header
	 * @return
	 */
	private static CPODClick processCPODClick(int nMinutes, short[] shortData, CPODHeader header) {

		long minuteMillis = header.fileStart + nMinutes * 60000L;

		int t = shortData[0]<<16 | 
				shortData[1]<<8 |
				shortData[2]; // 5 microsec intervals !
		long tMillis = minuteMillis + t/200;



		// do a sample number within the file as 5us intervals
		long fileSamples = t + minuteMillis * 200;

		/*
		 * 
		 */
		return CPODClick.makeCPODClick(tMillis, fileSamples, shortData);
	}

	/**
	 * Test the program
	 * @param args
	 */
	public static void main(String[] args) {
		//		String filePath = "/Users/au671271/Library/CloudStorage/GoogleDrive-macster110@gmail.com/My Drive/PAMGuard_dev/CPOD/FPOD_NunBank/0866 NunBankB 2023 06 27 FPOD_6480 file0.FP1";

		String filePath = "D:\\Dropbox\\PAMGuard_dev\\tutorials\\CPOD_wav\\data\\Hyskeir\\CPOD\\0740 Hyskeir 2022 12 02 POD1655 file01.CP3";
		//		String filePath = "D:\\DropBox\\PAMGuard_dev\\CPOD\\FPOD_NunBank\\0866 NunBankB 2023 06 27 FPOD_6480 file0.FP3";

		File fpfile = new File(filePath); 


		importCPODFile( fpfile, 0, Integer.MAX_VALUE);


	}

}
