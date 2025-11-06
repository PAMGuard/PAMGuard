package clipgenerator.clipDisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.PamScrollObserver;
import pamScrollSystem.ScrollPaneAddon;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettings;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamView.ColourArray;
import PamView.PamSlider;
import PamView.ColourArray.ColourArrayType;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorder;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamProcess;
import clipgenerator.ClipControl;

/**
 * Panel to go in the top of a clipDisplayPanel which has all the controls needed
 * to set contrast, colour, etc. 
 * @author Doug Gillespie
 *
 */
public class DisplayControlPanel {

	private ClipDisplayPanel clipDisplayPanel;
	private JPanel controlPanel2;
	private JPanel controlPanel;
	private SliderSubPanel minAmplitude, amplitudeRange, logFFT;
	private SliderSubPanel vScale, hScale;
	private JLabel fftResolution;
	private JComboBox colourList;
	private FreqMaxSlider freqMax;
	private JCheckBox showTrigger;
	private ColourPanel colourPanel;
	private SliderSubPanel maxClips;
	private SliderSubPanel maxMinutes;
	private JLabel viewerStart, viewerEnd;
	private ClipDisplayParent clipDisplayParent;
	private JCheckBox newClipsLast;
	private ScrollPaneAddon scrollButtons;
	public DisplayControlPanel(ClipDisplayParent clipDisplayParent,
			ClipDisplayPanel clipDisplayPanel) {
		super();
		this.clipDisplayParent = clipDisplayParent;
		this.clipDisplayPanel = clipDisplayPanel;
		
		controlPanel2 = new PamPanel(new BorderLayout());
		
		FlowLayout fl;
//		controlPanel = new PamPanel(fl = new FlowLayout(FlowLayout.LEFT));
		controlPanel = new PamPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		
		controlPanel2.add(BorderLayout.WEST, controlPanel);
		
		PamPanel ampPanel = new PamPanel();
		ampPanel.setBorder(new TitledBorder("FFT"));
//		ampPanel.setLayout(new BoxLayout(ampPanel, BoxLayout.Y_AXIS));
		ampPanel.setLayout(new GridLayout(2,2));
		ampPanel.add(minAmplitude = new SliderSubPanel("Min Amplitude", 0, 100, 10, "dB", 150));
		ampPanel.add(logFFT = new FFTSlider("FFT Length", 4, 15, 1));
		ampPanel.add(amplitudeRange = new SliderSubPanel("Amplitude Range", 30, 100, 10, "dB", 1));
		PamPanel resPanel = new PamPanel();
		JPanel resOutPanel = new PamPanel(new BorderLayout());
		resPanel.setLayout(new BorderLayout());
		resPanel.add(BorderLayout.NORTH, fftResolution = new PamLabel(" ", PamLabel.CENTER));
		fftResolution.setToolTipText("WARNING: This may vary if some data units have decimated data displayed");

		resPanel.add(BorderLayout.CENTER, showTrigger = new PamCheckBox("Overlay trigger data"));
		showTrigger.setToolTipText("Select to overlay graphical information from the detection triggering this clip (e.g. a whistle contour)");
//		resPanel.add(BorderLayout.CENTER, colourList = new JComboBox());
//		ColourArrayType[] types = ColourArray.ColourArrayType.values();
//		for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
//			colourList.addItem(ColourArray.getName(types[i]));
//		}
//		colourList.addActionListener(detectChanges);
		resOutPanel.add(BorderLayout.NORTH, resPanel);
		ampPanel.add(resOutPanel);
		
		minAmplitude.addChangeListener(new DetectChanges(true));
		logFFT.addChangeListener(new DetectChanges(true));
		amplitudeRange.addChangeListener(new DetectChanges(true));
		controlPanel.add(ampPanel);
		
		PamPanel scalePanel = new PamPanel();
		scalePanel.setBorder(new TitledBorder("Scale"));
		scalePanel.setLayout(new GridLayout(2, 2));
		scalePanel.add(hScale = new ScaleSlider("Horizontal", -2, 3, 1, "x", 1)); // set to default 1 and it will grow to match 
		// sice of drop down list for colour maps. 
		scalePanel.add(freqMax = new FreqMaxSlider("Max Frequency", 0, 1.0, 0.01, "Hz", 1));
		scalePanel.add(vScale = new ScaleSlider("Vertical", -2, 3, 1, "x", 1));
//		scalePanel.add(showTrigger = new PamCheckBox("Show trigger data"));
		PamPanel colPanel = new PamPanel(new BorderLayout());
		colPanel.add(BorderLayout.NORTH,  colourList = new JComboBox());
		ColourArrayType[] types = ColourArray.ColourArrayType.values();
		for (int i = 0; i < ColourArray.ColourArrayType.values().length; i++) {
			colourList.addItem(ColourArray.getName(types[i]));
		}
		colourList.addActionListener(new DetectChanges(true));
		colPanel.add(BorderLayout.CENTER, colourPanel = new ColourPanel());
		colourPanel.setBorder(PamBorder.createInnerBorder());
		scalePanel.add(colPanel);
		
		hScale.addChangeListener(new DetectChanges(true));
		vScale.addChangeListener(new DetectChanges(true));
		freqMax.addChangeListener(new DetectChanges(false));
		showTrigger.addActionListener(new DetectChanges(false));
		controlPanel.add(scalePanel);
		
		PamPanel sortPanel = new PamPanel(); 
		//Presently clips are sorted in the order in which they were created.
		//TODO: Add interface to allow for sorting by manual selection time or clip start time.
		sortPanel.setBorder(new TitledBorder("Sorting"));
		sortPanel.add(BorderLayout.CENTER, newClipsLast = new PamCheckBox("New Clips Last"));
		newClipsLast.setToolTipText("When checked, newly created clips will be placed at the bottom of the queue. Otherwise they will be placed at the top.");
		newClipsLast.addActionListener(new DetectChanges(false));
		sortPanel.setVisible(false);
		
		PamPanel historyPanel = new PamPanel();
		historyPanel.setBorder(new TitledBorder("History"));
		if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
			historyPanel.setLayout(new GridLayout(2,1));
			historyPanel.add(maxClips = new SliderSubPanel("Max Clips", 10, 1000, 10, "", 120));
			historyPanel.add(maxMinutes = new SliderSubPanel("Max Minutes", 1, 120, 1, "", 120));
			maxClips.addChangeListener(new DetectChanges(false));
			maxMinutes.addChangeListener(new DetectChanges(false));
			
			sortPanel.setVisible(true);
			
			setValues(clipDisplayPanel.clipDisplayParameters);
		}
		else {
			GridBagConstraints c = new PamGridBagContraints();
			historyPanel.setLayout(new GridBagLayout());
			
			scrollButtons = new ScrollPaneAddon(clipDisplayPanel.getScrollPane(), clipDisplayParent.getDisplayName(),
					AbstractPamScrollerAWT.HORIZONTAL, 1000, 3600*1000, true);
			scrollButtons.addDataBlock(clipDisplayParent.getClipDataBlock());
			c.gridwidth = 1;
			c.gridx = 1;
			c.fill = GridBagConstraints.NONE;
			PamDialog.addComponent(historyPanel, scrollButtons.getButtonPanel(), c);
			c.gridx = 0;
			c.gridy++;
			PamDialog.addComponent(historyPanel, new PamLabel("Start: ", JLabel.RIGHT), c);
			c.gridx++;
			PamDialog.addComponent(historyPanel, viewerStart = new PamLabel("1970-01-01 12:00:00"), c);
			c.gridx = 0;
			c.gridy++;
			PamDialog.addComponent(historyPanel, new PamLabel("End: ", JLabel.RIGHT), c);
			c.gridx++;
			PamDialog.addComponent(historyPanel, viewerEnd = new PamLabel("1970-01-01 12:00:00"), c);
			
//			historyPanel.add(sco.getButtonPanel());
//			historyPanel.add(new PamLabel("Start", JLabel.LEFT));
//			historyPanel.add(viewerStart = new PamLabel("1970-01-01 12:00:00"));
//			historyPanel.add(new PamLabel("End"));
//			historyPanel.add(viewerEnd = new PamLabel("1970-01-01 12:00:00"));
			scrollButtons.addObserver(new ScrollObserver());
			
		}
		controlPanel.add(historyPanel);
		controlPanel.add(sortPanel);
	}
	
	
	
	private class DetectChanges implements ChangeListener, ActionListener {

		private boolean needNewImage;
		
		public DetectChanges(boolean needNewImage) {
			super();
			this.needNewImage = needNewImage;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			detectChanges(needNewImage);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			detectChanges(needNewImage);
		}
		
	}
	
	void detectChanges(boolean needNewImage) {
		clipDisplayPanel.displayControlChanged(needNewImage);
		clipDisplayParent.displaySettingChange();
		colourPanel.repaint();
	}

	boolean settingValues;
	void setValues(ClipDisplayParameters clipParams) {
		settingValues = true;
		minAmplitude.setValue(clipParams.amlitudeMinVal);
		logFFT.setValue(clipParams.getLogFFTLength());
		amplitudeRange.setValue(clipParams.amplitudeRangeVal);
		hScale.setValue(clipParams.imageHScale);
		vScale.setValue(clipParams.imageVScale);
		colourList.setSelectedIndex(clipParams.getColourMap().ordinal());
		freqMax.setValue(clipParams.frequencyScale);
		showTrigger.setSelected(clipParams.showTriggerOverlay);
		if (maxClips != null) {
			maxClips.setValue(clipParams.maxClips);
		}
		if (maxMinutes != null) {
			maxMinutes.setValue(clipParams.maxMinutes);
		}

		sayFFTResolution();
		freqMax.showValue();
		if (newClipsLast != null) {
			newClipsLast.setSelected(clipParams.getNewClipOrder());
		}
		settingValues = false;
		
	}
	
	void sayFFTResolution() {
		int fftLen = 1<<(int) (logFFT.getValue()+.001);
		double sr = clipDisplayPanel.getSampleRate();
		if (sr <= 0.) {
			fftResolution.setText(" ");
			return;
		}
		double dt = fftLen/sr;
		double df = sr/fftLen;
		String str = String.format("\u0394t=%3.1f ms, \u0394f=%s", dt*1000., FrequencyFormat.formatFrequency(df, true));
		fftResolution.setText(str);
	}
	
	boolean getValues(ClipDisplayParameters clipParams) {
		if (settingValues) {
			return false;
		}
		clipParams.amlitudeMinVal = minAmplitude.getValue();
		clipParams.setLogFFTLength((int) (logFFT.getValue() + 0.01));
		clipParams.amplitudeRangeVal = amplitudeRange.getValue();
		clipParams.imageHScale = hScale.getValue();
		clipParams.imageVScale = vScale.getValue();
		clipParams.setColourMap(ColourArrayType.values()[colourList.getSelectedIndex()]);
		clipParams.frequencyScale = freqMax.getValue();
		clipParams.showTriggerOverlay = showTrigger.isSelected();
		if (maxClips != null) {
			clipParams.maxClips = (int) maxClips.getValue();
		}
		if (maxMinutes != null) {
			clipParams.maxMinutes = (int) maxMinutes.getValue();
		}
		if (newClipsLast != null) {
			clipParams.setNewClipOrder(newClipsLast.isSelected());
		}
		return true;
	}
	
	/**
	 * @return the controlPanel
	 */
	public JPanel getControlPanel() {
		return controlPanel2;
	}
	
	class ScrollObserver implements PamScrollObserver {

		@Override
		public void scrollRangeChanged(AbstractPamScroller pamScroller) {
			long start = pamScroller.getMinimumMillis();
			long end = pamScroller.getMaximumMillis();
			if (viewerStart != null) {
				viewerStart.setText(PamCalendar.formatDBDateTime(start));
				viewerEnd.setText(PamCalendar.formatDBDateTime(end));
			}
			clipDisplayPanel.newViewerTimes(start, end);
		}

		@Override
		public void scrollValueChanged(AbstractPamScroller pamScroller) {
			// TODO Auto-generated method stub
			
		}

	}
	
	/**
	 * Standard panel with a slider and some text for use 
	 * in the contorl panel
	 * @author doug
	 *
	 */
	class SliderSubPanel extends PamPanel implements ChangeListener {
		
		protected JSlider slider;
		protected JLabel currValue;
		protected String unitString;
		protected String title;
		protected double minVal, maxVal, stepVal;
		protected double stepScale;
		SliderSubPanel(String title, double minVal, double maxVal, double stepVal, String units, int width) {
			unitString = units;
			this.title = title;
			this.minVal = minVal;
			this.maxVal = maxVal;
			this.stepVal = stepVal;
			this.setLayout(new BorderLayout());

			add(BorderLayout.CENTER, slider = new PamSlider());
			slider.setMinimum(getSliderMinimum());
			slider.setMaximum(getSliderMaximum());
			setValue(getSliderMinimum());
			if (width > 0) {
				Dimension d = slider.getPreferredSize();
				d.width = width;
				slider.setPreferredSize(d);
			}
			
			JPanel topPanel = new PamPanel(new BorderLayout());
			Insets sInsets = slider.getInsets();
			Border sborder = slider.getBorder();
//			topPanel.setBorder(new )
			if (title != null) {
				topPanel.add(BorderLayout.CENTER, currValue = new PamLabel(title, JLabel.CENTER));
			}
			add(BorderLayout.NORTH, topPanel);
			
			slider.addChangeListener(this);
			showValue();
		}
		
		protected void addChangeListener(ChangeListener changeListener) {
			slider.addChangeListener(changeListener);
		}

		void setValue(double value) {
			slider.setValue((int) ((value-minVal)/stepVal));
		}
		
		int getSliderMaximum() {
			return (int) ((maxVal-minVal)/stepVal);
		}
		
		int getSliderMinimum() {
			return 0;
		}
		
		double getValue() {
			return slider.getValue() * stepVal + minVal;
		}
		protected void showValue() {
			String str = title + "    ";
			if (unitString != null) {
				currValue.setText(str + String.format("%.2f %s", getValue(), unitString));
			}
			else {
				currValue.setText(str + String.format("%.2f", getValue()));
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			showValue();
			clipDisplayParent.displaySettingChange();
		}
		
		
	}
	
	class ScaleSlider extends SliderSubPanel {

		ScaleSlider(String title, double minVal, double maxVal, double stepVal,
				String units, int width) {
			super(title, minVal, maxVal, stepVal, units, width);
		}

		@Override
		double getValue() {
			return Math.pow(2., super.getValue());
		}

		@Override
		void setValue(double value) {
//			need to take the log2 of the value
			super.setValue((int) Math.round(Math.log(value) / Math.log(2.)));
		}
		
	}
	
	class ColourPanel extends PamPanel {

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			int nRows = this.getWidth();
			ColourArray colArray = clipDisplayPanel.getColourArray();
			if (colArray == null) {
				return;
			}
			int nCols = colArray.getNumbColours();
			double scale = (double) nCols / (double) nRows;
			int y = getHeight();
			for (int i = 0; i < nRows; i++) {
				int iCol = (int) Math.round(scale * i);
				iCol = Math.max(0, Math.min(iCol, nCols));
				g.setColor(colArray.getColour(iCol));
				g.drawLine(i, 0, i, y);
			}
		}
		
	}
	class FFTSlider extends SliderSubPanel {

		FFTSlider(String title, double minVal, double maxVal, int width) {
			super(title, minVal, maxVal, 1, "pt", width);
		}
		
		@Override
		protected void showValue() {
			String str = title + "    ";
			int fftLen = 1<<((int) (getValue() + 0.01));
			double sr = Math.max(1, clipDisplayPanel.getSampleRate());
			double dt = fftLen/sr;
			double df = sr/fftLen;
			str = String.format("FFT Length %d pt", fftLen);
			currValue.setText(str);
		}

		/* (non-Javadoc)
		 * @see clipgenerator.clipDisplay.DisplayControlPanel.SliderSubPanel#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			super.stateChanged(e);
			sayFFTResolution();
		}
	}
	
	class FreqMaxSlider extends SliderSubPanel {

		FreqMaxSlider(String title, double minVal, double maxVal,
				double stepVal, String units, int width) {
			super(title, minVal, maxVal, stepVal, units, width);
		}

		/* (non-Javadoc)
		 * @see clipgenerator.clipDisplay.DisplayControlPanel.SliderSubPanel#showValue()
		 */
		@Override
		protected void showValue() {
//			double f = super.getValue() * clipDisplayPanel.getSampleRate() / 2.;
//			currValue.setText(FrequencyFormat.formatFrequency(f, true));
			currValue.setText(String.format("Max Niquist X%3.2f", super.getValue()));
		}
		
	}

	/**
	 * @return the scrollButtons
	 */
	public ScrollPaneAddon getScrollButtons() {
		return scrollButtons;
	}


}
