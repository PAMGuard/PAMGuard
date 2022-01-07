package dataPlotsFX.scrollingPlot2D;

import java.io.Serializable;

import PamController.PamController;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

public class PlotParams2D implements Serializable, Cloneable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	/**
	 * Amplitude limits- only used when serializing;
	 */
	public double[] amplitudeLimitsSerial={50,120};

	/**
	 * The maximum limits of the amplitude slider bar. 
	 */
	public double[] maxAmplitudeLimits={30,180}; 
	

	/**
	 * @return the amplitudeLimitsSerial
	 */
	public double[] getAmplitudeLimitsSerial() {
		if (amplitudeLimitsSerial == null) {
			double[] al = {50,120};
			amplitudeLimitsSerial = al;
		}
		return amplitudeLimitsSerial;
	}

	/**
	 * @return the maxAmplitudeLimits
	 */
	public double[] getMaxAmplitudeLimits() {
		if (maxAmplitudeLimits == null) {
			//TODO
			double[] mal = {0,180};
			maxAmplitudeLimits = mal;
		}
		return maxAmplitudeLimits;
	}

	/**
	 * @return the amplitudeLimits
	 */
	public DoubleProperty[] getAmplitudeLimits() {
		if (amplitudeLimits == null) { 
			double[] al = getAmplitudeLimitsSerial();
			DoubleProperty[] dp = {new SimpleDoubleProperty(al[0]), new SimpleDoubleProperty(al[1])};
			amplitudeLimits = dp;
		
		}
		return amplitudeLimits;
	}

	/**
	 * Limits of amplitude range- this is a double property which can be bound to colour sliders.
	 */
	private transient DoubleProperty[] amplitudeLimits= {new SimpleDoubleProperty(amplitudeLimitsSerial[0]), new SimpleDoubleProperty(amplitudeLimitsSerial[1])};
	
	public PlotParams2D() {
		super();
		//make sure the amplitude limits are set to the correct air/water to begin with. 
		maxAmplitudeLimits = PamController.getInstance().getGlobalMediumManager().getDefaultdBHzScales(); 
		// TODO Auto-generated constructor stub
	}

	/**
	 * Setup the double property for amplitude- use for creating from serialized settings 
	 */
	public void createAmplitudeProperty(){
		if (amplitudeLimitsSerial == null) {
			amplitudeLimitsSerial = new PlotParams2D().amplitudeLimitsSerial;
		}
		amplitudeLimits=new SimpleDoubleProperty[2];
		amplitudeLimits[0]=new SimpleDoubleProperty(amplitudeLimitsSerial[0]);
		amplitudeLimits[1]=new SimpleDoubleProperty(amplitudeLimitsSerial[1]);
	}

	/**
	 * Type of colour scheme for spectrogram. 
	 */
	private ColourArrayType colourMap = ColourArrayType.RED;

	/**
	 * The color of the wrap line on the spectrogram. 
	 */
	private transient Color wrapColor=Color.LIGHTGREEN;

	/**
	 * Get the colormap of the spectrogram 
	 * @return the colourmap of the spectrogram. 
	 */
	public ColourArrayType getColourMap() {
		if (colourMap == null) {
			colourMap = ColourArrayType.RED;
		}
		return colourMap;
	}

	/**
	 * Get the color of the wrap line (used if spectrogram wraps.)
	 * @return the colour of the wrap line
	 */
	public Color getWrapColour() {
		if (wrapColor == null) {
			setWrapLineColor(colourMap);
		}
		return wrapColor;
	}

	public void setColourMap(ColourArrayType colourMap) {
		if (colourMap == null) {
			colourMap = ColourArrayType.RED;
		}
		this.colourMap=colourMap;
	}

	/**
	 * Set the colort of the wrap line corresponding to the colourmap of the spectrogram. 
	 * @param colourMap - the colour map of the spectrogram. 
	 */
	public void setWrapLineColor(ColourArrayType colourMap){
		switch (colourMap){
		case BLUE:
			this.wrapColor = Color.RED;
			break;
		case GREEN:
			this.wrapColor = Color.RED;
			break;		
		case GREY:
			this.wrapColor = Color.RED;
			break;		
		case HOT:
			this.wrapColor = Color.WHITE;
			break;		
		case RED:
			this.wrapColor = Color.LIGHTGREEN;
			break;
		case REVERSEGREY:
			this.wrapColor = Color.RED;
			break;		
		default:
			this.wrapColor = Color.LIGHTGREEN;
			break;		
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
