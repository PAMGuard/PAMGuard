package wavFiles.xwav;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Calendar;

import PamUtils.LittleEndianDataInputStream;
import PamUtils.PamCalendar;

public class HarpHeader {

	private HarpHeader() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Unpack harp data junk from a xwav file. 
	 * @param chunkData
	 * @return
	 */
	public static HarpHeader readHarpHeader(byte[] chunkData) throws XWavException {
		/*
		 * Based on matlab code found at https://github.com/MarineBioAcousticsRC/Wav2XWav/blob/main/wrxwavhdX.m
		 */
		LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(chunkData));
//		new LittleEnd
		try {
			int harpSize = chunkData.length;
			int xhdVersion = dis.readUnsignedByte();
			String firmwareVersion = readString(dis, 10);
			String insId = readString(dis, 4);
			String site = readString(dis, 4);
			String experiment = readString(dis, 8);// could be 8 in example  
			int diskSequenceNumber = dis.readUnsignedByte();
			String diskSerialNumber = readString(dis, 8);
			int numRF = dis.readUnsignedShort();
			int longitude = dis.readInt(); // defo written as integers. guessing float*1e5. 
			int latitude = dis.readInt();
//			float longitude = dis.readFloat();
//			float latitude = dis.readFloat();
			int depth = dis.readShort();
			// skip 8. 
			dis.skip(8);
			/*
			 *  then read numRF chunks, each of which is 32 bytes. In this example, we
			 *  have harpSize = 29752, so expecting about (29752-50)/32
			 */
			long lastT = 0;
			for (int iRF = 0; iRF < numRF; iRF++) {
				// time is from datevec, so it's year, month ... second in the first six
				int[] dateVec = new int[7];
				for (int i = 0; i < 6; i++) {
					dateVec[i] = dis.readUnsignedByte();
				}
				dateVec[6] = dis.readUnsignedShort(); // number of millis.
				long byteLoc = dis.readUnsignedInt();
				long byteLength = dis.readUnsignedInt();
				long writeLength = dis.readUnsignedInt();
				long sampleRate = dis.readUnsignedInt();
				int gain = dis.readUnsignedByte();
				dis.skip(7);
				long tMillis = dateVec2Millis(dateVec);
//				if (lastT != 0) {
//					System.out.printf("%s length %d = %3.3fs, step = %dms\n", PamCalendar.formatDBDateTime(tMillis, true), byteLength, 
//							(double) byteLength / (double) sampleRate / 2., tMillis-lastT);
//				}
//				else {
//					System.out.printf("%s length %d = %3.3fs\n", PamCalendar.formatDBDateTime(tMillis, true), byteLength, 
//							(double) byteLength / (double) sampleRate / 2.);
//				}
				lastT = tMillis;
			}
			
		} catch (IOException e) {
			throw new XWavException(e.getMessage());
		}
		
		
		return null;
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
