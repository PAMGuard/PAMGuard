package dataMap;

public class MapOverlap {
	
	private long file2Start;
	
	private long file1End;

	public MapOverlap(long file1End, long file2Start) {
		super();
		this.file1End = file1End;
		this.file2Start = file2Start;
	}

	/**
	 * @return the file2Start
	 */
	public long getFile2Start() {
		return file2Start;
	}

	/**
	 * @return the file1End
	 */
	public long getFile1End() {
		return file1End;
	}
	
	
}
