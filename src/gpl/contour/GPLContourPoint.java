package gpl.contour;

import java.awt.Point;

public class GPLContourPoint extends Point {
	
	private static final long serialVersionUID = 1L;

	public double signalExcess;
	
	public double totalEnergy;

	public GPLContourPoint(Point p, double signalExcess, double energy) {
		super(p);
		this.signalExcess = signalExcess;
		this.totalEnergy = energy;
	}

	public GPLContourPoint(int x, int y, double signalExcess, double energy) {
		super(x, y);
		this.signalExcess = signalExcess;
		this.totalEnergy = energy;
	}

	/**
	 * @return the point amplitude
	 */
	public double getSignalExcess() {
		return signalExcess;
	}

	/**
	 * @param amplitude the point energy to set
	 */
	public void setSignalExcess(double signalExcess) {
		this.signalExcess = signalExcess;
	}

	/**
	 * @return the totalEnergy
	 */
	public double getTotalEnergy() {
		return totalEnergy;
	}

}
