package beamformer.localiser.plot;

import PamUtils.PamUtils;
import PamView.PamSymbolType;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamGroupProcess;
import beamformer.localiser.BeamFormLocaliserControl;
import beamformer.localiser.BeamLocaliserData;
import detectionPlotFX.plots.simple2d.Simple2DPlot;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.pamAxis.PamAxisFX;

public abstract class BeamDataDisplay {
	
	private Beam2DPlot simplePlot;
	
	private boolean paintPeakLine;
	
	private PamBorderPane borderPane;
	
	private Label plotTitle;
	
	private PamSymbolFX maxCross = new PamSymbolFX(PamSymbolType.SYMBOL_CROSS2, 10, 10, false, Color.WHITE, Color.WHITE);
	
	private double[] linePlotData;

	private BeamFormLocaliserControl bfLocControl;

	private int channelMap;

	private String plotName;

	public BeamDataDisplay(BeamFormLocaliserControl bfLocControl, int channelMap, String plotName, String xAxisLabel, String yAxisLabel) {
		super();
		this.bfLocControl = bfLocControl;
		this.channelMap = channelMap;
		this.plotName = plotName;
		borderPane = new PamBorderPane();
		simplePlot = new Beam2DPlot(plotName);
		borderPane.setCenter(simplePlot.getNode());
		borderPane.setTop(plotTitle = new Label(makePlotName(plotName, channelMap)));
		borderPane.setRightSpace(10);
		plotTitle.setTextAlignment(TextAlignment.CENTER);
		BorderPane.setAlignment(plotTitle, Pos.CENTER);
		HBox.setHgrow(borderPane, Priority.ALWAYS);
		simplePlot.setBottomLabel(xAxisLabel);
		simplePlot.setLeftLabel(yAxisLabel);
	}
	
	private String makePlotName(String plotName, int channelMap) {
		if (channelMap == 0) {
			return plotName;
		}
		else {
			return plotName + " ch " + PamUtils.getChannelList(channelMap);
		}
	}


	public Node getNode() {
		return borderPane;
	}

	public abstract void update(BeamLocaliserData beamLocData);

	/**
	 * @return the simplePlot
	 */
	public Simple2DPlot getSimplePlot() {
		return simplePlot;
	}
	
	private class Beam2DPlot extends Simple2DPlot {

		public Beam2DPlot(String unitName) {
			super(unitName);
			getPlotPane().setEmptyBorders(0, 10, 0, 0);
		}

		/* (non-Javadoc)
		 * @see detectionPlotFX.plots.Simple2DPlot#repaint()
		 */
		@Override
		public void repaint() {
			super.repaint();
			if (paintPeakLine) {
				drawScaledPeakLine();
			}
		}
		
	}

	public void drawScaledPeakLine() {
		if (linePlotData == null || linePlotData.length < 2) {
			return;
		}
		PamAxisFX[] allAx = simplePlot.getPlotPane().getAllAxis();
		PamAxisFX yRAxis = allAx[1];
		double minVal = yRAxis.getMinVal();
		double maxVal = yRAxis.getMaxVal();
		int nX = Math.max(2, linePlotData.length);
		Canvas c = simplePlot.getPlotPane().getPlotCanvas();
		GraphicsContext gc = c.getGraphicsContext2D();
		double xScale = c.getWidth() / (nX-1);
		double yScale = c.getHeight() / (maxVal-minVal);
		double h = c.getHeight();
		double x1, y1, x2, y2;
		x1 = 0;
		y1 = h-(linePlotData[0]-minVal)*yScale;
		gc.setStroke(Color.WHITESMOKE);
		gc.setLineWidth(3);
		int peakInd = 0;
		double maxV = linePlotData[0];
		for (int i = 1; i < nX; i++) {
			x2 = i*xScale;
			y2 = h-(linePlotData[i]-minVal)*yScale;
			gc.strokeLine(x1, y1, x2, y2);
			if (linePlotData[i] > maxV) {
				maxV = linePlotData[i];
				peakInd = i;
			}
			x1 = x2;
			y1 = y2;
		}
		x1 = peakInd*xScale;
		y1 = h-(linePlotData[peakInd]-minVal)*yScale;
		maxCross.draw(gc, new Point2D(x1, y1));
		
	}

	/**
	 * @return the paintPeakLine
	 */
	public boolean isPaintPeakLine() {
		return paintPeakLine;
	}

	/**
	 * @param paintPeakLine the paintPeakLine to set
	 */
	public void setPaintPeakLine(boolean paintPeakLine) {
		this.paintPeakLine = paintPeakLine;
		simplePlot.getPlotPane().setAxisVisible(false, paintPeakLine, true, true);
		PamAxisFX[] allAx = simplePlot.getPlotPane().getAllAxis();
		PamAxisFX yRAxis = allAx[1];
		yRAxis.setRange(-30, 0);
		yRAxis.setLabel("Peak Amplitude (dB)");
	}


	/**
	 * @return the linePlotData
	 */
	public double[] getLinePlotData() {
		return linePlotData;
	}
	
	/**
	 * Find the algorithm parameters for a channel map. 
	 * @param channelMap channel map from beam former output 
	 * @return corresponding algorithm parameters. 
	 */
	public BeamAlgorithmParams findAlgorithmParams(int channelMap) {
		BeamFormerBaseProcess bfProcess = getBfLocControl().getBeamFormerProcess();
		BeamGroupProcess groupProcess = bfProcess.findGroupProcess(channelMap);
		if (groupProcess == null) {
			return null;
		}
		return groupProcess.getAlgorithmParams();
	}


	/**
	 * @param linePlotData the linePlotData to set
	 */
	public void setLinePlotData(double[] linePlotData, double scaleMin, double scaleMax) {
		this.linePlotData = linePlotData;
		PamAxisFX[] allAx = simplePlot.getPlotPane().getAllAxis();
		PamAxisFX yRAxis = allAx[1];
		yRAxis.setRange(scaleMin, scaleMax);
	}


	/**
	 * @return the bfLocControl
	 */
	public BeamFormLocaliserControl getBfLocControl() {
		return bfLocControl;
	}


	/**
	 * @return the channelMap
	 */
	public int getChannelMap() {
		return channelMap;
	}


	/**
	 * @param channelMap the channelMap to set
	 */
	public void setChannelMap(int channelMap) {
		this.channelMap = channelMap;
	}


}
