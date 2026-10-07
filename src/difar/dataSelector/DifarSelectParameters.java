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

	public boolean showOnlyCrossBearings;
	
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
