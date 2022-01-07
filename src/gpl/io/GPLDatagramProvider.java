package gpl.io;

import java.util.ArrayList;

import PamUtils.FrequencyFormat;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramProvider;
import dataGram.DatagramScaleInformation;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import gpl.GPLProcess;
import gpl.contour.GPLContour;
import gpl.contour.GPLContourPoint;

public class GPLDatagramProvider implements DatagramProvider {

	private GPLProcess gplProcess;
	private GPLDetectionBlock gplDetectionBlock;

	public GPLDatagramProvider(GPLProcess gpsProcess, GPLDetectionBlock gplDetectionBlock) {
		this.gplProcess = gpsProcess;
		this.gplDetectionBlock = gplDetectionBlock;
		
	}

	@Override
	public int getNumDataGramPoints() {
		return gplProcess.getSourceFFTLength()/2;
	}

	@Override
	public int addDatagramData(PamDataUnit dataUnit, float[] dataGramLine) {
		GPLDetection gplDetection = (GPLDetection) dataUnit;
		GPLContour gplContour = gplDetection.getContour();
		if (gplContour == null) {
			return addFreqLimitData(gplDetection, dataGramLine);
		}
		else {
			return addContourData(gplDetection, gplContour, dataGramLine);
		}
	}

	private int addContourData(GPLDetection gplDetection, GPLContour gplContour, float[] dataGramLine) {
		ArrayList<GPLContourPoint> points = gplContour.getContourPoints();
		if (points == null) {
 			return 0;
		}
		int iMax = dataGramLine.length-1;
		for (GPLContourPoint aPoint : points) {
			int y = Math.min(aPoint.y, iMax);
			dataGramLine[y] += aPoint.totalEnergy; 
		}
		return 0;
	}

	private int addFreqLimitData(GPLDetection gplDetection, float[] dataGramLine) {
		double[] freqLims = gplDetection.getFrequency();
		if (freqLims == null || freqLims.length < 2) {
			return 0;
		}
		double scale = dataGramLine.length / (gplDetectionBlock.getSampleRate()/2);
		int[] fBins = new int[2];
		for (int i = 0; i < 2; i++) {
			fBins[i] = (int) Math.round(freqLims[i] * scale);
			fBins[i] = Math.max(0, Math.min(fBins[i], dataGramLine.length));
		}
		for (int i = fBins[0]; i < fBins[1]; i++) {
			dataGramLine[i] ++;
		}
		
		return 0;
	}

	@Override
	public DatagramScaleInformation getScaleInformation() {
		double maxFreq = gplDetectionBlock.getSampleRate()/2;
		FrequencyFormat ff = FrequencyFormat.getFrequencyFormat(maxFreq);
		return new DatagramScaleInformation(0, maxFreq/ff.getScale(), ff.getUnitText());
	}

}
