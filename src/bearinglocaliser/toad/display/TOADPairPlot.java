package bearinglocaliser.toad.display;

import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.PairBearingLocaliser;
import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;
import bearinglocaliser.display.BearingDataDisplay;
import bearinglocaliser.toad.TOADBearingAlgorithm;
import bearinglocaliser.toad.TOADBearingParams;
import detectionPlotFX.plots.simple2d.Simple2DPlot;
import detectionPlotFX.plots.simple2d.SimpleLineData;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class TOADPairPlot extends Simple2DPlot implements BearingDataDisplay, TOADPlot {

	private TOADBearingAlgorithm toadBearingAlgorithm;
	private BearingLocaliser bearingLocaliser;
	private TOADBearingParams toadAlgorithmParams;

	private double[] correlationRange = {-1.0, 1.0};
	private double[] xAxRange = new double[2];
	private double[] yAxRange = correlationRange;
	
	public TOADPairPlot(TOADBearingAlgorithm toadBearingAlgorithm, String name, BearingLocaliser bearingLocaliser, TOADBearingParams bearingAlgorithmParams) {
		super(name);
		this.toadBearingAlgorithm = toadBearingAlgorithm;
		this.bearingLocaliser = bearingLocaliser;
		this.toadAlgorithmParams = bearingAlgorithmParams;
		int[] aRange = toadAlgorithmParams.getBearingHeadings();
		for (int i = 0; i < 2; i++) {
			xAxRange [i] = aRange[i];
		}
		//		if (bearingLocaliser instanceof MLGridBearingLocaliser2) {
		this.setLeftLabel("Correlation");
		this.setBottomLabel("Angle");
		//		}
		getPlotPane().setAxisVisible(true, false, true, true);
		getPlotPane().setRightSpace(10);
//		setRightAxisRange(-10, 0);
//		setRightLabel("Beam gain (dB)");
		setLeftAxisRange(yAxRange[0], yAxRange[1]);
		setTopAxisRange(xAxRange[0], xAxRange[1]);
		setBottomAxisRange(xAxRange[0], xAxRange[1]);
		setTopLabel(name);
	}
	
	@Override
	public Node getNode() {
		return super.getNode();
	}

	@Override
	public void plotData(PamDataUnit pamDataUnit, BearingLocaliser bearingLocaliser, double[][] locBearings) {
		super.clearLineData(true);
		if (bearingLocaliser instanceof PairBearingLocaliser == false) {
			setData(null, xAxRange, yAxRange);
			return;
		}
		PamAxisFX xAxis = this.getPlotPane().getxAxisBottom();
		PamAxisFX yAxis = this.getPlotPane().getyAxisLeft();
		PairBearingLocaliser pairLocaliser = (PairBearingLocaliser) bearingLocaliser;
		double[] corrData = toadBearingAlgorithm.getCorrelations().getLastCorrelationData();
		if (corrData == null) {
			return;
		}
		double[] xData = new double[corrData.length];
		/**
		 * Work out WTF the angles were for each y value. 
		 */
		double hSep = pairLocaliser.getSpacing()/pairLocaliser.getSpeedOfSound();
		double sampleRate = toadBearingAlgorithm.getFftSourceData().getSampleRate();
		int n2 = xData.length/2;
		for (int i = 0; i < xData.length; i++) {
			double delay = (i-n2)/sampleRate;
			delay /= hSep;
			if (delay < -1) {
				xData[i] = -1.;
			}
			else if (delay > 1.) {
				xData[i] = 181.;
			}
			else {
				xData[i] = 180.-Math.toDegrees(Math.acos(delay));
			}
//			corrData[i] = 0.5;
		}
		SimpleLineData sld = new SimpleLineData(xData, corrData,  xAxis, yAxis);
		sld.setLineStroke(Color.WHITE);
		sld.setLineWidth(3);
		addLineData(sld, true);
		
		// now add a point at where the bearing lies ...
		// seem to need to do the correlation peak search again to acheive this (inefficient !)
		double[] lastPeak = toadBearingAlgorithm.getCorrelations().getLastPeak();
		addSymbol(getPeakSymbol(), xAxis.getPosition(Math.toDegrees(locBearings[0][0])), yAxis.getPosition(lastPeak[1]));
	}

}
