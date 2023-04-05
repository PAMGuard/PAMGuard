package dbht;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import Filters.FIRArbitraryFilter;
import Filters.FIRFilterMethod;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamDetection.RawDataUnit;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SourcePanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;

public class DbHtDialog extends PamDialog {
	
	private static final double MINGAIN = -60;
	
	private DbHtControl dbHtControl;
	
	private static DbHtDialog singleInstance;
	
	private DbHtParameters params;
	
	private JTable thresholdTable;
	
	private ThresholdTableDataModel tdModel;
	
	private ParamsPanel paramsPanel;
	
	private BodePlotAxis bodePlotAxis;
	
	private TapPlotAxis tapPlotAxis;

	public BodePlot bodePlot;

	public TapPlot tapPlot;
	
	private SourcePanel sourcePanel;	
	
	private double currentSampleRate = 0;

	private FIRArbitraryFilter firArbfilter;
	
	private DbHtDialog(DbHtControl dbHtControl, Window parentFrame) {
		super(parentFrame, dbHtControl.getUnitName(), false);
		this.dbHtControl = dbHtControl;
		firArbfilter = new FIRArbitraryFilter(1, null);
		tdModel = new ThresholdTableDataModel();
		thresholdTable = new JTable(tdModel);
		paramsPanel = new ParamsPanel();
		bodePlotAxis = new BodePlotAxis();
		tapPlotAxis = new TapPlotAxis();
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.WEST, paramsPanel);
		JPanel cent = new JPanel();
		cent.setLayout(new BoxLayout(cent, BoxLayout.Y_AXIS));
		cent.add(bodePlotAxis);
		cent.add(tapPlotAxis);
		mainPanel.add(BorderLayout.CENTER, cent);
		
		setDialogComponent(mainPanel);
		setResizable(true);
	}
	
	public static DbHtParameters showDialog(DbHtControl dbHtControl, Window parentFrame) {
		if (singleInstance == null || 
				singleInstance.dbHtControl != dbHtControl || 
				singleInstance.getOwner() != parentFrame) {
			singleInstance = new DbHtDialog(dbHtControl, parentFrame);
		}
		singleInstance.params = dbHtControl.dbHtParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.params;
	}

	private void setParams() {
		paramsPanel.setParams();
		updateEverything();
	}

	@Override
	public void cancelButtonPressed() {
		params = null;
	}

	@Override
	public boolean getParams() {
		boolean ok = paramsPanel.getParams();
		return ok;
	}

	@Override
	public void restoreDefaultSettings() {

	}
	
	/**
	 * Import a set of frequency  / hearing threshold values. 
	 */
	public void importButtonPress() {
		PamFileFilter ff = new PamFileFilter("CSV / Text files", ".txt");
		ff.addFileType(".csv");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(ff);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setSelectedFile(params.lastImportFile);
		int state = fileChooser.showOpenDialog(getOwner());
		if (state != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File currFile = fileChooser.getSelectedFile();
		List<String[]> readList;
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(currFile));
		} catch (FileNotFoundException e) {
			showWarning("unable to open csv file");
			return;
		}
		try {
			readList = reader.readAll();
		} catch (CsvException | IOException e) {
			showWarning("unable to read csv file");
			return;
		}
		params.lastImportFile = currFile;
		int n = readList.size();
		String[] aRow;
		double[] f = new double[n];
		double[] t = new double[n];
		double aF, aT;
		int iOk = 0;
		for (int i = 0; i < n; i++) {
			aRow = readList.get(i);
			try {
				aF = Double.valueOf(aRow[0]);
				aT = Double.valueOf(aRow[1]);
			}
			catch (NumberFormatException e) {
				continue;
			}
			f[iOk] = aF;
			t[iOk] = aT;
			iOk++;
		}
		f = Arrays.copyOf(f, iOk);
		t = Arrays.copyOf(t, iOk);
		params.setFrequencyPoints(f);
		params.hearingThreshold = t;
		
		updateEverything();
	}

	private void updateEverything() {
		getParams();
		try {
			params.calculateFilterThings(currentSampleRate);
		}
		catch (DbHtException e) {
			System.out.println("Error in DbHtDialog.updateEverything: " + e.getMessage());
			return;
		}
		double[] f = params.getFilterFrequencies(currentSampleRate);
		double[] g = params.getFilterGains(currentSampleRate);
		firArbfilter.setResponse(f, g, params.filterLogOrder, params.chebyGamma);
		firArbfilter.calculateFilter();
		tdModel.fireTableDataChanged();
		bodePlotAxis.setParams();
		tapPlotAxis.setParams();
	}

	private class ThresholdTableDataModel extends AbstractTableModel {

		String[] colHeads = {"Frequency", "Threshold"};
		@Override
		public int getColumnCount() {
			return colHeads.length;
		}

		@Override
		public int getRowCount() {
			if (params == null) {
				return 0;
			}
			if (params.getFrequencyPoints() == null) {
				return 0;
			}
			return params.getFrequencyPoints().length;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (params == null) {
				return null;
			}
			if (col == 0) {
				return params.getFrequencyPoints()[row];
			}
			else if (col == 1) {
				return params.hearingThreshold[row];
			}
			return null;
		}

		@Override
		public String getColumnName(int col) {
			return colHeads[col];
		}
		
	}
	
	private class ParamsPanel extends JPanel implements PamDialogPanel {
		private JTextField filterOrder;
		private JTextField chebyGamma;
		private JTextField measurementInterval;
		private JButton importButton;
		private JButton recalcButton;
		ParamsPanel() {
			filterOrder = new JTextField(3);
			chebyGamma = new JTextField(3);
			measurementInterval = new JTextField(3);
			sourcePanel = new SourcePanel(null, "Raw data source", RawDataUnit.class, true, true);
			this.setLayout(new BorderLayout());
			JPanel northOuter = new JPanel(new BorderLayout());
			northOuter.add(BorderLayout.NORTH, sourcePanel.getPanel());
			
			JPanel north = new JPanel();
			north.setBorder(new TitledBorder("Filter parameters"));
			north.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(north, new JLabel("Measurement Interval ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(north, measurementInterval, c);
			c.gridx++;
			addComponent(north, new JLabel(" (seconds)", SwingConstants.LEFT), c);
			c.gridy++;
			c.gridx = 0;
			addComponent(north, new JLabel("log2(Filter Order) ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(north, filterOrder, c);
			c.gridy++;
			c.gridx = 0;
			addComponent(north, new JLabel("Window gamma (3 - 5) ", SwingConstants.RIGHT), c);
			c.gridx++;
			addComponent(north, chebyGamma, c);
			c.gridy++;
			c.gridx = 2;
			addComponent(north, recalcButton = new JButton("Recalculate"), c);

			northOuter.add(BorderLayout.SOUTH, north);
			add(BorderLayout.NORTH, northOuter);
			
			JPanel cent = new JPanel();
			cent.setBorder(new TitledBorder("Threshold values"));
			cent.setLayout(new BorderLayout());
			JScrollPane sp = new JScrollPane(thresholdTable);
			sp.setPreferredSize(new Dimension(130, 300));
			cent.add(BorderLayout.CENTER, sp);
			JPanel south = new JPanel();
			cent.add(BorderLayout.SOUTH, south);
			south.setLayout(new BorderLayout());
			south.add(BorderLayout.EAST, importButton = new JButton("Import"));
			add(BorderLayout.CENTER, cent);
			importButton.addActionListener(new ImportButton());
			recalcButton.addActionListener(new RecalcButton());
		}
		@Override
		public JComponent getDialogComponent() {
			return this;
		}
		
		@Override
		public boolean getParams() {
			PamDataBlock dataBlock = sourcePanel.getSource();
			params.dataSource = dataBlock.getDataName();
			params.channelMap = sourcePanel.getChannelList();
			try {
				params.filterLogOrder = Integer.valueOf(filterOrder.getText());
				params.chebyGamma = Double.valueOf(chebyGamma.getText());
				params.measurementInterval = Integer.valueOf(measurementInterval.getText());
			}
			catch (NumberFormatException e) {
				return false;
			}
			if (params.dataSource == null || params.channelMap == 0) {
				return false;
			}
			currentSampleRate = dataBlock.getSampleRate();
			return true;
		}
		@Override
		public void setParams() {
			sourcePanel.setSource(params.dataSource);
			sourcePanel.setChannelList(params.channelMap);
			filterOrder.setText(String.format("%d", params.filterLogOrder));
			chebyGamma.setText(String.format("%3.1f", params.chebyGamma));
			measurementInterval.setText(String.format("%d", params.measurementInterval));
		}
	}
	
	private class RecalcButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			updateEverything();
		}
	}
	private class ImportButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			importButtonPress();
		}
	}

	private class BodePlotAxis extends PamAxisPanel implements PamDialogPanel {

		PamAxis freqAxis;
		PamAxis thresholdAxis;
		PamAxis gainAxis;
		/**
		 * 
		 */
		public BodePlotAxis() {
			super();
//			setBorder(new TitledBorder("Frequency Response"));
			bodePlot = new BodePlot();
//			this.setPlotPanel(bodePlot);
			this.setInnerPanel(bodePlot);
			freqAxis = new PamAxis(0, 0, 1, 1, 1, 100, PamAxis.BELOW_RIGHT, "Frequency (Hz)", PamAxis.LABEL_NEAR_CENTRE, "%d");
			freqAxis.setLogScale(true);
			gainAxis = new PamAxis(0, 0, 1, 1, MINGAIN, 0, PamAxis.BELOW_RIGHT, "Filter Gain (dB)", PamAxis.LABEL_NEAR_CENTRE, "%3d");
			gainAxis.setInterval(10);
			thresholdAxis = new PamAxis(0, 0, 1, 100, 170, 1, PamAxis.ABOVE_LEFT, "Hearing Threshold", PamAxis.LABEL_NEAR_CENTRE, "%d");
			thresholdAxis.setInterval(10);
			this.setSouthAxis(freqAxis);
			this.setWestAxis(thresholdAxis);
			this.setEastAxis(gainAxis);
			this.setAutoInsets(true);
			this.SetBorderMins(1,1,1,1);
			
			setPreferredSize(new Dimension(200,100));
//			bodePlot.setPreferredSize(new Dimension(200,100));
		}

		@Override
		public JComponent getDialogComponent() {
			return this;
		}

		@Override
		public boolean getParams() {
			return true;
		}

		@Override
		public void setParams() {
			double minFreq = currentSampleRate / 100;
			double[] f = params.getFrequencyPoints();
			if (f != null) {
				for (int i = 0; i < f.length; i++) {
					if (f[i] > 0) {
						minFreq = Math.min(minFreq, f[i]);
					}
				}
			}
			minFreq = Math.pow(10.,Math.floor(Math.log10(minFreq)));
			if (params != null) {
				freqAxis.setRange(minFreq, currentSampleRate/2);
			}
			setThresholdAxis();
				
				
			repaint();
			bodePlot.repaint();
		}

		private void setThresholdAxis() {
			double[] t = params.hearingThreshold;
			if (t == null) {
				return;
			}
			double minT = Double.MAX_VALUE;
			double maxT = Double.MIN_VALUE;
			for (int i = 0; i < t.length; i++) {
				minT = Math.min(minT, t[i]);
				maxT = Math.max(maxT, t[i]);
			}
			// round up and down;
			minT = Math.floor(minT/10.) * 10.;
			maxT = Math.ceil(maxT/10.) * 10.;
			thresholdAxis.setRange(minT, maxT);			
			
			double[] filterTaps = firArbfilter.getFilterTaps();
			if (filterTaps != null) {
				int n = FIRFilterMethod.NRESPONSEPOINTS;
				double omega;
				double r;
				double maxR = 0;
				for (int i = 0; i < n; i++) {
					omega = (double) i / n * Math.PI;
					r = firArbfilter.getFilterGain(omega);
					maxR = Math.max(maxR, r);
				}
				if (maxR > 0) {
					maxR = 20*Math.log10(maxR);
					maxR = Math.ceil(maxR/10.)*10.;
					gainAxis.setRange(MINGAIN, maxR);
				}
			}
		}
		
	}
	
	private class BodePlot extends PamPanel {
		
		PamSymbol thSymbol;
		
		PamSymbol gainSymbol;
		
		public BodePlot() {
			super(PamColor.PlOTWINDOW);
			setBorder(BorderFactory.createBevelBorder(1));
			setPreferredSize(new Dimension(400,100));
			thSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 12, 12, false, Color.RED, Color.RED);
			gainSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 12, 12, false, Color.BLUE, Color.BLUE);
			
		}

		@Override
		public void paint(Graphics g) {
			super.paintComponent(g);
			paintThresholds(g);
			paintFilterResponse(g);
			overlayGain(g);
		}

		private void paintFilterResponse(Graphics g) {
			double[] filterTaps = firArbfilter.getFilterTaps();
			if (filterTaps == null) {
				return;
			}
			int n = FIRFilterMethod.NRESPONSEPOINTS;
			double omega, freq;
			double r;
			int x1, x2, y1, y2;
			x1 = y1 = 0;
			for (int i = 0; i < n; i++) {
				omega = (double) i / n * Math.PI;
				freq = omega/Math.PI*currentSampleRate/2;
				r = firArbfilter.getFilterGain(omega);
				if (r > 0) {
					y2 = (int) bodePlotAxis.gainAxis.getPosition(20*Math.log10(r));					
				}
				else {
					y2 = getHeight()-2;
				}
				x2 = (int) bodePlotAxis.freqAxis.getPosition(freq);
				g.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
			}
			
		}
		
		/**
		 * Draw the hearing thresholds as gains on top of the filter response. 
		 * @param g
		 */
		private void overlayGain(Graphics g) {
			double lowTh = params.getLowestThreshold();
			if (Double.isNaN(lowTh)) {
				return;
			}
			double[] t = params.hearingThreshold;
			double[] f = params.getFrequencyPoints();
			Point pt = new Point();
			for (int i = 0; i < t.length; i++) {
				pt.x = (int) bodePlotAxis.freqAxis.getPosition(f[i]);
				pt.y = (int) bodePlotAxis.gainAxis.getPosition(lowTh - t[i]);
				gainSymbol.draw(g, pt);
			}
		}

		private void paintThresholds(Graphics g) {
			double[] t = params.hearingThreshold;
			double[] f = params.getFrequencyPoints();
			if (t == null || f == null) {
				return;
			}
			int n = Math.min(t.length, f.length) ;
			Point pt = new Point();
			for (int i = 0; i < n; i++) {
				pt.x = (int) bodePlotAxis.freqAxis.getPosition(f[i]);
				pt.y = (int) bodePlotAxis.thresholdAxis.getPosition(t[i]);
				thSymbol.draw(g, pt);
			}
		}

	}
	private class TapPlotAxis extends PamAxisPanel implements PamDialogPanel {

		private PamAxis nAxis;
		private PamAxis yAxis;
		public TapPlotAxis() {
			tapPlot = new TapPlot();
			this.setInnerPanel(tapPlot);
			setPreferredSize(new Dimension(400,100));
			nAxis = new PamAxis(0, 1, 0, 1, 0, 128, PamAxis.BELOW_RIGHT, "Tap", PamAxis.LABEL_NEAR_CENTRE, "%d");
			yAxis = new PamAxis(0, 1, 0, 1, -1, 1, PamAxis.ABOVE_LEFT, "Tap value", PamAxis.LABEL_NEAR_CENTRE, "%3.2f");
			setSouthAxis(nAxis);
			setWestAxis(yAxis);
			SetBorderMins(10, 0, 0, 30);
		}
		@Override
		public JComponent getDialogComponent() {
			return this;
		}

		@Override
		public boolean getParams() {
			return true;
		}

		@Override
		public void setParams() {
			if (params != null) {
				nAxis.setRange(0, 1<<params.filterLogOrder);
			}
			double[] filterTaps = firArbfilter.getFilterTaps();
			if (filterTaps == null) {
				return;
			}
			double minVal = filterTaps[0];
			double maxVal = filterTaps[0];
			for (int i = 0; i < filterTaps.length; i++) {
				minVal = Math.min(filterTaps[i], minVal);
				maxVal = Math.max(filterTaps[i], maxVal);
			}
			minVal = Math.floor(minVal*20.)/20.;
			maxVal = Math.ceil(maxVal*20.)/20.;
			yAxis.setRange(minVal, maxVal);
			repaint();
			tapPlot.repaint();
		}
		
	}
	
	private class TapPlot extends PamPanel {
		
		private PamSymbol tapSymbol;
		
		public TapPlot() { 
			super(PamColor.PlOTWINDOW);
			setBorder(BorderFactory.createBevelBorder(1));
			setPreferredSize(new Dimension(200,100));
			tapSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 8, 8, false, Color.BLUE, Color.BLUE);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paintComponent(g);
			double[] filterTaps = firArbfilter.getFilterTaps();
			if (filterTaps == null) {
				return;
			}
			int n = filterTaps.length;
			Point pt = new Point();
			int y0 = (int) tapPlotAxis.yAxis.getPosition(0);
			g.drawLine(0, y0, (int) tapPlotAxis.nAxis.getPosition(n), y0);
			for (int i = 0; i < n; i++) {
				pt.x = (int) tapPlotAxis.nAxis.getPosition(i);
				pt.y = (int) tapPlotAxis.yAxis.getPosition(filterTaps[i]);
				tapSymbol.draw(g, pt);
				g.drawLine(pt.x, pt.y, pt.x, y0);
			}
			
			
		}
	}
}
