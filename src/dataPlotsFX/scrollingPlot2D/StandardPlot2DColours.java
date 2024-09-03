package dataPlotsFX.scrollingPlot2D;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 * Colours for a 2D surface plot between two z limits. 
 * 
 * @author Jamie Macaulay
 *
 */
public class StandardPlot2DColours implements Plot2DColours {
	
	private static final int NCOLOUR_DEFUALT = 256;

	private ColourArrayType colourArrayType=ColourArrayType.HOT; 
	
	private Color[] colourArray=ColourArray.createStandardColourArray(NCOLOUR_DEFUALT, colourArrayType=ColourArrayType.HOT).getColours();
	
	/**
	 * The amplitude limits 
	 */
	private DoubleProperty[] amplitudeLimits= new DoubleProperty[2];

	private PlotParams2D spectParams;

	/**
	 * 
	 */
	public StandardPlot2DColours(){
		amplitudeLimits[0]=new SimpleDoubleProperty(84); 
		amplitudeLimits[1]=new SimpleDoubleProperty(115); 
	}
	
	/**
	 * 
	 * @param amplitudeLimits2
	 */
	public StandardPlot2DColours(DoubleProperty[] amplitudeLimits2) {
		amplitudeLimits[0]=new SimpleDoubleProperty(84); 
		amplitudeLimits[1]=new SimpleDoubleProperty(115); 
		amplitudeLimits[0].bind(amplitudeLimits2[0]);
		amplitudeLimits[1].bind(amplitudeLimits2[1]);
	}
	
	/*
	 * 
	 */
	public StandardPlot2DColours(PlotParams2D spectParams) {
		amplitudeLimits[0]=new SimpleDoubleProperty(84); 
		amplitudeLimits[1]=new SimpleDoubleProperty(115); 
		DoubleProperty[] al = spectParams.getAmplitudeLimits();
		amplitudeLimits[0].bind(al[0]);
		amplitudeLimits[1].bind(al[1]);
		this.setColourMap(spectParams.getColourMap());
		this.spectParams=spectParams; 
	}

	/**
	 * Constructor for StandardSpecColours
	 * @param colourMap - the colour map. 
	 * @param minAmp - the minimum amplitude.
	 * @param maxAmp - the maximum amplitude.
	 */
	public StandardPlot2DColours(ColourArrayType colourMap, double minAmp, double maxAmp) {
		amplitudeLimits[0]=new SimpleDoubleProperty(minAmp); 
		amplitudeLimits[1]=new SimpleDoubleProperty(maxAmp); 
		this.setColourMap(colourMap);
	}

	@Override
	public Color getWrapColor() {
		return Color.RED;
	}

	/**
	 * Get the index of a colour value. 
	 * @param dBLevel signal level in dB. 
	 * @return colour index (0 - 255)
	 */
	private int getColourIndex(double dBLevel) {
		//System.out.println("Get Colour Index: "+dBLevel);
		// fftMag is << 1
		return getColourIndex( dBLevel,   amplitudeLimits[0].get(),  amplitudeLimits[1].get(), colourArray.length);
	}
	
	/**
	 * Get the index for a colour within a colourmap for a specified dB level.
	 * @param dBlevel - the dB level of the signal. 
	 * @param minAmp - the minimum dB of the colourmap. 
	 * @param maxAmp - the maximum dB of the colourmap. 
	 * @param arrayLen - the length of the colour map. 
	 * @return - the index of the correct colour for the dBLevel. 
	 */
	public static int getColourIndex(double dBLevel, double minAmp, double maxAmp, int arrayLen) {
		double p = arrayLen	* (dBLevel - minAmp)
				/ (maxAmp - minAmp);
		return (int) Math.max(Math.min(p, 255), 0);
	}
	
	/**
	 * Get the colour triplet for a particular db value. 
	 * @param dBLevel
	 * @return colour triplet. 
	 */
	public Color getColours(double dBLevel) {
		return colourArray[getColourIndex(dBLevel)];
	}

	/*
	 * The current colourmap. 
	 */
	public ColourArrayType getColourMap() {
		return this.colourArrayType;
	}
	
	/**
	 * Set the current colour map. 
	 * @param colourMap - the colour map to set.
	 */
	public void setColourMap(ColourArrayType colourMap) {
		this.colourArrayType=colourMap; 
		if (spectParams!=null) spectParams.setColourMap(colourMap);
		ColourArray colourArray = ColourArray.createStandardColourArray(NCOLOUR_DEFUALT, colourArrayType);
		this.colourArray=colourArray.getColours();
	}

	/**
	 * Set the current colour map. 
	 * @param ncolours - the number of colours in the colour map. 
	 * @param colourMap - the colour map to set.
	 */
	public void setColourMap(ColourArrayType colourMap, int ncolours) {
		this.colourArrayType=colourMap; 
		if (spectParams!=null) spectParams.setColourMap(colourMap);
		ColourArray colourArray = ColourArray.createStandardColourArray(ncolours, colourArrayType);
		this.colourArray=colourArray.getColours();
	}

	/**
	 * Get the colour for the spectrogram. 
	 * @return
	 */
	public Color[] getColourArray() {
		return this.colourArray;
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public  DoubleProperty[]  getAmplitudeLimits() {
		return this.amplitudeLimits;
	}

}
