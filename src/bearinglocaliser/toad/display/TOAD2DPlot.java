package bearinglocaliser.toad.display;

import Localiser.algorithms.timeDelayLocalisers.bearingLoc.BearingLocaliser;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.MLGridBearingLocaliser2;
import PamguardMVC.PamDataUnit;
import bearinglocaliser.display.Bearing2DPlot;
import bearinglocaliser.toad.TOADBearingAlgorithm;
import bearinglocaliser.toad.TOADBearingParams;
import detectionPlotFX.plots.simple2d.SimpleLineData;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public class TOAD2DPlot extends Bearing2DPlot implements TOADPlot {

	private TOADBearingAlgorithm toadBearingAlgorithm;
	private BearingLocaliser bearingLocaliser;
	private TOADBearingParams toadAlgorithmParams;
	private double[] xAxRange = new double[2];
	private double[] yAxRange = new double[2];

	public TOAD2DPlot(TOADBearingAlgorithm toadBearingAlgorithm, String name, BearingLocaliser bearingLocaliser, TOADBearingParams bearingAlgorithmParams) {
		super(name);
		this.toadBearingAlgorithm = toadBearingAlgorithm;
		this.bearingLocaliser = bearingLocaliser;
		this.toadAlgorithmParams = bearingAlgorithmParams;
		setPaintPeakPos(true);

		int[] aRange = toadAlgorithmParams.getBearingHeadings();
		for (int i = 0; i < 2; i++) {
			xAxRange [i] = aRange[i];
		}

		this.setLeftLabel("Secondary Angle");
		this.setBottomLabel("Primary Angle");
		int[] sRange = toadAlgorithmParams.getBearingSlants();
		for (int i = 0; i < 2; i++) {
			yAxRange[i] = sRange[i];
		}

		getPlotPane().setAxisVisible(true, true, true, true);
		setRightAxisRange(-20, 0);
		setRightLabel("Log liklihood");
		setTopAxisRange(xAxRange[0], xAxRange[1]);
		setTopLabel(name);
		setData(null, xAxRange, yAxRange);
	}

	public void plotData(PamDataUnit pamDataUnit, BearingLocaliser bearingLocaliser, double[][] locBearings) {
		if (bearingLocaliser instanceof MLGridBearingLocaliser2) {
			plotML2Data(pamDataUnit, (MLGridBearingLocaliser2) bearingLocaliser, locBearings);
		}
		else {
			clearLineData(false);
			setData(null, xAxRange, yAxRange);
		}
	}

	private void plotML2Data(PamDataUnit pamDataUnit, MLGridBearingLocaliser2 mlBearingLocaliser2,
			double[][] locBearings) {
		double[][] llut = mlBearingLocaliser2.getLikelihoodLUT();
		clearLineData(false);
		if (llut == null || llut.length == 0) {
			setData(null, xAxRange, yAxRange);
			return;
		}
		setData(llut, xAxRange, yAxRange);
		PamAxisFX xAxis = this.getPlotPane().getxAxisBottom();
		PamAxisFX yAxis = this.getPlotPane().getyAxisRight();
		// need to get the maximum line out of the llut data. 
		int nThet = llut.length;
		int nPhi = llut[0].length;
		double maxVal = llut[0][0];
		int maxThet = 0, maxPhi = 0;
		for (int i = 0; i < nPhi; i++) {
			for (int j = 0; j < nThet; j++) {
				if (llut[j][i] > maxVal) {
					maxVal = llut[j][i];
					maxThet = j;
					maxPhi = i;
				}
			}
		}
		double[] thetLL = new double[nThet];
		double[] thetPoints = new double[nThet];
		double thetStep = (xAxRange[1]-xAxRange[0])/(nThet-1);
		for (int j = 0; j < nThet; j++) {
			thetLL[j] = llut[j][maxPhi];
			thetPoints[j] = xAxRange[0] + thetStep*j; 
		}
		SimpleLineData sld = new SimpleLineData(thetPoints, thetLL, xAxis, yAxis, Color.WHITE, 4.);
		addLineData(sld, true);
		
		// also plot the peak point...
		double x = Math.toDegrees(locBearings[0][0]);
		double y = 0;
		if (locBearings[0].length > 1) {
			y = Math.toDegrees(locBearings[1][0]);
			javafx.geometry.Point2D peakPt = new javafx.geometry.Point2D(x,y);
			setPeakPoint(peakPt);
		}
		else {
			// add a point in the middle of the plot
			x = xAxis.getPosition(x);
			y = getPlotPane().getPlotCanvas().getHeight()/2;
			addSymbol(getPeakSymbol(), new Point2D(x, y));
			setPeakPoint(null);
			setLeftLabel("No secondary angle information");
		}
	}

}
