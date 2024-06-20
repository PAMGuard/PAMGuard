package cpod;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import pamScrollSystem.ViewLoadObserver;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.RequestCancellationObject;

import dataGram.DatagramDataPoint;
import fileOfflineData.OfflineFileMapPoint;

@Deprecated
public class CPODMap {

	private File cpFile;
	private long fileStart;
	private long fileEnd;
	private short podId;
	private byte waterDepth;
	private CPODControl cpodControl;
	float[] tempDataGramData;
	
	public static final int FILE_CP1 = 1;
	public static final int FILE_CP3 = 3;
	
	int cpFileType = 0; 
	

	public CPODMap(CPODControl cpodControl, File cpFile, int fileType) {
		this.cpodControl = cpodControl;
		this.cpFile = cpFile;
		cpFileType = getFileType(cpFile);
		if (fileType != cpFileType) {
			System.err.println("CPOD Mismatched file type " + cpFile.getAbsolutePath());
		}
		mapFile();
	}
	
	public static int getFileType(File cpFile) {
		if (cpFile.getAbsolutePath().toLowerCase().endsWith("cp1")) {
			 return FILE_CP1;
		}
		else if (cpFile.getAbsolutePath().toLowerCase().endsWith("cp3")) {
			return FILE_CP3;
		}
		return 0;
	}
	
	public static int getHeadSize(int fileType) {
		switch (fileType) {
		case FILE_CP1:
			return 360;
		case FILE_CP3:
			return 720;
		}
		return 0;
	}
	
	public static int getDataSize(int fileType) {
		switch (fileType) {
		case FILE_CP1:
			return 10;
		case FILE_CP3:
			return 40;
		}
		return 0;
	}

	protected int mapFile() {
		long mapPointTime = 3600000L;
		BufferedInputStream bis = null;
		int bytesRead;
		FileInputStream fileInputStream = null;
		long totalBytes = 0;
		try {
			bis = new BufferedInputStream(fileInputStream = new FileInputStream(cpFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		CPODDataGramProvider datagram;
		if (readHeader(bis) == false) {
			return -2;
		};
		if (cpFileType == FILE_CP1) {
			 datagram = cpodControl.cpodDatagramProvider[0];
		}
		else {
			 datagram = cpodControl.cpodDatagramProvider[1];
		}
		
		totalBytes = getHeadSize(cpFileType);
		CPODDataMapPoint mapPoint = new CPODDataMapPoint(fileStart, fileStart+mapPointTime, 0, cpFile, totalBytes);
		CPODDataGram cpodDataGram = new CPODDataGram(600);
		mapPoint.setDatagram(cpodDataGram);
		
		long datagramGap = cpodDataGram.getIntervalSeconds() * 1000;
		DatagramDataPoint ddp = new DatagramDataPoint(cpodDataGram, fileStart, fileStart + datagramGap, 256);
		tempDataGramData = new float[256];
		// now work through the 10 or 20 byte objects. 
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
					shortData[i] = toUnsigned(byteData[i]);
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
					nClicks++;
					CPODClick cpodClick = processClick(nMinutes, shortData);
					mapPoint.setNDatas(mapPoint.getNDatas()+1);
					ddp = getDatagramPoint(cpodDataGram, ddp, cpodClick.getTimeMilliseconds());
					datagram.addDatagramData(cpodClick, tempDataGramData);
				}
				else {
					nMinutes ++;
					processMinute(byteData);
					long minuteTime = fileStart + nMinutes * 60000L;
					long mapPointEnd = Math.min(minuteTime + mapPointTime, fileEnd);
					if (nMinutes % 60 == 0) {
						cpodControl.getOfflineFileDataMap(cpFileType).addDataPoint(mapPoint);
						mapPoint = new CPODDataMapPoint(minuteTime, mapPointEnd, 0, cpFile, totalBytes);
						cpodDataGram = new CPODDataGram(600);
						mapPoint.setDatagram(cpodDataGram);
//						System.out.println("Minutes: " + nMinutes);
					}
				}
				totalBytes += dataSize;
			}
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(String.format("File read: Clicks %d, minutes %d", nClicks, nMinutes));
		cpodControl.getOfflineFileDataMap(cpFileType).addDataPoint(mapPoint);
		return 0;
	}
	
	private DatagramDataPoint getDatagramPoint(CPODDataGram cpodDataGram, DatagramDataPoint currentPoint, long unitTime) {
		while (unitTime >= currentPoint.getEndTime()) {
			float[] gd = currentPoint.getData();
			for (int i = 0; i < 256; i++) {
				gd[i] = (float) tempDataGramData[i];
				tempDataGramData[i] = 0;
			}
			cpodDataGram.addDataPoint(currentPoint);
			long newEnd = currentPoint.getEndTime() + cpodDataGram.getIntervalSeconds()*1000L;
			currentPoint = new DatagramDataPoint(cpodDataGram,
					newEnd-cpodDataGram.getIntervalSeconds()*1000L, newEnd, 256);
		}
		return currentPoint;
	}
	
	private void processMinute(byte[] byteData) {
		// TODO Auto-generated method stub
		
	}

	private CPODClick processClick(int nMinutes, short[] shortData) {
		/*
		 * 
		 */
		return CPODClick.makeClick(cpodControl, fileStart + nMinutes * 60000L, shortData);
	}
	
	/**
	 * Java will only have read signed bytes. Nick clearly
	 * uses a lot of unsigned data, so convert and inflate to int16. 
	 * @param signedByte
	 * @return unsigned version as int16. 
	 */
	static short toUnsigned(byte signedByte) {
		short ans = signedByte;
		if (ans < 0) {
			ans += 256;
		}
		return ans;
	}

	/**
	 * Is it the end of the file ? 
	 * @param byteData
	 * @return true if all bytes == 255
	 */
	static boolean isFileEnd(byte[] byteData) {
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

	boolean readHeader(BufferedInputStream bis) {
		int bytesRead;
		byte[] headData = new byte[getHeadSize(cpFileType)];
		try {
			bytesRead = bis.read(headData);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (bytesRead != headData.length) {
			return false;
		}
		// read as a load of 4 byte integers and see what we get !
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(headData));
		int nShort = headData.length / 2;
		short[] shortData = new short[nShort];
		for (int i = 0; i < shortData.length; i++) {
			try {
				shortData[i] = dis.readShort();
				if (shortData[i] == 414) {
					System.out.println("Found id at %d" + i);
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
				if (intData[i] > 0)
					System.out.println(String.format("%d, Int = %d, Float = %3.5f, Short = %d,%d, bytes = %d,%d,%d,%d", i, intData[i],
							floatData[i],
							shortData[sOff], shortData[sOff+1],
							headData[bOff], headData[bOff+1], headData[bOff+2], headData[bOff+3]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		fileStart = CPODControl.podTimeToMillis(intData[64]);
		fileEnd = CPODControl.podTimeToMillis(intData[65]);
		// other times seem to be packed in ints 66 - 69. 
		podId = shortData[50];
		waterDepth = headData[8];

		return true;
	}

	/**
	 * @return the fileStart
	 */
	public long getFileStart() {
		return fileStart;
	}

}
