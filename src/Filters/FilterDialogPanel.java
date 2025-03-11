package Filters;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import Layout.PamAxis;
import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import fftManager.Complex;

/**
 * Create a more generic dialog panel for the PAMGurd 
 * filters which can be incorporated into larger pnales
 * if desired. 
 * @author Doug Gillespie
 *
 */
public class FilterDialogPanel implements ActionListener {

	private FilterParams filterParams = new FilterParams();

	private float sampleRate;

	private String[] filterNames = { "None", "IIR Butterworth", "IIR Chebyshev", 
			"FIR Filter (Window Method)", "Arbitrary FIR Filter"};

	private JComboBox filterTypes;

	private JRadioButton highPass;

	private JRadioButton bandPass;

	private JRadioButton lowPass;

	private JRadioButton bandStop;

	private JRadioButton linScale, logScale;

	private JTextField highCut, lowCut;

	private JTextField order, passbandRipple;

	private JLabel rippleLabel;

	private JButton plotButton;

	private static final int xborder = 5;


	//		private IirfFilter filter;
	private FilterMethod filterMethod;

	private BodePlot bodePlot;

	private BodeGraph bodeGraph;

	private PoleZeroPlot pzPlot;

	private boolean isSetup = false;

	/*
	 * Some information from the ANSI S1.11-2004 standard 
	 * "Specification for Octave band and fractional octave band analog and digital filters."
	 */
	double[] relFreq = {1.0000,    1.0260,    1.0550,    1.0870,    1.1220,    1.1220,
			1.2940,    1.8810,    3.0530,    5.3910};
	double maxAtten = 10000;//Double.POSITIVE_INFINITY;
	double[] att0min = {-.15, -.15, -.15, -.15, -.15, 2.3, 18, 42.5, 62, 75};
	double[] att0max = {.15, .2, .4, 1.1, 4.5, 4.5, maxAtten, maxAtten, maxAtten, maxAtten};
	double[] att1min = {-.3, -.3, -.3, -.3, -.3, 2, 17.5, 42, 61, 70,};
	double[] att1max = {.3, .4, .6, 1.3, 5, 5, maxAtten, maxAtten, maxAtten, maxAtten};
	double[] att2min = {-.5, -.5, -.5, -.5, -.5, 1.6, 16.5, 41, 55, 60};
	double[] att2max = {.5, .6, .8, 1.6, 5.5, 5.5, maxAtten, maxAtten, maxAtten, maxAtten,};

	public JPanel normalPanel;

	public ArbTableModel arbTableModel;

	public ArbPanel arbPanel;

	private Window ownerWindow;

	private JPanel mainPanel;

	public FilterDialogPanel(Window ownerWindow, float sampleRate) {
		this.ownerWindow = ownerWindow;

		this.sampleRate = Math.max(sampleRate, 1.f);

		//		if (PamguardVersionInfo.getReleaseType() != PamguardVersionInfo.ReleaseType.SMRU) {
		//			filterNames = Arrays.copyOf(filterNames, 3);
		//		}

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		JPanel lP = new JPanel();
		lP.setLayout(new BorderLayout());
		lP.add(BorderLayout.NORTH, new SettingsPanel());
		lP.add(pzPlot = new PoleZeroPlot());
		mainPanel.add(BorderLayout.WEST, lP);


		JPanel scalePanel = new JPanel();
		scalePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		scalePanel.add(logScale);
		scalePanel.add(linScale);
		JPanel bodePanel = new JPanel();
		bodePanel.setBorder(new TitledBorder("Bode Plot"));
		bodePanel.setLayout(new BorderLayout());
		bodePanel.add(BorderLayout.NORTH, scalePanel);
		bodePanel.add(BorderLayout.CENTER, bodePlot = new BodePlot());
		mainPanel.add(BorderLayout.CENTER, bodePanel);

		filterTypes.addActionListener(this);
		highPass.addActionListener(this);
		bandPass.addActionListener(this);
		bandStop.addActionListener(this);
		lowPass.addActionListener(this);
		plotButton.addActionListener(this);


	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void setParams(FilterParams filterParams) {
		isSetup = false; 
		this.filterParams = filterParams;

		// create a filter method.
		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);

		setSettings();
		isSetup = true;
		getParams();

		enableControls();

		repaintAll();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!isSetup) {
			return;
		}
		if (e.getSource() == filterTypes) {
			setRippleParam();
		}
		enableControls();
		//				return;
		//			if (e.getSource() == cancelButton) {
		//				filterParams = null;
		//				this.setVisible(false);
		//			} else if (e.getSource() == okButton) {
		//				if (getSettings()) {
		//					this.setVisible(false);
		//				}
		//			}

		// anything else, get the values and make a new plot
		getParams();
		repaintAll();
	}

	private void enableControls() {
		int filterType = filterTypes.getSelectedIndex();
		boolean haveFilter = filterType > 0;
		lowPass.setEnabled(haveFilter);
		highPass.setEnabled(haveFilter);
		bandPass.setEnabled(haveFilter);
		bandStop.setEnabled(haveFilter);
		highCut.setEnabled(!lowPass.isSelected() & haveFilter);
		lowCut.setEnabled(!highPass.isSelected() & haveFilter);
		order.setEnabled(haveFilter);
		passbandRipple.setEnabled(haveFilter & filterType >=2);
		logScale.setEnabled(haveFilter);
		linScale.setEnabled(haveFilter);
		plotButton.setEnabled(haveFilter);
		switch (filterType) {
		case 2:
			rippleLabel.setText("Pass band ripple ");
			break;
		case 3:
		case 4:
			rippleLabel.setText("Gamma  ");
			break;
		}
		boolean isArb = filterType == 4;
		normalPanel.setVisible(!isArb);
		arbPanel.setVisible(isArb);
	}


	void repaintAll() {
		bodeGraph.repaint();
		bodePlot.repaint();
		pzPlot.repaint();
	}

	class ImportButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			importButtonPress();
		}
	}

	class SettingsPanel extends JPanel {

		SettingsPanel() {

			filterTypes = new JComboBox(filterNames);
			highPass = new JRadioButton("High Pass");
			bandPass = new JRadioButton("Band Pass");
			bandStop = new JRadioButton("Band Stop");
			lowPass = new JRadioButton("Low Pass");
			highCut = new JTextField(7);
			lowCut = new JTextField(7);
			order = new JTextField(3);
			passbandRipple = new JTextField(4);
			plotButton = new JButton("Plot");
			linScale = new JRadioButton("Linear Scale");
			logScale = new JRadioButton("Log Scale");

			ButtonGroup scaleGroup = new ButtonGroup();
			scaleGroup.add(linScale);
			scaleGroup.add(logScale);
			linScale.addActionListener(new ScaleType());
			logScale.addActionListener(new ScaleType());


			setBorder(new EmptyBorder(xborder, xborder, xborder, xborder));
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			JPanel panel1 = new JPanel();
			//				JPanel panel2 = new JPanel();
			JPanel panel3 = new JPanel();

			panel1.setBorder(BorderFactory.createTitledBorder("Filter Type"));
			panel1.setLayout(new BorderLayout());
			JPanel ddpanel = new JPanel();
			ddpanel.setLayout(new BorderLayout());
			ddpanel.add(BorderLayout.NORTH, filterTypes);
			panel1.add(BorderLayout.CENTER, ddpanel);
			////			panel1.add(BorderLayout.EAST, pbPanel);
			add(panel1);

			ButtonGroup group = new ButtonGroup();
			group.add(highPass);
			group.add(bandPass);
			group.add(bandStop);
			group.add(lowPass);

			normalPanel = new JPanel();
			normalPanel.setBorder(BorderFactory
					.createTitledBorder("Filter Response"));
			normalPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			normalPanel.add(highPass, c);
			c.gridy++;
			normalPanel.add(bandPass, c);
			c.gridy++;
			normalPanel.add(bandStop, c);
			c.gridy++;
			normalPanel.add(lowPass, c);
			c.gridx++;
			c.gridy = 0;
			normalPanel.add(new JLabel("     "), c);
			c.gridx++;
			int xc = c.gridx;
			c.gridwidth = 3;
			normalPanel.add(new JLabel(" Frequencies"), c);
			c.gridwidth = 1;
			c.gridy++;
			c.gridx = xc;
			normalPanel.add(new JLabel(" High Pass", SwingConstants.RIGHT), c);
			c.gridx++;
			normalPanel.add(highCut, c);
			c.gridx++;
			normalPanel.add(new JLabel(" Hz"), c);
			c.gridy++;
			c.gridx = xc;
			normalPanel.add(new JLabel(" Low Pass", SwingConstants.RIGHT), c);
			c.gridx++;
			normalPanel.add(lowCut, c);
			c.gridx++;
			normalPanel.add(new JLabel(" Hz"), c);
			add(normalPanel);

			arbPanel = new ArbPanel();
			add(arbPanel);


			//				JPanel pbPanel = new JPanel();
			//				pbPanel.setLayout(new GridLayout(4, 1));
			//				pbPanel.add(highPass);
			//				pbPanel.add(bandPass);
			//				pbPanel.add(bandStop);
			//				pbPanel.add(lowPass);
			//				panel1.add(BorderLayout.CENTER, ddpanel);
			////				panel1.add(BorderLayout.EAST, pbPanel);
			//				add(panel1);
			//
			//				JPanel panel2a = new JPanel(new BorderLayout());
			//				panel2a.add(BorderLayout.WEST, pbPanel);
			//				panel2a.setBorder(BorderFactory
			//						.createTitledBorder("Cut off frequencies"));
			//				panel2.setLayout(new GridLayout(2, 3));
			//				panel2.add(new JLabel("High Pass"));
			//				panel2.add(highCut);
			//				panel2.add(new JLabel(" Hz"));
			//				panel2.add(new JLabel("Low Pass"));
			//				panel2.add(lowCut);
			//				panel2.add(new JLabel(" Hz"));
			//				panel2a.add(BorderLayout.CENTER, panel2);
			//				add(panel2a);

			panel3.setBorder(BorderFactory
					.createTitledBorder("Filter parameters"));
			panel3.setLayout(new BorderLayout());
			JPanel sp = new JPanel();
			sp.setLayout(new GridLayout(2, 2));
			sp.add(new JLabel("Filter order"));
			sp.add(order);
			sp.add(rippleLabel = new JLabel("Pass band ripple"));
			sp.add(passbandRipple);
			panel3.add(BorderLayout.CENTER, sp);
			panel3.add(BorderLayout.EAST, plotButton);
			add(panel3);

		}
	}

	/**
	 * Panel for arbitrary shaped FIR filters. 
	 * @author Doug
	 *
	 */
	private class ArbPanel extends JPanel {

		public ArbPanel() {
			super();
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createTitledBorder("Filter shape"));
			arbTableModel = new ArbTableModel();
			JTable arbTable = new JTable(arbTableModel);
			JScrollPane scrollPane = new JScrollPane(arbTable);
			scrollPane.setPreferredSize(new Dimension(200, 150));
			this.add(BorderLayout.CENTER, scrollPane);
			JPanel bPanel = new JPanel(new BorderLayout());
			JButton impButton;
			bPanel.add(BorderLayout.NORTH, impButton = new JButton("Import"));
			impButton.addActionListener(new ImportButton());
			this.add(BorderLayout.EAST, bPanel);
		}

	}

	private class ArbTableModel extends AbstractTableModel {

		private final String[] colNames = {"Frequency (Hz)", "Gain (dB)"};
		@Override
		public int getRowCount() {
			if (filterParams == null || filterParams.getArbGainsdB() == null) {
				return 0;
			}
			return filterParams.getArbGainsdB().length;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			double data[] = null;
			switch(columnIndex) {
			case 0:
				data = filterParams.getArbFreqs();
				break;
			case 1:
				data = filterParams.getArbGainsdB();
				break;
			}
			if (data == null || data.length <= rowIndex) {
				return "Err";
			}
			return data[rowIndex];
		}

	}

	void setSettings() {
		// filterTypes = new JComboBox(filterNames);
		// highPass = new JRadioButton("High Pass");
		// bandPass = new JRadioButton("Band Pass");
		// lowPass = new JRadioButton("Low Pass");
		// highCut = new JEditorPane();
		// lowCut = new JEditorPane();
		// order = new JEditorPane();
		// passbandRipple = new JEditorPane();
		// plotButton = new JButton("Plot");

		if (filterParams == null) {
			filterParams = new FilterParams();
		}
		switch (filterParams.filterType) {
		case NONE:
			filterTypes.setSelectedIndex(0);
			break;
		case BUTTERWORTH:
			filterTypes.setSelectedIndex(1);
			break;
		case CHEBYCHEV:
			filterTypes.setSelectedIndex(2);
			break;
		case FIRWINDOW:
			if (filterNames.length > 3) {
				filterTypes.setSelectedIndex(3);
			}
			break;
		case FIRARBITRARY:
			if (filterNames.length > 4) {
				filterTypes.setSelectedIndex(4);
			}
		}
		switch (filterParams.filterBand) {
		case HIGHPASS:
			highPass.setSelected(true);
			break;
		case BANDPASS:
			bandPass.setSelected(true);
			break;
		case BANDSTOP:
			bandStop.setSelected(true);
			break;
		case LOWPASS:
			lowPass.setSelected(true);
			break;
		}
		// if (filterParams.filterType == FilterType.NONE)
		// filterTypes.setSelectedIndex(0);
		// if (filterParams.filterType == FilterType.BUTTERWORTH)
		// filterTypes.setSelectedIndex(1);
		// if (filterParams.filterType == FilterType.CHEBYCHEV)
		// filterTypes.setSelectedIndex(2);

		// if (filterParams.filterBand == FilterBand.HIGHPASS)
		// highPass.setSelected(true);
		// if (filterParams.filterBand == FilterBand.BANDPASS)
		// bandPass.setSelected(true);
		// if (filterParams.filterBand == FilterBand.LOWPASS)
		// lowPass.setSelected(true);

		highCut.setText(String.format("%1.1f", filterParams.highPassFreq));
		lowCut.setText(String.format("%1.1f", filterParams.lowPassFreq));
		order.setText(String.format("%d", filterParams.filterOrder));
		setRippleParam();
		logScale.setSelected(filterParams.scaleType == FilterParams.SCALE_LOG);
		linScale.setSelected(filterParams.scaleType == FilterParams.SCALE_LIN);
	}

	public void importButtonPress() {
		PamFileFilter ff = new PamFileFilter("CSV / Text files", ".txt");
		ff.addFileType(".csv");
		JFileChooser fileChooser = new PamFileChooser();
		fileChooser.setFileFilter(ff);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setSelectedFile(filterParams.lastImportFile);
		int state = fileChooser.showOpenDialog(ownerWindow);
		if (state != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File currFile = fileChooser.getSelectedFile();
		List<String[]> readList;
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(currFile));
		} catch (FileNotFoundException e) {
			PamDialog.showWarning(ownerWindow, "Filter Import", "unable to open csv file");
			return;
		}
		try {
			readList = reader.readAll();
		} catch (CsvException | IOException e) {
			PamDialog.showWarning(ownerWindow, "Filter Import", "unable to read csv file");
			return;
		}
		filterParams.lastImportFile = currFile;
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
		filterParams.setArbFilterShape(f, t);

		arbTableModel.fireTableDataChanged();
		repaintAll();
	}


	void setRippleParam() {
		int filtType = filterTypes.getSelectedIndex();
		switch(filtType) {
		case 2:
			passbandRipple.setText(String.format("%3.1f", filterParams.passBandRipple));
			break;
		case 3:
		case 4:
			passbandRipple.setText(String.format("%3.1f", filterParams.chebyGamma));
			break;
		}
	}

	public void cancelButtonPressed() {
		filterParams = null;
	}

	public void restoreDefaultSettings() {

	}

	public boolean getParams() {
		try {
			if (filterTypes.getSelectedIndex() == 0)
				filterParams.filterType = FilterType.NONE;
			else if (filterTypes.getSelectedIndex() == 1)
				filterParams.filterType = FilterType.BUTTERWORTH;
			else if (filterTypes.getSelectedIndex() == 2)
				filterParams.filterType = FilterType.CHEBYCHEV;
			else if (filterTypes.getSelectedIndex() == 3)
				filterParams.filterType = FilterType.FIRWINDOW;
			else if (filterTypes.getSelectedIndex() == 4)
				filterParams.filterType = FilterType.FIRARBITRARY;

			if (highPass.isSelected())
				filterParams.filterBand = FilterBand.HIGHPASS;
			if (bandPass.isSelected())
				filterParams.filterBand = FilterBand.BANDPASS;
			if (bandStop.isSelected())
				filterParams.filterBand = FilterBand.BANDSTOP;
			if (lowPass.isSelected())
				filterParams.filterBand = FilterBand.LOWPASS;

			if (highCut.getText().length() > 0)
				filterParams.highPassFreq = (float) (Double.valueOf(highCut
						.getText())/1.) ;
			if (lowCut.getText().length() > 0)
				filterParams.lowPassFreq = (float) (Double
						.valueOf(lowCut.getText()) / 1);
			if (order.getText().length() > 0)
				filterParams.filterOrder = Integer.valueOf(order.getText());
			if (passbandRipple.getText().length() > 0) {
				switch (filterParams.filterType) {
				case CHEBYCHEV:
					filterParams.passBandRipple = Double.valueOf(passbandRipple.getText());
					break;
				case FIRWINDOW:
				case FIRARBITRARY:
					filterParams.chebyGamma = Double.valueOf(passbandRipple.getText());
					break;
				}
			}

		}
		catch (NumberFormatException ex) {
			return false;
		}
		filterParams.scaleType = getScaleType();
		double niquist = sampleRate / 2.;
		if (filterParams.filterBand == FilterBand.BANDPASS || 
				filterParams.filterBand == FilterBand.BANDSTOP || 
				filterParams.filterBand == FilterBand.LOWPASS) {
			if (filterParams.lowPassFreq > niquist) {
				return PamDialog.showWarning(ownerWindow, "Filter Settings", "The low pass cut off frequency is too high");
			}
		}
		if (filterParams.filterBand == FilterBand.BANDPASS  || 
				filterParams.filterBand == FilterBand.BANDSTOP || 
				filterParams.filterBand == FilterBand.HIGHPASS) {
			if (filterParams.highPassFreq > niquist) {
				return PamDialog.showWarning(ownerWindow, "Filter Settings", "The high pass cut off frequency is too high");
			}
			
		}
		if (filterParams.filterType.isIIR()) {
			if (filterParams.filterOrder > 1 && filterParams.filterOrder%2 == 1) {
				return PamDialog.showWarning(ownerWindow, "Filter settings", "Filters with order greater than 1 must have an even order");
			}
		}
	
		/**
		 * Here - need to change the filter type if it's an FIR or IIRF filter. 
		 */
		//		filterMethod(filterParams);
		filterMethod = FilterMethod.createFilterMethod(sampleRate, filterParams);

		return true;
	}

	private int getScaleType() {
		if (logScale.isSelected()) {
			return FilterParams.SCALE_LOG;
		}
		else {
			return FilterParams.SCALE_LIN;
		}
	}

	class ScaleType implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			repaintAll();
		}

	}
	class PoleZeroPlot extends PamPanel {
		int pointsize;

		PoleZeroPlot() {
			super(PamColor.PlOTWINDOW);
			setBorder(BorderFactory.createBevelBorder(1));
			//				PamColors.getInstance().registerComponent(this,
			//						PamColors.PamColor.PlOTWINDOW);
			setPreferredSize(new Dimension(100,200));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (filterMethod == null) {
				return;
			}
			if (IIRFilterMethod.class.isAssignableFrom(filterMethod.getClass())) {
				paintIIRImpulseResponse(g);
				paintPoleZeros(g);
			}
			else if (FIRFilterMethod.class.isAssignableFrom(filterMethod.getClass())) {
				paintImpulseResponse(g);
			}
		}
		/**
		 * Paint the impulse response. For FIR filters, this is the only option and is
		 * simply the taps of the filter function. 
		 * @param g
		 */
		private void paintImpulseResponse(Graphics g) {
			int margin = 20;
			int cSize = 2;
			Rectangle r = getBounds();
			int midy = (int) (r.getHeight()/2);
			//				double yScale = (r.getHeight()-margin*2) / 2;
			double[] taps = ((FIRFilterMethod)filterMethod).getFilterTaps();
			if (taps == null) {
				return;
			}
			int nTaps = taps.length;
			double maxTap = 0;
			for (int i = 0; i < nTaps; i++) {
				maxTap = Math.max(maxTap, taps[i]);
			}
			double yScale = (r.getHeight()-margin*2) / (2*maxTap);
			double xScale = (r.getWidth()-margin*2) / (nTaps-1);
			g.setColor(Color.BLUE);
			g.drawLine(margin, margin, margin, getHeight()-margin);
			g.drawLine(margin, midy, getWidth()-margin, midy);
			g.drawString(String.format("%3.2f", maxTap), margin+1, margin);
			g.setColor(Color.RED);
			int x, y;
			for (int i = 0; i < nTaps; i++) {
				x = (int) ( margin + xScale*i);
				y = midy - (int) (taps[i]*yScale);
				g.drawLine(x, midy, x, y);
				g.drawOval(x-cSize, y-cSize, cSize*2+1, cSize*2+1);
			}

		}

		private void paintPoleZeros(Graphics g) {
			IIRFilterMethod iirFilterMethod = (IIRFilterMethod) filterMethod;

			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));

			Insets insets = getInsets();
			Rectangle r = getBounds();
			Point center = new Point(r.width / 2, r.height / 2);
			int radius = Math.min(r.width - 20, r.height - 20) / 2;
			g.drawOval(center.x - radius, center.y - radius, radius * 2,
					radius * 2);
			g.drawLine(center.x - radius, center.y, center.x + radius,
					center.y);
			g.drawLine(center.x, center.y - radius, center.x, center.y
					+ radius);
			Complex[] poles = iirFilterMethod.getPoles(filterParams);
			Complex[] zeros = iirFilterMethod.getZeros(filterParams);
			pointsize = 10;
			g2d.setStroke(new BasicStroke(2));
			g.setColor(Color.RED);
			for (int i = 0; i < iirFilterMethod.poleZeroCount(); i++) {
				if (poles[i] == null) {
					return;
				}
				drawPole(g, poles[i], center, radius);
			}
			g.setColor(Color.BLUE);
			for (int i = 0; i < iirFilterMethod.poleZeroCount(); i++) {
				drawZero(g, zeros[i], center, radius);
			}
		}

		/**
		 * Paint the impulse response for IIR filters: We can calculate an
		 * impulse response over a short time period by putting a 1 into the filter 
		 * function followed by a load of zeros. Probably want about 5 times the number of
		 * filter taps ? 
		 * @param g
		 */
		private void paintIIRImpulseResponse(Graphics g) {
			IIRFilterMethod iirFilterMethod = (IIRFilterMethod) filterMethod;
			if (filterMethod == null) {
				return;
			}
			int nPoints = iirFilterMethod.filterParams.filterOrder * 10;
			Filter filter = iirFilterMethod.createFilter(0);
			filter.prepareFilter();
			double[] input = new double[nPoints];
			double[] output = new double[nPoints];
			input[0] = 1;
			filter.runFilter(input, output);
			// can now plot that. 

			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(PamColors.getInstance().getColor(PamColor.AXIS));

			Insets insets = getInsets();
			Rectangle r = getBounds();
			
		}

		void drawPole(Graphics g, Complex p, Point center, int radius) {
			Point pt = findZPoint(p, center, radius);
			g.drawLine(pt.x - pointsize / 2, pt.y - pointsize / 2, pt.x
					+ pointsize / 2, pt.y + pointsize / 2);
			g.drawLine(pt.x + pointsize / 2, pt.y - pointsize / 2, pt.x
					- pointsize / 2, pt.y + pointsize / 2);
		}

		void drawZero(Graphics g, Complex p, Point center, int radius) {
			Point pt = findZPoint(p, center, radius);
			g.drawOval(pt.x - pointsize / 2, pt.y - pointsize / 2, pointsize,
					pointsize);
		}

		Point findZPoint(Complex complexValue, Point center, int radius) {
			Point p = new Point();
			p.x = (int) (center.x + radius * complexValue.real);
			p.y = (int) (center.y + radius * complexValue.imag);
			return p;
		}
	}

	/*
	 * The bode plot will contain an empty border with a different plot inside
	 * it.
	 */
	class BodePlot extends JPanel {

		private PamAxis xAxis, yAxis;

		private double f1 = .1, f2 = .5;

		int yAxisExtent = 0;

		int xAxisExtent = 0;

		BodePlot() {
			super();
			// setBorder(BorderFactory.createBevelBorder(2));
			// JPanel dummyPanel = new JPanel();
			setBorder(new EmptyBorder(20, 20, 10, 10));
			//				setBorder(new TitledBorder("Bode Plot"));
			setLayout(new BorderLayout());
			// add(BorderLayout.CENTER, dummyPanel);
			// dummyPanel.setLayout(new BorderLayout());
			add(BorderLayout.CENTER, bodeGraph = new BodeGraph());

			xAxis = new PamAxis(0, 10, 100, 10, 0, 100, true, "Frequency (Hz)",
					"%.0f");
			xAxis.setLogScale(true);
			xAxis.setDrawLine(true);
			yAxis = new PamAxis(10, 0, 10, 100, yScales[0], 0, true, "Gain (dB)",
					"%.0f");
			//				yAxis.setInterval(10);
			yAxis.setDrawLine(true);
			setPreferredSize(new Dimension(500,200));

			this.addMouseListener(new BodeMouseListener());

			setToolTipText("Double click on the amplitude scale to change it's range");
		}

		private class BodeMouseListener extends MouseAdapter {

			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() == 2 &&
						me.getX() < bodeGraph.getX()) {
					bodePlot.changeYScale();
				}
			}

		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			sortScales();

			//				yAxis.setRange(bodeGraph.yScaleMin, bodeGraph.yScaleMax);
			// see if there is enough room
			Rectangle r = getBounds();
			Rectangle rp = bodeGraph.getBounds();
			Insets insets = getInsets();
			Insets plotInsets = bodeGraph.getInsets();
			int newYExtent = yAxis.getExtent(g, "180.0");
			int newXExtent = xAxis.getExtent(g, "");
			if (newYExtent != yAxisExtent || newXExtent != xAxisExtent) {
				setBorder(new EmptyBorder(newXExtent, newYExtent,
						insets.bottom, insets.right));
				yAxisExtent = newYExtent;
				xAxisExtent = newXExtent;
			}
			yAxis.drawAxis(g, insets.left - 1, rp.height + insets.top,
					insets.left - 1, insets.top);
			xAxis.drawAxis(g, insets.left, insets.top - 1, insets.left
					+ rp.width, insets.top - 1);

		}

		private int yScaleIndex = 0;
		private double[] yScales = {-90, -60, -30};
		public void changeYScale() {
			if (++yScaleIndex >= yScales.length) {
				yScaleIndex = 0;
			}
			checkYScale();
			repaint();
		}


		private void sortScales() {
			int scaleType = getScaleType();
			if (scaleType == FilterParams.SCALE_LIN) {
				f1 = 0;
			}
			else {
				f1 = filterParams.highPassFreq  / 100;
				f1 = (int) Math.log10(f1);
				f1 = Math.pow(10., f1);
			}
			setF1(f1);
			setF2(sampleRate / 2.);		
			xAxis.setLogScale(scaleType == FilterParams.SCALE_LOG);
			xAxis.setRange(f1, f2);	
		}

		public double getF1() {
			return f1;
		}

		public void setF1(double f1) {
			this.f1 = f1;
			//				if (oldf1 != f1) bodePlot.repaint();
		}

		public double getF2() {
			return f2;
		}

		public void setF2(double f2) {
			this.f2 = f2;
			//				if (oldf2 != f2) bodePlot.repaint();
		}

		/**
		 * @return Returns the xAxis.
		 */
		public PamAxis getXAxis() {
			return xAxis;
		}

		/**
		 * @return Returns the yAxis.
		 */
		public PamAxis getYAxis() {
			return yAxis;
		}

		@Override
		public void repaint() {
			checkYScale();
			super.repaint();
		}


		private void checkYScale() {
			double yScale = 0;
			if (yAxis == null) return;

			if (filterTypes.getSelectedIndex() == 4) {
				double[] g = filterParams.getArbGainsdB();
				if (g != null && g.length >= 1) {
					yScale = g[0];
					for (int i = 1; i < g.length; i++) {
						yScale = Math.max(yScale, g[i]);
					}
					// now round to nearest 10dB
					yScale = 10 * Math.round(yScale/10.);
				}
			}
			yAxis.setRange(yScale+yScales[yScaleIndex], yScale);
		}
	}

	class BodeGraph extends PamPanel {

		//			double yScaleMax = 0;

		//			double yScaleMin = -70;

		Rectangle boundsRect;

		Color stopGray = new Color(235,235, 235);

		BodeGraph() {
			super(PamColor.PlOTWINDOW);
			//				PamColors.getInstance().registerComponent(this,
			//						PamColors.PamColor.PlOTWINDOW);
		}


		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (filterMethod == null) {
				return;
			}
			bodePlot.sortScales();

			boundsRect = getBounds();
			Graphics2D g2d = (Graphics2D) g;

			// plot from a couple of decades below the cut off frequency up to
			// half the sample rate.

			shadeStopBands(g);

			//				plotANSIcurves(g);

			bodePlot.getXAxis().drawGrid(g, getSize(), 1);
			bodePlot.getYAxis().drawGrid(g, getSize(), 1);


			g2d.setStroke(new BasicStroke(2));
			/*
			 * It's on a log scale, so set up enough points to fill the plot on
			 * a log scale
			 */
			int nPoints = Math.max(boundsRect.width, 1024);
			double[] freqPoints = new double[nPoints];
			double[] gainPoints = new double[nPoints];
			double[] phasePoints = new double[nPoints];

			double xScale = (double) boundsRect.width / (double) nPoints;

			//				double logf1 = Math.log10(f1);
			//				double logf2 = Math.log10(f2);
			//				double step = (logf2 - logf1) / (nPoints - 1);
			int i = 0;
			double logf;
			while (i < freqPoints.length) {
				//					logf = logf1 + step * i;
				//					freqPoints[i] = Math.pow(10., logf);
				freqPoints[i] = bodePlot.getXAxis().getDataValue(i*xScale);
				gainPoints[i] = filterMethod.getFilterGain(
						freqPoints[i] / sampleRate * 2 * Math.PI)
						/ filterMethod.getFilterGainConstant();
				phasePoints[i] = filterMethod.getFilterPhase(
						freqPoints[i] / sampleRate * 2 * Math.PI);
				i++;
			}
			int x1, x2, y1, y2;
			// g2d.setStroke(new Stroke())
			g2d.setColor(Color.RED);
			x1 = 0;
			y1 = yGainCoord(gainPoints[0]);
			for (i = 1; i < freqPoints.length; i++) {
				x2 = (int)Math.round(i*xScale);
				y2 = yGainCoord(gainPoints[i]);
				g2d.drawLine(x1, y1, x2, y2);
				x1 = x2;
				y1 = y2;
			}

		}

		/**
		 * Plots ansi standard 1/3 octave curves based around th emid frequency 
		 * of a bandpass filter. 
		 * @param g
		 */
		private void plotANSIcurves(Graphics g) {
			if (filterParams.filterBand != FilterBand.BANDPASS) {
				return;
			}
			double midFreq = Math.sqrt(filterParams.highPassFreq * filterParams.lowPassFreq);
			plotANSICurve(g, midFreq, att0min, att0max, Color.BLACK);
			plotANSICurve(g, midFreq, att1min, att1max, Color.BLUE);
			plotANSICurve(g, midFreq, att2min, att2max, Color.GREEN);
		}


		private void plotANSICurve(Graphics g, double midFreq, double[] attMin,
				double[] attMax, Color col) {
			g.setColor(col);
			int x1, x2, y1, y2;
			PamAxis xAx = bodePlot.xAxis;
			PamAxis yAx = bodePlot.yAxis;
			for (int i = 1; i < relFreq.length; i++) {
				x1 = (int) xAx.getPosition(midFreq * relFreq[i-1]);
				x2 = (int) xAx.getPosition(midFreq * relFreq[i]);
				y1 = (int) yAx.getPosition(-attMin[i-1]);
				y2 = (int) yAx.getPosition(-attMin[i]);
				g.drawLine(x1, y1, x2, y2);
				y1 = (int) yAx.getPosition(-attMax[i-1]);
				y2 = (int) yAx.getPosition(-attMax[i]);
				g.drawLine(x1, y1, x2, y2);
			}
			for (int i = 1; i < relFreq.length; i++) {
				x1 = (int) xAx.getPosition(midFreq / relFreq[i-1]);
				x2 = (int) xAx.getPosition(midFreq / relFreq[i]);
				y1 = (int) yAx.getPosition(-attMin[i-1]);
				y2 = (int) yAx.getPosition(-attMin[i]);
				g.drawLine(x1, y1, x2, y2);
				y1 = (int) yAx.getPosition(-attMax[i-1]);
				y2 = (int) yAx.getPosition(-attMax[i]);
				g.drawLine(x1, y1, x2, y2);
			}
		}


		private void shadeStopBands(Graphics g) {
			if (filterTypes.getSelectedIndex() >= 4) return;
			PamAxis xAx = bodePlot.xAxis;
			int h = this.getHeight();
			int w = this.getWidth();
			int x1, x2;
			g.setColor(stopGray);
			double f1 = Math.min(filterParams.highPassFreq, filterParams.lowPassFreq);
			double f2 = Math.max(filterParams.highPassFreq, filterParams.lowPassFreq);
			switch(filterParams.filterBand) {
			case BANDPASS:
				x1 = (int) xAx.getPosition(f1);
				g.fillRect(0, 0, x1, h);
				x1 = (int) xAx.getPosition(f2);
				g.fillRect(x1, 0, w, h);
				break;
			case HIGHPASS:
				x1 = (int) xAx.getPosition(filterParams.highPassFreq);
				g.fillRect(0, 0, x1, h);
				break;
			case LOWPASS:
				x1 = (int) xAx.getPosition(filterParams.lowPassFreq);
				g.fillRect(x1, 0, w, h);
				break;
			case BANDSTOP:
				x1 = (int) xAx.getPosition(f1);
				x2 = (int) xAx.getPosition(f2);
				g.fillRect(x1, 0, x2-x1, h);
				break;
			}

		}

		int yGainCoord(double gain) {
			double db = 20. * Math.log10(gain);
			return (int) bodePlot.yAxis.getPosition(db);
		}
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public FilterParams getFilterParams() {
		return filterParams;
	}


}
