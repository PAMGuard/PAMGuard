package networkTransfer.receive.status.base;

import java.util.ArrayList;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import networkTransfer.receive.status.BuoyStatusDataBlock;
import networkTransfer.receive.status.BuoyStatusDataUnit;

public class NetReceiverStatusDataUnit extends PamDataUnit{
		
	private long pamguardUptimeMillis;
	private int nBuoysConnected;
	private double memoryAvailablePercent;
	private String pamguardRunState;
	private int nBuoysRegistered;
	private ArrayList<BuoyStatusDataUnit> buoyStatusData;

	public NetReceiverStatusDataUnit(long timeMilliseconds,BuoyStatusDataBlock buoyData) {
		super(timeMilliseconds);
		this.buoyStatusData = buoyData.getUniqueBuoyStatuses();
		setMetrics(buoyData);
	}
	
	private String getPamRunning() {
		int pamStatus = PamController.getInstance().getRealStatus();
		switch(pamStatus) {
			case PamController.PAM_IDLE:
				return "PAM_IDLE";
			case PamController.PAM_RUNNING:
				return "PAM_RUNNING";
			case PamController.PAM_STALLED:
				return "PAM_STALLED";
			case PamController.PAM_STOPPING:
				return "PAM_STOPPING";
			case PamController.PAM_INITIALISING:
				return "PAM_INITIALIZING";
			default:
				return "UNKNOWN PAM STATE "+pamStatus;
		}
	}
	
	public void setMetrics(BuoyStatusDataBlock buoyData) {
		this.pamguardRunState = getPamRunning();
		this.pamguardUptimeMillis = PamCalendar.getTimeInMillis()-PamCalendar.getSessionStartTime();
		this.memoryAvailablePercent = PamController.getInstance().getMemoryAvailablePercent();
		this.nBuoysConnected = buoyData.getNBuoysConnectedAndRunning();
		this.nBuoysRegistered = buoyData.getNBuoysRegistered();
		//this.nBuoysConnected = buoyData.
	}

	public long getPamguardUptimeMillis() {
		return pamguardUptimeMillis;
	}

	public int getnBuoysConnected() {
		return nBuoysConnected;
	}

	public double getMemoryUsedPercent() {
		return memoryAvailablePercent;
	}

	public String getPamguardRunState() {
		return pamguardRunState;
	}

	public int getnBuoysRegistered() {
		return nBuoysRegistered;
	}

	public ArrayList<BuoyStatusDataUnit> getBuoyStatusData() {
		return buoyStatusData;
	}

}
