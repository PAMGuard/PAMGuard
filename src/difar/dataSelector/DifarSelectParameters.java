package difar.dataSelector;

import generalDatabase.lookupTables.LookupList;

import java.io.Serializable;
import java.util.Arrays;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamUtils;
import PamguardMVC.dataSelector.DataSelectParams;

public class DifarSelectParameters extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	public double minFreq, maxFreq;
	public double minAmplitude;
	public double minLengthMillis;
	public LookupList speciesList;
	public boolean[] speciesEnabled = null;
	public boolean[] channelEnabled = null;
	public boolean crossBearings;
	public int numChannels;

	/**
	 * Replaced by {@link #bearingsShown}. Kept so that settings saved with
	 * "Crosses only" ticked can be read; see {@link #upgrade()}.
	 */
	public boolean showOnlyCrossBearings;

	/** Show every bearing, in a triangulation or not. */
	public static final int BEARINGS_ALL = 0;

	/** Show only bearings that are part of a triangulation. */
	public static final int BEARINGS_TRIANGULATED = 1;

	/** Which bearings to show: one of the BEARINGS_ values. */
	public int bearingsShown = BEARINGS_ALL;

	/**
	 * Whether to hide bearings in a triangulation that lies on one of its
	 * own sonobuoys, where its location cannot be trusted.
	 */
	public boolean hideOnBuoyBearings = false;

	/**
	 * Parameters for the DIFAR data selector.
	 * @param speciesList
	 */
	public DifarSelectParameters(LookupList speciesList, int channelBitmap){
		this.speciesList = speciesList;
		int numSpecies = speciesList.getSelectedList().size();
		
		// All species enabled by default;
		this.speciesEnabled = new boolean[numSpecies];
		for (int i = 0; i < numSpecies; i++){
			this.speciesEnabled[i] = true;
		}
		
		// All channels enabled by default;
		this.numChannels = PamUtils.getNumChannels(channelBitmap);
		this.channelEnabled = new boolean[numChannels];
		for (int i = 0; i < numChannels; i++) {
			this.channelEnabled[i] = true;
		}
		this.showOnlyCrossBearings = false;
		this.bearingsShown = BEARINGS_ALL;
		this.hideOnBuoyBearings = false;
	}

	/**
	 * Carry settings saved before {@link #bearingsShown} existed: "Crosses
	 * only" ticked becomes "only bearings in a triangulation". A value from
	 * the briefly used third choice becomes the same.
	 */
	public void upgrade() {
		if (showOnlyCrossBearings) {
			if (bearingsShown == BEARINGS_ALL) {
				bearingsShown = BEARINGS_TRIANGULATED;
			}
			showOnlyCrossBearings = false;
		}
		if (bearingsShown != BEARINGS_ALL && bearingsShown != BEARINGS_TRIANGULATED) {
			bearingsShown = BEARINGS_TRIANGULATED;
		}
	}
	
	//TODO: Add classification type
	@Override
	public DifarSelectParameters clone()  {
		try {
			return (DifarSelectParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Make sure every channel in a channel map has a setting, indexed by
	 * channel number. Channels new to these parameters are shown. Saved
	 * parameters made when fewer channels were in use grow to fit.
	 * @param channelMap the channels to cover.
	 */
	public void coverChannels(int channelMap) {
		int needed = PamUtils.getHighestChannel(channelMap) + 1;
		boolean[] enabled = channelEnabled == null ? new boolean[0] : channelEnabled;
		if (enabled.length < needed) {
			int old = enabled.length;
			enabled = Arrays.copyOf(enabled, needed);
			Arrays.fill(enabled, old, needed, true);
		}
		channelEnabled = enabled;
		numChannels = channelEnabled.length;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
