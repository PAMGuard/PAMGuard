package Acquisition;

public class WavChunkData {

	static final byte RIFF[] = {'R','I','F','F'};
	static final byte WAVE[] = {'W','A','V','E'};
	static final byte FMT[] = {'f','m','t',' '};
	static final byte WAVLIST[] = {'L','I','S','T'};
	static final byte INFO[] = {'I','N','F','O'};
	static final byte ICOP[] = {'I','C','O','P'};
	static final byte ICRD[] = {'I','C','R','D'};
	static final byte DATA[] = {'d','a','t','a'};

	static public final int CHUNK_OTHER = 0;
	static public final int CHUNK_RIFF = 1;
	static public final int CHUNK_WAVE = 2;
	static public final int CHUNK_DATA = 3;
	static public final int CHUNK_FMT = 4;
	static public final int CHUNK_INFO = 5;
	static public final int CHUNK_ICOP = 6;
	static public final int CHUNK_ICRD = 7;

	int chunkSize;
	long fileOffset;
	byte[] chunkId;
	byte[] chunkData;
	int chunkType;

	public WavChunkData(int chunkSize, long fileOffset, byte[] chunkId, byte[] chunkData) {
		super();
		this.chunkSize = chunkSize;
		this.fileOffset = fileOffset;
		this.chunkId = chunkId;
		this.chunkData = chunkData;
		this.chunkType = getChunkType(chunkId);
//		String str = new String(chunkData);
//		System.out.println(new String(chunkId) + "   " + str);
	}

	public int getType() {
		return chunkType;
	}

	public static int getChunkType(byte[] chunkId) {
		if (matchChunkId(chunkId, RIFF)) {
			return CHUNK_RIFF;
		}
		if (matchChunkId(chunkId, WAVE)) {
			return CHUNK_WAVE;
		}
		if (matchChunkId(chunkId, FMT)) {
			return CHUNK_FMT;
		}
		if (matchChunkId(chunkId, DATA)) {
			return CHUNK_DATA;
		}
		if (matchChunkId(chunkId, INFO)) {
			return CHUNK_INFO;
		}
		if (matchChunkId(chunkId, ICOP)) {
			return CHUNK_ICOP;
		}
		if (matchChunkId(chunkId, ICRD)) {
			return CHUNK_ICRD;
		}

		return CHUNK_OTHER;
	}

	public static boolean matchChunkId(byte[] id1, byte[] id2) {
		if (id1.length != id2.length) {
			return false;
		}
		for (int i = 0; i < id1.length; i++) {
			if (id1[i] != id2[i]) {
				return false;
			}
		}
		return true;
	}

}
