package pamMaths;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;

/**
 * Provide a basic display panel for showing data from any number of
 * PamHistograms, either in a single panel, or as a series of sub panels.
 *  
 * @author Douglas Gillespie
 *
 */
public class HistogramDisplay extends Object implements Observer {

	private ArrayList<PamHistogram> pamHistograms;

	private HistoPanel histoPanel;

	protected PamAxis westAxis, southAxis;

	private HistoPlotPanel histoPlotPanel;

	private HistoAxisPanel histoAxisPanel;

	private HistoStatsWindow histoStatsWindow;

	private boolean showStats = true;

	private String xLabel, yLabel;

	private int statsWindowPos = CornerLayoutContraint.FIRST_LINE_END;

	private HistogramGraphicsLayer graphicsOverLayer, graphicsUnderLayer;

	public static final int STATS_N = 0x1;
	public static final int STATS_MEAN = 0x2;
	public static final int STATS_STD = 0x4;
	public static final int STATS_SKEW = 0x8;
	public static final int STATS_UNDER = 0x10;
	public static final int STATS_OVER = 0x20;
	
	public int selectedStats = 0x3F;

	public HistogramDisplay(PamHistogram pamHistogram) {

		pamHistograms = new ArrayList<PamHistogram>();
		pamHistograms.add(pamHistogram);
		pamHistogram.addObserver(this);

		histoPanel = new HistoPanel();
	}

	public HistogramDisplay() {

		pamHistograms = new ArrayList<PamHistogram>();

		histoPanel = new HistoPanel();

	}

	public void addHistogram(PamHistogram pamHistogram) {
		pamHistograms.add(pamHistogram);
		pamHistogram.addObserver(this);
	}

	public void removeHistogram(PamHistogram pamHistogram) {
		pamHistograms.remove(pamHistogram);
		pamHistogram.deleteObserver(this);
	}

	public void removeAllHistograms(PamHistogram pamHistogram) {
		for (int i = 0; i < pamHistograms.size(); i++) {
			pamHistograms.get(i).deleteObserver(this);
		}
		pamHistograms.clear();
	}

	public JComponent getGraphicComponent() {
		return histoPanel;
	}

	public String getXLabel() {
		return xLabel;
	}

	public void setXLabel(String label) {
		xLabel = label;
		histoAxisPanel.getSouthAxis().setLabel(label);
		histoPanel.histoBorderPanel.repaint();
	}

	public String getYLabel() {
		return yLabel;
	}

	public void setYLabel(String label) {
		yLabel = label;
		westAxis.setLabel(label);
		histoPanel.histoBorderPanel.repaint();
	}

	public void setXAxisNumberFormat(String format) {
		histoAxisPanel.getSouthAxis().setFormat(format);
	}

	public void update(Observable o, Object arg) {
		if (arg == o) {
			// it's the histogram itself which has changed, so redraw the axis.
			newHistoRanges();
		}
		else if (histoPanel != null && histoPanel.histoBorderPanel != null) {
			histoPanel.histoBorderPanel.repaintPlots();
		}

	}
	private void newHistoRanges() {
		histoAxisPanel.getSouthAxis().setMinVal(getXScaleMin());
		histoAxisPanel.getSouthAxis().setMaxVal(getXScaleMax());
	}

	public void setStatsWindowPos(int statsWindowPos) {
		this.statsWindowPos = statsWindowPos;
		if (histoPlotPanel != null) {
			histoPlotPanel.setKeyPosition(statsWindowPos);
		}
	}

	private double getXScaleMin() {
		double minVal = 0;
		if (pamHistograms.size() > 0){
			minVal = pamHistograms.get(0).getScaleMinVal();
			for (int i = 1; i < pamHistograms.size(); i++) {
				minVal  = Math.min(minVal, pamHistograms.get(i).getScaleMinVal());
			}
		}
		return minVal;
	}


	private double getXScaleMax() {
		double maxVal = 1;
		if (pamHistograms.size() > 0){
			maxVal = pamHistograms.get(0).getScaleMaxVal();
			for (int i = 1; i < pamHistograms.size(); i++) {
				maxVal  = Math.max(maxVal, pamHistograms.get(i).getScaleMaxVal());
			}
		}
		return maxVal;
	}
	private double getYScaleMin() {
		return 0;
	}

	private double getPlotYScaleMax() {

		return PamAxis.getDefaultScaleEnd(getYScaleMax()*1.05, 1);

	}
	private double getYScaleMax() {
		double maxVal = 1;
		if (pamHistograms.size() > 0){
			maxVal = pamHistograms.get(0).getMaxContent();
			for (int i = 1; i < pamHistograms.size(); i++) {
				maxVal  = Math.max(maxVal, pamHistograms.get(i).getMaxContent());
			}
		}
		return maxVal;
	}

	public void repaint() {
		histoPanel.repaint();
	}

	/**
	 * One histoPanel may contain many axis panels, each with one histogram
	 * or it may contain a single panel, wiht multiple histograms overlaid. 
	 * @author Douglas Gillespie
	 *
	 */
	class HistoPanel extends PamBorderPanel {

		HistoBorderPanel histoBorderPanel;

		//		Timer repaintTimer;

		HistoPanel() {

			setLayout(new BorderLayout());

			add(histoBorderPanel = new HistoBorderPanel(), BorderLayout.CENTER);

			//			repaintTimer = new Timer(2000, new TimerAction());
			//			repaintTimer.start();
		}

		//		class TimerAction implements ActionListener {
		//
		//			public void actionPerformed(ActionEvent e) {
		//				if (histoBorderPanel != null) {
		//					histoBorderPanel.repaintPlots();
		//				}
		//			}
		//			
		//		}
	}


	/**
	 * Handles layout and data for a histogram or series of histograms. 
	 * @author Douglas Gillespie
	 *
	 */
	class HistoBorderPanel extends PamBorderPanel {


		private PamLabel titleLabel;

		public HistoBorderPanel() {
			super();
			setLayout(new BorderLayout());
			add(BorderLayout.CENTER, histoAxisPanel = new HistoAxisPanel());
			setTitle(getAxisTitle());
		}

		public void repaintPlots() {
			histoAxisPanel.checkScales();
			histoPlotPanel.repaint();
			if (showStats) {
				histoStatsWindow.updateWindow();
			}
		}

		public void setTitle(String title) {
			clearTitle();
			if (title != null) {
				titleLabel = new PamLabel(title);
				titleLabel.setFont(PamColors.getInstance().getBoldFont());
				titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
				//				titleLabel.setFont(paml)
				this.add(BorderLayout.NORTH, titleLabel);
			}
		}
		private void clearTitle() {
			if (titleLabel != null) {
				this.remove(titleLabel);
				titleLabel = null;
			}
		}

		private String getAxisTitle() {
			if (pamHistograms.size() > 0 && pamHistograms.get(0).getName() != null){
				String allName = new String(pamHistograms.get(0).getName());
				for (int i = 1; i < pamHistograms.size(); i++) {
					allName += String.format(" + %s", pamHistograms.get(i).getName());
				}
				return allName;
			}
			else {
				return null;
			}
		}

	}
	class HistoAxisPanel extends PamAxisPanel {

		public HistoAxisPanel() {
			super();
			westAxis = new PamAxis(0, 0, 1, 1, 0, 1., PamAxis.ABOVE_LEFT, null, PamAxis.LABEL_NEAR_CENTRE, "%d");
			southAxis = new PamAxis(0, 0, 1, 1, 0, 1., PamAxis.BELOW_RIGHT, "", PamAxis.LABEL_NEAR_CENTRE, "%3.1f");
			if (pamHistograms.size() > 0 && pamHistograms.get(0).isBinCentres()) {
				southAxis.setForceFirstVal(true, pamHistograms.get(0).getMinVal());
			}
			setWestAxis(westAxis);
			setSouthAxis(southAxis);
			setInnerPanel(histoPlotPanel = new HistoPlotPanel());

			setAutoInsets(true);
			//				setTitle(getAxisTitle());
			checkScales();
			//				PamColors.getInstance().registerComponent(this, PamColor.BORDER);
		}


		public void checkScales() {
			boolean repaintNeeded = false;
			double v;
			v = getXScaleMax();
			if (v != southAxis.getMaxVal()) {
				southAxis.setMaxVal(v);
				repaintNeeded = true;
			}
			v = getXScaleMin();
			if (v != southAxis.getMinVal()) {
				southAxis.setMinVal(v);
				repaintNeeded = true;
			}

			v = getYScaleMin();
			if (v != westAxis.getMinVal()) {
				westAxis.setMinVal(v);
				repaintNeeded = true;
			}

			v = getPlotYScaleMax();
			if (v != westAxis.getMaxVal()) {
				westAxis.setMaxVal(v);
				repaintNeeded = true;
			}

			if (repaintNeeded) {
				this.repaint();
			}
		}
	}

	public class HistoPlotPanel extends JPanelWithPamKey {

		public HistoPlotPanel() {
			super();
			//				PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
			this.setDefaultColor(PamColor.PlOTWINDOW);
			setBorder(PamBorder.createInnerBorder());
			//				if (showStats) {
			histoStatsWindow = new HistoStatsWindow();
			setKeyPanel(histoStatsWindow);
			setStatsWindowPos(statsWindowPos);
			//				}
			setShowStats(isShowStats());
		}

		@Override
		protected void paintComponent(Graphics g) {
			histoAxisPanel.checkScales();
			super.paintComponent(g);
			if (graphicsUnderLayer != null) {
				graphicsUnderLayer.paintLayer(g);
			}
			for (int i = 0; i < pamHistograms.size(); i++) {
				paintHistogram(g, pamHistograms.get(i));
			}
			if (graphicsOverLayer != null) {
				graphicsOverLayer.paintLayer(g);
			}

		}
		private void paintHistogram(Graphics g, PamHistogram h) {
			Graphics2D g2d = (Graphics2D) g;
			double scaleStep = h.getStep();
			double minXVal = h.getScaleMinVal();
			double maxXVal = h.getScaleMaxVal();
			double xScale = getWidth() / (maxXVal - minXVal);
			double maxYVal = westAxis.getMaxVal();
			double minYVal = westAxis.getMinVal();
			double yScale = getHeight() / (maxYVal - minYVal);

			g2d.setColor(PamColors.getInstance().getColor(PamColor.PLAIN));

			double[] data = h.getData();
			int x0 = 0, x1, y0=getHeight()-1, y1;
			for (int i = 0; i < data.length; i++) {
				if (data[i] == 0) continue;
				x0 = (int) (i * scaleStep * xScale); 
				x1 = (int) (scaleStep * xScale);
				y1 = (int) (data[i] * yScale);
				g2d.drawRect(x0, y0-y1, x1, y1);
			}
		}

	}
	class HistoStatsWindow extends KeyPanel {

		int nHistos = 0;
		PamLabel[] labels;
		PamLabel[][] numbers;
		String[] rowLabels = {"N", "Mean ", "STD ", "Skew ", "Under ", "Over "};

		public HistoStatsWindow() {
			super("Stats", 0);
			//				PamColors.getInstance().registerComponent(this.getPanel(), PamColor.PlOTWINDOW);
			this.getPanel().setLayout(new GridBagLayout());
			//				this.getPanel().setBorder(new TitledBorder(new LineBorder(Color.BLACK)));
			getPanel().setOpaque(false);
			updateWindow();
		}

		private int oldStatsSelection = -1;
		public void updateWindow() {
			int nRows = rowLabels.length;
			int iRow;
			if (numbers == null || numbers.length == 0) {
				return;
			}
			// check all the controls are there and lay them out
			if (numbers == null || numbers.length != pamHistograms.size() || 
					numbers[0].length != rowLabels.length || selectedStats != oldStatsSelection) {
				getPanel().removeAll();
				oldStatsSelection = selectedStats;
				labels = new PamLabel[nRows];
				numbers = new PamLabel[pamHistograms.size()][nRows];
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = c.gridy = 0;
				c.fill = GridBagConstraints.HORIZONTAL;
				for (int i = 0; i < nRows; i++) {
					c.gridx = 0;
					PamBorderPanel.addComponent(getPanel(), labels[i] = new PamLabel(), c);
					labels[i].setHorizontalAlignment(SwingConstants.LEFT);
					labels[i].setVisible((selectedStats & 1<<i) != 0);
					for (int iH = 0; iH < pamHistograms.size(); iH++){
						c.gridx = 1;
						PamBorderPanel.addComponent(getPanel(), numbers[iH][i] = new PamLabel(), c);
						numbers[iH][i].setHorizontalAlignment(SwingConstants.LEFT);
						numbers[iH][i].setVisible((selectedStats & 1<<i) != 0);
					}
					c.gridy ++;
				}
				iRow = 0;
				for (int i = 0; i < rowLabels.length; i++) {
					labels[iRow+i].setText(rowLabels[i]);
				}
			}

			// write stuff into them
			iRow = 0;
			PamHistogram h;
			for (int iH = 0; iH < pamHistograms.size(); iH++) {
				h = pamHistograms.get(iH);
				numbers[iH][iRow++].setText(String.format("%d", (int) h.getTotalContent()));
				numbers[iH][iRow++].setText(String.format("%3.1f", h.getMean()));
				numbers[iH][iRow++].setText(String.format("%3.1f", h.getSTD()));
				numbers[iH][iRow++].setText(String.format("%3.1f", h.getSkew()));
				double n = h.getTotalContent();
				double u = h.getLoBin();
				double o = h.getHiBin();
				n += u + o;
				n = Math.max(n,1);
				numbers[iH][iRow++].setText(String.format("%3.1f %%", u/n*100));
				numbers[iH][iRow++].setText(String.format("%3.1f %%", o/n*100));
			}

		}

	}
	public PamAxis getSouthAxis() {
		return southAxis;
	}

	public PamAxis getWestAxis() {
		return westAxis;
	}

	public boolean isShowStats() {
		return showStats;
	}

	public void setShowStats(boolean showStats) {
		this.showStats = showStats;
		if (histoPlotPanel == null) {
			return;
		}
		if (showStats && histoStatsWindow != null) {
			histoPlotPanel.setKeyPanel(histoStatsWindow);
		}
		else {
			histoPlotPanel.setKeyPanel(null);
		}
	}

	public HistogramGraphicsLayer getGraphicsOverLayer() {
		return graphicsOverLayer;
	}

	public void setGraphicsOverLayer(HistogramGraphicsLayer graphicsOverLayer) {
		this.graphicsOverLayer = graphicsOverLayer;
	}

	public HistogramGraphicsLayer getGraphicsUnderLayer() {
		return graphicsUnderLayer;
	}

	public void setGraphicsUnderLayer(HistogramGraphicsLayer graphicsUnderLayer) {
		this.graphicsUnderLayer = graphicsUnderLayer;
	}

	/**
	 * @return the histoPlotPanel
	 */
	public HistoPlotPanel getHistoPlotPanel() {
		return histoPlotPanel;
	}

	/**
	 * @return the selectedStats
	 */
	public int getSelectedStats() {
		return selectedStats;
	}

	/**
	 * @param selectedStats the selectedStats to set
	 */
	public void setSelectedStats(int selectedStats) {
		this.selectedStats = selectedStats;
		histoStatsWindow.updateWindow();
	}

}
