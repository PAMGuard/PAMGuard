package dataMap.filemaps;

import java.io.Serializable;

/**
 * information added to a FileDataMapPoint when only part of the file
 * is associated with this map point. Start and end points are in bytes
 * relative to the start of the file I think ? My be absolute in the file ? 
 * @author dg50
 *
 */
public class FileSubSection implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long startByte, endByte;
	
	public FileSubSection(long startByte, long endByte) {
		this.startByte = startByte;
		this.endByte = endByte;
	}
	/**
	 * @return the startByte
	 */
	public long getStartByte() {
		return startByte;
	}
	/**
	 * @param startByte the startByte to set
	 */
	public void setStartByte(long startByte) {
		this.startByte = startByte;
	}
	/**
	 * @return the endByte
	 */
	public long getEndByte() {
		return endByte;
	}
	/**
	 * @param endByte the endByte to set
	 */
	public void setEndByte(long endByte) {
		this.endByte = endByte;
	}

}
