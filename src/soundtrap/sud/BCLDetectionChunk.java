package soundtrap.sud;

import org.pamguard.x3.sud.Chunk;

public class BCLDetectionChunk {

	private long javaMillis;
	private boolean isDetection;
	private String bclRecord;
	private Chunk sudChunk;
	private long javaMicros;
	
	public BCLDetectionChunk(long javaMillis, long javaMicros, boolean isDetection, String bclRecord, Chunk sudChunk) {
		super();
		this.javaMillis = javaMillis;
		this.javaMicros = javaMicros;
		this.isDetection = isDetection;
		this.bclRecord = bclRecord;
		this.sudChunk = sudChunk;
	}

	/**
	 * @return the javaMillis
	 */
	public long getJavaMillis() {
		return javaMillis;
	}

	/**
	 * @return the isDetection
	 */
	public boolean isDetection() {
		return isDetection;
	}

	/**
	 * @return the bclRecord
	 */
	public String getBclRecord() {
		return bclRecord;
	}

	/**
	 * @return the sudChunk
	 */
	public Chunk getSudChunk() {
		return sudChunk;
	}

	/**
	 * @return the javaMicros
	 */
	public long getJavaMicros() {
		return javaMicros;
	}
	
	

}
