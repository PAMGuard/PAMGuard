package PamView.chart.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTextField;

import Layout.PamAxis;
import PamView.PamSymbol;
import PamView.chart.PamChart;
import PamView.chart.PamChartRenderer;
import PamView.chart.PamChartSeries;
import PamView.chart.SeriesType;
import PamView.symbol.SymbolData;
import reportWriter.ReportFactory;

public class SwingChartRenderer extends PamChartRenderer {
	
	private Font axisFont;
	
	private Font titleFont;

	/**
	 * @param pamChart
	 */
	public SwingChartRenderer(PamChart pamChart) {
		super(pamChart);
	}
	
	/**
	 * Render the graph into the given graphics handle. Two are given 
	 * since this may be used with nested windows, i.e. with the plot within the 
	 * border. If this IS the case, then the border insets are needed to define
	 * where on the border to draw axis, etc. 
	 * @param border
	 * @param plot
	 * @param borderInsets
	 * @return
	 * @throws ChartRenderException 
	 */
	public boolean renderChart(Graphics border, Dimension dimension, Graphics plot, Insets borderInsets) throws ChartRenderException {
		renderborder(border, dimension, borderInsets, false);
		renderPlot(plot, dimension, null);
		return true;
	}
	
	/**
	 * Render the chart onto a single graphics handle. 
	 * Calculate required border insets and use accordingly ...
	 * @param g
	 * @return
	 * @throws ChartRenderException 
	 */
	public boolean renderChart(Graphics g, Dimension dimension) throws ChartRenderException {
		Insets borderInsets = calculateInsets(g);
		renderborder(g, dimension, borderInsets, true);
		renderPlot(g, dimension, borderInsets);
		return true;
	}
	
	public Insets calculateInsets(Graphics g) {
		int top, bottom, left, right;
		FontMetrics axisFontMetrics = g.getFontMetrics(getAxisFont());
		top = axisFontMetrics.stringWidth("00");
		bottom = top;
		left = right = top;
		PamChart pamChart = getPamChart();
		String title = pamChart.getChartTitle();
		if (title != null) {
			top += g.getFontMetrics(getTitleFont()).getHeight();
		}
		if (pamChart.getNorthAxis() != null) {
			top += pamChart.getNorthAxis().getExtent(g);
		}
		if (pamChart.getWestAxis() != null) {
			left += pamChart.getWestAxis().getExtent(g);
		}
		if (pamChart.getSouthAxis() != null) {
			bottom += pamChart.getSouthAxis().getExtent(g);
		}
		if (pamChart.getEastAxis() != null) {
			right += pamChart.getEastAxis().getExtent(g);
		}
		return new Insets(top, left, bottom, right);		
	}
	
	/**
	 * Render the plot border. 
	 * The insets for the plot are (probably) never null since they are needed to inform where 
	 * to paint the axis. 
	 * @param g
	 * @param plotInsets
	 * @return
	 */
	public boolean renderborder(Graphics g, Dimension dimension, Insets plotInsets, boolean drawBox) {
		setAntialiasing(g);
		PamAxis axis;
		PamChart pamChart = getPamChart();
		axis = pamChart.getSouthAxis();
		if (axis != null) {
			axis.drawAxis(g, plotInsets.left, dimension.height-plotInsets.bottom, 
					dimension.width-plotInsets.right, dimension.height-plotInsets.bottom);
		}
		axis = pamChart.getWestAxis();
		if (axis != null) {
			axis.drawAxis(g, plotInsets.left, dimension.height-plotInsets.bottom, 
					plotInsets.left, plotInsets.top);
		}
		axis = pamChart.getNorthAxis();
		if (axis != null) {
			axis.drawAxis(g, plotInsets.left, plotInsets.top, dimension.width-plotInsets.right, plotInsets.top);
		}
		axis = pamChart.getEastAxis();
		if (axis != null) {
			axis.drawAxis(g, dimension.width-plotInsets.right, dimension.height-plotInsets.bottom, 
					dimension.width-plotInsets.right, plotInsets.top);
		}
		if (drawBox) {
			g.drawRect(plotInsets.left, plotInsets.top, dimension.width-plotInsets.right-plotInsets.left, 
					dimension.height-plotInsets.bottom-plotInsets.top);
		}
		String tit = pamChart.getChartTitle();
		if (tit != null && tit.length() != 0) {
			Font titFont = getTitleFont();
			g.setFont(getTitleFont());
			FontMetrics fm = g.getFontMetrics(titFont);
			int w = fm.stringWidth(tit);
			int h = fm.getMaxAscent();
			int x = (dimension.width-plotInsets.right+plotInsets.left-w)/2;
			int y = h+1;
			if (pamChart.getNorthAxis() == null) {
				y = plotInsets.top - fm.getDescent() - 1;
			}
			g.drawString(tit, x, y);
		}
		return true;
	}

	/**
	 * Render the plot. If the graphics handle for this plot is different to that of 
	 * the border, then it's likely insets will be 0 or null. If it's a single graphics
	 * object the whole thing is being written to, then the insets will need to be no zero 
	 * to allow for axis. 
	 * @param g graphics handle to plot area
	 * @param dimension plot dimensions
	 * @param plotInsets plot insets
	 * @return OK if drawn 
	 * @throws ChartRenderException 
	 */
	public boolean renderPlot(Graphics g, Dimension dimension, Insets plotInsets) throws ChartRenderException {
		setAntialiasing(g);
		if (plotInsets == null) {
			plotInsets = new Insets(0,0,0,0);
		}
		ArrayList<PamChartSeries> chartSeries = getPamChart().getChartSeries();
		for (PamChartSeries series:chartSeries) {
			plotChartSeries(g, dimension, plotInsets, series);
		}
		// redraw border box
		g.setColor(Color.BLACK);
		int x = plotInsets.left;
		int y = plotInsets.top;
		int width = (int) (dimension.getWidth()-plotInsets.left-plotInsets.right);
		int height = (int) (dimension.getHeight()-plotInsets.top-plotInsets.bottom);
		g.drawRect(x, y, width, height);
		
		
		return renderKey(g, dimension, plotInsets);
	}

	/**
	 * Draw the key on the main plot area. 
	 * @param g graphics handle to plot area
	 * @param dimension plot dimensions
	 * @param plotInsets plot insets
	 * @return OK if drawn 
	 */
	public boolean renderKey(Graphics g, Dimension dimension, Insets plotInsets) {

		if (plotInsets == null) {
			plotInsets = new Insets(0,0,0,0);
		}
		ArrayList<PamChartSeries> chartSeries = getPamChart().getChartSeries();
		// make the plot key then crop it and draw it just inside the border. 
		BufferedImage key = new BufferedImage((int)dimension.getWidth(), (int)dimension.getHeight(), 
				BufferedImage.TYPE_INT_RGB);
		Graphics2D kg = key.createGraphics();
//	    AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
//	    kg.setComposite(composite);
//		kg.setColor(Color.WHITE);
		Color keyCol = new Color(255,255,255);
		kg.setColor(keyCol);
//		key.
		kg.fillRect(0, 0, (int)dimension.getWidth(), (int)dimension.getHeight());
		int fontHeight = kg.getFontMetrics().getHeight();
		int x = 2;
		int y = 2 + fontHeight;
		int x1, y1;
		for (PamChartSeries series:chartSeries) {
			SymbolData symbolData = series.getSymbolData();
			if(symbolData == null) {
				symbolData = new SymbolData();
			}
			if (series.getSeriesName() == null) {
				continue;
			}
			switch(series.getSeriesType()) {
			case BOTTOMBAR:
			case HANGINGBAR:
				Rectangle r = new Rectangle(x, y, fontHeight, fontHeight);
				kg.setColor(symbolData.getFillColor());
				kg.fillRect(x,y-fontHeight,fontHeight,fontHeight);
				kg.setColor(symbolData.getLineColor());
				kg.drawRect(x,y-fontHeight,fontHeight,fontHeight);
				x1 = x + fontHeight + 2;
				kg.setColor(Color.BLACK);
				kg.drawString(series.getSeriesName(), x1, y);
				break;
			case LINE:
				Color lineCol = series.getLineColor();
				if (lineCol == null) {
					lineCol = Color.BLACK;
				}
				kg.setColor(lineCol);
				y1 = y - kg.getFontMetrics().getAscent()/2;
				x1 = x + fontHeight;
				kg.drawLine(x, y1, x1, y1);
				x1 = x + fontHeight + 2;
				kg.setColor(Color.BLACK);
				kg.drawString(series.getSeriesName(), x1, y);
				break;
			case SCATTER:
				PamSymbol symbol = new PamSymbol(series.getSymbolData());
				x1 = x + symbol.getWidth()/2;
				y1 = y - symbol.getHeight()/2;
				symbol.draw(kg, new Point(x1, y1));
				x1 = x + fontHeight + 2;
				kg.setColor(Color.BLACK);
				kg.drawString(series.getSeriesName(), x1, y);
				break;
			default:
				break;
			}
			y+=fontHeight+1;
		}
		BufferedImage croppedKey = ReportFactory.trimImage(key, keyCol, null);
		if (croppedKey != null) {
			kg = (Graphics2D) croppedKey.getGraphics();
			kg.setColor(Color.BLACK);
			kg.drawRect(0, 0, croppedKey.getWidth()-1, croppedKey.getHeight()-1);
			x = dimension.width-plotInsets.right-croppedKey.getWidth()-2;
			y = plotInsets.top + (int) (dimension.getHeight()-plotInsets.bottom-plotInsets.top)/6;
//			g.drawImage(croppedKey, x,  y, null);
			drawTransparentImage(g, croppedKey, x, y, keyCol, 230);
		}
		
		return true;
	}
	
	/**
	 * Draw a transparent image into g at x,y assuming all pixels of transparentcol are transparent
	 * @param g
	 * @param image
	 * @param x
	 * @param y
	 * @param transparentCol
	 * @param alpha
	 */
	private void drawTransparentImage(Graphics g, BufferedImage image,  int x, int y, Color transparentCol, int alpha) {
		if (transparentCol == null) {
			transparentCol = Color.WHITE;
		}
//		Rectangle gRect = g.getClipBounds();
		int imWidth = image.getWidth();
		int imHeight = image.getHeight();
//		imWidth = Math.min(imWidth, gRect.width-x);
//		imHeight = Math.min(imHeight, gRect.height-y);
		WritableRaster raster = image.getRaster();
		int[] imCol = new int[3];
		int[] tCol = {transparentCol.getRed(), transparentCol.getGreen(), transparentCol.getBlue()};
//		Color tRepCol = null; 
//		if (alpha > 0) {
////			tRepCol = new Color(tCol[0], tCol[1], tCol[2], alpha);
//			tRepCol = new Color(.5f, .5f, .5f, .9f);
//		}
		for (int ix = 0, dx = x; ix < imWidth; ix++, dx++) {
			for (int iy = 0, dy = y; iy < imHeight; iy++, dy++) {
				imCol = raster.getPixel(ix, iy, imCol);
				if (Arrays.equals(imCol, tCol)) {
//					if (tRepCol == null) {
						continue;
//					}
//					else {
//						g.setColor(tRepCol);
//					}
				}
				else {
					g.setColor(new Color(imCol[0], imCol[1], imCol[2]));
					g.drawRect(dx, dy, 0, 0); // draw 0 size to do a single pixel
				}
			}
		}
	}
	
	private void setAntialiasing(Graphics g) {
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D)g;
			RenderingHints rh = new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHints(rh);
		}
	}

	private boolean plotChartSeries(Graphics g, Dimension dimension, Insets plotInsets, PamChartSeries series) throws ChartRenderException {
		PamAxis yAxis = series.getSeriesYAxis();
		PamChart pamChart = getPamChart();
		if (yAxis == null) {
			yAxis = pamChart.getWestAxis();
			if (yAxis == null) {
				yAxis = pamChart.getEastAxis();
				if (yAxis == null) {
					throw new ChartRenderException("No y axis found for series " + series.getSeriesName());
				}
			}
		}
		PamAxis xAxis = pamChart.getSouthAxis();
		if (xAxis == null) {
			// no exception on this. Assume it's there. 
			xAxis = pamChart.getNorthAxis();
		}
		switch (series.getSeriesType()) {
		case BOTTOMBAR:
		case HANGINGBAR:
			plotBarChart(g, dimension, plotInsets, series, xAxis, yAxis);
			break;
		case LINE:
		case SCATTER:
			plotXYChart(g, dimension, plotInsets, series, xAxis, yAxis);
			break;
		default:
			break;
		
		}
		
		return true;
	}

	private void plotXYChart(Graphics g, Dimension dimension, Insets plotInsets, PamChartSeries series, PamAxis xAxis, PamAxis yAxis) {
		Graphics2D g2d = null;
		if (g instanceof Graphics2D) {
			g2d = (Graphics2D) g;
		}
		double[] x = series.getxValues();
		double[] y = series.getyValues();
		if (x.length == 0) {
			return;
		}
		SymbolData symbolData = series.getSymbolData();
		double x1, x2, y1, y2;
		if (series.getSeriesType() == SeriesType.LINE) {
			Color lineCol = series.getLineColor();
			if (lineCol == null) {
				if (symbolData != null) {
					lineCol = symbolData.getLineColor();
				}
				else {
					lineCol = Color.BLACK;	
				}
			}
			float lineWid = series.getLineWidth();
			if (g2d != null) {
				g2d.setStroke(new BasicStroke(lineWid));
			}
			g.setColor(lineCol);
			x1 = xAxis.getOuterPosition(x[0]);
			y1 = yAxis.getOuterPosition(y[0]);
			for (int i = 1; i < x.length; i++) {
				x2 = xAxis.getOuterPosition(x[i]);
				y2 = yAxis.getOuterPosition(y[i]);
				g.drawLine((int) (x1+.5), (int) (y1+.5), (int) (x2+.5), (int) (y2+.5));
				x1 = x2;
				y1 = y2;
			}
		}
		if(symbolData != null) {
			PamSymbol symbol = new PamSymbol(symbolData);
		for (int i = 0; i < x.length; i++) {
			x2 = xAxis.getOuterPosition(x[i]);
			y2 = yAxis.getOuterPosition(y[i]);
			symbol.draw(g, new Point((int) (x2+.5), (int) (y2+.5)));
		}		
		}
	}

	protected void plotBarChart(Graphics g, Dimension dimension, Insets plotInsets, PamChartSeries series, PamAxis xAxis, PamAxis yAxis) {
		double[] x = series.getxValues();
		double[] y = series.getyValues();
		if (x.length == 0) {
			return;
		}
		SymbolData symbolData = series.getSymbolData();
		if(symbolData == null) {
			symbolData = new SymbolData();
		}
		double[] binEdges;
		if (x.length == y.length+1) {
			binEdges = x;
		}
		
		else {
			binEdges = new double[x.length+1];
			double binWidth = x[0];
			if (x.length > 1) {
				binWidth = x[1]-x[0];
				binEdges[0] = x[0]-binWidth/2;
				for (int i = 0; i < x.length; i++) {
					binEdges[i+1] = x[i]+binWidth/2;
				}
			}
		}
		// should now have a bin width array which is 1> y value array.
		int yZero = dimension.height - plotInsets.bottom;
		for (int i = 0; i < y.length; i++) {
			int yP = (int) (yAxis.getOuterPosition(y[i]) + 0.5);
			int y0 = (int) (yAxis.getOuterPosition(0) + 0.5);
			int x1 = (int) (xAxis.getOuterPosition(binEdges[i]) + 0.5);
			int x2 = (int) (xAxis.getOuterPosition(binEdges[i+1]) + 0.5);
			if (symbolData.fill) {
				g.setColor(symbolData.getFillColor());
				g.fillRect(x1, Math.min(y0, yP), x2-x1, Math.abs(y0-yP));
			}
			g.setColor(symbolData.getLineColor());
			g.drawLine(x1, y0, x1, yP);
			g.drawLine(x1, yP, x2, yP);
			g.drawLine(x2, yP, x2, y0);
		}
	}

	/**
	 * @return the axisFont
	 */
	protected Font getAxisFont() {
		if (axisFont == null) {
			axisFont = getDefaultFont();
		}
		return axisFont;
	}

	/**
	 * @param axisFont the axisFont to set
	 */
	protected void setAxisFont(Font axisFont) {
		this.axisFont = axisFont;
	}

	/**
	 * @return the titleFont
	 */
	protected Font getTitleFont() {
		if (titleFont == null) {
			titleFont = getDefaultFont();
		}
		return titleFont;
	}
	
	private Font getDefaultFont() {
		JTextField oa = new JTextField();
		return oa.getFont();
	}

	/**
	 * @param titleFont the titleFont to set
	 */
	protected void setTitleFont(Font titleFont) {
		this.titleFont = titleFont;
	}
	
	
	
}
