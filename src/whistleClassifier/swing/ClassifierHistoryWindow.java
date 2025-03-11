package whistleClassifier.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.panel.CornerLayoutContraint;
import PamView.panel.JPanelWithPamKey;
import PamView.panel.KeyPanel;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import classifier.Classifier;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.PamScroller;
import pamScrollSystem.RangeSpinner;
import pamScrollSystem.RangeSpinnerListener;
import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.WhistleClassificationDataBlock;
import whistleClassifier.WhistleClassificationDataUnit;
import whistleClassifier.WhistleClassificationParameters;
import whistleClassifier.WhistleClassifierControl;

/**
 * Provides a history window for classification results. 
 * 
 * @author Douglas Gillespie
 *
 */
public class ClassifierHistoryWindow extends PamObserverAdapter {

	private WhistleClassifierControl whistleClassifierControl;

	HistoryWindowBorder historyWindowBorder;

	HistoryAxisPanel historyAxisPanel;

	HistoryPlotPanel historyPlotPanel;

	PamAxis southAxis, westAxis, eastAxis;

	double historyMinutes = 120; // minutes.

	double historyStart = 0.1;

	double minPlotProbability = 0.01, maxPlotProbability = 1.05;

	PamSymbol[] speciesSymbols;

	KeyPanel keyPanel;

	Classifier fragmentClassifier;

	Timer timer;

	WhistleClassificationDataBlock whistleClasificationDataBlock;
	
	private boolean isViewer = false;
	
	private PamScroller pamScroller;
	
	public ClassifierHistoryWindow(WhistleClassifierControl whistleClassifierControl) {
		super();

		this.whistleClassifierControl = whistleClassifierControl;
		
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		historyWindowBorder = new HistoryWindowBorder();

		whistleClasificationDataBlock = whistleClassifierControl.getWhistleClassifierProcess().getWhistleClasificationDataBlock();

		if (whistleClasificationDataBlock != null) {
			whistleClasificationDataBlock.addObserver(this);
		}

		newSettings();


		timer = new Timer(6000, new TimerAction());
		timer.start();
		
		if (isViewer) {
			pamScroller.addDataBlock(whistleClasificationDataBlock);
		}

	}

	private void createSymbols(String[] speciesList) {
		if (speciesList == null) {
			return;
		}
		Color col = Color.BLACK;
		int symbolSize = 14;
		speciesSymbols = new PamSymbol[speciesList.length];
		for (int i = 0; i < speciesList.length; i++) {
			col = PamColors.getInstance().getWhaleColor(i+1);
			speciesSymbols[i] = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, symbolSize, symbolSize, true, col, col);
			speciesSymbols[i].setLineThickness(2);
		}
		rebuildKey();
	}

	private String[] getSpeciesList() {
		if (whistleClassifierControl.getWhistleClassificationParameters().fragmentClassifierParams != null) {
			return whistleClassifierControl.getWhistleClassificationParameters().fragmentClassifierParams.getSpeciesList();
		}
		return null;
	}

	private void rebuildKey() {
		keyPanel = new KeyPanel("Whistle Species", PamKeyItem.KEY_SHORT);
		String[] speciesList = getSpeciesList();
		if (speciesList == null) {
			return;
		}
		for (int i = 0; i < speciesSymbols.length; i++) {
			keyPanel.add(speciesSymbols[i].makeKeyItem(speciesList[i]));
		}
		historyPlotPanel.setKeyPanel(keyPanel);
		historyPlotPanel.setKeyPosition(CornerLayoutContraint.LAST_LINE_END);
	}

	public Component getGraphicComponent() {
		return historyWindowBorder;
	}

	@Override
	public String getObserverName() {
		return "Whistle classification history window";
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return (long) (historyMinutes * 60 * 1000);
	}

	@Override
	public void noteNewSettings() {
		newSettings();
	}

	private void newSettings() {

		fragmentClassifier = whistleClassifierControl.getFragmentClassifier();

		if (fragmentClassifier == null) {
			//			return;
		}
		FragmentClassifierParams fp = whistleClassifierControl.getWhistleClassificationParameters().fragmentClassifierParams;
		if (fp != null) {
			createSymbols(fp.getSpeciesList());
		}
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		repaintPlots();
	}

	@Override
	public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
		repaintPlots();
	}

	class TimerAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			repaintPlots();

		}

	}

	private void repaintPlots() {
		historyAxisPanel.repaint(100);
		historyPlotPanel.repaint(100);
	}
	

	private RangeSpinner rangeSpinner;
	class HistoryWindowBorder extends PamBorderPanel {


		public HistoryWindowBorder() {
			super();
			this.setBorder(new TitledBorder("Classification History"));
			this.setLayout(new BorderLayout());
			JPanel inner = new JPanel(new BorderLayout());
			inner.add(BorderLayout.CENTER, historyAxisPanel = new HistoryAxisPanel());
			if (isViewer) {
				pamScroller = new PamScroller(getObserverName(), AbstractPamScrollerAWT.HORIZONTAL, 
						1000, 3600000, true);
				inner.add(BorderLayout.SOUTH, pamScroller.getComponent());
				rangeSpinner = new RangeSpinner();
				TimeRangeListener trl;
				rangeSpinner.addRangeSpinnerListener(trl = new TimeRangeListener());
				pamScroller.addControl(rangeSpinner.getComponent());
				pamScroller.addObserver(rangeSpinner);
				pamScroller.addObserver(new ScrollObserver());
				rangeSpinner.setSpinnerValue(3600);
				trl.valueChanged(0, rangeSpinner.getSpinnerValue());
			}
			this.add(BorderLayout.CENTER, inner);
		}

	}
	
	/**
	 * Observs the scroll bar. 
	 * @author Doug
	 *
	 */
	private class ScrollObserver implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			repaintPlots();
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			repaintPlots();
		}
		
	}

	/**
	 * Observes the time range spinner
	 * @author Doug
	 *
	 */
	private class TimeRangeListener implements RangeSpinnerListener {

		@Override
		public void valueChanged(double oldValue, double newValue) {
//			btDisplayParameters.setTimeRange(newValue);
//			hScrollManager.timeRangeChanged();
//			repaintBoth();
			pamScroller.setVisibleMillis((long) (newValue*1000.));
			southAxis.setMaxVal(newValue);
			repaintPlots();
		}

	}
	class HistoryAxisPanel extends PamAxisPanel {

		public HistoryAxisPanel() {
			super();
			JPanel inner = new JPanel(new BorderLayout());
			historyPlotPanel = new HistoryPlotPanel();
			inner.add(BorderLayout.CENTER, historyPlotPanel);
			setInnerPanel(inner);
			if (isViewer) {
				southAxis = new PamAxis(0, 0, 1, 1, 0, 10, PamAxis.BELOW_RIGHT, "Time", 
						PamAxis.LABEL_NEAR_CENTRE, "%.1f");
			}
			else {
				southAxis = new ClassifierHistoryTimeAxis(0, 0, 1, 0, historyStart, historyMinutes, PamAxis.BELOW_RIGHT, "History (minutes)", PamAxis.LABEL_NEAR_CENTRE, "%1.0f");
				southAxis.setLogScale(true);
				southAxis.setLogTenthsScale(true);
			}
			southAxis.setFormat("%.1f");
			westAxis = new PamAxis(0, 0, 0, 1, minPlotProbability, maxPlotProbability, PamAxis.ABOVE_LEFT, "Probability", PamAxis.LABEL_NEAR_CENTRE, "%1.2f");
			westAxis.setLogScale(true);
			southAxis.setFormat("%.3g");
			eastAxis = new PamAxis(0, 0, 0, 1, minPlotProbability, maxPlotProbability, PamAxis.BELOW_RIGHT, "Probability", PamAxis.LABEL_NEAR_CENTRE, "%1.2f");
			eastAxis.setLogScale(true);
			southAxis.setFormat("%.3f");
			setSouthAxis(southAxis);
			setWestAxis(westAxis);
			setEastAxis(eastAxis);

			setAutoInsets(true);
			//			Insets insets = getInsets();
			//			insets.top = 20;
			//			set
		}


		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isViewer) {
				Insets insets = getInsets();
				FontMetrics fm = g.getFontMetrics();
				int y = getHeight()-fm.getDescent()-fm.getHeight()/2;
				g.drawString(PamCalendar.formatDateTime(pamScroller.getValueMillis()), 
						insets.left, y);
				String r = PamCalendar.formatDateTime(pamScroller.getValueMillis()+pamScroller.getVisibleAmount());
				int x = getWidth()-insets.right-fm.stringWidth(r);
				g.drawString(r, x, y);
			}
		}

	}

	class HistoryPlotPanel extends JPanelWithPamKey {

		public HistoryPlotPanel() {
			super();
			setBorder(PamBorder.createInnerBorder());
			//			PamColors.getInstance().registerComponent(this, PamColor.PlOTWINDOW);
		}

		private int probToYCoord(double probability) {
			//			return (int) (getHeight() * (1.-probability));
			if (probability < minPlotProbability){
				return getHeight();
			}
			return  (int) (getHeight() * (1. - Math.log(probability / minPlotProbability) / Math.log(maxPlotProbability / minPlotProbability)));
		}

		private int timeMilisToXCoord(long timeMillis) {
			//			double xScale = (double)getWidth() / (historyMinutes * 60000);
			//			return getWidth() - (int) (xScale * (PamCalendar.getTimeInMillis() - timeMillis));
			if (timeMillis <= 0) {
				return 0;
			}
			if (isViewer) {
				return (int) southAxis.getPosition((timeMillis-pamScroller.getMinimumMillis())/1000.);
			}
			else {
				return (int) Math.max(0., getWidth() * Math.log(timeMillis / 1000. / 60. / historyStart) / Math.log(historyMinutes / historyStart));
			}
		}

		@Override
		public void paint(Graphics g) {
			if (whistleClassifierControl.getWhistleClassificationParameters().operationMode == 
				WhistleClassificationParameters.COLLECT_TRAINING_DATA) {
				paintForTraining(g);
			}
			else {
				paintForRun(g);
			}
		}

		public void paintForTraining(Graphics g) {
			super.paint(g);
			String txt = "Training " + whistleClassifierControl.getWhistleClassificationParameters().trainingSpecies;
			int x = getWidth()/2;
			int y = getHeight()/2;
			g.setFont(PamColors.getInstance().getBoldFont());
			FontMetrics fm = g.getFontMetrics();
			x -= fm.stringWidth(txt)/2;
			y -= fm.getHeight()/2;
			g.drawString(txt, x, y);
		}
		public void paintForRun(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;
			southAxis.drawGrid(g, getSize(), 1);
			westAxis.drawGrid(g, getSize(), 1);
			if (getSpeciesList() == null) {
				return;
			}
			int[] lastYPoints  = new int[getSpeciesList().length];
			double[] probabilities;
			Point pt = new Point();
			int lastX = -1;

			long now = PamCalendar.getTimeInMillis();

			WhistleClassificationDataUnit wcdu;
			synchronized (whistleClasificationDataBlock.getSynchLock()) {
				int nPoints = whistleClasificationDataBlock.getUnitsCount();

				g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
						BasicStroke.JOIN_MITER));

				ListIterator<WhistleClassificationDataUnit> iterator = 
					whistleClasificationDataBlock.getListIterator(PamDataBlock.ITERATOR_END);
				while (iterator.hasPrevious()) {
					wcdu = iterator.previous();
					probabilities = wcdu.getSpeciesProbabilities();
					if (isViewer) {
						pt.x = (int) southAxis.getPosition((wcdu.getTimeMilliseconds() 
								- pamScroller.getValueMillis())/1000);
					}
					else {
						pt.x = timeMilisToXCoord(now-wcdu.getTimeMilliseconds());
					}
					for (int s = 0; s < probabilities.length; s++){
						if (probabilities[s] < minPlotProbability) {
							lastYPoints[s] = getHeight() + 100;
							continue;
						}
						pt.y = probToYCoord(probabilities[s]);
						speciesSymbols[s].draw(g, pt);
						if (lastX >= 0 && lastYPoints[s] <= getHeight()) {
							g2d.drawLine(pt.x, pt.y, lastX, lastYPoints[s]);
						}
						lastYPoints[s] = pt.y;
					}
					lastX = pt.x;
				}
			}

			drawKeyOnTop();
		}

		@Override
		public void print(Graphics g) {
			super.print(g);
			// paint the entire data history. 

		}
	}
}
