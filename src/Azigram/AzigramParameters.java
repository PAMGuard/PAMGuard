package Azigram;

import java.io.Serializable;
import java.lang.reflect.Field;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamConstants;
import whistlesAndMoans.WhistleToneParameters;

public class AzigramParameters implements Serializable, ManagedParameters, Cloneable {

	static public final long serialVersionUID = 2;

	String name = "";

	public int channelBitmap;
	
	public GroupedSourceParameters dataSource = new GroupedSourceParameters();
	
	/**
	 * Seconds to average PSD for long-term display or 0 for no averaging 
	 * (parameter from Thode Azigram)
	 */
	public double secAvg = 0;  

	/**
	 * Bearing bias/correction (degrees?)
	 * (parameter from Thode Azigram)
	 */
	// 
	public double[] bearingCorrection = {0, 0}; 
	
	public float outputSampleRate = 6000;
	
	/**
	 * Apply gain (in dB) when displaying on the spectrogram. This is a hack
	 * to help the Azigram look nicer. It allows the user some discretion 
	 * to adjust how Azigram transparency/fade-to-black works. 
	 */
	public float displayGaindB = 0;
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("name");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return name;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}
	
	@Override
	public AzigramParameters clone() {
		try {
			return (AzigramParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public float getOutputSampleRate() {

		return outputSampleRate;
	}

	/**
	 * Possible output-rates for the Azigram output. 
	 * @return
	 */
	public Float[] getOutputRateList() {
		// These all divide 48000 by a power of 2, so will keep FFT length a power of 2
		Float[] pow2rates = {6000f, 3000f, 1500f, 750f, 375f};

		// These include some rates that are more commonly used, but don't all
		// yield FFT lengths that are power of 2
		Float[] possibleRates = {8000f, 6000f, 4000f, 3000f, 2000f, 1500f, 1000f, 750f, 500f, 375f, 250f};
		return pow2rates;
	}

}
