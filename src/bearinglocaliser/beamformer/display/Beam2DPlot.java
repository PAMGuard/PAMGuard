package bearinglocaliser.beamformer.display;

import java.util.List;

import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;
import beamformer.BeamAlgorithmParams;
import beamformer.continuous.BeamOGramDataUnit;
import bearinglocaliser.beamformer.WrappedBeamFormAlgorithm;
import bearinglocaliser.display.Bearing2DPlot;
import detectionPlotFX.plots.simple2d.SimpleLineData;
import fftManager.FFTDataBlock;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class Beam2DPlot extends Bearing2DPlot {

	private int nDimensions;
	private BeamAlgorithmParams beamAlgorithmParams;
	double[] xAxRange = new double[2];
	double[] yAxRange = new double[2];
	double[] yAx2Range = new double[2];
	private WrappedBeamFormAlgorithm wrappedBeamFormAlgorithm;
	private FFTDataBlock fftSource;
	private double[] angle1Intervals;
	private PamSymbolFX maxCross = new PamSymbolFX(PamSymbolType.SYMBOL_CROSS, 11, 11, true, Color.WHITE, Color.WHITE);;

	public Beam2DPlot(WrappedBeamFormAlgorithm wrappedBeamFormAlgorithm, String name, int nDimensions, BeamAlgorithmParams beamAlgorithmParams) {
		super(name);
		this.wrappedBeamFormAlgorithm = wrappedBeamFormAlgorithm;
		this.nDimensions = nDimensions;
		this.beamAlgorithmParams = beamAlgorithmParams;
		fftSource = wrappedBeamFormAlgorithm.getFftSourceData();
		int[] aRange = beamAlgorithmParams.getBeamOGramAngles();
		for (int i = 0; i < 2; i++) {
			xAxRange[i] = aRange[i];
		}
		if (nDimensions == 1) {
			this.setLeftLabel("Frequency (Hz)");
			this.setBottomLabel("Primary Angle (\u00B0)");
			yAxRange[1] = fftSource.getSampleRate()/2.;
			maxCross = new PamSymbolFX(PamSymbolType.SYMBOL_CROSS, 11, 25, true, Color.WHITE, Color.WHITE);
		}
		else {
			this.setLeftLabel("Secondary Angle (\u00B0)");
			this.setBottomLabel("Primary Angle (\u00B0)");
			int[] sRange = beamAlgorithmParams.getBeamOGramSlants();
			for (int i = 0; i < 2; i++) {
				yAxRange[i] = sRange[i];
			}
		}
		maxCross.setLineThickness(2);
		
		yAx2Range[0] = -10;
		setRightAxisVisible(true);
		getPlotPane().setAxisVisible(true, true, true, true);
		setRightAxisRange(-10, 0);
		setRightLabel("Beam gain (dB)");
		setTopAxisRange(xAxRange[0], xAxRange[1]);
		setTopLabel(name);
		setData(null, xAxRange, yAxRange);
//		setPaintPeakPos(true);
	}

	public void plotBeamData(PamDataUnit pamDataUnit, List<BeamOGramDataUnit> collatedBeamOGram, double[] angles) {
		/*
		 * 
		 * The graphics output...
		 */
		this.clearLineData(false);
		switch (nDimensions) {
		case 1:
			plot1DData(pamDataUnit, collatedBeamOGram, angles);
			break;
		case 2:
			plot2DData(pamDataUnit, collatedBeamOGram, angles);
			break;
			
		}

		
	}

	private void plot1DData(PamDataUnit pamDataUnit, List<BeamOGramDataUnit> collatedBeamOGram, double[] angles) {
		double[][] faData = BeamOGramDataUnit.averageFrequencyAngle1Data(collatedBeamOGram);
		showAngle1Gain(collatedBeamOGram, angles);
		PamAxisFX xAx = getPlotPane().getxAxisBottom();
		double x = xAx.getPosition(Math.toDegrees(angles[0]));
		double y = 0;//maxCross.getDHeight()/2;
		addSymbol(maxCross, x, y);
		setData(faData, xAxRange, yAxRange);
	}

	private void plot2DData(PamDataUnit pamDataUnit, List<BeamOGramDataUnit> collatedBeamOGram, double[] angles) {		
		double[][] angleData = BeamOGramDataUnit.averageAngleAngleData(collatedBeamOGram);
		show2Angle1Gain(angleData, angles);

		PamAxisFX xAx = getPlotPane().getxAxisBottom();
		double x = xAx.getPosition(Math.toDegrees(angles[0]));
		PamAxisFX yAx = getPlotPane().getyAxisLeft();
		double y = yAx.getPosition(Math.toDegrees(angles[1]));
		addSymbol(maxCross, x, y);
		
		setData(angleData, xAxRange, yAxRange);
		
	}

	/**
	 * Plot the peak angle 1 line for 2 angle data. 
	 * @param angleData
	 * @param angles
	 */
	private void show2Angle1Gain(double[][] angleData, double[] angles) {
		int[] sRange = beamAlgorithmParams.getBeamOGramSlants();
		int slantBin = (int) Math.round((Math.toDegrees(angles[1]) - sRange[0])/sRange[2]);
		int nAng = angleData.length;
		if (angle1Intervals == null || angle1Intervals.length != nAng) {
			angle1Intervals = new double[nAng];
			int[] aRange = beamAlgorithmParams.getBeamOGramAngles();
			for (int i = 0; i < nAng; i++) {
				angle1Intervals[i] = aRange[0] + aRange[2] * i;
			}
		}
		double[] angle1Peak = new double[nAng];
		double maxVal = Double.MIN_VALUE;
		for (int i = 0; i < nAng; i++) {
			angle1Peak[i] = angleData[i][slantBin];
			maxVal = Math.max(maxVal, angle1Peak[i]);
		}
		double minDat = 0;
		for (int i = 0; i < angle1Peak.length; i++) {
			angle1Peak[i] = 20.*Math.log10(angle1Peak[i]/maxVal);
			minDat = Math.min(minDat, angle1Peak[i]);
		}
		setRightAxisRange(minDat, 0);
		SimpleLineData sld = new SimpleLineData(angle1Intervals, angle1Peak, 
				this.getPlotPane().getxAxisBottom(), this.getPlotPane().getyAxisRight(), Color.WHITE, 2.);
		this.addLineData(sld, true);
		
		
	}

	private void showAngle1Gain(List<BeamOGramDataUnit> collatedBeamOGram, double[] angles) {	
		// this averages angle data over all slants - better to get the line along the 
		// slant peak 
		double[] angleData = BeamOGramDataUnit.getAverageAngle1Data(collatedBeamOGram);		
		
		if (angle1Intervals == null || angle1Intervals.length != angleData.length) {
			angle1Intervals = new double[angleData.length];
			int[] aRange = beamAlgorithmParams.getBeamOGramAngles();
			for (int i = 0; i < angle1Intervals.length; i++) {
				angle1Intervals[i] = aRange[0] + aRange[2] * i;
			}
		}
		double maxDat = angleData[0];
		for (int i = 1; i < angleData.length; i++) {
			maxDat = Math.max(maxDat, angleData[i]);
		}
		double minDat = 0;
		for (int i = 0; i < angleData.length; i++) {
			angleData[i] = 20.*Math.log10(angleData[i]/maxDat);
			minDat = Math.min(minDat, angleData[i]);
		}
		setRightAxisRange(minDat, 0);
		SimpleLineData sld = new SimpleLineData(angle1Intervals, angleData, 
				this.getPlotPane().getxAxisBottom(), this.getPlotPane().getyAxisRight(), Color.WHITE, 4.);
		this.addLineData(sld, true);
	}

}
