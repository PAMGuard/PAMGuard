package networkTransfer.emulator;

import PamUtils.LatLong;

public class EmBuoyStatus {

	private int buoyId;
	
	private LatLong latLong;
	
	protected int unitsSent;
	
	protected long currentDataTime;
	
	protected boolean socketStatus;

	public int localPort;

	public int remotePort;

	public int totalBytes;

	/**
	 * @param buoyId
	 * @param latLong
	 */
	public EmBuoyStatus(int buoyId, LatLong latLong) {
		super();
		this.buoyId = buoyId;
		this.latLong = latLong;
	}

	/**
	 * @param latLong the latLong to set
	 */
	public void setLatLong(LatLong latLong) {
		this.latLong = latLong;
	}

	/**
	 * @return the latLong
	 */
	public LatLong getLatLong() {
		return latLong;
	}

	/**
	 * @return the buoyId
	 */
	public int getBuoyId() {
		return buoyId;
	}
	
}
