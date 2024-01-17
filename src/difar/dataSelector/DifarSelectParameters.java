package difar.dataSelector;

import generalDatabase.lookupTables.LookupList;

import java.io.Serializable;

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

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}

}
