package networkTransfer.receive;

import java.io.Serializable;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamCalendar;

/**
 * Separate out from BuoyStatusDataunit so that it can be serialized
 * @author dg50
 *
 */
public class BuoyStatusData implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 2L;

	private int buoyId1;
	
	private int buoyId2;
	
	private int channelMap;

	private long creationTime;
	
	private long lastDataTime;
	
	private String ipAddr;

	private Hashtable<String, BuoyStatusValue> genericStringPairs = new Hashtable<String, BuoyStatusValue>();

	public BuoyStatusData(int buoyId1, int buoyId2, int channelMap) {
		this.buoyId1 = buoyId1;
		this.buoyId2 = buoyId2;
		this.channelMap = channelMap;
		this.creationTime = PamCalendar.getTimeInMillis();
	}

	/**
	 * @return the buoyId1
	 */
	public int getBuoyId1() {
		return buoyId1;
	}

	/**
	 * @param buoyId1 the buoyId1 to set
	 */
	public void setBuoyId1(int buoyId1) {
		this.buoyId1 = buoyId1;
	}

	/**
	 * @return the buoyId2
	 */
	public int getBuoyId2() {
		return buoyId2;
	}

	/**
	 * @param buoyId2 the buoyId2 to set
	 */
	public void setBuoyId2(int buoyId2) {
		this.buoyId2 = buoyId2;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the genericStringPairs
	 */
	public Hashtable<String, BuoyStatusValue> getGenericStringPairs() {
		return genericStringPairs;
	}

	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}

	/**
	 * @param channelMap the channelMap to set
	 */
	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}

	/**
	 * @return the lastDataTime
	 */
	public long getLastDataTime() {
		return lastDataTime;
	}

	/**
	 * @param lastDataTime the lastDataTime to set
	 */
	public void setLastDataTime(long lastDataTime) {
		this.lastDataTime = lastDataTime;
		if (creationTime == 0) {
			creationTime = lastDataTime;
		}
	}

	/**
	 * @return the ipAddr
	 */
	public String getIpAddr() {
		return ipAddr;
	}

	/**
	 * @param ipAddr the ipAddr to set
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

	@Override
	public String toString() {
		return String.format("Buoy %d(%d) ip %s", buoyId1, buoyId2, ipAddr);
	}

}
