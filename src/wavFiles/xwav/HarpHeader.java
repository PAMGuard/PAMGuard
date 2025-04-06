package wavFiles.xwav;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import PamUtils.LittleEndianDataInputStream;
import PamUtils.PamCalendar;

public class HarpHeader {

	int harpSize;
	int xhdVersion;
	String firmwareVersion;
	String insId;
	String site;
	String experiment;// could be 8 in example  
	int diskSequenceNumber;
	String diskSerialNumber;
	int numRF;
	int longitude; // defo written as integers. guessing float*1e5. 
	int latitude;
	int depth;
	
	public ArrayList<HarpCycle> harpCycles;
	private int sampleRate;
	private short blockAlign;
	private short nChannels;
	
	private HarpHeader() {
		harpCycles = new ArrayList<>();
	}
	
	/**
	 * Unpack harp data junk from a xwav file. 
	 * @param chunkData
	 * @param blockAlign 
	 * @param sampleRate 
	 * @return
	 */
	public static HarpHeader readHarpHeader(byte[] chunkData, int sampleRate, short nChannels, short blockAlign) throws XWavException {
		/*
		 * Based on matlab code found at https://github.com/MarineBioAcousticsRC/Wav2XWav/blob/main/wrxwavhdX.m
		 */
		LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(chunkData));
		HarpHeader harpHeader = new HarpHeader();
//		new LittleEnd
		try {
			// add these three to the harp header since they are useful for calculating durations. 
			harpHeader.sampleRate = sampleRate;
			harpHeader.blockAlign = blockAlign;
			harpHeader.nChannels = nChannels;
			harpHeader.harpSize = chunkData.length;
			harpHeader.xhdVersion = dis.readUnsignedByte();
			harpHeader.firmwareVersion = readString(dis, 10);
			harpHeader.insId = readString(dis, 4);
			harpHeader.site = readString(dis, 4);
			harpHeader.experiment = readString(dis, 8);// could be 8 in example  
			harpHeader.diskSequenceNumber = dis.readUnsignedByte();
			harpHeader.diskSerialNumber = readString(dis, 8);
			harpHeader.numRF = dis.readUnsignedShort();
			harpHeader.longitude = dis.readInt(); // defo written as integers. guessing float*1e5. 
			harpHeader.latitude = dis.readInt();
			harpHeader.depth = dis.readShort();
			// skip 8. 
			dis.skip(8);
			/*
			 *  then read numRF chunks, each of which is 32 bytes. In this example, we
			 *  have harpSize = 29752, so expecting about (29752-50)/32
			 */
			long lastT = 0;
			for (int iRF = 0; iRF < harpHeader.numRF; iRF++) {
				// time is from datevec, so it's year, month ... second in the first six
				int[] dateVec = new int[7];
				for (int i = 0; i < 6; i++) {
					dateVec[i] = dis.readUnsignedByte();
				}
				dateVec[6] = dis.readUnsignedShort(); // number of millis.
				HarpCycle harpCycle = new HarpCycle();
				harpCycle.tMillis = dateVec2Millis(dateVec);
				harpCycle.byteLoc = dis.readUnsignedInt();
				harpCycle.byteLength = dis.readUnsignedInt();
				harpCycle.writeLength = dis.readUnsignedInt();
				harpCycle.sampleRate = dis.readUnsignedInt();
				harpCycle.gain = dis.readUnsignedByte();
				harpCycle.durationMillis = harpCycle.byteLength * 1000 / blockAlign / harpCycle.sampleRate;
				dis.skip(7);
				harpHeader.harpCycles.add(harpCycle);
//				if (lastT != 0) {
//					System.out.printf("%s length %d = %3.3fs, step = %dms\n", PamCalendar.formatDBDateTime(tMillis, true), byteLength, 
//							(double) byteLength / (double) sampleRate / 2., tMillis-lastT);
//				}
//				else {
//					System.out.printf("%s length %d = %3.3fs\n", PamCalendar.formatDBDateTime(tMillis, true), byteLength, 
//							(double) byteLength / (double) sampleRate / 2.);
//				}
			}
			
		} catch (IOException e) {
			throw new XWavException(e.getMessage());
		}
		harpHeader.consolodate();
		
		return harpHeader;
	}
	
	/**
	 * Consolodate the cycle information, merging any entries that clearly don't have
	 * any gap between them. 
	 * @return
	 */
	public int consolodate() {
		if (harpCycles == null) {
			return 0;
		}
		ArrayList<HarpCycle> cList = new ArrayList<>();
		if (harpCycles.size() == 0) {
			return 0;
		}
		HarpCycle current = harpCycles.get(0).clone();
		cList.add(current);
		for (int i = 1; i < harpCycles.size(); i++) {
			HarpCycle other = harpCycles.get(i);
			if (current.isConsecutive(other) && current.compatible(other)) {
				// extend the current cycle. 
				current.merge(other);
			}
			else {
				// start a new cycle. 
				current.isConsecutive(other);
				cList.add(current = other.clone());
			}
		}
		this.harpCycles = cList;
		
		return cList.size();
	}
	
	/**
	 * Convert datevec read from file to Java millis. 
	 * @param dateVec
	 */
	private static long dateVec2Millis(int[] dateVec) {
		// format is yy, mm, dd, hh, mm, ss, ms as int values. 
		Calendar c =  Calendar.getInstance();
		c.setTimeZone(PamCalendar.defaultTimeZone);
		c.clear();
		int yy = dateVec[0];
		if (yy < 90) {
			yy += 2000;
		}
		c.set(yy, dateVec[1]-1, dateVec[2], dateVec[3], dateVec[4], dateVec[5]);
		long millis = c.getTimeInMillis() + dateVec[6];
		return millis;
	}
	
	private static String readString(LittleEndianDataInputStream dis, int bytes) throws XWavException {
		byte[] data;
		try {
			data = dis.readNBytes(bytes);
			String str = new String(data);
			return str;
		} catch (IOException e) {
			throw new XWavException(e.getMessage());
		}
	}

}
