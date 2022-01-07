package Spectrogram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fftManager.FFTDataBlock;
import Layout.PamAxis;
import Layout.PamAxisPanel;
import PamController.PamController;
import PamController.soundMedium.GlobalMedium;
import PamView.ColourArray;
import PamView.ColourArray.ColourArrayType;
import PamView.PamColors.PamColor;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.ColourComboBox;
import PamView.PamColors;
import PamView.hidingpanel.HidingDialog;
import PamView.hidingpanel.HidingDialogChangeListener;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.hidingpanel.HidingPanel;
import PamView.panel.PamPanel;
import PamView.sliders.PamRangeSlider;



/**
 * Side panel for the spectrogram which allows users to quickly change spectrogram settings
 * @author Jamie Macaulay
 *
 */
public class SpectrogramHidingPanel extends HidingDialogComponent {
	
//	private HidingPanel hidingPanel;
	private ControlPanel panel;
	private SpectrogramParametersUser spectrogramDsiplay;
	private static int convertTokHz=2000;
	
	private ImageIcon tabIcon=new ImageIcon(ClassLoader
			.getSystemResource("Resources/tdSpectrogramIcon.png"));
	
	public SpectrogramHidingPanel(SpectrogramParametersUser spectrogramDisplay) {		
		this.spectrogramDsiplay=spectrogramDisplay;
		panel=new ControlPanel();
		setParams(spectrogramDisplay.getSpectrogramParameters());
		HidingDialog.invalidateEverything(panel);
	}

	public void changeAmplitudeParams(SpectrogramParameters spectrogramParams, Long millis) {

		//change amplitude scales
		double[] amplitudeLimits=new double[2];
		amplitudeLimits[0]=getAmplitudeLower();
		amplitudeLimits[1]=getAmplitudeUpper();
		spectrogramParams.amplitudeLimits=amplitudeLimits;
		
		spectrogramDsiplay.setSpectrogramParameters(spectrogramParams);
//		if (millis ==null )spectrogramDsiplay.setAmplitudeParams();
//		else spectrogramDsiplay.setAmplitudeParams(millis);
	}
	
	
	/**
	 * Changes the spectrogram params and recalcs spectrogram data if necessary. 
	 * @param spectrogramParams
	 */
	public void changeFrequencyParams(SpectrogramParameters spectrogramParams){
	
		//change frequency scales
		//frequency slider bar:
		if (spectrogramDsiplay.getFFTDataBlock() != null){
			double[] frequencyLimits=new double[2];
			frequencyLimits[0]=getLowerFrequency();
			frequencyLimits[1]=getUpperFrequency();
			/*
			 * check != 0 which can happen if the input FFT block has not
			 * yet been configured (depends on loading order of modules). 
			 */
			if (frequencyLimits[1] != 0) {
				spectrogramParams.frequencyLimits=frequencyLimits;
			}
		}
		
		//change colour box
//		spectrogramParams.setColourMap(colourComboBox.getSelectedColourMap());
	
		spectrogramDsiplay.setSpectrogramParameters(spectrogramParams);
	}
	
	public void setParams(SpectrogramParameters spectrogramParams){
		
		//colour comboBox
		colourComboBox.setSelectedColourMap(spectrogramParams.getColourMap());
		
//		System.out.println("Spectrogram Hiding Display: fftdatablock "+spectrogramDsiplay.getFFTDataBlock()+" minFreq: "+spectrogramParams.frequencyLimits[0]+" maxFreq: "+spectrogramParams.frequencyLimits[1]);
		//frequency slider bar:
		if (spectrogramDsiplay.getFFTDataBlock()!=null){
			double maxFreq=getAbsMaxFrequency();
			double minFreq=getAbsMinFrequency();
			setFrequencyAbsoluteRange(minFreq,maxFreq);
			setFrequencySliderRange(spectrogramParams.frequencyLimits[0],spectrogramParams.frequencyLimits[1],minFreq,maxFreq);
			//set the colour of the frequency slider
			getFrequencyRangeSlider().setRangeColour(getMidColour(spectrogramParams.getColourMap()));
		}
//		System.out.println("SliderLimits setParams: amplitudeLimits[0]"+ spectrogramParams.amplitudeLimits[0]+" amplitudeLimits[1]: "+spectrogramParams.amplitudeLimits[1]);
		//amplitudeBar
		//set the colour map. 
		getAmplRangeSlider().setColourMap(spectrogramParams.getColourMap());
		double[] ampLimits=spectrogramParams.amplitudeLimits;
		//unlike the frequency, we need to check that the amplitude range is correct. 
		//info is stored in the axis class
		double minValAmp=getAbsMinAmplitude();
		double maxValAmp=getAbsMaxAmplitude();
		//check if we need to change the slider range
		if (ampLimits[1]>maxValAmp || ampLimits[0]<minValAmp){
			setAmplitudeAbsoluteRange(ampLimits[0]-10,  ampLimits[1]+10);
		}
		//set the sliders to the correct positions
//		System.out.println(ampLimits[0] + " "+ ampLimits[1]+" "+amplitudePanel.getAmplitudeAxis().getMinVal()+" "+amplitudePanel.getAmplitudeAxis().getMaxVal());
		setAmplitudeSliderRange( ampLimits[0],   ampLimits[1],  getAbsMinAmplitude(), getAbsMaxAmplitude());


	}
	
	/**
	 * Returns the mid or average colour of the colourmap to colour the frequency slider. 
	 * @param colourArrayType
	 * @return
	 */
	private Color getMidColour(ColourArrayType colourArrayType){
		ColourArray colourArray = ColourArray.createStandardColourArray(256, colourArrayType);
		Color colour=colourArray.getColours()[colourArray.getColours().length/2];
		return brighten(colour,40);

	}
	
	private Color inverColor(Color colour){
		Color invertColor = new Color(255-colour.getRed(),
                255-colour.getGreen(),
                255-colour.getBlue());
		return invertColor;
	}
	
	public static Color brighten(Color color, int d) {
        while(d > 0) {
            color = color.brighter();
            d = d-1;
        }
        return color; 
    }
	
	
	private ColourComboBox colourComboBox;
	private FrequencyPanel frequencyPanel;
	private AmplitudePanel amplitudePanel; 
	
	private class ControlPanel extends PamPanel{
		
		private Color backCol=PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA);
		private Color foreCol=Color.WHITE;
		private ColourRangeSlider colourSlider;


		public ControlPanel(){
			super();
			
			int spacingx=20;
			int spacingy=0;
			this.setLayout(new BorderLayout());
			JPanel mainPanel = new JPanel(new BorderLayout());
			JPanel centPanel = new JPanel();
			mainPanel.setOpaque(false);
			centPanel.setOpaque(false);
			
//			centPanel.setLayout(new GridBagLayout());
//			GridBagConstraints c=new PamGridBagContraints();
			centPanel.setLayout(new BorderLayout());
//			c.fill = GridBagConstraints.VERTICAL;
//			c.insets=new Insets(spacingy,10,spacingy,spacingx);
//			c.weighty=1;
			frequencyPanel=new FrequencyPanel(0,250);
			amplitudePanel=new AmplitudePanel(30,180);
			frequencyPanel.setAutoInsets(false);
			amplitudePanel.setAutoInsets(false);

			//add frequency slider bar. 
//			c.gridx=0;
//			c.gridy = 1;
//			PamDialog.addComponent(centPanel, frequencyPanel=new FrequencyPanel(0,250), c);
			centPanel.add(BorderLayout.WEST, frequencyPanel);
			
			//add amplitude colour slider. 
//			c.gridx=1;
//			c.gridy = 1;
//			c.gridheight=5;
//			c.insets=new Insets(spacingy,0,spacingy,10);
//			PamDialog.addComponent(this, colourSlider=new ColourRangeSlider(0,100), c);
//			PamDialog.addComponent(centPanel, amplitudePanel=new AmplitudePanel(30,180), c);
			centPanel.add(BorderLayout.EAST, amplitudePanel);
			
//			c.insets=new Insets(0,0,0,10);

			//add frequency slider bar. 
//			c.gridx=0;
//			c.gridy = 0;
//			c.gridwidth = 2;
//			c.gridheight=1;
//			c.insets=new Insets(0,0,0,0);
			colourComboBox=new ColourComboBox(130,20);
			mainPanel.add(BorderLayout.NORTH, colourComboBox);
			mainPanel.add(BorderLayout.CENTER, centPanel);
//			PamDialog.addComponent(this,colourComboBox , c);
			colourComboBox.addActionListener(new ChangeColourComboBox());
			colourComboBox.setOpaque(false);

			this.add(BorderLayout.CENTER, mainPanel);
			this.setOpaque(false);
			this.setBackground(backCol);

			revalidate();
		}


		/* (non-Javadoc)
		 * @see javax.swing.JComponent#getPreferredSize()
		 */
		@Override
		public Dimension getPreferredSize() {
			return super.getPreferredSize();
		}





		
//		@Override
//		public void paintComponent(Graphics g) {
//			super.paintComponent(g);
//			g.setColor(backCol);
//			Rectangle r = g.getClipBounds();
//			g.fillRect(r.x, r.y, r.width, r.height);
//		}

	}
	
	private class ChangeColourComboBox implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			SpectrogramParameters specParams = spectrogramDsiplay.getSpectrogramParameters();
			specParams.setColourMap(colourComboBox.getSelectedColourMap());
			getFrequencyRangeSlider().setRangeColour(getMidColour(specParams.getColourMap()));
			getAmplRangeSlider().setColourMap(specParams.getColourMap());
			spectrogramDsiplay.setSpectrogramParameters(specParams);
			panel.repaint();
//			spectrogramDsiplay.createColours();
//			spectrogramDsiplay.setAmplitudeParams(0);
		}
	}
	
	
	private class AmplitudePanel extends PamAxisPanel {
		
		private ColourRangeSlider colourRangeSlider;
//		private PamAxis amplitudeLabel;
		private PamAxis amplitudeAxis;
		private static final int sliderRange=100;


		AmplitudePanel(int min, int max) {
			super();
			setLayout(new BorderLayout());
			add(BorderLayout.CENTER,colourRangeSlider= new ColourRangeSlider(0,sliderRange));
			colourRangeSlider.addChangeListener(new AmplitudeRangeListener());
			colourRangeSlider.setTrackDragging(true);
			colourRangeSlider.setOpaque(false);
			colourRangeSlider.setThumbSizes(15);
//			String dBRef = GlobalMedium.getdBRefString(PamController.getInstance().getGlobalMediumManager().getCurrentMedium());
			String dBRef = "dB re \u00B5Pa/\u221AHz";
			amplitudeAxis = new PamAxis(0, 200, 0, 0,
					min,
					max, true, "Amplitude (" + dBRef + ")", "%3.0f");
			
//			amplitudeLabel = new PamAxis(0, 0, 0, 0,
//					0,
//					0, true, "amplitude (dB)", "%3.0f");
			setWestAxis(amplitudeAxis);
//			setWestAxis(amplitudeLabel);
			amplitudeAxis.overrideAxisColour(Color.WHITE);
//			amplitudeLabel.overrideAxisColour(Color.WHITE);
			setOpaque(false);
			SetBorderMins(10, 0, 10, 10);
		
		}
		
		public PamAxis getAmplitudeAxis() {
			return amplitudeAxis;
		}

		public ColourRangeSlider getColourRangeSlider() {
			return colourRangeSlider;
		}

//		public PamAxis getAmplitudeLabel() {
//			return amplitudeLabel;
//		}

		public void setAmplitudeAxis(PamAxis amplitudeAxis) {
			this.amplitudeAxis = amplitudeAxis;
		}

		public void setColourRangeSlider(ColourRangeSlider colourRangeSlider) {
			this.colourRangeSlider = colourRangeSlider;
		}

//		public void setAmplitudeLabel(PamAxis amplitudeLabel) {
//			this.amplitudeLabel = amplitudeLabel;
//		}

	}
	
	private class FrequencyPanel extends PamAxisPanel {
		
//		private PamAxis frequencyLabel;
		private PamAxis frequencyAxis;
		private PamRangeSlider pamRangeSlider;
		private static final int sliderRange=100;


		FrequencyPanel(int min, int max) {
			super();
			setLayout(new BorderLayout());
			add(pamRangeSlider=new PamRangeSlider(0,sliderRange,PamRangeSlider.VERTICAL), BorderLayout.CENTER);
			pamRangeSlider.addChangeListener(new FrequencyRangeListener());
			pamRangeSlider.setTrackDragging(true);
			pamRangeSlider.setThumbSizes(15);
			pamRangeSlider.setOpaque(false);
			frequencyAxis = new PamAxis(0, 200, 0, 0,
					min,
					max, true, "Frequency (kHz)", "%3.0f");
			
//			frequencyLabel = new PamAxis(0, 0, 0, 0,
//					0,
//					0, true, "Frequency (kHz)", "%3.0f");
			setWestAxis(frequencyAxis);
//			setWestAxis(frequencyLabel);
			frequencyAxis.overrideAxisColour(Color.WHITE);
//			frequencyLabel.overrideAxisColour(Color.WHITE);
			setOpaque(false);
			SetBorderMins(10, 0, 10, 10);
		}
		
//		public PamAxis getFrequencyLabel() {
//			return frequencyLabel;
//		}

		public PamAxis getFrequencyAxis() {
			return frequencyAxis;
		}


//		public void setFrequencyLabel(PamAxis frequencyLabel) {
//			this.frequencyLabel = frequencyLabel;
//		}

		public void setFrequencyAxis(PamAxis frequencyAxis) {
			this.frequencyAxis = frequencyAxis;
		}
		
		public PamRangeSlider getPamRangeSlider() {
			return pamRangeSlider;
		}

	}
	
	private class AmplitudeRangeListener implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (getAmplRangeSlider().getValueIsAdjusting()){
				changeAmplitudeParams(spectrogramDsiplay.getSpectrogramParameters(),(long) 100);
			}
			else changeAmplitudeParams(spectrogramDsiplay.getSpectrogramParameters(),null);
		}
		
	}

	private class FrequencyRangeListener implements ChangeListener{

		@Override
		public void stateChanged(ChangeEvent arg0) {
				changeFrequencyParams(spectrogramDsiplay.getSpectrogramParameters());
		}
		
	}
	

	public PamRangeSlider getFrequencyRangeSlider() {
		return frequencyPanel.getPamRangeSlider();
	}

	@Override
	public String getName() {
		return "Spectrogram control";
	}
	/**
	 * The max frequency of the slider range
	 * @return
	 */
	public double getAbsMaxFrequency(){
		FFTDataBlock fftDataBlock = spectrogramDsiplay.getFFTDataBlock();
		if (fftDataBlock == null) {
			return 1;
		}
		else {
			return fftDataBlock.getSampleRate()/2.;
		}
	}
	
	/**
	 * The min frequency of the slider range
	 * @return
	 */
	public double getAbsMinFrequency(){
		return 0.0; 
	}
	
	/**
	 * The currently selected max frequency of the slider. 
	 * @return
	 */
	public double getUpperFrequency(){
		double rangeFraction=(((double) (getFrequencyRangeSlider().getUpperValue())/(double) (FrequencyPanel.sliderRange)));
		double frequencyRange=getAbsMaxFrequency()-getAbsMinFrequency();
		double upper=(rangeFraction*frequencyRange)+getAbsMinFrequency(); 
//		System.out.println(" frequencyRange: "+frequencyRange+ " rangeFraction: "+rangeFraction+" upper: "+upper);

		return Math.round(upper * 10) / 10;
	}
	
	/**
	 * The currently selected min frequency of the slider. 
	 * @return
	 */
	public double getLowerFrequency(){
		double lower=(((double) (getFrequencyRangeSlider().getValue())/(double) (FrequencyPanel.sliderRange))*(getAbsMaxFrequency()-getAbsMinFrequency()))+getAbsMinFrequency(); 
		return Math.round(lower * 10) / 10;
	}
	
	
	/*
	 *Set the maximum and minimum range iof the slider bar  
	 *@param min - min frequency kHz
	 *@param max - max frequency kHz
	 */
	public void setFrequencyAbsoluteRange(double min, double max){
		
		if (max>convertTokHz){
			min=min/1000;
			max=max/1000;
			frequencyPanel.frequencyAxis.setLabel("Frequency (kHz)");
		}
		else{
			frequencyPanel.frequencyAxis.setLabel("Frequency (Hz)");
		}
		//set the axis
		frequencyPanel.getFrequencyAxis().setMinVal(min);
		frequencyPanel.getFrequencyAxis().setMaxVal(max);

	}
	
	/**
	 * Set the slider frequecy range 
	 * @param minSliderFreq - min frequency Hz of the slider thumbs
	 * @param maxSliderFreq - max frequency Hz of the slider thumbs
	 * @param minFreq - absolute min frequency Hz of the slider
	 * @param maxFreq - absolute max frequency Hz of the slider
	 */
	public void setFrequencySliderRange(double minSliderFreq, double maxSliderFreq, double minFreq, double maxFreq){
		
		if (maxFreq>convertTokHz){
			minSliderFreq=minSliderFreq/1000;
			maxSliderFreq=maxSliderFreq/1000;
			minFreq=minFreq/1000;
			maxFreq=maxFreq/1000; 
		}
		
		double rangeFreq=maxFreq-minFreq;
		int minThumbPos=(int) (FrequencyPanel.sliderRange*((minSliderFreq-minFreq)/rangeFreq));
		int maxThumbPos=(int) (FrequencyPanel.sliderRange*((maxSliderFreq-minFreq)/rangeFreq));
		getFrequencyRangeSlider().setValue(minThumbPos);
		getFrequencyRangeSlider().setUpperValue(maxThumbPos);
	}
	
	/**
	 * Set the slider amplitude range 
	 * @param minSliderAmp - min amplitude dB of the slider thumbs
	 * @param maxSliderAmp - max amplitude dB of the slider thumbs
	 * @param minAmp - absolute min amplitude dB of the slider
	 * @param maxAmp - absolute max amplitude dB of the slider
	 */
	public void setAmplitudeSliderRange(double minSliderAmp, double maxSliderAmp, double minAmp, double maxAmp){
		
		double rangeAmp=maxAmp-minAmp;
		int minThumbPos=(int) (AmplitudePanel.sliderRange*((minSliderAmp-minAmp)/rangeAmp));
		int maxThumbPos=(int) (AmplitudePanel.sliderRange*((maxSliderAmp-minAmp)/rangeAmp));
		getAmplRangeSlider().setValue(minThumbPos);
		getAmplRangeSlider().setUpperValue(maxThumbPos);
		
	}
	
	/*
	 *Set the maximum and minimum range iof the slider bar  
	 *@param min - min amplitude dB
	 *@param max - max amplitude dB
	 */
	public void setAmplitudeAbsoluteRange(double min, double max){
		//set the axis
		amplitudePanel.getAmplitudeAxis().setMinVal(min);
		amplitudePanel.getAmplitudeAxis().setMaxVal(max);
	}
	
	public ColourRangeSlider getAmplRangeSlider() {
		return amplitudePanel.getColourRangeSlider();
	}
	
	/**
	 * Get the absolute maximum of the slider bar in dB
	 * @return the absolute maximum of the slid3er bar in dB.
	 */
	public double getAbsMinAmplitude(){
		return amplitudePanel.getAmplitudeAxis().getMinVal();
	}
	

	/**
	 * Get the absolute maximum of the slider bar in dB
	 * @return the absolute maximum of the slid3er bar in dB.
	 */
	public double getAbsMaxAmplitude(){
		return amplitudePanel.getAmplitudeAxis().getMaxVal();
	}
	
	/**
	 * Get the upper slider thumb of the amplitude bar in dB
	 * @return upper thumb position in dB
	 */
	public double getAmplitudeUpper(){
		double rangeFraction=((double) (getAmplRangeSlider().getUpperValue())/(double) (AmplitudePanel.sliderRange));
		double ampRange=getAbsMaxAmplitude()-getAbsMinAmplitude();
		double upper=(rangeFraction*ampRange)+getAbsMinAmplitude(); 
//		System.out.println(" AmpRange: "+ampRange+ " rangeFraction: "+rangeFraction+" upper: "+upper);

		return upper;
	}

	/**
	 * Get the lower thumb position for amplitude slider in dB
	 * @return lower thumb position in dB
	 */
	public double getAmplitudeLower(){
		double rangeFraction=((double) (getAmplRangeSlider().getValue())/(double) (AmplitudePanel.sliderRange));
		double ampRange=getAbsMaxAmplitude()-getAbsMinAmplitude();
		double lower=(rangeFraction*ampRange)+getAbsMinAmplitude(); 		
		return lower;
	}
	
	private class HidingPanelButton extends JButton {
		
		Color background=new Color(222,222,222,250);
		Color highlight=new Color(0,222,222,180);
		
		public HidingPanelButton(String string, ImageIcon imageIcon) {
			super(string,imageIcon);
//			this.setOpaque(false);
			this.setContentAreaFilled(false);

		}

		public HidingPanelButton(ImageIcon imageIcon) {
			super(imageIcon);
			this.setContentAreaFilled(false);
		}

		@Override
		public void paintComponent(Graphics g) {
				if (getModel().isRollover()){
					g.setColor(highlight);
				}
				else{
					g.setColor(background);
				}
		        Rectangle r = g.getClipBounds();
		        g.fillRect(r.x, r.y, r.width, r.height);
		        super.paintComponent(g);
		}
		
	}


	@Override
	public JComponent getComponent() {
		return panel;
	}

	@Override
	public boolean canHide() {
		return true;
	}

	@Override
	public void showComponent(boolean visible) {
		setParams(spectrogramDsiplay.getSpectrogramParameters());
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#hasMore()
	 */
	@Override
	public boolean hasMore() {
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#showMore(PamView.hidingpanel.HidingDialog)
	 */
	@Override
	public boolean showMore(HidingDialog hidingDialog) {
		SpectrogramParameters newParams = SpectrogramParamsDialog.showDialog(null, null, spectrogramDsiplay.getSpectrogramParameters());
		if (newParams != null) {
			spectrogramDsiplay.setSpectrogramParameters(newParams);
			notifyChangeListeners(HidingDialogChangeListener.MORE_DIALOG_CLOSED, newParams);
			return true;
		}
		return false;
	}
	
	@Override
	public Icon getIcon(){
		return tabIcon; 
	}
	

}
