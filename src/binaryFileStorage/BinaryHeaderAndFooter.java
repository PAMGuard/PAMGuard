package binaryFileStorage;

import dataGram.Datagram;

public class BinaryHeaderAndFooter {

	public BinaryHeader binaryHeader;
	
	public BinaryFooter binaryFooter;
	
	public BinaryObjectData moduleHeaderData;
	
	public BinaryObjectData moduleFooterData;
	
	public Datagram datagram;
	
	/**
	 * If there is no footer - due o a crash, this will contain the
	 * last data time in the file, or worst case, it will contain the 
	 * same info as the start time. 
	 */
	public long lastDataTime;
	
	public int nDatas;
	
}
