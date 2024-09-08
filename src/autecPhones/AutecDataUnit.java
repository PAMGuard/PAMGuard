package autecPhones;

import PamUtils.LatLong;
import PamguardMVC.PamDataUnit;

public class AutecDataUnit extends PamDataUnit {
	
	private LatLong latLong;
	private int iD;
	private double depth;
	private boolean isActive;

	public AutecDataUnit(int id, double latitude, double longitude, double depth, 
			boolean isActive) {
		super(0);
		this.depth = depth;
		iD = id;
		this.isActive = isActive;
		latLong = new LatLong(latitude, longitude);
	}

	public AutecDataUnit(double latitude, double longi) {
		super(0);
		// TODO Auto-generated constructor stub
	}

	public LatLong getLatLong() {
		return latLong;
	}

	public int getID() {
		return iD;
	}

	public double getDepth() {
		return depth;
	}

	public boolean isActive() {
		return isActive;
	}

}
