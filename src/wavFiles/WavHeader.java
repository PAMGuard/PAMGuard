package wavFiles;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

import clickDetector.WindowsFile;
import wavFiles.xwav.HarpHeader;
import wavFiles.xwav.XWavException;

public class WavHeader {

	
	private int fmtTag;
	
	private short nChannels = 0, blockAlign = 0, bitsPerSample = 0;
	
	private int sampleRate = 0, bytesPerSec = 0;
	
	private boolean headerOk = false;

	private long dataStart;
	
	private long dataSize;
	
	private ArrayList<WavHeadChunk> wavHeadChunks = new ArrayList<WavHeadChunk>();

	private long headerSize;

	private HarpHeader harpHeader;
	
	
	/**
	 * Construct a blank Wav Header object, generally used when about to read a header from a file. 
	 */
	public WavHeader() {
	}
	
	/**
	 * Construct a WavHeader from a Java AudioFormat. 
	 * @param audioFormat Java audio format. 
	 */
	public WavHeader(AudioFormat audioFormat) {
		this.fmtTag = 1;
		this.nChannels = (short) audioFormat.getChannels();
		this.blockAlign = (short) audioFormat.getFrameSize();
		this.bitsPerSample = (short) audioFormat.getSampleSizeInBits();
		this.sampleRate = (int) audioFormat.getSampleRate();
		this.bytesPerSec = audioFormat.getFrameSize()*sampleRate;
		this.headerOk = true;
		this.dataStart = 0;
		this.dataSize = 0;
	}
	/**
	 * Read the header data from file. 
	 * @param windowsWavFile Windows file
	 * @return true if header unpacked successfully. 
	 */
	public boolean readHeader(WindowsFile windowsWavFile) {

		headerOk = false;
		if (windowsWavFile == null) {
			return false;
		}
		
		try {
			windowsWavFile.seek(0);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		char riff[] = new char[4];
		long totalSize;
		char wave[] = new char[4];
		char dataHead[];
		char testChars[];
		String testString;
		long filePointer;
		int intDataSize;
		int fmtSize;
		long fmtEnd;
		int chunkSize = 0;
		byte[] headChunk;
		try {
			riff = read4Chars(windowsWavFile);
			totalSize = windowsWavFile.readWinInt();
			totalSize = checkUintProblem(totalSize);
			wave = read4Chars(windowsWavFile);
			while (true) {
				// look for the fmt chunk and skip all other chunks in the 
				// header. 
				filePointer = windowsWavFile.getFilePointer();
				testChars = read4Chars(windowsWavFile);
				testString = new String(testChars);
				if (testString.equals("data")) {
					break;
				}
				if (testString.equals("fmt ")) {

					// should now be at the start of the format section
					fmtSize = windowsWavFile.readWinInt();
					fmtEnd = windowsWavFile.getFilePointer() + fmtSize;
					fmtTag =  windowsWavFile.readWinUShort();
					nChannels = (short) windowsWavFile.readWinShort();
					sampleRate = windowsWavFile.readWinInt();
					bytesPerSec = windowsWavFile.readWinInt();
					blockAlign = (short) windowsWavFile.readWinShort();
					bitsPerSample = (short) windowsWavFile.readWinShort();
					long currPos = windowsWavFile.getFilePointer();
					int toRead = (int) (fmtEnd-currPos);
					byte[] more = new byte[toRead];
					windowsWavFile.read(more);
					
					windowsWavFile.seek(fmtEnd);
					//			break;
				}
				else if (testString.equals("harp")) {
					chunkSize = windowsWavFile.readWinInt();
					headChunk = new byte[chunkSize];
					windowsWavFile.read(headChunk);
					HarpHeader harpHeader = null;
					try {
						harpHeader = HarpHeader.readHarpHeader(headChunk, sampleRate, nChannels, blockAlign);
					} catch (XWavException e) {
						e.printStackTrace();
					}
					this.harpHeader = harpHeader;
//					wavHeadChunks.add(new WavHeadChunk(testString, headChunk));
				}
				else {
					/*
					 * As an example, SCRIPPS HARP .x.wav files have a chunk 
					 * in here called 'harp', (now dealt with above) an example of which has 29752
					 * bytes data, beginning  'V2.64 D104NO01CHNMS ...'
					 */
					chunkSize = windowsWavFile.readWinInt();
					headChunk = new byte[chunkSize];
					windowsWavFile.read(headChunk);
					wavHeadChunks.add(new WavHeadChunk(testString, headChunk));
				}
//				windowsWavFile.seek(windowsWavFile.getFilePointer() + chunkSize);
			}
//			dataHead = read4Chars(windowsWavFile); // should be 'data'
//			String dataString = new String(dataHead);
//			if (dataString.toLowerCase().equals("data") == false) {
//				return false;
//			}
			dataSize = windowsWavFile.readWinInt();
			dataSize = checkUintProblem(dataSize);
			headerSize = dataStart = windowsWavFile.getFilePointer();
			
			
		} catch (IOException e) {
			System.err.println("Error reading header from file " + e.getMessage());
			return false;
		}
		headerOk = true;
		return true;
	}
	
	/**
		 * Read the header data from file. 
		 * @param windowsWavFile Windows file
		 * @return true if header unpacked successfully. 
		 */
		public boolean writeHeader(WindowsFile windowsWavFile) {
	
			headerOk = false;
			if (windowsWavFile == null) {
				return false;
			}
			
			try {
				windowsWavFile.seek(0);
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
			int headerSize = 40;
			boolean ok = false;
			try {
				ok &= write4Chars(windowsWavFile, "RIFF");
				int totalSize = (int) (headerSize + dataSize);
				windowsWavFile.writeWinInt(totalSize); // NEED TO BE TOTAL SIZE OF FILE - 4 FOR RIFF
				ok &= write4Chars(windowsWavFile, "WAVE");
				ok &= write4Chars(windowsWavFile, "fmt ");
				// 16 bytes written so far. 
				windowsWavFile.writeWinInt(16); // size of format chunk. 
				windowsWavFile.writeWinShort(1); // PCM format, always 1. 
				windowsWavFile.writeWinShort(nChannels);
				windowsWavFile.writeWinInt(sampleRate);
				windowsWavFile.writeWinInt(bytesPerSec);
				windowsWavFile.writeWinShort(blockAlign);
				windowsWavFile.writeWinShort(bitsPerSample);
				// end of fmt section (16 bytes - 36 so far.)
				ok &= write4Chars(windowsWavFile, "data");
				windowsWavFile.writeWinInt((int) dataSize);
				// 44 bytes written. Then follow the data ...
				

				dataStart = windowsWavFile.getFilePointer();
				headerSize = (int) dataStart;
				
				
			} catch (IOException e) {
				System.err.println("Error writing WAV header to file " + e.getMessage());
				headerOk = false;
			}
			headerOk = ok;
			return headerOk;
		}

	private long checkUintProblem(long totalSize) {
		if (totalSize < 0) {
			totalSize += (1L<<32);
		}
		return totalSize;
	}

	private char[] read4Chars(WindowsFile wFile) throws IOException {
		char[] chars = new char[4];
		for (int i = 0; i < 4; i++) {
			chars[i] = (char) wFile.readByte();
		}
		return chars;
	}
	
	private boolean write4Chars(WindowsFile wfile, String chars) throws IOException {
		byte[] bytes = chars.getBytes();
		if (bytes.length != 4) {
			return false;
		}
		wfile.write(bytes);
		return true;
	}
	
	/**
	 *  
	 * @return all data from the header as an AudioFormat object. 
	 */
	public AudioFormat getAudioFormat() {
		return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, bitsPerSample, nChannels, blockAlign, sampleRate, false);
	}

	/**
	 * @param fmtTag the fmtTag to set
	 */
	public void setFmtTag(short fmtTag) {
		this.fmtTag = fmtTag;
	}

	/**
	 * @return the fmtTag
	 */
	public int getFmtTag() {
		return fmtTag;
	}

	/**
	 * @param nChannels the nChannels to set
	 */
	public void setNChannels(short nChannels) {
		this.nChannels = nChannels;
	}

	/**
	 * @return the nChannels
	 */
	public short getNChannels() {
		return nChannels;
	}

	/**
	 * @param blockAlign the blockAlign to set
	 */
	public void setBlockAlign(short blockAlign) {
		this.blockAlign = blockAlign;
	}

	/**
	 * @return the blockAlign
	 */
	public short getBlockAlign() {
		return blockAlign;
	}

	/**
	 * @param bitsPerSample the bitsPerSample to set
	 */
	public void setBitsPerSample(short bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	/**
	 * @return the bitsPerSample
	 */
	public short getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * @param sampleRate the sampleRate to set
	 */
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the sampleRate
	 */
	public int getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param bytesPerSec the bytesPerSec to set
	 */
	public void setBytesPerSec(int bytesPerSec) {
		this.bytesPerSec = bytesPerSec;
	}

	/**
	 * @return the bytesPerSec
	 */
	public int getBytesPerSec() {
		return bytesPerSec;
	}

	/**
	 * @param headerOk the headerOk to set
	 */
	public void setHeaderOk(boolean headerOk) {
		this.headerOk = headerOk;
	}

	/**
	 * @return the headerOk
	 */
	public boolean isHeaderOk() {
		return headerOk;
	}

	/**
	 * @param dataStart the dataStart to set
	 */
	public void setDataStart(long dataStart) {
		this.dataStart = dataStart;
	}

	/**
	 * 
	 * @return byte number for the start of the data. 
	 */
	public long getDataStart() {
		return dataStart;
	}

	/**
	 * @param dataSize the dataSize to set
	 */
	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	/**
	 * @return the dataSize
	 */
	public long getDataSize() {
		return dataSize;
	}
	
	/**
	 * Get the number of additional chunks in the wav header. 
	 * @return the number of additional chunks in the wav header. 
	 */
	public int getNumHeadChunks() {
		return wavHeadChunks.size();
	}
	
	/**
	 * Get a chunk from the wav header. 
	 * @param iChunk chunk number
	 * @return Chunk read from wav header. 
	 */
	public WavHeadChunk getHeadChunk(int iChunk) {
		return wavHeadChunks.get(iChunk);
	}

	/**
	 * @return the headerSize
	 */
	public long getHeaderSize() {
		return headerSize;
	}

	/**
	 * @return the harpHeader
	 */
	public HarpHeader getHarpHeader() {
		return harpHeader;
	}
}
