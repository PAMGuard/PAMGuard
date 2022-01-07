package gpl.contour;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GPLContour implements Cloneable {
	
	private ArrayList<GPLContourPoint> contourPoints;
	
	private double totalExcess, totalEnergy;
	
	private int minFBin, maxFBin;

	private int minTBin, maxTBin;

	/**
	 * Make an empty contour. Not to be used to generate a contour
	 * to then add data to, since it doesn't have an arraylist
	 * of points. Only for null contours. 
	 */
	protected GPLContour() {
		
	}

	public GPLContour(int iT, int iF, double signalExcess, double energy) {
		contourPoints = new ArrayList<>();
		addContourPoint(iT, iF, signalExcess, energy);
		minFBin = maxFBin = iF;
		minTBin = maxTBin = iT;
	}
	
	/**
	 * Make a null contour. not to be used for ANYTHING but a pla
	 * @return
	 */
	public static GPLContour makeNullContour() {
		return new GPLContour();
	}

	/**
	 * Add a contour point 
	 * @param iT time bin
	 * @param iF frequency bin
	 * @param dataValue whitened data value. 
	 */
	public void addContourPoint(int iT, int iF, double signalExcess, double energy) {
		addContourPoint(new GPLContourPoint(iT, iF, signalExcess, energy));
	}
	
	/**
	 * Add a contour point
	 * @param contourPoint
	 */
	public void addContourPoint(GPLContourPoint contourPoint) {
		contourPoints.add(contourPoint);
		totalExcess += contourPoint.signalExcess;
		totalEnergy += contourPoint.totalEnergy;
		minFBin = Math.min(minFBin, contourPoint.y);
		maxFBin = Math.max(maxFBin, contourPoint.y);
		minTBin = Math.min(minTBin, contourPoint.x);
		maxTBin = Math.max(maxTBin, contourPoint.x);
	}
	
	/**
	 * Get the area of the contour
	 * @return
	 */
	public int getArea() {
		if (contourPoints == null) {
			return 0;
		}
		return contourPoints.size();
	}

	/**
	 * sort the points in the contour by time and frequency. 
	 */
	public void sortContourPoints() {
		if (contourPoints == null) {
			return;
		}
		Collections.sort(contourPoints, new PointComparator());
	}

	/**
	 * This is the sum of the whitened data in the contour. Has no rational scale.  
	 * @return sum of whitened levels
	 */
	public double getTotalExcess() {
		return totalExcess;
	}
	
	/**
	 * This is the sum^2 of every FFT point that formed part of the contour. 
	 * @return total energy in contour, scaled to counts
	 */
	public double getTotalEnergy() {
		return totalEnergy;
	}

	public int[][] getOutline() {
		return getOutline(0);
	}
	
	public int[][] getOutline(int binOffset) {
		if (contourPoints == null || contourPoints.size() == 0) {
			return null;
		}
		int ax = -1;
		int nX = 0;
		for (Point aPoint : contourPoints) {
			if (aPoint.x != ax) {
				nX++;
				ax = aPoint.x;
			}
		}
		int[][] outline = new int[nX][3];
		ax = -1;
		int ix = -1;
		for (Point aPoint : contourPoints) {
			if (aPoint.x != ax) {
				ix++;
				ax = aPoint.x;
				outline[ix][0] = ax;
				outline[ix][1] = outline[ix][2] = aPoint.y + binOffset;
			}
			else {
				outline[ix][2] = aPoint.y + binOffset;
			}
			
		}
		return outline;
	}

	/**
	 * Sort points. Initially by x (time) then by y (frequency)
	 * @author dg50
	 *
	 */
	private class PointComparator implements Comparator<Point> {

		@Override
		public int compare(Point p1, Point p2) {
			int c1 = p1.x-p2.x;
			if (c1 == 0) {
				return p1.y-p2.y;
			}
			return c1;
		}	
	}
	
	/**
	 * Time is from the start of the call, 
	 * Frequency is referenced to the full spectrum, not just the detection band, so 0 
	 * represents 0 Hz and FFTLen/2 is Nyquist. 
	 * @return the contourPoints
	 */
	public ArrayList<GPLContourPoint> getContourPoints() {
		return contourPoints;
	}

	/**
	 * Referenced to the full spectrum, not just the detection band, so 0 
	 * represents 0 Hz and FFTLen/2 is Nyquist. 
	 * @return the minFBin
	 */
	public int getMinFBin() {
		return minFBin;
	}

	/**
	 * Referenced to the full spectrum, not just the detection band, so 0 
	 * represents 0 Hz and FFTLen/2 is Nyquist. 
	 * @return the maxFBin
	 */
	public int getMaxFBin() {
		return maxFBin;
	}

	/**
	 * @return the minTBin
	 */
	public int getMinTBin() {
		return minTBin;
	}

	/**
	 * @return the maxTBin
	 */
	public int getMaxTBin() {
		return maxTBin;
	}
	
	/**
	 * Get how long the sound was in FFT slices. 
	 * @return
	 */
	public int getBinsDuration() {
		return maxTBin-minTBin+1;
	}

	@Override
	protected GPLContour clone() {
		try {
			GPLContour newCont = (GPLContour) super.clone();
			if (this.contourPoints != null) {
				newCont.contourPoints = (ArrayList<GPLContourPoint>) this.contourPoints.clone();
			}
			return newCont;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * shift the contour so that everything is referenced to a first time bin of 0. 
	 */
	public void shiftTimeBins() {
		if (contourPoints == null) {
			return;
		}
		for (GPLContourPoint cP : contourPoints) {
			cP.x -= minTBin;
		}
		maxTBin -= minTBin;
		minTBin = 0;
	}

	/**
	 * @param totalExcess the totalExcess to set
	 */
	public void setTotalExcess(double totalExcess) {
		this.totalExcess = totalExcess;
	}

	/**
	 * @param totalEnergy the totalEnergy to set
	 */
	public void setTotalEnergy(double totalEnergy) {
		this.totalEnergy = totalEnergy;
	}

	/**
	 * @param minFBin the minFBin to set
	 */
	protected void setMinFBin(int minFBin) {
		this.minFBin = minFBin;
	}

	/**
	 * @param maxFBin the maxFBin to set
	 */
	protected void setMaxFBin(int maxFBin) {
		this.maxFBin = maxFBin;
	}

	/**
	 * @param minTBin the minTBin to set
	 */
	protected void setMinTBin(int minTBin) {
		this.minTBin = minTBin;
	}

	/**
	 * @param maxTBin the maxTBin to set
	 */
	protected void setMaxTBin(int maxTBin) {
		this.maxTBin = maxTBin;
	}
}
