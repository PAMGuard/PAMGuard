package whistlesAndMoans;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import spectrogramNoiseReduction.SpectrogramNoiseSettings;

import PamView.GroupedSourceParameters;


public class WhistleToneParameters extends GroupedSourceParameters implements Serializable, Cloneable, ManagedParameters {

	/* (non-Javadoc)
	 * @see PamView.GroupedSourceParameters#setDataSource(java.lang.String)
	 */
	@Override
	public void setDataSource(String dataSource) {
//		System.out.println("Swap ds from " + getDataSource() + " to " + dataSource);
		super.setDataSource(dataSource);
	}

	static public final long serialVersionUID = 1;
	
	static public final int FRAGMENT_NONE = 0;
	static public final int FRAGMENT_DISCARD = 1;
	static public final int FRAGMENT_FRAGMENT = 2;
	static public final int FRAGMENT_RELINK = 3;
	
	/**
	 * Policy for showing short whistles on the spectrogram. 
	 */
	static public final int SHORT_SHOWALL = 0;
	static public final int SHORT_HIDEALL = 1;
	static public final int SHORT_SHOWGREY = 2;
		
	private int connectType = 8;
		
	private double minFrequency;
	
	private double maxFrequency;
	
	public int minPixels = 20;
	
	public int minLength = 10;
	
	public int maxCrossLength = 5;
	
	public int fragmentationMethod = FRAGMENT_RELINK;
	
	private SpectrogramNoiseSettings specNoiseSettings;
	
	/**
	 * Show the full contour outline - not just the peak line
	 */
	public boolean showContourOutline = false;
	
	/**
	 * Keep short stubs sticking out of main contour. Using -ve of default
	 * so that old parameter sets go to default. Default is
	 * now to remove them. May 2021
	 */
	public boolean keepShapeStubs;
	
	/**
	 * On long spec displays stretch the contour so that the start time 
	 * is correct, but it then uses 1 pixel per fft bin. 
	 */
	public boolean stretchContours = false;
	
	/**
	 * Length of short whistles. 
	 */
	public int shortLength = 0;
	
	public int shortShowPolicy = SHORT_SHOWALL;

	private double mapLineLength = 1000.;
	
	private double backgroundInterval = 10;
	
	/**
	 * @return the connectType
	 */
	public int getConnectType() {
		if (connectType == 0) {
			connectType = 8;
		}
		return connectType;
	}

	/**
	 * @param connectType the connectType to set
	 */
	public void setConnectType(int connectType) {
		if (connectType != 4 && connectType != 8) {
			connectType = 8;
		}
		this.connectType = connectType;
	}


	/**
	 * @return the minFrequency
	 */
	public double getMinFrequency() {
		return minFrequency;
	}

	/**
	 * @param minFrequency the minFrequency to set
	 */
	public void setMinFrequency(double minFrequency) {
		this.minFrequency = minFrequency;
	}

	/**
	 * @return the maxFrequency
	 */
	public double getMaxFrequency(double sampleRate) {
		if (maxFrequency <= 0 || maxFrequency >= sampleRate / 2) {
			maxFrequency = sampleRate / 2;
		}
		return maxFrequency;
	}

	/**
	 * @param maxFrequency the maxFrequency to set
	 */
	public void setMaxFrequency(double maxFrequency) {
		this.maxFrequency = maxFrequency;
	}

	/**
	 * @return the specNoiseSettings
	 */
	public SpectrogramNoiseSettings getSpecNoiseSettings() {
		if (specNoiseSettings == null) {
			specNoiseSettings = new SpectrogramNoiseSettings();
		}
		return specNoiseSettings;
	}

	/**
	 * @param specNoiseSettings the specNoiseSettings to set
	 */
	public void setSpecNoiseSettings(SpectrogramNoiseSettings specNoiseSettings) {
		this.specNoiseSettings = specNoiseSettings;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

	@Override
	public WhistleToneParameters clone() {
		return (WhistleToneParameters) super.clone();
	}

	/**
	 * Interval in seconds between background noise measurements
	 * @return the backgroundInterval
	 */
	public double getBackgroundInterval() {
		if (backgroundInterval == 0) {
			backgroundInterval = 10.;
		}
		return backgroundInterval;
	}

	/**
	 * Interval in seconds between background noise measurements
	 * @param backgroundInterval the backgroundInterval to set
	 */
	public void setBackgroundInterval(double backgroundInterval) {
		this.backgroundInterval = backgroundInterval;
	}

//	/**
//	 * 
//	 * @return length of line to draw on map display in m
//	 */
//	public double getMapLineLength() {
//		if (mapLineLength <= 0) {
//			mapLineLength = 1000;
//		}
//		return mapLineLength;
//	}
//	
//	/**
//	 * 
//	 * set lenght of line to be drawn on map
//	 * @param mapLineLength length of line to be drawn on map
//	 */
//	public void setMapLineLength(double mapLineLength) {
//		this.mapLineLength = mapLineLength;
//	}

}
