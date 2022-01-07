package Localiser.algorithms;

/**
 * Peak search algorithms which can find a peak in an array of real numbers.<p>
 * Used by various localisers to find the best peak in correlation data.<p> 
 * Contains a number of options for interpolating in both x and y to find the best peak position. 
 * All functions return a PeakData object which contains the peak position and it's height.
 * @author Doug Gillespie
 *
 */
public class PeakSearch {

	/**
	 * Interpolates in height to get the true best peak 
	 */
	private boolean interpolateY;
	
	/**
	 * Wrap when interpolating, i.e. assume a circular set of values
	 * so the last is also before the first and the irst after the last
	 */
	private boolean wrapDim0 = false;
	
	private boolean wrapDim1 = false;
	
	/**
	 * Wrap steps - normally 1, when the last is before the first, 
	 * but what if the last == the first, i.e in a distribution that 
	 * includes a value at -180 and one at +180 degrees. Displays lok nicer
	 * like this, but when interpolating, need to jump one more, so set these
	 * params to 2. 
	 */
	private int wrapStep0 = 1;
	
	private int wrapStep1 = 1;
	
	/**
	 * Construct peak search algorithms. 
	 * @param interpolateY interpolate the Y coordinate (only when also interpolating x)
	 */
	public PeakSearch(boolean interpolateY) {
		super();
		this.interpolateY = interpolateY;
	}

	/**
	 * Find the highest bin, no interpolation or anything. 
	 * @param data
	 * @return
	 */
	public PeakPosition simplePeakSearch(double[] data) {
		if (data == null) {
			return null;
		}
		return simplePeakSearch(data, 0, data.length);
	}
	
	/**
	 * find the height and position of the peak in a range or data, starting from 
	 * binFrom and searching up to and including binTo-1
	 * @param data
	 * @param binFrom first bin to search
	 * @param binTo last bin + 1
	 * @return peak position (bin and height)
	 */
	public PeakPosition simplePeakSearch(double[] data, int binFrom, int binTo) {
		if (data == null || data.length == 0) {
			return null;
		}
		PeakPosition peakPos = new PeakPosition(binFrom, data[0]);
		for (int i = binFrom+1; i < binTo; i++) {
			if (data[i] > peakPos.height) {
				peakPos.height = data[i];
				peakPos.bin = i;
			}
		}
		return peakPos;
	}

	/**
	 * Simple peak search in two dimensions. 
	 * @param data 2D array
	 * @return Peak row, col and height. 
	 */
	public PeakPosition2D simplePeakSearch(double[][] data) {
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null;
		}
		return simplePeakSearch(data, 0, data.length, 0, data[0].length);
	}
	
	/**
	 * Simple peak search in two dimensions. 
	 * @param data 2D array
	 * @param binFrom0 start search bin in first dimension
	 * @param binTo0 end search bin in first dimension
	 * @param binFrom1 start search bin in second dimension
	 * @param binTo1 end search bin in second dimension
	 * @return Peak row, col and height. 
	 */
	public PeakPosition2D simplePeakSearch(double[][] data, int binFrom0, int binTo0, int binFrom1, int binTo1) {
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null;
		}
		double peakVal = data[0][0];
		int peak0 = 0, peak1 = 0;
		for (int i = binFrom0; i < binTo0; i++) {
			for (int j = binFrom1; j < binTo1; j++) {
				if (data[i][j] > peakVal) {
					peakVal = data[i][j];
					peak0 = i;
					peak1 = j;
				}
			}
		}
		return new PeakPosition2D(peak0, peak1, peakVal);
	}

	/**
	 * find the height and position of the peak in a range or data, starting from 
	 * binFrom and searching up to and including binTo-1<p>
	 * Uses parabolic interpolation to get a more accurate value
	 * @param data
	 * @return peak position
	 */
	public PeakPosition interpolatedPeakSearch(double[] data) {
		if (data == null) {
			return null;
		}
		return interpolatedPeakSearch(data, 0, data.length);
	}


	/**
	 * find the height and position of the peak in a range or data, starting from 
	 * binFrom and searching up to and including binTo-1<p>
	 * Uses parabolic interpolation to get a more accurate value
	 * @param data
	 * @param binFrom first bin to search
	 * @param binTo last bin + 1
	 * @return peak position (bin and height)
	 */
	public PeakPosition interpolatedPeakSearch(double[] data, int binFrom, int binTo) {
		PeakPosition peakPos = simplePeakSearch(data, binFrom, binTo);
		if (peakPos == null) {
			return null;
		}
		if ((peakPos.bin > 0 && peakPos.bin < binTo-1) || wrapDim0) {
			int currentBin = (int) peakPos.bin;
			int binBefore = currentBin-1;
			if (binBefore < 0) binBefore = data.length-wrapStep0;
			int binAfter = currentBin+1;
			if (binAfter == data.length) binAfter = wrapStep0-1;
			peakPos = parabolInterpolate(peakPos, data[binBefore], data[currentBin], data[binAfter]);
//			peakPos.bin += currentBin;
		}
		return peakPos;
	}

	/**
	 * Interpolated peak search in two dimensions. 
	 * @param data 2D array
	 * @return Peak row, col and height. 
	 */
	public PeakPosition2D interpolatedPeakSearch(double[][] data) {
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null;
		}
		return interpolatedPeakSearch(data, 0, data.length, 0, data[0].length);
		
	}	
	
	/**
	 * Interpolated peak search in two dimensions. 
	 * @param data 2D array
	 * @param binFrom0 start search bin in first dimension
	 * @param binTo0 end search bin in first dimension
	 * @param binFrom1 start search bin in second dimension
	 * @param binTo1 end search bin in second dimension
	 * @return Peak row, col and height. 
	 */
	public PeakPosition2D interpolatedPeakSearch(double[][] data, int binFrom0, int binTo0, int binFrom1, int binTo1) {
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null;
		}
		PeakPosition2D peak = simplePeakSearch(data, binFrom0, binTo0, binFrom1, binTo1);
		int x = (int) Math.round(peak.bin0);
		int y = (int) Math.round(peak.bin1);
		int n0 = data.length;
		int n1 = data[0].length;
		double h0 = peak.height, h1 = peak.height;
		double xOff;
		if ((peak.bin0 > 0 && peak.bin0 < n0-1) || wrapDim0) {
			int x1 = (int) peak.bin0;
			int x0 = x1-1;
			if (x0 < 0) x0 = n0-wrapStep0;
			int x2 = x1+1;
			if (x2 == n0) x2 = wrapStep0-1;
			xOff = parabolInterpolate(data[x0][y], data[x1][y], data[x2][y]);
			h0 = parabolicHeight(xOff, data[x0][y], data[x1][y], data[x2][y]);
			peak.bin0 += xOff;
		}
		if ((peak.bin1 > 0 && peak.bin1 < n1-1) || wrapDim1) {
			int y1 = (int) peak.bin1;
			int y0 = y1-1;
			if (y0 < 0) y0 = n1-wrapStep1;
			int y2 = y1+1;
			if (y2 == n1) y2 = wrapStep1-1;
			xOff = parabolInterpolate(data[x][y0], data[x][y1], data[x][y2]);
			h1 = parabolicHeight(xOff, data[x][y0], data[x][y1], data[x][y2]);
//			peak = simplePeakSearch(data, binFrom0, binTo0, binFrom1, binTo1);
//			xOff = parabolInterpolate(data[x][y0], data[x][y1], data[x][y2]);
			peak.bin1 += xOff;
		}
		
		return peak;
	}

	/**
	 * update a peak position using parabolic interpolation
	 * @param currentPos current value
	 * @param y1 value before peak
	 * @param y2 value at peak
	 * @param y3 value after peak
	 * @return updated position. 
	 */
	private PeakPosition parabolInterpolate(PeakPosition currentPos, double y1, double y2, double y3) {
		double x = parabolInterpolate(y1, y2, y3);
		currentPos.bin += x;
		if (interpolateY) {
			currentPos.height = parabolicHeight(x, y1, y2, y3);
		}
		return currentPos;
	}	
	
	/**
	 * Get the position offset (-.5 < val < .5) from three y values.
	 * @param y1 value before peak
	 * @param y2 value at peak
	 * @param y3 value after peak
	 * @return x offset. 
	 */
	private double parabolInterpolate(double y1, double y2, double y3) {
		double bottom = 2*y2-y1-y3;
		if (bottom == 0.) {
			return 0.;
		}
		return 0.5*(y3-y1)/bottom;
	}
	
	/**
	 * Calculate a parabolic maximum height based on three bin heights
	 * @param x X offset from y2. 
	 * @param y1 first bin
	 * @param y2 second bin
	 * @param y3 third bin
	 * @return height of parabola through those points. 
	 */
	public double parabolicHeight(double x, double y1, double y2, double y3) {
		double a = (y1+y3-2.*y2)/2.; 
		double b = (y3-y1) / 2.;
		double c = y2;
		return a*x*x + b*x + c;
	}
	
//	/**
//	 * Get the curvature of the likelihood surface, taking a log of th edata if necessary. 
//	 * @param dim dimension to operate on (0 or 1)
//	 * @param doLog take log of data before doing the calculation.
//	 * @return curvature of surface in the given dimension. 
//	 */
//	public double getCurvature(double[] data, int dim, int centreBin, boolean doLog) {
//		int b1 = centreBin-1;
//		int b2 = centreBin;
//		int b3 = centreBin+1;
//	}

	/**
	 * @return the interpolateY
	 */
	public boolean isInterpolateY() {
		return interpolateY;
	}

	/**
	 * @param interpolateY the interpolateY to set
	 */
	public void setInterpolateY(boolean interpolateY) {
		this.interpolateY = interpolateY;
	}

	/**
	 * When interpolating, allow dim0 to wrap (useful for circular angles)
	 * @return the wrapDim0
	 */
	public boolean isWrapDim0() {
		return wrapDim0;
	}

	/**
	 * When interpolating, allow dim0 to wrap (useful for circular angles)
	 * @param wrapDim0 the wrapDim0 to set
	 */
	public void setWrapDim0(boolean wrapDim0) {
		this.wrapDim0 = wrapDim0;
	}

	/**
	 * When interpolating, allow dim1 to wrap (useful for circular angles)
	 * @return the wrapDim1
	 */
	public boolean isWrapDim1() {
		return wrapDim1;
	}

	/**
	 * When interpolating, allow dim1 to wrap (useful for circular angles)
	 * @param wrapDim1 the wrapDim1 to set
	 */
	public void setWrapDim1(boolean wrapDim1) {
		this.wrapDim1 = wrapDim1;
	}

	/**
	 * @return the wrapStep0
	 */
	public int getWrapStep0() {
		return wrapStep0;
	}

	/**
	 * @param wrapStep0 the wrapStep0 to set
	 */
	public void setWrapStep0(int wrapStep0) {
		this.wrapStep0 = wrapStep0;
	}

	/**
	 * @return the wrapStep1
	 */
	public int getWrapStep1() {
		return wrapStep1;
	}

	/**
	 * @param wrapStep1 the wrapStep1 to set
	 */
	public void setWrapStep1(int wrapStep1) {
		this.wrapStep1 = wrapStep1;
	}
}
