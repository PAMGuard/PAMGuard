package dataPlotsFX.spectrogramPlotFX;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataPlotsFX.scrollingPlot2D.PlotParams2D;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;

/**
 * Spectrogram for the dataDisplayFX. 
 * @author Jamie Macaulay	
 *
 */
public class SpectrogramParamsFX  extends PlotParams2D implements Serializable, Cloneable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Frequency limits- only used when serializing;
	 */
	private double[] frequencyLimitsSerial={0,1};

	/**
	 * Frequency limits to display- this is a double property which can be bound to sliders etc. 
	 */
	private transient DoubleProperty[] frequencyLimits={new SimpleDoubleProperty(frequencyLimitsSerial[0]), new SimpleDoubleProperty(frequencyLimitsSerial[0])};

	/**
	 * Setup the double property for frequency- use for creating from serialized settings 
	 */
	public void createFrequencyProperty(){
		frequencyLimits=new SimpleDoubleProperty[2];
		frequencyLimits[0]=new SimpleDoubleProperty(frequencyLimitsSerial[0]);
		frequencyLimits[1]=new SimpleDoubleProperty(frequencyLimitsSerial[1]);
	}

	/**
	 * @return the frequencyLimitsSerial
	 */
	private double[] getFrequencyLimitsSerial() {
		if (frequencyLimitsSerial == null) {
			frequencyLimitsSerial = new SpectrogramParamsFX().frequencyLimitsSerial;
		}
		return frequencyLimitsSerial;
	}
//
//	/**
//	 * @param frequencyLimitsSerial the frequencyLimitsSerial to set
//	 */
//	public void setFrequencyLimitsSerial(double[] frequencyLimitsSerial) {
//		this.frequencyLimitsSerial = frequencyLimitsSerial;
//	}
//
//	/**
//	 * Set the serialised frequency limits 
//	 * @param lowLimit low frequency limit
//	 * @param highLimit high frequency limit
//	 */
//	public void setFrequencyLimitsSerial(double lowLimit, double highLimit) {
//		if (frequencyLimitsSerial == null) {
//			frequencyLimitsSerial = new double[2];
//		}
//		frequencyLimitsSerial[0] = lowLimit;
//		frequencyLimitsSerial[1] = highLimit;
//	}
	
	/**
	 * Set the serializable frequency limits from the values in the
	 * property fields. This function gets called just before everything 
	 * gets saved to the settings files.  
	 */
	public void setFrequencyLimitsSerial() {
		if (frequencyLimitsSerial == null) {
			frequencyLimitsSerial = new SpectrogramParamsFX().frequencyLimitsSerial;
		}
		DoubleProperty[] flimProp = getFrequencyLimits();
		for (int i = 0; i < 2; i++) {
			frequencyLimitsSerial[i] = flimProp[i].doubleValue();
		}
	}

	/**
	 * Get the frequency limits as a settable property. 
	 * @return the frequencyLimits
	 */
	public DoubleProperty[] getFrequencyLimits() {
		if (frequencyLimits == null) {
			double[] fLimSer = getFrequencyLimitsSerial();
			DoubleProperty[] fl = {new SimpleDoubleProperty(fLimSer[0]), new SimpleDoubleProperty(fLimSer[1])};
			frequencyLimits = fl;
		}
		return frequencyLimits;
	}
	
	/**
	 * Get a single frequency limit
	 * @param iLowHigh 0 = ow limit, 1 = high limit, anything else = crash
	 * @return frequency limit property. 
	 */
	public DoubleProperty getFrequencyLimit(int iLowHigh) {
		getFrequencyLimits(); // call just to make sure nothing is null
		return frequencyLimits[iLowHigh];
	}

	@Override
	public SpectrogramParamsFX clone() {	
		//Clone
		try{
			return (SpectrogramParamsFX) super.clone();
		}
		catch (CloneNotSupportedException Ex) {
			System.err.println("Error in serializing data plot FX settings for spectrogram.");
			Ex.printStackTrace();
		}
	
		return null;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
