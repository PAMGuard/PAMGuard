package wavFiles;

/**
 * Chunk information from a wav file header. 
 * @author Doug Gillespie
 *
 */
public class WavHeadChunk {

	private String chunkName;
	private byte[] headChunk;
	public WavHeadChunk(String chunkName, byte[] headChunk) {
		this.chunkName = chunkName;
		this.headChunk = headChunk;
	}


	@Override
	public String toString() {
		return new String(headChunk);
	}


	/**
	 * @return the chunkName
	 */
	public String getChunkName() {
		return chunkName;
	}


	/**
	 * @return the headChunk
	 */
	public byte[] getHeadChunk() {
		return headChunk;
	}
	

}
