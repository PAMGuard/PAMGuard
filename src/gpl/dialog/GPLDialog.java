package gpl.dialog;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import PamController.settings.output.xml.XMLImportData;
import PamView.component.FixedLabel;
import PamView.dialog.EnumComboBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.ScrollingPamLabel;
import PamView.dialog.SourcePanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamNorthPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.blockprocess.BlockMode;
import PamguardMVC.blockprocess.PamBlockParams;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import gpl.GPLControlledUnit;
import gpl.GPLParameters;
import gpl.GPLProcess;
import gpl.GPLParameters.ConnectType;
import gpl.contour.ContourMerge;
import noiseMonitor.ResolutionPanel;

public class GPLDialog extends PamDialog {

	private static GPLDialog singleInstance;
	private GPLControlledUnit gplControlledUnit;
	private GPLParameters gplParams;
	private SourcePanel sourcePanel;
	private ResolutionPanel fftResPanel;
	private EnumComboBox blockComboBox;
//	private PamBlockParamsPanel blockParamsPanel;

	private JTextField whiteTime, whiteFactor, xp1, xp2, minFreq, maxFreq;
	private JTextField noise_ceil, thresh, minGap, minLength, maxLength;
	private JLabel minFreqBin, maxFreqBin;
	private final String helpPoint = "detectors.gpl.docs.gpldetector";
	
	private JLabel lab_xp1, lab_xp2, lab_noise_ceil, lab_thresh, lab_minGap, lab_minlength, lab_maxLength;
	
	private static String tip_xp1 = "Power law (normalised over time). The sum of two powers should be about 2.5";
	private static String tip_xp2 = "Power law (normalised over frequency). The sum of two powers should be about 2.5";
	private static String tip_noise_ceil = "A lower threshold used to select the start and end of calls which have exceeded the Detection ON Threshold";
	private static String tip_threshold = "Detection threshold factor";
	private static String tip_minGap = "Minimum gap between detections (FFT time bins)";
	private static String tip_minLength = "Minimum length of detection in seconds";
	private static String tip_maxLength = "Maximum length of detection in seconds";
	
	
	private JTextField contourCut, contourArea;
	private JRadioButton connect4, connect8;
	private EnumComboBox contourMerge;
	private FixedLabel lab_contourCut;
	private JCheckBox nullContours;
	
	private JTextArea description;
	
	private JTextField importFile;
	
	public static final String approx = "\u2248";

	
	private GPLDialog(Window parentFrame, GPLControlledUnit gplControlledUnit) {
		super(parentFrame, gplControlledUnit.getUnitName() + " settings", true);
		this.gplControlledUnit = gplControlledUnit;

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel fftPanel = new JPanel();
		fftPanel.setLayout(new BoxLayout(fftPanel, BoxLayout.Y_AXIS));
		fftPanel.setBorder(new TitledBorder("FFT Data Source"));
		sourcePanel = new SourcePanel(this, FFTDataUnit.class, true, true);
		int nOuts = gplControlledUnit.getGplProcess().getNumOutputDataBlocks();
		for (int i = 0; i < nOuts; i++) {
			sourcePanel.excludeDataBlock(gplControlledUnit.getGplProcess().getOutputDataBlock(i), true);
		}
		sourcePanel.addSelectionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSourcePanelDetails();
			}
		});
		fftPanel.add(sourcePanel.getPanel());
		fftResPanel = new ResolutionPanel();
		fftPanel.add(fftResPanel.getPanel());

		mainPanel.add(fftPanel);

		JPanel whitePanel = new JPanel(new GridBagLayout());
		whitePanel.setBorder(new TitledBorder("Spectrogram Whitening"));
		GridBagConstraints c = new PamGridBagContraints();
		whitePanel.add(new JLabel("Whitening time period ", JLabel.RIGHT), c);
		c.gridx++;
		whitePanel.add(whiteTime = new JTextField(6), c);
		c.gridx++;
		whitePanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx = 0; 
		c.gridy++;
		c.gridwidth = 1;
		whitePanel.add(new JLabel("Process blocking ", JLabel.RIGHT), c);
		c.gridx++;
		blockComboBox = new EnumComboBox(GPLProcess.blockModes);
		c.gridwidth = 2;
		whitePanel.add(blockComboBox, c);
		mainPanel.add(whitePanel);
		tabbedPane.add("FFT", mainPanel);

		/**
		 * Detection panel
		 */
		JPanel detPanel = new JPanel(new GridBagLayout());
		detPanel.setBorder(new TitledBorder("Detection"));
		c = new PamGridBagContraints();
		detPanel.add(new JLabel("Minimum Frrequency ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(minFreq = new JTextField(6), c);
		c.gridx++;
		detPanel.add(minFreqBin = new FixedLabel("(bin 10245)"), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Maximum Frequency ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(maxFreq = new JTextField(6), c);
		c.gridx++;
		detPanel.add(maxFreqBin = new FixedLabel("        "), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Whitening scale factor ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(whiteFactor = new JTextField(6), c);
		c.gridx++;
		c.gridwidth = 2;
		detPanel.add(new JLabel(" (1 to 1.5ish)", JLabel.LEFT), c);
		c.gridx = 0; 
		c.gridy++;
		c.gridwidth = 1;
		detPanel.add(new JLabel("Power law (time)", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(xp1 = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" (2.5)", JLabel.LEFT), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Power law (frequency)", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(xp2 = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" (0.5)", JLabel.LEFT), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Detection ON Threshold ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(thresh = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" (200)", JLabel.LEFT), c);
		c.gridx++;
		detPanel.add(lab_thresh = new FixedLabel("      "), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Detection OFF Threshold ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(noise_ceil = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" (50)", JLabel.LEFT), c);
		c.gridx++;
		detPanel.add(lab_noise_ceil = new FixedLabel("      "), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Minimum Gap ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(minGap = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" (2)", JLabel.LEFT), c);
		c.gridx++;
		detPanel.add(lab_minGap = new FixedLabel("      "), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Minimum Length ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(minLength = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx++;
		detPanel.add(lab_minlength = new FixedLabel("      "), c);
		c.gridx = 0; 
		c.gridy++;
		detPanel.add(new JLabel("Maximum Length ", JLabel.RIGHT), c);
		c.gridx++;
		detPanel.add(maxLength = new JTextField(6), c);
//		c.gridx++;
//		detPanel.add(new JLabel(" s", JLabel.LEFT), c);
		c.gridx++;
		detPanel.add(lab_maxLength = new FixedLabel("      "), c);

		tabbedPane.add("Detection", new PamNorthPanel(detPanel));

		minFreq.getDocument().addDocumentListener(new FreqBinChange(minFreq, minFreqBin));
		maxFreq.getDocument().addDocumentListener(new FreqBinChange(maxFreq, maxFreqBin));

		minFreq.setToolTipText("Minimum frequency for power law summation");
		maxFreq.setToolTipText("Maximum frequency for power law summation");
		whiteTime.setToolTipText("Time constant for spectrogram whitening");
		whiteFactor.setToolTipText("Spectrogram whitening scale factor");

		xp1.setToolTipText(tip_xp1);
		xp2.setToolTipText(tip_xp2);
		//		xp1.setToolTipText("Power");
		noise_ceil.setToolTipText(tip_noise_ceil);
		thresh.setToolTipText(tip_threshold);
		minGap.setToolTipText(tip_minGap);
		minLength.setToolTipText(tip_minLength);
		maxLength.setToolTipText(tip_maxLength);
		
		new FieldListener(thresh, lab_thresh) {
			@Override
			protected String getLabelText(String txt) {
				return getThresholdLabel(txt);
			}
		};		
		new FieldListener(xp1, lab_thresh) {
			@Override
			protected String getLabelText(String txt) {
				return getThresholdLabel(txt);
			}
		};
		new FieldListener(xp2, lab_thresh) {
			@Override
			protected String getLabelText(String txt) {
				return getThresholdLabel(txt);
			}
		};new FieldListener(noise_ceil, lab_noise_ceil) {
			@Override
			protected String getLabelText(String txt) {
				return getNoiseCeilLabel(txt);
			}
		};		
		new FieldListener(xp1, lab_noise_ceil) {
			@Override
			protected String getLabelText(String txt) {
				return getNoiseCeilLabel(txt);
			}
		};
		new FieldListener(xp2, lab_noise_ceil) {
			@Override
			protected String getLabelText(String txt) {
				return getNoiseCeilLabel(txt);
			}
		};
		new FieldListener(minGap, lab_minGap) {
			@Override
			protected String getLabelText(String txt) {
				return getMinGapLabel(txt);
			}
		};
		new FieldListener(minLength, lab_minlength) {
			@Override
			protected String getLabelText(String txt) {
				return getMinLengthLabel(txt);
			}
		};
		new FieldListener(maxLength, lab_maxLength) {
			@Override
			protected String getLabelText(String txt) {
				return getMaxLengthLabel(txt);
			}
		};
		
		/*
		 * contour panel
		 */
		JPanel contPanel = new JPanel();
		contPanel.setBorder(new TitledBorder("Cotour detection"));
		contPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		contPanel.add(new JLabel("Contour detection threshold ", JLabel.RIGHT), c);
		c.gridx++;
		contPanel.add(contourCut = new JTextField(6), c);
		c.gridx++;
		contPanel.add(lab_contourCut = new FixedLabel(" units"), c);	
		c.gridx = 0;
		c.gridy++;
		contPanel.add(new JLabel("Minimum contour area ", JLabel.RIGHT), c);
		c.gridx++;
		contPanel.add(contourArea = new JTextField(6), c);
		c.gridx++;
		contPanel.add(new JLabel(" pixels", JLabel.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		contPanel.add(connect4 = new JRadioButton("Connect 4"), c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		contPanel.add(connect8 = new JRadioButton("Connect 8"), c);
		ButtonGroup bg = new ButtonGroup();
		bg.add(connect4);
		bg.add(connect8);
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy++;
		contPanel.add(new JLabel("Multiple contours ", JLabel.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		contourMerge = new EnumComboBox(ContourMerge.values());
		contPanel.add(contourMerge, c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		nullContours = new JCheckBox("Keep detections with no contour");
		contPanel.add(nullContours, c);
		
		contourCut.setToolTipText("Contour detection threshold");
		contourArea.setToolTipText("Minimum area for detected contours");
		connect4.setToolTipText("Joins spectrogram pixels into contours if the are adjacent in time or frequency (top, bottom and sides)");
		connect8.setToolTipText("Joins all touching spectrogram pixels into contours (top, bottom, sides and diagonals)");
		nullContours.setToolTipText("Otherwise, detections which have no contours will not be saved");
//		ContourMerge[] mergeTypes = ContourMerge.values();
//		for (int i = 0; i < mergeTypes.length; i++) {
//			contourMerge.addItem(mergeTypes[i]);
//		}	
//		new FieldListener(contourCut, lab_contourCut) {
//			@Override
//			protected String getLabelText(String txt) {
//				return getContourLabel(txt);
//			}
//		};
		
		
		tabbedPane.add(new PamAlignmentPanel(contPanel, BorderLayout.NORTH), "Contours");

		// notes panel. 
		JPanel notePanel = new JPanel(new BorderLayout());
		notePanel.add(BorderLayout.NORTH, importFile = new JTextField(30));
		notePanel.add(BorderLayout.CENTER, description = new JTextArea());
		notePanel.setBorder(new TitledBorder("Configuration notes"));
		description.setLineWrap(true);
		importFile.setBorder(new BevelBorder(BevelBorder.LOWERED));
		description.setBorder(new BevelBorder(BevelBorder.LOWERED));
		description.setToolTipText("Freeform notes to help keep track of things ...");
		tabbedPane.add(notePanel, "Notes");
		getDefaultButton().setText("Defaults");
		getDefaultButton().setToolTipText("Load default settings, import and export settings to files");
		// add pane for PamBlockProcess params
		
//		blockParamsPanel = new PamBlockParamsPanel();
//		tabbedPane.add("Block Parameters", blockParamsPanel.getDialogComponent());

		setHelpPoint(helpPoint );
		setDialogComponent(tabbedPane);
	}
	
	protected String getThresholdLabel(String txt) {
		return getNoiseCeilLabel(txt);
	}
	protected String getNoiseCeilLabel(String txt) {
		double thresh = Double.valueOf(txt);
		double db = 10*Math.log10(thresh) / getTotalPower();
		return String.format(approx + "%3.1fdB", db);
	}
	protected String getMinGapLabel(String txt) {
		double secs = Integer.valueOf(txt)*getSecsPerBin();
		return String.format(" bins (%3.2fs)", secs);
	}
	
	protected String getMinLengthLabel(String txt) {
		double bins = Double.valueOf(txt) / getSecsPerBin();
		return String.format(" s (%d FFT's)", (int) (bins+.1));
	}
	protected String getMaxLengthLabel(String txt) {
		double bins = Double.valueOf(txt) / getSecsPerBin();
		return String.format(" s (%d FFT's)", (int) (bins+.1));
	}	
	
//	protected String getContourLabel(String txt) {
//		/*
//		 * So far as I can tell, the contour threshold is quite simply the original data - the central 
//		 * mean for each frequency (i.e. the white data) then divided by the central mean again. so if we have 
//		 * S and N it's (S-N)/N' There seems to be little physical meaning left though, so give up for now
//		 */
//		double cut = Double.valueOf(txt);
//		return String.format(approx+"%3.1fdB", 20*Math.log10(cut));
//	}

	/**
	 * Get the time scale in seconds per FFT bin. 
	 * @return time scale
	 */
	private double getSecsPerBin() {
		FFTDataBlock fftDataBlock = gplControlledUnit.getGplProcess().getSourceFFTData();
		return fftDataBlock.getFftLength() / fftDataBlock.getSampleRate();
	}
	/**
	 * Get the sum of what's in the two power laws
	 */
	private double getTotalPower() {
		double totPower = 0;
		try {
			totPower += Double.valueOf(xp1.getText());
			totPower += Double.valueOf(xp2.getText());
		}
		catch (NumberFormatException e) {
			return 0;
		}
		return totPower;
	}
	
	protected void setSourcePanelDetails() {
		PamDataBlock sp = sourcePanel.getSource();
		fftResPanel.setParams((FFTDataBlock) sp);
	}

	public static GPLParameters showDialog(Window parentFrame, GPLControlledUnit gplControlledUnit) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame || singleInstance.gplControlledUnit != gplControlledUnit) {
			singleInstance = new GPLDialog(parentFrame, gplControlledUnit);
		}
		singleInstance.gplParams = gplControlledUnit.getGplParameters().clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);		

		return singleInstance.gplParams;
	}

	private void setParams() {
		sourcePanel.setSource(gplParams.fftSourceName);
		sourcePanel.setChannelList(gplParams.sequenceMap);
		setSourcePanelDetails();

		setNumericParams(gplParams);
		
		
//		blockParamsPanel.setParams(gplParams.blockParams);
	}
	
	/**
	 * Separate from source params since they don't get changed when a new default is selected. 
	 * @param params
	 */
	private void setNumericParams(GPLParameters params) {
		minFreq.setText(String.format("%3.1f", params.minFreq));
		maxFreq.setText(String.format("%3.1f", params.maxFreq));
		whiteTime.setText(String.format("%3.1f", params.backgroundTimeSecs));
		whiteFactor.setText(String.format("%3.1f", params.white_fac_x));
		if (params.blockParams != null) {
			blockComboBox.setSelectedItem(params.blockParams.blockMode);
		}

		xp1.setText(String.format("%3.1f", params.xp1));
		xp2.setText(String.format("%3.1f", params.xp2));
		noise_ceil.setText(String.format("%3.1f", params.noise_ceiling));
		thresh.setText(String.format("%3.1f", params.thresh));
		minGap.setText(String.format("%d", params.minPeakGap));
		minLength.setText(String.format("%3.2f", params.minCallLengthSeconds));
		maxLength.setText(String.format("%3.2f", params.maxCallLengthSeconds));

		contourCut.setText(String.format("%3.1f", params.contourCut));
		contourArea.setText(String.format("%d", params.minContourArea));
		connect4.setSelected(params.connectType == ConnectType.CONNECT4);
		connect8.setSelected(params.connectType == ConnectType.CONNECT8);
		contourMerge.setSelectedItem(params.contourMerge);
		nullContours.setSelected(params.keepNullContours);

		description.setText(params.description);
		gplParams.importFile = params.importFile;
		importFile.setText(params.importFile);

	}

	@Override
	public boolean getParams() {
		PamDataBlock pdb = sourcePanel.getSource();
		if (pdb == null) {
			return showWarning("No data FFT source selected");
		}
		
		gplParams.fftSourceName = pdb.toString(); // SourcePanel lookup uses toString(), not getLongDataName()
		gplParams.sequenceMap = sourcePanel.getChannelList();
		if (gplParams.sequenceMap == 0) {
			return showWarning("you must select at least one data channel");
		}

		try {
			gplParams.minFreq = Double.valueOf(minFreq.getText());
			gplParams.maxFreq = Double.valueOf(maxFreq.getText());
			if (gplParams.minFreq < 0 | gplParams.minFreq >= gplParams.maxFreq) {
				return showWarning("Minimum frequency must be > 0 and < max Frequency.");
			}
			
			/**
			 *  Make sure maximum frequency bin is less suitable
			 *  It's not quite as simple as <= Nyquist, but close
			 */
			FFTDataBlock fftdb = (FFTDataBlock) pdb;
			int maxBin = (fftdb.getFftLength()/2)-1;
			double fStep = fftdb.getFftLength() / fftdb.getSampleRate();
			double maxFreq = Math.floor(maxBin / fStep);
			if (Math.round(gplParams.maxFreq * fStep) > maxBin+1) {	
				return showWarning("Max frequency must be less than or equal to " + maxFreq);
			}
				
			gplParams.backgroundTimeSecs = Double.valueOf(whiteTime.getText());
			gplParams.white_fac_x = Double.valueOf(whiteFactor.getText());
			
			if (gplParams.blockParams == null) {
				gplParams.blockParams = new PamBlockParams();
			}
			gplParams.blockParams.blockMode = (BlockMode) blockComboBox.getSelectedItem();
			gplParams.blockParams.blockLengthMillis = (long) (gplParams.backgroundTimeSecs * 1000.);
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter in FFT panel");
		}

		try {
			gplParams.xp1 = Double.valueOf(xp1.getText());
			gplParams.xp2 = Double.valueOf(xp2.getText());
			gplParams.noise_ceiling = Double.valueOf(noise_ceil.getText());
			gplParams.thresh = Double.valueOf(thresh.getText());
			gplParams.minPeakGap = Integer.valueOf(minGap.getText());
			gplParams.minCallLengthSeconds = Double.valueOf(minLength.getText());
			gplParams.maxCallLengthSeconds = Double.valueOf(maxLength.getText());

		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter in Detection panel");
		}
		
		// contour stuff
		try {
			gplParams.contourCut = Double.valueOf(contourCut.getText());
			gplParams.minContourArea = Integer.valueOf(contourArea.getText());
			gplParams.connectType = connect4.isSelected() ? ConnectType.CONNECT4 : ConnectType.CONNECT8; 
			gplParams.contourMerge = (ContourMerge) contourMerge.getSelectedItem();
			gplParams.keepNullContours = nullContours.isSelected();
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid parameter in Contour panel");
		}
		
		gplParams.description = description.getText();
		gplParams.importFile = importFile.getText();
//		PamBlockParams newParams = blockParamsPanel.getParams();
//		if (newParams==null) {
//			return false;
//		}
//		gplParams.blockParams=newParams;
		
		return true;
	}

	private class FreqBinChange implements DocumentListener {
		private JTextField fText;
		private JLabel fBin;

		public FreqBinChange(JTextField fText, JLabel fBin) {
			super();
			this.fText = fText;
			this.fBin = fBin;
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			update();
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			update();
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			update();
		}
		private void update() {
			try {
				double f = Double.parseDouble(fText.getText());
				FFTDataBlock fdb = (FFTDataBlock) sourcePanel.getSource();
				if (fdb == null) {
					fBin.setText(" - ");
					return;
				}
				f = f * fdb.getFftLength() / fdb.getSampleRate();
				int b;
				if (fText == minFreq) {
					b = (int) f;
				}
				else {
					b = (int) Math.ceil(f);
				}
				fBin.setText(String.format(" Hz (bin %d)", b));
			}
			catch (Exception e) {
				fBin.setText(" - ");
			}
		}

	}

	@Override
	public void cancelButtonPressed() {
		gplParams = null;
	}


	/**
	 * this one get's called first so need to override to show a menu !
	 */
	public void restoreDefaultSettingsQ() {
		ArrayList<GPLParameters> defaultList = gplControlledUnit.getDefaultSettingsList();
		JPopupMenu popMenu = new JPopupMenu();
		int nDef = 0;
		for (GPLParameters dp:defaultList) {
			JMenuItem menuItem = new JMenuItem(dp.description);
			menuItem.setToolTipText("Built in default for " + dp.description);
			menuItem.addActionListener(new DefaultSettingAction(dp));
			popMenu.add(menuItem);
			nDef++;
		}
		if (nDef > 0) {
			popMenu.addSeparator();
		}
		JMenuItem item = new JMenuItem("Export current values ...");
		item.setToolTipText("Export current settings to a XML file");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportSettings(gplParams.description);
			}
		});
		popMenu.add(item);
		item = new JMenuItem("Import configuration ...");
		item.setToolTipText("Import settings from a XML file");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				importSettings();
			}
		});
		popMenu.add(item);
		
		JButton defButton = getDefaultButton();
		popMenu.show(defButton, defButton.getWidth()/2, defButton.getHeight()/2);
	}

	protected void importSettings() {
		XMLImportData importData = super.importSettings(GPLParameters.class);
		if (importData == null) {
			return;
		}
		Object set = importData.getImportObject();
		if (set instanceof GPLParameters) {
			GPLParameters newParams = (GPLParameters) set;
			if (importData.getImportFile() != null) {
				String name = importData.getImportFile().getName();
				newParams.importFile = "Imported from " + name;
			}
			else {
				newParams.importFile = "Imported from unknown file";
			}
			setNumericParams(newParams);
		}
	}

	protected void exportSettings(String name) {
		if (getParams() == false) {
			return;
		}
		
		super.exportSettings(gplControlledUnit, gplParams, null);
	}

	private class DefaultSettingAction implements ActionListener {
		GPLParameters defParams;

		public DefaultSettingAction(GPLParameters defParams) {
			super();
			this.defParams = defParams;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int ans = JOptionPane.showConfirmDialog(getContentPane(), 
					"Are you sure you want to restore all default settings for " + defParams.description, 
					getTitle(), JOptionPane.YES_NO_OPTION);
			if (ans == JOptionPane.YES_OPTION) {
				setNumericParams(defParams);

			}
		}

	}

	private abstract class FieldListener implements DocumentListener {
		
		private JTextField textField;
		private JLabel label;
		public FieldListener(JTextField textField, JLabel label) {
			super();
			this.textField = textField;
			this.label = label;
			textField.getDocument().addDocumentListener(this);
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			change(e);
		}
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			change(e);
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			change(e);
		}

		private void change(DocumentEvent e) {
			String txt = textField.getText();
			if (txt == null || txt.length() == 0) {
				label.setText("");
				return;
			}
			try {
				String labText = getLabelText(txt);
				label.setText(labText);
			}
			catch (Exception ex) {
				label.setText("??");
			}
		}

		/**
		 * Interpret the text from the text field and return something to 
		 * write into the label. 
		 * @param txt text from user input text field.
		 * @return test to write to label
		 */
		protected abstract String getLabelText(String txt);
		
		
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
}
