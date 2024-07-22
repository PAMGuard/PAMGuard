package clickDetector;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import clickDetector.tdPlots.ClickSymbolOptions;

public class BTDisplayParameters implements Serializable, Cloneable, ManagedParameters {

	static public final long serialVersionUID = 2;
	
	static public final int DISPLAY_BEARING   = 0;
	static public final int DISPLAY_ICI       = 1;
	static public final int DISPLAY_AMPLITUDE = 2;
	static public final int DISPLAY_SLANT = 3;
	
	//make legacy flags the same as the current flags. 
	static public final int COLOUR_BY_TRAIN = ClickSymbolOptions.COLOUR_BY_SUPERDET;
	static public final int COLOUR_BY_SPECIES = ClickSymbolOptions.COLOUR_SPECIAL;
	static public final int COLOUR_BY_TRAINANDSPECIES = ClickSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL;
	static public final int COLOUR_BY_HYDROPHONE = ClickSymbolOptions.COLOUR_HYDROPHONE;

	public static final String[] colourNames = {"Click Train", "Click Type", "Train then Type", "Hydrophone"};
	public static final String[] angleTypeNames = {"Relative to array", "Relative to vessel", "Relative to north"};
	
	/**
	 * Rotation options for angles. These should match the above angleTypeNames. 
	 */
	static public final int ROTATE_TOARRAY = 0; // no rotation, raw angles relative to the array
	static public final int ROTATE_TOVESSEL = 1; // fix pitch and roll, but leave the heading relative to the array
	static public final int ROTATE_TONORTH = 2; // rotate by heading pitch and roll. 
	
	
	// main BT display
	public int VScale = DISPLAY_BEARING;
	public double maxICI = 1;
	public double minICI = 0.001; // used on log scale only. 
	public double[] amplitudeRange = {60, 160};
	public int nBearingGridLines = 1;
	public int nAmplitudeGridLines = 0;
	public int nICIGridLines = 0;
//	public boolean showEchoes = true;
	public int minClickLength = 2, maxClickLength = 12;
	public int minClickHeight = 2, maxClickHeight = 12;
	private double timeRange = 10;
	public int displayChannels = 0;
	public boolean view360;
	public boolean amplitudeSelect = false;
//	public double minAmplitude = 0;
	public boolean showUnassignedICI = false;
//	public boolean showEventsOnly = false;
//	public boolean showANDEvents = true;
	public boolean logICIScale;
	public int angleRotation = ROTATE_TOARRAY;
	
//	/*
//	 * Show all unidentified species.
//	 */
//	public boolean showNonSpecies = true;
	/*
	 * Show identified species
	 */
//	private boolean[] showSpeciesList;
	
	public int colourScheme = COLOUR_BY_TRAIN;
	
	public boolean showKey;
	
	/**
	 * Show little markers for tracked click angles down the sides of the display
	 */
	public boolean trackedClickMarkers = true;

	@Override
	public BTDisplayParameters clone() {
		try {
			return (BTDisplayParameters) super.clone();
		} catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
//		showSpeciesList = null;
		return null;
	}

	/**
	 * @return the timeRange
	 */
	public double getTimeRange() {
		if (timeRange <= 0) {
			timeRange = 20;
		}
		return timeRange;
	}

	/**
	 * @param timeRange the timeRange to set
	 */
	public void setTimeRange(double timeRange) {
		this.timeRange = timeRange;
	}

	/**
	 * @return the showSpeciesList
	 */
//	public boolean getShowSpecies(int speciesIndex) {
//		if (showSpeciesList != null && showSpeciesList.length > speciesIndex) {
//			return showSpeciesList[speciesIndex];
//		}
//		makeShowSpeciesList(speciesIndex);
//		return true;
//	}
//	private void makeShowSpeciesList(int maxIndex) {
//		if (showSpeciesList == null) {
//			showSpeciesList = new boolean[0];
//		}
//		else if (showSpeciesList.length > maxIndex) {
//			return;
//		}
//		int oldLength = showSpeciesList.length;
//		showSpeciesList = Arrays.copyOf(showSpeciesList, maxIndex + 1);
//		for (int i = oldLength; i <= maxIndex; i++) {
//			showSpeciesList[i] = true;
//		}
//	}

	/**
	 * @param showSpeciesList the showSpeciesList to set
	 */
//	public void setShowSpecies(int speciesIndex, boolean showSpecies) {
//		makeShowSpeciesList(speciesIndex);
//		showSpeciesList[speciesIndex] = showSpecies;
//	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
//		try {
//			Field field = this.getClass().getDeclaredField("showSpeciesList");
//			ps.put(new PrivatePamParameterData(this, field) {
//				@Override
//				public Object getData() throws IllegalArgumentException, IllegalAccessException {
//					return showSpeciesList;
//				}
//			});
//		} catch (NoSuchFieldException | SecurityException e) {
//			e.printStackTrace();
//		}
		return ps;
	}

}
