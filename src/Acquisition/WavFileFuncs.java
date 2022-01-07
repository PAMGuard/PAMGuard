package Acquisition;

import java.io.File;
import java.io.IOException;

import clickDetector.WindowsFile;

public class WavFileFuncs extends AudioFileFuncs {


	public static int checkHeader(File aFile, boolean repair) {
		WindowsFile file;
		try {
			file = new WindowsFile(aFile.getAbsolutePath(), "r");
		} catch (IOException e) {
			//			e.printStackTrace();
			return FILE_CANTOPEN;
		}
		WavHeader wavHead = readWavHeader(file);
		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (wavHead.checkSizes(aFile.length()) == false) {
			wavHead.repairSizes(aFile.length());
			return writeNewSizes(wavHead, aFile);
		}

		return FILE_OK;
	}

	public static WavHeader readWavHeader(WindowsFile file) {
		int chunkType = -1;
		int chunkSize;
		long filePos;
		byte[] chunkId;
		byte[] chunkInfo;
		WavChunkData chunkData;
		WavHeader wavHead = new WavHeader();
		while (chunkType != WavChunkData.CHUNK_DATA) {
			try {
				filePos = file.getFilePointer();
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}
			chunkId = readChunkId(file);
			if (chunkId == null) {
				return null;
			}
			try {
				chunkSize = file.readWinInt();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			chunkType = WavChunkData.getChunkType(chunkId);

			if (chunkType == WavChunkData.CHUNK_RIFF) {
				wavHead.riffChunkSize = chunkSize;
				wavHead.riffFormat = readChunkId(file);
			}
			else if (chunkType == WavChunkData.CHUNK_DATA) {
				wavHead.dataSize = chunkSize;
				wavHead.dataStart = filePos + 8;
				break;
			}
			else {
				chunkInfo = new byte[chunkSize];
				try {
					file.read(chunkInfo);
				}
				catch (IOException e) {
					e.printStackTrace();
					return null;
				}
				wavHead.addChunkData(new WavChunkData(chunkSize, filePos, chunkId, chunkInfo));
			}
		}
		return wavHead;
	}

	private static void readRiffData(WavHeader wavHead, WindowsFile file) {
		wavHead.riffFormat = readChunkId(file);

	}

	private static void readFmtData(WavHeader wavHead, WindowsFile file) {
		try {
			wavHead.audioformat = file.readWinShort();
			wavHead.nChannels = file.readWinShort();
			wavHead.sampleRate = file.readWinInt();
			wavHead.byteRate = file.readWinInt();
			wavHead.blockAlign = file.readWinShort();
			wavHead.bitsPerSamples = file.readWinShort();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private static byte[] readChunkId(WindowsFile file) {

		byte[] chunkId = new byte[4]; 
		try {
			for (int i = 0; i < 4; i++) {
				chunkId[i] = file.readByte();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return chunkId;
	}

	private static int writeNewSizes(WavHeader wavHead, File aFile) {
		WindowsFile file;
		try {
			file = new WindowsFile(aFile.getAbsolutePath(), "rw");
		} catch (IOException e) {
			return FILE_CANTOPEN;
		}
		// only want to write the two sizes into RIFF and DATA chunks.
		if (wavHead.dataStart < 32) {
			return FILE_ERROR;
		}
		try {
			file.seek(4);
			file.writeWinInt((int)wavHead.riffChunkSize);
			file.seek(wavHead.dataStart-4);
			file.writeWinInt((int)wavHead.dataSize);
		} catch (IOException e) {
			return FILE_CANTOPEN;
		}

		try {
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return FILE_REPAIRED;
	}
}
