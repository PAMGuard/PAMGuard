package whistleClassifier;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;

public class WhistleClassificationDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements AcousticDataUnit {
	
	private double[] speciesLogLikelihoods;
	
	private double[] speciesProbabilities;
	
	private int nFragments;
	
	private String species;

	public WhistleClassificationDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

//	public double[] getSpeciesLikelihoods() {
//		return speciesLogLikelihoods;
//	}
//
//	public void setSpeciesLikelyhoods(double[] speciesLogLikelihoods) {
//		this.speciesLogLikelihoods = speciesLogLikelihoods;
//	}

	public double[] getSpeciesProbabilities() {
		return speciesProbabilities;
	}

	public void setSpeciesProbabilities(double[] speciesProbabilities) {
		this.speciesProbabilities = speciesProbabilities;
	}

	public int getNFragments() {
		return nFragments;
	}

	public void setNFragments(int fragments) {
		nFragments = fragments;
	}

}
