package PamView.symbol.modifier;

import java.awt.Color;

import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import clickDetector.ClickDetection;
import cpod.CPODClick;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.symbol.PeakFreqOptionsPane;
import pamViewFX.symbol.SymbolModifierPane;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;


/**
 * Peak frequency symbol modifier. Colours a symbol by it's peak frequency. 
 * 
 * @author Jamie Macaulay
 *
 */
public class PeakFreqModifier extends SymbolModifier {

	public final static String PEAK_FREQ_MODIFIER_NAME = "Peak Frequency";

	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, Color.BLACK, Color.BLACK);

	/**
	 * The colour array for frequency 
	 */
	private ColourArray colourArray;

	/**
	 * Peak frequency symbol options. 
	 */
	private PeakFreqSymbolOptions peakFreqSymbolOptions = new PeakFreqSymbolOptions(); 

	/**
	 * The colour array type. 
	 */
	private ColourArrayType colourArrayType;

	/**
	 * JavaFX pane for frequency symbol options. 
	 */
	private PeakFreqOptionsPane peakFreqOptionsPaneFX;

	/**
	 * Swing panel for frequency symbol options
	 */
	private PamDialogPanel peakFreqOptionsPanel;

	public PeakFreqModifier(PamSymbolChooser symbolChooser) {
		super(PEAK_FREQ_MODIFIER_NAME, symbolChooser, SymbolModType.FILLCOLOUR |  SymbolModType.LINECOLOUR );
		checkColourArray();
		setToolTipText("Colour by the peak frequency of the sound");
	}

	public SymbolData modifySymbol(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
		//		 checkColourArray();
		return super.modifySymbol(symbolData, projector, dataUnit); 
	}


	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {		
		return colourByFreq(symbolData, projector, dataUnit);
	}



	/**
	 * Colour the symbol by frequency. 
	 * @param symbolData - the symbol data. 
	 * @param projector - the projector. This handles conversation of data values to screen pixels. 
	 * @param dataUnit - the data unit. 
	 * @return  the symbol data 
	 */
	private SymbolData colourByFreq(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {

		double frequency = Double.NaN;

		if (dataUnit instanceof CPODClick) {

			//A bit of a HACK

			frequency = ((CPODClick) dataUnit).getkHz()*1000; 

			//				System.out.println("Frequency: " + frequency +  " Upper freq: " + peakFreqSymbolOptions.freqLimts[1]  " Lower freq: " + peakFreqSymbolOptions.freqLimts[0]  ); 
		}		
		else if (dataUnit instanceof RawDataHolder) {

			RawDataHolder click = (RawDataHolder) dataUnit;


			double[]  powerSpectrum = click.getDataTransforms().getTotalPowerSpectrum(click.getDataTransforms().getShortestFFTLength());

			int maxIndex = PamUtils.getMaxIndex(powerSpectrum);

			frequency= (maxIndex/(double) powerSpectrum.length)*dataUnit.getParentDataBlock().getSampleRate()/2;

		}
		else {
			return null;
		}

		frequency=(frequency - peakFreqSymbolOptions.freqLimts[0])/(peakFreqSymbolOptions.freqLimts[1]-peakFreqSymbolOptions.freqLimts[0]);

		checkColourArray(); 

		Color freqCol = PamUtilsFX.fxToAWTColor(this.colourArray.getColour(frequency));

		//		System.out.println("Freq colour: " + freqCol.getRed() + "  " + freqCol.getGreen() + "  " + freqCol.getBlue()); 
		symbolData.setFillColor(freqCol);
		symbolData.setLineColor(freqCol);


		return symbolData;
	}



	/**
	 * Check the correct colour array is being used and, if not, change it. 
	 */
	public void checkColourArray() {
		if (this.colourArrayType!=peakFreqSymbolOptions.freqColourArray) {
			colourArray = ColourArray.createStandardColourArray(128, peakFreqSymbolOptions.freqColourArray); 
			this.colourArrayType=peakFreqSymbolOptions.freqColourArray; 
		}
	}

	@Override
	public SymbolModifierPane getOptionsPane() {
		//System.out.println("PEAK FREQ COLOUR ARRAY2: " + peakFreqSymbolOptions.freqColourArray);
		if (this.peakFreqOptionsPaneFX==null) {
			peakFreqOptionsPaneFX = new PeakFreqOptionsPane(this); 
			peakFreqOptionsPaneFX.setParams();
		}
		return peakFreqOptionsPaneFX; 
	}
	
	public PamDialogPanel getDialogPanel() {
		//System.out.println("PEAK FREQ COLOUR ARRAY2: " + peakFreqSymbolOptions.freqColourArray);
		if (this.peakFreqOptionsPanel==null) {
			peakFreqOptionsPanel = new PeakFreqOptionsPanel(this); 
			peakFreqOptionsPanel.setParams();
		}
		return peakFreqOptionsPanel; 
	}

	@Override
	public SymbolModifierParams getSymbolModifierParams() {
		//System.out.println("PEAK FREQ COLOUR ARRAY3: " + peakFreqSymbolOptions.freqColourArray);
		if (peakFreqSymbolOptions==null) peakFreqSymbolOptions = new PeakFreqSymbolOptions(); 
		return peakFreqSymbolOptions;
	}

	@Override
	public void setSymbolModifierParams(SymbolModifierParams symbolModifierParams) {
		if (!(symbolModifierParams instanceof PeakFreqSymbolOptions)) {
			System.err.println("PeakFreqModifier: warning: the saved parameters were not an instance of PeakFreqSymbolOptions"); 
			peakFreqSymbolOptions = new PeakFreqSymbolOptions(); 
			checkColourArray();
			return; 
		}
		else {
			this.peakFreqSymbolOptions = (PeakFreqSymbolOptions) symbolModifierParams;
			//System.out.println("PEAK FREQ COLOUR ARRAY: " + peakFreqSymbolOptions.freqColourArray);
			checkColourArray();
		}

	}


}
