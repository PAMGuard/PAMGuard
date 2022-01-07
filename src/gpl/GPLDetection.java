package gpl;

import java.awt.Point;
import java.util.ArrayList;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.TFContourData;
import PamguardMVC.TFContourProvider;
import gpl.contour.GPLContour;
import gpl.contour.GPLContourPoint;

public class GPLDetection extends PamDataUnit implements AcousticDataUnit, PamDetection, TFContourProvider {

	private double peakValue;
	private GPLContour contour;

	/**
	 * These are in SI units, so seconds per FFT bin, and Hz per FFT bin, i.e.
					double dT = sourceFFTData.getHopSamples() / sourceFFTData.getSampleRate();
					double dF = sourceFFTData.getSampleRate() / sourceFFTData.getFftLength();
	 */
	private double timeRes;
	private double freqRes;
	
	private TFContourData tfContourData;
	
	public GPLDetection(long timeMilliseconds, int channelBitmap, long startSample, long durationSamples, double timeRes, double freqRes, GPLContour aContour) {
		super(timeMilliseconds, channelBitmap, startSample, durationSamples);
		this.timeRes = timeRes;
		this.freqRes = freqRes;
		this.contour = aContour;
	}

	public GPLDetection(DataUnitBaseData basicData, double timeRes, double freqRes, GPLContour aContour) {
		super(basicData);
		this.timeRes = timeRes;
		this.freqRes = freqRes;
		this.contour = aContour;
	}

	/**
	 * @return the peakValue
	 */
	public double getPeakValue() {
		return peakValue;
	}

	/**
	 * @param peakValue the peakValue to set
	 */
	public void setPeakValue(double peakValue) {
		this.peakValue = peakValue;
	}
	

	/**
	 * @return the contour
	 */
	public GPLContour getContour() {
		return contour;
	}

	/**
	 * @return the timeRes
	 */
	public double getTimeRes() {
		return timeRes;
	}

	/**
	 * @return the freqRes
	 */
	public double getFreqRes() {
		return freqRes;
	}


	@Override
	public TFContourData getTFContourData() {
		if (tfContourData == null) {
			tfContourData = makeTFContourData();
		}
		return tfContourData;
	}

	private TFContourData makeTFContourData() {
		if (contour == null) {
			return null;
		}
		if (timeRes == 0 || freqRes == 0) {
			return null; // no can do !
		}
		int ax = -1;
		int nX = 0;
		ArrayList<GPLContourPoint> contourPoints = contour.getContourPoints();
		for (Point aPoint : contourPoints) {
			if (aPoint.x != ax) {
				nX++;
				ax = aPoint.x;
			}
		}
		long[] t = new long[nX];
		double[] fRidge = new double[nX];
		double[] fLow = new double[nX];
		double[] fHigh = new double[nX];
		double aMax= 0;
		
		long t0 = getTimeMilliseconds();
		ax = -1;
		int ix = -1;
		for (GPLContourPoint aPoint : contourPoints) {
			double fHz = aPoint.y*freqRes;
			double a = aPoint.totalEnergy;
			if (aPoint.x != ax) {
				ax = aPoint.x;
				ix++;
				t[ix] = t0 + (long) (aPoint.x * timeRes * 1000.);
				fRidge[ix] = fLow[ix] = fHigh[ix] = fHz;
				aMax = a;
			}
			else {
				fLow[ix] = Math.min(fLow[ix], fHz);
				fHigh[ix] = Math.max(fHigh[ix], fHz);
				if (a > aMax) {
					aMax = a;
					fRidge[ix] = fHz;
				}
			}
			
		}
		
		TFContourData tfcd = new TFContourData(t, fRidge, fLow, fHigh);
		return tfcd;
	}

}
