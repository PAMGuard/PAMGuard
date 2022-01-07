package Acquisition;

import java.io.IOException;
import java.util.ArrayList;

import clickDetector.WindowsBuffer;

public class WavHeader {

	byte[] riffChunkId;
	long riffChunkSize;
	byte[] riffFormat;

	byte[] fmtId;
	int fmtSize;
	int audioformat;
	int nChannels;
	int sampleRate;
	int byteRate;
	int blockAlign;
	int bitsPerSamples;

	byte[] dataId;
	long dataSize;
	long dataStart;

	ArrayList<WavChunkData> otherChunks;

	public void addChunkData(WavChunkData chunkData) {
		if (otherChunks == null) {
			otherChunks = new ArrayList<WavChunkData>();
		}
		otherChunks.add(chunkData);
		int t = chunkData.getType();
		if (t == WavChunkData.CHUNK_FMT) {
			unpackFmtData(chunkData);
		}

	}

	private boolean unpackFmtData(WavChunkData chunkData) {
		// read the 12 bytes of data from the byte array. 
		if (chunkData.chunkSize < 12) {
			return false;
		}
		WindowsBuffer winBuff = new WindowsBuffer(chunkData.chunkData);
		
		try {
			this.audioformat = winBuff.readWinShort();
			this.nChannels = winBuff.readWinShort();
			this.sampleRate = winBuff.readWinInt();
			this.byteRate = winBuff.readWinInt();
			this.blockAlign = winBuff.readWinShort();
			this.bitsPerSamples = winBuff.readWinShort();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Check the two size informations in the wav file. 
	 * The first in the second 4 bytes in the RIFF chunk
	 * which should be equal to the number of bytes in the file - 4
	 * The second is the data length, which should be equal to the 
	 * length of the file - the data start point. 
	 * @param length
	 */
	public boolean checkSizes(long length) {
		boolean ok = true;
		if (length != riffChunkSize+8) {
//			System.out.println(String.format("RIFF Chunk Bytes difference = %d", length-riffChunkSize));
			ok = false;
		}
		if (length != dataStart + dataSize) {
//			System.out.println(String.format("Data bytes different = %d expected %d" , length - dataSize, dataStart));
			ok = false;
		}
		return ok;
	}
		
	/** 
	 * Write the correct sizes into the file data. 
	 * @param length total length of file in bytes.  
	 */
	public void repairSizes(long length) {
		riffChunkSize = length - 8;
		dataSize = length - dataStart;
	}
}
