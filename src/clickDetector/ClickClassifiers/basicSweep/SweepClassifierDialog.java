package clickDetector.ClickClassifiers.basicSweep;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamSettings;
import fftFilter.FFTFilterDialog;
import fftFilter.FFTFilterParams;
import fftManager.FFTLengthModel;
import fftManager.FFTLengthModeled;
import PamModel.ImportExport;
import PamModel.ImportExportUser;
import PamUtils.PamUtils;
import PamView.PamSymbolViewer;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;
import clickDetector.ClickAlarm;
import clickDetector.ClickControl;

import java.awt.Insets;

/**
 * The dialog for the sweep click classifier. 
 * 
 * @author Doug Gillespie
 *
 */
public class SweepClassifierDialog extends PamDialog implements ImportExportUser {

	private static SweepClassifierDialog singleInstance;

	private SweepClassifierSet sweepClassifierSet;
	
    private ClickControl clickControl = null;
    
    private ImportExport importExport;

	/**
	 * Need to check we're not refusing to allow to keep the same code !
	 */
	private int classifierItemRow;

    /**
     * ComboBox to list available alarms
     */
    private JComboBox alarmChooser;

	private SweepClassifier sweepClassifier;

	private ArrayList<IdBlock> idBlocks = new ArrayList<IdBlock>();
	
	
	private AmplitudeBlock amplitudeBlock; 
	
	private FilterBlock filterBlock;

	private OptionsBlock optionsBlock;

	private LengthBlock lengthBlock;

	private EnergyBandsBlock energyBandsBlock;

	private PeakFreqBlock peakFreqBlock;

	private ZeroXBlock zeroXBlock;
	
	private XCorrBlock xCorrBlock;

	private BearingBlock bearingBlock;
	
//	private ZeroSweepBlock zeroSweepBlock;

	private AlarmBlock alarmBlock;
    
    private JButton importButton, exportButton;

    /**
     * Maximum amount of elapsed time between detections to ring the alarm
     */
    private JTextField maxTime;

	private final int freqTextLength = 7;
	
	private final int dbTextLength = 3;



	private SweepClassifierDialog(Window parentFrame, SweepClassifier sweepClassifier) {
		super(parentFrame, "Classifier Parameters", true);
		this.sweepClassifier = sweepClassifier;
		setWarnDefaultSetting(false);
		JPanel mainPanel = new JPanel();
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setLayout(new BorderLayout());
		
		idBlocks.add(optionsBlock = new OptionsBlock());
		idBlocks.add(filterBlock = new FilterBlock());
		idBlocks.add(amplitudeBlock = new AmplitudeBlock());
		idBlocks.add(lengthBlock = new LengthBlock());
		idBlocks.add(energyBandsBlock = new EnergyBandsBlock());
		idBlocks.add(peakFreqBlock = new PeakFreqBlock());
		idBlocks.add(zeroXBlock = new ZeroXBlock());
		idBlocks.add(xCorrBlock = new XCorrBlock());
		idBlocks.add(bearingBlock = new BearingBlock());

//		idBlocks.add(zeroSweepBlock = new ZeroSweepBlock());
        idBlocks.add(alarmBlock = new AlarmBlock());

        mainPanel.add(BorderLayout.NORTH, optionsBlock);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel wavePanel = new JPanel();
        wavePanel.setLayout(new BoxLayout(wavePanel, BoxLayout.Y_AXIS));
        wavePanel.add(filterBlock);
        wavePanel.add(amplitudeBlock);
        wavePanel.add(lengthBlock);
        wavePanel.add(zeroXBlock);
        tabbedPane.add("Waveform", new PamNorthPanel(wavePanel));
        
        JPanel specPanel = new JPanel();
        specPanel.setLayout(new BoxLayout(specPanel, BoxLayout.Y_AXIS));
        specPanel.add(energyBandsBlock);
        specPanel.add(peakFreqBlock);
        tabbedPane.add("Spectrum", new PamNorthPanel(specPanel));
                
        JPanel bearingPanel = new JPanel();
        bearingPanel.setLayout(new BoxLayout(bearingPanel, BoxLayout.Y_AXIS));
        bearingPanel.add(xCorrBlock);
        bearingPanel.add(bearingBlock);
        tabbedPane.add("Bearing", new PamNorthPanel(bearingPanel));

        JPanel optsPanel = new JPanel();
        optsPanel.setLayout(new BoxLayout(optsPanel, BoxLayout.Y_AXIS));
        optsPanel.add(alarmBlock);
        tabbedPane.add("Options", new PamNorthPanel(optsPanel));

        
//        
//        
//		for (int i = 0; i < idBlocks.size(); i++) {
//			mainPanel.add(idBlocks.get(i));
//		}
        mainPanel.add(BorderLayout.CENTER, tabbedPane);
        setDialogComponent(mainPanel);
		
//		setDialogComponent(new JScrollPane(mainPanel));
		JPanel buttonPanel = this.getButtonPanel();
		importExport = new ImportExport("Click classifier settings", "pgccs", this);
		buttonPanel.add(importExport.getImportButton(), 2);
		buttonPanel.add(importExport.getExportButton(), 3);

		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_betterClassification");
//		this.setResizable(true);
	}

	/**
     * New constructor required in order to pass clickControl object, so that
     * we can access the alarm list through clickParameters
     * @param parentFrame
     * @param clickControl
     * @param sweepClassifier
     * @param sweepClassifierSet
     * @return
     */
	public static SweepClassifierSet showDialog(Window parentFrame,
            ClickControl clickControl,
            SweepClassifier sweepClassifier,
			SweepClassifierSet sweepClassifierSet) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame ||
				sweepClassifier != singleInstance.sweepClassifier) {
			singleInstance = new SweepClassifierDialog(parentFrame, sweepClassifier);
		}
        singleInstance.clickControl = clickControl;
        showDialog(parentFrame, sweepClassifier, sweepClassifierSet);
        return singleInstance.sweepClassifierSet;
    }

	public static SweepClassifierSet showDialog(Window parentFrame, SweepClassifier sweepClassifier, 
			SweepClassifierSet sweepClassifierSet) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame ||
				sweepClassifier != singleInstance.sweepClassifier) {
			singleInstance = new SweepClassifierDialog(parentFrame, sweepClassifier);
		}
		if (sweepClassifierSet != null) {
			singleInstance.sweepClassifierSet = sweepClassifierSet.clone();
		}
		else {
			singleInstance.sweepClassifierSet = null;
		}
		singleInstance.classifierItemRow = sweepClassifier.sweepClassifierParameters.getSetRow(sweepClassifierSet);
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.sweepClassifierSet;
	}

    /**
     * add the list of available alarms to the ComboBox
     */
    private void addAlarmList() {
        if (clickControl.getClickParameters().clickAlarmList==null)return;
    	alarmChooser.removeAllItems();
        
        for (int i=0 ; i<clickControl.getClickParameters().clickAlarmList.size() ; i++ ) {
            alarmChooser.addItem(clickControl.getClickParameters().clickAlarmList.get(i));
        }
    }

	private void setParams() {
        singleInstance.addAlarmList();
		for (int i = 0; i < idBlocks.size(); i++) {
			idBlocks.get(i).setParams();
		}
		enableAllBlocks();
	}

	@Override
	public void cancelButtonPressed() {
		sweepClassifierSet = null;
	}

	@Override
	public boolean getParams() {
		if (sweepClassifierSet == null) {
			sweepClassifierSet = new SweepClassifierSet();
		}
		for (int i = 0; i < idBlocks.size(); i++) {
			if (idBlocks.get(i).getParams() == false) {
				return false;
			}
		}
		return sweepClassifierSet.canProcess(sweepClassifier.getClickDetector().getSampleRate(), true);
	}

	private void enableAllBlocks() {
		for (int i = 0; i < idBlocks.size(); i++) {
			idBlocks.get(i).enableControls();
		}
	}

	@Override
	public void restoreDefaultSettings() {
		String[] speciesList = SweepClassifierSet.defaultSpecies;
		JPopupMenu menu = new JPopupMenu();
		JMenuItem menuItem;
		for (int i = 0; i < speciesList.length; i++) {
			menuItem = new JMenuItem(speciesList[i]);
			menuItem.addActionListener(new DefaultSetting(speciesList[i]));
			menu.add(menuItem);
		}
		JButton db = getDefaultButton();
		menu.show(db, db.getWidth()/2, db.getHeight()/2);
	}
	
	class DefaultSetting implements ActionListener {
		
		private String species;
		
		public DefaultSetting(String species) {
			super();
			this.species = species;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (sweepClassifierSet == null) {
				sweepClassifierSet = new SweepClassifierSet();
			}
			sweepClassifierSet.setSpeciesDefaults(species);
			setParams();
		}
	}
	
	private class AmplitudeBlock extends IdBlock {
		private JTextField[] ampRange = new JTextField[2];
		public AmplitudeBlock() {
			super("Amplitude Range", true);
			setDescription("Set a minimum and maximum click amplitude for this type");
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			p.add(new JLabel("Minimum ", JLabel.RIGHT));
			c.gridx++;
			p.add(ampRange[0] = new JTextField(4), c);
			c.gridx++;
			p.add(new JLabel(";   Maximum ", JLabel.RIGHT));
			c.gridx++;
			p.add(ampRange[1] = new JTextField(4), c);
			c.gridx++;
			p.add(new JLabel(" dB ", JLabel.LEFT));
			add(p);
		}

		@Override
		protected void setParams() {
			this.setEnableBox(sweepClassifierSet.testAmplitude);
			double[] range = sweepClassifierSet.amplitudeRange;
			if (range == null || range.length != 2) {
				range = new double[2];
				range[0] = 0;
				range[1] = 200;
			}
			for (int i = 0; i < 2; i++) {
				ampRange[i].setText(String.format("%3.1f", range[i]));
			}
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.testAmplitude = getEnableBox();
			if (getEnableBox()) {
				sweepClassifierSet.amplitudeRange = new double[2];
				for (int i = 0; i < 2; i++) {
					try {
						sweepClassifierSet.amplitudeRange[i] = Double.valueOf(ampRange[i].getText());
					}
					catch (NumberFormatException e) {
						return showWarning("Invalid amplitude range");
					}
				}
			}
			return true;
		}

		@Override
		protected void enableControls() {
			for (int i = 0; i < 2; i++) {
				ampRange[i].setEnabled(getEnableBox());
			}
			
		}
	}
	
	private class FilterBlock extends IdBlock {

		private JButton filterParams;
		private JLabel description;
		FilterBlock() {
			super("Pre Filtering", true);
			setDescription("Pre filtering before classificaiton can improve some species recognition");
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add(description = new JLabel("No filter", JLabel.CENTER), BorderLayout.CENTER);
			p.add(filterParams = new JButton("Filter Settings"), BorderLayout.EAST);
			filterParams.addActionListener(new FilterParams());
			add(BorderLayout.CENTER, p);
		}

		@Override
		protected void enableControls() {
			filterParams.setEnabled(getEnableBox());
			description.setEnabled(getEnableBox());
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.enableFFTFilter = getEnableBox();
			if (sweepClassifierSet.enableFFTFilter == false) return true;
			if (sweepClassifierSet.fftFilterParams == null) {
				return showWarning("FFT Filter Parameters must be set");
			}
			return true;
		}
		
		void setDescription() {
			if (sweepClassifierSet.fftFilterParams == null) {
				description.setText("No filter defined");
			}
			else {
				description.setText(sweepClassifierSet.fftFilterParams.toString());
			}
		}

		@Override
		protected void setParams() {
			setEnableBox(sweepClassifierSet.enableFFTFilter);
			setDescription();
		}
		
		class FilterParams implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FFTFilterParams newParams = FFTFilterDialog.showDialog(getOwner(), sweepClassifierSet.fftFilterParams);
				if (newParams != null) {
					sweepClassifierSet.fftFilterParams = newParams.clone();
					setDescription();
				}
			}			
		}
	}

	private abstract class IdBlock extends JPanel {
		private JCheckBox enableBox;
		private JLabel description;
		private JPanel tP;
		IdBlock(String borderTitle, Boolean enableButton) {
			if (borderTitle != null) {
				setBorder(new TitledBorder(borderTitle));
			}
			setLayout(new BorderLayout());
			tP = new JPanel();
			tP.setLayout(new BorderLayout());
			if (enableButton) {
				tP.add(BorderLayout.WEST, enableBox = new JCheckBox("Enable"));
				enableBox.addActionListener(new EnableBox());
				if (borderTitle != null) {
					enableBox.setToolTipText("Enable " + borderTitle + " measurements");
				}
			}
			tP.add(BorderLayout.CENTER, description = new JLabel("", JLabel.CENTER));
			add(BorderLayout.NORTH, tP);
			showTopStrip();
		}
		protected void setDescription(String desc) {
			description.setText(desc);
			showTopStrip();
		}
		private void showTopStrip() {
			tP.setVisible(enableBox != null && description.getText().length() > 0);
		}
		protected class EnableBox implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		}
		protected void setEnableBox(boolean enabled) {
			if (enableBox == null) {
				return;
			}
			enableBox.setSelected(enabled);
			enableControls();
		}
		protected boolean getEnableBox() {
			if (enableBox == null) {
				return false;
			}
			return enableBox.isSelected();
		}
		protected abstract void setParams();
		protected abstract boolean getParams();
		protected abstract void enableControls();
	}


	private class OptionsBlock extends IdBlock implements FFTLengthModeled, CodeHost {

		private JTextField name, code;
		JSpinner codeSpinner;
		CodeSpinnerModel codeSpinnerModel;
		private PamSymbolViewer symbolViewer;
		private JComboBox channelsBox;
		private JCheckBox restrictLength;
		private FFTLengthModel fftLengthModel;
		private JSpinner fftLengthSpinner;
		private JTextField fftLengthData;
		private JLabel lengthMS;
		
		/**
		 * Selects where the click is trimmed from. 
		 */
		private JComboBox<String> lengthTypeBox;

		OptionsBlock() {
			super("General Options", false);


			JPanel p = new JPanel(new GridBagLayout());


			JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p2.add(new JLabel("Name "));
			p2.add(name = new JTextField(20));
			codeSpinnerModel = new CodeSpinnerModel(sweepClassifier, this);
			codeSpinner = new JSpinner(codeSpinnerModel);
			code = new JTextField(3);
			codeSpinner.setEditor(code);
			p2.add(new JLabel(",  Unique Code "));
			p2.add(codeSpinner);
			symbolViewer = new PamSymbolViewer(getOwner(), "Symbol");
			p2.add(symbolViewer.getComponent());

			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 6;
			addComponent(p, p2, c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Channel options ", JLabel.RIGHT), c);
			c.gridx+=c.gridwidth;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.HORIZONTAL;
			addComponent(p, channelsBox = new JComboBox(), c);
			for (int i = 0; i < 3; i++) {
				channelsBox.addItem(SweepClassifierSet.getChannelOptionsName(i));
			}
			c.gridx = 0;
			c.gridy ++;
			c.gridwidth = 2;
			addComponent(p, restrictLength = 
				new JCheckBox("Restrict parameter extraction to "), c);
			restrictLength.addActionListener(new RestrictLength());
			fftLengthModel = new FFTLengthModel(this);
			fftLengthSpinner = new JSpinner(fftLengthModel);
			fftLengthData = new JTextField(4);
			fftLengthSpinner.setEditor(fftLengthData);
			c.gridx+=c.gridwidth;
			c.gridwidth = 1;
			addComponent(p, fftLengthSpinner, c);
			c.gridx++;
			c.gridwidth = 1;
			addComponent(p, new JLabel(" samples "), c);
			c.gridx++;
			addComponent(p, lengthMS = new JLabel(), c);
			c.gridx++;
			c.gridwidth = 1;
			
			addComponent(p, lengthTypeBox = new JComboBox<String>(), c);
			lengthTypeBox.addItem("around click center");
			lengthTypeBox.addItem("from start of click");

			add(BorderLayout.CENTER, p);
		}



		@Override
		protected void enableControls() {
			fftLengthData.setEditable(false);
			fftLengthSpinner.setEnabled(restrictLength.isSelected());			
			lengthTypeBox.setEnabled(restrictLength.isSelected());
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.setName(name.getText());
			sweepClassifierSet.symbol = symbolViewer.getSymbol();
			if (sweepClassifierSet.getName().length() <= 0) {
				return showWarning("You must enter a name for this type of click");
			}
			sweepClassifierSet.setSpeciesCode(getCode());
			if (sweepClassifier.codeDuplicated(sweepClassifierSet, classifierItemRow) ||
					sweepClassifierSet.getSpeciesCode() <= 0){
				return showWarning("You must enter a unique positive integer sepcies code");
			}
			if (sweepClassifierSet.symbol == null) {
				return showWarning("You must select a symbol");
			}
			sweepClassifierSet.channelChoices = channelsBox.getSelectedIndex();
			sweepClassifierSet.restrictLength = restrictLength.isSelected();
			
			sweepClassifierSet.restrictedBinstype = lengthTypeBox.getSelectedIndex(); 
			
			try {
				sweepClassifierSet.restrictedBins = Integer.valueOf(fftLengthData.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid Restricted length value");
			}
			return true;
		}

		@Override
		protected void setParams() {
			if (sweepClassifierSet == null) {
				symbolViewer.setSymbol(null);
				name.setText("");
				setCode(sweepClassifier.getNextFreeCode(0));
			}
			else {
				symbolViewer.setSymbol(sweepClassifierSet.symbol);
				name.setText(sweepClassifierSet.getName());
				setCode(sweepClassifierSet.getSpeciesCode());
			}

			if (sweepClassifierSet == null) {
				return;
			}
			
			lengthTypeBox.setSelectedIndex(sweepClassifierSet.restrictedBinstype);
			
			channelsBox.setSelectedIndex(sweepClassifierSet.channelChoices);
			restrictLength.setSelected(sweepClassifierSet.restrictLength);
			setFFTLength(sweepClassifierSet.restrictedBins);
		}

		class RestrictLength implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableControls();
			}
		}
		@Override
		public int getFFTLength() {
			int l = 2;
			try {
				l = Integer.valueOf(fftLengthData.getText());
			}
			catch(NumberFormatException e) {
				return 1;
			}
			return l;
		}

		@Override
		public void setFFTLength(int fftLength) {
			fftLengthData.setText(String.format("%d", fftLength));
			float sr = sweepClassifier.getClickDetector().getSampleRate();
			lengthMS.setText(String.format("(%.2f ms)", fftLength * 1000 / sr));
			pack();
		}



		@Override
		public int getCode() {
			if (code == null) {
				return 0;
			}
			try {
				return Integer.valueOf(code.getText());
			}
			catch (NumberFormatException e) {
				return 0;
			}
		}



		@Override
		public void setCode(int code) {
			this.code.setText(String.format("%d", code));
		}

	}

	private class LengthBlock extends IdBlock {

		private JTextField dB, smooth, minLength, maxLength;

		LengthBlock() {
			super("Click Length", true);
			setDescription("Click length is measured from the analytic wavform");
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(p, new JLabel("Smoothing "), c);
			c.gridx++;
			addComponent(p, smooth = new JTextField(3), c);
			c.gridx++;
			c.gridwidth = 3;
			addComponent(p, new JLabel(" bins (must be odd); "), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(p, new JLabel(" Threshold "), c);
			c.gridx++;
			addComponent(p, dB = new JTextField(3), c);
			c.gridx++;
			addComponent(p, new JLabel(" dB below maximum"), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(p, new JLabel("Length range "), c);
			c.gridx++;
			addComponent(p, minLength = new JTextField(5), c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, maxLength = new JTextField(5), c);
			c.gridx++;
			addComponent(p, new JLabel(" ms"), c);


			add(BorderLayout.CENTER, p);

		}

		@Override
		protected void enableControls() {
			minLength.setEnabled(getEnableBox());
			maxLength.setEnabled(getEnableBox());
			dB.setEnabled(getEnableBox());
			smooth.setEnabled(getEnableBox());
		}

		@Override
		protected boolean getParams() {
			sweepClassifierSet.enableLength = getEnableBox();
			try {
				sweepClassifierSet.lengthSmoothing = Integer.valueOf(smooth.getText());
				sweepClassifierSet.lengthdB = Double.valueOf(dB.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Click Length Parameters", 
				"The length smoothing or threshold parameter is invalid");
			}
			if (sweepClassifierSet.lengthSmoothing%2 == 0 || sweepClassifierSet.lengthSmoothing <= 0) {
				return showWarning("The number of length smoothing bins must be odd and positive");
			}
			if (sweepClassifierSet.lengthdB == 0) {
				return showWarning("The length threshold cannot be zero dB");
			}
			if (!sweepClassifierSet.enableLength) {
				return true;
			}
			try {
				sweepClassifierSet.minLength = Double.valueOf(minLength.getText());
				sweepClassifierSet.maxLength = Double.valueOf(maxLength.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Click Length Parameters", 
				"One or more click length parameters is invalid");
			}
			if (sweepClassifierSet.maxLength <= sweepClassifierSet.minLength) {
				return showWarning("Click Length Parameters", 
				"The maximum click length must be greater than the minimum");
			}
			return true;
		}

		@Override
		protected void setParams() {
			if (sweepClassifierSet == null) {
				return;
			}
			setEnableBox(sweepClassifierSet.enableLength);
			smooth.setText(String.format("%d", sweepClassifierSet.lengthSmoothing));
			dB.setText(String.format("%3.1f", sweepClassifierSet.lengthdB));
			minLength.setText(String.format("%3.2f", sweepClassifierSet.minLength));
			maxLength.setText(String.format("%3.2f", sweepClassifierSet.maxLength));
		}

	}

	private class EnergyBandsBlock extends IdBlock {

		private JTextField[] testEnergy = new JTextField[2];
		private JTextField[][] controlEnergy;
		private JTextField[] thresholds;
		EnergyBandsBlock() {
			super("Energy Bands", true);
			setDescription("Compare energy in diffrent frequency bands");
			controlEnergy = new JTextField[SweepClassifierSet.nControlBands][2];
			thresholds = new JTextField[SweepClassifierSet.nControlBands];
			JPanel p = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = 1;
			c.gridwidth = 3;
			addComponent(p, new JLabel("Frequency Range (Hz)", JLabel.CENTER), c);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("Test Band ", Label.RIGHT), c);
			c.gridx++;
			addComponent(p, testEnergy[0] = new JTextField(freqTextLength), c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, testEnergy[1] = new JTextField(freqTextLength), c);
			c.gridx++;
			c.gridwidth = 2;
			addComponent(p, new JLabel(" Threshold", JLabel.LEFT), c);
			c.gridwidth = 1;
			for (int i = 0; i < SweepClassifierSet.nControlBands; i++) {
				c.gridx = 0;
				c.gridy++;
				addComponent(p, new JLabel("Control Band ", Label.RIGHT), c);
				c.gridx++;
				addComponent(p, controlEnergy[i][0] = new JTextField(freqTextLength), c);
				c.gridx++;
				addComponent(p, new JLabel(" to "), c);
				c.gridx++;
				addComponent(p, controlEnergy[i][1] = new JTextField(freqTextLength), c);
				c.gridx++;
				addComponent(p, thresholds[i] = new JTextField(dbTextLength), c);
				c.gridx++;
				addComponent(p, new JLabel(" dB"), c);
			}

			add(BorderLayout.CENTER, p);
		}

		@Override
		protected void enableControls() {
			boolean e = getEnableBox();
			for (int i = 0; i < 2; i++) {
				testEnergy[i].setEnabled(e);
				for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
					controlEnergy[j][i].setEnabled(e);
				}
			}
			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
				thresholds[j].setEnabled(e);
			}
		}

		@Override
		protected boolean getParams() {
			if ((sweepClassifierSet.enableEnergyBands = getEnableBox()) == false) {
				return true;
			}
			if (sweepClassifierSet == null) {
				return false;
			}
			sweepClassifierSet.checkEnergyParamsAllocation();
			try {
				for (int i = 0; i < 2; i++) {
					sweepClassifierSet.testEnergyBand[i] = Double.valueOf(testEnergy[i].getText());
					for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
						sweepClassifierSet.controlEnergyBand[j][i] = Double.valueOf(controlEnergy[j][i].getText());
					}
				}
				for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
					sweepClassifierSet.energyThresholds[j] = Double.valueOf(thresholds[j].getText());
				}
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid energy band parameter");
			}
			if (sweepClassifierSet.testEnergyBand[1] <= sweepClassifierSet.testEnergyBand[0]) {
				return showWarning("Energh Bands", "The high test band frequency must be greater than the low test band frequency");
			}
			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
				if (sweepClassifierSet.controlEnergyBand[j][1] <= sweepClassifierSet.controlEnergyBand[j][0]) {
					return showWarning("Energh Bands", "The high control band frequency must be greater than the low control band frequency");
				}
			}
			return true;
		}

		@Override
		protected void setParams() {
			setEnableBox(sweepClassifierSet.enableEnergyBands);
			if (sweepClassifierSet == null) {
				return;
			}
			sweepClassifierSet.checkEnergyParamsAllocation();
			for (int i = 0; i < 2; i++) {
				testEnergy[i].setText(String.format("%3.1f", sweepClassifierSet.testEnergyBand[i]));
				for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
					controlEnergy[j][i].setText(String.format("%3.1f", sweepClassifierSet.controlEnergyBand[j][i]));
				}
			}
			for (int j = 0; j < SweepClassifierSet.nControlBands; j++) {
				thresholds[j].setText(String.format("%3.1f", sweepClassifierSet.energyThresholds[j]));
			}
		}

	}

	class PeakFreqBlock extends IdBlock {

		JTextField peakSearch[] = new JTextField[2];
		JTextField peakRange[] = new JTextField[2];
		JTextField peakWidth[] = new JTextField[2];
		JTextField peakMean[] = new JTextField[2];
		JCheckBox enablePeak, enableWidth, enableMean;
		JTextField peakSmoothing, peakThreshold;

		PeakFreqBlock() {
			super("Peak and Mean Frequency", false);
			setDescription("Peak and mean frequency extracted from click spectrum");
			JPanel p = new JPanel();

			// create everything here so can play with layout later. 
			for (int i = 0; i < 2; i++) {
				peakSearch[i] = new JTextField(freqTextLength);
				peakRange[i] = new JTextField(freqTextLength);
				peakWidth[i] = new JTextField(freqTextLength);
				peakMean[i] = new JTextField(freqTextLength);				
			}
			peakSmoothing = new JTextField(dbTextLength);
			peakThreshold = new JTextField(dbTextLength);
			String enableStr = "Enable";
			enablePeak = new JCheckBox(enableStr);
			enableWidth = new JCheckBox(enableStr);
			enableMean = new JCheckBox(enableStr);
			enablePeak.addActionListener(new EnableBox());
			enableWidth.addActionListener(new EnableBox());
			enableMean.addActionListener(new EnableBox());
			//			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = 1;
			c.gridwidth = 2;
			addComponent(p, new JLabel("Search and Integration range ", JLabel.RIGHT), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(p, peakSearch[0], c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, peakSearch[1], c);
			c.gridx++;
			addComponent(p, new JLabel(" Hz"), c);
			c.gridx++;
			addComponent(p, new JLabel("; Smoothing "), c);
			c.gridx++;
			addComponent(p, peakSmoothing, c);
			c.gridx++;
			addComponent(p, new JLabel(" bins"), c);

			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, enablePeak, c);
			c.gridx+=2;
			//			c.gridy++;
			//			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("  Peak frequency ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(p, peakRange[0], c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, peakRange[1], c);
			c.gridx++;
			addComponent(p, new JLabel(" Hz"), c);

			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, enableWidth, c);
			c.gridx+=2;
			//			c.gridy++;
			//			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("  Peak width ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(p, peakWidth[0], c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, peakWidth[1], c);
			c.gridx++;
			addComponent(p, new JLabel(" Hz"), c);
			c.gridx++;
			addComponent(p, new JLabel("; Threhsold"), c);
			c.gridx++;
			addComponent(p, peakThreshold, c);
			c.gridx++;
			addComponent(p, new JLabel(" dB "), c);


			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, enableMean, c);
			c.gridx+=2;
			//			c.gridy++;
			//			c.gridx = 0;
			c.gridwidth = 1;
			addComponent(p, new JLabel("  Mean frequency ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(p, peakMean[0], c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, peakMean[1], c);
			c.gridx++;
			addComponent(p, new JLabel(" Hz"), c);

			JPanel p2 = new JPanel(new BorderLayout());
			p2.add(BorderLayout.WEST, p);

			add(BorderLayout.CENTER, p2);
		}

		@Override
		protected void enableControls() {
			boolean p = enablePeak.isSelected();
			boolean w = enableWidth.isSelected();
			boolean m = enableMean.isSelected();
			for (int i = 0; i < 2; i++) {

				peakSearch[i].setEnabled(m ||p || w);
				peakRange[i].setEnabled(p);
				peakWidth[i].setEnabled(w);
				peakMean[i].setEnabled(m);
			}
			peakSmoothing.setEnabled(m ||p || w);
			peakThreshold.setEnabled(w);
		}

		@Override
		protected boolean getParams() {
			boolean p = enablePeak.isSelected();
			boolean w = enableWidth.isSelected();
			boolean m = enableMean.isSelected();
			sweepClassifierSet.enablePeak = p;
			sweepClassifierSet.enableWidth = w;
			sweepClassifierSet.enableMean = m;
			sweepClassifierSet.checkPeakFreqAllocation();
			if (!(m || p || w)) {
				return true;
			}
			if (getDoubleParams(sweepClassifierSet.peakSearchRange, peakSearch) == false) {
				return showWarning("Peak and Mean Frequency Measurement",
						"Search and integration parameter out of range");
			}
			try {
				sweepClassifierSet.peakSmoothing = Integer.valueOf(peakSmoothing.getText());
				if (sweepClassifierSet.peakSmoothing < 1 ||
						sweepClassifierSet.peakSmoothing % 2 == 0) {
					return showWarning("Peak and Mean Frequency Measurement",
					"The peak smoothing parameter must be positive and odd");
				}
			}
			catch (NumberFormatException e) {
				return showWarning("Peak and Mean Frequency Measurement",
				"The peak smoothing parameter must be positive and odd");
			}
			if (p) {
				if (getDoubleParams(sweepClassifierSet.peakRange, peakRange) == false) {
					return showWarning("Peak and Mean Frequency Measurement",
					"Peak Frequency parameter out of range");
				}
			}
			if (w) {
				if (getDoubleParams(sweepClassifierSet.peakWidthRange, peakWidth) == false) {
					return showWarning("Peak and Mean Frequency Measurement",
					"Peak Width parameter out of range");
				}
				try {
					sweepClassifierSet.peakWidthThreshold = Double.valueOf(peakThreshold.getText());
					if (sweepClassifierSet.peakWidthThreshold == 0) {
						return showWarning("Peak and Mean Frequency Measurement",
						"The Width threshold parameter cannot be zero");
					}
				}
				catch (NumberFormatException e) {
					return showWarning("Peak and Mean Frequency Measurement",
					"The Width threshold parameter cannot be zero");
				}
			}
			if (m) {
				if (getDoubleParams(sweepClassifierSet.meanRange, peakMean) == false) {
					return showWarning("Peak and Mean Frequency Measurement",
					"Mean Frequency parameter out of range");
				}
			}
			return true;
		}
		

		@Override
		protected void setParams() {
			enablePeak.setSelected(sweepClassifierSet.enablePeak);
			enableWidth.setSelected(sweepClassifierSet.enableWidth);
			enableMean.setSelected(sweepClassifierSet.enableMean);
			sweepClassifierSet.checkPeakFreqAllocation();
			peakSmoothing.setText(String.format("%d", sweepClassifierSet.peakSmoothing));
			peakThreshold.setText(String.format("%3.1f", sweepClassifierSet.peakWidthThreshold));
			for (int i = 0; i < 2; i++) {
				peakSearch[i].setText(String.format("%3.1f", sweepClassifierSet.peakSearchRange[i]));		
				peakRange[i].setText(String.format("%3.1f", sweepClassifierSet.peakRange[i]));		
				peakWidth[i].setText(String.format("%3.1f", sweepClassifierSet.peakWidthRange[i]));		
				peakMean[i].setText(String.format("%3.1f", sweepClassifierSet.meanRange[i]));				
			}
		}		
	}

	/**
	 * Get a pair of double parameters. 
	 * @param dest destination array (must already exist)
	 * @param source source array of text fields/ 
	 * @return true if OK and p1 < p1. 
	 */
	private boolean getDoubleParams(double[] dest, JTextField[] source) {
		try {
			for (int i = 0; i < 2; i++) {
				dest[i] = Double.valueOf(source[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
		return dest[1] > dest[0];
	}
	/**
	 * Get a pair of integer parameters. 
	 * @param dest destination array (must already exist)
	 * @param source source array of text fields/ 
	 * @return true if OK and p1 < p1. 
	 */
	private boolean getIntParams(int[] dest, JTextField[] source) {
		try {
			for (int i = 0; i < 2; i++) {
				dest[i] = Integer.valueOf(source[i].getText());
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
		return dest[1] > dest[0];
	}
	
	class ZeroXBlock extends IdBlock {

		JTextField[] nZC = new JTextField[2];
		JTextField[] zcRate = new JTextField[2];
		JCheckBox enableCount, enableSweep;
		ZeroXBlock() {
			super("Zero Crossings", false);
			setDescription("Parameters extracted from zero crossings");
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(p, enableCount = new JCheckBox("Enable"), c);
			c.gridx++;
			addComponent(p, new JLabel("     Number of zero crossings ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(p, nZC[0] = new JTextField(4), c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, nZC[1] = new JTextField(4), c);
			c.gridy++;
			c.gridx = 0;
			addComponent(p, enableSweep = new JCheckBox("Enable"), c);
			c.gridx++;
			addComponent(p, new JLabel("     Zero crossing frequency sweep ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(p, zcRate[0] = new JTextField(4), c);
			c.gridx++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx++;
			addComponent(p, zcRate[1] = new JTextField(4), c);
			c.gridx++;
			addComponent(p, new JLabel(" kHz/ms "), c);
			enableCount.addActionListener(new EnableBox());
			enableSweep.addActionListener(new EnableBox());

			add(BorderLayout.WEST, p);
		}

		@Override
		protected void enableControls() {
			// can only do sweep if you're doing count. 
			enableSweep.setEnabled(enableCount.isSelected());
			if (enableCount.isSelected() == false) {
				enableSweep.setSelected(false);
			}
			for (int i = 0; i < 2; i++) {
				nZC[i].setEnabled(enableCount.isSelected());
				zcRate[i].setEnabled(enableSweep.isSelected());
			}
		}

		@Override
		protected boolean getParams() {
			if ((sweepClassifierSet.enableZeroCrossings = enableCount.isSelected()) == true) {
				sweepClassifierSet.checkZCAllocation();
				if (getIntParams(sweepClassifierSet.nCrossings, nZC) == false) {
					return showWarning("Zero Crossings", "Invalid number of zero crossings");
				}
			}
			if ((sweepClassifierSet.enableSweep = enableSweep.isSelected()) == true) {
				if (getDoubleParams(sweepClassifierSet.zcSweep, zcRate) == false) {
					return showWarning("Zero Crossings", "Invalid zero crossing sweep parameter");
				}
			}
			return true;
		}

		@Override
		protected void setParams() {
			sweepClassifierSet.checkZCAllocation();
//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			enableCount.setSelected(sweepClassifierSet.enableZeroCrossings);
			enableSweep.setSelected(sweepClassifierSet.enableSweep);
			for (int i = 0; i < 2; i++) {
				nZC[i].setText(String.format("%d", sweepClassifierSet.nCrossings[i]));
				zcRate[i].setText(String.format("%3.1f", sweepClassifierSet.zcSweep[i]));
			}
		}

	}

//	class ZeroSweepBlock extends IdBlock {
//
//		JTextField[] nZC = new JTextField[2];
//		JTextField[] zcRate = new JTextField[2];
//		ZeroSweepBlock() {
//			super("Zero Sweep", true);
//			setDescription("Parameters extracted from zero sweep");
//			JPanel p = new JPanel();
//			p.setLayout(new GridBagLayout());
//			GridBagConstraints c = new PamGridBagContraints();
//			addComponent(p, new JLabel("Zero crossing frequency sweep ", JLabel.RIGHT), c);
//			c.gridx++;
//			addComponent(p, zcRate[0] = new JTextField(4), c);
//			c.gridx++;
//			addComponent(p, new JLabel(" to "), c);
//			c.gridx++;
//			addComponent(p, zcRate[1] = new JTextField(4), c);
//			c.gridx++;
//			addComponent(p, new JLabel(" kHz/ms "), c);
//
//
//			add(BorderLayout.CENTER, p);
//		}
//
//		@Override
//		protected void enableControls() {
//			boolean e = getEnableBox();
//			for (int i = 0; i < 2; i++) {
//				zcRate[i].setEnabled(e);
//			}
//		}
//
//		@Override
//		protected boolean getParams() {
//			if ((sweepClassifierSet.enableSweep = getEnableBox()) == false) {
//				return true;
//			}
//			if (sweepClassifierSet.enableZeroCrossings == false) {
//				return showWarning("Sweep", "Can't enable Sweep without enabling Zero Crossing");
//			}
//			
//			sweepClassifierSet.checkZCAllocation();
////			if (getIntParams(sweepClassifierSet.nCrossings, nZC) == false) {
////				return showWarning("Zero Crossings", "Invalid number of zero crossings");
////			}
//			if (getDoubleParams(sweepClassifierSet.zcSweep, zcRate) == false) {
//				return showWarning("Sweep", "Invalid zero crossing sweep parameter");
//			}
//			return true;
//		}
//
//		@Override
//		protected void setParams() {
//			sweepClassifierSet.checkZCAllocation();
//			setEnableBox(sweepClassifierSet.enableSweep);
//			for (int i = 0; i < 2; i++) {
////				nZC[i].setText(String.format("%d", sweepClassifierSet.nCrossings[i]));
//				zcRate[i].setText(String.format("%3.1f", sweepClassifierSet.zcSweep[i]));
//			}
//		}
//
//	}
	
	/**
	 * 
	 * Parameters for testing the cross correlation value. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class XCorrBlock extends IdBlock {

		/**
		 * The minimum correlation value. 
		 */
		private JTextField minCorrelation;
		
		private JTextField minPeakTorugh;

		private JCheckBox minXCorrEnable;

		private JCheckBox minPeakTroughEnable;

		private JTextField minPeakFactor;
		
		/**
		 * True if using multi-channel data
		 */
		boolean multiChan = false;


		XCorrBlock() {
			super("Cross Correlation", false);
			
			JPanel p = new JPanel();

			minXCorrEnable = new JCheckBox("Enable"); 
			minXCorrEnable.addActionListener((action)->{
				enableControls();
			});
			minCorrelation = new JTextField(5);
			
			minPeakTroughEnable = new JCheckBox("Enable"); 
			minPeakTroughEnable.addActionListener((action)->{
				enableControls();
			});
			minPeakFactor = new JTextField(5);
			
			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			
			c.gridx = 0;
			addComponent(p, minXCorrEnable, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel("	Min. xcorr value ", JLabel.RIGHT), c);
			c.gridx += c.gridwidth;
			c.gridwidth = 1;
			addComponent(p, minCorrelation, c);
			
			c.gridy++;
			c.gridx = 0;
			addComponent(p, minPeakTroughEnable, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel(" 	Max. xcorr value greater than ", JLabel.RIGHT), c);
			c.gridx += c.gridwidth;
			addComponent(p, minPeakFactor, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel(" 	* absolute min. value", JLabel.LEFT), c);

			add(BorderLayout.WEST, p);
			
			this.multiChan = checkMultiChan();
		}

		@Override
		protected void setParams() {
			sweepClassifierSet.checkXCCorrAllocation();
//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			minXCorrEnable.setSelected(sweepClassifierSet.enableMinXCrossCorr);
			minPeakTroughEnable.setSelected(sweepClassifierSet.enablePeakXCorr);
			
			this.minCorrelation.setText(String.format("%3.1f", sweepClassifierSet.minCorr));
			this.minPeakFactor.setText(String.format("%3.1f", sweepClassifierSet.corrFactor));
			
			this.multiChan = checkMultiChan();		
		}
	
		@Override
		protected boolean getParams() {
			sweepClassifierSet.enableMinXCrossCorr	 = minXCorrEnable.isSelected();
			sweepClassifierSet.enablePeakXCorr	 = minPeakTroughEnable.isSelected();

			if (minXCorrEnable.isSelected()) {
				try {
					sweepClassifierSet.minCorr = Double.valueOf(minCorrelation.getText());
				}
				catch (NumberFormatException e) {
					return showWarning("Invalid minimum correlation value");
				}
			}
			
			if (minPeakTroughEnable.isSelected()) {
				try {
					sweepClassifierSet.corrFactor = Double.valueOf(minPeakFactor.getText());
				}
				catch (NumberFormatException e) {
					return showWarning("Invalid minimum correlation factor value");
				}
			}
			return true;
		}

		@Override
		protected void enableControls() {
				this.multiChan = checkMultiChan();
			
				minXCorrEnable.setEnabled(multiChan);
				minCorrelation.setEnabled(multiChan);
				minPeakTroughEnable.setEnabled(multiChan);
				minPeakFactor.setEnabled(multiChan);
			
				if (!multiChan) return;
				
				minCorrelation.setEnabled(minXCorrEnable.isSelected());
				minPeakFactor.setEnabled(minPeakTroughEnable.isSelected());
		}
	}
	
	/**
	 * 
	 * Parameters for testing bearing values. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	private class BearingBlock extends IdBlock {

		/**
		 * The minimum correlation value. 
		 */
		private JTextField minBearing;
		
		/**
		 * The maximum bearing field
		 */
		private JTextField maxBearing;
		
		/**
		 * The enable bearings check box. 
		 */
		private JCheckBox enableBearings; 

		/**
		 * True if using multi-channel data
		 */
		boolean multiChan = false;

		/**
		 * Combo box to select whetrher bearings should be kept or excluded within limits. 
		 */
		private JComboBox<String> bearingsExcludeBox;


		BearingBlock() {
			super("Bearings", false);
			
			JPanel p = new JPanel();

			enableBearings = new JCheckBox("Enable"); 
			enableBearings.addActionListener((action)->{
				enableControls();
			});
			
			minBearing = new JTextField(5); 
			maxBearing = new JTextField(5); 
			
			bearingsExcludeBox = new JComboBox<String>(); 
			bearingsExcludeBox.addItem("Include only");
			bearingsExcludeBox.addItem("Exclude");
		
			p.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			
			c.gridx = 0;
			addComponent(p, enableBearings, c);
			c.gridx += c.gridwidth;
			addComponent(p, bearingsExcludeBox, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel("bearings between ", JLabel.RIGHT), c);
			c.gridx += c.gridwidth;
			addComponent(p, minBearing, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel(" and ", JLabel.RIGHT), c);
			c.gridx += c.gridwidth;
			addComponent(p, maxBearing, c);
			c.gridx += c.gridwidth;
			addComponent(p, new JLabel("(\u00B0)", JLabel.LEFT), c);

			add(BorderLayout.WEST, p);
			
			this.multiChan = checkMultiChan();

		}

		@Override
		protected void setParams() {
			sweepClassifierSet.checkBearingAllocation();
//			setEnableBox(sweepClassifierSet.enableZeroCrossings);
			enableBearings.setSelected(sweepClassifierSet.enableBearingLims);
			
			if (sweepClassifierSet.excludeBearingLims) bearingsExcludeBox.setSelectedIndex(1);
			else  bearingsExcludeBox.setSelectedIndex(0);
			
			this.minBearing.setText(String.format("%3.1f", Math.toDegrees(sweepClassifierSet.bearingLims[0])));
			this.maxBearing.setText(String.format("%3.1f", Math.toDegrees(sweepClassifierSet.bearingLims[1])));
			
			this.multiChan = checkMultiChan();
		}
		
	
		@Override
		protected boolean getParams() {
			if (enableBearings.isSelected()) {

				sweepClassifierSet.enableBearingLims	 = enableBearings.isSelected();
				sweepClassifierSet.excludeBearingLims	 = bearingsExcludeBox.getSelectedIndex()==1 ? true : false; 

				try {
					sweepClassifierSet.bearingLims[0] = Math.toRadians(Double.valueOf(minBearing.getText()));
				}
				catch (NumberFormatException e) {
					return showWarning("Invalid minimum correlation value");
				}
				
				try {
					sweepClassifierSet.bearingLims[1] = Math.toRadians(Double.valueOf(maxBearing.getText()));
				}
				catch (NumberFormatException e) {
					return showWarning("Invalid maximum bearing limits value");
				}
			}
			return true;
		}

		@Override
		protected void enableControls() {
				checkMultiChan();
			
				enableBearings.setEnabled(multiChan);
				bearingsExcludeBox.setEnabled(multiChan);
				minBearing.setEnabled(multiChan);
				maxBearing.setEnabled(multiChan);
			
				if (!multiChan) return;
				
				bearingsExcludeBox.setEnabled(enableBearings.isSelected());
				minBearing.setEnabled(enableBearings.isSelected());
				maxBearing.setEnabled(enableBearings.isSelected());
		}
	}

	
	/**
	 * Check whether the click classifier is set up for multi-channel and set 
	 * the dialog components uprightly
	 */
	private boolean checkMultiChan() {
		boolean multiChan = false; 
		//do we have multi-channel clicks?
		if  (clickControl!=null) {
			int[] chanGroups = clickControl.getClickParameters().getGroupedSourceParameters().getChannelGroups();
			multiChan = false;
			for (int i=0; i<chanGroups.length; i++) {
				int chans = clickControl.getClickParameters().getGroupedSourceParameters().getGroupChannels(i);
//				Debug.out.println("Check multi-channel: " + chanGroups[i] + "  num: " + PamUtils.getNumChannels(chans));
				if (PamUtils.getNumChannels(chans)>1) {
					multiChan = true;
					break; 
				}
			}
		}
		else multiChan = true;
//		Debug.out.println("Check multi-channel: " + multiChan);
		
		return multiChan; 
	}


    /**
     * Inner class to display the alarm panel
     */
    class AlarmBlock extends IdBlock {
        String[] alarmList;

		AlarmBlock() {
			super("Select Alarm", false);
			setDescription("Select alarm to be used with this detection");
			// GridBagLayout gb;
			setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();

			gc.gridx = 0;
			gc.gridy = 0;
            gc.insets = new Insets(5, 20, 0, 0);
			gc.anchor = GridBagConstraints.NORTHWEST;
            alarmChooser = new JComboBox();
            add(alarmChooser,gc);
            gc.gridy++;
			add(new JLabel("Max amount of time between detections "), gc);
			gc.gridx++;
            gc.insets.left = 0;
			add(maxTime = new JTextField(6), gc);
			gc.gridx++;
			add(new JLabel(" ms"), gc);
            gc.gridx++;
            gc.weightx = 1.0;
            add(new JLabel("   "), gc);
            gc.gridx = 0;
            gc.insets.left = 20;
            gc.insets.top = 20;
            gc.gridy++;
            gc.gridwidth=4;
            gc.fill = GridBagConstraints.HORIZONTAL;
			add(new JLabel("Note: alarm is enabled/disabled on previous screen"), gc);
}

		@Override
		protected void setParams() {
            alarmChooser.setSelectedItem(sweepClassifierSet.getAlarm());
            maxTime.setText(String.format("%d", sweepClassifierSet.getMaxTime()));
        }

		@Override
		protected boolean getParams() {
            try {
                sweepClassifierSet.setAlarm((ClickAlarm) alarmChooser.getSelectedItem());
                sweepClassifierSet.setMaxTime(Long.parseLong(maxTime.getText()));
            } catch (Exception ex) {
                return false;
            }
            return true;
        }

		@Override
		protected void enableControls(){}
    }

	@Override
	public SweepClassifierSet getExportObject() {
		if (getParams() == false) {
			return null;
		}
		return sweepClassifierSet;
	}

	@Override
	public void setImportObject(Serializable importObject) {
		sweepClassifierSet = (SweepClassifierSet) importObject;
		setParams();
	}

	@Override
	public int getExportTypes() {
		return ImportExportUser.EXPORT_SERIALIZED;
//		| ImportExportUser.EXPORT_XML;
	}

	@Override
	public Class getIOClass() {
		return SweepClassifierSet.class;
	}

	@Override
	public PamSettings getSettingsWrapper() {
		return clickControl;
	}

}
